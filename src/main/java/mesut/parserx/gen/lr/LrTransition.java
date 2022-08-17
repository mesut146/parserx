package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;

public class LrTransition {
    LrItemSet from;
    LrItemSet target;
    Name symbol;

    public LrTransition(LrItemSet from, LrItemSet target, Name symbol) {
        this.from = from;
        this.target = target;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return from.kernel + " by " + symbol + " to " + target.kernel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (LrTransition) o;
        return symbol.equals(that.symbol) && from.equals(that.from) && target.equals(that.target);
    }

}
