package mesut.parserx.gen.ll;

import mesut.parserx.nodes.*;

//simplify a little for llrec
public class Normalizer extends SimpleTransformer {

    int groupCount = 1;

    public Normalizer(Tree tree) {
        super(tree);
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            groupCount = 1;
            transformRule(tree.rules.get(i));
        }
    }

    @Override
    public RuleDecl transformRule(RuleDecl decl) {
        if (decl.original == null) {
            decl.original = decl.ref();
        }
        return super.transformRule(decl);
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        String cls = curRule.original.name + "g" + groupCount;
        //String varName = "g" + groupCount++;
        groupCount++;
        RuleDecl tmp = new RuleDecl(cls, node.node);
        tmp.original = curRule.original;
        tree.addRule(tmp);
        new Normalizer(tree) {{
            groupCount = Normalizer.this.groupCount;
        }}.transformRule(tmp);
        return tmp.ref();
    }
}