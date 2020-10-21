package nodes;

import dfa.CharClass;

import java.util.Objects;

public class RangeNode extends Node {

    public int start;
    public int end;

    public RangeNode(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static RangeNode of(int start, int end) {
        return new RangeNode(start, end);
    }

    @Override
    public String toString() {
        if (start == end) {
            return CharClass.printChar(start);
        }
        return CharClass.printChar(start) + "-" + CharClass.printChar(end);
    }


    public static RangeNode intersect(RangeNode r1, RangeNode r2) {
        int l = Math.max(r1.start, r2.start);
        int r = Math.min(r1.end, r2.end);
        if (l > r) {
            return null;
        }
        return new RangeNode(l, r);
    }

    public boolean intersect(RangeNode other) {
        return intersect(this, other) != null;
    }

    public boolean same(RangeNode other) {
        return start == other.start && end == other.end;
    }

    public boolean isValid() {
        return start <= end;
    }

    public boolean isSingle() {
        return start < end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeNode rangeNode = (RangeNode) o;
        return start == rangeNode.start &&
                end == rangeNode.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }


    public int[] toArray() {
        return new int[]{start, end};
    }
}
