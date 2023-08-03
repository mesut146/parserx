package mesut.parserx.dfa.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    static final int DEFAULT = 0;
    static final int EOF = 0;
    public static int bufSize = 100;
    static String cMapPacked = "\76\76\7\50\50\3\164\164\4\40\40\6\12\12\53\162\162\11\54\54\12\56\56\24\156\156\14\154\154\17\146\146\23\142\142\33\136\136\26\134\134\50\140\140\31\52\53\41\144\145\42\152\153\43\72\74\21\0\10\2" +
            "\60\71\0\16\37\35\133\133\13\11\11\1\15\15\52\163\163\10\51\51\15\155\155\16\57\57\20\75\75\22\55\55\25\137\137\27\135\135\51\151\151\30\143\143\34\141\141\32\77\100\36\13\14\37\147\150\44\157\161\45" +
            "\173\uffff\47\165\172\46\41\47\5\101\132\40";
    //input -> input id
    static int[] cMap = unpackCMap(cMapPacked);
    //input id -> regex string for error reporting
    static String[] cMapRegex = {"0-9", "\\t", "\\u0000-\\b", "(", "t", "!-'", "\\u0020", ">", "s", "r", ",", "[", "n", ")", "m", "l", "/", ":-<", "=", "f", ".", "-", "^", "_", "i", "`", "a", "b", "c", "\\u000e-\\u001f", "?-@", "\\u000b-\\f", "A-Z", "*-+", "d-e", "j-k", "g-h", "o-q", "u-z", "\\u007b-\\uffff", "\\\\", "]", "\\r", "\\n"};
    //id -> token name
    static String[] names = {"EOF", "NUM", "LP", "RP", "EQ", "ARROW", "COMMA", "BRACKET", "START", "FINAL", "IDENT", "nls", "ANY", "ws", "comment"};
    //state->token id
    static int[] ids = {0, 1, 13, 12, 2, 10, 10, 6, 12, 3, 12, 4, 10, 12, 10, 10, 11, 1, 10, 10, 0,
            0, 7, 14, 10, 5, 10, 10, 11, 10, 0, 14, 10, 10, 9, 10, 10, 10, 8, 9, 10,
            10, 8};
    static String trans_packed = "\54" +
            "\54\0\1\1\2\2\3\3\4\4\5\5\3\6\2\7\3\10\6\11\5\12\7\13\10\14\5\15\11\16\5\17\5\20\12\21\3\22\13\23\14\24\3\25\15\26\3\27\5\30\16\31\3\32\17\33\5\34\5\35\3\36\3\37\3\40\5\41\3\42\5\43\5\44\5\45\5\46\5\47\3\50\3\51\3\52\20\53\20" +
            "\1\0\21" +
            "\2\1\2\6\2" +
            "\0" +
            "\0" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\4\23\44\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\0" +
            "\52\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\25\51\26" +
            "\0" +
            "\1\20\27" +
            "\0" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\30\32\22\33\22\34\22" +
            "\1\7\31" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\32\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\33" +
            "\2\52\34\53\34" +
            "\1\0\21" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\35\33\22\34\22" +
            "\52\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\25\51\26" +
            "\53\0\36\1\36\2\36\3\36\4\36\5\36\6\36\7\36\10\36\11\36\12\36\13\36\14\36\15\36\16\36\17\36\20\36\21\36\22\36\23\36\24\36\25\36\26\36\27\36\30\36\31\36\32\36\33\36\34\36\35\36\36\36\37\36\40\36\41\36\42\36\43\36\44\36\45\36\46\36\47\36\50\36\51\36\52\36" +
            "\0" +
            "\53\0\37\1\37\2\37\3\37\4\37\5\37\6\37\7\37\10\37\11\37\12\37\13\37\14\37\15\37\16\37\17\37\20\37\21\37\22\37\23\37\24\37\25\37\26\37\27\37\30\37\31\37\32\37\33\37\34\37\35\37\36\37\37\37\40\37\41\37\42\37\43\37\44\37\45\37\46\37\47\37\50\37\51\37\52\37" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\40\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\0" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\41\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\42" +
            "\2\52\34\53\34" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\43\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\52\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\25\51\26" +
            "\53\0\37\1\37\2\37\3\37\4\37\5\37\6\37\7\37\10\37\11\37\12\37\13\37\14\37\15\37\16\37\17\37\20\37\21\37\22\37\23\37\24\37\25\37\26\37\27\37\30\37\31\37\32\37\33\37\34\37\35\37\36\37\37\37\40\37\41\37\42\37\43\37\44\37\45\37\46\37\47\37\50\37\51\37\52\37" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\44\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\4\45\44\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\4\46\44\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\47\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\50\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\51\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\52\23\22\27\22\30\22\32\22\33\22\34\22" +
            "\23\40\22\0\22\42\22\43\22\44\22\4\22\45\22\46\22\10\22\11\22\14\22\16\22\17\22\23\22\27\22\30\22\32\22\33\22\34\22";
    static int[][] trans = unpackTrans(trans_packed);
    int[] skip = {24576};
    int[] accepting = {-1076887554, 2047};
    //acc state -> new mode_state
    int[] mode_map = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Reader reader;
    int yypos = 0;//pos in file
    int yyline = 1;
    int yychar;
    int bufPos = 0;//pos in buffer
    int bufStart = bufPos;
    char[] yybuf = new char[bufSize];
    int curMode = DEFAULT;
    Token lastToken;


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
        res.line = yyline;
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
        int curState = curMode;
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
                    token.name = names[token.type];
                    token.line = startLine;
                    bufStart = bufPos;
                    curMode = mode_map[lastState];
                    lastToken = token;
                    callAction(token, lastState);
                    return token;
                } else {
                    throw new IOException(String.format("invalid input=%c(%d) pos=%s line=%d mode=%s buf='%s' expecting=%s", yychar, yychar, yypos, yyline, printMode(), getText(), findExpected(backupState)));
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

    String printMode() {
        switch (curMode) {
            case 0:
                return "DEFAULT";

        }
        throw new RuntimeException("invalid mode: " + curMode);
    }

    void callAction(Token token, int lastState) {
        switch (lastState) {

        }
    }
}
