package mesut.parserx.utils;

import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;

import java.io.File;
import java.io.FileOutputStream;

public class Debug {

    public static void dot(Tree tree, LLDfaBuilder builder) {
        try {
            builder.dumpItems(new FileOutputStream(new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dump"))));
            builder.dump(new FileOutputStream(new File(tree.options.outDir, Utils.newName(tree.file.getName(), ".dump2"))));
//            for (var dot : builder.dotAll(new File(tree.options.outDir))) {
//                Runtime.getRuntime().exec(("dot -Tpng -O " + dot).split(" "));
//                //Thread.sleep(100);
//                dot.delete();
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
