package nodes;

//base type places in grammar
public class Node
{
    String name;
    
    public int getType(){
        return name.hashCode();
    }
}
