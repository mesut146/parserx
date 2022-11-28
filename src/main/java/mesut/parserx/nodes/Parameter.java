package mesut.parserx.nodes;

import mesut.parserx.gen.lldfa.Type;

public class Parameter {
    public Type type;
    public String name;

    public Parameter(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
