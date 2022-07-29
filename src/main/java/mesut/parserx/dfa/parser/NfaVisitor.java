package mesut.parserx.dfa.parser;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Bracket;
import mesut.parserx.nodes.StringNode;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

public class NfaVisitor {

    public static NFA make(File file) throws IOException {
        return make(Files.readString(file.toPath()));
    }

    public static NFA make(String string) throws IOException {
        Lexer lexer = new Lexer(new StringReader(string));
        Parser parser = new Parser(lexer);
        return build(parser.nfa());
    }

    public static NFA build(Ast.nfa nfa) {
        NFA res = new NFA(100);
        Alphabet alphabet = new Alphabet();
        res.tree.alphabet = alphabet;
        res.init(Integer.parseInt(nfa.startDecl.NUM.value));
        if (!nfa.finalDecl.finalList.namedState.isEmpty()) {
            for (Ast.namedState ns : nfa.finalDecl.finalList.namedState) {
                namedState(ns, res);
            }
        }
        else {
            namedState(nfa.finalDecl.finalList.finallist2.namedState, res);
            for (Ast.finalListg1 g1 : nfa.finalDecl.finalList.finallist2.g1) {
                namedState(g1.namedState, res);
            }
        }
        for (Ast.trLine trLine : nfa.trLine) {
            String state, target;
            int input = -1;
            if (trLine.g1.trSimple != null) {
                state = trLine.g1.trSimple.NUM.value;
                target = trLine.g1.trSimple.NUM2.value;
                if (trLine.g1.trSimple.INPUT != null) {
                    input = input(trLine.g1.trSimple.INPUT, alphabet);
                }
            }
            else {
                state = trLine.g1.trArrow.NUM.value;
                target = trLine.g1.trArrow.NUM2.value;
                if (trLine.g1.trArrow.g1 != null) {
                    input = input(trLine.g1.trArrow.g1.INPUT, alphabet);
                }
            }
            var st = res.getState(Integer.parseInt(state));
            if (input == -1) {
                st.addEpsilon(res.getState(Integer.parseInt(target)));
            }
            else {
                res.addTransition(st, res.getState(Integer.parseInt(target)), input);
            }
        }
        return res;
    }

    static int input(Ast.INPUT input, Alphabet alphabet) {
        if (input.IDENT != null) {
            return alphabet.addRegex(new StringNode(input.IDENT.value));
        }
        else if (input.BRACKET != null) {
            return alphabet.addRegex(new Bracket(input.BRACKET.value));
        }
        else {
            //any
            return alphabet.addRegex(new StringNode(input.ANY.value));
        }
    }

    static void namedState(Ast.namedState ns, NFA res) {
        int st = Integer.parseInt(ns.NUM.value);
        res.setAccepting(st, true);
        if (ns.g1 != null) {
            res.getState(st).addName(ns.g1.IDENT.value);
        }
    }

}
