package mesut.parserx.gen.lr;

import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.*;

//split alts into separate decl
public class TreeInfo {
    public LinkedHashMap<String, List<Node>> nodeMap = new LinkedHashMap<>();
    public Tree tree;
    public HashMap<String, List<RuleDecl>> ruleMap = new HashMap<>();

    public static class TransformInfo {
        public boolean isPlus;
        public boolean isStar;
        public boolean isOpt;
        public String orgName;
    }

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
                var rd = new RuleDecl(name, rhs);
                var original = tree.getRule(name);
                rd.transformInfo = original.transformInfo;
                rd.which = id++;
                rd.index = index++;
                rd.retType = original.retType;
                rules.add(rd);
            }
            res.ruleMap.put(name, rules);
        }
        return res;
    }

    public static LinkedHashMap<String, List<Node>> makeMap(Tree input) {
        //LinkedHashMap preserves rule order
        var map = new LinkedHashMap<String, List<Node>>();
        for (var decl : input.rules) {
            var or = map.computeIfAbsent(decl.ref.name, k -> new ArrayList<>());
            if (decl.rhs.isOr()) {
                or.addAll(decl.rhs.asOr().list);
            }
            else {
                or.add(decl.rhs);
            }
        }
        return map;
    }
}
