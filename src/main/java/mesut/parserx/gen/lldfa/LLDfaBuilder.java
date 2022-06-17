package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.gen.transform.FactorLoop;
import mesut.parserx.gen.transform.GreedyNormalizer;
import mesut.parserx.nodes.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LLDfaBuilder {
    public Name rule;
    public Tree tree;
    public Tree ebnf;
    ItemSet firstSet;
    Map<Name, List<Item>> firstItems = new HashMap<>();
    Set<ItemSet> all;
    Set<ItemSet> all2 = new HashSet<>();
    Queue<ItemSet> queue = new LinkedList<>();
    String type = "lr1";
    public static Name dollar = new Name("$", true);//eof
    Map<String, Set<ItemSet>> rules = new HashMap<>();
    Set<ItemSet> inlined = new HashSet<>();
    public static Logger logger = Logger.getLogger(LLDfaBuilder.class.getName());


    public LLDfaBuilder(Tree tree) {
        this.ebnf = tree;
        ItemSet.lastId = Item.lastId = 0;
        logger.setLevel(Level.ALL);
    }

    void prepare() {
        new Normalizer(ebnf).normalize();
        ebnf.prepare();

        tree = new Tree(ebnf);
        tree.checkDup = false;
        for (RuleDecl rd : ebnf.rules) {
            Node rhs = rd.rhs;
            if (rhs.isOr()) {
                int id = 1;
                for (Node ch : rhs.asOr()) {
                    ch = plus(ch);
                    RuleDecl decl = new RuleDecl(rd.ref, ch);
                    decl.retType = rd.retType;
                    decl.which = id++;
                    tree.addRule(decl);
                }
            }
            else {
                rhs = plus(rhs);
                RuleDecl decl = new RuleDecl(rd.ref, rhs);
                decl.retType = rd.retType;
                tree.addRule(decl);
            }
        }
    }

    Sequence plus(Node node) {
        AstInfo info = node.astInfo;
        Sequence seq = node.isSequence() ? node.asSequence() : new Sequence(node);
        List<Node> list = new ArrayList<>();
        for (Node ch : seq) {
            if (ch.isPlus()) {
                Regex rn = ch.asRegex();
                Node copy = rn.node.copy();
                //copy.astInfo.isInLoop = true;
                list.add(copy);
                Regex star = new Regex(rn.node.copy(), RegexType.STAR);
                star.astInfo = rn.astInfo.copy();
                list.add(star);
            }
            else {
                list.add(ch);
            }
        }
        Sequence ret = new Sequence(list);
        ret.astInfo = info;
        return ret;
    }

    public void factor() {
        prepare();
        for (RuleDecl rd : ebnf.rules) {
            FactorVisitor visitor = new FactorVisitor();
            visitor.tree = ebnf;
            if (rd.rhs.accept(visitor, null)) {
                rule = rd.ref;
                build();
            }
        }
    }

    static class FactorVisitor extends BaseVisitor<Boolean, Void> {
        Tree tree;

        @Override
        public Boolean visitOr(Or or, Void arg) {
            for (int i = 0; i < or.size(); i++) {
                //try seq
                or.get(i).accept(this, null);
                for (int j = i + 1; j < or.size(); j++) {
                    if (hasCommon(or.get(i), or.get(j), tree)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Boolean visitSequence(Sequence seq, Void arg) {
            for (int i = 0; i < seq.size(); i++) {
                if (i == seq.size() - 1) break;

                Node a = seq.get(i).copy();
                Node b = seq.get(i + 1).copy();
                if (a.astInfo.isFactored) continue;
                GreedyNormalizer.TailInfo sym = GreedyNormalizer.hasGreedyTail(a, FirstSet.firstSet(b, tree), tree, new FactorLoop(tree, new Factor(tree)));
                if (sym != null) {
                    throw new RuntimeException(seq + " has greediness");
                }
            }
            return false;
        }
    }

    static boolean hasCommon(Node a, Node b, Tree tree) {
        return new FactorHelper(tree, new Factor(tree)).common(a, b) != null;
    }

    Set<Name> computeLa(Name ref) {
        Set<Name> set = new HashSet<>();
        BaseVisitor<Void, Void> visitor = new BaseVisitor<>() {
            @Override
            public Void visitSequence(Sequence seq, Void arg) {
                for (int i = 0; i < seq.size(); i++) {
                    Node ch = seq.get(i);
                    if (i == seq.size() - 1) {
                        set.addAll(computeLa(curRule.ref));
                    }
                    if (ch.equals(ref) && i < seq.size() - 1) {
                        Sequence rest = new Sequence(seq.list.subList(i + 1, seq.size()));
                        set.addAll(FirstSet.tokens(rest, tree));
                    }
                    else if (ch.isRegex() && ch.asRegex().node.equals(ref)) {
                        Regex regex = ch.asRegex();
                        if (regex.isOptional() && i < seq.size() - 1) {
                            Sequence rest = new Sequence(seq.list.subList(i + 1, seq.size()));
                            set.addAll(FirstSet.tokens(rest, tree));
                        }
                        else if (regex.isStar()) {

                        }
                        else if (regex.isPlus()) {

                        }
                    }
                }
                return null;
            }
        };
        for (RuleDecl decl : tree.rules) {
            if (decl.ref.equals(ref)) continue;
            visitor.curRule = decl;
            decl.rhs.accept(visitor, null);
        }
        return set;
    }

    public void build() {
        queue.clear();
        all = new TreeSet<>(Comparator.comparingInt(set -> set.stateId));
        //ItemSet.lastId = 0;
        //Item.lastId = 0;

        logger.log(Level.FINE, "building " + rule + " in " + tree.file.getName());

        firstSet = new ItemSet(tree, type);
        firstSet.isStart = true;

        for (RuleDecl rd : tree.getRules(rule)) {
            Item first = new Item(rd, 0);
            first.lookAhead.add(dollar);
            firstSet.addItem(first);
            List<Item> list = firstItems.computeIfAbsent(rd.ref, k -> new ArrayList<>());
            list.add(first);
        }
        for (Item item : firstSet.all) {
            item.siblings.addAll(firstSet.all);
            item.siblings.remove(item);
        }

        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            ItemSet curSet = queue.poll();
            //if (log) System.out.println("curSet = " + curSet.stateId);
            logger.log(Level.FINE, "curSet = " + curSet.stateId);
            //closure here because it needs all items
            curSet.closure();
            Map<Node, List<Item>> map = new HashMap<>();
            for (Item item : curSet.all) {
                //System.out.println("item = " + item);
                //improve stars as non closured
                for (int i = item.dotPos; i < item.rhs.size(); i++) {
                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if (item.closured[i]) continue;

                    Node node = item.getNode(i);
                    Node sym = node.isName() ? node.asName() : node.asRegex().node.asName();
                    int newPos = node.isStar() ? i : i + 1;
                    Item target;
                    if (node.isOptional() && sym.asName().isToken && !curSet.isFactor(item, i)) {
                        //.a? b c | b d -> a b c
                        List<Node> rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        rhs.set(0, sym);
                        sym = Sequence.make(rhs);
                        target = new Item(item, item.rhs.size());
                        logger.log(Level.FINE, "shrink opt=" + sym);
                    }
                    //if sym is not factor shrink transition
                    else if (canShrink(curSet, item, i) && item.rhs.size() - i > 1) {
                        List<Node> rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        sym = Sequence.make(rhs);
                        target = new Item(item, item.rhs.size());
                        logger.log(Level.FINE, "shrink=" + sym);
                        target.gotoSet2.add(curSet);
                        addMap(map, sym, target);
                        break;
                    }
                    else {
                        target = new Item(item, newPos);
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
        findInlined();
        all2.addAll(all);
        rules.put(rule.name, all);
    }


    boolean canShrink(ItemSet set, Item item, int i) {
        Node node = item.getNode(i);
        Name sym = node.isName() ? node.asName() : node.asRegex().node.asName();

        if (set.isFactor(item, i)) return false;
        if (node.isName() && sym.isToken && !set.isFactor(item, i)) return true;
        if (!Helper.canBeEmpty(node, tree)) return true;//not closured
        return !isFollowHasFactor(set, item, i);
    }

    boolean isFollowHasFactor(ItemSet set, Item item, int pos) {
        for (int i = pos + 1; i < item.rhs.size(); i++) {
            //if(i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
            if (set.isFactor(item, i)) return true;
            if (!FirstSet.canBeEmpty(item.getNode(i), tree)) break;
        }
        return false;
    }

    void addMap(Map<Node, List<Item>> map, Node sym, Item target) {
        List<Item> list = map.get(sym);
        if (list == null) {
            list = new ArrayList<>();
            map.put(sym, list);
        }
        else {
            //factor
            Node f = sym.copy();
            f.astInfo.isFactor = true;
            map.remove(sym);
            map.put(f, list);
            logger.log(Level.FINE, "factor " + f);
        }
        list.add(target);
    }

    void makeTrans(ItemSet curSet, Map<Node, List<Item>> map) {
        logger.log(Level.FINE, "makeTrans " + curSet.stateId + " " + map);
        for (Map.Entry<Node, List<Item>> e : map.entrySet()) {
            Node sym = e.getKey();
            List<Item> list = e.getValue();
            ItemSet targetSet = new ItemSet(tree, type);
            targetSet.addAll(list);
            targetSet.addAll(targetSet.genReduces());

            ItemSet sim = findSimilar(new ArrayList<>(targetSet.kernel));
            if (sim != null) {
                ItemSet.lastId--;
                //merge lookaheads
                for (Item it : targetSet.all) {
                    sim.update(it, true);
                }
                targetSet = sim;
            }
            else {
                all.add(targetSet);
                queue.add(targetSet);
            }
            if (targetSet.symbol != null && !targetSet.symbol.equals(sym)) {
                //throw new RuntimeException("invalid state: multi symbol");
            }
            targetSet.symbol = sym;
            curSet.addTransition(sym, targetSet);
            logger.log(Level.FINE, String.format("trans %d -> %d with %s\n", curSet.stateId, targetSet.stateId, sym));
        }
    }

    ItemSet getTarget(ItemSet set, Name sym) {
        for (Transition tr : set.transitions) {
            if (tr.symbol.equals(sym)) return tr.target;
        }
        return null;
    }

    void sort(List<Item> list) {
        list.sort(Comparator.comparingInt(item -> item.rule.index));
    }

    //find a set that has same kernel
    ItemSet findSimilar(List<Item> target) {
        //System.out.println("similar " + target);
        sort(target);
        for (ItemSet set : all) {
            if (target.size() != set.kernel.size()) continue;
            List<Item> l2 = new ArrayList<>(set.kernel);
            sort(l2);
            boolean same = true;
            for (int i = 0; i < target.size(); i++) {
                if (!target.get(i).isSame(l2.get(i))) {
                    same = false;
                    break;
                }
            }
            if (same) return set;
        }
        return null;
    }

    public void moveReductions() {
        Queue<ItemSet> q = new LinkedList<>(all);
        while (!q.isEmpty()) {
            Set<Item> toRemove = new HashSet<>();
            ItemSet set = q.poll();
            //clear sub reductions, they only needed for reducers
            for (Iterator<Item> it = set.all.iterator(); it.hasNext(); ) {
                Item item = it.next();
                if (!item.isReduce(tree)) continue;
                if (item.lookAhead.contains(dollar)) continue;
                if (!item.reduceParent.isEmpty()) {
                    it.remove();
                    System.out.println("rem sub = " + item);
                }
            }
            //move parent reductions
            for (Item it : set.all) {
                if (!it.isReduce(tree)) continue;
                if (it.lookAhead.contains(dollar)) continue;
                moveReduction(set, it, q);
                if (it.lookAhead.isEmpty()) toRemove.add(it);
            }
            for (Item it : toRemove) {
                for (int i = 0; i < set.all.size(); i++) {
                    if (it.isSame(set.all.get(i))) {
                        set.all.remove(i);
                        set.kernel.remove(it);
                        System.out.println("deleted " + set.stateId + " " + it);
                        break;
                    }
                }
            }

        }
    }

    List<Item> findParents(Item it) {
        List<Item> parents = new ArrayList<>();
        if (it.reduceParent.isEmpty()) {
            parents.add(it);
            return parents;
        }
        for (Item p : it.reduceParent) {
            parents.addAll(findParents(p));
        }
        return parents;
    }

    void moveReduction(ItemSet set, Item it, Queue<ItemSet> q) {
        //System.out.println("trace "+it);
        for (Transition tr : set.transitions) {
            if (tr.target == set) continue;
            //if(!tr.symbol.isName()) continue;
            //System.out.println("sym "+tr.symbol.debug());
            //copy for each symbol
            Item cur = new Item(it, it.dotPos);
            if (tr.symbol.astInfo.isFactor) {
                ItemSet target = tr.target;
                System.out.printf("moved %d -> %d %s\n", set.stateId, target.stateId, it);
                Item it2 = new Item(it, it.dotPos);
                it2.senders.addAll(it.senders);
                it.lookAhead.remove(tr.symbol.asName());
                it2.lookAhead.clear();
                for (Transition tr2 : target.transitions) {
                    //todo not all
                    Name sym = tr2.symbol.isSequence() ? tr2.symbol.asSequence().last().asName() : tr2.symbol.asName();
                    it2.lookAhead.add(sym);
                }
                target.addItem(it2);
                if (!q.contains(target)) q.add(target);
                System.out.printf("new = %s\n", it2);
            }
            else {
                Name sym = tr.symbol.isSequence() ? tr.symbol.asSequence().last().asName() : tr.symbol.asName();
                it.lookAhead.remove(sym);
                Name s = it.rule.ref.copy();
                s.name += "$";
                tr.symbol = seq(s, tr.symbol);
            }
        }
    }

    Sequence seq(Node a, Node b) {
        if (b.isSequence()) {
            Sequence s = b.asSequence();
            s.list.add(0, a);
            return s;
        }
        else {
            return new Sequence(a, b);
        }
    }

    private void findInlined() {
        for (ItemSet set : all) {
            if (canBeInlined(set)) {
                inlined.add(set);
            }
        }
    }

    boolean canBeInlined(ItemSet set) {
        if (countOutgoings(set) == 1) return true;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for (Transition tr : set.transitions) {
            if (tr.target != set) {
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            boolean r = reachFinal(cur, set);
            for (Transition tr : cur.transitions) {
                if (tr.target == set && r) return false;
                if (tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }

    public void eliminate() {
        List<ItemSet> toRemove = new ArrayList<>();
        while (all.size() > 2) {
            for (ItemSet set : all) {
                if (set.isStart) continue;
                if (canBeRemoved2(set)) {
                    eliminate(set);
                    toRemove.add(set);
                }
            }
            if (toRemove.isEmpty()) break;
            toRemove.forEach(all::remove);
            toRemove.clear();
        }
    }

    int countOutgoings(ItemSet set) {
        int cnt = 0;
        for (Transition tr : set.transitions) {
            if (tr.target.stateId != set.stateId) cnt++;
        }
        return cnt;
    }

    boolean canBeRemoved2(ItemSet set) {
        System.out.println("canBeRemoved2 " + set.stateId);
        if (countOutgoings(set) != 1) return false;

        if (hasFinal(set)) return false;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        for (Transition tr : set.transitions) {
            if (tr.target != set) {
                queue.add(tr.target);
                visited.add(tr.target.stateId);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            boolean r = reachFinal(cur, set);
            for (Transition tr : cur.transitions) {
                if (tr.target == set && r) return false;
                if (tr.target != set && visited.add(tr.target.stateId)) queue.add(tr.target);
            }
        }
        return true;
    }

    boolean reachFinal(ItemSet from, ItemSet except) {
        //System.out.printf("reachFinal %d -> %d\n", from.stateId, except.stateId);
        Set<Integer> visited = new HashSet<>();
        Queue<ItemSet> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.stateId);
        while (!queue.isEmpty()) {
            ItemSet cur = queue.poll();
            if (hasFinal(cur)) return true;
            for (Transition tr : cur.transitions) {
                ItemSet trg = tr.target;
                if (trg == except) continue;
                if (visited.add(trg.stateId)) queue.add(trg);
            }
        }
        return false;
    }

    boolean hasFinal(ItemSet set) {
        for (Item it : set.all) {
            if (it.isReduce(tree) && it.lookAhead.contains(dollar)) return true;
        }
        return false;
    }

    void eliminate(ItemSet set) {
        System.out.println("eliminate " + set.stateId);
        Node loop = null;
        Transition out = null;
        for (Transition tr : set.transitions) {
            if (tr.target == set) loop = tr.symbol;
            else out = tr;
        }
        for (Transition in : set.incomings) {
            in.target = out.target;
            out.target.incomings.remove(out);
            if (loop == null) {
                in.symbol = new Sequence(wrapOr(in.symbol), wrapOr(out.symbol));
            }
            else {
                in.symbol = new Sequence(wrapOr(in.symbol), new Regex(new Group(wrapOr(loop)), RegexType.STAR), wrapOr(out.symbol));
            }
        }
        combine();
    }


    Node wrapOr(Node sym) {
        if (sym.isOr()) return new Group(sym);
        return sym;
    }

    Node loopSym(ItemSet set) {
        for (Transition tr : set.transitions) {
            if (tr.target == set) return tr.symbol;
        }
        return null;
    }

    void mergeFinals() {
        ItemSet ns = new ItemSet(tree, type);
        for (Iterator<ItemSet> it = all.iterator(); it.hasNext(); ) {
            ItemSet set = it.next();
            for (Item item : set.all) {
                if (item.lookAhead.contains(dollar) && item.isReduce(tree)) {
                    ns.addAll(set.all);
                    for (Transition in : set.incomings) {
                        in.target = ns;
                        System.out.printf("new1 %d -> %d\n", in.from.stateId, ns.stateId);
                    }
                    for (Transition tr : set.transitions) {
                        tr.from = ns;
                        ns.addTransition(tr.symbol, tr.target);
                        System.out.printf("new2 %d -> %d\n", ns.stateId, tr.target.stateId);
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

    void combine() {
        for (ItemSet set : all) {
            //target -> ors
            Map<ItemSet, List<Node>> map = new HashMap<>();
            for (Transition tr : set.transitions) {
                List<Node> list = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                list.add(tr.symbol);
                tr.target.incomings.remove(tr);
            }
            set.transitions.clear();
            for (ItemSet trg : map.keySet()) {
                List<Node> list = map.get(trg);
                Node sym = list.size() == 1 ? list.get(0) : new Or(list);
                set.addTransition(sym, trg);
            }
        }
    }

    public void dot(java.io.PrintWriter w) {
        w.println("digraph G{");
        //w.println("rankdir = TD");
        w.println("size=\"100,100\";");
        for (ItemSet set : all2) {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for (Item it : set.all) {
                String line = it.toString() + " " + it.ids;
                line = line.replace(">", "&gt;");
                if (it.isReduce(tree)) {
                    sb.append("<FONT color=\"blue\">");
                    sb.append(line);
                    sb.append("</FONT>");
                }
                else if (it.dotPos == 0) {
                    sb.append("<FONT color=\"red\">");
                    sb.append(line);
                    sb.append("</FONT>");
                }
                else {
                    sb.append(line);
                }
                sb.append("<BR ALIGN=\"LEFT\"/>");
            }
            sb.append(">");
            w.printf("%s [shape=box xlabel=\"%s\" label=%s]\n", set.stateId, set.stateId, sb);
            for (Transition tr : set.transitions) {
                StringBuilder sb2 = new StringBuilder();
                if (tr.symbol.astInfo.isFactor) {
                    sb2.append("<<FONT color=\"green\">");
                    sb2.append(tr.symbol);
                    sb2.append("</FONT>>");
                }
                else {
                    sb2.append("\"").append(tr.symbol).append("\"");
                }
                w.printf("%s -> %s [label=%s]\n", set.stateId, tr.target.stateId, sb2);
            }
        }
        w.println("\n}");
        w.close();
    }
}
