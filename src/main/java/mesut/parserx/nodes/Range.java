package mesut.parserx.nodes;

import mesut.parserx.utils.UnicodeUtils;

import java.util.Objects;

public class Range extends Node implements Comparable<Range> {

    public int start;
    public int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
        if (start > end) {
            throw new RuntimeException("invalid range: " + this);
        }
    }

    public Range(int start) {
        this(start, start);
    }

    public static Range of(int start, int end) {
        if (start <= end) {
            return new Range(start, end);
        }
        return null;
    }

    public static Range intersect(Range r1, Range r2) {
        int l = Math.max(r1.start, r2.start);
        int r = Math.min(r1.end, r2.end);
        if (l <= r) {
            return new Range(l, r);
        }
        return null;
    }

    @Override
    public String toString() {
        if (start == end) {
            if (start == '\"' || start == '\'') {
                return "\\" + (char) start;
            } else if (UnicodeUtils.isSpecial(start)) {
                return UnicodeUtils.escapeUnicode(start);
            }
            return UnicodeUtils.printChar(start);
        }
        if (UnicodeUtils.isPrintableChar(start) && UnicodeUtils.isPrintableChar(end)) {
            return (char) start + "-" + (char) end;
        }
        return UnicodeUtils.escapeUnicode(start) + "-" + UnicodeUtils.escapeUnicode(end);
    }

    public boolean intersect(Range other) {
        return intersect(this, other) != null;
    }

    public boolean isSingle() {
        return start == end;
    }

    @Override
    public int compareTo(Range other) {
        if (start < other.start) {
            return -1;
        } else if (start > other.start) {
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

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitRange(this, arg);
    }

}
