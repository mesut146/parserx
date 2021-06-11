package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.Tree;

import java.util.HashMap;
import java.util.Map;


public class Lr1Generator extends LRGen<Lr1ItemSet> {

    public Lr1Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    @Override
    void prepare() {
        super.prepare();
        first.lookAhead.add(dollar);
    }


    @Override
    public Lr1ItemSet makeSet(LrItem item) {
        return new Lr1ItemSet(item, tree);
    }

    //lalr merger
    //merge sets that have same kernel
    public void merge() {
        LrDFA<Lr1ItemSet> res = new LrDFA<>();
        Map<Integer, Map<NameNode, Lr1ItemSet>> map = new HashMap<>();
        for (Lr1ItemSet from : table.itemSets) {
            int id = table.getId(from);
            Map<NameNode, Lr1ItemSet> m = map.get(id);
            //init ids
            if (m == null) {
                m = new HashMap<>();
                map.put(id, m);
            }
            for (LrItemSet other : table.itemSets) {
                if (from == other) continue;
                if(from.kernel.equals(other.kernel)){

                }
            }

        }

    }


}
