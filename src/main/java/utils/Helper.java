package utils;

import dfa.DFA;
import dfa.NFA;
import nodes.Tree;

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

    public static void write(String data, File file) throws IOException {
        FileWriter wr = new FileWriter(file);
        wr.write(data);
        wr.close();
    }
}
