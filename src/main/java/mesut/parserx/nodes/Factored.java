package mesut.parserx.nodes;

public class Factored extends Epsilon {
    public Name original;
    public String name;

    public Factored(Name original, String name) {
        this.original = original;
        this.name = name;
    }

    @Override
    public String toString() {
        return original.name + "_f";
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitFactored(this,arg);
    }
}
