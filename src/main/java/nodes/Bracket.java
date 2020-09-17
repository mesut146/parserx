package nodes;

import dfa.CharClass;
import grammar.ParseException;
import utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//character set
//[a-zA-Z0-9_]
//consist of char or char range
public class Bracket extends Node {

    public NodeList<Node> list = new NodeList<>();
    public List<RangeNode> rangeNodes;
    public boolean negate;//[^abc]
    public boolean debug = false;
    private int pos;

    //return intersection of two ranges
    public static RangeNode intersect(RangeNode r1, RangeNode r2) {
        int l = Math.max(r1.start, r2.start);
        int r = Math.min(r1.end, r2.end);
        if (l > r) {
            return null;
        }
        return new RangeNode(l, r);
    }

    //node is RangeNode
    public void add(RangeNode node) {
        list.add(node);
    }

    public void add(char chr) {
        list.add(new CharNode(chr));
    }

    public void parse(String str) throws ParseException {
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
                list.add(new RangeNode(c, end));
            }
            else {
                list.add(new CharNode(c));
            }
        }
    }

    char readUnicode(String str) throws ParseException {
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
        if (debug) System.out.println("negating " + list);
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
        }
        sort(ranges);
        if (debug) System.out.println("sorted=" + ranges);
        res = mergeRanges(ranges);
        if (debug) System.out.println("merged=" + res);
        ranges.clear();
        ranges.addAll(res);
        res.clear();
        //negate distinc ranges
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
                if (intersect(cur, next) != null) {
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


    void err() throws ParseException {
        throw new ParseException("Invalid character list");
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

    public List<RangeNode> getRanges() {
        if (rangeNodes == null) {
            rangeNodes = new ArrayList<>();
            for (Node node : list) {
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
    public static class CharNode extends Node {
        public char chr;

        public CharNode(char chr) {
            this.chr = chr;
        }

        @Override
        public String toString() {
            return UnicodeUtils.escape(chr);
        }
    }
}
