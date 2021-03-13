
import nodes.*;
import java.io.*;
import gen.*;public class Main
 {


    public static void main(String[] args) {
        String path="/storage/emulated/0/AppProjects/parserx/src/test/resources/leftRec.g";
        Tree tree=Tree.makeTree(new File(path));
        String r;
        //r="a? c | b* (c? A)+";
        //r="(A|b) a? b";//E|A a? b
        //r="(b|c)? A";//A|b A
        r="(a+ | A)*";
        //r="(a* A b)+";
        
        Node n=tree.getRule("A").rhs;
        
        //System.out.println(n);
        LeftRecursive.SplitInfo s =new LeftRecursive(tree).split(n,new NameNode("A",false));
        System.out.println("1="+s.one);
        System.out.println("0="+s.zero);
    }
    
}
