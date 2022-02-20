package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;

public class Transition{
  Name symbol;
  ItemSet target;

  public Transition(Name symbol, ItemSet target){
    this.symbol = symbol;
    this.target = target;
  }

}