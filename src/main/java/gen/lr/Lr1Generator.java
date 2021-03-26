package gen.lr;

import gen.BnfTransformer;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class Lr1Generator {
    public static NameNode dollar = Lr0Generator.dollar;
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<Lr1ItemSet> table = new LrDFA<>();
    RuleDecl start;

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        check();

        Queue<Lr1ItemSet> queue = new LinkedList<>();

        LrItem first = new LrItem(start, 0);
        first.lookAhead.add(dollar);
        Lr1ItemSet firstSet = new Lr1ItemSet(first, tree);
        table.addId(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr1ItemSet curSet = queue.poll();
            for (LrItem from : curSet.getAll()) {
                curSet.done.add(from);
                NameNode symbol = from.getDotNode();
                if (symbol == null) continue;
                //goto
                LrItem toFirst = new LrItem(from, from.dotPos + 1);
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
                        System.out.println("merge");
                        //merge
                        targetSet.addItem(toFirst);
                        /*targetSet.all.add(toFirst);
                        targetSet.kernel.add(toFirst);
                        targetSet.closure(toFirst);*/
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
            }//for
        }

        //merge();
    }

    //merge same kernel states with different lookaheads
    public void merge() {
        LrDFA<Lr1ItemSet> table2 = new LrDFA<>();
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

    public void writeDot(PrintWriter dotWriter) {
        if (dotWriter == null) {
            try {
                dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        DotWriter.writeDot(table, dotWriter);
    }

    public void makeTable() {
        DotWriter.lr1Table(this);
    }

    //get itemSet that contains item
    Lr1ItemSet getSet(LrItem kernel) {
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

    private void check() {
        PrepareTree.checkReferences(tree);
        BnfTransformer.expand_or = true;
        BnfTransformer.rhsSequence = true;
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);

        start = Lr0Generator.makeStart(tree);
    }
}
