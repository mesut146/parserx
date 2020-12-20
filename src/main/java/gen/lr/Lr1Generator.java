package gen.lr;

import gen.EbnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.*;

import java.io.PrintWriter;
import java.util.*;


public class Lr1Generator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    List<Lr1Transition> transitions;
    List<Lr1ItemSet> itemSets;
    public PrintWriter dotWriter;
    Map<Lr1ItemSet, Integer> idMap = new HashMap<>();
    int lastId = -1;
    RuleDecl start;
    NameNode dollar=new NameNode("$");

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        check();
        //System.out.println(tree);
        dotBegin();

        transitions = new ArrayList<>();
        itemSets = new ArrayList<>();
        Queue<Lr1ItemSet> queue = new LinkedList<>();

        //make start rule
        start = new RuleDecl("s'", tree.start);
        if (!start.rhs.isSequence()) {
            start.rhs = new Sequence(start.rhs);
        }
        Lr1Item first = new Lr1Item(start, 0);
        first.lookAhead.add(dollar);
        Lr1ItemSet firstSet = new Lr1ItemSet(first, tree);
        addId(firstSet);
        itemSets.add(firstSet);
        firstSet.closure();
        //System.out.println("\nset = " + firstSet);

        queue.add(firstSet);

        while (!queue.isEmpty()) {
            Lr1ItemSet curSet = queue.peek();
            Lr1Item from = curSet.findTransitable();
            if (from != null) {
                NameNode symbol = from.getDotNode();
                Lr1Item toFirst = new Lr1Item(from, from.dotPos2 + 1);
                curSet.curIndex++;
                Lr1ItemSet targetSet;
                targetSet = getSet(toFirst);
                if (targetSet == null) {
                    targetSet = getTransition(curSet, symbol);
                    if (targetSet == null) {//new transition
                        targetSet = new Lr1ItemSet(toFirst, tree);
                        addId(targetSet);
                        itemSets.add(targetSet);
                        targetSet.closure();
                        System.out.printf("transition(%d) by %s (%s)%s to (%s)%s\n", transitions.size(), symbol, getId(curSet), curSet.kernel, getId(targetSet), targetSet.kernel);
                        transitions.add(new Lr1Transition(curSet, targetSet, symbol));
                    }
                    else {
                        //merge and update transition
                        if (!targetSet.kernel.contains(toFirst)) {
                            targetSet.kernel.add(toFirst);
                            targetSet.all.clear();
                            targetSet.all.addAll(targetSet.kernel);
                            targetSet.closure();

                            targetSet.curIndex = targetSet.kernel.size() - 1;
                        }
                    }
                    queue.add(targetSet);
                }
            }
            else {
                queue.poll();
            }
            curSet.done.add(from);
        }
        dotLabels();
        for (Lr1Transition transition : transitions) {
            dot(transition.from, transition.to, transition.symbol);
        }
        dotEnd();
    }

    void addId(Lr1ItemSet item) {
        idMap.put(item, ++lastId);
    }

    //get itemSet that contains item
    Lr1ItemSet getSet(Lr1Item first) {
        for (Lr1ItemSet itemSet : itemSets) {
            if (itemSet.kernel.contains(first)) {
                return itemSet;
            }
        }
        return null;
    }

    //if there exist another transition from this
    Lr1ItemSet getTransition(Lr1ItemSet from, Node symbol) {
        for (Lr1Transition transition : transitions) {
            if (transition.from.equals(from) && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return null;
    }

    private void dotBegin() {
        if (dotWriter != null) {
            dotWriter.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            dotWriter.println("rankdir = TD");
            dotWriter.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");
        }
    }

    void dotEnd() {
        if (dotWriter != null) {
            dotWriter.println("}");
            dotWriter.flush();
            dotWriter.close();
        }
    }

    private void dot(Lr1ItemSet curSet, Lr1ItemSet toSet, NameNode symbol) {
        if (dotWriter != null) {
            dotWriter.printf("%s -> %s [label=\"%s\"]\n", getId(curSet), getId(toSet), symbol.name);
        }
    }

    int getId(Lr1ItemSet itemSet) {
        for (Lr1Item kernel : itemSet.kernel) {
            int id = getId(kernel);
            if (id != -1) {
                return id;
            }
        }
        return -1;
    }

    int getId(Lr1Item item) {
        for (Map.Entry<Lr1ItemSet, Integer> entry : idMap.entrySet()) {
            if (entry.getKey().kernel.contains(item)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    void dotLabels() {
        if (dotWriter != null) {
            for (Lr1ItemSet set : itemSets) {
                if (isFinal(set)) {
                    dotWriter.printf("%s [color=red shape=box xlabel=\"%s\" label=\"%s\"]\n", getId(set), getId(set), set.toString().replace("\n", "\\n"));
                }
                else {
                    dotWriter.printf("%s [shape=box xlabel=\"%s\" label=\"%s\"]\n", getId(set), getId(set), set.toString().replace("\n", "\\n"));
                }

            }
        }
    }

    boolean isFinal(Lr1ItemSet set) {
        //has epsilon
        for (Lr1Item item : set.all) {
            if (hasEpsilon(item.ruleDecl.rhs)) {
                return true;
            }
        }
        for (Lr1Transition transition : transitions) {
            if (set.equals(transition.from)) {
                return false;
            }
        }
        return true;
    }

    boolean hasEpsilon(Node node) {
        if (node.isEmpty()) {
            return true;
        }
        if (node.isSequence()) {
            for (Node c : node.asSequence()) {
                if (c.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }


    private void check() {
        PrepareTree.checkReferences(tree);
        EbnfTransformer.expand_or = true;
        EbnfTransformer.rhsSequence = true;
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
    }
}
