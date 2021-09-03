package common;

import mesut.parserx.grammar.GParser;
import mesut.parserx.nodes.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

public class Env {
    public static String dir;
    public static String testJava;
    public static String testRes;

    static {
        dir = "/home/mesut/Desktop/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        testJava = dir + "/src/test/java";
        testRes = dir + "/src/test/resources";
    }

    public static File dotDir() {
        return new File(dir + "/dots");
    }

    public static File dotFile(String name) throws IOException {
        dotDir().mkdirs();
        File file = new File(dotDir(), name);
        file.createNewFile();
        return file;
    }

    public static File getJavaLexer() throws Exception {
        return Env.getResFile("/javaLexer.g");
    }

    public static File getFile2(String name) {
        return new File(testRes, name);
    }

    public static File getResFile(String name) throws Exception {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        URL url = Env.class.getResource(name);
        if (url == null) {
            throw new Exception(name + " not found");
        }
        return new File(url.getPath());
    }

    public static Tree makeRule(String grammar) {
        grammar += " ";
        try {
            GParser parser = new GParser(new StringReader(grammar));
            final Tree tree = parser.tree(null);
            new SimpleTransformer(tree) {
                @Override
                public Node transformName(Name node, Node parent) {
                    if (tree.getRule(node) == null) {
                        node.isToken = true;
                        //add fake token
                        tree.tokens.add(new TokenDecl(node.name));
                    }
                    return node;
                }
            }.transformAll();
            return tree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
