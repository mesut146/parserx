package base.asd;

import com.test2.*;

public class test<Integer> extends other implements iface, iface2<Integer> {

    public static int x = 5 + 1;
    Object y = null;
    String s = new String("mystr");
    Object t = 0, u;
    
    /*
    my block comment
     */

    public static final void normal(Param p1, X p2, String... p3) {
        //comment
        new X().run();
        return;
    }

    <T> void generic(int x, T y) {

    }

    test() {

    }

    <T> test(T t) {

    }

    class inner {
        int test;
    }

}

class second {

}
