package mesut.parserx.gen.ast;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.lldfa.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class CppAst {
    public Tree tree;
    CodeWriter astWriter = new CodeWriter(true);
    CodeWriter classes;
    Options options;
    //class name -> map of node -> count
    CountingMap2<String, String> varCount = new CountingMap2<>();
    String curRule;
    CodeWriter sourceWriter = new CodeWriter(true);

    public CppAst(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    void source() throws IOException {
        sourceWriter.append("#include \"%s.h\"", options.astClass);
        sourceWriter.append("");
        for (RuleDecl decl : tree.rules) {
            curRule = decl.baseName();
            sourceWriter.append("%s::%s::%s() = default;", options.astClass, decl.baseName(), decl.baseName());
            writePrinter(decl.baseName(), decl.rhs, sourceWriter, false);

            if (decl.rhs.isOr()) {
                int id = 1;
                for (Node ch : decl.rhs.asOr()) {
                    String alt = decl.baseName() + id;
                    sourceWriter.append("%s::%s::%s::%s() = default;", options.astClass, decl.baseName(), alt, alt);
                    writePrinter(alt, ch, sourceWriter, true);

                    id++;
                }
            }
        }
        File file = new File(options.outDir, options.astClass + ".cpp");
        Utils.write(sourceWriter.get(), file);
    }

    public void genAst() throws IOException {
        //ns
        astWriter.append("#pragma once");
        astWriter.append("");
        astWriter.append("#include <vector>");
        astWriter.append("#include <string>");
        astWriter.append("#include <sstream>");
        astWriter.append("#include \"%s.h\"", options.tokenClass);
        astWriter.append("");

        astWriter.append("class %s{", options.astClass);
        astWriter.append("public:");
        //forwards
        for (RuleDecl decl : tree.rules) {
            astWriter.append("class %s;", decl.baseName());
        }
        astWriter.append("");

        for (RuleDecl decl : tree.rules) {
            curRule = decl.baseName();
            model(decl);
        }
        astWriter.down();
        astWriter.append("};");

        File file = new File(options.outDir, options.astClass + ".h");
        Utils.write(astWriter.get(), file);
        varCount.clear();

        source();
    }

    void model(RuleDecl decl) {
        classes = new CodeWriter(true);
        astWriter.append("class %s{", decl.baseName());
        astWriter.append("public:");
        astWriter.append("%s();", decl.baseName());//ctor
        if (decl.rhs.isOr()) {
            //forward alts
            int id = 1;
            for (Node ch : decl.rhs.asOr()) {
                astWriter.append("class %s;", decl.baseName() + id);
                id++;
            }
            astWriter.append("");
        }
        Type type = new Type(options.astClass, decl.baseName());
        model(decl.rhs, type, "res", astWriter);
        astWriter.all(classes.get());
        //todo create parent
        astWriter.append("std::string toString();");
        astWriter.down();
        astWriter.append("};");
    }

    void writePrinter(String rule, Node rhs, CodeWriter c, boolean isAlt) {
        if (isAlt) {
            c.append("std::string %s::%s::%s::toString(){", options.astClass, curRule, rule);
            c.append("std::stringstream sb;");
            getPrint(rhs, c);
            c.append("return sb.str();");
        }
        else {
            c.append("std::string %s::%s::toString(){", options.astClass, rule);
            c.append("std::stringstream sb;");
            c.append("sb << \"%s{\";", rule);
            getPrint(rhs, c);
            c.append("sb << \"}\";");
            c.append("return sb.str();");
        }
        c.append("}");//toString
    }

    void getPrint(Node node, CodeWriter c) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                c.append("sb << \"'\" << %s->value << \"'\";", node.astInfo.varName);
            }
            else {
                c.append("sb << %s->toString();", node.astInfo.varName);
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                if (i > 0) {
                    Node prev = s.get(i - 1);
                    if (prev.isOptional()) {
                        c.append("if(%s != null) sb << \"%s\";", prev.astInfo.varName, options.sequenceDelimiter);
                    }
                    else if (prev.isStar()) {
                        c.append("if(!%s.empty()) sb << \"%s\";", prev.asRegex().node.astInfo.varName, options.sequenceDelimiter);
                    }
                    else {
                        c.append("sb << \"%s\";", options.sequenceDelimiter);
                    }
                }
                getPrint(s.get(i), c);
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            String v = regex.node.astInfo.varName;
            if (regex.isStar() || regex.isPlus()) {
                c.append("if(!%s.empty()){", v);
                c.append("sb << \"[\";");
                c.append("for(int i = 0;i < %s.size();i++){", v);
                if (regex.node.asName().isToken) {
                    c.append("sb << \"'\" << %s.at(i)->value << \"'\";", v);
                }
                else {
                    c.append("sb << %s.at(i)->toString();", v);
                }
                c.append("if(i < %s.size() - 1) sb << \",\";", v);
                c.append("}");
                c.append("sb << ']';");
                c.append("}");
            }
            else {
                if (regex.node.asName().isToken) {
                    c.append("if(%s != nullptr) sb << %s->value;", v, v);
                }
                else {
                    c.append("if(%s != nullptr) sb << %s.toString();", v, v);
                }
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
                    c.append("sb << %s->toString();", ch.astInfo.varName);
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
            Name name = node.asName();
            //check if user supplied var name
            String varName = name.astInfo.varName;
            if (varName == null) {
                varName = vName(name, outerCls.toString());
                name.astInfo.varName = varName;
            }
            parent.append("%s* %s;", name.isToken ? options.tokenClass : name.name, varName);
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
                String vname = ch.astInfo.varName;
                if (vname == null) {
                    vname = vName(name, outerCls.toString());
                    ch.astInfo.varName = vname;
                }
                ch.astInfo.isInLoop = true;
                ch.astInfo.outerVar = outerVar;
                String type = name.isToken ? options.tokenClass : name.name;
                parent.append("std::vector<%s*> %s;", type, vname);
            }
        }
        else if (node.isOr()) {
            parent.append("int which;");
            int num = 1;
            for (Node ch : node.asOr()) {
                if (ch.isEpsilon()) continue;
                Type clsName = new Type(outerCls, Utils.camel(outerCls.name) + num);
                String v = outerCls.name.toLowerCase() + num;

                //in case of factorization pre-write some code
                ch.astInfo.which = num;
                //sequence
                //complex choice point inits holder
                ch.astInfo.nodeType = clsName;
                ch.astInfo.varName = v;
                ch.astInfo.outerVar = outerVar;
                parent.append("%s* %s;", clsName.name, v);
                CodeWriter c = new CodeWriter(false);
                c.append("class %s{", clsName.name);
                c.append("public:");
                c.append("%s();", clsName.name);//ctor
                model(ch, clsName, v, c);
                c.append("std::string toString();");
                c.append("};");
                classes.all(c.get());

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
