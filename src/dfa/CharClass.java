package dfa;

public class CharClass {
    public int start, end;
    public static int min = 0;
    public static int max = 0xffff;

    public static CharClass fromChar(char c) {
        CharClass cc = new CharClass();
        cc.start = cc.end = c;
        return cc;
    }

    public static CharClass fromRange(char c) {
        CharClass cc = new CharClass();
        cc.start = cc.end = c;
        return cc;
    }

    public static int segment(int start, int end) {
        return (start << 16) | end;
    }

    //segment to code points range
    public static int[] desegment(int seg) {
        int end = seg & ((1 << 16) - 1);
        int start = seg >>> 16;
        return new int[]{start, end};
    }

    //segment to printable range
    public static String seg2str(int seg) {
        int[] arr = desegment(seg);
        return (char) arr[0] + "-" + (char) arr[1];
    }

}
