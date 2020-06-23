public class Env {
    static String dir;
    static String javaDir;
    static String testDir;
    static String testJava;

    static {
        dir = "/home/mesut/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        testDir = dir + "/test";
        javaDir = dir + "/src/main/java";
        testJava = dir + "/src/test/java";
    }
}
