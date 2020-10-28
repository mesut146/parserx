public class a {

    private static inner in = null;
    public int myInt = 5;
    char chr = 'a';
    String str[][] = {{"test"}, {"asd"}};
    long mylong[];

    public static void main() {
        // line comment
        System.out.println("Hello World!");

        in = new inner();
        in.inner_norm();//in->inner_norm();
        inner.inner_static();//inner::inner_static();
        //inner_static();//inner_static();
    }

    String[] asd(int i, String[][] s) {
        return null;
    }

    int[][] sec(int[] asd) {
        return null;
    }

    static class inner {
        Object field = null;

        static void inner_static() {
            System.out.println("inner_static");
        }

        void inner_norm() {
            System.out.println("inner_norm");
        }
    }

    /*
     ** block comment
     */

}

class outer {

    static void outer_static() {
        System.out.println("outer_static");
    }

    void outer_norm() {
        System.out.println("outer_norm");
    }
}
