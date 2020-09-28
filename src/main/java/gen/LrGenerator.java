package gen;

import nodes.NameNode;
import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// lr(0)
public class LrGenerator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    List<LrTransition> transitions;
    List<Lr0ItemSet> itemSets = new ArrayList<>();

    public LrGenerator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        check();
        //System.out.println(tree);

        transitions = new ArrayList<>();
        Queue<Lr0ItemSet> queue = new LinkedList<>();

        RuleDecl start = new RuleDecl("s'", tree.start);
        Lr0Item first = new Lr0Item(start, 0);
        Lr0ItemSet firstSet = new Lr0ItemSet(first, tree);
        itemSets.add(firstSet);
        firstSet.closure();
        //System.out.println("\nset = " + firstSet);

        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr0ItemSet curSet = queue.peek();
            Lr0Item from = curSet.findTransitable();
            if (from != null) {
                NameNode symbol = from.getDotNode();
                Lr0Item toFirst = new Lr0Item(from.ruleDecl, ++from.dotPos);
                if (toFirst.getDotNode() == null) {//we cant transit any more->increment
                    curSet.curIndex++;
                }
                Lr0ItemSet toSet;
                toSet = getSet(toFirst);
                if (toSet == null) {
                    toSet = new Lr0ItemSet(toFirst, tree);
                    itemSets.add(toSet);
                    toSet.closure();
                }
                System.out.println("from " + curSet.first);
                System.out.println("to " + toSet.first);
                transitions.add(new LrTransition(curSet, toSet, symbol));
                queue.add(toSet);

            }
            else {
                queue.poll();
            }

        }


    }

    //get itemSet that contains item
    Lr0ItemSet getSet(Lr0Item first) {
        for (Lr0ItemSet itemSet : itemSets) {
            if (itemSet.first.equals(first)) {
                return itemSet;
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr0ItemSet getTransition(Lr0ItemSet from, Node symbol) {
        for (LrTransition transition : transitions) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    private void check() {
        PrepareTree.checkReferences(tree);
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
    }
}
