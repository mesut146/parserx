package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.gen.ast.AstGen;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class ParserGen {

    public static void genCC(Tree tree, Lang target) throws IOException {
        tree.prepare();
        //LexerGenerator.gen(tree, target);
        new Normalizer(tree).normalize();
        AstGen.gen(tree, target);
        if (target == Lang.JAVA) {
            new CcStateGenJava(tree).gen();
        } else {
            throw new RuntimeException("not yet");
        }
    }

    public static void writeTS(Options options) throws IOException {
        File file = new File(options.outDir, "TokenStream.java");
        var temp = new Template("token_stream.java.template");
        if (options.packageName == null) {
            temp.set("package", "");
        } else {
            temp.set("package", "package " + options.packageName + ";");
        }
        temp.set("lexer_class", options.lexerClass);
        temp.set("lexer_function", options.lexerFunction);
        temp.set("token_class", options.tokenClass);
        Utils.write(temp.toString(), file);
    }

    public static void writeRest(Tree tree, LLDfaBuilder builder, CodeWriter w) {
        for (var decl : tree.rules) {
            if (builder.rules.containsKey(decl.ref)) continue;
            w.append("public %s %s throws IOException{", decl.retType, ruleHeader(decl));
            if (decl.recInfo != null && (decl.recInfo.isState || decl.recInfo.isRec)) {
                w.append("%s res = null;", decl.retType);
            } else {
                w.append("%s res = new %s();", decl.retType, decl.retType);
            }
            var nw = new NormalWriter(w, tree);
            nw.curRule = decl;
            decl.rhs.accept(nw, null);
            w.append("return res;");
            w.append("}");

        }
    }

    public static String ruleHeader(RuleDecl decl) {
        StringBuilder sb = new StringBuilder();
        sb.append(decl.ref.name);
        sb.append("(");
        if (!decl.parameterList.isEmpty()) {
            for (int i = 0; i < decl.parameterList.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(decl.parameterList.get(i).toString());
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
