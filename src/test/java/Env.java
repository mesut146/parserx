import java.io.File;
import java.net.URL;

public class Env {
    static String dir;
    static String testJava;
    static String testRes;

    static {
        dir = "/home/mesut/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        testJava = dir + "/src/test/java";
        testRes = dir + "/src/test/resources";
    }


    public static File getJavaLexer() throws Exception {
        return Env.getResFile("/javaLexer.g");
    }

    public static File getCalc() throws Exception {
        return Env.getResFile("/calc.g");
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
}
