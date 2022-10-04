package mesut.parserx.nodes;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.utils.UnicodeUtils;

import java.util.*;

//character set
//e.g [^a-zA-Z0-9_]
//consist of char or char range
public class Bracket extends Node {

    public List<Range> list = new ArrayList<>();
    public List<Range> ranges;
    public boolean negate;
    private int pos;

    public Bracket(String str) {
        parse(str);
    }

    public Bracket() {

    }

    public void add(char chr) {
        list.add(Range.of(chr, chr));
    }

    public void add(Range range) {
        list.add(range);
    }

    public void parse(String str) {
        pos = 0;
        if (str.charAt(pos) != '[') {
            err();
        }
        ++pos;
        if (str.charAt(pos) == '^') {
            negate = true;
            pos++;
        }
        while (pos < str.length()) {
            char c = str.charAt(pos);
            pos++;
            if (c == ']') {//end
                break;
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
                list.add(new Range(c, end));
            }
            else {
                add(c);
            }
        }
        checkIntersecting();
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

    static void sort(List<Range> ranges) {
        ranges.sort((r1, r2) -> {
            if (r1.start < r2.start) {
                return -1;
            }
            if (r1.start == r2.start) {
                return Integer.compare(r1.end, r2.end);
            }
            return 1;
        });
    }

    void checkIntersecting() {
        for (int i = 0; i < list.size(); i++) {
            var ch = list.get(i);
            if (!ch.isValid()) {
                throw new RuntimeException("invalid range: " + ch);
            }
            for (int j = i + 1; j < list.size(); j++) {
                var ch2 = list.get(j);
                if (ch.intersect(ch2)) {
                    throw new RuntimeException(String.format("intersecting ranges: %s %s", ch, ch2));
                }
            }
        }
    }

    //remove negation
    public Bracket normalize() {
        if (!negate) {
            ranges = new ArrayList<>(list);
            return this;
        }
        var merged = merge(list);
        ranges = new ArrayList<>();
        //negate distinct ranges
        int last = Alphabet.min;
        for (var range : merged) {
            if (range.start < last) {
                //intersect
                last = range.end + 1;
            }
            ranges.add(new Range(last, range.start - 1));
            last = range.end + 1;
        }
        ranges.add(new Range(last, Alphabet.max));
        return this;
    }

    //merge neighbour ranges
    public static ArrayList<Range> merge(List<Range> list) {
        sort(list);
        var res = new ArrayList<Range>();
        int pos = 0;
        while (pos < list.size()) {
            Range cur = list.get(pos++);
            if (pos == list.size()) {
                res.add(cur);
                break;
            }
            var next = list.get(pos);
            //end of cur + 1 is start of next, then merge
            if (cur.end + 1 == next.start) {
                while (cur.end + 1 == next.start) {
                    cur = new Range(cur.start, cur.end);
                    cur.end = next.end;
                    pos++;
                    if (pos < list.size()) {
                        next = list.get(pos);
                    }
                }
                res.add(cur);
            }
            else {
                res.add(cur);
            }
        }
        return res;
    }

    public Bracket optimize() {
        ranges = merge(list);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        var bracket = (Bracket) o;
        return negate == bracket.negate && list.equals(bracket.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, negate);
    }

    void err() {
        throw new RuntimeException("Invalid character list");
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("[");
        if (negate) {
            sb.append("^");
        }
        for (var node : list) {
            sb.append(node.toString().replace("]", "\\]"));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitBracket(this, arg);
    }
}
