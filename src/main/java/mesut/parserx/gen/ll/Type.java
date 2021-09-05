package mesut.parserx.gen.ll;

//qualified class;
public class Type {
    Type scope;
    String name;

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
}
