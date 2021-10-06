package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

public class Epsilons {
    Tree tree;

    public Epsilons(Tree tree) {
        this.tree = tree;
    }

    public static Node trim(Node node, Tree tree) {
        return new Epsilons(tree).trim(node);
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
                Node a1 = trim(a);
                if (Helper.canBeEmpty(b, tree)) {
                    //A | B = A1 | € | B1 | € = A1 | B1 | €
                    if (a1 == null) {
                        return trim(b);
                    }
                    else {
                        return new Or(a1, trim(b));
                    }
                }
                else {
                    //A | B = A1 | € | B
                    if (a1 == null) {
                        return b;
                    }
                    else {
                        return new Or(a1, b);
                    }
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
            if (a == null) {
                if (isFactored(s.first())) {
                    Sequence s1 = new Sequence(s.first(), b);
                    s1.astInfo.code = s.astInfo.code;
                    b.astInfo.code = s.astInfo.code;
                    return new Or(s1, b);
                }
                else {
                    //a is epsilon without factor
                    b.astInfo.code = s.astInfo.code;
                    return b;
                }
            }
            else {
                Sequence s1 = new Sequence(a, b);
                s1.astInfo.code = s.astInfo.code;
                a.astInfo.code = s.astInfo.code;
                b.astInfo.code = s.astInfo.code;
                return new Or(s1, a, b);
            }
        }
        else if (node.isGroup()) {
            Group res = new Group(trim(node.asGroup().node));
            res.astInfo = node.astInfo;
            return res;
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.isFactored) {
                //throw new RuntimeException();
                //todo null is better
                //return name;
                return null;
            }
            if (isFactored(name)) {
                //still factored but chained
                return null;
            }

            RuleDecl decl = tree.getRule(name);
            String newName = name.encode().name + "_noe";
            RuleDecl newDecl = tree.getRule(newName);
            if (newDecl == null) {
                newDecl = new RuleDecl(newName, trim(decl.rhs));
                newDecl.retType = decl.retType;
                newDecl.ref.args = name.args;//todo improve
                tree.addRule(newDecl);
            }
            return newDecl.ref.copy();
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }

    private boolean isFactored(Node a) {
        return Helper.first(a, tree, true, false, true).isEmpty();
    }
}
