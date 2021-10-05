package mesut.parserx.gen.ll;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

//generate ast file and astinfo per node
public class AstGen {
    public Tree tree;
    CodeWriter astWriter = new CodeWriter(true);
    CodeWriter classes;
    Options options;
    //class name -> map of node -> count
    CountingMap2<String, String> varCount = new CountingMap2<>();
    int groupCount;
    String curRule;

    public AstGen(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public void genAst() throws IOException {
        if (options.packageName != null) {
            astWriter.append("package %s;", options.packageName);
            astWriter.append("");
        }
        astWriter.append("import java.util.List;");
        astWriter.append("import java.util.ArrayList;");
        astWriter.append("");

        astWriter.append("public class %s{", options.astClass);
        for (RuleDecl decl : tree.rules) {
            groupCount = 1;
            curRule = decl.baseName();
            model(decl);
        }
        astWriter.append("}");

        File file = new File(options.outDir, options.astClass + ".java");
        Utils.write(astWriter.get(), file);
        varCount.clear();
    }

    void model(RuleDecl decl) {
        classes = new CodeWriter(true);
        astWriter.append("public static class %s{", decl.baseName());
        Type type = new Type(options.astClass, decl.baseName());
        model(decl.rhs, type, "res", astWriter);
        astWriter.all(classes.get());
        if (decl.isOriginal && options.genVisitor) {
            astWriter.append(String.format("public <R,P> R accept(%sVisitor<R,P> visitor, P arg){", options.parserClass));
            astWriter.all(String.format("return visitor.visit%s(this, arg);\n}", Utils.camel(decl.baseName())));
        }
        writePrinter(decl.rhs, astWriter);
        astWriter.append("}");
    }

    void writePrinter(Node rhs, CodeWriter c) {
        c.append("public String toString(){");
        c.append("StringBuilder sb=new StringBuilder();");
        getPrint(rhs, c);
        c.append("return sb.toString();");
        c.append("}");//toString
    }

    void getPrint(Node node, CodeWriter c) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                c.append("sb.append(" + node.astInfo.varName + ".value);");
            }
            else {
                c.append("sb.append(" + node.astInfo.varName + ".toString());");
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                getPrint(s.get(i), c);
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            String v = regex.node.astInfo.varName;
            if (regex.isStar() || regex.isPlus()) {
                c.append("sb.append('{');");
                c.append("for(int i=0;i<%s.size();i++){", v);
                c.append("sb.append(%s.get(i));", v);
                c.append("}");
                c.append("sb.append('}');");
            }
            else {
                c.append("sb.append(%s);", v + "==null?\"\":" + v);
            }
        }
        else if (node.isGroup()) {
            getPrint(node.asGroup().node, c);
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            for (int i = 0; i < or.size(); i++) {
                if (i == 0) {
                    c.append("if(which==1){");
                }
                else {
                    c.append("else if(which==%d){", i + 1);
                }
                Node ch = or.get(i);
                if (ch.isName()) {
                    getPrint(ch, c);
                }
                else {
                    c.append("sb.append(%s);", ch.astInfo.varName);
                }

                c.append("}");//if
            }
        }
        else {
            throw new RuntimeException();
        }
    }

    private void model(Node node, Type parentClass, String outerVar, CodeWriter parent) {
        node.astInfo.outerVar = outerVar;
        node.astInfo.outerCls = parentClass;
        if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                model(ch, parentClass, outerVar, parent);
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            String vname = name.astInfo.varName;
            if (vname == null) {
                vname = vName(name, parentClass.toString());
                name.astInfo.varName = vname;
            }
            if (name.isToken) {
                parent.append(String.format("public Token %s;", vname));
            }
            else {
                if (tree.getRule(name) != null) {
                    name.astInfo.type = new Type(options.astClass, name.name);
                }
                else {
                    //or child not visible
                    //name.astInfo.type = new Type();
                }
                parent.append(String.format("public %s %s;", name.name, vname));
            }
        }
        else if (node.isOr()) {
            parent.append("public int which;");
            int num = 1;
            for (Node ch : node.asOr()) {
                if (ch.isEpsilon()) continue;
                Type clsName = new Type(parentClass, Utils.camel(parentClass.name) + num);
                String v = parentClass.name.toLowerCase() + num;

                //in case of factorization pre-write some code
                String code = String.format("%s.which = %d;\n", outerVar, num);
                if (!RecDescent.isSimple(ch)) {
                    code += String.format("%s %s = %s.%s = new %s();", clsName, v, outerVar, v, clsName);
                }
                ch.astInfo.code = code;

                if (RecDescent.isSimple(ch)) {
                    //todo vname
                    //ch.astInfo.varName = parentClass.toLowerCase() + num;
                    model(ch, parentClass, outerVar, parent);
                }
                else {
                    parent.append(String.format("%s %s;", clsName.name, v));

                    CodeWriter c = new CodeWriter(false);
                    c.append(String.format("public static class %s{", clsName.name));
                    model(ch, clsName, v, c);
                    writePrinter(ch, c);
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
                ch.astInfo.isInLoop = true;
                if (ch.isName()) {
                    String vname = ch.astInfo.varName;
                    if (vname == null) {
                        vname = vName(ch.asName(), parentClass.toString());
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
                    Type cls = new Type(parentClass, curRule + var);
                    parent.append(String.format("public List<%s> %s = new ArrayList<>();", cls.name, var));
                    CodeWriter c = new CodeWriter(true);
                    c.append("public static class " + cls.name + "{");
                    model(ch.asGroup().node, cls, var, c);
                    c.append("}");
                    classes.all(c.get());
                }
            }
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            String var;
            if (node.astInfo.varName != null) {
                var = node.astInfo.varName;
            }
            else {
                var = "g" + groupCount++;
            }
            Type cls = new Type(parentClass, curRule + var);
            group.astInfo.varName = var;
            parent.append(String.format("public %s %s;", cls.name, var));

            CodeWriter c = new CodeWriter(true);
            c.append("public static class " + cls.name + "{");
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
        int i = varCount.get(cls, name.name);
        if (i == 1) {
            return name.name;
        }
        return name.name + i;
    }
}
