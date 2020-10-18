package dfa;

public class CharClass {
    public static int min = 0;
    public static int max = 0xffff;

    public static int segment(int start, int end) {
        return (start << 16) | end;
    }

    public static int segment(int[] arr) {
        return segment(arr[0], arr[1]);
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
        return printChar(arr[0]) + "-" + printChar(arr[1]);
    }

    //for dot format
    public static String seg2escaped(int seg) {
        int[] arr = desegment(seg);
        String l = printChar(arr[0]);
        String r = printChar(arr[1]);
        if (arr[0] == '"') {
            l = "\\" + l;
        }
        if (arr[1] == '"') {
            r = "\\" + r;
        }
        return l + "-" + r;
    }

    public static String printChar(int chr) {
        if (Character.isAlphabetic(chr) || Character.isDigit(chr) || isPrintableChar((char) chr)) {
            return Character.toString((char) chr);
        }
        return String.format("\\u%04x", chr);//unicode style
    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }


}
