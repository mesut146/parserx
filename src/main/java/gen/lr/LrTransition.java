package gen.lr;

import nodes.NameNode;

public class LrTransition<T extends LrItemSet> {
    T from;
    T to;
    NameNode symbol;

    public LrTransition(T from, T to, NameNode symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return from.kernel + " by " + symbol + " to " + to.kernel;
    }
}
