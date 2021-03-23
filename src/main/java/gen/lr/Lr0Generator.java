package gen.lr;

import gen.parser.BnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

// lr(0)
public class Lr0Generator extends IndentWriter {
    public static NameNode dollar = new NameNode("$");
    public PrintWriter dotWriter;
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    LrDFA<Lr0ItemSet> table = new LrDFA<>();
    Map<RuleDecl, Integer> itemIds = new HashMap<>();
    RuleDecl start;

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        start = new RuleDecl("s'", tree.start);
        tree.addRule(start);

        check();
        start = tree.getRule("s'");

        Queue<Lr0ItemSet> queue = new LinkedList<>();

        Lr0Item first = new Lr0Item(start, 0);
        Lr0ItemSet firstSet = new Lr0ItemSet(first, tree);
        table.addId(firstSet);
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr0ItemSet curSet = queue.poll();
            for (Lr0Item from : curSet.all) {
                //curSet.done.add(from);
                NameNode symbol = from.getDotNode();
                if (symbol != null) {
                    //goto
                    Lr0Item toFirst = new Lr0Item(from.ruleDecl, from.dotPos + 1);
                    Lr0ItemSet targetSet = getSet(toFirst);
                    if (targetSet == null) {
                        targetSet = getOldSet(curSet, symbol);
                        if (targetSet == null) {
                            targetSet = new Lr0ItemSet(toFirst, tree);
                            table.addId(targetSet);
                            queue.add(targetSet);
                            table.addTransition(curSet, targetSet, symbol);
                        }
                        else {
                            System.out.println("merge " + curSet);
                            //merge
                            //targetSet.all.add(toFirst);
                            targetSet.kernel.add(toFirst);
                            targetSet.closure();
                            queue.add(targetSet);
                        }
                        System.out.printf("%s %s to %s\n", symbol, printSet(curSet), printSet(targetSet));
                    }
                    else {
                        table.addTransition(curSet, targetSet, symbol);
                    }
                    if (toFirst.getDotNode() == null) {
                        targetSet.reduce = toFirst;
                    }
                    //throw new RuntimeException();
                }
            }//for
        }
        writeDot();
        makeTable();
    }

    void writeSource(){

    }

    void makeTable() {
        DotWriter.lr0Table(this);
    }

    private void writeDot() {
        if (dotWriter == null) {
            try {
                dotWriter = new PrintWriter(new File(dir, tree.file.getName() + ".dot"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        DotWriter.writeDot(this, dotWriter);
    }

    //get itemSet that contains item
    Lr0ItemSet getSet(Lr0Item kernel) {
        for (Lr0ItemSet itemSet : table.itemSets) {
            for (Lr0Item item : itemSet.kernel) {
                if (item.equals(kernel)) {
                    return itemSet;
                }
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr0ItemSet getOldSet(Lr0ItemSet from, NameNode symbol) {
        for (LrTransition<Lr0ItemSet> transition : table.getTrans(from)) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    String printSet(Lr0ItemSet set) {
        return String.format("(%d)%s", table.getId(set), set.kernel);
    }

    private void check() {
        PrepareTree.checkReferences(tree);
        BnfTransformer.rhsSequence = true;
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);

        int id = 0;
        for (RuleDecl rule : tree.rules) {
            itemIds.put(rule, id++);
        }
    }
}
