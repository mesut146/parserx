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
    public static int lastId = 0;
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
            if (!it.isReduce(tree)) continue;
            //is there any transition with my reduce symbol
            for (ItemSet gt: it.gotoSet2) {
                for (LrItem gti: gt.all) {
                    for(int i = gti.dotPos;i < gti.rhs.size();i++){
                        if(i > gti.dotPos && !FirstSet.canBeEmpty(gti.getNode(i - 1), tree)) break;
                        Name sym = sym(gti.getNode(i));
                        if (sym.equals(it.rule.ref)) {
                            int newPos = gti.getNode(i).isStar() ? i : i + 1;
                            LrItem target = new LrItem(gti, newPos);
                            target.gotoSet2.add(gt);
                            res.add(target);
                        }
                    }
                }
            }
        }
        return res;
    }

    boolean hasReduce(LrItem item) {
        if (item.getDotNode() == null) return true;
        Sequence seq = item.rhs;
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
    
    boolean common(Node s1, Node s2){
        return new FactorHelper(tree, new Factor(tree)).common(s1, s2) != null;
    }
    
    Name sym(Node node){
        return node.isName() ? node.asName() : node.asRegex().node.asName();
    }    

    public void closure(LrItem item) {
        //if dot sym have common factor ,closure is forced to reveal factor
        List<Name> syms = symbols();
        for(int i = item.dotPos;i < item.rhs.size();i++){
            if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
            Node node = item.getNode(i);
            Name sym = sym(node);
            if(sym.isToken) continue;
            //check two consecutive syms have common
            for(int j = item.dotPos;j < item.rhs.size();j++){
                if(i == j) continue;
                if(j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                Node next = item.getNode(j);
                if(common(node, next)){
                    item.closured[i] = true;
                    closure(sym, item);
                    break;
                }
            }
            if(item.closured[i]) continue;
            //check dot sym and any other sym have common
            for (Name s2 : syms) {
                    if (s2 == sym) continue;
                    if (common(sym, s2)) {
                        item.closured[i] = true;
                        closure(sym, item);
                        break;
                    }
            }//for
        }    
    }
    
    List<Name> symbols(){
        List<Name> res = new ArrayList<>();
        for(LrItem item : all){
            for(int i = item.dotPos;i < item.rhs.size();i++){
                if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                Node node = item.getNode(i);
                res.add(sym(node));
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