package rule;

import nodes.Node;
import nodes.NodeList;
import java.util.*;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {
    
    public String name;
    public NodeList list = new NodeList();

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this.name = name;
    }

    public void add(Node node) {
        list.add(node);
    }
    
    public void addAll(List<Node> other){
        list.addAll(other);
    }
    
    /*public void addAll(List<Rule> other){
        list.addAll((List)other);
    }*/

    @Override
    public String toString() {
        return name + " = " + list.join(" ") + ";";
    }


}
