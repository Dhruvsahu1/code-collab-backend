package com.codesync.collab.ot;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OTEngine {

    // Per-session operation history: sessionId -> list of operations (index = version)
    private final Map<String, List<Operation>> operationHistory = new ConcurrentHashMap<>();

    public void initSession(String sessionId) {
        operationHistory.put(sessionId, new ArrayList<>());
    }

    public void removeSession(String sessionId) {
        operationHistory.remove(sessionId);
    }

    /**
     * Receives a client operation, transforms it against any concurrent server ops,
     * and returns the transformed operation ready to apply and broadcast.
     */
    public synchronized Operation receive(String sessionId, Operation clientOp) {
        List<Operation> history = operationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        long baseVersion = clientOp.getBaseVersion();
        long currentVersion = history.size();

        // Transform against all operations that happened since the client's base version
        Operation transformed = clientOp;
        for (long i = baseVersion; i < currentVersion; i++) {
            Operation serverOp = history.get((int) i);
            TransformResult result = transform(transformed, serverOp);
            transformed = result.client();
        }

        // Add to history
        history.add(transformed);
        return transformed;
    }

    public long getCurrentVersion(String sessionId) {
        List<Operation> history = operationHistory.get(sessionId);
        return history == null ? 0 : history.size();
    }

    /**
     * Core OT transform: given two concurrent operations A and B (both applied to same document state),
     * produce A' and B' such that apply(apply(doc, A), B') == apply(apply(doc, B), A')
     */
    public TransformResult transform(Operation clientOp, Operation serverOp) {
        List<Operation.Component> clientComps = clientOp.getComponents();
        List<Operation.Component> serverComps = serverOp.getComponents();

        List<Operation.Component> clientPrime = new ArrayList<>();
        List<Operation.Component> serverPrime = new ArrayList<>();

        int ci = 0, si = 0;
        int clientRemaining = 0, serverRemaining = 0;

        while (ci < clientComps.size() || si < serverComps.size()) {
            Operation.Component cc = ci < clientComps.size() ? clientComps.get(ci) : null;
            Operation.Component sc = si < serverComps.size() ? serverComps.get(si) : null;

            // If client inserts, it goes into clientPrime, serverPrime retains over it
            if (cc != null && cc.getType() == Operation.Component.Type.INSERT && clientRemaining == 0) {
                clientPrime.add(Operation.Component.insert(cc.getText()));
                serverPrime.add(Operation.Component.retain(cc.getCount()));
                ci++;
                continue;
            }

            // If server inserts, it goes into serverPrime, clientPrime retains over it
            if (sc != null && sc.getType() == Operation.Component.Type.INSERT && serverRemaining == 0) {
                serverPrime.add(Operation.Component.insert(sc.getText()));
                clientPrime.add(Operation.Component.retain(sc.getCount()));
                si++;
                continue;
            }

            if (cc == null && sc == null) break;

            int cLen = clientRemaining > 0 ? clientRemaining : (cc != null ? cc.getCount() : 0);
            int sLen = serverRemaining > 0 ? serverRemaining : (sc != null ? sc.getCount() : 0);
            int minLen = Math.min(cLen, sLen);

            if (minLen == 0) {
                if (cLen == 0) { ci++; clientRemaining = 0; }
                if (sLen == 0) { si++; serverRemaining = 0; }
                continue;
            }

            Operation.Component.Type cType = cc != null ? cc.getType() : Operation.Component.Type.RETAIN;
            Operation.Component.Type sType = sc != null ? sc.getType() : Operation.Component.Type.RETAIN;

            if (cType == Operation.Component.Type.RETAIN && sType == Operation.Component.Type.RETAIN) {
                clientPrime.add(Operation.Component.retain(minLen));
                serverPrime.add(Operation.Component.retain(minLen));
            } else if (cType == Operation.Component.Type.DELETE && sType == Operation.Component.Type.RETAIN) {
                clientPrime.add(Operation.Component.delete(minLen));
            } else if (cType == Operation.Component.Type.RETAIN && sType == Operation.Component.Type.DELETE) {
                serverPrime.add(Operation.Component.delete(minLen));
            } else if (cType == Operation.Component.Type.DELETE && sType == Operation.Component.Type.DELETE) {
                // Both deleted the same text -- no output needed
            }

            clientRemaining = cLen - minLen;
            serverRemaining = sLen - minLen;
            if (clientRemaining == 0) ci++;
            if (serverRemaining == 0) si++;
        }

        Operation clientResult = new Operation();
        clientResult.setBaseVersion(clientOp.getBaseVersion());
        clientResult.setUserId(clientOp.getUserId());
        clientResult.setComponents(compact(clientPrime));

        Operation serverResult = new Operation();
        serverResult.setBaseVersion(serverOp.getBaseVersion());
        serverResult.setUserId(serverOp.getUserId());
        serverResult.setComponents(compact(serverPrime));

        return new TransformResult(clientResult, serverResult);
    }

    private List<Operation.Component> compact(List<Operation.Component> components) {
        List<Operation.Component> result = new ArrayList<>();
        for (Operation.Component c : components) {
            if (c.getCount() == 0 && (c.getText() == null || c.getText().isEmpty())) continue;
            if (!result.isEmpty()) {
                Operation.Component last = result.get(result.size() - 1);
                if (last.getType() == c.getType()) {
                    if (c.getType() == Operation.Component.Type.INSERT) {
                        last.setText(last.getText() + c.getText());
                        last.setCount(last.getText().length());
                    } else {
                        last.setCount(last.getCount() + c.getCount());
                    }
                    continue;
                }
            }
            result.add(c);
        }
        return result;
    }

    public record TransformResult(Operation client, Operation server) {}
}
