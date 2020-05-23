package utils;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<Character, Character> escapeMap = new HashMap<>();

    static {
        escapeMap.put('t', '\t');
        escapeMap.put('b', '\b');
        escapeMap.put('n', '\n');
        escapeMap.put('r', '\r');
        escapeMap.put('f', '\f');
        escapeMap.put('\'', '\'');
        escapeMap.put('"', '\"');
        escapeMap.put('\\', '\\');
        escapeMap.put('s', ' ');//space
    }

    public static char get(char c) {
        return escapeMap.get(c);
    }

    //make escaed(slash) form
    public static String escape(int val) {
        for (Map.Entry<Character, Character> e : escapeMap.entrySet()) {
            if (e.getValue() == val) {
                return "\\" + (char) e.getKey();
            }
        }
        return Character.toString((char) val);
    }

    public static String trim(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static String fromEscaped(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                char next = str.charAt(i + 1);
                sb.append(get(next));
                i++;
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
