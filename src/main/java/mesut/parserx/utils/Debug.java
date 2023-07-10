package mesut.parserx.utils;

import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;

import java.io.File;
import java.io.FileOutputStream;

public class Debug {

    public static void dot(Tree tree, LLDfaBuilder builder) {
        try {
            var f1 = new File(tree.options.outDir, Utils.trimExt(tree.file.getName()) + "-items");
            var f2 = new File(tree.options.outDir, Utils.trimExt(tree.file.getName()) + "-trans");
            builder.dumpItems(new FileOutputStream(f1));
            builder.dump(new FileOutputStream(f2));
            System.out.println("dump " + f1);
            System.out.println("dump " + f2);
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
