package mesut.parserx.gen;

import mesut.parserx.gen.lldfa.Type;

import java.util.Optional;

public class AstInfo {
    public String varName;
    public String outerVar;
    public Type nodeType;
    public boolean isFactor;//no assign
    public boolean isInLoop;
    public boolean isPrimary;
    public Optional<Integer> which = Optional.empty();

    public AstInfo copy() {
        AstInfo res = new AstInfo();
        res.varName = varName;
        res.outerVar = outerVar;
        res.nodeType = nodeType;
        res.isFactor = isFactor;
        res.isInLoop = isInLoop;
        res.which = which;
        res.isPrimary = isPrimary;
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (isFactor) {
            sb.append("factor ").append(varName);
        } else {
            sb.append(String.format("%s.%s", outerVar, varName));
        }
        if (which.isPresent()) {
            sb.append(" #").append(which);
        }
        sb.append('}');
        return sb.toString();
    }
}
