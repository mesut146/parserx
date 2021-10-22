package mesut.parserx.utils;

import mesut.parserx.nodes.*;
import mesut.parserx.parser.AstBuilder;
import mesut.parserx.regex.RegexFromStr;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static Tree fromRegex(String regex) {
        Node rhs = RegexFromStr.build(regex);
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("START", rhs));
        makeTokens(tree, true);
        return tree;
    }

    public static Tree fromGrammar(String grammar) throws IOException {
        Tree tree = AstBuilder.makeTree(grammar);
        makeTokens(tree, false);
        return tree;
    }

    public static Tree makeTokenLessTree(String grammar, boolean fromRegex) {
        try {
            Tree tree = AstBuilder.makeTree(grammar);
            makeTokens(tree, fromRegex);
            return tree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeTokens(final Tree tree, final boolean fromRegex) {
        new SimpleTransformer(tree) {
            int count = 1;
            Map<String, TokenDecl> newTokens = new HashMap<>();

            @Override
            public Node transformName(Name node, Node parent) {
                //if it is not a rule then must be a token
                if (tree.getRule(node) == null) {
                    //add fake token
                    node.isToken = true;
                    tree.tokens.add(new TokenDecl(node.name, new StringNode(node.name)));
                }
                return node;
            }

            @Override
            public Node transformString(StringNode node, Node parent) {
                TokenDecl decl = tree.getTokenByValue(node.value);
                if (decl == null) {
                    if (fromRegex) {
                        return node;
                    }
                    else {
                        decl = new TokenDecl("T" + count++, node);
                        newTokens.put(decl.name, decl);
                    }
                }
                return decl.ref();
            }

            @Override
            public void transformAll() {
                super.transformAll();
                for (TokenDecl decl : newTokens.values()) {
                    tree.addToken(decl);
                }
            }
        }.transformAll();
    }

    public static String camel(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static String newName(String name, String suffix) {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
        }
        else {
            name = name + ".";
        }
        name = name + suffix;
        return name;
    }

    public static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static String read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public static void write(String data, File file) throws IOException {
        System.out.println("writing " + file);
        file.getParentFile().mkdirs();
        FileWriter wr = new FileWriter(file);
        wr.write(data);
        wr.close();
    }

}
