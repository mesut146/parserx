package nodes;

import dfa.CharClass;
import grammar.ParseException;
import utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//character set
//e.g [a-zA-Z0-9_]
//consist of char or char range
public class Bracket extends NodeList {

    public List<RangeNode> rangeNodes;
    public boolean negate;//[^abc]
    public boolean debug = false;
    private int pos;

    public Bracket(String str){
        parse(str);
    }

    public Bracket() {

    }

    public Bracket(Node... args) {
        super(args);
    }

    public Bracket(List<Node> args) {
        super(args);
    }

    public void add(char chr) {
        add(new CharNode(chr));
    }

    public void parse(String str) {
        //System.out.println(str);
        pos = 0;
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
            if (c == '\\') {//escape
                c = readUnicode(str);
            }

            if (pos < str.length() && str.charAt(pos) == '-') {
                pos++;
                char end = str.charAt(pos++);
                if (end == '\\') {
                    end = readUnicode(str);
                }
                if (pos > end) {
                    throw new RuntimeException(String.format("invalid range %s-%s in: %s", c, end, str));
                }
                add(new RangeNode(c, end));
            }
            else {
                add(new CharNode(c));
            }
        }
    }

    char readUnicode(String str) {
        char c = str.charAt(pos++);
        if (c == 'u') {//unicode
            char c1 = str.charAt(pos++);
            char c2 = str.charAt(pos++);
            char c3 = str.charAt(pos++);
            char c4 = str.charAt(pos++);
            int hex = fromHex(c1) << 12 |
                    fromHex(c2) << 8 |
                    fromHex(c3) << 4 |
                    fromHex(c4);
            c = (char) hex;
        }
        else {
            c = UnicodeUtils.get(c);
        }
        return c;
    }

    int fromHex(char c) {
        if (Character.isDigit(c)) {
            return c - '0';
        }
        if (Character.isLowerCase(c)) {
            return c - 'a' + 10;
        }
        //upper
        return c - 'A' + 10;
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
        if (debug) System.out.println("negating " + this);
        List<RangeNode> res;
        List<RangeNode> ranges;
        ranges = getRanges();
        sort(ranges);
        if (debug) System.out.println("sorted=" + ranges);
        res = mergeRanges(ranges);
        if (debug) System.out.println("merged=" + res);
        ranges.clear();
        ranges.addAll(res);
        res.clear();
        //negate distinct ranges
        int last = CharClass.min;
        for (int i = 0; i < ranges.size(); i++) {
            RangeNode range = ranges.get(i);
            if (range.start < last) {
                //intersect
                last = range.end + 1;
            }
            res.add(new RangeNode(last, range.start - 1));
            last = range.end + 1;
        }
        res.add(new RangeNode(last, CharClass.max));
        if (debug) System.out.println("negated=" + res);
        return res;
    }

    //merge neighbor ranges
    List<RangeNode> mergeRanges(List<RangeNode> ranges) {
        List<RangeNode> res = new ArrayList<>();
        RangeNode cur = null;
        RangeNode next;
        for (int i = 0; i < ranges.size(); i++) {
            if (cur == null) {
                cur = ranges.get(i);
            }
            if (i < ranges.size() - 1) {
                next = ranges.get(i + 1);
                if (RangeNode.intersect(cur, next) != null) {
                    cur = new RangeNode(cur.start, Math.max(cur.end, next.end));
                }
                else {
                    res.add(cur);
                    cur = null;
                }
            }
            else {
                res.add(cur);
                cur = null;
            }
        }
        return res;
    }

    public Bracket optimize() {
        rangeNodes = mergeRanges(getRanges());
        list.clear();
        list.addAll(rangeNodes);
        return this;
    }

    void err(){
        throw new RuntimeException("Invalid character list");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (negate) {
            sb.append("^");
        }
        sb.append(join(""));
        sb.append("]");
        return sb.toString();
    }

    public List<RangeNode> getRanges() {
        if (rangeNodes == null) {
            rangeNodes = new ArrayList<>();
            for (Node node : this) {
                if (node.isChar()) {
                    rangeNodes.add(new RangeNode(node.asChar().chr, node.asChar().chr));
                }
                else {
                    rangeNodes.add(node.asRange());
                }
            }
        }
        return rangeNodes;
    }

    //remove negation
    public Bracket normalize() {
        if (negate) {
            rangeNodes = negateAll();
            negate = false;
        }
        else {
            getRanges();
        }
        return this;
    }

    //single char
    public static class CharNode extends RangeNode {
        public char chr;

        public CharNode(char chr) {
            super(chr, chr);
            this.chr = chr;
        }

        @Override
        public String toString() {
            return UnicodeUtils.escape(chr);
        }
    }
}
