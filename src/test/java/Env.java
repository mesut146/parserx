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
        return Env.getResFile("javaLexer.g");
    }

    public static File getCalc() {
        return Env.getResFile("calc.g");
    }

    public static File getFile2(String name) {
        return new File(testRes, name);
    }

    public static File getResFile(String name) {
        return new File(Env.class.getClassLoader().getResource(name).getPath());
    }
}
