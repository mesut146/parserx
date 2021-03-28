package mesut.parserx.utils;

import mesut.parserx.grammar.ParseException;

import java.util.HashMap;
import java.util.Map;

public class UnicodeUtils {
    public static Map<Character, Character> escapeMap = new HashMap<>();
    public static Map<Character, Character> escapeMapSimple = new HashMap<>();

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
        //meta chars
        escapeMap.put('*', '*');
        escapeMap.put('+', '+');
        escapeMap.put('?', '?');
        escapeMapSimple.putAll(escapeMap);
        escapeMapSimple.remove('s');
        //escapeMapSimple.remove('\\');
        escapeMapSimple.remove('\'');
        escapeMapSimple.remove('*');
        escapeMapSimple.remove('+');
        escapeMapSimple.remove('?');
    }

    //get escaped char to real char
    public static char get(char c){
        if (escapeMap.containsKey(c)) {
            return escapeMap.get(c);
        }
        throw new RuntimeException("invalid escape character = " + c);
    }

    //make escaped(slash) form
    public static String escape(int val) {
        for (Map.Entry<Character, Character> entry : escapeMap.entrySet()) {
            if (entry.getValue() == val) {
                return "\\" + (char) entry.getKey();
            }
        }
        return Character.toString((char) val);
    }

    //make unicode escaped form
    public static String escapeUnicode(int val) {
        if (isSpecial(val)) {
            return escape(val);
        }
        StringBuilder hex = new StringBuilder(Integer.toHexString(val));
        if (hex.length() < 4) {
            int need = 4 - hex.length();
            for (int i = 0; i < need; i++) {
                hex.insert(0, "0");
            }
        }
        hex.insert(0, "\\u");
        return hex.toString();
    }

    public static boolean isSpecial(int val) {
        return escapeMapSimple.containsValue((char) val);
    }

    //remove string quotes
    public static String trimQuotes(String str) {
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

    public static String escapeString(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (isSpecial(c)) {
                sb.append(escape(c));
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String printChar(int chr) {
        if (Character.isAlphabetic(chr) || Character.isDigit(chr) || isPrintableChar((char) chr)) {
            return Character.toString((char) chr);
        }
        return String.format("\\u%04x", chr);//unicode style
    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }
}
