package mesut.parserx.utils;

import java.io.*;

public class IOUtils {

    public static File newName(File file, String ext) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i == -1) {
            name = name + "." + ext;
        }
        else {
            name = name.substring(0, i) + "." + ext;
        }
        return new File(file.getParent(), name);
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
        FileWriter wr = new FileWriter(file);
        wr.write(data);
        wr.close();
    }

}
