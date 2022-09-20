package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.TokenDecl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DotWriter {

    public static void table(PrintWriter writer, LrDFAGen generator, boolean writeRules) {
        var tokens = new LinkedHashSet<Name>();
        generator.tree.tokenBlocks.forEach(tb -> {
            tb.tokens.forEach(decl -> tokens.add(decl.ref()));
            tb.modeBlocks.forEach(mb -> mb.tokens.forEach(decl -> tokens.add(decl.ref())));
        });
        tokens.add(LrDFAGen.dollar);

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
            }
            else {
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
                        }
                        else {
                            writer.print("R");
                            writer.print(name);
                            //index is needed
                            writer.print(item.rule.index);
                        }
                    }
                    else {
                        //lr1
                        if (item.lookAhead.contains(token)) {
                            if (item.lookAhead.contains(LrDFAGen.dollar) && name.equals(start)) {
                                writer.print("accept");
                            }
                            else {
                                writer.print("R");
                                writer.print(name);
                                //index is needed
                                writer.print(item.rule.index);
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
}
