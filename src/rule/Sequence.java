package rule;
import nodes.*;

//list of rules
public class Sequence extends Rule
{
    NodeList<Rule> list=new NodeList<>();
    
    public void add(Rule rule){
        list.add(rule);
    }
    
    public void addAll(){
        
    }
    
    public Rule normal(){
        if(list.list.size()==1){
            return list.get(0);
        }
        return this;
    }

    @Override
    public String toString()
    {
        return list.join(" ");
    }
    
    
}
