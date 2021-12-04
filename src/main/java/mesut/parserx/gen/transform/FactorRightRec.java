package mesut.parserx.gen.transform;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Transformer;

import java.util.ArrayList;
import java.util.List;

public class FactorRightRec extends Transformer {
    RuleDecl decl;
    List list = new ArrayList<>();
    Factor factor;

    @Override
    public Node visitOr(Or or, Void arg) {
        factor.visitOr(or,arg);
        return or;
    }
}
