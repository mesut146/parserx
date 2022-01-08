package parser;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LLDfaTest {

    void dots(NFA nfa, File f) throws IOException {
        File file = Env.dotFile(Utils.newName(f.getName(), ".dot"));
        nfa.dot(new FileWriter(file));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    @Test
    public void dfa() throws IOException {
        //Tree tree = Env.tree("lldfa/a.g");
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        Tree tree = Env.tree("lldfa/factor.g");
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.build();
        //dfa = Minimization.combineAlphabet(dfa);
        dots(builder.dfa, tree.file);
    }

    @Test
    public void llregex() throws IOException {
        //Tree tree = Env.tree("lldfa/a.g");
        Tree tree = Env.tree("lldfa/factor.g");
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.build();
        //c | a a* c | c b b* | a a* c b b*
        dots(builder.dfa, tree.file);
    }

}
