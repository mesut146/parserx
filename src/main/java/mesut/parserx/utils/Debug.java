package mesut.parserx.utils;

import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Debug {

    public static void dot(Tree tree, LLDfaBuilder builder) {
        File dot = new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dot"));
        try {
            builder.dot(new PrintWriter(dot));
            Runtime.getRuntime().exec("dot -Tpng -O " + dot).waitFor();
            //Thread.sleep(100);
            dot.delete();
            File dump = new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dump"));
            builder.dumpItems(new FileOutputStream(dump));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
