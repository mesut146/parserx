package gen.lr;

import nodes.NameNode;
import nodes.RuleDecl;

import java.util.ArrayList;
import java.util.List;

public class Lr1Item extends Lr0Item {

    List<NameNode> lookAhead = new ArrayList<>();

    public Lr1Item(RuleDecl ruleDecl, int dotPos) {
        super(ruleDecl, dotPos);
    }
}
