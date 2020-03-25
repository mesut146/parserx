package nodes;
import java.util.*;

public class NodeList extends Node
{
    public List<Node> list=new ArrayList<>();
    
    public void add(Node node){
        list.add(node);
    }
    
    public void addAll(List<Node> other){
        list.addAll(other);
    }
    
    public void addAll(NodeList other){
        list.addAll(other.list);
    }
    
    public Node get(int index){
        return list.get(index);
    }

    public static <T> String join(List<T> list,String del){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<list.size();i++){
            sb.append(list.get(i));
            if(i<list.size()-1){
                sb.append(del);
            }
        }
        return sb.toString();
    }
    
    public String join(String del){
        return join(list,del);
    }
    
    
}
