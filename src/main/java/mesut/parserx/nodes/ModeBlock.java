package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.List;

public class ModeBlock {

    public String name;
    public List<TokenDecl> tokens = new ArrayList<>();

    public ModeBlock(String name) {
        this.name = name;
    }

}
