package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class LLTransition {
    ItemSet from;
    ItemSet target;
    Node symbol;
    List<ItemPair> pairs = new ArrayList<>();

    public LLTransition(ItemSet from, ItemSet target, Node symbol) {
        this.from = from;
        this.symbol = symbol;
        this.target = target;
    }

    @Override
    public String toString() {
        return "LLTransition{" +
                "from=" + from.stateId +
                ", target=" + target.stateId +
                ", symbol=" + symbol +
                '}';
    }

    public static class ItemPair {
        public Item origin;
        public Item target;

        public ItemPair(Item origin, Item target) {
            this.origin = origin;
            this.target = target;
        }
    }
}