package nodes;
import java.util.*;

//lexer or node aka character list
//[a-zA-Z_0-9]
//consist of char,char range
public class Bracket extends Node
{
    NodeList list=new NodeList();
    public boolean negate;//[^abc]
    
    public void add(Node node){
        list.add(node);
    }
    
    public void add(char chr){
        list.add(new CharNode(chr));
    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("[");
        if(negate){
            sb.append("^");
        }
        sb.append(list.join(""));
        sb.append("]");
        return sb.toString();
    }
    
    
    
    static class CharNode extends Node{
        char chr;
        public CharNode(char chr){
            this.chr=chr;
        }
    }
}
