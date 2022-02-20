package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;
import mesut.parserx.gen.*;
import mesut.parserx.gen.lr.*;
import mesut.parserx.gen.transform.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemSet {
    public Set <LrItem> kernel = new HashSet < > ();
    public List <LrItem> all = new ArrayList < > ();
    public List <LrItem> reduce = new ArrayList < > ();
    public boolean isStart = false;
    public int stateId = -1;
    static int lastId = 0;
    public String type;
    Tree tree;
    public List < Transition > transitions = new ArrayList < > ();

    public ItemSet(LrItem kernel, Tree tree, String type) {
        this.kernel.add(kernel);
        all.add(kernel);
        this.tree = tree;
        this.type = type;
        stateId = lastId++;
    }
    public ItemSet(Tree tree, String type) {
        this.tree = tree;
        this.type = type;
        stateId = lastId++;
    }

    public void addItem(LrItem item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
        }
    }
    
    public void addAll(List<LrItem> list){
        for(LrItem it : list){
            addItem(it);
        }    
    }   

    public void addTransition(Name sym, ItemSet target) {
        transitions.add(new Transition(sym, target));
    }

    public boolean hasReduce() {
        return !getReduce().isEmpty();
    }

    public List < LrItem > getReduce() {
        List < LrItem > list = new ArrayList < > ();
        for (LrItem item: all) {
            if (item.hasReduce()) {
                list.add(item);
            }
        }
        return list;
    }

    public List<LrItem> genReduces() {
        List <LrItem > res = new ArrayList <>();
        for (LrItem it: kernel) {
            if (!hasReduce(it)) continue;
            //is there any transition with my reduce symbol
            for (ItemSet gt: it.gotoSet2) {
                for (LrItem gti: gt.all) {
                    Name sym = gti.getDotSym();
                    if (sym == null || sym.isToken) continue;
                    if (sym.equals(it.rule.ref)) {
                        int newPos = gti.getDotNode().isStar() ? gti.dotPos : gti.dotPos + 1;
                        LrItem target = new LrItem(gti, newPos);
                        target.gotoSet2.add(gt);
                        res.add(target);
                    }
                    /*if (FirstSet.canBeEmpty(gti.getDotNode(), tree)) {
                        LrItem target2 = new LrItem(gti, gti.dotPos + 2);
                        target2.gotoSet2.add(gt);
                        toAdd.add(target2);
                    }*/
                }
            }
        }
        return res;
    }

    boolean hasReduce(LrItem item) {
        if (item.getDotNode() == null) return true;
        Sequence seq = item.rule.rhs.asSequence();
        if (item.dotPos == seq.size() - 1) {
            return FirstSet.canBeEmpty(seq.get(item.dotPos), tree);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(all.get(i).toString2(tree));
            if (i < all.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public void closure() {
        addAll(genReduces());
        for (LrItem item: kernel) {
            closure(item);
        }
        for (LrItem it: all) {
            if (it.dotPos == 0) {
                it.gotoSet2.add(this);
            }
        }
    }

    public void closure(LrItem item) {
        //if two alternations are different and have common factor
        //closure is forced to reveal factor
        Name s1 = item.getDotSym();
        if (s1 == null || s1.isToken) return;
        /*if (isStart || stateId == 0) {
            item.closured1 = true;
            closure(s1, item);
            return;
        }*/
        List<Name> syms = symbols();
        if (FirstSet.canBeEmpty(s1, tree) && item.getDotNode2() != null) {
            Name next = item.getDotSym2();
            //two consecutive syms
            if (new FactorHelper(tree, new Factor(tree)).common(s1, next) != null) {
                    item.closured1 = true;
                    closure(s1, item);
                    if(next.isRule()){
                        item.closured2 = true;
                        closure(next, item);
                    }
                    return;
            }
            if(next.isRule()){
                //second and other
                for (Name s2 : syms) {
                    if (s2 == s1 || s2 == next) continue;
                    //if (next.equals(s2) || (next.isToken && s2.isToken)) continue;
                    if (new FactorHelper(tree, new Factor(tree)).common(next, s2) != null) {
                        item.closured2 = true;
                        closure(next, item);
                        return;
                    }
                }//for
            }
        }
        for(Name s2 : syms){
            if(s1 == s2) continue;
            //if (s1.equals(s2) || (s1.isToken && s2.isToken)) continue;
            if (new FactorHelper(tree, new Factor(tree)).common(s1, s2) != null) {
                item.closured1 = true;
                closure(s1, item);
                return;
            }
        }    
    }
    
    List<Name> symbols(){
        List<Name> res = new ArrayList<>();
        for(LrItem it : all){
            Name s1 = it.getDotSym();
            if(s1 == null) continue;
            res.add(s1);
            if(FirstSet.canBeEmpty(s1, tree)){
                Name s2 = it.getDotSym2();
                if(s2 == null) continue;
                res.add(s2);
            }
        }
        return res;
    }    

    private void closure(Name node, LrItem sender) {
        if (node.isToken) return;

        Set < Name > laList = sender.follow(tree);
        for (RuleDecl decl: tree.getRules(node)) {
            LrItem newItem = new LrItem(decl, 0);
            if (!type.equals("lr0")) {
                newItem.lookAhead = new HashSet < > (laList);
            }
            newItem.sender = sender;
            addItem0(newItem);
        }
    }

    void addItem0(LrItem item) {
        for (LrItem prev: all) {
            if (!prev.isSame(item)) continue;
            if (type.equals("lr0")) {
                return;
            }
            //merge la
            prev.lookAhead.addAll(item.lookAhead);
            //update other items too
            if (item.isDotNonTerminal()) {
                for (LrItem cl: all) {
                    if (cl.sender == prev) {
                        cl.lookAhead.addAll(prev.lookAhead);
                        addItem0(cl);
                    }
                }
            }
            return;
        }
        all.add(item);
        closure(item);
    }

}