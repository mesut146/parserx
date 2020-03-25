package nodes;
import java.util.*;

public class Tree extends NodeList
{

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append(join("\n"));
        return sb.toString();
    }
    
    
}
