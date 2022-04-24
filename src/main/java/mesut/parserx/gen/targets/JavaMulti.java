package mesut.parserx.gen.targets;

import mesut.parserx.gen.*;
import mesut.parserx.gen.ll.*;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static mesut.parserx.gen.ll.RecDescent.loopLimit;
import static mesut.parserx.gen.ll.RecDescent.tokens;

public class JavaMulti {

    public static boolean debug = false;
    public Options options;
    Tree tree;
    CodeWriter code = new CodeWriter(true);
    RuleDecl curRule;
    int flagCount;
    int firstCount;

    public JavaMulti(Tree tree) {
        this.tree = tree;
        options = tree.options;
    }

    public void gen() throws IOException {
        if (options.packageName != null) {
            code.append("package %s;", options.packageName);
            code.append("");
        }
        code.append("import java.util.List;");
        code.append("import java.util.ArrayList;");
        code.append("import java.io.IOException;");
        if (options.packageName != null) {
            code.append("import %s.%s;", options.packageName, options.astClass);
        }
        code.append("");
        code.append("public class %s{", options.parserClass);
        code.append("%s lexer;", options.lexerClass);
        code.append("List<%s> list = new ArrayList<>();", options.tokenClass);
        code.append("%s la;", options.tokenClass);
        code.append("int pos;");
        code.append("");

        code.append("public %s(%s lexer) throws IOException{", options.parserClass, options.lexerClass);

        code.all("this.lexer = lexer;\nfill();\n}");
        code.append("");

		writeFill();
        writeConsume();

        for (RuleDecl decl : tree.rules) {
            curRule = decl;
            gen(decl);
        }
        code.append("}");

        File file = new File(options.outDir, options.parserClass + ".java");

        Utils.write(code.get(), file);
        genTokenType();
    }
    
    void writeFill(){
    	code.append("void fill() throws IOException {");
    	code.append("while(true){");
    	code.append("%s t = lexer.%s();", options.tokenClass, options.lexerFunction);
    	code.append("list.add(t);");
    	code.append("if(t.type == 0) break;");
        code.append("}");
        code.append("la = list.get(0);");
        code.append("}");
    }

    void writeConsume() {
        code.append("%s consume(int type, String name){", options.tokenClass);
        code.append("if(la.type != type){");
        code.append("throw new RuntimeException(\"unexpected token: \" + la + \" expecting: \" + name);");
        code.all("}");
       code.append("%s old = la;", options.tokenClass);
       code.append("la = list.get(++pos);", options.tokenClass);
        code.append("return old;");
        code.append("}");
    }

