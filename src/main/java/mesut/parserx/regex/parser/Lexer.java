package mesut.parserx.regex.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {

    static final int INITIAL = 0;
    static final int EOF = 0;
    public static int bufSize = 100;
    static String cMapPacked = "\56\56\0\50\50\1\174\174\2\52\52\4\136\136\5\134\134\11\54\54\12\12\12\23\0\11\17\100\132\15\51\51\3\53\53\7\135\135\10\55\55\13\133\133\14\77\77\16\175\uffff\6\57\76\21\137\173\20\13\47\22";
    //input -> input id
    static int[] cMap = unpackCMap(cMapPacked);
    //input id -> regex string for error reporting
    static String[] cMapRegex = {".", "(", "|", ")", "*", "^", "\\u007d-\\uffff", "+", "]", "\\\\", ",", "-", "[", "@-Z", "?", "\\u0000-\\t", "_-{", "/->", "\\u000b-\\u0027", "\\n"};
    static String trans_packed = "\24" +
            "\24\0\1\1\2\2\3\3\4\4\5\5\6\6\7\7\10\10\11\11\12\12\7\13\13\14\14\15\7\16\15\17\7\20\7\21\7\22\7\23\7" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\0" +
            "\23\0\16\1\16\2\16\3\16\4\16\5\16\6\16\7\16\10\16\11\16\12\16\13\16\14\16\15\16\16\16\17\16\20\16\21\16\22\16" +
            "\0" +
            "\0" +
            "\0" +
            "\0";
    static int[][] trans = unpackTrans(trans_packed);
    int[] skip = {0};
    int[] accepting = {31742};
    //boolean[] after = {$after_list$};
    //id -> token name
    String[] names = {"EOF", "DOT", "BAR", "BOPEN", "BCLOSE", "LPAREN", "RPAREN", "QUES", "STAR", "PLUS", "XOR", "MINUS", "ESCAPED", "CHAR"};
    //state->token id
    int[] ids = {0, 1, 5, 2, 6, 8, 10, 13, 9, 4, 0, 11, 3, 7, 12};
    Reader reader;
    int yypos = 0;//pos in file
    int yyline = 1;
    int yychar;
    int bufPos = 0;//pos in buffer
    int bufStart = bufPos;
    int bufEnd;
    char[] yybuf = new char[bufSize];

    public Lexer(Reader reader) throws IOException {
        this.reader = reader;
        init();
    }

    public Lexer(File file) throws IOException {
        this.reader = new BufferedReader(new FileReader(file));
        init();
    }

    static boolean getBit(int[] arr, int state) {
        return ((arr[state / 32] >> (state % 32)) & 1) != 0;
    }

    static int[][] unpackTrans(String str) {
        int pos = 0;
        int max = str.charAt(pos++);
        List<int[]> list = new ArrayList<>();
        while (pos < str.length()) {
            int[] arr = new int[max];
            Arrays.fill(arr, -1);
            int trCount = str.charAt(pos++);
            for (int input = 0; input < trCount; input++) {
                //input -> target state
                arr[str.charAt(pos++)] = str.charAt(pos++);
            }
            list.add(arr);
        }
        return list.toArray(new int[0][]);
    }

    static int[] unpackCMap(String str) {
        int pos = 0;
        int[] arr = new int[0x010FFFF];//covers all code points
        Arrays.fill(arr, -1);//unused chars leads error
        while (pos < str.length()) {
            int left = str.charAt(pos++);
            int right = str.charAt(pos++);
            int id = str.charAt(pos++);
            for (int i = left; i <= right; i++) {
                arr[i] = id;
            }
        }
        return arr;
    }

    void init() throws IOException {
        reader.read(yybuf, 0, bufSize);
    }

    void fill() throws IOException {
        if (bufPos == yybuf.length) {
            char[] newBuf = new char[yybuf.length * 2];
            System.arraycopy(yybuf, 0, newBuf, 0, yybuf.length);
            reader.read(newBuf, bufPos, yybuf.length);
            yybuf = newBuf;
        }
    }

    String getText() {
        return new String(yybuf, bufStart, bufPos - bufStart);
    }

    String findExpected(int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < trans[from].length; i++) {
            sb.append(cMapRegex[i]);
            sb.append(",");
        }
        return sb.toString();
    }

    Token getEOF() {
        Token res = new Token(EOF, "");
        res.name = "EOF";
        return res;
    }

    public Token next() throws IOException {
        Token tok = next_normal();
        if (getBit(skip, tok.type)) {
            return next();
        }
        return tok;
    }

    public Token next_normal() throws IOException {
        fill();
        int curState = INITIAL;
        int lastState = -1;
        int startPos = yypos;
        int startLine = yyline;
        yychar = yybuf[bufPos];
        if (yychar == EOF) return getEOF();
        int backupState = -1;
        while (true) {
            fill();
            yychar = yybuf[bufPos];
            if (yychar == EOF) {
                curState = -1;
            } else {
                backupState = curState;
                if (cMap[yychar] == -1) {
                    throw new IOException(String.format("unknown input=%c(%d) pos=%s line=%d", yychar, yychar, yypos, yyline));
                }
                curState = trans[curState][cMap[yychar]];
            }
            if (curState == -1) {
                if (lastState != -1) {
                    Token token = new Token(ids[lastState], getText());
                    token.offset = startPos;
                    token.name = names[ids[lastState]];
                    token.line = startLine;
                    bufStart = bufPos;
                    /*if(!after[ids[lastState]]){
                      curState = INITIAL;
                    }*/
                    return token;
                } else {
                    throw new IOException(String.format("invalid input=%c(%d) pos=%s line=%d buf='%s' expecting=%s", yychar, yychar, yypos, yyline, getText(), findExpected(backupState)));
                }
            } else {
                if (getBit(accepting, curState)) lastState = curState;
                if (yychar == '\n') {
                    yyline++;
                    if (bufPos > 0 && yybuf[bufPos - 1] == '\r') {
                        yyline--;
                    }
                } else if (yychar == '\r') {
                    yyline++;
                }
                bufPos++;
                yypos++;
            }
        }
    }
}
