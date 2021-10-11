package mesut.parserx.gen;

import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class VisitorGenerator {
    Tree tree;
    Options options;
    CodeWriter writer;

    public VisitorGenerator(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
        writer = new CodeWriter(true);
    }

    String ruleType(RuleDecl decl) {
        return options.astClass + "." + decl.baseName();
    }

    public void generate() throws IOException {
        //genInterface();
        genImpl();
    }

    void genInterface() throws IOException {
        String className = options.parserClass + "Visitor";
        if (options.packageName != null) {
            writer.append("package %s;", options.packageName);
            writer.append("");
        }
        writer.append("public interface %s<R,P>{", className);
        writer.append("");
        for (RuleDecl decl : tree.rules) {
            writer.append("R visit%s(%s node, P p);", Utils.camel(decl.baseName()), ruleType(decl));
        }
        writer.append("}");

        File file = new File(options.outDir, className + ".java");
        Utils.write(writer.get(), file);
    }

    void genImpl() throws IOException {
        String className = options.parserClass + "Visitor";
        if (options.packageName != null) {
            writer.append("package %s;", options.packageName);
            writer.append("");
        }
        writer.append("public class %s<R,P>{", className);
        writer.append("");
        for (RuleDecl decl : tree.rules) {
            writer.append("public R visit%s(%s node, P p){", Utils.camel(decl.baseName()), ruleType(decl));
            writer.append("return null;");
            writer.append("}");
            writer.append("");
        }
        writer.append("}");

        File file = new File(options.outDir, className + ".java");
        Utils.write(writer.get(), file);
    }
}
