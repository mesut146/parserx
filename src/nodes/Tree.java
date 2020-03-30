package nodes;

import rule.Rule;
import rule.RuleDecl;

public class Tree extends NodeList<Node> {

    NodeList<TokenDecl> tokens;
    NodeList<RuleDecl> rules;

    public Tree() {
        tokens = new NodeList<>();
        rules = new NodeList<>();
    }

    public void addToken(TokenDecl token) {
        tokens.add(token);
    }

    public void addRule(RuleDecl rule) {
        rules.add(rule);
    }

    //ebnf to bnf
    public void transform() {
        Tree tree = new Tree();//result tree
        for (RuleDecl decl : rules.list) {
            Rule rhs = decl.rhs;
            if (rhs.isGroup()) {
                rhs.asGroup().transform(decl, tree);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* tokens */\n\n");
        sb.append(tokens.join("\n"));

        sb.append("\n\n");

        sb.append("/* rules */\n\n");
        sb.append(rules.join("\n"));
        return sb.toString();
    }


}
