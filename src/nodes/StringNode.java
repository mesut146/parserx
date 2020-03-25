package nodes;

public class StringNode extends TokenNode
{
    public String value;
    
    public StringNode(){}
    
    public StringNode(String value){
        this.value=value;
    }

    @Override
    public String toString()
    {
        return value;
    }
    
    
}
