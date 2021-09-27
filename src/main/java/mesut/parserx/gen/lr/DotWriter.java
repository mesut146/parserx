package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.TokenDecl;

import java.io.PrintWriter;
import java.util.List;

public class DotWriter {

    public static void table(PrintWriter writer, LrDFAGen<?> generator, boolean writeRules) {
        LrDFA<?> table = generator.table;
        List<Name> tokens = table.tokens;
        tokens.add(LrDFAGen.dollar);

        writer.println("digraph G{");
        writer.println("rankdir = TD");
        writer.println("size=\"100,100\";");
        writer.println("node [shape=plaintext]");
        writer.println("some_node[label=");
        writer.println("<<TABLE>");
        writer.println("<TR>");
        writer.println("<TD>States</TD>");
        if (writeRules)
            writer.println("<TD>Rules</TD>");
        //tokens
        for (Name token : tokens) {
            TokenDecl decl = generator.tree.getToken(token.name);
            if (decl != null && decl.rhs.isString()) {
                writer.print("<TD>" + decl.rhs + "</TD>");
            }
            else {
                writer.print("<TD>" + token.name + "</TD>");
            }
        }
        //rules
        for (Name rule : table.rules) {
            writer.println("<TD>" + rule.name + "</TD>");
        }
        writer.println("</TR>");
        String start = generator.start.name;
        for (LrItemSet set : table.itemSets) {
            writer.print("<TR>");
            writer.println("<TD>" + table.getId(set) + "</TD>");
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
            for (Name token : tokens) {
                writer.print("<TD>");
                //shift
                for (LrTransition<?> tr : table.getTrans(set)) {
                    if (tr.symbol.equals(token)) {
                        writer.print("S" + table.getId(tr.to));
                    }
                }
                //reduce
                for (LrItem item : set.getReduce()) {
                    String name = item.rule.name;
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
            for (Name rule : table.rules) {
                writer.print("<TD>");
                /*for (LrItem item : set.getReduce()) {
                    if (item.ruleDecl.name.equals(rule.name)) {
                        writer.print(table.getId(item.gotoSet));
                    }
                }*/
                for (LrTransition<?> tr : table.getTrans(set)) {
                    if (tr.symbol.equals(rule)) {//tr.to.hasReduce()
                        writer.print(table.getId(tr.to));
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

    public static <T extends LrItemSet> void writeDot(LrDFA<T> table, PrintWriter writer) {
        try {
            writer.println("digraph G{");
            //dotWriter.println("rankdir = LR");
            writer.println("rankdir = TD");
            writer.println("size=\"100,100\";");
            //dotWriter.println("ratio=\"fill\";");

            //labels
            for (T set : table.itemSets) {
                writer.printf("%s [shape=box xlabel=\"%s\" %s label=\"%s\"]\n",
                        table.getId(set), table.getId(set), set.hasReduce() ? "color=red " : "", set.toString().replace("\n", "\\l") + "\\l");
            }

            for (int i = 0; i <= table.lastId; i++) {
                if (table.map[i] != null)
                    for (LrTransition<T> t : table.map[i]) {
                        writer.printf("%s -> %s [label=\"%s\"]\n",
                                table.getId(t.from), table.getId(t.to), t.symbol.name);
                    }
            }

            writer.println("}");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
