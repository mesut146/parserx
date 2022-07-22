package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class CcGen {
    LLDfaBuilder builder;
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;
    ItemSet curSet;
    GrammarEmitter emitter;

    void decide() {
        //abcx | abdy
        emitter = new GrammarEmitter(builder);
        emitter.makeForRule("E");
    }

    void eliminate_nonfactors(Set<ItemSet> all) {
        for (var set : all) {
            for (var tr : set.transitions) {
                if (tr.symbol.astInfo.isFactor) continue;
                if (tr.target.stateId == set.stateId) continue;
                if (!emitter.hasFinal(tr.target)) {
                    System.out.println("eliminate " + tr.target.stateId);
                }
            }
        }
    }
}
