package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.Map;

//shortens rule names
public class Minifier extends SimpleTransformer {
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
        decl.ref = transformName(decl.ref, null);
        decl.rhs = transformNode(decl.rhs, null);
        return decl;
    }

    @Override
    public Name transformName(Name name, Node parent) {
        if (name.isRule()) {
            Name res;
            if (map.containsKey(name)) {
                res = map.get(name);
            }
            else {
                res = new Name(prefix + count);
                map.put(name, res);
            }
            return res;
        }
        return name;
    }
}
