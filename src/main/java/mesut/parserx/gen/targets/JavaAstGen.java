package mesut.parserx.gen.targets;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class JavaAstGen {
    public Tree tree;
    CodeWriter astWriter = new CodeWriter(true);
    CodeWriter classes;
    Options options;
    //class name -> map of node -> count
    CountingMap2<String, String> varCount = new CountingMap2<>();
    int groupCount;
    String curRule;

    public JavaAstGen(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public void genAst() throws IOException {
        new Normalizer(tree).normalize();
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
            astWriter.append("public <R,P> R accept(%sVisitor<R,P> visitor, P arg){", options.parserClass);
            astWriter.all("return visitor.visit%s(this, arg);\n}", Utils.camel(decl.baseName()));
        }
        //todo create parent
        writePrinter(decl.rhs, astWriter, false);
        astWriter.append("}");
    }

    void writePrinter(Node rhs, CodeWriter c, boolean isAlt) {
        c.append("public String toString(){");
        if (isAlt) {
            c.append("StringBuilder sb=new StringBuilder();");
            getPrint(rhs, c);
            c.append("return sb.toString();");
        }
        else {
            c.append("StringBuilder sb=new StringBuilder(\"%s{\");", curRule);
            getPrint(rhs, c);
            c.append("return sb.append(\"}\").toString();");
        }
        c.append("}");//toString
    }

    void getPrint(Node node, CodeWriter c) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                c.append("sb.append(%s.value);", node.astInfo.varName);
            }
            else {
                c.append("sb.append(%s.toString());", node.astInfo.varName);
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                getPrint(s.get(i), c);
                //c.append("sb.append(\" \");");
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            String v = regex.node.astInfo.varName;
            if (regex.isStar() || regex.isPlus()) {
                c.append("if(!%s.isEmpty()){", v);
                c.append("sb.append('[');");
                c.append("for(int i = 0;i < %s.size();i++){", v);
                if (regex.node.asName().isToken) {
                    c.append("sb.append(%s.get(i).value);", v);
                }
                else {
                    c.append("sb.append(%s.get(i));", v);
                }
                c.append("if(i < %s.size() - 1) sb.append(\",\");", v);
                c.append("}");
                c.append("sb.append(']');");
                c.append("}");
            }
            else {
                c.append("sb.append(%s == null?\"\":%s);", v, v);
            }
        }
        else if (node.isGroup()) {
            getPrint(node.asGroup().node, c);
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            for (int i = 0; i < or.size(); i++) {
                if (i == 0) {
                    c.append("if(which == 1){");
                }
                else {
                    c.append("else if(which == %d){", i + 1);
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
            throw new RuntimeException("invalid child");
        }
    }

    private void model(Node node, Type outerCls, String outerVar, CodeWriter parent) {
        if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                model(ch, outerCls, outerVar, parent);
            }
        }
        else if (node.isName()) {
            node.astInfo.outerVar = outerVar;
            node.astInfo.outerCls = outerCls;
            Name name = node.asName();
            //check if user supplied var name
            String varName = name.astInfo.varName;
            if (varName == null) {
                varName = vName(name, outerCls.toString());
                name.astInfo.varName = varName;
            }
            parent.append("public %s %s;", name.isToken ? options.tokenClass : name.name, varName);
        }
        else if (node.isRegex()) {
            node.astInfo.outerVar = outerVar;
            node.astInfo.outerCls = outerCls;
            Regex regex = node.asRegex();
            Node ch = regex.node;
            if (regex.isOptional()) {
                model(ch, outerCls, outerVar, parent);
            }
            else {
                Name name = ch.asName();
                String vname = ch.astInfo.varName;
                if (vname == null) {
                    vname = vName(name, outerCls.toString());
                    ch.astInfo.varName = vname;
                }
                ch.astInfo.isInLoop = true;
                ch.astInfo.outerCls = outerCls;
                ch.astInfo.outerVar = outerVar;
                String type = name.isToken ? options.tokenClass : name.name;
                parent.append("public List<%s> %s = new ArrayList<>();", type, vname);
            }
        }
        else if (node.isOr()) {
            parent.append("public int which;");
            int num = 1;
            for (Node ch : node.asOr()) {
                if (ch.isEpsilon()) continue;
                Type clsName = new Type(outerCls, Utils.camel(outerCls.name) + num);
                String v = outerCls.name.toLowerCase() + num;

                //in case of factorization pre-write some code
                ch.astInfo.which = num;
                if (RecDescent.isSimple(ch)) {
                    //todo vname
                    //ch.astInfo.varName = parentClass.toLowerCase() + num;
                    model(ch, outerCls, outerVar, parent);
                }
                else {
                    //sequence
                    //complex choice point inits holder
                    ch.astInfo.createNode = true;
                    ch.astInfo.nodeType = clsName;
                    ch.astInfo.varName = v;
                    ch.astInfo.outerVar = outerVar;
                    ch.astInfo.outerCls = outerCls;
                    ch.astInfo.assign = true;
                    parent.append(String.format("%s %s;", clsName.name, v));
                    CodeWriter c = new CodeWriter(false);
                    c.append("public static class %s{", clsName.name);
                    model(ch, clsName, v, c);
                    writePrinter(ch, c, true);
                    c.append("}");
                    classes.all(c.get());
                }
                num++;
            }
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
