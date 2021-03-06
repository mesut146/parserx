package gen;

import nodes.*;

import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

//remove left recursions
public class LeftRecursive {
    Tree tree;//must be bnf grammar
    public Tree resTree;

    public LeftRecursive(Tree tree) {
        this.tree = tree;
        this.resTree = new Tree(tree);
    }

    public void transform() {
        for (RuleDecl rule : tree.rules) {
            transform(rule);
        }
    }

    private void transform(RuleDecl rule) {
        //handle direct
        Node rhs = rule.rhs;

        if (rhs.isOr()) {
            //A = A a | A b | c | d | E
            OrNode or = rhs.asOr();
            List<Node> normals = new ArrayList<>();
            List<Node> tails = new ArrayList<>();
            for (Node ch : or) {
                if (ch.isName()) {
                    normals.add(ch.asName());
                }
                else if (ch.isEmpty()) {

                }
                else {//seq
                    Sequence seq = ch.asSequence();
                    if (!first(ch).isToken && first(ch).name.equals(rule.name)) {
                        Node tail = new Sequence(seq.list.subList(1, seq.size())).normal();
                        tails.add(tail);
                    }
                    else {
                        normals.add(ch);
                    }
                }
            }
            OrNode res = new OrNode();
            NameNode tail = new NameNode(rule.name + "'");
            OrNode tailOr = new OrNode();

            for (Node normal : normals) {
                res.add(new Sequence(normal, tail));
            }
            for (Node t : tails) {
                tailOr.add(new Sequence(t, tail));
            }
            tailOr.add(new EmptyNode());
            resTree.addRule(new RuleDecl(rule.name, res));
            resTree.addRule(new RuleDecl(tail.name, tailOr));
        }
    }

    /*boolean canBeEmpty(NameNode node) {
        for (RuleDecl decl : tree.getRules(node.name)) {
            if (first(decl.rhs)) {

            }
        }
        return false;
    }*/

    NameNode first(Node node) {
        if (node.isName()) {
            return node.asName();
        }
        else if (node.isSequence()) {
            return node.asSequence().get(0).asName();
        }
        else if (node.isOr()) {
            return first(node.asOr().get(0));
        }
        throw new RuntimeException("first: " + node);
    }

    NameNode makeName(NameNode node) {
        return new NameNode(node.name + "'");
    }

    List<Node> firstSequence(Node rhs) {
        List<Node> list = new ArrayList<>();
        if (rhs.isName()) {
            list.add(rhs);
            RuleDecl decl = tree.getRule(rhs.asName().name);
            list.addAll(firstSequence(decl.rhs));
        }
        else if (rhs.isSequence()) {

        }

        return list;
    }

    Node getFirst(RuleDecl decl, Node rhs) {
        if (rhs.isName() && !rhs.asName().isToken) {
            return rhs;
        }
        else if (rhs.isSequence()) {
            return getFirst(decl, rhs.asSequence().get(0));
        }
        else if (rhs.isOr()) {
            for (Node or : rhs.asOr()) {
                Node first = getFirst(decl, or);
                if (first != null) {
                    return first;
                }
            }

        }
        else if (rhs.isGroup()) {
            return getFirst(decl, rhs.asGroup().rhs);
        }

        return null;
    }
}
