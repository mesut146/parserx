package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class CcGen {
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;
    ItemSet curSet;
    NFA nfa;

    public CcGen(Tree tree) {
        this.tree = tree;
    }

    void build() {
        nfa = new NFA(100);
    }
}
