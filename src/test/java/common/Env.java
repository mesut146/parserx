package common;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexFromStr;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class Env {
    public static String dir = "/home/mesut/Desktop/IdeaProjects/parserx";

    public static File dotDir() {
        return new File(dir + "/dots");
    }

    public static File dotFile(String name) throws IOException {
        dotDir().mkdirs();
        File file = new File(dotDir(), name);
        file.createNewFile();
        return file;
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
        return Tree.makeTree(getResFile(res));
    }

    @Test
    public void tokenLessTest() {
        System.out.println(Utils.makeTokenLessTree("A: \"asd\" \"a\"*;", false));
    }

    @Test
    public void tokenLessTest2() {
        Node rhs = RegexFromStr.build("a*b");
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("START", rhs));
        System.out.println(Utils.makeTokenLessTree(tree.toString(), true));
    }
}
