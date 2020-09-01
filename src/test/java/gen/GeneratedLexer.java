package gen;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class GeneratedLexer {
    static int[][] inputMap = unpack(
            "\b\u0029\u0029\u0001\u0028\u0028\u0002\u002f\u002f\u0003\u002a\u002a\u0004\u002d\u002d\u0005\u002b\u002b\u0006\u005e\u005e\u0007\u0030\u0039\b" +
                    "\u0000" +
                    "\u0000" +
                    "\u0000" +
                    "\u0000" +
                    "\u0000" +
                    "\u0000" +
                    "\u0000" +
                    "\u0002\u0030\u0039\b\u002e\u002e\t" +
                    "\u0001\u0030\u0039\n" +
                    "\u0001\u0030\u0039\n");
    static int INITIAL = 0;
    static int EOF = -1;
    int[] skip = {0, 0, 0, 0, 0, 0, 0};
    int[] accepting = {1534, 0, 0, 0, 0, 0, 0};
    String[] names = {"null", "RPAREN", "LPAREN", "DIV", "STAR", "MINUS", "PLUS", "POW", "NUMBER", "null", "NUMBER"};
    int[] ids = {0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 8};
    Reader reader;
    int curState;
    int lastState = -1;
    int yypos = 0;
    int yychar;
    boolean backup = false;
    StringBuilder yybuf = new StringBuilder();

    public GeneratedLexer(Reader reader) {
        this.reader = reader;
    }

    public GeneratedLexer(String file) throws FileNotFoundException {
        this.reader = new FileReader(file);
    }

    static boolean getBit(int[] arr, int state) {
        return ((arr[state / 32] >> (state % 32)) & 1) != 0;
    }

    static int[][] unpack(String str) {
        int pos = 0;
        List<int[]> list = new ArrayList<>();
        while (pos < str.length()) {
            char groupLen = str.charAt(pos++);
            int[] arr = new int[groupLen * 3];//left,right,target
            int arrPos = 0;
            for (int i = 0; i < groupLen; i++) {
                arr[arrPos++] = str.charAt(pos++);
                arr[arrPos++] = str.charAt(pos++);
                arr[arrPos++] = str.charAt(pos++);
            }
            list.add(arr);
        }
        return list.toArray(new int[0][]);
    }

    int read() throws IOException {
        if (!backup) {
            yychar = reader.read();
            yypos++;
        }
        return yychar;
    }

    int getState() {
        int[] arr = inputMap[curState];
        for (int i = 0; i < arr.length; i += 3) {
            if (yychar >= arr[i] && yychar <= arr[i + 1]) {
                return arr[i + 2];
            }
        }
        return -1;
    }

    public Token next() throws IOException {
        curState = INITIAL;
        lastState = -1;
        int startPos = yypos;
        read();
        if (yychar == EOF) return null;
        backup = true;
        while (true) {
            read();
            int st = getState();
            if (st == -1) {
                if (lastState != -1) {
                    Token token = null;
                    if (!getBit(skip, lastState)) {
                        token = new Token(ids[lastState], yybuf.toString());
                        token.offset = startPos;
                        token.name = names[lastState];
                        lastState = -1;
                    }
                    curState = 0;
                    backup = true;
                    yybuf.setLength(0);
                    if (token != null) return token;
                    if (yychar == -1) break;
                    if (getBit(skip, lastState)) return next();
                }
                else {
                    throw new IOException("invalid input=" + yychar + " yybuf= " + yybuf);
                }
            }
            else {
                yybuf.append((char) yychar);
                backup = false;
                curState = st;
                if (getBit(accepting, curState)) lastState = curState;
            }
        }
        return null;
    }
}
