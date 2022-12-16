package mesut.parserx.nodes;

import mesut.parserx.gen.lldfa.Type;

public class Parameter {
    public Type type;
    public String name;

    public Parameter(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    //arg
    public Parameter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (type == null) return name;
        return type + " " + name;
    }

    @Override
    public boolean equals(Object obj) {
        Parameter other = (Parameter) obj;
        if (name == null) return type.equals(other.type);
        return type.equals(other.type) && name.equals(other.name);
    }

    public Parameter copy() {
        return new Parameter(type, name);
    }
}
