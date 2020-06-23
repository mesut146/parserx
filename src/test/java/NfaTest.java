import dfa.DFA;
import dfa.NFA;
import gen.LexerGenerator;
import nodes.Tree;

import java.io.File;

public class NfaTest {

    static File getFile() {
        return new File(Env.testDir, "test.g");
    }

    static NFA makeNFA(File grammar) throws Exception {
        //tokens(parser);
        Tree tree = Tree.makeTree(grammar);
        //System.out.println(tree);
        //System.out.println("----------");
        //System.out.println(tree);
        NFA nfa = tree.makeNFA();
        System.out.println("total nfa states=" + nfa.numStates);
        //nfa.dump("");
        //nfa.dot(dir + "asd.dot");
        return nfa;
    }
}
