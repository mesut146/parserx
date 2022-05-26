package mesut.parserx.gen.ll;

import mesut.parserx.nodes.*;
import mesut.parserx.gen.*;
import mesut.parserx.gen.targets.*;
import mesut.parserx.gen.lr.*;
import mesut.parserx.gen.transform.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cc extends BaseVisitor<Void, Void>{
	Tree tree;
	CodeWriter w = new CodeWriter(true);
	JavaRecDescent rec;
	
	public void gen(){
		for(RuleDecl decl : tree.rules){
			Node rhs = decl.rhs;
			rhs.accept(this, null);
		}
	}
	
	public Void visitOr(Or or, Void arg){
		for(int i = 0;i < or.size();i++){
			Node ch = or.get(i);
			if(hasCommon(ch, or)){
				//advanced lookahead
			}else{
				//simple switch or if
			}
		}
		return null;
	}
	
	boolean hasCommon(Node ch, Or or){
		for(Node n : or){
			if(n != ch && common(n, ch)) return true;
		}
		return false;
	}
	
	boolean common(Node s1, Node s2){
        return new FactorHelper(tree, new Factor(tree)).common(s1, s2) != null;
    }
}