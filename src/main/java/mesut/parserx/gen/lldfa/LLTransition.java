package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;

public class LLTransition {
    Node symbol;
    ItemSet from;
    ItemSet target;

    public LLTransition(ItemSet from, Node symbol, ItemSet target) {
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
}