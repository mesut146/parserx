package mesut.parserx.utils;

import mesut.parserx.nodes.*;
import mesut.parserx.parser.AstVisitor;
import mesut.parserx.regex.parser.RegexVisitor;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static Tree fromRegex(String regex) throws IOException {
        Node rhs = RegexVisitor.make(regex);
        Tree tree = new Tree();
        var block = new TokenBlock();
        tree.tokenBlocks.add(block);
        tree.addToken(new TokenDecl("START", rhs), block);
        makeTokens(tree, true);
        return tree;
    }

    public static Tree fromGrammar(String grammar) throws IOException {
        Tree tree = AstVisitor.makeTree(grammar);
        makeTokens(tree, false);
        return tree;
    }

    public static Tree makeTokenLessTree(String grammar, boolean fromRegex) {
        try {
            Tree tree = AstVisitor.makeTree(grammar);
            makeTokens(tree, fromRegex);
            return tree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //make missing tokens
    public static void makeTokens(final Tree tree, final boolean fromRegex) {
        final Map<String, TokenDecl> newTokens = new HashMap<>();
        var block = new TokenBlock();
        tree.tokenBlocks.add(block);
        new Transformer(tree) {
            int count = 1;

            @Override
            public Node visitName(Name name, Void parent) {
                //if it is not a rule then must be a token
                if (tree.getRule(name) == null) {
                    //add fake token
                    name.isToken = true;
                    tree.addToken(new TokenDecl(name.name, new StringNode(name.name)), block);
                }
                return name;
            }

            @Override
            public Node visitString(StringNode node, Void parent) {
                var decl = tree.getTokenByValue(node.value);
                if (decl == null) {
                    if (fromRegex) {
                        return node;
                    } else {
                        decl = new TokenDecl("T" + count++, node);
                        newTokens.put(decl.name, decl);
                    }
                }
                return decl.ref();
            }
        }.transformAll();
        for (var decl : newTokens.values()) {
            tree.addToken(decl, block);
        }
    }

    public static String camel(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static String trimExt(String name) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            return name.substring(0, i);
        }
        return name;
    }

    public static String newName(String name, String suffix) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
        } else {
            name = name + ".";
        }
        name = name + suffix;
        return name;
    }

    public static String read(InputStream in) throws IOException {
        return new String(in.readAllBytes());
    }

    public static String read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public static void copy(File src, File target) throws IOException {
        System.out.println("writing " + target);
        target.getParentFile().mkdirs();
        Files.deleteIfExists(target.toPath());
        Files.copy(src.toPath(), target.toPath());
    }

    public static void write(String data, File file) throws IOException {
        System.out.println("writing " + file);
        file.getParentFile().mkdirs();
        FileWriter wr = new FileWriter(file);
        wr.write(data);
        wr.close();
    }

}
