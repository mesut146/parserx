package mesut.parserx.utils;

import mesut.parserx.nodes.*;
import mesut.parserx.parser.AstBuilder;

import java.io.*;

public class Utils {

    public static Tree makeTokenLessTree(String grammar) {
        try {
            final Tree tree = AstBuilder.makeTree(grammar);
            new SimpleTransformer(tree) {
                int count = 1;

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
                        decl = new TokenDecl("T" + count++, node);
                        tree.addToken(decl);
                    }
                    return decl.ref();
                }
            }.transformAll();
            return tree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
