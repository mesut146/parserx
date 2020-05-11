package nodes;

import grammar.ParseException;

import java.util.HashMap;
import java.util.Map;

//lexer or node aka character list
//[a-zA-Z_0-9]
//consist of char,char range
public class Bracket extends Node {

    public NodeList<Node> list = new NodeList<>();
    public boolean negate;//[^abc]

    static Map<Character, Character> escapeMap = new HashMap<>();

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

    public void add(Node node) {
        list.add(node);
    }

    public void add(char chr) {
        list.add(new CharNode(chr));
    }

    //todo escaped
    public void parse(String str) throws ParseException {
        int pos = 0;
        if (str.charAt(pos++) != '[') {
            err();
        }
        if (str.charAt(pos) == '^' || str.charAt(pos) == '!') {
            negate = true;
            pos++;
        }
        while (pos < str.length()) {
            char c = str.charAt(pos++);
            if (c == ']') {//end
                return;
            }
            if (c != '-') {
                if (c == '\\') {//escape
                    c = escapeMap.get(str.charAt(pos++));
                }
                if (pos < str.length() && str.charAt(pos) == '-') {
                    pos++;
                    char end = str.charAt(pos++);
                    if (end == '\\') {
                        end = escapeMap.get(str.charAt(pos++));
                    }
                    list.add(new RangeNode(c, end));
                }
                else {
                    list.add(new CharNode(c));
                }
            }
            else {
                err();
            }
        }
    }


    void err() throws ParseException {
        throw new ParseException("Invalid character list");
    }

    public boolean hasRange() {
        for (Node n : list.list) {
            if (n.isRange()) {
                return true;
            }

        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (negate) {
            sb.append("^");
        }
        sb.append(list.join(""));
        sb.append("]");
        return sb.toString();
    }


    public static class CharNode extends Node {
        public char chr;

        public CharNode(char chr) {
            this.chr = chr;
        }

        @Override
        public String toString() {
            return Character.toString(chr);
        }
    }
}
