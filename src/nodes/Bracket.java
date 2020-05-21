package nodes;

import dfa.CharClass;
import grammar.ParseException;

import java.util.*;

//for lexer or node, aka character list
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
        //System.out.println(str);
        int pos = 0;
        if (str.charAt(pos) != '[') {
            err();
        }
        ++pos;
        if (str.charAt(pos) == '^' || str.charAt(pos) == '!') {
            negate = true;
            pos++;
        }
        while (pos < str.length()) {
            char c = str.charAt(pos);
            pos++;
            if (c == ']') {//end
                return;
            }
            if (c != '-') {
                if (c == '\\') {//escape
                    //System.out.println("old="+str.charAt(pos));
                    c = escapeMap.get(str.charAt(pos));
                    //System.out.println("new="+Integer.toHexString(c));
                    //System.out.println("n="+escape(c));
                    ++pos;
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
    
    public static String escape(int val){
        for(Map.Entry<Character,Character> e:escapeMap.entrySet()){
            if(e.getValue()==val){
                return "\\"+(char)e.getKey();
            }
        }
        return Character.toString((char)val);
    }

    public void sort(List<RangeNode> ranges) {
        Collections.sort(ranges, new Comparator<RangeNode>() {
                @Override
                public int compare(RangeNode r1, RangeNode r2) {
                    if (r1.start < r2.start) {
                        return -1;
                    }
                    if (r1.start == r2.start) {
                        return Integer.compare(r1.end, r2.end);
                    }
                    return 1;
                }
            });
    }

    public List<RangeNode> negateAll() {
        System.out.println("negating "+list);
        List<RangeNode> res = new ArrayList<>();
        List<RangeNode> ranges = new ArrayList<>();
        //negate all ranges
        for (Node node : list) {
            RangeNode range;
            if (node instanceof CharNode) {
                range = new RangeNode(node.asChar().chr, node.asChar().chr);
            }
            else {
                range = node.asRange();
            }
            ranges.add(range);
            //ranges.add(new RangeNode(CharClass.min, range.start - 1));
            //ranges.add(new RangeNode(range.end + 1, CharClass.max));
        }
        sort(ranges);
        System.out.println(ranges);
        //merge the intersections
        for (int i = 0; i < ranges.size(); i++) {
            RangeNode range = ranges.get(i);
            if(i<ranges.size()){
                
            }
            RangeNode next=ranges.get(i + 1);
            if (intersect(range, next) != null) {
                res.add(new RangeNode(range.start, next.end));
            }else{
                res.add(range);
            }
        }
        System.out.println(res);
        ranges.clear();
        ranges.addAll(res);
        res.clear();
        //negate distinc ranges
        int last=CharClass.min;
        for (int i=0;i < ranges.size();i++){
            RangeNode range = ranges.get(i);
            if(range.start<last){
                //intersect
                last=range.end+1;
            }
            res.add(new RangeNode(last, range.start - 1));
            last = range.end + 1;
        }
        res.add(new RangeNode(last, CharClass.max));
        return res;
    }

    //r1 covers r2
    //e.g 5-100, 7,50
    boolean covers(RangeNode r1, RangeNode r2) {
        if (r1.start <= r2.start && r1.end >= r2.end) {
            return true;
        }
        return false;
    }

    RangeNode intersect(RangeNode r1, RangeNode r2) {
        int l = Math.max(r1.start, r2.start);
        int r = Math.min(r1.end, r2.end);
        if (l > r) {
            return null;
        }
        return new RangeNode(l, r);
    }

    /*List<RangeNode> merge(RangeNode r1, RangeNode r2) {

     return null;
     }*/


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


    //single char
    public static class CharNode extends Node {
        public char chr;

        public CharNode(char chr) {
            this.chr = chr;
        }

        @Override
        public String toString() {
            return escape(chr);
        }
    }
}
