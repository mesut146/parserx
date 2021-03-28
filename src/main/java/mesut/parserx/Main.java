package mesut.parserx;

import java.io.File;
import java.io.FileOutputStream;

public class Main {


    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        File input;
        File output;
        String cmd = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-input") || args[i].equals("-in")) {
                input = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-output") || args[i].equals("-out")) {
                output = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-left") || args[i].equals("-epsilon")) {
                cmd = args[i];
                i++;
            }
        }
    }

}
