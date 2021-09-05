package mesut.parserx.gen.ll;

import mesut.parserx.nodes.Node;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type outerCls;
    public boolean isFactor;//no assign
    public boolean isFactored;//epsilon
    public boolean isGroup;
    public boolean isFactorGroup;//follows a factor
    String code;
    Node old;
    boolean isArr;

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.outerCls = outerCls;
        res.isFactor = isFactor;
        res.isFactored = isFactored;
        res.code = code;
        res.old = old;
        res.isArr = isArr;
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor");
        }
        else {
            sb.append(String.format("var=%s cls=%s v2=%s", varName, outerCls, outerVar));
        }
        sb.append('}');
        return sb.toString();
    }
}
