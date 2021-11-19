package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;

//Deep Copy
public class Copier extends Transformer {

    public Copier(Tree tree) {
        super(tree);
    }

    Node withAst(Node res, Node info) {
        res.astInfo = info.astInfo.copy();
        return res;
    }

    @Override
    public Node visitName(Name name, Void arg) {
        Name res = new Name(name.name, name.isToken);
        res.args = new ArrayList<>(name.args);
        return withAst(res, name);
    }

    @Override
    public Node visitRegex(Regex regex, Void arg) {
        Node ch = transformNode(regex.node, arg);
        return withAst(new Regex(ch, regex.type), regex);
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        Sequence res = new Sequence();
        for (Node ch : seq) {
            res.add(transformNode(ch, arg));
        }
        res.assocLeft = seq.assocLeft;
        res.assocRight = seq.assocRight;
        return withAst(res, seq);
    }

    @Override
    public Node visitOr(Or or, Void arg) {
        Or res = new Or();
        for (Node ch : or) {
            String label = ch.label;
            ch = transformNode(ch, arg);
            ch.label = label;
            res.add(ch);
        }
        return withAst(res, or);
    }

    @Override
    public Node visitGroup(Group node, Void arg) {
        Node ch = transformNode(node.node, arg);
        return withAst(new Group(ch), node);
    }

}
