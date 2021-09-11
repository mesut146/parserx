package mesut.parserx.nodes;

import mesut.parserx.utils.UnicodeUtils;

import java.util.Objects;

public class Range extends Node implements Comparable<Range> {

    public int start;
    public int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static Range of(int start, int end) {
        return new Range(start, end);
    }

    public static Range of(int start) {
        return new Range(start, start);
    }

    public static Range intersect(Range r1, Range r2) {
        int l = Math.max(r1.start, r2.start);
        int r = Math.min(r1.end, r2.end);
        if (l > r) {
            return null;
        }
        return new Range(l, r);
    }

    @Override
    public String toString() {
        if (start == end) {
            if (start == '\"' || start == '\'') {
                return "" + (char) start;
            }
            else if (UnicodeUtils.isSpecial(start)) {
                return UnicodeUtils.escapeUnicode(start);
            }
            return UnicodeUtils.printChar(start);
        }
        if (UnicodeUtils.isPrintableChar((char) start) && UnicodeUtils.isPrintableChar((char) end)) {
            return (char) start + "-" + (char) end;
        }
        return UnicodeUtils.escapeUnicode(start) + "-" + UnicodeUtils.escapeUnicode(end);
    }

    public boolean intersect(Range other) {
        return intersect(this, other) != null;
    }

    public boolean isValid() {
        return start <= end;
    }

    public boolean isSingle() {
        return start == end;
    }

    @Override
    public int compareTo(Range other) {
        if (start < other.start) {
            return -1;
        }
        else if (start > other.start) {
            return 1;
        }
        return Integer.compare(end, other.end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return start == range.start &&
                end == range.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

}
