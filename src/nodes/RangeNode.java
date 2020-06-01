package nodes;

import dfa.CharClass;

import java.util.Iterator;
import java.util.Objects;

public class RangeNode extends Node {

    public int start;
    public int end;

    public RangeNode(String s1, String s2) {
        start = s1.charAt(0);
        end = s2.charAt(0);
    }

    public RangeNode(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return CharClass.printChar(start) + "-" + CharClass.printChar(end);
        //return start + "-" + end;
        //return Bracket.escape(start)+"-"+Bracket.escape(end);
    }

    public boolean intersect(RangeNode other) {
        return Bracket.intersect(this, other) != null;
    }

    public boolean same(RangeNode other) {
        return start == other.start && end == other.end;
    }

    public static RangeNode of(int start, int end) {
        return new RangeNode(start, end);
    }

    public boolean isValid() {
        return start <= end;
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

    public Iterator<Character> iterator() {
        return new Iterator<Character>() {
            int pos = start;

            @Override
            public boolean hasNext() {
                return pos <= end;
            }

            @Override
            public Character next() {
                char c = (char) pos;
                ++pos;
                return c;
            }

            @Override
            public void remove() {
            }
        };
    }

}
