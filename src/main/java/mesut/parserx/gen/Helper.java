package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.List;

public class Helper {

    public static Node trim(Sequence s) {
        List<Node> list = s.list.subList(1, s.size());
        if (list.size() == 1) {
            return list.get(0);
        }
        return new Sequence(list);
    }

    public static Node trim(Or s) {
        List<Node> list = s.list.subList(1, s.size());
        if (list.size() == 1) return list.get(0);
        return new Or(list);
    }


    //put back terminals as string nodes for good visuals
    public static void revert(final Tree tree) {
        Transformer transformer = new Transformer() {
            @Override
            public Node visitName(Name name, Void arg) {
                if (name.isToken) {
                    Node rhs = tree.getToken(name.name).rhs;
                    if (rhs.isString()) {
                        return rhs;
                    }
                }
                return name;
            }
        };
        for (RuleDecl ruleDecl : tree.rules) {
            ruleDecl.rhs = ruleDecl.rhs.accept(transformer, null);
        }
    }
}
