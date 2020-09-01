package nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//list of rules
//rhs
public class Sequence extends Node implements Iterable<Node> {

    public List<Node> list = new ArrayList<>();

    public Sequence(Node... arr) {
        list.addAll(Arrays.asList(arr));
    }

    public static <T> String join(List<T> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    /*public Rule transform(RuleDecl decl, Tree tree) {
        Sequence s=new Sequence();
        for (Rule rule : list.list) {
            s.add(rule.transform(decl,tree));
        }
        return s;
    }*/

    public void add(Node rule) {
        list.add(rule);
    }

    public Node normal() {
        if (list.size() == 1) {
            return list.get(0);
        }
        return this;
    }

    @Override
    public String toString() {
        return join(list, " ");
    }


    @Override
    public Iterator<Node> iterator() {
        return list.iterator();
    }
}
