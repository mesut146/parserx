package gen.lr;

import nodes.NameNode;

public class Lr1Transition {
    Lr1ItemSet from;
    Lr1ItemSet to;
    NameNode symbol;

    public Lr1Transition(Lr1ItemSet from, Lr1ItemSet to, NameNode symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return from.first + " by " + symbol;
    }
}
