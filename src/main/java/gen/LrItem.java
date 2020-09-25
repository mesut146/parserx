package gen;

import rule.RuleDecl;

public class LrItem {
    RuleDecl ruleDecl;
    int dotPos;


    public LrItem(RuleDecl ruleDecl, int dotPos) {
        this.ruleDecl = ruleDecl;
        this.dotPos = dotPos;
    }
}
