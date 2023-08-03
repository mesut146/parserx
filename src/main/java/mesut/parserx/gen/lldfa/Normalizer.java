package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;

//simplify a little for llrec
public class Normalizer extends Transformer {

    int groupCount = 1;

    public Normalizer(Tree tree) {
        super(tree);
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            groupCount = 1;
            //todo restart
            var decl = tree.rules.get(i);
            transformRule(decl);
        }
    }

    @Override
    public Node visitGroup(Group node, Void parent) {
        var cls = curRule.baseName() + "g" + groupCount;
        var tmp = new RuleDecl(cls, node.node);
        tmp.retType = new Type(tree.options.astClass, cls);
        tree.addRuleBelow(tmp, curRule);

        var ref = tmp.ref.copy();
        ref.astInfo = node.astInfo.copy();
        if (node.astInfo.varName != null) {
            ref.astInfo.varName = node.astInfo.varName;
        } else {
            ref.astInfo.varName = "g" + groupCount;
        }
        groupCount++;
        tmp.rhs = transformNode(tmp.rhs, null);
        return ref;
    }
}
