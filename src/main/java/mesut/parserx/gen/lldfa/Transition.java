package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;

public class Transition{
    Node symbol;
    ItemSet from;
    ItemSet target;

  public Transition(ItemSet from, Node symbol, ItemSet target){
      this.from = from;
      this.symbol = symbol;
      this.target = target;
  }

}