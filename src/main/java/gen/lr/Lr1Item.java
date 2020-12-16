package gen.lr;

import nodes.NameNode;
import nodes.NodeList;
import nodes.RuleDecl;

import java.util.ArrayList;
import java.util.List;

public class Lr1Item extends Lr0Item {

    public List<NameNode> lookAhead = new ArrayList<>();

    public Lr1Item(RuleDecl ruleDecl, int dotPos) {
        super(ruleDecl, dotPos);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append(NodeList.join(lookAhead, "/"));
        return sb.toString();
    }
}
