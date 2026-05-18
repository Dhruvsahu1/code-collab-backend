package com.codesync.chat.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to manage chat room state (online users, typing users) in memory.
 * Note: For production with multiple instances, consider using Redis or a shared store.
 */
@Component
public class ChatRoomManager {

    // projectId -> Set of userIds (online in that project)
    private final Map<Long, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

    // projectId -> Set of userIds (currently typing in that project)
    private final Map<Long, Set<Long>> typingUsers = new ConcurrentHashMap<>();

    /**
     * Add a user to the online users of a project.
     */
    public void addOnlineUser(Long projectId, Long userId) {
        onlineUsers.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    /**
     * Remove a user from the online users of a project.
     */
    public void removeOnlineUser(Long projectId, Long userId) {
        Set<Long> users = onlineUsers.get(projectId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                onlineUsers.remove(projectId);
            }
        }
    }

    /**
     * Get the set of online user IDs for a project.
     */
    public Set<Long> getOnlineUsers(Long projectId) {
        Set<Long> users = onlineUsers.get(projectId);
        return users != null ? users : ConcurrentHashMap.newKeySet();
    }

    /**
     * Add a user to the typing users of a project.
     */
    public void addTypingUser(Long projectId, Long userId) {
        typingUsers.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    /**
     * Remove a user from the typing users of a project.
     */
    public void removeTypingUser(Long projectId, Long userId) {
        Set<Long> users = typingUsers.get(projectId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                typingUsers.remove(projectId);
            }
        }
    }

    /**
     * Get the set of typing user IDs for a project.
     */
    public Set<Long> getTypingUsers(Long projectId) {
        Set<Long> users = typingUsers.get(projectId);
        return users != null ? users : ConcurrentHashMap.newKeySet();
    }

    /**
     * Clear all data for a project (e.g., when project is deleted).
     */
    public void clearProject(Long projectId) {
        onlineUsers.remove(projectId);
        typingUsers.remove(projectId);
    }
}