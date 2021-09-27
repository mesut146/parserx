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
        super(kernel, tree);
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
        List<RuleDecl> rules = tree.getRules(node);
        for (RuleDecl decl : rules) {
            if (mergeLa) {
                LrItem newItem = new LrItem(decl, 0);
                newItem.lookAhead = new HashSet<>(laList);
                addItem0(newItem);
            }
            else {
                //create per one
                for (Name la : laList) {
                    LrItem newItem = new LrItem(decl, 0);
                    newItem.lookAhead.add(la);
                    addItem0(newItem);
                }
            }
        }
    }

    void addItem0(LrItem item) {
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
        return Helper.first(name, tree, true, false, true);
    }

}
