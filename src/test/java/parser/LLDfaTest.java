package parser;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.*;

public class LLDfaTest {

    void dots(NFA nfa, File f) throws IOException {
        File file = Env.dotFile(Utils.newName(f.getName(), ".dot"));
        nfa.dot(new FileWriter(file));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }
    
    void dot(LLDfaBuilder b) throws IOException {
        File file = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dot"));
        b.dot(new PrintWriter(new FileWriter(file)));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    void single(String path) throws IOException{
        System.out.println("------------------------------------");
        Tree tree = Env.tree(path);
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.factor();
        //builder.normalize();
        dot(builder);
    }    

    @Test
    public void dfa() throws IOException {
         Env.dir = "/asd/parserx";
        //Tree tree = Env.tree("lldfa/a.g");
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        single("lldfa/factor.g");
        single("lldfa/mid.g");
        single("lldfa/mid2.g");
        single("lldfa/left.g");
        single("lldfa/left-indirect.g");
        single("lldfa/right.g");
        single("lldfa/right-indirect.g");
        single("lldfa/greedy.g");
        single("lldfa/greedy2.g");
        single("lldfa/or.g");
        single("lldfa/or-mid.g");
        single("lldfa/or2.g");
        single("lldfa/len2.g");
        single("lldfa/sr.g");
        //single("lldfa/sr2.g");
        single("lldfa/rr.g");
    }
    
    

}
