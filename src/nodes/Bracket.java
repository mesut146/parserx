package nodes;

import grammar.ParseException;

//lexer or node aka character list
//[a-zA-Z_0-9]
//consist of char,char range
public class Bracket extends Node {

    public NodeList<Node> list = new NodeList<>();
    public boolean negate;//[^abc]

    public void add(Node node) {
        list.add(node);
    }

    public void add(char chr) {
        list.add(new CharNode(chr));
    }

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
            char c = str.charAt(pos);
            if (c == ']') {
                return;
            }
            if (c != '-') {
                if ((pos + 1) < str.length() && str.charAt(pos + 1) == '-') {
                    char end = str.charAt(pos + 2);
                    list.add(new RangeNode(c, end));
                    pos += 3;
                }
                else {
                    list.add(new CharNode(c));
                    pos++;
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
    public boolean hasRange(){
        for(Node n:list.list){
            if(n.isRange()){
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
