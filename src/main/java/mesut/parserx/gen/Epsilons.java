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
            if (regex.isOptional()) {
                //A? = A | €
                return trim(regex.node);
            }
            else if (regex.isStar()) {
                //A* = A+ | € | A A* | €
                return new Sequence(trim(regex.node), regex);
            }
            else {
                //A+ = A A*
                return new Sequence(trim(regex.node), regex);
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Or res = new Or();
            for (int i = 0; i < or.size(); i++) {
                Node ch = or.get(i);
                res.add(trim(ch));
            }
            return res;
        }
        else if (node.isSequence()) {
            //A B -> A1 B | A B1
            Sequence s = node.asSequence();
            Node a = s.first();
            Node b = Helper.trim(s);
            Sequence s1 = new Sequence(trim(a), b);
            Sequence s2 = new Sequence(a, trim(b));
            return new Or(s1, s2);
        }
        else if (node.isGroup()) {
            return new Group(trim(node.asGroup().node)).normal();
        }
        else if (node.isName()) {
            Name name = node.asName();
            RuleDecl decl = tree.getRule(name.name);
            RuleDecl newDecl = new RuleDecl(name.name + "_noe", trim(decl.rhs));
            tree.addRule(newDecl);
            return newDecl.ref();
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }
}
