package mesut.parserx.gen.ll;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//simplify a little for llrec
public class Normalizer extends SimpleTransformer {

    int groupCount = 1;
    Map<String, RuleDecl> map = new HashMap<>();

    public Normalizer(Tree tree) {
        super(tree);
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            groupCount = 1;
            //todo restart
            RuleDecl decl = tree.rules.get(i);
            decl.retType = decl.ref();
            if (!map.containsKey(decl.name)) {
                decl.isOriginal = true;
            }
            transformRule(decl);
        }
    }

    RuleDecl getParent() {
        if (map.containsKey(curRule.name)) {
            //group
            return map.get(curRule.name);
        }
        //not group,so original rule
        return curRule;
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        RuleDecl original = getParent();
        String cls = original.name + "g" + groupCount;
        RuleDecl tmp = new RuleDecl(cls, node.node);
        tmp.retType = tmp.ref();
        map.put(cls, original);
        tree.addRule(tmp);
        Name ref = tmp.ref();
        ref.astInfo.varName = "g" + groupCount++;
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
