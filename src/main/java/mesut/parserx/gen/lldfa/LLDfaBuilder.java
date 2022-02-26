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
    Set<ItemSet> all = new HashSet<>();
    Set<ItemSet> all2 = new HashSet<>();
    Queue<ItemSet> queue = new LinkedList<>();
    int lastId = 0;
    String type = "lr1";
    public static Name dollar = new Name("$", true);//eof

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
        //ItemSet.lastId = 0;
        //LrItem.lastId = 0;
        System.out.println("building " + rule);
        
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
                    if(i > item.dotPos){
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
        all2.addAll(all);
    }
    
    void makeTrans(ItemSet curSet, Map<Name, List<LrItem>> map){
        System.out.println("makeTrans " + curSet.stateId + " " + map);
        for(Map.Entry<Name, List<LrItem>> e : map.entrySet()){
            Name sym = e.getKey();
            List<LrItem> list = e.getValue();
            ItemSet s = new ItemSet(tree, type);
            s.addAll(list);
            s.addAll(s.genReduces());
            ItemSet.lastId--;
            list = new ArrayList<>(s.kernel);
            ItemSet targetSet = findSimilar(list);
            if(targetSet != null){
                //System.out.println("found similar " + targetSet.stateId);
                curSet.addTransition(sym, targetSet);
            }else{
                targetSet = new ItemSet(tree, type);
                targetSet.addAll(list);
                all.add(targetSet);
                curSet.addTransition(sym, targetSet);
                queue.add(targetSet);
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
    
    public void normalize(){
        for(ItemSet set : all){
            for(LrItem it : set.all){
                if(!it.isReduce(tree)) continue;
                //has shift reduce conflict?
                trace(set, it);
            }    
        }    
    }
    
    void trace(ItemSet set, LrItem it){
        System.out.println("trace "+it);
        for(Transition tr:set.transitions){
            //System.out.println("sym "+tr.symbol.debug());
            if(tr.symbol.astInfo.isFactor){
                ItemSet target = tr.target;
                target.addItem(it);
                System.out.println("moved "+ set.stateId + " " + it);
                //set.all.remove(it);
            }    
        }    
    }    
    
    public void dot(java.io.PrintWriter w){
        w.println("digraph G{");
        w.println("rankdir = TD");
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
