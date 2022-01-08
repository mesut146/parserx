package mesut.parserx.dfa.parser;

import java.io.*;
import java.util.*;

public class Lexer {

    static String cMapPacked = "\76\76\10\50\50\4\164\164\5\40\40\7\12\12\54\162\162\12\54\54\13\56\56\25\156\156\15\154\154\20\146\146\24\142\142\34\136\136\27\134\134\51\140\140\32\52\53\42\144\145\43\152\153\44\72\74\22\0\10\3"+
            "\60\71\0\16\37\36\135\134\1\133\133\14\11\11\2\15\15\53\163\163\11\51\51\16\155\155\17\57\57\21\75\75\23\55\55\26\137\137\30\135\135\52\151\151\31\143\143\35\141\141\33\77\100\37\13\14\40\147\150\45"+
            "\157\161\46\173\uffff\50\165\172\47\41\47\6\101\132\41";
    //input -> input id
    static int[] cMap = unpackCMap(cMapPacked);
    //input id -> regex string for error reporting
    static String[] cMapRegex = {"0-9", "]-\\", "\\t", "\\u0000-\\b", "(", "t", "!-'", "\\u0020", ">", "s", "r", ",", "[", "n", ")", "m", "l", "/", ":-<", "=", "f", ".", "-", "^", "_", "i", "`", "a", "b", "c", "\\u000e-\\u001f", "?-@", "\\u000b-\\f", "A-Z", "*-+", "d-e", "j-k", "g-h", "o-q", "u-z", "\\u007b-\\uffff", "\\\\", "]", "\\r", "\\n"};
    int[] skip = {24576};
    int[] accepting = {-1076887554,2047,0};
    //boolean[] after = {$after_list$};
    //id -> token name
    String[] names = {"EOF","NUM","LP","RP","EQ","ARROW","COMMA","BRACKET","START","FINAL","IDENT","nls","ANY","ws","comment"};
    //state->token id
    int[] ids = {0,1,13,12,2,10,10,6,0,3,12,4,10,12,10,10,11,0,10,10,0,
            0,7,14,10,5,10,10,0,10,0,0,10,10,9,10,10,10,8,0,10,
            10};
    static final int INITIAL = 0;
    static final int EOF = 0;
    Reader reader;
    int yypos = 0;//pos in file
    int yyline = 1;
    int yychar;
    public static int bufSize = 100;
    int bufPos = 0;//pos in buffer
    int bufStart = bufPos;
    int bufEnd;
    char[] yybuf = new char[bufSize];
    static String trans_packed = "\55" +
        "\54\0\1\2\2\3\3\4\4\5\5\6\3\7\2\10\3\11\6\12\5\13\7\14\3\15\5\16\11\17\5\20\5\21\12\22\3\23\13\24\14\25\3\26\15\27\3\30\5\31\16\32\3\33\17\34\5\35\5\36\3\37\3\40\3\41\5\42\3\43\5\44\5\45\5\46\5\47\5\50\3\51\3\52\3\53\20\54\20" +
        "\1\0\1" +
        "\2\2\2\7\2" +
        "\53\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\24\51\25\52\26" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\5\23\45\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\0" +
        "\0" +
        "\0" +
        "\1\21\27" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\30\33\22\34\22\35\22" +
        "\1\10\31" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\32\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\33" +
        "\2\53\20\54\20" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\35\34\22\35\22" +
        "\53\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\24\51\25\52\26" +
        "\53\0\36\2\36\3\36\4\36\5\36\6\36\7\36\10\36\11\36\12\36\13\36\14\36\15\36\16\36\17\36\20\36\21\36\22\36\23\36\24\36\25\36\26\36\27\36\30\36\31\36\32\36\33\36\34\36\35\36\36\36\37\36\40\36\41\36\42\36\43\36\44\36\45\36\46\36\47\36\50\36\51\36\52\36\53\36" +
        "\0" +
        "\53\0\27\2\27\3\27\4\27\5\27\6\27\7\27\10\27\11\27\12\27\13\27\14\27\15\27\16\27\17\27\20\27\21\27\22\27\23\27\24\27\25\27\26\27\27\27\30\27\31\27\32\27\33\27\34\27\35\27\36\27\37\27\40\27\41\27\42\27\43\27\44\27\45\27\46\27\47\27\50\27\51\27\52\27\53\27" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\40\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\41\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\42" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\43\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\53\0\24\1\24\2\24\3\24\4\24\5\24\6\24\7\24\10\24\11\24\12\24\13\24\14\24\15\24\16\24\17\24\20\24\21\24\22\24\23\24\24\24\25\24\26\24\27\24\30\24\31\24\32\24\33\24\34\24\35\24\36\24\37\24\40\24\41\24\42\24\43\24\44\24\45\24\46\24\47\24\50\24\51\25\52\26" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\44\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\45\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\5\46\45\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\42\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\50\33\22\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\22\34\22\35\22" +
        "\0" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\22\24\22\30\22\31\22\33\51\34\22\35\22" +
        "\23\0\22\41\22\43\22\44\22\45\22\5\22\46\22\47\22\11\22\12\22\15\22\17\22\20\46\24\22\30\22\31\22\33\22\34\22\35\22";
    static int[][] trans = unpackTrans(trans_packed);

    public Lexer(Reader reader) throws IOException{
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

    void init() throws IOException{
      reader.read(yybuf, 0, bufSize);
    }

    void fill() throws IOException{
      if(bufPos == yybuf.length){
        char[] newBuf = new char[yybuf.length * 2];
        System.arraycopy(yybuf, 0, newBuf, 0, yybuf.length);
        reader.read(newBuf, bufPos, yybuf.length);
        yybuf = newBuf;
      }
    }

    String getText(){
      return new String(yybuf, bufStart, bufPos - bufStart);
    }

    String findExpected(int from){
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < trans[from].length;i++){
            sb.append(cMapRegex[i]);
            sb.append(",");
        }
        return sb.toString();
    }

    Token getEOF(){
        Token res =  new Token(EOF, "");
        res.name = "EOF";
        return res;
    }

    public Token next() throws IOException {
        Token tok = next_normal();
        if(getBit(skip, tok.type)){
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
            if(yychar == EOF){
                curState = -1;
            }else{
                backupState = curState;
                if(cMap[yychar] == -1){
                    throw new IOException(String.format("unknown input=%c(%d) pos=%s line=%d",yychar, yychar, yypos, yyline));
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
                }
                else {
                    throw new IOException(String.format("invalid input=%c(%d) pos=%s line=%d buf='%s' expecting=%s",yychar,yychar,yypos,yyline,getText(),findExpected(backupState)));
                }
            }
            else {
                if (getBit(accepting, curState)) lastState = curState;
                if(yychar == '\n'){
                    yyline++;
                    if(bufPos > 0 && yybuf[bufPos - 1] == '\r'){
                        yyline--;
                    }
                }
                else if(yychar == '\r'){
                    yyline++;
                }
                bufPos++;
                yypos++;
            }
        }
    }
}
