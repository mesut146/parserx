package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.AstGen;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class AstBuilderGen {
    Tree tree;
    CodeWriter writer = new CodeWriter(true);
    Options options;

    public AstBuilderGen(Tree tree) {
        this.tree = tree;
    }

    public void gen() throws IOException {
        new AstGen(tree).genAst();
        tree = EbnfToBnf.combineOr(tree);
        new Normalizer(tree).normalize();
        options = tree.options;
        if (options.packageName != null) {
            writer.append("package %s;", options.packageName);
            writer.append("");
        }
        writer.append("import java.util.List;");
        writer.append("import java.util.ArrayList;");
        writer.append("");

        writer.append("public class AstBuilder{");

        for (RuleDecl decl : tree.rules) {
            writer.append("public static %s make%s(Symbol node){", decl.retType, decl.name);
            writer.append("%s res = new %s();", decl.retType, decl.retType);
            Or or = decl.rhs.asOr();

            for (int i = 0; i < or.size(); i++) {
                if (i == 0) {
                    writer.append("if(node.index == %d){", i);
                }
                else {
                    writer.append("else if(node.index == %d){", i);
                }

                writer.append("res.which = %d;", i + 1);
                Node ch = or.get(i);
                if (ch.isSequence()) {
                    Sequence s = ch.asSequence();
                    Type type = new Type(new Type(options.astClass, decl.name), Utils.camel(decl.name) + (i + 1));
                    String v = decl.name.toLowerCase() + (i + 1);
                    writer.append("%s %s = res.%s = new %s();", type, v, v, type);
                    for (int j = 0; j < s.size(); j++) {
                        Name name = s.get(j).asName();
                        if (name.isToken) {
                            writer.append("%s.%s = node.children.get(%d).token;", v, name.astInfo.varName, j);
                        }
                        else {
                            writer.append("%s.%s = make%s(node.children.get(%d));", v, name.astInfo.varName, name.name, j);
                        }
                    }
                }
                else {
                    Name name = ch.asName();
                    if (name.isToken) {
                        writer.append("res.%s = node.children.get(0).token;", name.astInfo.varName);
                    }
                    else {
                        writer.append("res.%s = make%s(node.children.get(0));", name.astInfo.varName, name.name);
                    }
                }
                writer.append("}");
            }

            writer.append("return res;");
            writer.append("}");
        }

        writer.append("}");

        File file = new File(options.outDir, "AstBuilder.java");
        Utils.write(writer.get(), file);
    }
}
