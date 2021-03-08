package gen.lr;

import gen.BnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class Lr1Generator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<Lr1Item, Lr1ItemSet> table = new LrDFA<>();
    RuleDecl start;
    public static NameNode dollar = new NameNode("$");

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        //make start rule
        start = new RuleDecl("s'", tree.start);
        tree.addRule(start);

        check();
        start = tree.getRule("s'");

        Queue<Lr1ItemSet> queue = new LinkedList<>();

        Lr1Item first = new Lr1Item(start, 0);
        first.lookAhead.add(dollar);
        Lr1ItemSet firstSet = new Lr1ItemSet(first, tree);
        table.addId(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr1ItemSet curSet = queue.poll();
            for (Lr1Item from : curSet.getAll()) {
                curSet.done.add(from);
                NameNode symbol = from.getDotNode();
                if (symbol != null) {
                    //goto
                    Lr1Item toFirst = new Lr1Item((Lr1Item) from, from.dotPos + 1);
                    Lr1ItemSet targetSet = getSet(toFirst);
                    if (targetSet == null) {
                        targetSet = getOldSet(curSet, symbol);
                        if (targetSet == null) {
                            targetSet = new Lr1ItemSet(toFirst, tree);
                            table.addId(targetSet);
                            queue.add(targetSet);
                            table.addTransition(curSet, targetSet, symbol);
                        }
                        else {
                            //merge
                            targetSet.all.add(toFirst);
                            targetSet.kernel.add(toFirst);
                            targetSet.closure(toFirst);
                            /*targetSet = new Lr1ItemSet(toFirst, tree);
                            table.addId(targetSet);*/
                            queue.add(targetSet);
                        }
                        System.out.printf("%s %s to %s\n", symbol, printSet(curSet), printSet(targetSet));
                    }
                    else {
                        table.addTransition(curSet, targetSet, symbol);
                    }
                    //throw new RuntimeException();
                }
            }//for
        }

        //merge();
        writeDot();
    }

    //merge same kernel states with different lookaheads
    void merge() {
        LrDFA<Lr1Item, Lr1ItemSet> table2 = new LrDFA<>();
        Map<Integer, Map<NameNode, Lr1ItemSet>> map = new HashMap<>();
        for (Lr1ItemSet from : table.itemSets) {
            int id = table.getId(from);
            Map<NameNode, Lr1ItemSet> m = map.get(id);
            if (m == null) {
                m = new HashMap<>();
                map.put(id, m);
            }

            for (LrTransition<Lr1ItemSet> t : table.getTrans(from)) {
                /** if(table2.getTrans(from,t.symbol)){
                 }*/

                if (m.containsKey(t.symbol)) {
                    //merge
                    m.get(t.symbol).all.addAll(t.to.all);
                    //remove t
                    table2.addTransition(from, t.to, t.symbol);
                    table.getTrans(from).remove(t);
                }
                else {
                    m.put(t.symbol, t.to);
                }
            }
        }

    }

    private void writeDot() {
        try {
            PrintWriter dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            dotWriter.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            dotWriter.println("rankdir = TD");
            dotWriter.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");

            //labels
            for (Lr1ItemSet set : table.itemSets) {
                dotWriter.printf("%s [shape=box xlabel=\"%s\" %s label=\"%s\"]\n", table.getId(set), table.getId(set), isFinal(set) ? "color=red " : "", set.toString().replace("\n", "\\l") + "\\l");
            }

            for (int i = 0; i <= table.lastId; i++) {
                for (LrTransition<Lr1ItemSet> t : table.map[i]) {
                    dotWriter.printf("%s -> %s [label=\"%s\"]\n",
                            table.getId(t.from), table.getId(t.to), t.symbol.name);
                }
            }

            dotWriter.println("}");
            dotWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get itemSet that contains item
    Lr1ItemSet getSet(Lr1Item kernel) {
        for (Lr1ItemSet itemSet : table.itemSets) {
            for (LrItem item : itemSet.kernel) {
                if (item.equals(kernel)) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr1ItemSet getOldSet(Lr1ItemSet from, NameNode symbol) {
        for (LrTransition<Lr1ItemSet> transition : table.getTrans(from)) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    String printSet(Lr1ItemSet set) {
        return String.format("(%d)%s", table.getId(set), set.kernel);
    }

    boolean isFinal(Lr1ItemSet set) {
        //has epsilon
        for (LrItem item : set.all) {
            if (hasEpsilon(item.ruleDecl.rhs.asSequence())) {
                return true;
            }
        }
        return table.getTrans(set).isEmpty();
    }

    boolean hasEpsilon(Sequence node) {
        for (Node c : node) {
            if (c.isEmpty()) {
                return true;
            }
        }
        return false;
    }


    private void check() {
        PrepareTree.checkReferences(tree);
        BnfTransformer.expand_or = true;
        BnfTransformer.rhsSequence = true;
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);
    }
}
