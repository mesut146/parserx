
import java.io.*;
import java.util.*;

public class Lexer {

    static int[] cMap = unpackCMap("\u0028\u0028\u0001\u0029\u0029\u0002\u002b\u002b\u0003\u002d\u002d\u0004\u002a\u002a\u0005\u002f\u002f\u0006\u0030\u0039\u0000");
    int[] skip = {0,0,0,0,0,0,0};
    int[] accepting = {254,0,0,0,0,0,0};
    //state -> token name
    String[] names = {"","NUMBER","LP","RP","PLUS","MINUS","MUL","DIV"};
    //state->token id
    int[] ids = {0,1,2,3,4,5,6,7};
    static final int INITIAL = 0;
    static final int EOF = 0;
    Reader reader;
    int yypos = 0;//pos in file
    int yychar;
    int bufSize = 10000;
    int bufPos = bufSize;//pos in buffer
    int bufStart = bufSize;
    int bufEnd;
    char[] yybuf = new char[bufSize];

    public Lexer(Reader reader) {
        this.reader = reader;
    }

    public Lexer(File file) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(file));
    }

    static boolean getBit(int[] arr, int state) {
        return ((arr[state / 32] >> (state % 32)) & 1) != 0;
    }

    static int[][] unpackTrans(String str, int max) {
        int pos = 0;
        List<int[]> list = new ArrayList<>();
        while (pos < str.length()) {
            int size = str.charAt(pos++);
            int[] arr = new int[max];
            Arrays.fill(arr, -1);
            for (int input = 0; input < size; input++) {
                //input -> target state
                arr[str.charAt(pos++)] = str.charAt(pos++);
            }
            list.add(arr);
        }
        return list.toArray(new int[0][]);
    }

    static int[] unpackCMap(String str){
        int pos = 0;
        int[] arr = new int[0x010FFFF];//covers all code points
        Arrays.fill(arr, -1);//unused chars leads error
        while(pos < str.length()){
            int left = str.charAt(pos++);
            int right = str.charAt(pos++);
            int id = str.charAt(pos++);
            for(int i = left;i <= right;i++){
                arr[i] = id;
            }
      }
      return arr;
    }

    void fill() throws IOException{
      if(bufPos == bufSize){
        char[] newBuf = new char[bufSize];
        System.arraycopy(yybuf, bufStart, newBuf, 0, bufPos - bufStart);
        reader.read(newBuf, bufPos - bufStart, bufSize - bufPos + bufStart);
        yybuf = newBuf;
        bufPos = bufPos - bufStart;
        bufStart = 0;
      }

    }

    String getText(){
      return new String(yybuf, bufStart, bufPos - bufStart);
    }

    public Token next() throws IOException {
        fill();
        int curState = INITIAL;
        int lastState = -1;
        int startPos = yypos;
        yychar = yybuf[bufPos];
        if (yychar == EOF) return new Token(EOF);
        while (true) {
            yychar = yybuf[bufPos];
            if(yychar == EOF){
              curState = -1;
            }else{
              curState = trans[curState][cMap[yychar]];
            }
            if (curState == -1) {
                if (lastState != -1) {
                    if (!getBit(skip, lastState)) {
                        Token token = new Token(ids[lastState], getText());
                        token.offset = startPos;
                        token.name = names[lastState];
                        bufStart = bufPos;
                        return token;
                    }
                    bufStart = bufPos;
                    if (yychar == EOF) break;//return null;
                    if (getBit(skip, lastState)) return next();
                }
                else {
                    throw new IOException("invalid input=" + yychar + " yybuf= " + getText());
                }
            }
            else {
                bufPos++;
                yypos++;
                if (getBit(accepting, curState)) lastState = curState;
            }
        }
        return new Token(EOF);
    }


    static String trans_packed = 
        "\7\0\1\1\2\2\3\3\4\4\5\5\6\6\7" +
        "\1\0\1" +
        "\0" +
        "\0" +
        "\0" +
        "\0" +
        "\0" +
        "\0";
    static int[][] trans = unpackTrans(trans_packed, 7);
}
