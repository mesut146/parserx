package mesut.parserx.gen.lr;

import mesut.parserx.gen.ParserUtils;
import mesut.parserx.nodes.BaseVisitor;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DotWriter {

    static Set<Name> getUsedTokens(Tree tree) {
        var res = new HashSet<Name>();
        var visitor = new BaseVisitor<>() {
            @Override
            public Object visitName(Name name, Object arg) {
                if (name.isToken) {
                    res.add(name);
                }
                return super.visitName(name, arg);
            }
        };
        tree.rules.forEach(rd -> rd.rhs.accept(visitor, null));
        return res;
    }

    public static void table(PrintWriter writer, LrDFAGen generator, boolean writeRules) {
        var tokens = new LinkedHashSet<Name>();
        var used=getUsedTokens(generator.tree);
        generator.tree.tokenBlocks.forEach(tb -> {
            tb.tokens.stream()
                    .filter(decl->used.contains(decl.ref()))
                    .forEach(decl->tokens.add(decl.ref()));
            tb.modeBlocks.forEach(mb -> mb.tokens
                    .stream()
                    .filter(decl->used.contains(decl.ref()))
                    .forEach(decl -> tokens.add(decl.ref())));
        });
        tokens.add(ParserUtils.dollar);

        writer.println("digraph G{");
        writer.println("rankdir = TD");
        writer.println("size=\"100,100\";");
        writer.println("node [shape=plaintext]");
        writer.println("some_node[label=");
        writer.println("<<TABLE>");
        writer.println("<TR>");
        writer.println("<TD>States</TD>");
        if (writeRules) {
            writer.println("<TD>Rules</TD>");
        }
        //tokens
        for (var token : tokens) {
            var decl = generator.tree.getToken(token.name);
            if (decl != null && decl.rhs.isString()) {
                writer.print("<TD>" + decl.rhs + "</TD>");
            } else {
                writer.print("<TD>" + token.name + "</TD>");
            }
        }
        //rules
        var rules = generator.tree.rules.stream()
                .map(rd -> rd.ref)
                .collect(Collectors.toList());
        for (var rule : rules) {
            writer.println("<TD>" + rule.name + "</TD>");
        }
        writer.println("</TR>");
        var start = generator.start.baseName();
        for (var set : generator.itemSets) {
            writer.print("<TR>");
            writer.println("<TD>" + set.stateId + "</TD>");
            if (writeRules) {
                String s = set.toString();
                writer.println("<TD>");
                for (String line : s.replace("<", "&lt;").replace(">", "&gt;").split("\n")) {
                    writer.print("<BR/>");
                    writer.print(line);
                }
                writer.print("</TD>");
            }
            //shift/reduce
            for (var token : tokens) {
                writer.print("<TD>");
                //shift
                for (LrTransition tr : set.transitions) {
                    if (tr.symbol.equals(token)) {
                        writer.print("S" + tr.target.stateId);
                    }
                }
                //reduce
                for (var item : set.getReduce()) {
                    var name = item.rule.baseName();
                    if (item.lookAhead.isEmpty()) {
                        //lr0
                        if (name.equals(start)) {
                            writer.print("accept");
                        } else {
                            writer.print("R");
                            writer.print(name);
                            //index is needed
                            writer.print(item.rule.index + 1);
                        }
                    } else {
                        //lr1
                        if (item.lookAhead.contains(token)) {
                            if (item.lookAhead.contains(ParserUtils.dollar) && name.equals(start)) {
                                writer.print("accept");
                            } else {
                                writer.print("R");
                                writer.print(name);
                                //index is needed
                                writer.print(item.rule.index + 1);
                            }
                        }
                    }
                    /*if (name.equals(start)) {
                        writer.print("accept");
                    }*/
                }
                writer.print("</TD>");
            }
            //goto
            for (var rule : rules) {
                writer.print("<TD>");
                /*for (LrItem item : set.getReduce()) {
                    if (item.ruleDecl.name.equals(rule.name)) {
                        writer.print(table.getId(item.gotoSet));
                    }
                }*/
                for (LrTransition tr : set.transitions) {
                    if (tr.symbol.equals(rule)) {//tr.to.hasReduce()
                        writer.print(tr.target.stateId);
                    }
                }
                writer.print("</TD>");
            }
            writer.print("</TR>");
        }
        writer.println("</TABLE>>];");

        writer.println("}");
        writer.close();
    }

    public static void writeDot(LrDFAGen gen, PrintWriter writer) {
        try {
            writer.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            writer.println("rankdir = TD");
            writer.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");

            //labels
            for (LrItemSet set : gen.itemSets) {
                writer.printf("%s [shape=box xlabel=\"%s\" %s label=\"%s\"]\n",
                        set.stateId, set.stateId, set.hasReduce() ? "color=red " : "", set.toString().replace("\n", "\\l").replace("\"", "\\\"") + "\\l");
            }

            for (var set : gen.itemSets) {
                for (var tr : set.transitions) {
                    writer.printf("%s -> %s [label=\"%s\"]\n", set.stateId, tr.target.stateId, tr.symbol.name);
                }
            }

            writer.println("}");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dump(LrDFAGen gen, PrintWriter w) {
        for (var set : gen.itemSets) {
            w.println("----------------------------");
            w.printf("S%d\n", set.stateId);
            w.print(set);
            w.println();
            for (var tr : set.transitions) {
                w.printf("%s -> %s, %s\n", set.stateId, tr.target.stateId, tr.symbol.name);
            }
            w.println("\n");
        }
        w.flush();
        w.close();
    }
}
