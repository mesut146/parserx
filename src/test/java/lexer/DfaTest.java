package lexer;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.FileWriter;

public class DfaTest {

    @Test
    public void hopcroft() throws Exception {
        NFA dfa = NFA.read(Env.getResFile("min/dfa2.dfa"));
        //Minimization.removeUnreachable(dfa);
        //dfa = Minimization.optimize(dfa);
        dfa = Minimization.Hopcroft(dfa);
        dfa.dump();
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
    }

    @Test
    public void minimizeMy() throws Exception {
        //File file = Env.getResFile("fsm/dfa2.dfa");
        NFA dfa = NFA.read(Env.getResFile("min/dfa-min.dfa"));
        Minimization.removeUnreachable(dfa);
        Minimization.removeDead(dfa);
        Minimization.optimize(dfa);
        //dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void loop() throws Exception {
        Tree tree = Env.tree("min/a.g");
        NFA dfa = tree.makeNFA().dfa();
        //dfa = Minimization.Hopcroft(dfa);
        dfa = Minimization.optimize(dfa);
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
        dfa.dump();
    }
}
