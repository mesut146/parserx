package mesut.parserx.gen.lldfa;

import mesut.parserx.nodes.*;
import mesut.parserx.gen.lr.*;

public class TreeBuilder{
    public ItemSet startSet;
    Tree res;
    
    public Tree build(){
        res = new Tree();
        return res;
    }


}