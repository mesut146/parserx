package mesut.parserx.grammar;

public class MyParser {

    char[] buf;
    int pos;
    int line;

    Token next() {
        char c = buf[pos++];
        while (c == ' ' | c == '\r' || c == '\n' || c == '\t') {
            if (c == '\r' || c == '\n') {
                line++;
            }
            c = buf[pos++];
        }
        if (c == '(') {
            return new Token(TokenType.LP);
        }
        else if (c == ')') {
            return new Token(TokenType.RP);
        }
        return null;
    }

    enum TokenType {
        TOKEN, SKIP, IDENT, BOOLEAN, OPTION, START, EPSILON, BRACKET, STRING,
        LP, RP, LBRACE, RBRACE, STAR, PLUS, QUES, POW, TILDE, SEPARATOR
    }

    static class Token {
        int line, offset;
        TokenType type;
        String value;

        public Token(TokenType type) {
            this.type = type;
        }
    }
}
