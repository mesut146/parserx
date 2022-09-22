package mesut.parserx.gen.lr;

import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

//table driven parser gen
public class LrCodeGen {
    public Options options;
    public LrDFAGen gen;
    LrType type;
    IdMap idMap;
    Template template;
    TreeSet<RuleDecl> ruleSet = new TreeSet<>(Comparator.comparingInt(rd -> rd.index));

    public LrCodeGen(Tree tree, LrType type) {
        this.gen = new LrDFAGen(tree, type);
        this.options = tree.options;
        this.type = type;
    }

    public void gen() throws IOException {
        gen.generate();
        gen.checkAndReport();
        idMap = LexerGenerator.gen(gen.tree, Lang.JAVA).idMap;

        template = new Template("lalr1.java.template");
        template.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        template.set("parser_class", options.parserClass);
        template.set("lexer_class", options.lexerClass);
        template.set("lexer_method", options.lexerFunction);
        template.set("token_class", options.tokenClass);
        for (var list : gen.treeInfo.ruleMap.values()) {
            ruleSet.addAll(list);
        }
        ruleId();
        writeTable();
        names();
        alt();
        isLoop();
        isStar();
        var file = new File(options.outDir, options.parserClass + ".java");
        Utils.write(template.toString(), file);
        idMap.writeSym(options);

        var symbolTemplate = new Template("Node.java.template");
        symbolTemplate.set("package", options.packageName == null ? "" : "package " + options.packageName + ";\n");
        symbolTemplate.set("token_class", options.tokenClass);
        Utils.write(symbolTemplate.toString(), new File(options.outDir, "Node.java"));
    }

    void names() {
        var sb = new StringBuilder();
        int i = 0;
        for (var rd : ruleSet) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"');
            if (rd.transformInfo != null) {
                sb.append(rd.transformInfo.orgName);

            }
            else {
                sb.append(rd.getName());
            }
            sb.append('"');
            i++;
        }
        template.set("names", sb.toString());
    }

    //map rule ids to symbol ids
    void ruleId() {
        var ruleIds = new StringBuilder();
        var sizes = new StringBuilder();

        int i = 0;
        for (var decl : ruleSet) {
            ruleIds.append(idMap.getId(decl.ref));
            if (LrItem.isEpsilon(decl)) {
                sizes.append(0);
            }
            else {
                sizes.append(decl.rhs.asSequence().size());
            }
            if (i < ruleSet.size() - 1) {
                ruleIds.append(", ");
                sizes.append(", ");
            }
            i++;
        }
        template.set("ruleIds", ruleIds.toString());
        template.set("rhs_sizes", sizes.toString());
    }

    void alt() {
        var sb = new StringBuilder();
        int i = 0;
        for (var rd : ruleSet) {
            if (i > 0) {
                sb.append(", ");
            }
            if (!rd.isAlt() || (rd.transformInfo != null && (rd.transformInfo.isOpt || rd.transformInfo.isStar || rd.transformInfo.isPlus))) {
                sb.append(0);
            }
            else {
                sb.append(rd.which);
            }
            i++;
        }
        template.set("alt_map", sb.toString());
    }


    void isLoop() {
        var sb = new StringBuilder();
        int i = 0;
        for (var rd : ruleSet) {
            if (i > 0) {
                sb.append(", ");
            }
            if (rd.transformInfo != null && rd.transformInfo.isPlus && rd.rhs.isSequence() && rd.rhs.asSequence().size() == 2) {
                sb.append("true");
            }
            else {
                sb.append("false");
            }
            i++;
        }
        template.set("isPlus", sb.toString());
    }

    void isStar() {
        var sb = new StringBuilder();
        int i = 0;
        for (var rd : ruleSet) {
            if (i > 0) {
                sb.append(", ");
            }
            if (rd.transformInfo != null && rd.transformInfo.isStar && rd.rhs.asSequence().get(0).isName()) {
                sb.append("true");
            }
            else {
                sb.append("false");
            }
            i++;
        }
        template.set("isStar", sb.toString());
    }

    void writeTable() {
        var sb = new StringBuilder();
        sb.append("\"");

        sb.append(pack(gen.lastId + 1));//state count,width
        sb.append(pack(idMap.lastId + 1));//symbol count,height

        //write accept
        sb.append(pack(gen.acc.stateId));
        sb.append(pack(1));
        for (var item : gen.acc.kernel) {
            if (item.isReduce(gen.tree) && item.rule.ref.equals(gen.start.ref) && item.lookAhead.contains(LrDFAGen.dollar)) {
                sb.append(pack(idMap.getId(IdMap.EOF)));
                break;
            }
        }

        for (var set : gen.itemSets) {
            //write shifts
            List<? extends LrTransition> shifts = set.transitions;
            sb.append(pack(shifts.size()));//shift count
            for (var tr : shifts) {
                sb.append(pack(idMap.getId(tr.symbol)));
                int action = tr.target.stateId;
                sb.append(pack(action));
            }
            //write reduces
            var list = set.getReduce();
            int count = list.size();//reduce count
            if (gen.acc == set) {
                count--;
            }
            sb.append(pack(count));
            for (var reduce : list) {
                if (reduce.rule.ref.equals(gen.start.ref)) {
                    continue;
                }
                int action = reduce.rule.index;
                sb.append(pack(action));
                sb.append(pack(reduce.lookAhead.size()));
                for (var sym : reduce.lookAhead) {
                    sb.append(pack(idMap.getId(sym)));
                }
            }
        }

        sb.append("\"");
        template.set("table_packed", sb.toString());
    }

    String pack(int i) {
        if (i < 0) {
            return "-" + UnicodeUtils.escapeUnicode(-i);
        }
        return UnicodeUtils.escapeUnicode(i);
    }

}
