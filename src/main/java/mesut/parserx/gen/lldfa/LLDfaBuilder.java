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
import mesut.parserx.utils.Debug;

import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;

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
        initLog();
    }

    private void initLog() {
        logger.setLevel(Level.ALL);
        var handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return formatMessage(record) + "\n";
                //return record.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);
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
                    ch = expandPlus(ch);
                    RuleDecl decl = new RuleDecl(rd.ref, ch);
                    decl.retType = rd.retType;
                    decl.which = id++;
                    tree.addRule(decl);
                }
            }
            else {
                rhs = expandPlus(rhs);
                RuleDecl decl = new RuleDecl(rd.ref, rhs);
                decl.retType = rd.retType;
                tree.addRule(decl);
            }
        }
    }

    //rewrite a+ as a a*
    Sequence expandPlus(Node node) {
        AstInfo info = node.astInfo;
        Sequence seq = node.isSequence() ? node.asSequence() : new Sequence(node);
        List<Node> list = new ArrayList<>();
        for (Node ch : seq) {
            if (ch.isPlus()) {
                Regex rn = ch.asRegex();
                Node copy = rn.node.copy();
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
        if (tree.options.dump) {
            Debug.dot(tree, this);
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

    public void build() {
        queue.clear();
        all = new TreeSet<>(Comparator.comparingInt(set -> set.stateId));
        //ItemSet.lastId = 0;
        //Item.lastId = 0;

        logger.log(Level.FINE, "building " + rule + " in " + tree.file.getName());

        firstSet = new ItemSet(tree, type);
        firstSet.isStart = true;
        Set<Name> la;
        if (rule.equals(tree.start)) {
            la = new HashSet<>();
            la.add(dollar);
        }
        else {
            la = LaFinder.computeLa(rule, tree);
            if (tree.start == null) {
                la.add(dollar);
            }
        }
        if (tree.start != null && !rule.equals(tree.start)) {
            //throw new RuntimeException("la need to be computed");
        }

        for (RuleDecl rd : tree.getRules(rule)) {
            Item first = new Item(rd, 0);
            first.gotoSet.add(firstSet);
            //first.lookAhead.add(dollar);
            first.lookAhead.addAll(la);
            firstSet.addItem(first);
            List<Item> list = firstItems.computeIfAbsent(rd.ref, k -> new ArrayList<>());
            list.add(first);
        }
        for (Item item : firstSet.all) {
            item.siblings.addAll(firstSet.all);
        }

        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            ItemSet curSet = queue.poll();
            //if (log) System.out.println("curSet = " + curSet.stateId);
            logger.log(Level.FINE, "curSet = " + curSet.stateId);
            //closure here because it needs all items
            curSet.closure();
            //curSet.addAll(curSet.genReduces());
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
                        //target.gotoSet.add(curSet);
                        addMap(map, sym, target);
                        break;
                    }
                    else {
                        target = new Item(item, newPos);
                    }
                    //target.gotoSet.add(curSet);
                    addMap(map, sym, target);
                }
            }
            makeTrans(curSet, map);
        }
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
            targetSet.alreadyGenReduces = true;

            ItemSet sim = findSimilar(new ArrayList<>(targetSet.kernel));
            if (sim != null) {
                ItemSet.lastId--;
                //merge lookaheads
                for (Item it : targetSet.all) {
                    sim.update(it, true, true);
                }
                targetSet = sim;
            }
            else {
                all.add(targetSet);
                queue.add(targetSet);
            }
            /*for (Item item : list) {
                item.itemSet = targetSet;
            }*/
            if (targetSet.symbol != null && !targetSet.symbol.equals(sym)) {
                //throw new RuntimeException("invalid state: multi symbol");
            }
            targetSet.symbol = sym;
            curSet.addTransition(sym, targetSet);
            logger.log(Level.FINE, String.format("trans %d -> %d with %s", curSet.stateId, targetSet.stateId, sym));
        }
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

    public void findInlined() {
        for (ItemSet set : all) {
            if (canBeInlined(set)) {
                inlined.add(set);
            }
        }
    }

    public boolean canBeInlined(ItemSet set) {
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


    public static int countOutgoings(ItemSet set) {
        int cnt = 0;
        for (Transition tr : set.transitions) {
            if (tr.target.stateId != set.stateId) cnt++;
        }
        return cnt;
    }

    public boolean reachFinal(ItemSet from, ItemSet except) {
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

    public boolean hasFinal(ItemSet set) {
        for (Item it : set.all) {
            if (it.isReduce(tree) && it.lookAhead.contains(dollar)) return true;
        }
        return false;
    }

    public void dump(PrintWriter w) {
        for (var e : this.rules.entrySet()) {
            w.println("----------------------------------");
            w.println("//" + e.getKey());
            for (var set : e.getValue()) {
                w.println("----------------------------------");
                w.printf("S%d\n", set.stateId);
                for (var item : set.all) {
                    w.printf("%s\n", item);
                }
                w.println();
                for (var tr : set.transitions) {
                    w.printf("%s -> %s\n", tr.symbol, tr.target.stateId);
                }
            }
        }
        w.flush();
        w.close();
    }

    public void dot(PrintWriter w) {
        w.println("digraph G{");
        //w.println("rankdir = TD");
        w.println("size=\"100,100\";");
        for (var all : rules.values()) {
            for (ItemSet set : all) {
                StringBuilder sb = new StringBuilder();
                sb.append("<");
                for (Item it : set.all) {
                    String line = it.toString();
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
        }

        w.println("\n}");
        w.close();
    }
}
