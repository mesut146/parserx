package mesut.parserx.gen.lr;

import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class TreeInfo {
    public LinkedHashMap<String, List<Node>> nodeMap = new LinkedHashMap<>();
    public Tree tree;
    public HashMap<String, List<RuleDecl>> ruleMap = new HashMap<>();

    public static TreeInfo make(Tree tree) {
        var res = new TreeInfo();
        res.tree = tree;
        res.nodeMap = EbnfToBnf.makeMap(tree);
        int index = 0;
        for (var entry : res.nodeMap.entrySet()) {
            var name = entry.getKey();
            int id = entry.getValue().size() == 1 ? 0 : 1;
            var rules = new ArrayList<RuleDecl>();
            for (var rhs : entry.getValue()) {
                var rd = new RuleDecl(name, rhs);
                rd.which = id++;
                rd.index = index++;
                rules.add(rd);
            }
            res.ruleMap.put(name, rules);
        }
        return res;
    }
}
