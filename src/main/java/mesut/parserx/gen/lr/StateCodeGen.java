package mesut.parserx.gen.lr;

import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Name;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

public class StateCodeGen {
    public static boolean debugState = false;
    public static boolean debugReduce = false;
    public Options options;
    LrDFA<?> dfa;
    LRTableGen gen;
    CodeWriter writer = new CodeWriter(true);
    IdMap idMap;

    public StateCodeGen(LrDFA<?> dfa, LRTableGen tableGen, IdMap idMap) {
        this.dfa = dfa;
        this.gen = tableGen;
        this.idMap = idMap;
        options = tableGen.tree.options;
    }

    public void gen() throws IOException {
        if (options.packageName != null) {
            writer.append("package %s;", options.packageName);
            writer.append("");
        }
        writer.append("import java.util.*;");
        writer.append("import java.io.*;");
        writer.append("");
        writer.append("public class %s{", options.parserClass);
        writer.append("%s lexer;", options.lexerClass);
        writer.append("Stack<Symbol> stack = new Stack<>();");
        writer.append("Stack<Integer> states = new Stack<>();");
        writer.append("Symbol la;");
        writer.append(" ");

        writer.all("public %s(%s lexer){\n" + "this.lexer = lexer;\n" + "}\n\n", options.parserClass, options.lexerClass);

        writer.all("Symbol next() throws IOException{\n" +
                "return new Symbol(lexer.%s());\n" +
                "}\n\n", options.lexerFunction);

        writer.all("Symbol parse() throws IOException{\n" +
                "la = next();\n" +
                "state0();\n" +
                "return stack.pop();\n" +
                "}\n\n");

        for (LrItemSet set : dfa.itemSets) {
            gen(set);
        }
        writer.append("}");
        File file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(writer.get(), file);
        System.out.println("writing " + file);

        Template sym = new Template("Symbol.java.template");
        sym.set("token_class", options.tokenClass);
        sym.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        File symFile = new File(options.outDir, "Symbol.java");
        Utils.write(sym.toString(), symFile);
        System.out.println("writing " + symFile);

        idMap.writeSym(options);
    }

    String getName(LrItemSet set) {
        int id = dfa.getId(set);
        return "state" + id;
    }

    String symName(Name name) {
        if (name.name.equals("$")) {
            return "EOF";
        }
        return name.name;
    }

    String writeCase(Name sym) {
        return "sym." + symName(sym);
        //return String.valueOf(idMap.getId(sym));
    }

    private void gen(LrItemSet set) {
        writer.append("public void %s() throws IOException{", getName(set));
        if (debugState) {
            writer.append("System.out.println(\"%s\");", getName(set));
        }
        writer.append("states.push(%d);", dfa.getId(set));
        if (!dfa.getTrans(set).isEmpty()) {
            //shifts
            writer.append("switch(la.id){");
            for (LrTransition tr : dfa.getTrans(set)) {
                if (tr.symbol.isToken) {
                    writer.append("case %s:{", writeCase(tr.symbol));
                    writer.append("stack.push(la);");
                    writer.append("la = next();");
                    writer.append(getName(tr.to) + "();");
                    writer.append("return;");
                    writer.append("}");
                }
                else {
                    //goto
                }
            }
            writer.append("}");
        }
        //reduces
        if (!set.getReduce().isEmpty()) {
            writer.append("switch(la.id){");
            for (LrItem item : set.getReduce()) {
                for (Name la : item.lookAhead) {
                    writer.append("case %s:", writeCase(la));
                }
                writer.append("{");
                if (item.rule.name.equals(gen.start.name)) {
                    //accept
                    writer.append("System.out.println(\"accept\");");
                    writer.append("System.out.println(stack.peek());");
                }
                else {
                    writer.append("Symbol tmp = new Symbol(%d);", idMap.getId(item.rule.ref()));
                    writer.append("tmp.name = \"%s\";", item.rule.name);
                    if (item.dotPos == 1) {
                        writer.append("tmp.children.add(stack.pop());");
                        writer.append("states.pop();");
                    }
                    else {
                        writer.append("Symbol[] children = new Symbol[%d];", item.dotPos);
                        writer.append("for(int i = 0;i < %d;i++){", item.dotPos);
                        writer.append("children[%d - i] = stack.pop();", item.dotPos - 1);
                        writer.append("states.pop();");
                        writer.append("}");//for
                        writer.append("tmp.children = new ArrayList<>(Arrays.asList(children));");
                    }
                    writer.append("stack.push(tmp);");
                    if (debugReduce) {
                        writer.append("System.out.println(\"reduce: \" + tmp + \" stack = \" + stack);");
                    }
                    //goto
                    writer.append("switch(states.peek()){");
                    for (LrItemSet s : item.gotoSet) {
                        writer.append("case %d:{", dfa.getId(s));
                        writer.append("%s();", getName(getGoto(s, item.rule.ref())));
                        writer.append("break;");
                        writer.append("}");
                    }
                    writer.append("}");
                    //writer.append("%s();", getName(getGoto(item.gotoSet, item.rule.ref())));
                }
                writer.append("return;");
                writer.append("}");//
            }
            writer.append("}");//switch
        }
        writer.append("throw new RuntimeException(\"invalid input: \" + la);");
        writer.append("}");//func
    }

    LrItemSet getGoto(LrItemSet from, Name symbol) {
        for (LrTransition tr : dfa.getTrans(from)) {
            if (tr.symbol.equals(symbol)) {
                return tr.to;
            }
        }
        return null;
    }
}
