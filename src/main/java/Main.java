
import nodes.*;
import java.io.*;
import gen.*;public class Main
 {


    public static void main(String[] args) {
        String path="/storage/emulated/0/AppProjects/parserx/src/test/resources/leftRec.g";
        Tree tree=Tree.makeTree(new File(path));
        RuleDecl rule=tree.getRule("A");
        LeftRecursive l=new LeftRecursive(tree);
        l.removeDirect(rule);
        System.out.println(rule);
        l.removeDirect(rule);
        System.out.println(rule);
        /*
        Node n = rule.rhs;
        //System.out.println(n);
        LeftRecursive.SplitInfo s =l.split(n,rule.ref());
        System.out.println("1="+s.one);
        System.out.println("0="+s.zero);*/
    }
    
}
