package mesut.parserx.gen.ll;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//simplify a little for llrec
public class Normalizer extends SimpleTransformer {

    int groupCount = 1;
    Set<Name> map = new HashSet<>();//created rules

    public Normalizer(Tree tree) {
        super(tree);
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            groupCount = 1;
            //todo restart
            RuleDecl decl = tree.rules.get(i);
            decl.retType = new Type(tree.options.astClass, decl.baseName());
            if (!map.contains(decl.ref)) {
                decl.isOriginal = true;
            }
            transformRule(decl);
        }
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        String cls = curRule.baseName() + "g" + groupCount;
        RuleDecl tmp = new RuleDecl(cls, node.node);
        tmp.retType = new Type(tree.options.astClass, cls);
        tree.addRule(tmp);
        map.add(tmp.ref);

        Name ref = tmp.ref.copy();
        if (node.astInfo.varName != null) {
            ref.astInfo.varName = node.astInfo.varName;
        }
        else {
            ref.astInfo.varName = "g" + groupCount;
        }
        groupCount++;
        tmp.rhs = transformNode(tmp.rhs, tmp);
        return ref;
    }

    @Override
    public Node transformOr(Or node, Node parent) {
        Node tmp = super.transformOr(node, parent);
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
