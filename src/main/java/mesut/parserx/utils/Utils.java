package mesut.parserx.utils;

import java.io.*;

public class Utils {

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
