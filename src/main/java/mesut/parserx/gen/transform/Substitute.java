package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

public class Substitute {
    Tree tree;

    public Substitute(Tree tree) {
        this.tree = tree;
    }

    //regex or sequence
    public Node process(Node node, Name ref) {
        if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                if (s.get(i).equals(ref)) {
                    Sequence res = new Sequence(s.list);
                    res.list.remove(i);
                    RuleDecl decl = tree.getRule(ref);
                    res.list.add(i, decl.rhs);//todo norm
                }
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
        }
        return node;
    }
}
