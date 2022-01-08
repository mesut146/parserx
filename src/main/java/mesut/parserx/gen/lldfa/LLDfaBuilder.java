package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.lr.LrItem;
import mesut.parserx.gen.lr.LrItemSet;
import mesut.parserx.nodes.*;

import java.util.*;

public class LLDfaBuilder {
    public NFA dfa = new NFA(100);
    public RuleDecl start;
    Tree tree;
    Alphabet alphabet;
    LrItem first;

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        dfa.tree = tree;
    }

    public void makeStart() {
        if (tree.start == null) {
            throw new RuntimeException("no start rule is declared");
        }
        start = new RuleDecl(LrDFAGen.startName, new Sequence(tree.start));
        tree.addRule(start);
    }

    void prepare() {
        new Normalizer(tree).normalize();
        tree.prepare();

        makeStart();
        first = new LrItem(start, 0);
        first.lookAhead.add(LrDFAGen.dollar);

    }

    public void build() {
        makeAlphabet();
        prepare();

        Queue<LrItemSet> queue = new LinkedList<>();
        LrItemSet firstSet = new LrItemSet(Collections.singleton(first), tree, "lr1");
        queue.add(firstSet);

        while (!queue.isEmpty()) {
            LrItemSet curSet = queue.poll();
            //symbol -> target set
            Map<Name, LrItemSet> map = new HashMap<>();
            while (true) {
                LrItem item = curSet.getItem();
                Node symNode = item.getDotNode();
                Name sym = symNode.isName() ? symNode.asName() : symNode.asRegex().node.asName();
                if (sym == null) continue;

                LrItem target = new LrItem(item, item.dotPos + 1);

                LrItemSet targetSet = map.get(sym);
                if (targetSet == null) {
                    targetSet = new LrItemSet(Collections.singleton(target), tree, "lr1");
                    map.put(sym, targetSet);
                    queue.add(targetSet);
                }
            }
        }
    }

    void addKernel(LrItemSet set, LrItem item) {
        set.kernel.add(item);
    }


    void makeAlphabet() {
        alphabet = new Alphabet();
        tree.alphabet = alphabet;
        alphabet.lastId = 1;//skip eof
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            alphabet.addRegex(decl.ref());
        }
    }


    int getId(Name token) {
        return alphabet.getId(token);
    }

}
