package rule;

//right side
//can refer to rule or token
public class RuleRef extends Rule {

    String name;
    public boolean isToken;//if we reference to a token

    public RuleRef(String name) {
        this.name = name;
    }

    public RuleDecl declare() {
        return new RuleDecl(name);
    }

    @Override
    public String toString() {
        if (isToken) {
            return "{" + name + "}";
        }
        return name;
    }


}
