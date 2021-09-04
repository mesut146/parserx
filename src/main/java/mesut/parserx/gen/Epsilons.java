package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class Epsilons {
    Tree tree;

    public Epsilons(Tree tree) {
        this.tree = tree;
    }

    //trim major epsilon so that result is at least one token long
    //E: ... -> E1: ...,     E: E1 | €
    public Node trim(Node node) {
        if (!Helper.canBeEmpty(node, tree)) {
            return node;
        }
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            boolean empty = Helper.canBeEmpty(regex.node, tree);
            if (regex.isOptional()) {
                if (empty) {
                    //A? = A | € = A_no_eps | €
                    return trim(regex.node);
                }
                return regex.node;
            }
            else if (regex.isStar()) {
                if (empty) {
                    //A* = A+ | € = A A* | € = (A1 | €) A1* | € = A1 A1* | A1* | €
                    //A1 A1* | A1+ | € = A1+ | €
                    return new Regex(trim(regex.node), "+");
                }
                //A+ | €
                return new Regex(regex.node, "+");
            }
            else {
                //no must be empty
                //A+ = A A* = (A_no_eps | €) (A_no_eps | €)* =  A_no_eps A_no_eps* | A_no_eps*
                //=A_no_eps (A_no_eps)* | A_no_eps+ | € = A_no_eps+ | €
                return new Regex(trim(regex.node), "+");
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Node a = or.first();
            Node b = Helper.trim(or);
            if (Helper.canBeEmpty(a, tree)) {
                if (Helper.canBeEmpty(b, tree)) {
                    //A | B = A1 | € | B1 | € = A1 | B1 | €
                    return new Or(trim(a), trim(b));
                }
                else {
                    //A | B = A1 | € | B
                    return new Or(trim(a), b);
                }
            }
            else {
                //b must be empty
                // A | B = A | B1 | €
                return new Or(a, trim(b));
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            Node a = trim(s.first());
            Node b = trim(Helper.trim(s));
            //both a b are empty
            //A B = (A1 | €) (B1 | €) = A1 B1 | A1 | B1 | € = A1 B1? | B1 | €
            Sequence s1 = new Sequence(a, b);
            return new Or(s1, a, b);
        }
        else if (node.isGroup()) {
            return new Group(trim(node.asGroup().node)).normal();
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.factored) {
                return name;
            }

            RuleDecl decl = tree.getRule(name);
            String newName = Factor.encode(name).name + "_noe";
            RuleDecl newDecl = tree.getRule(newName);
            if (newDecl == null) {
                newDecl = new RuleDecl(newName, trim(decl.rhs));
                tree.addRule(newDecl);
            }
            return newDecl.ref();
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }
}
