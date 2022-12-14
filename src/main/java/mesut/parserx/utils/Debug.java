package mesut.parserx.utils;

import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Debug {

    public static void dot(Tree tree, LLDfaBuilder builder) {
        try {
            File dot = new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dot"));
            builder.dumpItems(new FileOutputStream(new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dump"))));
            builder.dump(new FileOutputStream(new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dump2"))));
            builder.dot(new PrintWriter(dot));
            Runtime.getRuntime().exec(("dot -Tpng -O " + dot).split(" "));
            //Thread.sleep(100);
            dot.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