    void genTokenType() throws IOException {
        CodeWriter c = new CodeWriter(true);
        if (options.packageName != null) {
            c.append("package %s;", options.packageName);
            c.append("");
        }
        c.append("public class %s{", tokens);
        c.append("public static final int EOF = 0;");
        int id = 1;
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            //if (decl.isSkip) continue;
            c.append("public static final int %s = %d;", decl.name, id);
            id++;
        }
        c.append("}");
        File file = new File(options.outDir, tokens + ".java");
        Utils.write(c.get(), file);
    }

    String peekExpr() {
        return "la.type";
    }

    void gen(RuleDecl decl) {
        Type type = decl.retType;
        code.append("public %s %s(){", type, decl.baseName());
        code.append("%s res = new %s();", type, type);
        flagCount = 0;
        firstCount = 0;

        write(decl.rhs);

        code.append("return res;");
        code.append("}");
        code.append("");
        if(decl.rhs.isOr()){
        	int i=0;
        	for(Node ch : decl.rhs.asOr()){
        		if(!RecDescent.isSimple(ch))
        			genAlt(ch, i);
        		i++;
        	}
        }
    }
    
    void genAlt(Node ch, int i){
    	Type type = ch.astInfo.nodeType;
        code.append("public %s %s_%d(){", type, curRule.baseName(), i+1);
        code.append("%s res = new %s();", type, type);
        flagCount = 0;
        firstCount = 0;

        write(ch);

        code.append("return res;");
        code.append("}");
    }

    void write(Node node) {
        if (node.astInfo.which != -1) {
            code.all(node.astInfo.writeWhich());
        }
        if (node.astInfo.nodeType != null) {
            code.all(node.astInfo.writeNode());
        }
        if (node.astInfo.substitution) {
            code.append("%s.%s = %s;", node.astInfo.outerVar, node.astInfo.subVar, node.astInfo.varName);
        }
        if (node.isOr()) {
            Or or = node.asOr();
            writeOr(or);
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            write(group.node);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                write(s.get(i));
            }
        }
        else if (node.isName()) {
            Name name = node.asName();
            writeName(name);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            writeRegex(regex);
        }
        else if (node.isEpsilon()) {
            if (node.astInfo.isFactored) {
                code.append("%s = %s;", node.astInfo.varName, node.astInfo.factor.varName);
            }
        }
    }

    Type getType(Name name) {
        if (name.isToken) return new Type(options.tokenClass);
        return tree.getRule(name).retType;
    }

    String tokenConsumer(Name token) {
        return String.format("consume(%s.%s, \"%s\")", tokens, token.name, token.name);
    }

    private void writeRegex(Regex regex) {
        Set<Name> set = FirstSet.tokens(regex, tree);
        if (regex.isOptional()) {
            if (set.size() <= loopLimit) {
                code.append("if(%s){", loopExpr(set));
                write(regex.node);
                code.append("}");
            }
            else {
                beginSwitch(set);
                write(regex.node);
                endSwitch("");
            }
            return;
        }
        Name name = regex.node.asName();
        if (set.isEmpty()) {
            //factored loop
            code.append("for(int i = 0;i < %s.size();i++){", regex.astInfo.factor.varName);
            code.append("%s.%s.add(%s(%s.get(i)));", regex.astInfo.outerVar, name.astInfo.varName, name.name, regex.astInfo.factor.varName);
            code.append("}");
            return;
        }

        //regex.node.astInfo.isInLoop = regex.isStar() || regex.isPlus();
        if (regex.isStar()) {
            if (set.size() <= loopLimit) {
                    code.append("while(%s){", loopExpr(set));
                    write(name);
                    code.append("}");
                
            }
            else {
                flagCount++;
                String flagStr = "flag";
                if (flagCount > 1) flagStr += flagCount;
                code.append(String.format("boolean %s = true;", flagStr));
                code.append(String.format("while(%s){", flagStr));
                String def = flagStr + " = false;\n";
                beginSwitch(set);
                write(name);
                endSwitch(def);
                code.append("}");
            }
        }
        else {
            //plus
            if (set.size() <= loopLimit) {
                if (regex.astInfo.isFactor) {
                    code.append("List<%s> %s = new ArrayList<>();", getType(name), regex.astInfo.varName);
                    code.append("do{");
                    String consumer = name.isToken ? tokenConsumer(name) : name + "()";
                    code.append("%s.add(%s);", regex.astInfo.varName, consumer);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
                else {
                    code.append("do{");
                    write(name);
                    code.down();
                    code.append("}while(%s);", loopExpr(set));
                }
            }
            else {
                flagCount++;
                firstCount++;
                String flagStr = "flag";
                String firstStr = "first";
                if (flagCount > 1) flagStr += flagCount;
                if (firstCount > 1) firstStr += firstCount;
                code.append("boolean %s = true;", flagStr);
                code.append("boolean %s = true;", firstStr);
                code.append("while(%s){", flagStr);
                String def = String.format("if(!%s)  %s = false;\n" + "else  throw new RuntimeException(\"unexpected token: \"+la);", firstStr, flagStr);
                beginSwitch(set);
                write(name);
                endSwitch(def);
                code.append("first = false;\n");
                code.append("}");
            }
        }
    }

    String loopExpr(Set<Name> set) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            Name tok = it.next();
            sb.append(String.format("%s == %s.%s", peekExpr(), tokens, tok.name));
            if (it.hasNext()) {
                sb.append(" || ");
            }
        }
        return sb.toString();
    }

    String withArgs(Name name) {
        StringBuilder args = new StringBuilder();
        if (!name.args.isEmpty()) {
            for (int i = 0; i < name.args.size(); i++) {
                args.append(name.args.get(i).astInfo.varName);
                if (i < name.args.size() - 1) {
                    args.append(",");
                }
            }
        }
        return name.name + "(" + args + ")";
    }

    private void writeName(Name name) {
        if (name.astInfo.isFactored && name.astInfo.isFactor) {
            //factor names may be different?
            if (!name.astInfo.varName.equals(name.astInfo.factor.varName) && !name.astInfo.isInLoop) {
                //redeclare so it's available to rest
                code.append("%s %s = %s;", getType(name), name.astInfo.varName, name.astInfo.factor.varName);
            }
            //!name.astInfo.isInLoop
            return;
        }
        if (name.astInfo.isFactored) {
            //no consume
            if (name.astInfo.isInLoop) {
                if (name.astInfo.isPrimary) throw new RuntimeException("todo");
                code.append("%s.%s.add(%s);", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
            }
            else {
                if (name.astInfo.isPrimary) {
                    code.append("%s = %s;", name.astInfo.varName, name.astInfo.factor.varName);
                }
                else {
                    code.append("%s.%s = %s;", name.astInfo.outerVar, name.astInfo.varName, name.astInfo.factor.varName);
                }
            }
        }
        else if (name.astInfo.isPrimary) {
            code.append("%s = %s;", name.astInfo.varName, withArgs(name));
        }
        else if (name.astInfo.isSecondary) {
            code.append("%s = %s;", name.astInfo.varName, withArgs(name));
            //code.append("%s = %s(%s);", name.astInfo.outerVar, name.name, name.astInfo.outerVar);
        }
        else {
            String rhs = rhs(name);
            
            if (name.astInfo.isFactor) {
                code.append("%s %s = %s;", getType(name), name.astInfo.varName, rhs);
            }
            else {
                if (name.astInfo.isInLoop) {
                    code.append("%s.%s.add(%s);", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
                else {
                    code.append("%s.%s = %s;", name.astInfo.outerVar, name.astInfo.varName, rhs);
                }
            }
        }
    }
    
    String rhs(Name name){
    	if (name.isRule()) {
                return withArgs(name);
        }
        else {
        	return tokenConsumer(name);
        }
    }

    private void writeOr(Or or) {
        code.append("int marked;");
        for (int i = 0; i < or.size(); i++) {
            Node ch = or.get(i);
            code.append("marked = pos;");
            code.append("try{");
            if(RecDescent.isSimple(ch)){
            	if(ch.isName()){
            		String rhs = rhs(ch.asName());
            		code.append("res.%s = %s;", ch.astInfo.varName, rhs);
            	}else{
            		Regex r = ch.asRegex();
            		writeRegex(r);
            	}
            }else{
            	code.append("res.%s = %s_%d();", ch.astInfo.varName, curRule.baseName(), i + 1);
            }
            code.append("res.which = %d;", i + 1);
            code.append("return res;");
            code.all("}catch(RuntimeException re){");
            code.append("pos = marked;");
            code.append("}");
        }
       
            StringBuilder arr = new StringBuilder();
            boolean first = true;
            for (Name tok : FirstSet.tokens(or, tree)) {
                if (!first) {
                    arr.append(",");
                }
                arr.append(tok);
                first = false;
            }
            code.append("throw new RuntimeException(\"expecting one of [%s] got: \"+la);", arr);
        code.append("}");
    }

    void beginSwitch(Set<Name> set) {
        code.append("switch(" + peekExpr() + "){");
        for (Name token : set) {
            code.append("case %s.%s:", tokens, token.name);
        }
        code.append("{");
    }

    void endSwitch(String def) {
        code.all("}\nbreak;");
        if (!def.isEmpty()) {
            code.append("default:{");
            code.all(def);
            code.append("}");
        }
        code.append("}");
    }
}
