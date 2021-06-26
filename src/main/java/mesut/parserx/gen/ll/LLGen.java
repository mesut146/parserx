package mesut.parserx.gen.ll;

import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.io.PrintWriter;
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
            Set<Name> first = Helper.first(node, tree, true);
            Set<Name> tmp = new HashSet<>();
            for (Name name : first) {
                if (name.isToken) {
                    tmp.add(name);
                }
            }
            first = tmp;
            if (Helper.canBeEmpty(node, tree)) {
                Set<Name> follow = new HashSet<>();
                Helper.follow(ruleDecl.ref(), tree, follow);
                for (Name name : follow) {
                    table.set(ruleDecl.ref(), name, ruleDecl.index);
                }
            }
            else {
                for (Name name : first) {
                    table.set(ruleDecl.ref(), name, ruleDecl.index);
                }
            }
        }
        System.out.println(table.idMap);
    }

    public void dot(PrintWriter writer) {

    }

    static class LLTable {
        Map<Name, Map<Name, Integer>> idMap = new HashMap<>();
        List<Name> tokens = new ArrayList<>();

        void set(Name rule, Name token, int id) {
            if (!token.isToken) {
                throw new RuntimeException("non terminal cell ");
            }
            Map<Name, Integer> list = idMap.get(rule);
            if (list == null) {
                list = new HashMap<>();
                idMap.put(rule, list);
            }
            list.put(token, id);
        }
    }

}
