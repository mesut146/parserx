package gen;

import nodes.NameNode;

public class LrTransition {
    Lr0ItemSet from;
    Lr0ItemSet to;
    NameNode symbol;

    public LrTransition(Lr0ItemSet from, Lr0ItemSet to, NameNode symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }
}
