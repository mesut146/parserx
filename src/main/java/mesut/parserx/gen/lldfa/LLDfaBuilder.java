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
    Map<LrItem, Name> reducers = new HashMap<>();
    Set<ItemSet> extras = new HashSet<>();

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
            Map<Node, List<LrItem>> map = new HashMap<>();
            for (LrItem item : curSet.all) {
                //System.out.println("item = " + item);
                //improve stars as non closured
                for(int i = item.dotPos;i < item.rhs.size(); i++){
                    if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if(item.closured[i]) continue;
                    
                    Node node = item.getNode(i);               
                    Node sym = node.isName() ? node.asName() : node.asRegex().node.asName();
                    int newPos = node.isStar() ? i : i + 1;
                    LrItem target;
                    if(node.isOptional() && sym.asName().isToken && !curSet.isFactor(item, i)){
                    	//.a? b c | b d -> a b c
						List<Node> rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
						rhs.set(0, sym);
                        sym = Sequence.make(rhs);
                        target = new LrItem(item, item.rhs.size());
                        System.out.println("shrink opt="+sym);
                    }
                    //if sym is not factor shrink transition
                    else if(canShrink(curSet, item, i)){
                    	List<Node> rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        sym = Sequence.make(rhs);
                        target = new LrItem(item, item.rhs.size());
                        System.out.println("shrink="+sym);
                        target.gotoSet2.add(curSet);
                        addMap(map, sym, target);
                        break;
                    }
                    else if(false && i > item.dotPos){
                        //prev part is epsilon, create correct reducer item
                        //a b .c? d? e | a b .d f
                        List<Node> rhs = new ArrayList<>(item.rhs.list);
                        for(int j = item.dotPos;j < i;j++){
                            rhs.remove(item.dotPos);
                        }
                        if(node.isOptional()){
                            rhs.set(item.dotPos, sym);
                        }    
                        RuleDecl rd = new RuleDecl(item.rule.ref, new Sequence(rhs));
                        target = new LrItem(rd, item.dotPos + 1);
                    }else{
                    	target = new LrItem(item,  newPos);
                    }
                    target.gotoSet2.add(curSet);
                    addMap(map, sym, target);
             
                }
            }
            makeTrans(curSet, map);
        }
        //moveReductions();
        //mergeFinals();
        //eliminate();
        all2.addAll(all);
    }
    
    public void write(){
        CodeWriter w = new CodeWriter();
        if (options.packageName != null) {
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("");
        w.append("public class %s{", options.parserClass);
        
        w.append("%s lexer;", options.lexerClass);
        w.append("%s la;", options.tokenClass);
        
        w.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);

        w.all("this.lexer = lexer;\nla = lexer.next();\n}");
        w.append("");

        writeConsume();
        
        void writeRule();
        
        w.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(w.get(), file);
    }
    
    void writeRule(){
        Map<LrItem, String> names = new HashMap<>();
        for(ItemSet set : all){
            if(set.isStart){
                w.append("public %s %s(){", rule.type, rule.name);
                //create res
                w.append("%s res = new %s();", rule.type, rule.type);
            }else{
                w.append("public %s S%d{", rule.type, set.stateId);
            }
            if(set.symbol != null){
                w.append("%s f = consume(Tokens.%s);", options.tokenClass, set.symbol.name);
            }
            int cnt = 0;
            Map<Name, List<LrItem>> map = new HashMap<>();
            for(LrItem item : set.all){
                //factor consume & assign
                //node creations
                //if reduce then assign
                
                if(item.dotPos == 0){
                    Type type = item.;
                    String name = "v" + cnt++;
                    w.append("%s %s = new %s();", name);
                    names.put(item, name);
                }else if(item.getNode(item.dotPos - 1).equals(set.symbol)){
                    String name = names.get(item);
                    w.append("%s.%s = f;", name, item.getNode(item.dotPos - 1).astInfo.varName);
                }else if(item.isReduce()){
                    //assign parent
                    for(LrItem parent : item.reduceParent){
                        Node p=parent.getNode(parent.doPos - 1);
                        w.append("%s.%s = %s;", names.get(parent), p.astInfo.varName, names.get(item));
                    }
                }
            }
            //collect syms
            
            w.append("}");
        }
    }
    
    boolean canShrink(ItemSet set, LrItem item, int i){
    	Node node = item.getNode(i);               
        Name sym = node.isName() ? node.asName() : node.asRegex().node.asName();
                   
        if(node.isName() && sym.isToken && !set.isFactor(item, i)) return true;
        if(set.isFactor(item, i)) return false;
        if(!Helper.canBeEmpty(node, tree)) return true;//not closured
        return !isFollowHasFactor(set, item, i);
    }
    
    boolean isFollowHasFactor(ItemSet set, LrItem item, int pos){
    	for(int i = pos + 1;i < item.rhs.size();i++){
    		//if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
    		if(set.isFactor(item, i)) return true;
    		if(!FirstSet.canBeEmpty(item.getNode(i), tree)) break;
    	}
    	return false;
    }
    
    void addMap(Map<Node, List<LrItem>> map, Node sym, LrItem target){
    	List<LrItem> list = map.get(sym);
                    if(list == null){
                        list = new ArrayList<>();
                        map.put(sym, list);
                    }else{
                        //factor
                        Node f = sym.copy();
                        f.astInfo.isFactor = true;
                        map.remove(sym);
                        map.put(f, list);
                        System.out.println("factor " + f);
                    }    
                    list.add(target);
                    if(target.symbol != null && target.symbol.equals(sym)){
                        new RuntimeException("invalid state: multi symbol");
                    }
                    target.symbol = sym;
    }
    
    void makeTrans(ItemSet curSet, Map<Node, List<LrItem>> map){
        System.out.println("makeTrans " + curSet.stateId + " " + map);
        for(Map.Entry<Node, List<LrItem>> e : map.entrySet()){
            Node sym = e.getKey();
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
    
    public void createReducers(){
        for(ItemSet set : all){
            for(LrItem it : set.all){
                if(!it.isReduce(tree)) continue;
                if(it.lookAhead.contains(dollar)) continue;
                //createReducer(it, set);
            }
        }    
    }    
    
    public void moveReductions(){
        Queue<ItemSet> q = new LinkedList<>();
        q.addAll(all);
        while(!q.isEmpty()){
            Set<LrItem> toRemove = new HashSet<>();
            ItemSet set = q.poll();
                //clear sub reductions, they only needed for reducers
                for(Iterator<LrItem> it = set.all.iterator();it.hasNext();){
                    LrItem item = it.next();
                    if(!item.isReduce(tree)) continue;
                    if(item.lookAhead.contains(dollar)) continue;
                    if(!item.reduceParent.isEmpty()){
                        it.remove();
                        System.out.println("rem sub = " + item);
                    }
                }
                //move parent reductions
                for(LrItem it : set.all){
                    if(!it.isReduce(tree)) continue;
                    if(it.lookAhead.contains(dollar)) continue;
                    moveReduction(set, it, q);
                    if(it.lookAhead.isEmpty()) toRemove.add(it);
                }
                for(LrItem it:toRemove){
                    for(int i = 0;i < set.all.size();i++){
                        if(it.isSame(set.all.get(i))){
                            set.all.remove(i);
                            set.kernel.remove(it);
                            System.out.println("deleted " +set.stateId +" "+ it);
                            break;
                        }    
                    }
                }    
            
        }
    }
    
    List<LrItem> findParents(LrItem it){
        List<LrItem> parents = new ArrayList<>();
        if(it.reduceParent.isEmpty()){
            parents.add(it);
            return parents;
        }    
        for(LrItem p:it.reduceParent){
            parents.addAll(findParents(p));
        }
        return parents;
    }
    
    void moveReduction(ItemSet set, LrItem it, Queue<ItemSet> q){
        //System.out.println("trace "+it);
        for(Transition tr:set.transitions){
            if(tr.target == set) continue;
            //if(!tr.symbol.isName()) continue;
            //System.out.println("sym "+tr.symbol.debug());
            //copy for each symbol
            LrItem cur = new LrItem(it, it.dotPos);
            if(tr.symbol.astInfo.isFactor){
                ItemSet target = tr.target;
                LrItem sender = it.sender;
                System.out.printf("moved %d -> %d %s\n", set.stateId, target.stateId, it);
                LrItem it2= new LrItem(it, it.dotPos);
                it2.sender = sender;
                it.lookAhead.remove(tr.symbol.asName());
                it2.lookAhead.clear();
                for(Transition tr2:target.transitions){
                    //todo not all
                    Name sym = tr2.symbol.isSequence()?tr2.symbol.asSequence().last().asName():tr2.symbol.asName();
                    it2.lookAhead.add(sym);
                }
                target.addItem(it2);
                if(!q.contains(target)) q.add(target);
                System.out.printf("new = %s\n", it2);
            }else{
                Name sym = tr.symbol.isSequence()?tr.symbol.asSequence().last().asName():tr.symbol.asName();
                it.lookAhead.remove(sym);
                Name s = it.rule.ref.copy();
                s.name += "$";
                tr.symbol = seq(s, tr.symbol);
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
        while(all.size() > 2){
            for(ItemSet set : all){
                if(set.isStart) continue;
                if(canBeRemoved2(set)){
                    eliminate(set);
                    toRemove.add(set);
                }    
            }
            if(toRemove.isEmpty()) break;
            all.removeAll(toRemove);
            toRemove.clear();
        }
    }
    
    boolean has1OutGoing(ItemSet set){
        int cnt = 0;
        for(Transition tr:set.transitions){
            if(tr.target.stateId != set.stateId) cnt++;
        }
        return cnt == 1;
    }
    
    boolean canBeRemoved2(ItemSet set){
    	System.out.println("canBeRemoved2 " + set.stateId);
        if(!has1OutGoing(set)) return false;
        
        if(hasFinal(set)) return false;
        if(true) return true;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for(Transition tr : set.transitions){
            if(tr.target != set){
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while(!queue.isEmpty()){
            ItemSet cur = queue.poll();
            boolean r = reachFinal(cur, set);
            for(Transition tr : cur.transitions){
                if(tr.target == set && r) return false;
                if(tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }
    
    boolean reachFinal(ItemSet from, ItemSet except){
    	//System.out.printf("reachFinal %d -> %d\n", from.stateId, except.stateId);
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.stateId);
        while(!queue.isEmpty()){
            ItemSet cur = queue.poll();
            if(hasFinal(cur)) return true;
            for(Transition tr : cur.transitions){
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
        System.out.println("eliminate " + set.stateId);
        Node loop = null;
        Transition out = null;
        for(Transition tr : set.transitions){
             if(tr.target == set) loop = tr.symbol;
             else out = tr;
         }
        for(Transition in : set.incomings){
            in.target = out.target;
            out.target.incomings.remove(out);
            if(loop == null){
                in.symbol = new Sequence(wrapOr(in.symbol), wrapOr(out.symbol));
            }else{
                in.symbol = new Sequence(wrapOr(in.symbol), new Regex(new Group(wrapOr(loop)), "*"), wrapOr(out.symbol));
            }    
        }
        combine();
     }
     
     void replaceByRef(){
     }    
     
     Node wrapOr(Node sym){
         if(sym.isOr()) return new Group(sym);
         return sym;
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
         combine();
         System.out.printf("new final = %d\n", ns.stateId);
         all.add(ns);
     }
     
     void combine(){
         for(ItemSet set : all){
             //target -> ors
             Map<ItemSet, List<Node>> map = new HashMap<>();
             for(Transition tr : set.transitions){
                 List<Node> list = map.get(tr.target);
                 if(list == null){
                     list = new ArrayList<>();
                     map.put(tr.target, list);
                 }
                 list.add(tr.symbol);
                 tr.target.incomings.remove(tr);
             }
             set.transitions.clear();
             for(ItemSet trg : map.keySet()){
                 List<Node> list = map.get(trg);
                 Node sym = list.size() == 1 ? list.get(0) : new Or(list);
                 set.addTransition(sym, trg);
             }    
         }    
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
                StringBuilder sb2 = new StringBuilder();
                if(tr.symbol.astInfo.isFactor){
                    sb2.append("<<FONT color=\"green\">");
                    sb2.append(tr.symbol);
                    sb2.append("</FONT>>");
                }else{
                    sb2.append("\""+tr.symbol+"\"");
                }    
              w.printf("%s -> %s [label=%s]\n",set.stateId, tr.target.stateId, sb2.toString());
            }
        }
        w.println("\n}");
        w.close();
    }
}
