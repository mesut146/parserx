package nodes;

import dfa.NFA;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.List;

//the grammar file
public class Tree {

    List<TokenDecl> skip;
    List<TokenDecl> tokens;
    List<RuleDecl> rules;

    public Tree() {
        tokens = new ArrayList<>();
        rules = new ArrayList<>();
        skip = new ArrayList<>();
    }

    public void addToken(TokenDecl token) {
        tokens.add(token);
    }

    public void addSkip(TokenDecl token) {
        skip.add(token);
    }

    public void addRule(RuleDecl rule) {
        rules.add(rule);
    }

    public TokenDecl getToken(String name) {
        for (TokenDecl td : tokens) {
            if (td.tokenName.equals(name)) {
                return td;
            }
        }
        for (TokenDecl td : skip) {
            if (td.tokenName.equals(name)) {
                return td;
            }
        }

        return null;
    }

    public NFA makeNFA() {
        NFA nfa = new NFA(100);
        nfa.tree = this;
        for (TokenDecl decl : tokens) {
            if (!decl.fragment) {
                nfa.addRegex(decl);
            }
        }
        return nfa;
    }

    //ebnf to bnf
    /*public Tree transform() {
        Tree tree = new Tree();//result tree

        for (RuleDecl decl : rules.list) {
            RuleDecl d = new RuleDecl(decl.name);
            Rule rhs = decl.rhs;
            if (rhs.isGroup()) {
                //remove unnecessary parenthesis
                //r = (s1 s2);
                d.rhs = rhs.asGroup().rhs;
                tree.addRule(d);
            }
            else if (rhs.isName()) {
                tree.addRule(decl);
            }
            else if (rhs.isSequence()) {
                //todo
                d.rhs=rhs.asSequence().transform(decl,tree);
                tree.addRule(d);
            }
        }

        return tree;
    }*/

    void printTokens(StringBuilder sb) {
        sb.append("/* tokens */\n\n");
        sb.append("tokens{\n");
        for (TokenDecl td : tokens) {
            sb.append("  ");
            sb.append(td);
            sb.append("\n");
        }
        sb.append("}");
    }

    void printSkips(StringBuilder sb) {
        sb.append("/* skip tokens */\n\n");
        sb.append("skip{\n");
        for (TokenDecl td : skip) {
            sb.append("  ");
            sb.append(td);
            sb.append("\n");
        }
        sb.append("}");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        printTokens(sb);
        sb.append("\n\n");
        printSkips(sb);
        sb.append("\n\n");

        sb.append("/* rules */\n\n");
        sb.append(NodeList.join(rules, "\n"));
        return sb.toString();
    }


}
