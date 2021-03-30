package mesut.parserx.gen.ll;

import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class LLGen {
    Tree tree;

    public LLGen(Tree tree) {
        this.tree = tree;
    }

    void init() {
        EbnfToBnf.expand_or = true;
        EbnfToBnf.combine_or = false;
        tree = EbnfToBnf.transform(tree);
    }

    public void gen() {
        init();

        LLTable table = new LLTable();
        for (RuleDecl ruleDecl : tree.rules) {
            Node node = ruleDecl.rhs;
            Set<NameNode> first = Helper.first(node, tree, true);
            Set<NameNode> tmp = new HashSet<>();
            for (NameNode name : first) {
                if (name.isToken) {
                    tmp.add(name);
                }
            }
            first = tmp;
            if (Helper.canBeEmpty(node, tree)) {
                Set<NameNode> follow = new HashSet<>();
                Helper.follow(ruleDecl.ref(), tree, follow);
                for (NameNode name : follow) {
                    table.set(ruleDecl.ref(), name, ruleDecl.index);
                }
            }
            else {
                for (NameNode name : first) {
                    table.set(ruleDecl.ref(), name, ruleDecl.index);
                }
            }
        }
        System.out.println(table.idMap);
    }

    static class LLTable {
        Map<NameNode, Map<NameNode, Integer>> idMap = new HashMap<>();
        List<NameNode> tokens = new ArrayList<>();

        void set(NameNode rule, NameNode token, int id) {
            if (!token.isToken) {
                throw new RuntimeException("non terminal cell ");
            }
            Map<NameNode, Integer> list = idMap.get(rule);
            if (list == null) {
                list = new HashMap<>();
                idMap.put(rule, list);
            }
            list.put(token, id);
        }
    }

}
