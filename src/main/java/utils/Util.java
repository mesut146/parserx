package utils;

import grammar.ParseException;

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

    //get escaped char to real char
    public static char get(char c) throws ParseException {
        if(escapeMap.containsKey(c)){
           return escapeMap.get(c);
        }
        throw new ParseException("invalid escape character = "+c);
    }

    //make escaped(slash) form
    public static String escape(int val) {
        for (Map.Entry<Character, Character> e : escapeMap.entrySet()) {
            if (e.getValue() == val) {
                return "\\" + (char) e.getKey();
            }
        }
        return Character.toString((char) val);
    }

    //remove string quotes
    public static String trim(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    //convert escaped string to unescaped
    //e.g "a\tb\sc" to "a    b c"
    public static String fromEscaped(String str) throws ParseException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char chr = str.charAt(i);
            if (chr == '\\') {
                char next = str.charAt(i + 1);
                sb.append(get(next));
                i++;
            }
            else {
                sb.append(chr);
            }
        }
        return sb.toString();
    }
}
