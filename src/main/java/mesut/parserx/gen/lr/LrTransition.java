package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

public class LrTransition {
    LrItemSet from;
    LrItemSet to;
    Name symbol;

    public LrTransition(LrItemSet from, LrItemSet to, Name symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return from.kernel + " by " + symbol + " to " + to.kernel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LrTransition that = (LrTransition) o;
        return symbol.equals(that.symbol) && from.equals(that.from) && to.equals(that.to);
    }

}
