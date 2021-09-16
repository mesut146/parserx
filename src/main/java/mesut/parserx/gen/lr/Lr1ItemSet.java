package mesut.parserx.gen.lr;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Sequence;
import mesut.parserx.nodes.Tree;

import java.util.*;


public class Lr1ItemSet extends LrItemSet {

    public static final boolean mergeLa = true;

    public Lr1ItemSet(List<LrItem> kernel, Tree tree) {
        this.kernel.addAll(kernel);
        this.tree = tree;
    }

    public Lr1ItemSet(LrItem kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
    }

    public void closure(Name node, LrItem sender) {
        if (node.isToken) {
            throw new RuntimeException("closure error on node: " + node + ", was expecting rule");
        }
        Set<Name> laList = new HashSet<>();
        //lookahead
        //first(follow(node),sender la)
        if (sender.getDotNode2() != null) {
            Name la = sender.getDotNode2();
            if (la.isToken) {
                laList.add(la);
            }
            else {
                //first of la becomes follow
                laList.addAll(first(la));
            }
        }
        if (laList.isEmpty()) {
            //first of sender
            laList.addAll(sender.lookAhead);
            //laList.add(sender.lookAhead.get(0));
        }
        //get rules
        List<RuleDecl> rules = tree.getRules(node.name);
        for (RuleDecl decl : rules) {
            if (mergeLa) {
                LrItem newItem = new LrItem(decl, 0);
                newItem.lookAhead = new HashSet<>(laList);
                newItem.gotoSet = this;
                addItem(newItem, node);
            }
            else {
                //create per one
                for (Name la : laList) {
                    LrItem newItem = new LrItem(decl, 0);
                    newItem.lookAhead.add(la);
                    newItem.gotoSet = this;
                    addItem(newItem, node);
                }
            }
        }
    }

    void addItem(LrItem item, Name node) {
        for (LrItem prev : all) {
            if (prev.isSame(item)) {
                //merge la
                prev.lookAhead.addAll(item.lookAhead);
                return;
            }
        }
        all.add(item);
        //!item.getDotNode().equals(node)
        if (item.isDotNonTerminal()) {
            closure(item.getDotNode(), item);
        }
    }

    //first terminals of rule
    public Set<Name> first(Name name) {
        //todo if first has epsilon look next
        Set<Name> list = new HashSet<>();
        for (RuleDecl decl : tree.getRules(name.name)) {
            Sequence node = decl.rhs.asSequence();
            Helper.first(node, tree, true, false, true);
            /*Name first = node.get(0).asName();
            if (first.isToken) {
                list.add(first);
            }
            else {
                //todo prevent left recursion
                list.addAll(first(first));
            }*/
        }
        return list;
    }

}
