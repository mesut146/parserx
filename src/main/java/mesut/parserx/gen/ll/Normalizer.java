package mesut.parserx.gen.ll;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            RuleDecl decl = tree.rules.get(i);
            decl.retType = new Type(tree.options.astClass, decl.baseName());
            transformRule(decl);
        }
    }

    @Override
    public Node visitGroup(Group node, Void parent) {
        String cls = curRule.baseName() + "g" + groupCount;
        RuleDecl tmp = new RuleDecl(cls, node.node);
        tmp.retType = new Type(tree.options.astClass, cls);
        tree.addRuleBelow(tmp, curRule);

        Name ref = tmp.ref.copy();
        if (node.astInfo.varName != null) {
            ref.astInfo.varName = node.astInfo.varName;
        }
        else {
            ref.astInfo.varName = "g" + groupCount;
        }
        groupCount++;
        tmp.rhs = transformNode(tmp.rhs, null);
        return ref;
    }

    @Override
    public Node visitOr(Or node, Void parent) {
        Node tmp = super.visitOr(node, parent);
        if (!tmp.isOr()) {
            return tmp;
        }
        List<Node> list = new ArrayList<>();
        for (Node ch : node) {
            if (!list.contains(ch)) {
                list.add(ch);
            }
            else {
                System.out.println("removed duplicate child " + ch + " in " + curRule);
            }
        }
        return new Or(list);
    }
}
