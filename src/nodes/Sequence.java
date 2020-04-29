package nodes;

import nodes.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//list of rules
//rhs
public class Sequence extends Node {

    //public NodeList<Node> list = new NodeList<>();
    public List<Node> list = new ArrayList<>();

    public Sequence(Node... arr) {
        list.addAll(Arrays.asList(arr));
    }

    public void add(Node rule) {
        list.add(rule);
    }

    /*public Rule transform(RuleDecl decl, Tree tree) {
        Sequence s=new Sequence();
        for (Rule rule : list.list) {
            s.add(rule.transform(decl,tree));
        }
        return s;
    }*/

    public Node normal() {
        if (list.size() == 1) {
            return list.get(0);
        }
        return this;
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

    @Override
    public String toString() {
        return join(list, " ");
    }


}
