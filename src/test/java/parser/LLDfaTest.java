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
    public void all() throws IOException {
		File dir=new File(Env.dir, "src/test/resources/lldfa");
		for(String s : dir.list()){
			single("lldfa/"+s);
		}
	}

    @Test
    public void dfa() throws IOException {
         Env.dir = "/asd/parserx";
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        single("lldfa/mid.g");
        single("lldfa/mid2.g");
        single("lldfa/left.g");
        single("lldfa/left-indirect.g");
        single("lldfa/right.g");
        single("lldfa/right-indirect.g");
        
        single("lldfa/factor.g");
        single("lldfa/greedy.g");
        single("lldfa/greedy2.g");
        single("lldfa/rr-loop.g");
        single("lldfa/rr-loop-len2.g");
        single("lldfa/rr-loop-x.g");
        single("lldfa/rr-loop2.g");
        single("lldfa/rr-loop2-len2.g");
        single("lldfa/len2.g");
        single("lldfa/sr.g");
        single("lldfa/sr2.g");
        single("lldfa/rr.g");
        single("lldfa/rr-loop-sub.g");
        single("lldfa/rr-loop-rec.g");
        single("lldfa/sr-loop.g");
        //single("lldfa/rr2.g");
        
        single("lldfa/rr-loop-deep.g");
        single("lldfa/rr-loop-deep1.g");
        single("lldfa/rr-loop-deep2.g");
        single("lldfa/rr-loop-deep3.g");
        single("lldfa/rr-loop-deep4.g");
    }
    @Test
    public void single() throws IOException {
         //Env.dir = "/asd/parserx";
         single("lldfa/rr-loop2-len2.g");
         single("lldfa/rr-loop-x.g");
    }
    

}
