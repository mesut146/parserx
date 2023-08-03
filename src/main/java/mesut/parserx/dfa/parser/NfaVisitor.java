package mesut.parserx.dfa.parser;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Bracket;
import mesut.parserx.nodes.StringNode;
import mesut.parserx.nodes.TokenDecl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class NfaVisitor {

    public static NFA make(File file) throws IOException {
        return make(Files.readString(file.toPath()));
    }

    public static NFA make(String string) throws IOException {
        Lexer lexer = new Lexer(new StringReader(string));
        Parser parser = new Parser(lexer);
        var nfa = build(parser.nfa());
        //for each final state make token
        Map<String, TokenDecl> declMap = new HashMap<>();
        for (var state : nfa.it()) {
            if (state.accepting) {
                if (declMap.containsKey(state.name)) {
                    state.decl = declMap.get(state.name);
                } else {
                    var decl = new TokenDecl(state.name, null);
                    state.decl = decl;
                    declMap.put(state.name, decl);
                }
            }
        }
        return nfa;
    }

    public static NFA build(Ast.nfa nfa) {
        var res = new NFA(100);
        var alphabet = res.tree.alphabet;
        res.init(Integer.parseInt(nfa.startDecl.NUM.value));
        namedState(nfa.finalDecl.finalList.namedState, res);
        for (var ns : nfa.finalDecl.finalList.g1) {
            namedState(ns.namedState, res);
        }

        for (var trLine : nfa.trLine) {
            String state, target;
            int input = -1;
            if (trLine.g1.trSimple != null) {
                state = trLine.g1.trSimple.trSimple.NUM.value;
                target = trLine.g1.trSimple.trSimple.NUM2.value;
                if (trLine.g1.trSimple.trSimple.INPUT != null) {
                    input = input(trLine.g1.trSimple.trSimple.INPUT, alphabet);
                }
            } else {
                state = trLine.g1.trArrow.trArrow.NUM.value;
                target = trLine.g1.trArrow.trArrow.NUM2.value;
                if (trLine.g1.trArrow.trArrow.g1 != null) {
                    input = input(trLine.g1.trArrow.trArrow.g1.INPUT, alphabet);
                }
            }
            var st = res.getState(Integer.parseInt(state));
            if (input == -1) {
                st.addEpsilon(res.getState(Integer.parseInt(target)));
            } else {
                res.addTransition(st, res.getState(Integer.parseInt(target)), input);
            }
        }
        return res;
    }

    static int input(Ast.INPUT input, Alphabet alphabet) {
        if (input.IDENT != null) {
            return alphabet.addRegex(new StringNode(input.IDENT.IDENT.value));
        } else if (input.BRACKET != null) {
            var br = new Bracket(input.BRACKET.BRACKET.value).normalize();
            br.list.clear();
            br.list.addAll(br.ranges);
            return alphabet.addRegex(br);
        } else if (input.ANY != null) {
            return alphabet.addRegex(new StringNode(input.ANY.ANY.value));
        } else {
            //num
            return alphabet.addRegex(new StringNode(input.NUM.NUM.value));
        }
    }

    static void namedState(Ast.namedState ns, NFA res) {
        int st = Integer.parseInt(ns.NUM.value);
        res.setAccepting(st, true);
        if (ns.g1 != null) {
            res.getState(st).name = ns.g1.IDENT.value;
        } else {
            //res.getState(st).name = "S" + st;
            res.getState(st).name = "S";
        }
    }

}
