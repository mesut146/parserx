package parser;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.LLDfaBuilder;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LLDfaTest {

    void dots(NFA nfa) throws IOException {
        File file = Env.dotFile(Utils.newName(nfa.tree.file.getName(), ".dot"));
        nfa.dot(new FileWriter(file));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    @Test
    public void dfa() throws IOException {
        Tree tree = Env.tree("lldfa/a.g");
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.build();
        NFA nfa = builder.dfa;
        NFA dfa = nfa.dfa();
        //dfa = Minimization.optimize(dfa);
        dots(nfa);
    }
}
