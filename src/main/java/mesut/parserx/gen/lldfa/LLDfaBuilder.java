package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.ParserUtils;
import mesut.parserx.gen.lr.TreeInfo;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Debug;
import mesut.parserx.utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static mesut.parserx.gen.ParserUtils.dollar;

public class LLDfaBuilder {
    public Tree tree;
    TreeInfo treeInfo;
    Map<Name, ItemSet> firstSets = new HashMap<>();
    Map<Name, Set<ItemSet>> rules = new LinkedHashMap<>();
    Set<ItemSet> all;
    Queue<ItemSet> queue = new LinkedList<>();

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        ItemSet.lastId = Item.lastId = 0;
    }

    static boolean isSame(ItemSet set, ItemSet set2) {
        //if (isFinal(set) || isFinal(set2)) return false;//why not finals?
        if (set.transitions.size() != set2.transitions.size()) return false;
        var map = new HashMap<Node, ItemSet>();
        for (var tr : set.transitions) {
            map.put(tr.symbol, tr.target);
        }
        for (var tr : set2.transitions) {
            if (!map.containsKey(tr.symbol)) return false;
            if (!map.get(tr.symbol).equals(tr.target)) return false;
        }
        return true;
    }

    public static void dot(PrintWriter w, Set<ItemSet> all) {
        Item.printLa = false;
        w.println("digraph G{");
        w.println("rankdir = TD");
        w.println("ratio=\"fill\";");
        for (var set : all) {
            var sb = new StringBuilder();
            var id = String.valueOf(set.stateId);
            //items
            sb.append("<");
//                for (var it : set.all) {
//                    var line = it.toString2(tree);
//                    line = line.replace(">", "&gt;");
//                    if (it.isReduce(tree)) {
//                        sb.append("<FONT color=\"blue\">");
//                        sb.append(line);
//                        sb.append("</FONT>");
//                    }
//                    else if (it.dotPos == 0) {
//                        sb.append("<FONT color=\"red\">");
//                        sb.append(line);
//                        sb.append("</FONT>");
//                    }
//                    else {
//                        sb.append(line);
//                    }
//                    sb.append("<BR ALIGN=\"LEFT\"/>");
//                }
            sb.append(">");
            w.printf("%s [shape=box xlabel=\"%s\" label=%s];\n", id, id, sb);
            if (set.hasFinal()) {
                w.printf("%s [style=filled fillcolor=gray];\n", id);
            }
            for (var tr : set.transitions) {
                var labelBuf = new StringBuilder();
//                if (tr.symbol.astInfo.isFactor) {
//                    labelBuf.append("<<FONT color=\"green\">");
//                    labelBuf.append(tr.symbol);
//                    labelBuf.append("</FONT>>");
//                }
                labelBuf.append("\"").append(tr.symbol).append("\"");
                w.printf("%s -> %s [label=%s];\n", id, tr.target.stateId, labelBuf);
            }
        }
        w.println("\n}");
        w.flush();
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
                    } else {
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
            visitor.curRule = rd;
            if (rd.rhs.accept(visitor, null)) {
                build(rd);
            }
        }
        if (tree.options.dump) {
            Debug.dot(tree, this);
        }
    }

    public void build(RuleDecl rd) {
        var rule = rd.ref;
        queue.clear();
        all = new TreeSet<>(Comparator.comparingInt(set -> set.stateId));

        Log.log(Level.FINE, "building " + rule + " in " + tree.file.getName());

        var firstSet = new ItemSet(treeInfo);
        firstSet.isStart = true;
        firstSets.put(rule, firstSet);
        Set<Name> la;
        if (rule.equals(tree.start)) {
            la = new HashSet<>();
            la.add(ParserUtils.dollar);
        } else {
            la = LaFinder.computeLa(rule, tree);
            la.add(ParserUtils.dollar);
        }
        for (var firstDecl : treeInfo.ruleMap.get(rule)) {
            var firstItem = new Item(firstDecl, 0);
            firstItem.first = true;
            firstItem.gotoSet.add(firstSet);
            firstItem.lookAhead.addAll(la);
            firstSet.addItem(firstItem);
        }

        all.add(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            var curSet = queue.poll();
            Log.log(Level.FINEST, "curSet = " + curSet.stateId);
            //closure here because it needs all items
            curSet.closure();
            if (curSet.which.isEmpty() && curSet.hasFinal()) {
                curSet.which = findWhich(curSet, rule);
            }
            var map = new HashMap<Node, LLTransition>();
            for (var item : curSet.all) {
                //improve stars as non closured
                for (int i = item.dotPos; i < item.rhs.size(); i++) {
                    if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                    if (item.closured[i]) continue;

                    var node = item.getNode(i);
                    if (node instanceof Factored) continue;
                    var sym = (Node) ItemSet.sym(node.copy());
                    int newPos = node.isStar() ? i : i + 1;
                    if (curSet.isFactor(item, i, true)) {
                        sym.astInfo.isFactor = true;
                    }
                    //if not factor dont add to queue
                    if (!sym.astInfo.isFactor) {
                        //direct transition to alt final state
                        makeAlt(item, sym.asName(), curSet);
                    } else {
                        var target = new Item(item, newPos);
                        addMap(map, sym, new LLTransition.ItemPair(item, target), curSet);
                    }
                }
            }
            makeTrans(curSet, map);
        }
        //mergeSameSets();
        rules.put(rule, all);
    }

    void makeAlt(Item item, Name sym, ItemSet curSet) {
        var targetSet = new ItemSet(treeInfo);
        targetSet.noClosure = true;
        targetSet.alreadyGenReduces = true;
        targetSet.which = Optional.of(findAltEarly(item));
//        if (item.first) {
//            var target = new Item(item, item.rhs.size());
//            targetSet.addItem(target);
//        }
//        else {
//            if (item.firstParents.size() == 1) {
//                var p = item.firstParents.iterator().next();
//                var target = new Item(p, p.rhs.size());
//                targetSet.addItem(target);
//            }
//            else {
//                //throw new RuntimeException();
//            }
//        }

        all.add(targetSet);
        curSet.addTransition(new LLTransition(curSet, targetSet, sym));
    }

    int findAltEarly(Item item) {
        var cur = item;
        while (true) {
            if (cur.first && cur.rule.which.isPresent()) {
                return cur.rule.which.get();
            }
            if (cur.prev.isEmpty()) {
                cur = cur.parents.get(0);
            } else {
                cur = cur.prev.iterator().next();
            }
        }
    }

    void fillAlt() {
        for (var set : all) {
            for (var tr : set.transitions) {
                if (tr.target.which.isPresent()) {
                    tr.symbol.astInfo.which = tr.target.which;
                }
            }
        }
    }

    Optional<Integer> findWhich(ItemSet target, Name curRule) {
        var res = target.all.stream().filter(set -> isFinal(set, curRule)).findFirst();
        if (res.isPresent()) {
            return res.get().rule.which;
        }
        return Optional.empty();
    }

    boolean isFinal(Item item, Name curRule) {
        return item.isReduce(tree) && item.rule.ref.equals(curRule) && item.lookAhead.contains(dollar) && item.first;
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
            var tr = e.getValue();
            var targetSet = new ItemSet(treeInfo);
            targetSet.addAll(tr.pairs.stream().map(pair -> pair.target).collect(Collectors.toList()));
            targetSet.addAll(targetSet.genReduces());
            targetSet.alreadyGenReduces = true;

            var sim = findSimilar(new ArrayList<>(targetSet.kernel));
            if (sim != null) {
                ItemSet.lastId--;
                //merge lookaheads
                for (var it : targetSet.all) {
                    sim.update(it);
                }
                targetSet = sim;
            } else {
                all.add(targetSet);
                queue.add(targetSet);
            }
            tr.target = targetSet;
            curSet.addTransition(tr);
            Log.log(Level.FINE, String.format("trans %d -> %d with %s", curSet.stateId, targetSet.stateId, tr.symbol));
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

    public void mergeSameSets() {
        while (true) {
            var any = false;
            for (var set1 : all) {
                //find similar sets
                var similars = new HashSet<ItemSet>();
                for (var set2 : all) {
                    if (set2 == set1) continue;
                    if (isSame(set1, set2)) {
                        similars.add(set2);
                        any = true;
                    }
                }
                //remove sets
                if (!similars.isEmpty()) {
                    Log.log(Level.FINE, "group " + set1.stateId + "," + similars.stream().map(s -> String.valueOf(s.stateId)).collect(Collectors.joining(",")));
                    similars.forEach(all::remove);
                    merge(similars, set1);
                    //groups.put(similars, set1);
                    break;
                }
            }
            if (!any) break;
        }
    }

    private void merge(HashSet<ItemSet> similars, ItemSet set1) {
        for (var set : all) {
            for (var tr : set.transitions) {
                if (similars.contains(tr.from)) {
                    tr.from = set1;
                }
                if (similars.contains(tr.target)) {
                    tr.target = set1;
                }
            }
        }
    }

    //dump only transitions
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
                    if (tr.target.which.isPresent()) {
                        w.printf("%s#%s S%d?", tr.symbol.toString(), tr.target.which.get(), tr.target.stateId);
                    } else {
                        w.printf("%s S%d", tr.symbol.toString(), tr.target.stateId);
                    }
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
        Item.printLa = false;
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

    public List<File> dotAll(File dir) throws FileNotFoundException {
        var list = new ArrayList<File>();
        for (var e : rules.entrySet()) {
            var file = new File(dir, tree.file.getName() + "-" + e.getKey() + ".dot");
            dot(new PrintWriter(file), e.getValue());
            list.add(file);
        }
        return list;
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

//                var a = seq.get(i).copy();
//                var b = seq.get(i + 1).copy();
//                var info = GreedyNormalizer.hasGreedyTail(a, FirstSet.firstSet(b, tree), tree);
//                if (info != null) {
//                    throw new RuntimeException(String.format("%s has greediness, %s follows %s, list: %s, sym: %s", curRule.getName(), a, b, info.list, info.sym));
//                }
            }
            return false;
        }
    }
}
