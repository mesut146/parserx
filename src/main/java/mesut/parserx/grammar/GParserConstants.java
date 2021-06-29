/* Generated By:JavaCC: Do not edit this line. GParserConstants.java */
package mesut.parserx.grammar;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface GParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 8;
  /** RegularExpression Id. */
  int FORMAL_COMMENT = 9;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 10;
  /** RegularExpression Id. */
  int LPAREN = 12;
  /** RegularExpression Id. */
  int RPAREN = 13;
  /** RegularExpression Id. */
  int LBRACE = 14;
  /** RegularExpression Id. */
  int RBRACE = 15;
  /** RegularExpression Id. */
  int LBRACKET = 16;
  /** RegularExpression Id. */
  int RBRACKET = 17;
  /** RegularExpression Id. */
  int SEMI = 18;
  /** RegularExpression Id. */
  int COLON = 19;
  /** RegularExpression Id. */
  int DOT = 20;
  /** RegularExpression Id. */
  int COMMA = 21;
  /** RegularExpression Id. */
  int QUOTE = 22;
  /** RegularExpression Id. */
  int EQ = 23;
  /** RegularExpression Id. */
  int COLONEQEQ = 24;
  /** RegularExpression Id. */
  int COLONEQ = 25;
  /** RegularExpression Id. */
  int ARROW = 26;
  /** RegularExpression Id. */
  int STAR = 27;
  /** RegularExpression Id. */
  int PLUS = 28;
  /** RegularExpression Id. */
  int QUES = 29;
  /** RegularExpression Id. */
  int XOR = 30;
  /** RegularExpression Id. */
  int TILDE = 31;
  /** RegularExpression Id. */
  int HASH = 32;
  /** RegularExpression Id. */
  int EMPTY = 33;
  /** RegularExpression Id. */
  int KEYWORD_TOKEN = 34;
  /** RegularExpression Id. */
  int KEYWORD_TOKENS = 35;
  /** RegularExpression Id. */
  int KEYWORD_SKIP = 36;
  /** RegularExpression Id. */
  int STRING_LITERAL = 37;
  /** RegularExpression Id. */
  int INCLUDE_DIRECTIVE = 38;
  /** RegularExpression Id. */
  int START_SIRECTIVE = 39;
  /** RegularExpression Id. */
  int IDENT = 40;
  /** RegularExpression Id. */
  int CHAR = 41;
  /** RegularExpression Id. */
  int DIGIT = 42;
  /** RegularExpression Id. */
  int HEX_DIGIT = 43;
  /** RegularExpression Id. */
  int OCTAL_DIGIT = 44;
  /** RegularExpression Id. */
  int ESCAPED_HEX = 45;
  /** RegularExpression Id. */
  int ESCAPED_OCTAL = 46;
  /** RegularExpression Id. */
  int BRACKET_LIST = 47;
  /** RegularExpression Id. */
  int INTEGER = 48;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_SINGLE_LINE_COMMENT = 1;
  /** Lexical state. */
  int IN_FORMAL_COMMENT = 2;
  /** Lexical state. */
  int IN_MULTI_LINE_COMMENT = 3;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\r\"",
    "\"\\n\"",
    "\"\\t\"",
    "\"//\"",
    "<token of kind 6>",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 11>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\":\"",
    "\".\"",
    "\",\"",
    "\"\\\'\"",
    "\"=\"",
    "\":==\"",
    "\":=\"",
    "\"->\"",
    "\"*\"",
    "\"+\"",
    "\"?\"",
    "\"^\"",
    "\"~\"",
    "\"#\"",
    "<EMPTY>",
    "\"token\"",
    "\"tokens\"",
    "\"skip\"",
    "<STRING_LITERAL>",
    "\"include\"",
    "\"@start\"",
    "<IDENT>",
    "<CHAR>",
    "<DIGIT>",
    "<HEX_DIGIT>",
    "<OCTAL_DIGIT>",
    "<ESCAPED_HEX>",
    "<ESCAPED_OCTAL>",
    "<BRACKET_LIST>",
    "<INTEGER>",
    "\"|\"",
  };

}
