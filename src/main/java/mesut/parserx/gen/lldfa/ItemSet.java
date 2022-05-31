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
    public List < Transition > incomings = new ArrayList < > ();
    public Name symbol;

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

    public void addTransition(Node sym, ItemSet target) {
        for(Transition t:transitions){
            if(t.symbol.equals(sym) && t.target == target) return;
        }
        
        Transition tr = new Transition(this, sym, target);
        transitions.add(tr);
        target.incomings.add(tr);
    }
    
    public void addComing(Node sym, ItemSet from){
        incomings.add(new Transition(from, sym, this));
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
    
    void gen(LrItem it, List<LrItem> list){
         if (!it.isReduce(tree)) return;
         //is there any transition with my reduce symbol
            for (ItemSet gt: it.gotoSet2) {
                for (LrItem gti: gt.all) {
                    for(int i = gti.dotPos;i < gti.rhs.size();i++){
                        if(i > gti.dotPos && !FirstSet.canBeEmpty(gti.getNode(i - 1), tree)) break;
                        Name sym = sym(gti.getNode(i));
                        if (sym.equals(it.rule.ref)) {
                            int newPos = gti.getNode(i).isStar() ? i : i + 1;
                            LrItem target = new LrItem(gti, newPos);
                            if(target.isReduce(tree)){
                                it.reduceParent.add(target);
                                target.reduceChild = it;
                            }    
                            //target.gotoSet2.add(gt);
                            if(!list.contains(target)){
                                list.add(target);
                                gen(target, list);
                            }
                        }
                    }
                }
            }
    }

    public List<LrItem> genReduces() {
        List <LrItem > res = new ArrayList <>();
        for (LrItem it: kernel) {
            gen(it, res);
        }
        return res;
    }

    /*boolean hasReduce(LrItem item) {
        if (item.getDotNode() == null) return true;
        Sequence seq = item.rhs;
        if (item.dotPos == seq.size() - 1) {
            return FirstSet.canBeEmpty(seq.get(item.dotPos), tree);
        }
        return false;
    }*/

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
    
    boolean isFactor(LrItem item, int i){
    	List<Name> syms = symbols();
    	Node node = item.getNode(i);
        Name sym = sym(node);
        //check two consecutive syms have common
            for(int j = item.dotPos;j < item.rhs.size();j++){
                if(i == j) continue;
                if(j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                Node next = item.getNode(j);
                if(common(node, next)){
   	             return true;
                }
            }
            //check dot sym and any other sym have common
            for (Name s2 : syms) {
                    if (s2 == sym) continue;
                    if (common(sym, s2)) {
                        return true;
                    }
            }//for
            return false;
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
            addOrUpdate(newItem);
        }
    }
    
    void addOrUpdate(LrItem item){
        if (type.equals("lr0")) return;
        if(!update(item)){
            all.add(item);
            closure(item);
        }
    }    

    public boolean update(LrItem item) {
        for (LrItem prev: all) {
            if (!prev.isSame(item)) continue;
            //merge la
            prev.lookAhead.addAll(item.lookAhead);
            prev.ids.addAll(item.ids);
            //update other items too
            /*if (item.isDotNonTerminal()) {
                for (LrItem cl: all) {
                    if (cl.sender == prev) {
                        cl.lookAhead.addAll(prev.lookAhead);
                        update(cl);
                    }
                }
            }*/
            return true;
        }
        return false;
    }
    

}