import java.io.File;

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


    public static File getJavaLexer() {
        return Env.getFile("javaLexer.g");
    }

    public static File getCalc() {
        return Env.getFile("calc.g");
    }

    public static File getFile(String name) {
        return new File(Env.class.getClassLoader().getResource(name).getPath());
    }
}
