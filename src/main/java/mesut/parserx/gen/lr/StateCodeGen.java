package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.Name;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class StateCodeGen {
    public Options options;
    public LexerGenerator lexerGenerator;
    LrDFA<?> dfa;
    LRTableGen gen;
    CodeWriter writer = new CodeWriter(true);
    HashMap<Name, Integer> ids = new HashMap<>();

    public StateCodeGen(LrDFA<?> dfa, LRTableGen tableGen) {
        this.dfa = dfa;
        this.gen = tableGen;
        options = tableGen.tree.options;
    }

    public void gen() throws IOException {
        genIds();
        writer.append("public class Parser{");
        for (LrItemSet set : dfa.itemSets) {
            gen(set);
        }
        writer.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(writer.get(), file);
    }

    //symbol ids
    void genIds() {
        int id = lexerGenerator.idMap.size() + 1;
        for (Name rule : dfa.rules) {
            ids.put(rule, id++);
        }
        ids.put(gen.start.ref(), id);
    }

    int getId(Name name) {
        if (name.isToken) {
            if (name.name.equals("$")) {
                return lexerGenerator.idMap.get("EOF");
            }
            return lexerGenerator.idMap.get(name.name);
        }
        return ids.get(name);
    }

    String getName(LrItemSet set) {
        int id = dfa.getId(set);
        return "state" + id;
    }

    String symName(Name name) {
        if (name.name.equals("$")) {
            return "EOF";
        }
        return name.name;
    }

    private void gen(LrItemSet set) {
        writer.append("public void " + getName(set) + "(){");
        if (!dfa.getTrans(set).isEmpty()) {
            //shifts
            writer.append("switch(la.type){");
            for (LrTransition tr : dfa.getTrans(set)) {
                if (tr.symbol.isToken) {
                    writer.append("case sym.%s:{", symName(tr.symbol));
                    writer.append("stack.push(la);");
                    writer.append("la = next();");
                    writer.append(getName(tr.to) + "();");
                    writer.append("break;");
                    writer.append("}");
                }
                else {
                    //goto
                }
            }
            writer.append("}");
        }
        //reduces
        if (!set.getReduce().isEmpty()) {
            writer.append("switch(la.type){");
            for (LrItem item : set.getReduce()) {
                for (Name la : item.lookAhead) {
                    writer.append("case sym.%s:", symName(la));
                }
                writer.append("{");
                writer.append("Symbol tmp = new Symbol(%d);", getId(item.rule.ref()));
                writer.append("tmp.name = \"%s\";", item.rule.name);
                writer.append("for(int i = 0;i < %d;i++){", item.dotPos);
                writer.append("tmp.children.set(%d - i,stack.pop());", item.dotPos);
                writer.append("}");//for
                writer.append("stack.push(tmp);");
                //goto
                writer.append("%s();", getName(getGoto(set, item.rule.ref())));
                writer.append("break;");
                writer.append("}");//
            }
            writer.append("}");//switch
        }
        writer.append("}");//func
    }

    LrItemSet getGoto(LrItemSet from, Name symbol) {
        for (LrTransition tr : dfa.getTrans(from)) {
            if (tr.symbol.equals(symbol)) {
                return tr.to;
            }
        }
        return null;
    }
}
