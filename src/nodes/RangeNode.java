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
    
    RangeNode unpack(int seg){
        int mask=(1<<16)-1;
        return new RangeNode(seg>>>16,seg&mask);
    }

    @Override
    public String toString() {
        return Bracket.escape(start)+"-"+Bracket.escape(end);
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
                char c=(char)pos;
                ++pos;
                return c;
            }

            @Override
            public void remove() {
            }
        };
    }

}
