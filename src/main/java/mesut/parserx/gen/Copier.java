package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;

//Deep Copy
public class Copier extends SimpleTransformer {

    public Copier(Tree tree) {
        super(tree);
    }

    Node withAst(Node res, Node info) {
        res.astInfo = info.astInfo.copy();
        return res;
    }

    @Override
    public Node transformName(Name name, Node parent) {
        Name res = new Name(name.name, name.isToken);
        res.args = new ArrayList<>(name.args);
        return withAst(res, name);
    }

    @Override
    public Node transformRegex(Regex regex, Node parent) {
        Node ch = transformNode(regex.node, regex);
        return withAst(new Regex(ch, regex.type), regex);
    }

    @Override
    public Node transformSequence(Sequence seq, Node parent) {
        Sequence res = new Sequence();
        for (Node ch : seq) {
            res.add(transformNode(ch, seq));
        }
        return withAst(res, seq);
    }

    @Override
    public Node transformOr(Or or, Node parent) {
        Or res = new Or();
        res.label = or.label;
        for (Node ch : or) {
            res.add(transformNode(ch, or));
        }
        return withAst(res, or);
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        Node ch = transformNode(node.node, node);
        return withAst(new Group(ch), node);
    }

}
