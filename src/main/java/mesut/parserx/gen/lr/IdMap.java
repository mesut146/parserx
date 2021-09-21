package mesut.parserx.gen.lr;

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

public class IdMap {
    public static Name EOF = new Name("EOF", true);
    public static Name dollar = new Name("$", true);
    HashMap<Name, Integer> map = new HashMap<>();
    int lastId;


    public int getId(Name name) {
        if (name.isToken && name.name.equals("$")) {
            return map.get(EOF);
        }
        return map.get(name);
    }

    //symbol ids
    public void genSymbolIds(Tree tree) {
        map.put(EOF, 0);
        lastId = 0;
        for (TokenDecl decl : tree.tokens) {
            if (decl.isSkip) continue;
            map.put(decl.ref(), ++lastId);
        }

        for (RuleDecl rule : tree.rules) {
            map.put(rule.ref(), ++lastId);
        }
        map.put(new Name(LrDFAGen.startName, false), ++lastId);
    }

    public void writeSym(Options options) throws IOException {
        StringBuilder sb = new StringBuilder();

        if (options.packageName != null) {
            sb.append("package ").append(options.packageName).append(";\n\n");
        }

        sb.append("public class sym{\n");

        sb.append("  //tokens\n");
        for (Map.Entry<Name, Integer> entry : map.entrySet()) {
            if (entry.getKey().isToken) {
                if (entry.getKey().name.equals("$")) continue;
                sb.append(field(entry.getKey().name, entry.getValue()));
            }
        }
        sb.append("  //rules\n");

        for (Map.Entry<Name, Integer> entry : map.entrySet()) {
            if (entry.getKey().isRule()) {
                if (entry.getKey().name.equals(LrDFAGen.startName)) continue;
                sb.append(field(entry.getKey().name, entry.getValue()));
            }
        }
        sb.append("\n}");

        File file = new File(options.outDir, "sym.java");
        Utils.write(sb.toString(), file);
        System.out.println("writing " + file);
    }

    String field(String name, int id) {
        return String.format("  public static final int %s = %d;\n", name, id);
    }
}
