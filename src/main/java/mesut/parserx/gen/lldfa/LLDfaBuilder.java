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
    Set<ItemSet> all = new HashSet<>();
    Set<ItemSet> all2 = new HashSet<>();
    Queue<ItemSet> queue = new LinkedList<>();
    int lastId = 0;

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
    }

    void prepare() {
        new Normalizer(tree).normalize();
        tree.prepare();
        
        Tree res = new Tree(tree);
        res.checkDup = false;
        for(RuleDecl rd : tree.rules){
            Node rhs = rd.rhs;
            if(rhs.isOr()){
                int id = 0;
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
        
        //first.lookAhead.add(LrDFAGen.dollar);
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
        //lastId = 0;
        System.out.println("building " + rule);
        
        Set<ItemSet> done = new HashSet<>();
        
        firstSet = new ItemSet(tree, "lr0");
        firstSet.isStart = true;
        
        for(RuleDecl rd : tree.getRules(rule)){
            LrItem first = new LrItem(rd, 0);
            first.ids.add(lastId++);
            firstSet.addItem(first);
        }    
        
        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            ItemSet curSet = queue.poll();
            //closure here because it needs all items
            curSet.closure();
            Map<Name, List<LrItem>> map = new HashMap<>();
            for (LrItem item : curSet.all) {
                //improve stars as non closured
                Node symNode = item.getDotNode();
                 if (symNode == null) continue;
                 if(!item.closured2 && FirstSet.canBeEmpty(symNode, tree)){
                     Name next = item.getDotSym2();
                     if(next != null){
                         int np = item.getDotNode2().isStar() ? item.dotPos + 1: item.dotPos + 2;
                         LrItem nextItem = new LrItem(item,  np);
                         nextItem.gotoSet2.add(curSet);
                         List<LrItem> list = map.get(next);
                         if(list == null){
                             list = new ArrayList<>();
                             map.put(next, list);
                         }
                         list.add(nextItem);
                         //handle(curSet, next, nextItem);
                     } 
                 }
                Name sym = item.getDotSym();
                if(item.closured1) continue;
                int newPos = symNode.isStar() ? item.dotPos : item.dotPos + 1;
                LrItem target = new LrItem(item,  newPos);
                target.gotoSet2.add(curSet);
                List<LrItem> list = map.get(sym);
                if(list == null){
                    list = new ArrayList<>();
                    map.put(sym, list);
                }
                list.add(target);
                //handle(curSet, sym, target);
            }
            makeTrans(curSet, map);
        }
        all2.addAll(all);
    }
    
    void makeTrans(ItemSet curSet, Map<Name, List<LrItem>> map){
        System.out.println("makeTrans " + curSet.stateId + " " + map);
        for(Map.Entry<Name, List<LrItem>> e : map.entrySet()){
                Name sym = e.getKey();
                List<LrItem> list = e.getValue();
                ItemSet s = new ItemSet(tree, "lr0");
                s.addAll(list);
                s.addAll(s.genReduces());
                list = new ArrayList<>(s.kernel);
                ItemSet targetSet = getTarget(curSet, sym);
                if(targetSet != null){
                    for(LrItem target : list){
                         targetSet.addItem(target);
                         //queue.add(targetSet);
                     }
                }else{
                    targetSet = findSimilar(list);
                    if(targetSet != null){
                        System.out.println("found similar " + targetSet.stateId);
                        curSet.addTransition(sym, targetSet);
                    }else{
                        targetSet = new ItemSet(tree, "lr0");
                        for(LrItem it : list){
                            targetSet.addItem(it);
                        }
                        all.add(targetSet);
                        curSet.addTransition(sym, targetSet);
                        queue.add(targetSet);
                    }
                }    
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
    
    ItemSet findSimilar(List<LrItem> target){
        System.out.println("similar " + target);
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
    
    public void dot(java.io.PrintWriter w){
        w.println("digraph G{");
        w.println("rankdir = TD");
        w.println("size=\"100,100\";");
        for(ItemSet set: all2){
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for(LrItem it : set.all){
                String line = it.toString();
                line = line.replace(">", "&gt;");
                if(it.hasReduce()){
                    sb.append("<FONT color=\"blue\">");
                    sb.append(line);
                    sb.append("</FONT>");
                }else{
                    sb.append(line);
                }
                sb.append("<BR/>");
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
