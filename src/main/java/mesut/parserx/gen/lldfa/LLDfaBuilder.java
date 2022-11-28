package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ParserUtils;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.gen.lr.TreeInfo;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.gen.transform.GreedyNormalizer;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Debug;
import mesut.parserx.utils.Utils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LLDfaBuilder {
    public Tree tree;
    TreeInfo treeInfo;
    Map<Name, ItemSet> firstSets = new HashMap<>();
    Map<Name, Set<ItemSet>> rules = new HashMap<>();
    Set<ItemSet> all;
    Queue<ItemSet> queue = new LinkedList<>();
    LrType type = LrType.LR1;
    public static Logger logger = Utils.getLogger();
    public static boolean noshrink = false;
    public static boolean skip = false;

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        ItemSet.lastId = Item.lastId = 0;
    }

    void prepare() {
        //expand plus
        new Transformer(tree) {
            @Override
            public Node visitSequence(Sequence seq, Void arg) {
                var list = new ArrayList<Node>();
                for (int i = 0; i < seq.size(); i++) {
                    var ch = seq.get(i);
                    if (ch.isPlus()) {
                        //a+: a a*
                        var regex = ch.asRegex();
                        var node = regex.node.copy();
                        node.astInfo.isInLoop = true;
                        list.add(node);
                        var star = new Regex(regex.node.copy(), RegexType.STAR);
                        star.astInfo = regex.astInfo.copy();
                        list.add(star);
                    }
                    else {
                        list.add(ch);
                    }
                }
                var res = new Sequence(list);
                res.astInfo = seq.astInfo.copy();
                return res;
            }
        }.transformRules();
        treeInfo = TreeInfo.make(tree);
    }

    public void factor() {
        prepare();
        for (var rd : tree.rules) {
            var visitor = new FactorVisitor();
            visitor.tree = tree;
            if (rd.rhs.accept(visitor, null)) {
                build(rd);
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
                    if (FactorHelper.hasCommon(or.get(i), or.get(j), tree)) {
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
                var sym = GreedyNormalizer.hasGreedyTail(a, FirstSet.firstSet(b, tree), tree);
                if (sym != null) {
                    throw new RuntimeException(seq + " has greediness->" + sym.sym);
                }
            }
            return false;
        }
    }

    public void build(RuleDecl rd) {
        var rule = rd.ref;
        queue.clear();
        all = new TreeSet<>(Comparator.comparingInt(set -> set.stateId));

        logger.log(Level.FINE, "building " + rule + " in " + tree.file.getName());

        var firstSet = new ItemSet(treeInfo, type);
        firstSet.isStart = true;
        firstSets.put(rule, firstSet);
        Set<Name> la;
        if (rule.equals(tree.start)) {
            la = new HashSet<>();
            la.add(ParserUtils.dollar);
        }
        else {
            la = LaFinder.computeLa(rule, tree);
//            if (tree.start == null) {
//                la.add(ParserUtils.dollar);
//            }
            la.add(ParserUtils.dollar);
        }
        for (var firstDecl : treeInfo.ruleMap.get(rule)) {
            var firstItem = new Item(firstDecl, 0);
            firstItem.first = true;
            firstItem.gotoSet.add(firstSet);
            firstItem.lookAhead.addAll(la);
            firstSet.addItem(firstItem);
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
            Map<Node, LLTransition> map = new HashMap<>();
            for (var item : curSet.all) {
                //improve stars as non closured
                for (int i = item.dotPos; i < item.rhs.size(); i++) {
                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if (item.closured[i]) continue;

                    var node = item.getNode(i);
                    if (node instanceof Factored) continue;
                    var sym = (Node) ItemSet.sym(node.copy());
                    int newPos = node.isStar() ? i : i + 1;
                    Item target;
                    if (curSet.isFactor(item, i)) {
                        sym.astInfo.isFactor = true;
                    }
                    if (skip && !sym.astInfo.isFactor) {
                        //direct transition to alt final state
                        target = new Item(item, item.rhs.size());
                        if (i < item.rhs.size() - 1) {
                            target.lookAhead = item.follow(tree, i);
                        }
                    }
                    else if (noshrink) {
                        target = new Item(item, newPos);
                    }
                    else if (node.isOptional() && sym.asName().isToken && !curSet.isFactor(item, i)) {
                        //.a? b c | b d -> a b c
                        var rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        rhs.set(0, sym);
                        sym = Sequence.make(rhs);
                        target = new Item(item, item.rhs.size());
                    }
                    else if (canShrink(curSet, item, i) && item.rhs.size() - i > 1) {
                        //if sym is not factor shrink transition
                        var rhs = new ArrayList<>(item.rhs.list.subList(i, item.rhs.size()));
                        sym = Sequence.make(rhs);
                        target = new Item(item, item.rhs.size());
                        //todo below line and break not necessary?
                        //addMap(map, sym, new LLTransition.ItemPair(item, target), curSet);
                        //break;
                    }
                    else {
                        target = new Item(item, newPos);
                    }
                    addMap(map, sym, new LLTransition.ItemPair(item, target), curSet);
                }
            }
            makeTrans(curSet, map);
        }
        rules.put(rule, all);
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

    void addMap(Map<Node, LLTransition> map, Node sym, LLTransition.ItemPair pair, ItemSet from) {
        var tr = map.get(sym);
        if (tr == null) {
            tr = new LLTransition(from, null, sym);
            map.put(sym, tr);
        }
        tr.pairs.add(pair);
    }

    void makeTrans(ItemSet curSet, Map<Node, LLTransition> map) {
        for (var e : map.entrySet()) {
            var sym = e.getKey();
            var tr = e.getValue();
            var targetSet = new ItemSet(treeInfo, type);
            targetSet.addAll(tr.pairs.stream().map(pair -> pair.target).collect(Collectors.toList()));
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
            tr.target = targetSet;
            targetSet.symbol = sym;
            curSet.addTransition(tr);
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

    public void dump(OutputStream os) {
        PrintWriter w = new PrintWriter(os);
        for (var e : rules.entrySet()) {
            w.println("//" + e.getKey());
            for (var set : e.getValue()) {
                if (set.transitions.isEmpty()) continue;
                w.printf("S%d: ", set.stateId);
                int i = 0;
                for (var tr : set.transitions) {
                    if (i > 0) {
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
    }

    public void dumpItems(OutputStream os) {
        PrintWriter w = new PrintWriter(os);
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
    }

    public void dot(PrintWriter w) {
        w.println("digraph G{");
        w.println("rankdir = TD");
        w.println("ratio=\"fill\";");
        for (var all : rules.values()) {
            for (var set : all) {
                var sb = new StringBuilder();
                var id = String.valueOf(set.stateId);
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
                w.printf("%s [shape=box xlabel=\"%s\" label=%s];\n", id, id, sb);
                if (set.hasFinal()) {
                    w.printf("%s [style=filled fillcolor=gray];\n", id);
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
                    w.printf("%s -> %s [label=%s];\n", id, tr.target.stateId, labelBuf);
                }
            }
        }
        w.println("\n}");
        w.flush();
    }
}
