package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;

public class Transition{
  Node symbol;
  ItemSet target;

  public Transition(Node symbol, ItemSet target){
    this.symbol = symbol;
    this.target = target;
  }

}