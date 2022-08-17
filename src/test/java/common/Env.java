package common;

import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class Env {
    public static String dir = new File(".").getAbsolutePath();

    public static File dotDir() {
        return new File(dir, "dots");
    }

    public static File dotFile(String name) throws IOException {
        dotDir().mkdirs();
        File file = new File(dotDir(), name);
        file.createNewFile();
        return file;
    }

    public static void dot(File name) {
        try {
            Runtime.getRuntime().exec(("dot -Tpng -O " + name).split(" "));
            System.out.println("writing " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getResFile(String name) throws IOException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        URL url = Env.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(name + " not found");
        }
        return new File(url.getPath());
    }

    public static Tree tree(String res) throws IOException {
        var tree = Tree.makeTree(getResFile(res));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        return tree;
    }

    @Test
    public void tokenLessTest() throws IOException {
        System.out.println(Utils.fromGrammar("A: \"asd\" \"a\"*;"));
    }

    @Test
    public void tokenLessTest2() throws IOException {
        System.out.println(Utils.fromRegex("a*b"));
    }
}
