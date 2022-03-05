package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.lr.LrItem;
import mesut.parserx.gen.*;
import mesut.parserx.gen.transform.*;
import mesut.parserx.nodes.*;

import java.util.*;

public class LLDfaBuilder {
    public Name rule;
    public Tree tree;
    ItemSet firstSet;
    Map<Name, List<LrItem>> firstItems = new HashMap<>();
    Map<ItemSet, Name> firstSets = new HashMap<>();
    Set<ItemSet> finals = new HashSet<>();
    Set<ItemSet> all = new HashSet<>();
    Set<ItemSet> all2 = new HashSet<>();
    Queue<ItemSet> queue = new LinkedList<>();
    String type = "lr1";
    public static Name dollar = new Name("$", true);//eof
    boolean moved;

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        ItemSet.lastId = LrItem.lastId = 0;
    }

    void prepare() {
        new Normalizer(tree).normalize();
        tree.prepare();
        
        Tree res = new Tree(tree);
        res.checkDup = false;
        for(RuleDecl rd : tree.rules){
            Node rhs = rd.rhs;
            if(rhs.isOr()){
                for(Node ch : rhs.asOr()){
                    ch = plus(ch);
                    res.addRule(new RuleDecl(rd.ref, ch));
                }
            }else{
                rhs = plus(rhs);
                res.addRule(new RuleDecl(rd.ref, rhs));
            }
        }
        tree = res;
    }
    
    Sequence plus(Node node){
        Sequence s = node.isSequence()? node.asSequence() : new Sequence(node);
        List<Node> res = new ArrayList<>();
        for(Node ch : s){
            if(ch.isPlus()){
                Regex rn = ch.asRegex();
                res.add(rn.node.copy());
                Regex star = new Regex(rn.node.copy(), "*");
                star.astInfo = rn.astInfo.copy();
                res.add(star);
            }
            else{
                res.add(ch);
            }
        }
        return new Sequence(res);
    }
    
    public void factor(){
        for(RuleDecl rd : tree.rules){
            final boolean[] res = new boolean[1];
            Transformer t = new Transformer(tree){
                public Node visitOr(Or or, Void p){
                    for(int i = 0;i < or.size();i++){
                        //try seq
                        or.get(i).accept(this, null);
                        for(int j = i + 1 ; j < or.size();j++){
                            if(hasCommon(or.get(i), or.get(j))){
                                res[0] = true;
                            }    
                        }   
                    }
                    return or;
                }
                public Node visitSequence(Sequence seq, Void p){
                    //greedy norm
                    return seq;
                }    
            };
            rd.rhs.accept(t, null);
            if(res[0]){
                rule = rd.ref;
                build();
            }    
        }    
    }
    
    boolean hasCommon(Node a, Node b){
        return new FactorHelper(tree, new Factor(tree)).common(a, b) != null;
    }    

    public void build() {
        prepare();
        queue.clear();
        all.clear();
        //ItemSet.lastId = 0;
        //LrItem.lastId = 0;
        System.out.println("building " + rule +" in " + tree.file.getName());
        
        Set<ItemSet> done = new HashSet<>();
        
        firstSet = new ItemSet(tree, type);
        firstSet.isStart = true;
        
        for(RuleDecl rd : tree.getRules(rule)){
            LrItem first = new LrItem(rd, 0);
            first.lookAhead.add(dollar);
            firstSet.addItem(first);
            List<LrItem> list = firstItems.get(rd.ref);
            if(list == null){
                list = new ArrayList<>();
                firstItems.put(rd.ref, list);
            }
            list.add(first);
        }    
        
        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            ItemSet curSet = queue.poll();
            System.out.println("curSet = " + curSet.stateId);
            //closure here because it needs all items
            curSet.closure();
            Map<Name, List<LrItem>> map = new HashMap<>();
            for (LrItem item : curSet.all) {
                //System.out.println("item = " + item);
                //improve stars as non closured
                for(int i = item.dotPos;i < item.rhs.size(); i++){
                    if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if(item.closured[i]) continue;
                    
                    Node node = item.getNode(i);               
                    Name sym = node.isName() ? node.asName() : node.asRegex().node.asName();             
                    int newPos = node.isStar() ? i : i + 1;
                    LrItem target;
                    if(false && i > item.dotPos){
                        //prev part is epsilon, create correct reducer item
                        //a b .c? d? .e
                        List<Node> rhs = new ArrayList<>(item.rhs.list);
                        for(int j = 0;j < (i - item.dotPos);j++){
                            rhs.remove(item.dotPos);
                        }    
                        RuleDecl rd = new RuleDecl(item.rule.ref, new Sequence(rhs));
                        target = new LrItem(rd, item.dotPos + 1);
                    }else{
                        target = new LrItem(item,  newPos);
                    }
                    target.gotoSet2.add(curSet);
                    List<LrItem> list = map.get(sym);
                    if(list == null){
                        list = new ArrayList<>();
                        map.put(sym, list);
                    }else{
                        //factor
                        Name f = sym.copy();
                        f.astInfo.isFactor = true;
                        map.remove(sym);
                        map.put(f, list);
                        System.out.println("factor " + f);
                    }    
                    list.add(target);
                }
            }
            makeTrans(curSet, map);
        }
        moveReductions();
        //mergeFinals();
        //eliminate();
        all2.addAll(all);
    }
    
    void makeTrans(ItemSet curSet, Map<Name, List<LrItem>> map){
        System.out.println("makeTrans " + curSet.stateId + " " + map);
        for(Map.Entry<Name, List<LrItem>> e : map.entrySet()){
            Name sym = e.getKey();
            List<LrItem> list = e.getValue();
            ItemSet targetSet = new ItemSet(tree, type);
            targetSet.addAll(list);
            targetSet.addAll(targetSet.genReduces());
            
            ItemSet sim = findSimilar(new ArrayList<>(targetSet.kernel));
            if(sim != null){
                ItemSet.lastId--;
                //merge lookaheads
                for(LrItem it : targetSet.all){
                    sim.update(it);
                }
                targetSet = sim;
            }else{
                all.add(targetSet);
                queue.add(targetSet);
            }
            curSet.addTransition(sym, targetSet);
            System.out.printf("trans %d -> %d with %s\n", curSet.stateId,targetSet.stateId,sym);
        } 
    }    
    
    ItemSet  getTarget(ItemSet set, Name sym){
        for(Transition tr : set.transitions){
          if(tr.symbol.equals(sym)) return tr.target;
        }
        return null;
    }
    
    void sort(List<LrItem> list){
        Collections.sort(list, new Comparator<LrItem>(){
            public int compare(LrItem i1, LrItem i2){
                return Integer.compare(i1.rule.index, i2.rule.index);
            }  
         });   
    }    
    
    //find a set that has same kernel
    ItemSet findSimilar(List<LrItem> target){
        //System.out.println("similar " + target);
        sort(target);
        for(ItemSet set : all){
            if(target.size() != set.kernel.size()) continue;
            List<LrItem> l2 = new ArrayList<>(set.kernel);
            sort(l2);
            boolean same = true;
            for(int i = 0; i < target.size();i++){
                if(!target.get(i).isSame(l2.get(i))){
                    same = false;
                    break;
                }    
            }
            if(same) return set;
        }
        return null;
    }
    
    public void moveReductions(){
        if(true) return;
        moved = true;
        while(moved){
            moved = false;
            Set<LrItem> toRemove = new HashSet<>();
            for(ItemSet set : all){
                for(LrItem it : set.all){
                    if(!it.isReduce(tree)) continue;
                    if(it.lookAhead.contains(dollar)) continue;
                    //has shift reduce conflict?
                    moveReduction(set, it);
                    if(it.lookAhead.isEmpty()) toRemove.add(it);
                }
                for(LrItem it:toRemove){
                    for(int i = 0;i < set.all.size();i++){
                        if(it.isSame(set.all.get(i))){
                            set.all.remove(i);
                            System.out.println("deleted " + it);
                            break;
                        }    
                    }
                }    
            }
        }
    }
    
    void moveReduction(ItemSet set, LrItem it){
        //System.out.println("trace "+it);
        for(Transition tr:set.transitions){
            if(tr.target == set) continue;
            //if(!tr.symbol.isName()) continue;
            //System.out.println("sym "+tr.symbol.debug());
            if(tr.symbol.astInfo.isFactor){
                ItemSet target = tr.target;
                System.out.printf("moved %d -> %d %s\n", set.stateId, target.stateId, it);
                LrItem it2= new LrItem(it, it.dotPos);
                it.lookAhead.remove(tr.symbol.asName());
                it2.lookAhead.clear();
                for(Transition tr2:target.transitions){
                    //todo not all
                    Name sym = tr2.symbol.isSequence()?tr2.symbol.asSequence().last().asName():tr2.symbol.asName();
                    it2.lookAhead.add(sym);
                }
                target.addItem(it2);
                
                System.out.printf("new = %s\n", it2);
                moved = true;
            }else{
                Name sym = tr.symbol.isSequence()?tr.symbol.asSequence().last().asName():tr.symbol.asName();
                it.lookAhead.remove(sym);
                Name s = it.rule.ref.copy();
                s.name += "$";
                tr.symbol = seq(s, tr.symbol);
                moved = true;
            }    
        }    
    }
    
    Sequence seq(Node a, Node b){
        if(b.isSequence()){
            Sequence s = b.asSequence();
            s.list.add(0, a);
            return s;
        }else{
            return new Sequence(a,b);
        }    
    }    
    
    public void eliminate(){
        List<ItemSet> toRemove = new ArrayList<>();
        for(ItemSet set:all){
            boolean hasReduce = false;
            for(LrItem it:set.all){
                hasReduce |= it.isReduce(tree);
            }
            //   if(!hasReduce && !set.isStart 
            if(!set.isStart && canBeRemoved2(set)){
                System.out.println("rem " + set.stateId);
                //eliminate(set);
                //toRemove.add(set);
            }    
        }
        all.removeAll(toRemove);
    }
    
    boolean canBeRemoved(ItemSet set){
        //if it has 2 unique out going trans
        int cnt = 0;
        for(Transition tr:set.transitions){
            if(tr.target != set) cnt++;
        }
        return cnt <= 1;
    }
    
    boolean canBeRemoved2(ItemSet set){
        if(hasFinal(set)) return false;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for(Transition tr:set.transitions){
            if(tr.target != set){
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while(!queue.isEmpty()){
            ItemSet cur = queue.poll();
            for(Transition tr:cur.transitions){
                if(tr.target == set && reachFinal(cur, set)) return false;
                if(tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }
    
    boolean reachFinal(ItemSet from, ItemSet except){
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.stateId);
        while(!queue.isEmpty()){
            ItemSet cur = queue.poll();
            if(hasFinal(cur)) return true;
            for(Transition tr:cur.transitions){
                ItemSet trg = tr.target;
                if(trg == except) continue;
                if(visited.add(trg.stateId))  queue.add(trg);
            }
        }
        return false;
    }    
    
    boolean hasFinal(ItemSet set){
        for(LrItem it : set.all){
            
            if(it.isReduce(tree) && it.lookAhead.contains(dollar)) return true;
         }
         return false;
    }
    
    void eliminate(ItemSet set){
        System.out.println("removing " + set.stateId);
        Node loop = loopSym(set);
        for(ItemSet from:all){
            if(from == set) continue;
            List<Transition> toRemove = new ArrayList<>();
            List<Transition> toAdd = new ArrayList<>();
            for(Transition tr:from.transitions){
                if(tr.target != set) continue;
                toRemove.add(tr);
                for(Transition tr2:set.transitions){
                    if(tr2.target == set) continue;
                    if(loop == null){
                        toAdd.add(new Transition(from, new Sequence(tr.symbol, tr2.symbol), tr2.target));
                    }else{
                        toAdd.add(new Transition(from, new Sequence(tr.symbol, new Regex(loop, "*"), tr2.symbol), tr2.target));
                    }    
                }
            }
            from.transitions.removeAll(toRemove);
            from.transitions.addAll(toAdd);
        }
     }
     
     Node loopSym(ItemSet set){
         for(Transition tr:set.transitions){
             if(tr.target == set) return tr.symbol;
         }
         return null;   
     }
     
     void mergeFinals(){
         ItemSet ns = new ItemSet(tree, type);
         for(Iterator<ItemSet> it=all.iterator();it.hasNext();){
             ItemSet set=it.next();
             for(LrItem item:set.all){
                 if(item.lookAhead.contains(dollar) && item.isReduce(tree)){
                     ns.addAll(set.all);
                     for(Transition in:set.incomings){
                         in.target = ns;
                         System.out.printf("new1 %d -> %d\n", in.from.stateId,ns.stateId);
                     }
                     for(Transition tr:set.transitions){
                         tr.from = ns;
                         ns.addTransition(tr.symbol, tr.target);
                         System.out.printf("new2 %d -> %d\n", ns.stateId,tr.target.stateId);
                     }
                     it.remove();
                     System.out.printf("final %d\n", set.stateId);
                     break;
                 }    
             }    
         }
         System.out.printf("new final = %d\n", ns.stateId);
         all.add(ns);
     }    
    
    public void dot(java.io.PrintWriter w){
        w.println("digraph G{");
        //w.println("rankdir = TD");
        w.println("size=\"100,100\";");
        for(ItemSet set: all2){
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for(LrItem it : set.all){
                String line = it.toString() + " " + it.ids;
                line = line.replace(">", "&gt;");
                if(it.isReduce(tree)){
                    sb.append("<FONT color=\"blue\">");
                    sb.append(line);
                    sb.append("</FONT>");
                }else if(it.dotPos == 0){
                    sb.append("<FONT color=\"red\">");
                    sb.append(line);
                    sb.append("</FONT>");
                }    
                else{
                    sb.append(line);
                }
                sb.append("<BR ALIGN=\"LEFT\"/>");
            }
            sb.append(">");
            w.printf("%s [shape=box xlabel=\"%s\" label=%s]\n",set.stateId, set.stateId, sb);
            for(Transition tr : set.transitions){
              w.printf("%s -> %s [label=\"%s\"]\n",set.stateId, tr.target.stateId, tr.symbol);
            }
        }
        w.println("\n}");
        w.close();
    }
}
