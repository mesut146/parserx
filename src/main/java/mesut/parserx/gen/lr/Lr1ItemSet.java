package mesut.parserx.gen.lr;

import mesut.parserx.nodes.NameNode;
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

    @Override
    public void closure() {
        if (all.isEmpty()) {
            all.addAll(kernel);
            for (LrItem item : kernel) {
                closure(item);
            }
        }
    }

    @Override
    public void closure(LrItem it) {
        if (it.isDotNonTerminal()) {
            closure(it.getDotNode(), it);
        }
    }

    public void closure(NameNode node, LrItem sender) {
        if (node.isToken) {
            throw new RuntimeException("closure error on node: " + node + ", was expecting rule");
        }
        //get rules
        List<RuleDecl> rules = tree.getRules(node.name);
        for (RuleDecl decl : rules) {
            List<NameNode> laList = new ArrayList<>();
            //page 261
            //lookahead
            //first(follow(node),sender la)
            if (sender.getDotNode2() != null) {
                NameNode la = sender.getDotNode2();
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
            if (mergeLa) {
                LrItem newItem = new LrItem(decl, 0);
                newItem.lookAhead = laList;
                newItem.gotoSet = this;
                addItem(newItem, node);
            }
            else {
                //create per one
                for (NameNode la : laList) {
                    LrItem newItem = new LrItem(decl, 0);
                    newItem.lookAhead.add(la);
                    newItem.gotoSet = this;
                    addItem(newItem, node);
                }
            }
        }
    }

    void addItem(LrItem item, NameNode node) {
        if (all.contains(item)) {
            return;
        }
        all.add(item);
        if (item.isDotNonTerminal() && !item.getDotNode().equals(node)) {
            closure(item);
        }
    }

    //first terminals of rule
    public Set<NameNode> first(NameNode nameNode) {
        //todo if first has epsilon look next
        Set<NameNode> list = new HashSet<>();
        for (RuleDecl decl : tree.getRules(nameNode.name)) {
            Sequence node = decl.rhs.asSequence();
            handleFirst(node.get(0).asName(), list);
        }
        return list;
    }

    void handleFirst(NameNode node, Set<NameNode> list) {
        if (node.asName().isToken) {
            list.add(node);
        }
        else {
            //todo prevent left recursion
            list.addAll(first(node));
        }
    }

}
