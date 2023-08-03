package mesut.parserx.gen;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Transformer;
import mesut.parserx.nodes.Tree;

import java.util.HashMap;
import java.util.Map;

//shortens rule names
public class Minifier extends Transformer {
    Map<Name, Name> map = new HashMap<>();
    int count = 1;
    String prefix = "E";

    public Minifier(Tree tree) {
        super(tree);
    }

    public void minify() {
        transformRules();
    }

    @Override
    public RuleDecl transformRule(RuleDecl decl) {
        decl.ref = visitName(decl.ref, null);
        decl.rhs = decl.rhs.accept(this, null);
        return decl;
    }

    @Override
    public Name visitName(Name name, Void parent) {
        if (name.isRule()) {
            Name res;
            if (map.containsKey(name)) {
                res = map.get(name);
            } else {
                res = new Name(prefix + count);
                map.put(name, res);
            }
            return res;
        }
        return name;
    }
}
