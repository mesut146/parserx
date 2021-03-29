package mesut.parserx.utils;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.OrNode;
import mesut.parserx.nodes.Sequence;

import java.io.*;

public class Helper {

    public static String read(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static String read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public static void write(String data, File file) throws IOException {
        FileWriter wr = new FileWriter(file);
        wr.write(data);
        wr.close();
    }

    public static Node split(Node node) {
        if (node.isSequence()) {
            Sequence sequence = node.asSequence();
            return new Sequence(sequence.list.subList(1, sequence.size())).normal();
        }
        else if (node.isOr()) {
            OrNode or = node.asOr();
            return new OrNode(or.list.subList(1, or.size())).normal();
        }
        return node;
    }
}
