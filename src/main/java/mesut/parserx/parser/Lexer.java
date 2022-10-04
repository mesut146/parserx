package mesut.parserx.parser;

import java.io.*;
import java.util.*;

public class Lexer {
    static String cMapPacked = "\142\142\4\156\156\5\146\146\2\162\162\1\72\72\40\76\76\43\136\136\50\52\52\42\56\56\45\42\42\44\12\12\107\46\46\72\172\172\27\176\176\41\152\152\24\166\167\34\116\132\32\16\37\101\77\77\57\153\153\7"+
            "\137\137\0\133\133\47\73\73\46\53\53\53\57\57\54\47\47\52\43\43\63\143\143\6\157\157\10\163\163\11\173\173\74\147\147\12\13\14\103\140\140\60\74\74\55\100\100\70\134\134\106\54\54\67\40\40\62\44\44\71"+
            "\50\50\65\154\154\14\160\160\16\164\164\17\170\170\23\174\174\76\144\144\21\150\150\22\0\10\77\60\71\35\u03b6\uffff\100\177\u03b4\102\135\134\36\141\141\3\115\115\31\135\135\105\11\11\37\15\15\104\41\41\61\51\51\64"+
            "\45\45\73\55\55\66\75\75\56\165\165\26\171\171\30\u03b5\u03b5\51\175\175\75\151\151\25\145\145\20\155\155\13\161\161\15\101\114\33";
    //input -> input id
    static int[] cMap = unpackCMap(cMapPacked);
    //input id -> regex string for error reporting
    static String[] cMapRegex = {"_", "r", "f", "a", "b", "n", "c", "k", "o", "s", "g", "m", "l", "q", "p", "t", "e", "d", "h", "x", "j", "i", "u", "z", "y", "M", "N-Z", "A-L", "v-w", "0-9", "]-\\", "\\t", ":", "~", "*", ">", "\"", ".", ";", "[", "^", "\\u03b5", "'", "+", "/", "<", "=", "?", "`", "!", "\\u0020", "#", ")", "(", "-", ",", "@", "$", "&", "%", "{", "}", "|", "\\u0000-\\b", "\\u03b6-\\uffff", "\\u000e-\\u001f", "\\u007f-\\u03b4", "\\u000b-\\f", "\\r", "]", "\\\\", "\\n"};
    int[] skip = {0,78};
    int[] accepting = {-44302344,105431033,-1544548289,-1090777152,95};
    //acc state -> new mode_state
    int[] mode_map = {0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    //id -> token name
    static String[] names = {"EOF","BOOLEAN","OPTIONS","TOKEN","INCLUDE","START","EPSILON","LEFT","RIGHT","IDENT","CALL_BEGIN","SHORTCUT","BRACKET","STRING","CHAR","NUMBER","LP","RP","LBRACE","RBRACE",
"STAR","PLUS","QUES","SEPARATOR","TILDE","HASH","COMMA","OR","DOT","SEMI","ARROW","EQ","MINUS","LINE_COMMENT","BLOCK_COMMENT","WS","ACTION","LEXER_MEMBERS_BEGIN","WS1","LEXER_MEMBER",
"MEMBERS_END"};
    //state->token id
    static int[] ids = {0,0,0,38,40,38,39,9,9,9,9,9,9,15,35,23,24,20,0,28,29,
            0,6,0,21,0,31,22,25,17,16,32,26,0,0,18,19,27,9,10,9,
            9,9,9,9,9,0,13,0,0,0,12,0,0,14,0,0,33,30,0,0,
            0,0,0,9,9,9,9,9,9,0,0,0,0,0,0,33,0,0,0,0,
            0,0,0,9,9,9,1,9,9,0,0,0,34,0,36,0,0,0,0,0,
            0,1,9,9,3,9,11,0,0,0,0,0,0,7,9,9,9,36,6,0,
            8,5,2,9,4,0,9,6,9,9,9,9,0,37};
    //final state -> action id
    static int[] actions = new int[135];;
    static final int member_mode = 1;
    static final int DEFAULT = 0;

    static final int EOF = 0;
    Reader reader;
    int yypos = 0;//pos in file
    int yyline = 1;
    int yychar;
    public static int bufSize = 100;
    int bufPos = 0;//pos in buffer
    int bufStart = bufPos;
    char[] yybuf = new char[bufSize];
    static String trans_packed = "\110" +
        "\71\0\7\1\7\2\10\3\7\4\7\5\7\6\7\7\7\10\11\11\7\12\7\13\7\14\12\15\7\16\7\17\13\20\7\21\7\22\7\23\7\24\7\25\14\26\7\27\7\30\7\31\7\32\7\33\7\34\7\35\15\37\16\40\17\41\20\42\21\44\22\45\23\46\24\47\25\51\26\52\27\53\30\54\31\56\32\57\33\62\16\63\34\64\35\65\36\66\37\67\40\70\41\73\42\74\43\75\44\76\45\104\16\107\16" +
        "\106\0\2\1\2\2\2\3\2\4\2\5\2\6\2\7\2\10\2\11\2\12\2\13\2\14\2\15\2\16\2\17\2\20\2\21\2\22\2\23\2\24\2\25\2\26\2\27\2\30\2\31\2\32\2\33\2\34\2\35\2\37\3\40\2\41\2\42\2\43\2\44\2\45\2\47\2\50\2\51\2\52\2\53\2\54\2\55\2\56\2\57\2\60\2\61\2\62\3\63\2\64\2\65\2\66\2\67\2\70\2\71\2\72\2\73\2\74\2\75\4\76\2\77\2\100\2\101\2\102\2\103\2\104\3\105\2\106\2\107\5" +
        "\105\0\2\1\2\2\2\3\2\4\2\5\2\6\2\7\2\10\2\11\2\12\2\13\2\14\2\15\2\16\2\17\2\20\2\21\2\22\2\23\2\24\2\25\2\26\2\27\2\30\2\31\2\32\2\33\2\34\2\35\2\37\2\40\2\41\2\42\2\43\2\44\2\45\2\46\6\47\2\50\2\51\2\52\2\53\2\54\2\55\2\56\2\57\2\60\2\61\2\62\2\63\2\64\2\65\2\66\2\67\2\70\2\71\2\72\2\73\2\74\2\76\2\77\2\100\2\101\2\102\2\103\2\104\2\105\2\106\2" +
        "\106\0\2\1\2\2\2\3\2\4\2\5\2\6\2\7\2\10\2\11\2\12\2\13\2\14\2\15\2\16\2\17\2\20\2\21\2\22\2\23\2\24\2\25\2\26\2\27\2\30\2\31\2\32\2\33\2\34\2\35\2\37\3\40\2\41\2\42\2\43\2\44\2\45\2\46\6\47\2\50\2\51\2\52\2\53\2\54\2\55\2\56\2\57\2\60\2\61\2\62\3\63\2\64\2\65\2\66\2\67\2\70\2\71\2\72\2\73\2\74\2\76\2\77\2\100\2\101\2\102\2\103\2\104\3\105\2\106\2\107\5" +
        "\0" +
        "\4\62\5\104\5\107\5\37\5" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\50\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\51\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\52\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\53\2\46\3\46\4\46\5\46\6\46\7\46\10\54\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\55\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\1\35\15" +
        "\4\62\16\104\16\107\16\37\16" +
        "\0" +
        "\0" +
        "\0" +
        "\105\0\56\1\56\2\56\3\56\4\56\5\56\6\56\7\56\10\56\11\56\12\56\13\56\14\56\15\56\16\56\17\56\20\56\21\56\22\56\23\56\24\56\25\56\26\56\27\56\30\56\31\56\32\56\33\56\34\56\35\56\37\56\40\56\41\56\42\56\43\56\44\57\45\56\46\56\47\56\50\56\51\56\52\56\53\56\54\56\55\56\56\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\70\56\71\56\72\56\73\56\74\56\75\56\76\56\77\56\100\56\101\56\102\56\103\56\105\56\106\60" +
        "\0" +
        "\0" +
        "\106\0\61\1\61\2\61\3\61\4\61\5\61\6\61\7\61\10\61\11\61\12\61\13\61\14\61\15\61\16\61\17\61\20\61\21\61\22\61\23\61\24\61\25\61\26\61\27\61\30\61\31\61\32\61\33\61\34\61\35\61\36\61\37\61\40\62\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\0" +
        "\105\0\65\1\65\2\65\3\65\4\65\5\65\6\65\7\65\10\65\11\65\12\65\13\65\14\65\15\65\16\65\17\65\20\65\21\65\22\65\23\65\24\65\25\65\26\65\27\65\30\65\31\65\32\65\33\65\34\65\35\65\37\65\40\65\41\65\42\65\43\65\44\65\45\65\46\65\47\65\50\65\51\65\52\66\53\65\54\65\55\65\56\65\57\65\60\65\61\65\62\65\63\65\64\65\65\65\66\65\67\65\70\65\71\65\72\65\73\65\74\65\75\65\76\65\77\65\100\65\101\65\102\65\103\65\105\65\106\67" +
        "\0" +
        "\2\42\70\54\71" +
        "\0" +
        "\0" +
        "\0" +
        "\0" +
        "\0" +
        "\1\43\72" +
        "\0" +
        "\1\74\73" +
        "\4\20\74\1\75\11\76\14\77" +
        "\0" +
        "\0" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\100\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\101\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\102\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\103\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\104\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\105\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\105\0\56\1\56\2\56\3\56\4\56\5\56\6\56\7\56\10\56\11\56\12\56\13\56\14\56\15\56\16\56\17\56\20\56\21\56\22\56\23\56\24\56\25\56\26\56\27\56\30\56\31\56\32\56\33\56\34\56\35\56\37\56\40\56\41\56\42\56\43\56\44\57\45\56\46\56\47\56\50\56\51\56\52\56\53\56\54\56\55\56\56\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\70\56\71\56\72\56\73\56\74\56\75\56\76\56\77\56\100\56\101\56\102\56\103\56\105\56\106\60" +
        "\0" +
        "\106\0\106\1\106\2\106\3\106\4\106\5\106\6\106\7\106\10\106\11\106\12\106\13\106\14\106\15\106\16\106\17\106\20\106\21\106\22\106\23\106\24\106\25\106\26\106\27\106\30\106\31\106\32\106\33\106\34\106\35\106\37\106\40\106\41\106\42\106\43\106\44\106\45\106\46\106\47\106\50\106\51\106\52\106\53\106\54\106\55\106\56\106\57\106\60\106\61\106\62\106\63\106\64\106\65\106\66\106\67\106\70\106\71\106\72\106\73\106\74\106\75\106\76\106\77\106\100\106\101\106\102\106\103\106\104\106\105\106\106\106" +
        "\106\0\61\1\61\2\61\3\61\4\61\5\61\6\61\7\61\10\61\11\61\12\61\13\61\14\61\15\61\16\61\17\61\20\61\21\61\22\61\23\61\24\61\25\61\26\61\27\61\30\61\31\61\32\61\33\61\34\61\35\61\36\61\37\61\40\61\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\106\0\107\1\107\2\107\3\107\4\107\5\107\6\107\7\107\10\107\11\107\12\107\13\107\14\107\15\107\16\107\17\107\20\107\21\107\22\107\23\107\24\107\25\107\26\107\27\107\30\107\31\107\32\107\33\107\34\107\35\61\36\61\37\61\40\61\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\0" +
        "\106\0\110\1\110\2\110\3\110\4\110\5\110\6\110\7\110\10\110\11\110\12\110\13\110\14\110\15\110\16\110\17\110\20\110\21\110\22\110\23\110\24\110\25\110\26\110\27\110\30\110\31\110\32\110\33\110\34\110\35\110\37\110\40\110\41\110\42\110\43\110\44\110\45\110\46\110\47\110\50\110\51\110\52\110\53\110\54\110\55\110\56\110\57\110\60\110\61\110\62\110\63\110\64\110\65\110\66\110\67\110\70\110\71\110\72\110\73\110\74\110\75\110\76\110\77\110\100\110\101\110\102\110\103\110\104\110\105\110\106\110" +
        "\105\0\65\1\65\2\65\3\65\4\65\5\65\6\65\7\65\10\65\11\65\12\65\13\65\14\65\15\65\16\65\17\65\20\65\21\65\22\65\23\65\24\65\25\65\26\65\27\65\30\65\31\65\32\65\33\65\34\65\35\65\37\65\40\65\41\65\42\65\43\65\44\65\45\65\46\65\47\65\50\65\51\65\52\66\53\65\54\65\55\65\56\65\57\65\60\65\61\65\62\65\63\65\64\65\65\65\66\65\67\65\70\65\71\65\72\65\73\65\74\65\75\65\76\65\77\65\100\65\101\65\102\65\103\65\105\65\106\67" +
        "\0" +
        "\106\0\111\1\111\2\111\3\111\4\111\5\111\6\111\7\111\10\111\11\111\12\111\13\111\14\111\15\111\16\111\17\111\20\111\21\111\22\111\23\111\24\111\25\111\26\111\27\111\30\111\31\111\32\111\33\111\34\111\35\111\37\111\40\111\41\111\42\111\43\111\44\111\45\111\46\111\47\111\50\111\51\111\52\111\53\111\54\111\55\111\56\111\57\111\60\111\61\111\62\111\63\111\64\111\65\111\66\111\67\111\70\111\71\111\72\111\73\111\74\111\75\111\76\111\77\111\100\111\101\111\102\111\103\111\104\111\105\111\106\111" +
        "\107\0\112\1\112\2\112\3\112\4\112\5\112\6\112\7\112\10\112\11\112\12\112\13\112\14\112\15\112\16\112\17\112\20\112\21\112\22\112\23\112\24\112\25\112\26\112\27\112\30\112\31\112\32\112\33\112\34\112\35\112\37\112\40\112\41\112\42\113\43\112\44\112\45\112\46\112\47\112\50\112\51\112\52\112\53\112\54\112\55\112\56\112\57\112\60\112\61\112\62\112\63\112\64\112\65\112\66\112\67\112\70\112\71\112\72\112\73\112\74\112\75\112\76\112\77\112\100\112\101\112\102\112\103\112\104\112\105\112\106\112\107\112" +
        "\106\0\114\1\114\2\114\3\114\4\114\5\114\6\114\7\114\10\114\11\114\12\114\13\114\14\114\15\114\16\114\17\114\20\114\21\114\22\114\23\114\24\114\25\114\26\114\27\114\30\114\31\114\32\114\33\114\34\114\35\114\37\114\40\114\41\114\42\114\43\114\44\114\45\114\46\114\47\114\50\114\51\114\52\114\53\114\54\114\55\114\56\114\57\114\60\114\61\114\62\114\63\114\64\114\65\114\66\114\67\114\70\114\71\114\72\114\73\114\74\114\75\114\76\114\77\114\100\114\101\114\102\114\103\114\104\114\105\114\106\114" +
        "\0" +
        "\107\0\115\1\115\2\115\3\115\4\115\5\115\6\115\7\115\10\115\11\115\12\115\13\115\14\115\15\115\16\115\17\115\20\115\21\115\22\115\23\115\24\115\25\115\26\115\27\115\30\115\31\115\32\115\33\115\34\115\35\115\37\115\40\115\41\115\42\115\43\115\44\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115\60\115\61\115\62\115\63\115\64\115\65\115\66\115\67\115\70\115\71\115\72\115\73\115\74\115\75\116\76\115\77\115\100\115\101\115\102\115\103\115\104\115\105\115\106\115\107\115" +
        "\2\13\117\16\120" +
        "\1\25\121" +
        "\1\17\122" +
        "\1\20\123" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\124\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\125\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\126\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\127\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\130\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\131\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\105\0\56\1\56\2\56\3\56\4\56\5\56\6\56\7\56\10\56\11\56\12\56\13\56\14\56\15\56\16\56\17\56\20\56\21\56\22\56\23\56\24\56\25\56\26\56\27\56\30\56\31\56\32\56\33\56\34\56\35\56\37\56\40\56\41\56\42\56\43\56\44\57\45\56\46\56\47\56\50\56\51\56\52\56\53\56\54\56\55\56\56\56\57\56\60\56\61\56\62\56\63\56\64\56\65\56\66\56\67\56\70\56\71\56\72\56\73\56\74\56\75\56\76\56\77\56\100\56\101\56\102\56\103\56\105\56\106\60" +
        "\106\0\132\1\132\2\132\3\132\4\132\5\132\6\132\7\132\10\132\11\132\12\132\13\132\14\132\15\132\16\132\17\132\20\132\21\132\22\132\23\132\24\132\25\132\26\132\27\132\30\132\31\132\32\132\33\132\34\132\35\132\36\61\37\61\40\133\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\106\0\61\1\61\2\61\3\61\4\61\5\61\6\61\7\61\10\61\11\61\12\61\13\61\14\61\15\61\16\61\17\61\20\61\21\61\22\61\23\61\24\61\25\61\26\61\27\61\30\61\31\61\32\61\33\61\34\61\35\61\36\61\37\61\40\61\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\105\0\65\1\65\2\65\3\65\4\65\5\65\6\65\7\65\10\65\11\65\12\65\13\65\14\65\15\65\16\65\17\65\20\65\21\65\22\65\23\65\24\65\25\65\26\65\27\65\30\65\31\65\32\65\33\65\34\65\35\65\37\65\40\65\41\65\42\65\43\65\44\65\45\65\46\65\47\65\50\65\51\65\52\66\53\65\54\65\55\65\56\65\57\65\60\65\61\65\62\65\63\65\64\65\65\65\66\65\67\65\70\65\71\65\72\65\73\65\74\65\75\65\76\65\77\65\100\65\101\65\102\65\103\65\105\65\106\67" +
        "\107\0\112\1\112\2\112\3\112\4\112\5\112\6\112\7\112\10\112\11\112\12\112\13\112\14\112\15\112\16\112\17\112\20\112\21\112\22\112\23\112\24\112\25\112\26\112\27\112\30\112\31\112\32\112\33\112\34\112\35\112\37\112\40\112\41\112\42\113\43\112\44\112\45\112\46\112\47\112\50\112\51\112\52\112\53\112\54\112\55\112\56\112\57\112\60\112\61\112\62\112\63\112\64\112\65\112\66\112\67\112\70\112\71\112\72\112\73\112\74\112\75\112\76\112\77\112\100\112\101\112\102\112\103\112\104\112\105\112\106\112\107\112" +
        "\107\0\134\1\134\2\134\3\134\4\134\5\134\6\134\7\134\10\134\11\134\12\134\13\134\14\134\15\134\16\134\17\134\20\134\21\134\22\134\23\134\24\134\25\134\26\134\27\134\30\134\31\134\32\134\33\134\34\134\35\134\37\134\40\134\41\134\42\134\43\134\44\134\45\134\46\134\47\134\50\134\51\134\52\134\53\134\54\135\55\134\56\134\57\134\60\134\61\134\62\134\63\134\64\134\65\134\66\134\67\134\70\134\71\134\72\134\73\134\74\134\75\134\76\134\77\134\100\134\101\134\102\134\103\134\104\134\105\134\106\134\107\134" +
        "\106\0\114\1\114\2\114\3\114\4\114\5\114\6\114\7\114\10\114\11\114\12\114\13\114\14\114\15\114\16\114\17\114\20\114\21\114\22\114\23\114\24\114\25\114\26\114\27\114\30\114\31\114\32\114\33\114\34\114\35\114\37\114\40\114\41\114\42\114\43\114\44\114\45\114\46\114\47\114\50\114\51\114\52\114\53\114\54\114\55\114\56\114\57\114\60\114\61\114\62\114\63\114\64\114\65\114\66\114\67\114\70\114\71\114\72\114\73\114\74\114\75\114\76\114\77\114\100\114\101\114\102\114\103\114\104\114\105\114\106\114" +
        "\107\0\115\1\115\2\115\3\115\4\115\5\115\6\115\7\115\10\115\11\115\12\115\13\115\14\115\15\115\16\115\17\115\20\115\21\115\22\115\23\115\24\115\25\115\26\115\27\115\30\115\31\115\32\115\33\115\34\115\35\115\37\115\40\115\41\115\42\115\43\115\44\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115\60\115\61\115\62\115\63\115\64\115\65\115\66\115\67\115\70\115\71\115\72\115\73\115\74\115\75\116\76\115\77\115\100\115\101\115\102\115\103\115\104\115\105\115\106\115\107\115" +
        "\107\0\136\1\136\2\136\3\136\4\136\5\136\6\136\7\136\10\136\11\136\12\136\13\136\14\136\15\136\16\136\17\136\20\136\21\136\22\136\23\136\24\136\25\136\26\136\27\136\30\136\31\136\32\136\33\136\34\136\35\136\37\136\40\136\41\136\42\136\43\136\44\136\45\136\46\136\47\136\50\136\51\136\52\136\53\136\54\136\55\136\56\136\57\136\60\136\61\136\62\136\63\136\64\136\65\136\66\136\67\136\70\137\71\136\72\136\73\136\74\136\75\140\76\136\77\136\100\136\101\136\102\136\103\136\104\136\105\136\106\136\107\136" +
        "\1\16\141" +
        "\1\11\142" +
        "\1\12\143" +
        "\1\3\144" +
        "\1\2\145" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\146\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\147\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\150\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\151\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\152\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\106\0\132\1\132\2\132\3\132\4\132\5\132\6\132\7\132\10\132\11\132\12\132\13\132\14\132\15\132\16\132\17\132\20\132\21\132\22\132\23\132\24\132\25\132\26\132\27\132\30\132\31\132\32\132\33\132\34\132\35\132\36\61\37\61\40\133\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\63\106\64" +
        "\106\0\61\1\61\2\61\3\61\4\61\5\61\6\61\7\61\10\61\11\61\12\61\13\61\14\61\15\61\16\61\17\61\20\61\21\61\22\61\23\61\24\61\25\61\26\61\27\61\30\61\31\61\32\61\33\61\34\61\35\61\36\61\37\61\40\61\41\61\42\61\43\61\44\61\45\61\46\61\47\61\50\61\51\61\52\61\53\61\54\61\55\61\56\61\57\61\60\61\61\61\62\61\63\61\64\61\65\61\66\61\67\61\70\61\71\61\72\61\73\61\74\61\75\61\76\61\77\61\100\61\101\61\102\61\103\61\105\153\106\64" +
        "\107\0\112\1\112\2\112\3\112\4\112\5\112\6\112\7\112\10\112\11\112\12\112\13\112\14\112\15\112\16\112\17\112\20\112\21\112\22\112\23\112\24\112\25\112\26\112\27\112\30\112\31\112\32\112\33\112\34\112\35\112\37\112\40\112\41\112\42\113\43\112\44\112\45\112\46\112\47\112\50\112\51\112\52\112\53\112\54\112\55\112\56\112\57\112\60\112\61\112\62\112\63\112\64\112\65\112\66\112\67\112\70\112\71\112\72\112\73\112\74\112\75\112\76\112\77\112\100\112\101\112\102\112\103\112\104\112\105\112\106\112\107\112" +
        "\0" +
        "\107\0\115\1\115\2\115\3\115\4\115\5\115\6\115\7\115\10\115\11\115\12\115\13\115\14\115\15\115\16\115\17\115\20\115\21\115\22\115\23\115\24\115\25\115\26\115\27\115\30\115\31\115\32\115\33\115\34\115\35\115\37\115\40\115\41\115\42\115\43\115\44\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115\60\115\61\115\62\115\63\115\64\115\65\115\66\115\67\115\70\115\71\115\72\115\73\115\74\115\75\116\76\115\77\115\100\115\101\115\102\115\103\115\104\115\105\115\106\115\107\115" +
        "\0" +
        "\107\0\154\1\154\2\154\3\154\4\154\5\154\6\154\7\154\10\154\11\154\12\154\13\154\14\154\15\154\16\154\17\154\20\154\21\154\22\154\23\154\24\154\25\154\26\154\27\154\30\154\31\154\32\154\33\154\34\154\35\154\37\154\40\154\41\154\42\154\43\154\44\154\45\154\46\154\47\154\50\154\51\154\52\154\53\154\54\154\55\154\56\154\57\154\60\154\61\154\62\154\63\154\64\154\65\154\66\154\67\154\70\115\71\154\72\154\73\154\74\154\75\155\76\154\77\154\100\154\101\154\102\154\103\154\104\154\105\154\106\154\107\154" +
        "\1\17\156" +
        "\1\25\157" +
        "\1\22\160" +
        "\1\1\161" +
        "\1\17\162" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\163\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\164\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\165\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\0" +
        "\107\0\115\1\115\2\115\3\115\4\115\5\115\6\115\7\115\10\115\11\115\12\115\13\115\14\115\15\115\16\115\17\115\20\115\21\115\22\115\23\115\24\115\25\115\26\115\27\115\30\115\31\115\32\115\33\115\34\115\35\115\37\115\40\115\41\115\42\115\43\115\44\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115\60\115\61\115\62\115\63\115\64\115\65\115\66\115\67\115\70\115\71\115\72\115\73\115\74\115\75\116\76\115\77\115\100\115\101\115\102\115\103\115\104\115\105\115\106\115\107\115" +
        "\107\0\154\1\154\2\154\3\154\4\154\5\154\6\154\7\154\10\154\11\154\12\154\13\154\14\154\15\154\16\154\17\154\20\154\21\154\22\154\23\154\24\154\25\154\26\154\27\154\30\154\31\154\32\154\33\154\34\154\35\154\37\154\40\154\41\154\42\154\43\154\44\154\45\154\46\154\47\154\50\154\51\154\52\154\53\154\54\154\55\154\56\154\57\154\60\154\61\154\62\154\63\154\64\154\65\154\66\154\67\154\70\166\71\154\72\154\73\154\74\154\75\155\76\154\77\154\100\154\101\154\102\154\103\154\104\154\105\154\106\154\107\154" +
        "\1\30\167" +
        "\1\14\170" +
        "\1\17\171" +
        "\1\17\172" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\173\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\174\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\175\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\107\0\115\1\115\2\115\3\115\4\115\5\115\6\115\7\115\10\115\11\115\12\115\13\115\14\115\15\115\16\115\17\115\20\115\21\115\22\115\23\115\24\115\25\115\26\115\27\115\30\115\31\115\32\115\33\115\34\115\35\115\37\115\40\115\41\115\42\115\43\115\44\115\45\115\46\115\47\115\50\115\51\115\52\115\53\115\54\115\55\115\56\115\57\115\60\115\61\115\62\115\63\115\64\115\65\115\66\115\67\115\70\115\71\115\72\115\73\115\74\115\75\116\76\115\77\115\100\115\101\115\102\115\103\115\104\115\105\115\106\115\107\115" +
        "\0" +
        "\1\10\176" +
        "\0" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\177\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\1\5\200" +
        "\37\0\46\1\46\2\46\3\46\4\201\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\0" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\202\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\203\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\37\0\46\1\46\2\46\3\46\4\46\5\46\6\46\7\46\10\46\11\204\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\65\47" +
        "\44\0\46\1\46\2\46\3\46\4\46\104\205\5\46\6\46\7\46\107\205\10\46\11\46\12\46\13\46\14\46\15\46\16\46\17\46\20\46\21\46\22\46\23\46\24\46\25\46\26\46\27\46\30\46\31\46\32\46\33\46\34\46\35\46\37\205\62\205\65\47\74\206" +
        "\5\62\205\104\205\107\205\74\206\37\205" +
        "\0";
    static int[][] trans = unpackTrans(trans_packed);
    int curMode = DEFAULT;
    Token lastToken;


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
        res.line = yyline;
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
                    token.name = names[token.type];
                    token.line = startLine;
                    bufStart = bufPos;
                    curMode = mode_map[lastState];
                    lastToken = token;
                    callAction(token, lastState);
                    return token;
                }
                else {
                    throw new IOException(String.format("invalid input=%c(%d) pos=%s line=%d mode=%s buf='%s' expecting=%s",yychar,yychar,yypos,yyline,printMode(),getText(),findExpected(backupState)));
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

    String printMode(){
        switch(curMode){
            case 1: return "member_mode";
            case 0: return "DEFAULT";

        }
        throw new RuntimeException("invalid mode: " + curMode);
    }

    void callAction(Token token, int lastState){
        switch(actions[lastState]){

        }
    }
}
