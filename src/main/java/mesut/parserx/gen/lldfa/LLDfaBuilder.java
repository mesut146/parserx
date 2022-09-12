package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.gen.transform.FactorLoop;
import mesut.parserx.gen.transform.GreedyNormalizer;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Debug;
import mesut.parserx.utils.Utils;

import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LLDfaBuilder {
    public Name rule;
    public Tree tree;
    public Tree ebnf;
    Map<String, ItemSet> firstSets = new HashMap<>();
    Set<ItemSet> all;
    Queue<ItemSet> queue = new LinkedList<>();
    String type = "lr1";
    Map<String, Set<ItemSet>> rules = new HashMap<>();
    public static Logger logger = Utils.getLogger();

    public LLDfaBuilder(Tree tree) {
        this.ebnf = tree;
        ItemSet.lastId = Item.lastId = 0;
    }

    void prepare() {
        new Normalizer(ebnf).normalize();
        ebnf.prepare();

        tree = new Tree(ebnf);
        tree.checkDup = false;
        for (var rd : ebnf.rules) {
            var rhs = rd.rhs;
            if (rhs.isOr()) {
                int id = 1;
                for (var ch : rhs.asOr()) {
                    ch = expandPlus(ch);
                    var decl = new RuleDecl(rd.ref, ch);
                    decl.retType = rd.retType;
                    decl.which = id++;
                    tree.addRule(decl);
                }
            }
            else {
                rhs = expandPlus(rhs);
                var decl = new RuleDecl(rd.ref, rhs);
                decl.retType = rd.retType;
                tree.addRule(decl);
            }
        }
    }

    //rewrite a+ as a a*
    public static Sequence expandPlus(Node node) {
        var info = node.astInfo;
        var seq = node.isSequence() ? node.asSequence() : new Sequence(node);
        var list = new ArrayList<Node>();
        for (var ch : seq) {
            if (ch.isPlus()) {
                var rn = ch.asRegex();
                var copy = rn.node.copy();
                list.add(copy);
                var star = new Regex(rn.node.copy(), RegexType.STAR);
                star.astInfo = rn.astInfo.copy();
                list.add(star);
            }
            else {
                list.add(ch);
            }
        }
        var ret = new Sequence(list);
        ret.assocLeft = seq.assocLeft;
        ret.assocRight = seq.assocRight;
        ret.astInfo = info;
        return ret;
    }

    public void factor() {
        prepare();
        for (var rd : ebnf.rules) {
            var visitor = new FactorVisitor();
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

                var a = seq.get(i).copy();
                var b = seq.get(i + 1).copy();
                var sym = GreedyNormalizer.hasGreedyTail(a, FirstSet.firstSet(b, tree), tree, new FactorLoop(tree, new Factor(tree)));
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

        var firstSet = new ItemSet(tree, type);
        firstSet.isStart = true;
        firstSets.put(rule.name, firstSet);
        Set<Name> la;
        if (rule.equals(tree.start)) {
            la = new HashSet<>();
            la.add(LrDFAGen.dollar);
        }
        else {
            la = LaFinder.computeLa(rule, tree);
            if (tree.start == null) {
                la.add(LrDFAGen.dollar);
            }
        }
        if (tree.start != null && !rule.equals(tree.start)) {
            //throw new RuntimeException("la need to be computed");
        }

        for (var rd : tree.getRules(rule)) {
            var first = new Item(rd, 0);
            first.first = true;
            first.gotoSet.add(firstSet);
            //first.lookAhead.add(dollar);
            first.lookAhead.addAll(la);
            firstSet.addItem(first);
        }
        for (var item : firstSet.all) {
            item.siblings.addAll(firstSet.all);
        }

        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            var curSet = queue.poll();
            //if (log) System.out.println("curSet = " + curSet.stateId);
            logger.log(Level.FINE, "curSet = " + curSet.stateId);
            //closure here because it needs all items
            curSet.closure();
            //curSet.addAll(curSet.genReduces());
            Map<Node, List<Item>> map = new HashMap<>();
            for (var item : curSet.all) {
                //System.out.println("item = " + item);
                //improve stars as non closured
                for (int i = item.dotPos; i < item.rhs.size(); i++) {
                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if (item.closured[i]) continue;

                    var node = item.getNode(i);
                    Node sym = ItemSet.sym(node);
                    int newPos = node.isStar() ? i : i + 1;
                    Item target;
                    if (node.isOptional() && sym.asName().isToken && !curSet.isFactor(item, i)) {
                        //.a? b c | b d -> a b c
                        var rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        rhs.set(0, sym);
                        sym = Sequence.make(rhs);
                        target = new Item(item, item.rhs.size());
                        logger.log(Level.FINE, "shrink opt=" + sym);
                    }
                    //if sym is not factor shrink transition
                    else if (canShrink(curSet, item, i) && item.rhs.size() - i > 1) {
                        var rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
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
        rules.put(rule.name, all);
    }

    boolean canShrink(ItemSet set, Item item, int i) {
        var node = item.getNode(i);
        var sym = node.isName() ? node.asName() : node.asRegex().node.asName();

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
        var list = map.get(sym);
        if (list == null) {
            list = new ArrayList<>();
            map.put(sym, list);
        }
        else {
            //factor
            var f = sym.copy();
            f.astInfo.isFactor = true;
            map.remove(sym);
            map.put(f, list);
            logger.log(Level.FINE, "factor " + f);
        }
        list.add(target);
    }

    void makeTrans(ItemSet curSet, Map<Node, List<Item>> map) {
        logger.log(Level.FINE, "makeTrans " + curSet.stateId + " " + map);
        for (var e : map.entrySet()) {
            var sym = e.getKey();
            var list = e.getValue();
            var targetSet = new ItemSet(tree, type);
            targetSet.addAll(list);
            targetSet.addAll(targetSet.genReduces());
            targetSet.alreadyGenReduces = true;

            var sim = findSimilar(new ArrayList<>(targetSet.kernel));
            if (sim != null) {
                ItemSet.lastId--;
                //merge lookaheads
                for (var it : targetSet.all) {
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
            targetSet.symbol = sym;
            curSet.addTransition(sym, targetSet);
            logger.log(Level.FINE, String.format("trans %d -> %d with %s", curSet.stateId, targetSet.stateId, sym));
        }
    }

    void sort(List<Item> list) {
        list.sort((i1, i2) -> {
            if (i1.rule.index == i2.rule.index) {
                return Integer.compare(i1.dotPos, i2.dotPos);
            }
            return Integer.compare(i1.rule.index, i2.rule.index);
        });
    }

    //find a set that has same kernel
    ItemSet findSimilar(List<Item> target) {
        //System.out.println("similar " + target);
        sort(target);
        for (var set : all) {
            if (target.size() != set.kernel.size()) continue;
            var l2 = new ArrayList<>(set.kernel);
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

    public void dump2(PrintWriter w) {
        for (var e : rules.entrySet()) {
            w.println("//" + e.getKey());
            for (var set : e.getValue()) {
                w.printf("S%d: ", set.stateId);
                int i = 0;
                for (var tr : set.transitions) {
                    if (i>0){
                        w.print(" | ");
                    }
                    w.printf("%s S%d", tr.symbol.toString(), tr.target.stateId);
                    i++;
                }
                w.println(";");
            }
            w.println();
        }
        w.flush();
        w.close();
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
        w.println("rankdir = TD");
        //w.println("size=\"6,5\";");
        w.println("ratio=\"fill\";");
        for (var all : rules.values()) {
            for (var set : all) {
                var sb = new StringBuilder();
                //items
                sb.append("<");
                for (var it : set.all) {
                    var line = it.toString2(tree);
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
                w.printf("%s [shape=box xlabel=\"%s\" label=%s];\n", set.stateId, set.stateId, sb);
                if (set.hasFinal()) {
                    w.printf("%d [style=filled fillcolor=gray];\n", set.stateId);
                }
                for (var tr : set.transitions) {
                    var labelBuf = new StringBuilder();
                    if (tr.symbol.astInfo.isFactor) {
                        labelBuf.append("<<FONT color=\"green\">");
                        labelBuf.append(tr.symbol);
                        labelBuf.append("</FONT>>");
                    }
                    else {
                        labelBuf.append("\"").append(tr.symbol).append("\"");
                    }
                    w.printf("%s -> %s [label=%s];\n", set.stateId, tr.target.stateId, labelBuf);
                }
            }
        }

        w.println("\n}");
        w.close();
    }
}
