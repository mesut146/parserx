package mesut.parserx.gen.lr;

import mesut.parserx.nodes.NameNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class DotWriter {

    public static void lr0Table(Lr0Generator generator) {
        try {
            PrintWriter writer = new PrintWriter(generator.tableDotFile());
            LrDFA<Lr0ItemSet> table = generator.table;
            List<NameNode> tokens = table.tokens;
            tokens.add(Lr0Generator.dollar);

            writer.println("digraph G{");
            writer.println("rankdir = TD");
            writer.println("size=\"100,100\";");
            writer.println("node [shape=plaintext]");
            writer.println("some_node[label=");
            writer.println("<<TABLE>");
            writer.println("<TR><TD>States</TD>");
            for (NameNode token : tokens) {
                writer.print("<TD>" + token.name + "</TD>");
            }
            for (NameNode rule : table.rules) {
                writer.println("<TD>" + rule.name + "</TD>");
            }
            writer.println("</TR>");
            String start = generator.start.name;
            for (Lr0ItemSet set : table.itemSets) {
                writer.print("<TR>");
                writer.println("<TD>I" + table.getId(set) + "</TD>");
                //shift/reduce
                for (NameNode token : tokens) {
                    writer.print("<TD>");
                    for (LrTransition<Lr0ItemSet> tr : table.getTrans(set)) {
                        if (tr.symbol.equals(token)) {
                            writer.print("S" + table.getId(tr.to));
                        }
                    }
                    if (set.hasReduce()) {
                        List<LrItem> reduce = set.getReduce();
                        String name = reduce.get(0).ruleDecl.name;
                        if (name.equals(start)) {
                            writer.print("accept");
                        }
                        else {
                            writer.print("R" + name);
                            if (generator.tree.getRules(name).size() > 1) {
                                //index is needed
                                writer.print(reduce.get(0).ruleDecl.index);
                            }
                        }
                    }
                    writer.print("</TD>");
                }
                //goto
                for (NameNode rule : table.rules) {
                    writer.print("<TD>");
                    for (LrTransition<Lr0ItemSet> tr : table.getTrans(set)) {
                        if (tr.symbol.equals(rule) && tr.to.hasReduce()) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void lr1Table(Lr1Generator generator) {
        lr1Table(generator, false);
    }

    public static void lr1Table(Lr1Generator generator, boolean writeRules) {
        try {
            PrintWriter writer = new PrintWriter(new File(generator.dir, generator.tree.file.getName() + "-table.dot"));
            LrDFA<Lr1ItemSet> table = generator.table;
            List<NameNode> tokens = table.tokens;
            tokens.add(Lr0Generator.dollar);

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
            for (NameNode token : tokens) {
                writer.print("<TD>" + token.name + "</TD>");
            }
            //rules
            for (NameNode rule : table.rules) {
                writer.println("<TD>" + rule.name + "</TD>");
            }
            writer.println("</TR>");
            String start = generator.start.name;
            for (Lr1ItemSet set : table.itemSets) {
                writer.print("<TR>");
                writer.println("<TD>I" + table.getId(set) + "</TD>");
                if (writeRules) {
                    String s = set.toString();
                    //int size = s.replaceAll("[^\n]", "").length();
                    writer.println("<TD>");
                    for (String line : s.replace("<", "&lt;").replace(">", "&gt;").split("\n")) {
                        writer.print("<BR/>");
                        writer.print(line);
                    }
                    writer.print("</TD>");
                }
                //shift/reduce
                for (NameNode token : tokens) {
                    writer.print("<TD>");
                    for (LrTransition<Lr1ItemSet> tr : table.getTrans(set)) {
                        if (tr.symbol.equals(token)) {
                            writer.print("S" + table.getId(tr.to));
                        }
                    }
                    if (set.hasReduce()) {
                        List<LrItem> reduce = set.getReduce();
                        if (reduce.get(0).ruleDecl.name.equals(start)) {
                            writer.print("accept");
                        }
                        else {
                            writer.print("R" + reduce.get(0).ruleDecl.name);
                        }
                    }
                    writer.print("</TD>");
                }
                //goto
                for (NameNode rule : table.rules) {
                    writer.print("<TD>");
                    for (LrTransition<Lr1ItemSet> tr : table.getTrans(set)) {
                        if (tr.symbol.equals(rule) && tr.to.hasReduce()) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
