package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//common for lexer and parser
public class IdMap {
    public static Name EOF = new Name("EOF", true);
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
        Set<String> done = new HashSet<>();
        for (var decl : tree.getTokens()) {
            if (decl.fragment) continue;
            if (done.contains(decl.name)) continue;
            map.put(decl.ref(), ++lastId);
            done.add(decl.name);
        }
        lastTokenId = lastId;

        for (var rule : tree.rules) {
            map.put(rule.ref, ++lastId);
        }
        map.put(new Name(LrDFAGen.startName, false), ++lastId);

        for (var entry : map.entrySet()) {
            id_to_name.put(entry.getValue(), entry.getKey());
        }
    }

    public void writeSym(Options options) throws IOException {
        var writer = new CodeWriter(true);

        if (options.packageName != null) {
            writer.all("package %s;\n\n", options.packageName);
        }
        writer.all("public class %s {\n", className);

        writer.append("//tokens");
        var tokens = map.entrySet()
                .stream()
                .filter(e -> e.getKey().isToken)
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        for (var entry : tokens) {
            if (entry.getKey().name.equals("$")) continue;
            writer.append(field(entry.getKey().name, entry.getValue()));

        }
        writer.append("//rules");

        var rules = map.entrySet()
                .stream()
                .filter(e -> e.getKey().isRule())
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        for (var entry : rules) {
            if (entry.getKey().name.equals(LrDFAGen.startName)) continue;
            writer.append(field(entry.getKey().name, entry.getValue()));
        }
        writer.all("\n}");

        var file = new File(options.outDir, className + ".java");
        Utils.write(writer.toString(), file);
    }

    String field(String name, int id) {
        return String.format("public static final int %s = %d;", name, id);
    }
}
