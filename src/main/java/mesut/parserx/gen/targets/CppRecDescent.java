package mesut.parserx.gen.targets;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.Type;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static mesut.parserx.gen.ll.RDParserGen.loopLimit;
import static mesut.parserx.gen.ll.RDParserGen.tokens;

public class CppRecDescent {
    public Options options;
    Tree tree;
    CodeWriter code = new CodeWriter(true);
    RuleDecl curRule;
    int flagCount;
    int firstCount;
    CodeWriter headerWriter = new CodeWriter(true);

    public CppRecDescent(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public static boolean isSimple(Node node) {
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return regex.node.isName();
        }
        return node.isName();
    }

    public void gen() throws IOException {
        //ns
        headerWriter.append("#pragma once");
        headerWriter.append("");
        headerWriter.append("#include <string>");
        headerWriter.append("#include <vector>");
        headerWriter.append("#include \"%s.h\"", options.astClass);
        headerWriter.append("#include \"%s.h\"", options.lexerClass);
        headerWriter.append("");

        code.append("#include <stdexcept>");
        code.append("#include \"%s.h\"", options.parserClass);
        code.append("#include \"%s.h\"", tokens);
        code.append("");

        headerWriter.append("class %s{", options.parserClass);
        headerWriter.append("public:");
        headerWriter.append("%s* lexer;", options.lexerClass);
        headerWriter.append("%s* la;", options.tokenClass);
        headerWriter.append("");

        headerWriter.append("%s(%s& lexer);", options.parserClass, options.lexerClass);

        code.append("%s::%s(%s& lexer){", options.parserClass, options.parserClass, options.lexerClass);
        code.all("this->lexer = &lexer;\nla = lexer.next();\n}");
        code.append("");

        writeConsume();

        for (RuleDecl decl : tree.rules) {
            curRule = decl;
            gen(decl);
            code.append("");
        }
        headerWriter.append("};");


        File headerFile = new File(options.outDir, options.parserClass + ".h");
        Utils.write(headerWriter.get(), headerFile);

        File file = new File(options.outDir, options.parserClass + ".cpp");
        Utils.write(code.get(), file);
        genTokenType();
    }

    void writeConsume() {
        headerWriter.append("%s* consume(int type);", options.tokenClass);

        code.append("%s* %s::consume(int type){", options.tokenClass, options.parserClass);
        code.append("if(la->type != type){");
        code.append("throw std::runtime_error(\"unexpected token: \" + la->toString() + \" expecting: \" + std::to_string(type));");
        code.all("}");
        code.append("try{");
        code.append("%s* res = la;", options.tokenClass);
        code.append("la = lexer->next();");
        code.append("return res;");
        code.all("}\ncatch(...){");
        code.append("throw std::runtime_error(\"consume error\");");
        code.append("}");
        code.append("}");
    }

