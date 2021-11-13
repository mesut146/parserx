package mesut.parserx.parser;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.IOException;

public class HandParser {
    Lexer lexer;
    Token la;

    public HandParser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        la = lexer.next();
    }

    Token consume(int id) throws IOException {
        if (la.type == id) {
            Token res = la;
            la = lexer.next();
            return res;
        }
        else {
            throw new RuntimeException("unexpected " + la);
        }
    }

    Tree tree() throws IOException {
        Tree res = new Tree();
        while (la.type == Tokens.INCLUDE) {
            la = lexer.next();
            res.addInclude(UnicodeUtils.trimQuotes(consume(Tokens.STRING).value));
        }
        while (la.type == Tokens.TOKEN || la.type == Tokens.SKIP) {
            if (la.type == Tokens.TOKEN) {
                consume(Tokens.TOKEN);
                consume(Tokens.LBRACE);
                while (la.type == Tokens.IDENT) {
                    res.addToken(parseToken());
                }
                consume(Tokens.RBRACE);
            }
            else {
                consume(Tokens.SKIP);
                consume(Tokens.LBRACE);
                while (la.type == Tokens.IDENT) {
                    res.addSkip(parseToken());
                }
                consume(Tokens.RBRACE);
            }
        }
        return res;
    }

    TokenDecl parseToken() throws IOException {
        boolean frag = false;
        if (la.type == Tokens.HASH) {
            frag = true;
            consume(Tokens.HASH);
        }
        String name = consume(Tokens.IDENT).value;
        consume(Tokens.SEPARATOR);
        Node node = rhs();
        consume(Tokens.SEMI);
        TokenDecl tokenDecl = new TokenDecl(name, node);
        tokenDecl.fragment = frag;
        return tokenDecl;
    }

    private Node rhs() {
        return null;
    }
}
