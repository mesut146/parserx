package rule;

//right side
//can refer to rule or token
public class RuleRef extends Rule {

    String name;
    boolean isToken;

    public RuleRef(String name) {
        this.name = name;
    }
    
    public RuleDecl declare(){
        return new RuleDecl(name);
    }

    @Override
    public String toString() {
        return name;
    }


}
