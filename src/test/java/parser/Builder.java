package parser;

import common.Env;
import mesut.parserx.nodes.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Builder {
    Tree tree;
    String curRule;
    List<RuleInfo> cases = new ArrayList<>();

    static class RuleInfo {
        String rule;
        String input;
        String expected;
        boolean isFile = false;
    }

    public Builder dump() {
        tree.options.dump = true;
        return this;
    }

    public static Builder tree(String name) {
        var res = new Builder();
        try {
            res.tree = Env.tree(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static Builder tree(Tree tree) {
        var res = new Builder();
        res.tree = tree;
        return res;
    }

    public Builder rule(String name) {
        curRule = name;
        return this;
    }

    public Builder input(String input, String expected) {
        var info = new RuleInfo();
        info.input = input;
        info.expected = expected;
        info.rule = curRule;
        cases.add(info);
        return this;
    }

    public Builder file(String input) {
        var info = new RuleInfo();
        info.input = input;
        info.rule = curRule;
        info.isFile = true;
        cases.add(info);
        return this;
    }

    public void check() throws Exception {
        System.out.println("testing " + tree.file.getName());
        DescTester.check(this, true);
    }

    public void checkCC() throws Exception {
        System.out.println("testing " + tree.file.getName());
        DescTester.check(this, true);
    }

    public void lr() throws Exception {
        System.out.println("testing " + tree.file.getName());
        LrTester.check0(this);
    }

    public void checkTokens() throws Exception {
        System.out.println("testing " + tree.file.getName());
        DescTester.checkTokens(this);
    }
}
