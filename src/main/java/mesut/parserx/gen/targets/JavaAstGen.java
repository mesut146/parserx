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

public class JavaAstGen{
    public Tree tree;
    CodeWriter w = new CodeWriter(true);
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
            w.append("package %s;", options.packageName);
            w.append("");
        }
        w.append("import java.util.List;");
        w.append("import java.util.ArrayList;");
        w.append("");

        w.append("public class %s{", options.astClass);
        for (RuleDecl decl : tree.rules) {
            groupCount = 1;
            curRule = decl.baseName();
            model(decl);
        }
        w.append("}");

        File file = new File(options.outDir, options.astClass + ".java");
        Utils.write(w.get(), file);
        varCount.clear();
    }

    void model(RuleDecl decl) {
        classes = new CodeWriter(true);
        w.append("public static class %s{", decl.baseName());
        model(decl.rhs, decl.retType, "res", w);
        w.all(classes.get());
        if (tree.isOriginal(decl.ref) && options.genVisitor) {
            w.append("public <R,P> R accept(%sVisitor<R,P> visitor, P arg){", options.parserClass);
            w.all("return visitor.visit%s(this, arg);\n}", Utils.camel(decl.baseName()));
        }
        //todo create parent
        writePrinter(decl.rhs, w, false);
        w.append("}");
    }

    void writePrinter(Node rhs, CodeWriter c, boolean isAlt) {
        c.append("public String toString(){");
        if (isAlt) {
            c.append("StringBuilder sb = new StringBuilder();");
            getPrint(rhs, c);
            c.append("return sb.toString();");
        }
        else {
            if (rhs.isOr()) {
                c.append("StringBuilder sb = new StringBuilder(\"%s#\" + which + \"{\");", curRule);
            }
            else {
                c.append("StringBuilder sb = new StringBuilder(\"%s{\");", curRule);
            }
            getPrint(rhs, c);
            c.append("return sb.append(\"}\").toString();");
        }
        c.append("}");//toString
    }

    String printer(Name name) {
        if (name.isToken) {
            return String.format("\"'\" + %s.value + \"'\"", name.astInfo.varName);
        }
        else {
            return String.format("%s.toString()", name.astInfo.varName);
        }
    }

    void getPrint(Node node, CodeWriter c) {
        if (node.isName()) {
            c.append("sb.append(%s);", printer(node.asName()));
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                getPrint(s.get(i), c);
                if (i < s.size() - 1) {
                    Node next = s.get(i + 1);
                    if (next.isOptional()) {
                        c.append("if(%s != null) sb.append(\"%s\");", next.astInfo.varName, options.sequenceDelimiter);
                    }
                    else if (next.isStar()) {
                        c.append("if(!%s.isEmpty()) sb.append(\"%s\");", next.asRegex().node.astInfo.varName, options.sequenceDelimiter);
                    }
                    else {
                        c.append("sb.append(\"%s\");", options.sequenceDelimiter);
                    }
                }
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            String v = regex.node.astInfo.varName;
            Name name = regex.node.asName();
            if (regex.isStar() || regex.isPlus()) {
                c.append("if(!%s.isEmpty()){", v);
                c.append("sb.append('[');");
                c.append("for(int i = 0;i < %s.size();i++){", v);

                if (name.isToken) {
                    c.append("sb.append(\"'\" + %s.get(i).value + \"'\");", v);
                }
                else {
                    c.append("sb.append(%s.get(i).toString());", v);
                }
                c.append("if(i < %s.size() - 1) sb.append(\",\");", v);
                c.append("}");
                c.append("sb.append(']');");
                c.append("}");
            }
            else {
                c.append("sb.append(%s == null?\"\":%s);", v, printer(name));
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
    }

    void setVarName(Name name, Type outerCls) {
        String varName = name.astInfo.varName;
        if (varName == null) {
            varName = vName(name, outerCls.toString());
            name.astInfo.varName = varName;
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
            Name name = node.asName();
            //check if user supplied var name
            setVarName(name, outerCls);
            parent.append("public %s %s;", name.isToken ? options.tokenClass : name.name, name.astInfo.varName);
        }
        else if (node.isRegex()) {
            node.astInfo.outerVar = outerVar;
            Regex regex = node.asRegex();
            Node ch = regex.node;
            if (regex.isOptional()) {
                model(ch, outerCls, outerVar, parent);
            }
            else {
                Name name = ch.asName();
                setVarName(name, outerCls);
                ch.astInfo.isInLoop = true;
                ch.astInfo.outerVar = outerVar;
                String type = name.isToken ? options.tokenClass : name.name;
                parent.append("public List<%s> %s = new ArrayList<>();", type, name.astInfo.varName);
            }
            node.astInfo.varName = ch.astInfo.varName;
        }
        else if (node.isOr()) {
            parent.append("public int which;");
            int id = 1;
            for (Node ch : node.asOr()) {
                if (ch.isEpsilon()) continue;

                //in case of factorization pre-write some code
                ch.astInfo.which = id;
                if (options.useSimple && RecDescent.isSimple(ch)) {
                    model(ch, outerCls, outerVar, parent);
                }
                else {
                    //sequence
                    //complex choice point inits holder
                    Type clsName = new Type(outerCls, Utils.camel(outerCls.name) + id);
                    String v = outerCls.name.toLowerCase() + id;
                    ch.astInfo.nodeType = clsName;
                    ch.astInfo.varName = v;
                    ch.astInfo.outerVar = outerVar;
                    ch.astInfo.assignOuter = true;
                    parent.append("%s %s;", clsName.name, v);
                    CodeWriter c = new CodeWriter(false);
                    c.append("public static class %s{", clsName.name);
                    model(ch, clsName, v, c);
                    writePrinter(ch, c, true);
                    c.append("}");
                    classes.all(c.get());
                }
                id++;
            }
        }
    }

    //make incremental variable name with class scoped
    public String vName(Name name, String cls) {
        int i = varCount.get(cls, name.name);
        return i == 1 ? name.name : name.name + i;
    }
}