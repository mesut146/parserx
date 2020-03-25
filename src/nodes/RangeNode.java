package nodes;
import core.*;

public class RangeNode extends Node
{
    public StringNode start;
    public StringNode end;

    @Override
    public String toString()
    {
        return start+"-"+end;
    }
    
    
}
