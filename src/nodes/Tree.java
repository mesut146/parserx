package nodes;

import rule.Rule;
import rule.RuleDecl;
import java.util.*;

public class Tree extends NodeList<Node> {

    //List<Node> tokenDef;//token block
    List<TokenDecl> skip;
    NodeList<TokenDecl> tokens;
    NodeList<RuleDecl> rules;

    public Tree() {
        tokens = new NodeList<>();
        rules = new NodeList<>();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* tokens */\n\n");
        sb.append("tokens{\n");
        for(TokenDecl td:tokens.list){
            sb.append("  ");
            sb.append(td);
            sb.append("\n");
        }
        sb.append("}");
        
        sb.append("\n\n");

        sb.append("/* rules */\n\n");
        sb.append(rules.join("\n"));
        return sb.toString();
    }


}