    void genTokenType() throws IOException {
        CodeWriter c = new CodeWriter(true);
        //ns
        c.append("#pragma once");
        c.append("");
        c.append("class %s{", tokens);
        c.append("public:");
        c.append("const int EOF_ = 0;");
        int id = 1;
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            //if (decl.isSkip) continue;
            c.append("static constexpr int %s = %d;", decl.name, id);
            id++;
        }
        c.down();
        c.append("};");
        File file = new File(options.outDir, tokens + ".h");
        Utils.write(c.get(), file);
    }

    String peekExpr() {
        return "la->type";
    }


    void gen(RuleDecl decl) {
        Type type = new Type(options.astClass, decl.retType.name);//todo decl.type
        StringBuilder params = new StringBuilder();
        int i = 0;
        for (Node arg : decl.ref.args) {
            if (i > 0) params.append(", ");
            if (arg.isName()) {
                Name name = arg.asName();
                if (name.isToken) {
                    params.append(options.tokenClass).append(" ").append(arg.astInfo.varName);
                }
                else {
                    params.append(String.format("%s* %s", tree.getRule(arg.asName()).retType, arg.astInfo.varName));
                }
            }
            else {
                Regex regex = arg.asRegex();
                Name name = regex.node.asName();
                if (name.isToken) {
                    params.append(String.format("std::vector<%s*> %s", options.tokenClass, regex.astInfo.varName));
                }
                else {
                    params.append(String.format("std::vector<%s::%s*> %s", options.astClass, name.name, regex.astInfo.varName));
                }
            }
            i++;
        }
        code.append("%s* %s::%s(%s){", type.cpp(), options.parserClass, decl.baseName(), params);
        code.append("%s* res = new %s();", type.cpp(), type.cpp());
        flagCount = 0;
        firstCount = 0;

        write(decl.rhs);

        code.append("return res;");
        code.append("}");

        headerWriter.append("%s* %s(%s);", type.cpp(), decl.baseName(), params);
    }

    String tokenConsumer(Name token) {
        return String.format("consume(%s, \"%s\")", tokenRef(token), token.name);
    }

    void write(Node node) {
        if (node.astInfo.which != -1) {
            code.all(node.astInfo.writeWhichCpp());
        }
        if (node.astInfo.nodeType != null) {
            code.all(node.astInfo.writeNodeCpp());
        }
        if (node.astInfo.substitution) {
            code.append("%s->%s = %s;", node.astInfo.outerVar, node.astInfo.subVar, node.astInfo.varName);
        }
        if (node.isOr()) {
            Or or = node.asOr();
            writeOr(or);
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            write(group.node);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                write(s.get(i));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            writeName(name);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            writeRegex(regex);
        }
        else if (!node.isEpsilon()) {
            throw new RuntimeException("unexpected " + node);
        }
    }

    String getType(Name name) {
        if (name.isToken) return options.tokenClass;
        return options.astClass + "." + name.name;
    }

    private void writeRegex(Regex regex) {
        if (regex.astInfo.isFactored) {
            Name name = regex.node.asName();
            if (regex.astInfo.factor == null) {
                code.append("%s->%s.insert(%s->%s.end(),%s.begin(),%s.end());", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.outerVar, name.astInfo.varName, regex.astInfo.varName);
            }
            else {
                code.append("for(int i = 0;i < %s.size();i++){", regex.astInfo.factor.varName);
                code.append("%s->%s.push_back(%s(%s.at(i)));", regex.astInfo.outerVar, name.astInfo.varName, name.name, regex.astInfo.factor.varName);
                code.append("}");
            }
            return;
        }
        Set<Name> set = FirstSet.tokens(regex, tree);
        regex.node.astInfo.isInLoop = regex.isStar() || regex.isPlus();
        if (regex.isOptional()) {
            if (set.size() <= loopLimit) {
                code.append("if(%s){", loopExpr(set));
                write(regex.node);
                code.append("}");
            }
            else {
                beginSwitch(set);
                write(regex.node);
                endSwitch("");
            }
        }
        else if (regex.isStar()) {
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    Name name = regex.node.asName();
                    String type = name.isToken ? options.tokenClass : options.astClass + "::" + name.name;
                    code.append("std::vector<%s*> %s;", type, regex.astInfo.varName);
                    code.append("while(%s){", loopExpr(set));
                    String consumer = name.isToken ? tokenConsumer(name) : name + "()";
                    code.append("%s.push_back(%s);", regex.astInfo.varName, consumer);
                    code.append("}");
                }
                else {
                    code.append("while(%s){", loopExpr(set));
                    write(regex.node);
                    code.append("}");
                }
            }
            else {
                flagCount++;
                String flagStr = "flag";
                if (flagCount > 1) flagStr += flagCount;
                code.append(String.format("bool %s = true;", flagStr));
                code.append(String.format("while(%s){", flagStr));
                String def = flagStr + " = false;\n";
                beginSwitch(set);
                write(regex.node);
                endSwitch(def);
                code.append("}");
            }
        }
        else {
            //plus
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    Name name = regex.node.asName();
                    String type = name.isToken ? options.tokenClass : options.astClass + "." + name.name;
                    code.append("std::vector<%s*> %s;", type, regex.astInfo.varName);
                    code.append("do{");
                    String consumer = name.isToken ? tokenConsumer(name) : name + "()";
                    code.append("%s.push_back(%s);", regex.astInfo.varName, consumer);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
                else {
                    code.append("do{");
                    write(regex.node);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
            }
            else {
                flagCount++;
                firstCount++;
                String flagStr = "flag";
                String firstStr = "first";
                if (flagCount > 1) flagStr += flagCount;
                if (firstCount > 1) firstStr += firstCount;
                code.append("bool %s = true;", flagStr);
                code.append("bool %s = true;", firstStr);
                code.append("while(%s){", flagStr);
                String def = String.format("if(!%s)  %s = false;\n" + "else  throw runtime_error(std::string(\"unexpected token: \") + la->toString());", firstStr, flagStr);
                beginSwitch(set);
                write(regex.node);
                endSwitch(def);
                code.append("first = false;\n");
                code.append("}");
            }
        }
    }

    String loopExpr(Set<Name> set) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            Name tok = it.next();
            sb.append(peekExpr()).append(" == ").append(tokenRef(tok));
            if (it.hasNext()) {
                sb.append(" || ");
            }
        }
        return sb.toString();
    }

    String withArgs(Name name) {
        StringBuilder args = new StringBuilder();
        if (!name.args.isEmpty()) {
            for (int i = 0; i < name.args.size(); i++) {
                args.append(name.args.get(i).astInfo.varName);
                if (i < name.args.size() - 1) {
                    args.append(",");
                }
            }
        }
        return name.name + "(" + args + ")";
    }

    private void writeName(Name name) {
        if (name.astInfo.isFactored && name.astInfo.isFactor) {
            //factor names may be different?
            return;
        }
        if (name.astInfo.isFactored) {
            //no consume
            if (name.astInfo.isInLoop) {
                code.append("%s->%s.push_back(%s);", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
            }
            else {
                code.append("%s->%s = %s;", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
            }
        }
        else if (name.astInfo.isPrimary) {
            code.append("%s = %s;", name.astInfo.outerVar, withArgs(name));
        }
        else if (name.astInfo.isSecondary) {
            code.append("%s = %s(%s);", name.astInfo.outerVar, name.name, name.astInfo.outerVar);
        }
        else {
            String rhs;
            if (name.isRule()) {
                rhs = withArgs(name);
            }
            else {
                rhs = tokenConsumer(name);
            }
            if (name.astInfo.isFactor) {
                String type = name.isToken ? options.tokenClass : (options.astClass + "." + name.name);
                code.append("%s %s = %s;", type, name.astInfo.varName, rhs);
            }
            else {
                if (name.astInfo.isInLoop) {
                    code.append("%s->%s.push_back(%s);", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
                else {
                    code.append("%s->%s = %s;", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
            }
        }
    }

    String tokenRef(Name tok) {
        return tokens + "::" + tok.name;
    }

    private void writeOr(Or or) {
        code.append(String.format("switch(%s){", peekExpr()));
        Node empty = null;
        for (int i = 0; i < or.size(); i++) {
            final Node ch = or.get(i);
            Set<Name> set = FirstSet.tokens(ch, tree);
            if (!set.isEmpty()) {
                for (Name la : set) {
                    code.append(String.format("case %s:", tokenRef(la)));
                }
                code.append("{");
                write(ch);
                code.append("break;");
                code.append("}");
                if (Helper.canBeEmpty(ch, tree)) {
                    empty = ch;
                }
            }
            else {
                empty = ch;
            }
        }
        if (empty != null) {
            code.append("default:{");
            write(empty);
            code.append("}");
        }
        else if (!Helper.canBeEmpty(or, tree)) {
            code.append("default:{");
            StringBuilder arr = new StringBuilder();
            boolean first = true;
            for (Name tok : FirstSet.tokens(or, tree)) {
                if (!first) {
                    arr.append(",");
                }
                arr.append(tok);
                first = false;
            }
            code.append("throw std::runtime_error(\"expecting one of [%s] got: \"+la->toString());", arr);
            code.append("}");
        }
        code.append("}");
    }

    void beginSwitch(Set<Name> set) {
        code.append("switch(" + peekExpr() + "){");
        for (Name token : set) {
            code.append("case %s:", tokenRef(token));
        }
        code.append("{");
    }

    void endSwitch(String def) {
        code.all("}\nbreak;");
        if (!def.isEmpty()) {
            code.append("default:{");
            code.all(def);
            code.append("}");
        }
        code.append("}");
    }
}
