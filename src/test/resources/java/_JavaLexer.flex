package com.mesut.psi.java.flex;


@SuppressWarnings("ALL")
%%

%{

  public int getPosition(){
      return zzCurrentPos;
  }

  public void goTo(int offset) {
    zzCurrentPos = zzMarkedPos = zzStartRead = offset;
    zzAtEOF = false;
  }
%}

%unicode
%public
%line
%column
%class JavaLexer
//%implements FlexLexer
%function advance
%type Element

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WS     = {LineTerminator} | [ \t\f]

    /* comments */
COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT} | {DOC_COMMENT}
BLOCK_COMMENT   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
LINE_COMMENT     = "//" {InputCharacter}* {LineTerminator}?
DOC_COMMENT = "/**" {COMMENT_CONTENT} "*"+ "/"
COMMENT_CONTENT       = ( [^*] | \*+ [^/*] )*

IDENTIFIER = [:jletter:]  ([:jletter] | [:jletterdigit:] | "_")*

DIGIT = [0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]

STRING_LITERAL = "\"" {string_content}* "\""
CHAR_LITERAL = "'" (.*) "'"

string_content = "\\." | [^\n\r\"]

INTEGER_LITERAL = {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL}
LONG_LITERAL = {INTEGER_LITERAL} [Ll]
HEX_INTEGER_LITERAL = 0 [Xx] {HEX_DIGIT_OR_UNDERSCORE}*
BIN_INTEGER_LITERAL = 0 [Bb] {DIGIT_OR_UNDERSCORE}*

FLOAT_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Ff] | {DIGITS} [Ff]
DOUBLE_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Dd]? | {DIGITS} [Dd]
DEC_FP_LITERAL = {DIGITS} {DEC_EXPONENT} | {DEC_SIGNIFICAND} {DEC_EXPONENT}?
DEC_SIGNIFICAND = "." {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*
DEC_EXPONENT = [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL = {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND = 0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT = [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*

%%

/*
 * NOTE: the rule set does not include rules for whitespaces, comments, and text literals -
 * they are implemented in com.intellij.lang.java.lexer.JavaLexer class.
 */

<YYINITIAL> {
  {WS} {}

  {STRING_LITERAL} { return JavaTokenType.STRING_LITERAL; }
  {CHAR_LITERAL} { return JavaTokenType.CHARACTER_LITERAL; }

  {LONG_LITERAL} { return JavaTokenType.LONG_LITERAL; }
  {INTEGER_LITERAL} { return JavaTokenType.INTEGER_LITERAL; }
  {FLOAT_LITERAL} { return JavaTokenType.FLOAT_LITERAL; }
  {DOUBLE_LITERAL} { return JavaTokenType.DOUBLE_LITERAL; }

  "true" { return JavaTokenType.TRUE_KEYWORD; }
  "false" { return JavaTokenType.FALSE_KEYWORD; }
  "null" { return JavaTokenType.NULL_KEYWORD; }

  "abstract" { return JavaTokenType.ABSTRACT_KEYWORD; }
  "assert" { return JavaTokenType.ASSERT_KEYWORD; }
  "boolean" { return JavaTokenType.BOOLEAN_KEYWORD; }
  "break" { return JavaTokenType.BREAK_KEYWORD; }
  "byte" { return JavaTokenType.BYTE_KEYWORD; }
  "case" { return JavaTokenType.CASE_KEYWORD; }
  "catch" { return JavaTokenType.CATCH_KEYWORD; }
  "char" { return JavaTokenType.CHAR_KEYWORD; }
  "class" { return JavaTokenType.CLASS_KEYWORD; }
  "const" { return JavaTokenType.CONST_KEYWORD; }
  "continue" { return JavaTokenType.CONTINUE_KEYWORD; }
  "default" { return JavaTokenType.DEFAULT_KEYWORD; }
  "do" { return JavaTokenType.DO_KEYWORD; }
  "double" { return JavaTokenType.DOUBLE_KEYWORD; }
  "else" { return JavaTokenType.ELSE_KEYWORD; }
  "enum" { return JavaTokenType.ENUM_KEYWORD;  }
  "extends" { return JavaTokenType.EXTENDS_KEYWORD; }
  "final" { return JavaTokenType.FINAL_KEYWORD; }
  "finally" { return JavaTokenType.FINALLY_KEYWORD; }
  "float" { return JavaTokenType.FLOAT_KEYWORD; }
  "for" { return JavaTokenType.FOR_KEYWORD; }
  "goto" { return JavaTokenType.GOTO_KEYWORD; }
  "if" { return JavaTokenType.IF_KEYWORD; }
  "implements" { return JavaTokenType.IMPLEMENTS_KEYWORD; }
  "import" { return JavaTokenType.IMPORT_KEYWORD; }
  "instanceof" { return JavaTokenType.INSTANCEOF_KEYWORD; }
  "int" { return JavaTokenType.INT_KEYWORD; }
  "interface" { return JavaTokenType.INTERFACE_KEYWORD; }
  "long" { return JavaTokenType.LONG_KEYWORD; }
  "native" { return JavaTokenType.NATIVE_KEYWORD; }
  "new" { return JavaTokenType.NEW_KEYWORD; }
  "package" { return JavaTokenType.PACKAGE_KEYWORD; }
  "private" { return JavaTokenType.PRIVATE_KEYWORD; }
  "public" { return JavaTokenType.PUBLIC_KEYWORD; }
  "short" { return JavaTokenType.SHORT_KEYWORD; }
  "super" { return JavaTokenType.SUPER_KEYWORD; }
  "switch" { return JavaTokenType.SWITCH_KEYWORD; }
  "synchronized" { return JavaTokenType.SYNCHRONIZED_KEYWORD; }
  "this" { return JavaTokenType.THIS_KEYWORD; }
  "throw" { return JavaTokenType.THROW_KEYWORD; }
  "protected" { return JavaTokenType.PROTECTED_KEYWORD; }
  "transient" { return JavaTokenType.TRANSIENT_KEYWORD; }
  "return" { return JavaTokenType.RETURN_KEYWORD; }
  "void" { return JavaTokenType.VOID_KEYWORD; }
  "static" { return JavaTokenType.STATIC_KEYWORD; }
  "strictfp" { return JavaTokenType.STRICTFP_KEYWORD; }
  "while" { return JavaTokenType.WHILE_KEYWORD; }
  "try" { return JavaTokenType.TRY_KEYWORD; }
  "volatile" { return JavaTokenType.VOLATILE_KEYWORD; }
  "throws" { return JavaTokenType.THROWS_KEYWORD; }

  {IDENTIFIER} { return JavaTokenType.IDENTIFIER; }

  {COMMENT} { return JavaTokenType.COMMENT; }

  "==" { return JavaTokenType.EQEQ; }
  "!=" { return JavaTokenType.NE; }
  "||" { return JavaTokenType.OROR; }
  "++" { return JavaTokenType.PLUSPLUS; }
  "--" { return JavaTokenType.MINUSMINUS; }

  "<" { return JavaTokenType.LT; }
  "<=" { return JavaTokenType.LE; }
  "<<=" { return JavaTokenType.LTLTEQ; }
  "<<" { return JavaTokenType.LTLT; }
  ">" { return JavaTokenType.GT; }
  "&" { return JavaTokenType.AND; }
  "&&" { return JavaTokenType.ANDAND; }

  "+=" { return JavaTokenType.PLUSEQ; }
  "-=" { return JavaTokenType.MINUSEQ; }
  "*=" { return JavaTokenType.ASTERISKEQ; }
  "/=" { return JavaTokenType.DIVEQ; }
  "&=" { return JavaTokenType.ANDEQ; }
  "|=" { return JavaTokenType.OREQ; }
  "^=" { return JavaTokenType.XOREQ; }
  "%=" { return JavaTokenType.PERCEQ; }

  "("   { return JavaTokenType.LPARENTH; }
  ")"   { return JavaTokenType.RPARENTH; }
  "{"   { return JavaTokenType.LBRACE; }
  "}"   { return JavaTokenType.RBRACE; }
  "["   { return JavaTokenType.LBRACKET; }
  "]"   { return JavaTokenType.RBRACKET; }
  ";"   { return JavaTokenType.SEMICOLON; }
  ","   { return JavaTokenType.COMMA; }
  "..." { return JavaTokenType.ELLIPSIS; }
  "."   { return JavaTokenType.DOT; }

  "=" { return JavaTokenType.EQ; }
  "!" { return JavaTokenType.EXCL; }
  "~" { return JavaTokenType.TILDE; }
  "?" { return JavaTokenType.QUEST; }
  ":" { return JavaTokenType.COLON; }
  "+" { return JavaTokenType.PLUS; }
  "-" { return JavaTokenType.MINUS; }
  "*" { return JavaTokenType.ASTERISK; }
  "/" { return JavaTokenType.DIV; }
  "|" { return JavaTokenType.OR; }
  "^" { return JavaTokenType.XOR; }
  "%" { return JavaTokenType.PERC; }
  "@" { return JavaTokenType.AT; }

  "::" { return JavaTokenType.DOUBLE_COLON; }
  "->" { return JavaTokenType.ARROW; }
}

[^]  { return JavaTokenType.BAD_CHARACTER; }
