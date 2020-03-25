package rule;
import java.util.*;
import nodes.*;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node
{
    public String name;
    public NodeList list=new NodeList();
    
    public RuleDecl(){}
    
    public RuleDecl(String name){
        this.name=name;
    }
    
    public void add(Node node){
        list.add(node);
    }

    @Override
    public String toString()
    {
        return name+" = "+list.join(" ")+";";
    }

    
}
