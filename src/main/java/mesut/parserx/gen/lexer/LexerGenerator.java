package mesut.parserx.gen.lexer;

import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.nodes.ModeBlock;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

public class LexerGenerator {
    public IdMap idMap = new IdMap();
    public NFA dfa;
    public Tree tree;
    //state -> id
    public int[] idArr;
    //name -> id pairs
    public TreeSet<Map.Entry<Name, Integer>> tokens;
    //acc state -> new mode state
    public int[] mode_arr;
    //final state -> action
    public String[] actions;
    boolean hasActions = false;
    Lang target;

    public LexerGenerator(Tree tree, Lang target) {
        this.tree = tree;
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

    public static String writeIntArr(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    public static String makeOctal(int val) {
        if (val <= 255) {
            return "\\" + Integer.toOctalString(val);
        }
        return UnicodeUtils.escapeUnicode(val);
    }

    public void generate() throws IOException {
        dfa = tree.makeNFA().dfa();
        //dfa = Minimization.optimize(this.dfa);
        nameAndId();
        skipList();
        modes();
        actions();
        more();
        if (target == Lang.JAVA) {
            new JavaLexer(this).gen();
        } else if (target == Lang.CPP) {
            new CppLexer().gen(this);
        }
    }

    public int[] acc() {
        var arr = new boolean[dfa.lastState + 1];
        for (var state : dfa.it()) {
            arr[state.id] = state.accepting;
        }
        return makeIntArr(arr);
    }

    public int[] more() {
        var arr = new boolean[dfa.lastState + 1];
        for (var state : dfa.it()) {
            if (state.decl != null && state.decl.isMore) {
                arr[state.id] = true;
            }
        }
        return makeIntArr(arr);
    }

    private void actions() {
        actions = new String[dfa.lastState + 1];
        hasActions = false;
        for (var token : tree.getTokens()) {
            if (!token.rhs.isSequence()) continue;
            var seq = token.rhs.asSequence();
            var last = seq.get(seq.size() - 1);
            if (last.action == null) continue;
            for (var state : dfa.it()) {
                if (state.decl != null && state.decl == token) {
                    actions[state.id] = last.action;
                    //todo use this flag in template to reduce code size
                    hasActions = true;
                    break;
                }
            }
        }
    }

    private void modes() {
        mode_arr = new int[dfa.lastState + 1];
        for (var state : dfa.it()) {
            if (!state.accepting) continue;
            if (state.decl.mode != null) {
                mode_arr[state.id] = dfa.modes.get(state.decl.mode).id;
            } else {
                //preserve mode
                var mb = getModeBlock(state.decl);
                if (mb == null) {
                    //global token -> DEFAULT
                    mode_arr[state.id] = dfa.initialState.id;
                } else {
                    //mode token -> same mode
                    mode_arr[state.id] = dfa.modes.get(mb.name).id;
                }
            }
        }
    }

    ModeBlock getModeBlock(TokenDecl decl) {
        for (var tb : tree.tokenBlocks) {
            for (var mb : tb.modeBlocks) {
                if (mb.tokens.contains(decl)) return mb;
            }
        }
        return null;
    }

    public int[] skipList() {
        var arr = new boolean[dfa.lastState + 1];
        for (var state : dfa.it()) {
            if (state.decl != null && state.decl.isSkip) {
                arr[state.id] = true;
            }
        }
        return makeIntArr(arr);
    }

    private void nameAndId() {
        //generate name and id list
        idMap.genSymbolIds(dfa.tree);

        idArr = new int[dfa.lastState + 1];//state->id
        for (var state : dfa.it()) {
            //make id for token
            if (state.accepting) {
                idArr[state.id] = idMap.getId(new Name(state.name, true));
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
