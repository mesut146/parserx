package mesut.parserx.utils;

import java.io.*;

public class IOUtils {

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

}
