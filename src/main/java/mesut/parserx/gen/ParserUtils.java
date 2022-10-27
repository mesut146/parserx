package mesut.parserx.gen;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ParserUtils {
    public static String tokens = "Tokens";
    public static Name dollar = new Name("$", true);//eof

    public static void genTokenType(Tree tree) throws IOException {
        var options = tree.options;
        var c = new CodeWriter(true);
        if (options.packageName != null) {
            c.append("package %s;", options.packageName);
            c.append("");
        }
        c.append("public class %s{", tokens);
        c.append("public static final int EOF = 0;");
        int id = 1;
        Set<String> done = new HashSet<>();
        for (var decl : tree.getTokens()) {
            if (decl.fragment) continue;
            if (done.contains(decl.name)) continue;
            c.append("public static final int %s = %d;", decl.name, id);
            done.add(decl.name);
            id++;
        }
        c.append("}");
        var file = new File(options.outDir, tokens + ".java");
        Utils.write(c.get(), file);
    }

    public static String loopExpr(Set<Name> set, String expr) {
        var sb = new StringBuilder();
        for (var it = set.iterator(); it.hasNext(); ) {
            var tok = it.next();
            sb.append(String.format("%s == %s.%s", expr, tokens, tok.name.equals("$") ? "EOF" : tok.name));
            if (it.hasNext()) {
                sb.append(" || ");
            }
        }
        return sb.toString();
    }

    public static String loopExpr(Set<Name> set) {
        return loopExpr(set, peekExpr());
    }

    static String peekExpr() {
        return "la.type";
    }
}
