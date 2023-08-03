package mesut.parserx.gen.lldfa;

//qualified ast type;
public class Type {
    public String name;
    public Type scope;

    public Type(String name) {
        this.name = name;
    }

    public Type(Type scope, String name) {
        this.scope = scope;
        this.name = name;
    }

    public Type(String scope, String name) {
        this.scope = new Type(scope);
        this.name = name;
    }


    @Override
    public String toString() {
        if (scope == null) {
            return name;
        }
        return scope + "." + name;
    }

    public String cpp() {
        if (scope == null) {
            return name;
        }
        return scope.cpp() + "::" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Type)) return false;
        Type type = (Type) o;
        return toString().equals(type.toString());
    }
}
