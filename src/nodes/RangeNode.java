package nodes;

import java.util.Iterator;

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

    //encode into single int
    public int pack() {
        return (start << 16) | end;
    }

    @Override
    public String toString() {
        return start + "-" + end;
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
                return (char) pos;
            }

            @Override
            public void remove() {
            }


        };
    }

}
