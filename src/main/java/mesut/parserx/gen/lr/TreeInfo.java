package mesut.parserx.gen.lr;

import mesut.parserx.nodes.*;

import java.util.*;

//split alts into separate decl
public class TreeInfo {
    public LinkedHashMap<Name, List<Node>> nodeMap = new LinkedHashMap<>();
    public Tree tree;
    public HashMap<Name, List<RuleDecl>> ruleMap = new HashMap<>();

    public static TreeInfo make(Tree tree) {
        var res = new TreeInfo();
        res.tree = tree;
        res.nodeMap = makeMap(tree);
        int index = 0;
        for (var entry : res.nodeMap.entrySet()) {
            var name = entry.getKey();
            int id = entry.getValue().size() == 1 ? 0 : 1;
            var rules = new ArrayList<RuleDecl>();
            for (var rhs : entry.getValue()) {
                if (!rhs.isSequence()) rhs = new Sequence(rhs);
                var rd = new RuleDecl(name, rhs);
                var original = tree.getRule(name);
                rd.transformInfo = original.transformInfo;
                if (id != 0) {
                    rd.which = Optional.of(id++);
                }
                rd.index = index++;
                rd.retType = original.retType;
                rules.add(rd);
            }
            res.ruleMap.put(name, rules);
        }
        return res;
    }

    public static LinkedHashMap<Name, List<Node>> makeMap(Tree input) {
        //LinkedHashMap preserves rule order
        var map = new LinkedHashMap<Name, List<Node>>();
        for (var decl : input.rules) {
            var or = map.computeIfAbsent(decl.ref, k -> new ArrayList<>());
            if (decl.rhs.isOr()) {
                or.addAll(decl.rhs.asOr().list);
            } else {
                or.add(decl.rhs);
            }
        }
        return map;
    }

    public static class TransformInfo {
        public boolean isPlus;
        public boolean isStar;
        public boolean isOpt;
        public String orgName;
    }
}
