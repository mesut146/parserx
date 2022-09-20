package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.List;

public class TokenBlock {
    public List<TokenDecl> tokens = new ArrayList<>();
    public List<ModeBlock> modeBlocks = new ArrayList<>();

    public TokenDecl getToken(String name) {
        for (var decl : tokens) {
            if (decl.name.equals(name)) {
                return decl;
            }
        }
        for (var mb : modeBlocks) {
            for (var decl : mb.tokens) {
                if (decl.name.equals(name)) {
                    return decl;
                }
            }
        }
        return null;
    }
}
