package com.codesync.collab.ot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    private Long baseVersion;
    private String userId;
    private List<Component> components = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Component {
        private Type type;
        private int count;      // for RETAIN and DELETE: number of characters
        private String text;    // for INSERT: the text to insert

        public enum Type {
            RETAIN, INSERT, DELETE
        }

        public static Component retain(int count) {
            return new Component(Type.RETAIN, count, null);
        }

        public static Component insert(String text) {
            return new Component(Type.INSERT, text.length(), text);
        }

        public static Component delete(int count) {
            return new Component(Type.DELETE, count, null);
        }
    }

    public int inputLength() {
        int len = 0;
        for (Component c : components) {
            if (c.getType() == Component.Type.RETAIN || c.getType() == Component.Type.DELETE) {
                len += c.getCount();
            }
        }
        return len;
    }

    public int outputLength() {
        int len = 0;
        for (Component c : components) {
            if (c.getType() == Component.Type.RETAIN) len += c.getCount();
            else if (c.getType() == Component.Type.INSERT) len += c.getCount();
        }
        return len;
    }
}
