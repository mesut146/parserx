package mesut.parserx.gen;

import mesut.parserx.gen.ll.Type;

public class Insn {

    //declares a factor
    public static class FactorInsn extends Insn {
        public String factorName;
    }

    public static class AssignInsn extends Insn {
        public String left;
        public String right;
    }

    public static class AssignOuter extends Insn {
        public String outerVar;
        public String varName;
        public String right;
    }

    public static class CreateNodeInsn extends Insn {
        public Type type;
        public String varName;
    }
}
