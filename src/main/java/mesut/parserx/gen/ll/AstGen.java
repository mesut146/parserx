package mesut.parserx.gen.ll;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//generate ast file and astinfo per node
public class AstGen {
    Tree tree;
    CodeWriter astWriter = new CodeWriter(true);
    CodeWriter classes;
    Options options;
    //class name -> map of node -> count
    Map<String, Map<String, Integer>> varCount = new HashMap<>();
    int groupCount;
    String curRule;

    public AstGen(Tree tree, Options options) {
        this.tree = tree;
        this.options = options;
    }

    void genAst() throws IOException {
        if (options.packageName != null) {
            astWriter.append("package " + options.packageName + ";");
            astWriter.append("");
        }
        astWriter.append("import java.util.List;");
        astWriter.append("import java.util.ArrayList;");
        astWriter.append("");

        astWriter.append(String.format("public class %s{", options.astClass));
        for (RuleDecl decl : tree.rules) {
            groupCount = 1;
            curRule = decl.name;
            model(decl);
        }
        astWriter.append("}");

        File file = new File(options.outDir, options.astClass + ".java");
        Utils.write(astWriter.get(), file);
        System.out.println("writing " + file);
    }

    void model(RuleDecl decl) {
        classes = new CodeWriter(true);
        astWriter.append(String.format("public static class %s{", decl.name));
        model(decl.rhs, decl.name, "res", astWriter);
        astWriter.all(classes.get());
        astWriter.append("}");
    }

    private void model(Node node, String parentClass, String outerVar, CodeWriter parent) {
        node.astInfo.outerVar = outerVar;
        node.astInfo.outerCls = parentClass;
        if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                model(ch, parentClass, outerVar, parent);
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            String vname = vName(name, parentClass);
            if (name.astInfo.varName == null) {
                name.astInfo.varName = vname;
            }
            if (name.isToken) {
                parent.append(String.format("public Token %s;", vname));
            }
            else {
                parent.append(String.format("public %s %s;", name, vname));
            }
        }
        else if (node.isOr()) {
            parent.append("public int which;");
            int num = 1;
            for (final Node ch : node.asOr()) {
                if (ch.isEpsilon()) continue;
                if (LLRec.isSimple(ch)) {
                    //todo vname
                    //ch.astInfo.varName = parentClass.toLowerCase() + num;
                    model(ch, parentClass, outerVar, parent);
                }
                else {
                    String clsName = Utils.camel(parentClass) + num;
                    String v = parentClass.toLowerCase() + num;
                    parent.append(String.format("%s %s%d;", clsName, parentClass.toLowerCase(), num));

                    CodeWriter c = new CodeWriter(true);
                    c.append(String.format("public static class %s{", clsName));
                    model(ch, clsName, v, c);
                    c.append("}");
                    classes.all(c.get());
                    ch.astInfo.varName = v;
                }
                num++;
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            Node ch = regex.node;
            if (regex.isOptional()) {
                model(ch, parentClass, outerVar, parent);
            }
            else {
                if (ch.isName()) {
                    String vname = ch.astInfo.varName;
                    if (vname == null) {
                        vname = vName(ch.asName(), parentClass);
                        ch.astInfo.varName = vname;
                    }
                    ch.astInfo.outerCls = parentClass;
                    ch.astInfo.outerVar = outerVar;
                    String type = ch.asName().isToken ? "Token" : ch.asName().name;
                    parent.append(String.format("public List<%s> %s = new ArrayList<>();", type, vname));
                }
                else {
                    //group
                    String var = "g" + groupCount++;
                    String cls = curRule + var;
                    parent.append(String.format("public List<%s> %s = new ArrayList<>();", cls, var));
                    CodeWriter c = new CodeWriter(true);
                    c.append("public static class " + cls + "{");
                    model(ch.asGroup().node, cls, var, c);
                    c.append("}");
                    classes.all(c.get());
                }
            }
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            String var = "g" + groupCount++;
            String cls = curRule + var;
            parent.append(String.format("public %s %s;", cls, var));

            CodeWriter c = new CodeWriter(true);
            c.append("public static class " + cls + "{");
            model(group.node, cls, var, c);
            c.append("}");
            classes.all(c.get());
        }
        else if (!node.isEpsilon()) {
            throw new RuntimeException("invalid node: " + node.getClass());
        }
    }

    //make incremental variable name with class scoped
    public String vName(Name name, String cls) {
        Map<String, Integer> map = varCount.get(cls);
        if (map == null) {
            map = new HashMap<>();
            varCount.put(cls, map);
        }
        Integer i = map.get(name.name);
        if (i == null) {
            i = 1;
        }
        map.put(name.name, i + 1);
        if (i == 1) {
            return name.name;
        }
        else {
            return name.name + i;
        }
    }
}
