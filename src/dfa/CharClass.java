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
        return printChar(arr[0]) + "-" + printChar(arr[1]);
    }
    
    static String printChar(int chr){
        if(Character.isAlphabetic(chr)|| Character.isDigit(chr)||isPrintableChar((char)chr)){
            return Character.toString((char)chr);
        }
        return "\\"+chr;
    }
    
    public static boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
            block != null &&
            block != Character.UnicodeBlock.SPECIALS;
    }

}
