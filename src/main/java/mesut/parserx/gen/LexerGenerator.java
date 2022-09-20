package mesut.parserx.gen;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.gen.targets.CppLexer;
import mesut.parserx.gen.targets.JavaLexer;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

public class LexerGenerator {
    public IdMap idMap = new IdMap();
    public NFA dfa;
    public Tree tree;
    public boolean[] skipList;
    public int[] idArr;
    public TreeSet<Map.Entry<Name, Integer>> tokens;
    Options options;
    Lang target;

    public LexerGenerator(Tree tree, Lang target) {
        this.tree = tree;
        this.dfa = tree.makeNFA().dfa();
        this.dfa = Minimization.optimize(this.dfa);
        this.options = tree.options;
        this.target = target;
    }

    public static LexerGenerator gen(Tree tree, Lang target) throws IOException {
        var gen = new LexerGenerator(tree, target);
        gen.generate();
        return gen;
    }

    //compress boolean bits to integers
    public static int[] makeIntArr(boolean[] arr) {
        int[] res = new int[arr.length / 32 + 1];
        int pos = 0;
        int cur;
        for (int start = 0; start < arr.length; start += 32) {
            cur = 0;
            for (int j = 0; j < 32 && start + j < arr.length; j++) {
                int bit = arr[start + j] ? 1 : 0;
                cur |= bit << j;
            }
            res[pos++] = cur;
        }
        return res;
    }

    public static String makeOctal(int val) {
        if (val <= 255) {
            return "\\" + Integer.toOctalString(val);
        }
        return UnicodeUtils.escapeUnicode(val);
    }

    public void generate() throws IOException {
        nameAndId();
        skipList();
        if (target == Lang.JAVA) {
            new JavaLexer().gen(this);
        }
        else if (target == Lang.CPP) {
            new CppLexer().gen(this);
        }
    }


    private void skipList() {
        skipList = new boolean[idMap.lastTokenId + 1];
        for (int id = 1; id <= idMap.lastTokenId; id++) {
            Name tok = idMap.getName(id);
            if (tree.getToken(tok.name).isSkip) {
                skipList[id] = true;
            }
        }
    }

    private void nameAndId() {
        //generate name and id list
        idMap.genSymbolIds(dfa.tree);

        idArr = new int[dfa.lastState + 1];//state->id
        for (var state : dfa.it()) {
            //make id for token
            var names = state.names;
            if (!names.isEmpty() && state.accepting) {
                //!dfa.isSkip[state]
                if (names.size() != 1) {
                    throw new RuntimeException("only one token per state");
                }
                idArr[state.id] = idMap.getId(new Name(names.get(0), true));
            }
        }
        //sort tokens by id
        tokens = new TreeSet<>(Map.Entry.comparingByValue());
        for (var entry : idMap.map.entrySet()) {
            if (entry.getKey().isToken) {
                tokens.add(entry);
            }
        }
    }

}
