package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.AstGen;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

//simple Node to Ast node converter builder
public class AstBuilderGen {
    Tree tree;
    CodeWriter writer = new CodeWriter(true);
    Options options;

    public AstBuilderGen(Tree tree) {
        this.tree = tree;
    }

    public void gen() throws IOException {
        tree = EbnfToBnf.combineOr(tree);
        AstGen.gen(tree, "java");
        options = tree.options;
        if (options.packageName != null) {
            writer.append("package %s;", options.packageName);
            writer.append("");
        }
        writer.append("import java.util.List;");
        writer.append("import java.util.ArrayList;");
        writer.append("");

        writer.append("public class AstBuilder{");

        for (var decl : tree.rules) {
            writer.append("public static %s make%s(Symbol node){", decl.retType, decl.baseName());
            writer.append("%s res = new %s();", decl.retType, decl.retType);
            var or = decl.rhs.asOr();

            for (int i = 0; i < or.size(); i++) {
                if (i == 0) {
                    writer.append("if(node.index == %d){", i);
                }
                else {
                    writer.append("else if(node.index == %d){", i);
                }

                writer.append("res.which = %d;", i + 1);
                var ch = or.get(i);
                if (ch.isSequence()) {
                    var s = ch.asSequence();
                    var type = new Type(new Type(options.astClass, decl.baseName()), Utils.camel(decl.baseName()) + (i + 1));
                    var v = decl.baseName().toLowerCase() + (i + 1);
                    writer.append("%s %s = res.%s = new %s();", type, v, v, type);
                    for (int j = 0; j < s.size(); j++) {
                        var name = s.get(j).asName();
                        if (name.isToken) {
                            writer.append("%s.%s = node.children.get(%d).token;", v, name.astInfo.varName, j);
                        }
                        else {
                            writer.append("%s.%s = make%s(node.children.get(%d));", v, name.astInfo.varName, name.name, j);
                        }
                    }
                }
                else {
                    var name = ch.asName();
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

        var file = new File(options.outDir, "AstBuilder.java");
        Utils.write(writer.get(), file);
    }
}
