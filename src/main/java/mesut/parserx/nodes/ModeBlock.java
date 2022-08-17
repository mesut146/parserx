package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.List;

public class ModeBlock extends Node {

    public String name;
    public List<TokenDecl> tokens = new ArrayList<>();
    public List<ModeBlock> blocks = new ArrayList<>();


    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitModeBlock(this, arg);
    }
}
