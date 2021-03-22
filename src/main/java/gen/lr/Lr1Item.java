package gen.lr;

import nodes.NameNode;
import nodes.NodeList;
import nodes.RuleDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lr1Item extends Lr0Item {

    public List<NameNode> lookAhead = new ArrayList<>();

    public Lr1Item(RuleDecl ruleDecl, int dotPos) {
        super(ruleDecl, dotPos);
    }

    public Lr1Item(Lr1Item item, int dotPos) {
        super(item.ruleDecl, dotPos);
        lookAhead = item.lookAhead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lr1Item item = (Lr1Item) o;
        return super.equals(item) && Objects.equals(lookAhead, item.lookAhead);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lookAhead != null ? lookAhead.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", " +
                NodeList.join(lookAhead, "/");
    }

}
