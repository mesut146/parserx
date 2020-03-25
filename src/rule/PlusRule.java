package rule;

//rule+
public class PlusRule extends Rule
{
    public Rule rule;

    public PlusRule(){}
    
    public PlusRule(Rule node)
    {
        this.rule = node;
    }

    @Override
    public String toString()
    {
        return rule+"+";
    }
    
   
}
