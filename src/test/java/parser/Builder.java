package parser;

import java.util.ArrayList;
import java.util.List;

public class Builder {
    String tree;
    String curRule;
    List<RuleInfo> cases = new ArrayList<>();

    static class RuleInfo {
        String rule;
        String input;
        String expected;
    }

    public static Builder tree(String name) {
        Builder res = new Builder();
        res.tree = name;
        return res;
    }

    public Builder rule(String name) {
        curRule = name;
        return this;
    }

    public Builder input(String input, String expected) {
        RuleInfo info = new RuleInfo();
        info.input = input;
        info.expected = expected;
        info.rule = curRule;
        cases.add(info);
        return this;
    }
}
