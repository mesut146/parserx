package rule;

//rule*
public class StarRule extends Rule
{
    public Rule rule;
    
    public StarRule(){}
    
    public StarRule(Rule node){
        this.rule=node;
    }

    @Override
    public String toString()
    {
        return rule+"*";
    }
    
    
}
