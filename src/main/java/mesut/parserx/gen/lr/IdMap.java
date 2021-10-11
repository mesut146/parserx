package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//common for lexer and parser
public class IdMap {
    public static Name EOF = new Name("EOF", true);
    public static Name dollar = new Name("$", true);
    public static String className = "Symbols";
    public HashMap<Name, Integer> map = new HashMap<>();
    public HashMap<Integer, Name> id_to_name = new HashMap<>();
    public int lastId;
    public int lastTokenId;

    public int getId(Name name) {
        if (name.isToken && name.name.equals("$")) {
            return map.get(EOF);
        }
        return map.get(name);
    }

    public Name getName(int id) {
        return id_to_name.get(id);
    }

    //symbol ids
    public void genSymbolIds(Tree tree) {
        map.put(EOF, 0);
        lastId = 0;
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            //if (decl.isSkip) continue;
            map.put(decl.ref(), ++lastId);
        }
        lastTokenId = lastId;

        for (RuleDecl rule : tree.rules) {
            map.put(rule.ref, ++lastId);
        }
        map.put(new Name(LrDFAGen.startName, false), ++lastId);

        for (Map.Entry<Name, Integer> entry : map.entrySet()) {
            id_to_name.put(entry.getValue(), entry.getKey());
        }
    }

    public void writeSym(Options options) throws IOException {
        CodeWriter writer = new CodeWriter(true);

        if (options.packageName != null) {
            writer.all("package %s;\n\n", options.packageName);
        }
        writer.all("public class %s {\n", className);

        writer.append("//tokens");
        for (Map.Entry<Name, Integer> entry : map.entrySet()) {
            if (entry.getKey().isToken) {
                if (entry.getKey().name.equals("$")) continue;
                writer.append(field(entry.getKey().name, entry.getValue()));
            }
        }
        writer.append("//rules");

        for (Map.Entry<Name, Integer> entry : map.entrySet()) {
            if (entry.getKey().isRule()) {
                if (entry.getKey().name.equals(LrDFAGen.startName)) continue;
                writer.append(field(entry.getKey().name, entry.getValue()));
            }
        }
        writer.all("\n}");

        File file = new File(options.outDir, className + ".java");
        Utils.write(writer.toString(), file);
    }

    String field(String name, int id) {
        return String.format("public static final int %s = %d;", name, id);
    }
}
