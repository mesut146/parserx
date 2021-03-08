package gen;

import nodes.*;

import java.util.ArrayList;
import java.util.List;

//remove left recursions
public class LeftRecursive {
    public Tree resTree;
    Tree tree;//must be bnf grammar

    public LeftRecursive(Tree tree) {
        this.tree = tree;
        this.resTree = new Tree(tree);
    }

    public void process() {
        for (RuleDecl rule : tree.rules) {
            if (Helper.startWith(rule, rule.name)) {
                //direct
                direct(rule);
            }
            else {
                //subs
                if (rule.rhs.isOr()) {
                    for (Node ch : rule.rhs.asOr()) {
                        /*if (startWith()) {

                        }*/
                    }
                }
            }
        }
    }

    private void direct(RuleDecl rule) {
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
        else {
            throw new RuntimeException("invalid left rec on: " + rule);
        }
    }

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

}
