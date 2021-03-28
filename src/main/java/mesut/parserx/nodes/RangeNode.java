package mesut.parserx.nodes;

import mesut.parserx.utils.UnicodeUtils;

import java.util.Objects;

public class RangeNode extends Node implements Comparable<RangeNode> {

    public int start;
    public int end;

    public RangeNode(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static RangeNode of(int start, int end) {
        return new RangeNode(start, end);
    }

    public static RangeNode of(int start) {
        return new RangeNode(start, start);
    }

    @Override
    public String toString() {
        if (start == end) {
            return UnicodeUtils.printChar(start);
        }
        return UnicodeUtils.printChar(start) + "-" + UnicodeUtils.printChar(end);
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

    public boolean isValid() {
        return start <= end;
    }

    public boolean isSingle() {
        return start == end;
    }

    @Override
    public int compareTo(RangeNode other) {
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
        RangeNode rangeNode = (RangeNode) o;
        return start == rangeNode.start &&
                end == rangeNode.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

}
