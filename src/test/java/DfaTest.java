import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.IOUtils;
import mesut.parserx.dfa.NfaReader;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

public class DfaTest {

    @Test
    public void hopcroft() throws Exception {
        //File file = Env.getResFile("fsm/dfa-min.dfa");
        File file = Env.getResFile("min/dfa2.dfa");
        NFA dfa = NfaReader.read(IOUtils.read(file));
        //Minimization.removeUnreachable(dfa);
        //dfa = Minimization.optimize(dfa);
        dfa = Minimization.Hopcroft(dfa);
        dfa.dump();
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
    }

    @Test
    public void minimizeMy() throws Exception {
        File file = Env.getResFile("min/dfa-min.dfa");
        //File file = Env.getResFile("fsm/dfa2.dfa");
        NFA dfa = NfaReader.read(IOUtils.read(file));
        Minimization.removeUnreachable(dfa);
        Minimization.removeDead(dfa);
        Minimization.optimize(dfa);
        //dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void minimize2() throws Exception {
        NFA dfa = NFA.makeDFA(Env.getResFile("javaLexer.g"));
        System.out.println("before " + Minimization.numOfStates(dfa));
        //Minimization.removeUnreachable(dfa);
        //Minimization.removeDead(dfa);
        //dfa = Minimization.Hopcroft(dfa);
        dfa = Minimization.optimize(dfa);
        dfa = Minimization.combineAlphabet(dfa);
        System.out.println("after " + Minimization.numOfStates(dfa));
        //dfa.dump();
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
    }

    @Test
    public void name() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("min/a.g"));
        NFA dfa = tree.makeNFA().dfa();
        //dfa = Minimization.Hopcroft(dfa);
        dfa = Minimization.optimize(dfa);
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
        dfa.dump();
    }
}
