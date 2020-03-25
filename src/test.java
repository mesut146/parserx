
import java.util.*;public class test
{
    class Symbol{
        String name;
        int id;
    }
    class Token{
        String name;
    }
    
    Stack<Token> tokens=new Stack<>();
    Stack<Symbol> symbols=new Stack<>();
    
    public void test(){
        //rule1: t1 t2?
        //rule2: t3 t4 t5
        /*while(true){
            Token cur=null;
            tokStack.push(cur);
            for(rule in rules){
                if(rule match tokStack){
                    stack.push(make(tokStack));//reduce
                }
            }
        }*/
    }
    
}
