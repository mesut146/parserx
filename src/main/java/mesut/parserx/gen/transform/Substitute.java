package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class Substitute {
    Tree tree;
    boolean mergeLeft;
    boolean mergeRight;
    int count = 0;

    public Substitute(Tree tree) {
        this.tree = tree;
    }

    //expand ref in sequence
    public Node process(Sequence seq, Name ref) {
        for (int i = 0; i < seq.size(); i++) {
            if (seq.get(i).equals(ref)) {
                Sequence res = new Sequence(seq.list);
                res.list.remove(i);
                RuleDecl decl = tree.getRule(ref);
                if (decl.rhs.isOr()) {
                    Or or = decl.rhs.asOr();
                    List<Node> or2 = new ArrayList<>();
                    Node right = i == seq.size() - 1 ? null : new Sequence(seq.list.subList(i + 1, seq.size()));
                    Node left = i == 0 ? null : new Sequence(seq.list.subList(0, i));

                    if (mergeLeft) {
                        if (mergeRight) {
                            //A B1 C | A B2 C
                            for (Node ch : or) {
                                or2.add(new Sequence(left, ch, right));
                            }
                            return Or.make(or2);
                        }
                        else {
                            //(A B1 | A B2) C
                            for (Node ch : or) {
                                or2.add(new Sequence(left, ch));
                            }
                            return new Sequence(new Group(Or.make(or2)), right);
                        }
                    }
                    else {
                        if (mergeRight) {
                            //A (B1 C | B2 C)
                            for (Node ch : or) {
                                or2.add(new Sequence(ch, right));
                            }
                            return new Sequence(left, new Group(Or.make(or2)));
                        }
                        else {
                            return new Sequence(left, new Group(or), right);
                        }
                    }
                }
                else {
                    res.list.add(i, decl.rhs);
                }
                return res;
            }
        }
        return seq;
    }

    void updateCode(Node node) {

    }
}
