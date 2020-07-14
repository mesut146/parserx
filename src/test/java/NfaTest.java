import dfa.NFA;
import nodes.Tree;

import java.io.File;

public class NfaTest {

    static File getFile() {
        return new File(Env.testDir, "test.g");
    }

    static NFA makeNFA(File grammar) throws Exception {
        Tree tree = Tree.makeTree(grammar);
        NFA nfa = tree.makeNFA();
        System.out.println("total nfa states=" + nfa.numStates);
        //nfa.dump("");
        return nfa;
    }
}
