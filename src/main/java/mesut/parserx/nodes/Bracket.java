package mesut.parserx.nodes;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.utils.UnicodeUtils;

import java.util.*;

//character set
//e.g [a-zA-Z0-9_]
//consist of char or char range
public class Bracket extends NodeList {

    public List<Range> ranges;
    public boolean negate;//[^abc]
    public boolean debug = false;
    private int pos;

    public Bracket(String str) {
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
        add(Range.of(chr, chr));
    }

    public void parse(String str) {
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
                add(new Range(c, end));
            }
            else {
                add(c);
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

    public void sort(List<Range> ranges) {
        Collections.sort(ranges, new Comparator<Range>() {
            @Override
            public int compare(Range r1, Range r2) {
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

    public List<Range> negateAll() {
        if (debug) System.out.println("negating " + this);
        List<Range> rangeList = getRanges();
        sort(rangeList);
        if (debug) System.out.println("sorted=" + rangeList);
        List<Range> res = mergeRanges(rangeList);
        if (debug) System.out.println("merged=" + res);
        rangeList.clear();
        rangeList.addAll(res);
        res.clear();
        //negate distinct ranges
        int last = Alphabet.min;
        for (int i = 0; i < rangeList.size(); i++) {
            Range range = rangeList.get(i);
            if (range.start < last) {
                //intersect
                last = range.end + 1;
            }
            res.add(new Range(last, range.start - 1));
            last = range.end + 1;
        }
        res.add(new Range(last, Alphabet.max));
        if (debug) System.out.println("negated=" + res);
        return res;
    }

    //merge neighbor ranges
    List<Range> mergeRanges(List<Range> ranges) {
        sort(ranges);
        List<Range> res = new ArrayList<>();
        Range cur = null;
        Range next;
        for (int i = 0; i < ranges.size(); i++) {
            if (cur == null) {
                cur = ranges.get(i);
            }
            if (i < ranges.size() - 1) {
                next = ranges.get(i + 1);
                if (Range.intersect(cur, next) != null) {
                    cur = new Range(cur.start, Math.max(cur.end, next.end));
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
        ranges = mergeRanges(getRanges());
        list.clear();
        list.addAll(ranges);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Bracket bracket = (Bracket) o;
        return negate == bracket.negate;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + super.hashCode();
        result = 31 * result + Objects.hashCode(negate);
        return result;
    }

    void err() {
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

    public List<Range> getRanges() {
        if (ranges == null) {
            ranges = new ArrayList<>();
            for (Node node : this) {
                ranges.add(node.asRange());
            }
        }
        return ranges;
    }

    //remove negation
    public Bracket normalize() {
        if (negate) {
            ranges = negateAll();
            negate = false;
        }
        else {
            getRanges();
        }
        return this;
    }
}
