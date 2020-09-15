import java.io.File;

public class Env {
    static String dir;
    static String testJava;

    static {
        dir = "/home/mesut/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        testJava = dir + "/src/test/java";
    }

    public static File getFile(String name) {
        return new File(Env.class.getClassLoader().getResource(name).getPath());
    }
}
