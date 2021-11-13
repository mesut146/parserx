package mesut.parserx.gen.ll;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Template;
import mesut.parserx.gen.lr.IdMap;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class LLDfaParserGen {
    Tree tree;
    NFA dfa;
    IdMap idMap;
    Template template;

    public LLDfaParserGen(Tree tree) {
        this.tree = tree;
    }

    public void gen() throws IOException {
        LexerGenerator lexerGenerator = LexerGenerator.gen(tree, "java");
        idMap = lexerGenerator.idMap;

        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.build();
        NFA nfa = builder.dfa;
        dfa = nfa.dfa();

        template = new Template("lldfa.java.template");
        template.set("package", tree.options.packageName == null ? "" : "package " + tree.options.packageName + ";");
        template.set("token_class", tree.options.tokenClass);
        template.set("parser_class", tree.options.parserClass);
        template.set("lexer_class", tree.options.lexerClass);

        makeids();
        makeTrans();
        template.set("final_list", NodeList.join(LexerGenerator.makeIntArr(dfa.accepting), ","));

        File file = new File(tree.options.outDir, tree.options.parserClass + ".java");
        Utils.write(template.toString(), file);
    }

    void makeTrans() {
        StringBuilder transWriter = new StringBuilder();
        String indent = "        ";
        transWriter.append("\"");
        transWriter.append(LexerGenerator.makeOctal(idMap.lastTokenId));
        transWriter.append("\" +");
        transWriter.append("\n");
        for (int state = 0; state <= dfa.lastState; state++) {
            List<Transition> list = dfa.trans[state];
            transWriter.append(indent);
            transWriter.append("\"");
            if (list == null || list.isEmpty()) {
                transWriter.append(LexerGenerator.makeOctal(0));
            }
            else {
                transWriter.append(LexerGenerator.makeOctal(list.size()));
                for (Transition transition : list) {
                    transWriter.append(LexerGenerator.makeOctal(transition.input));
                    transWriter.append(LexerGenerator.makeOctal(transition.target));
                }
            }
            transWriter.append("\"");
            if (state <= dfa.lastState - 1) {
                transWriter.append(" +\n");
            }
        }
        template.set("trans", transWriter.toString());
    }

    private void makeids() {
        int[] idArr = new int[dfa.lastState + 1];//state->id
        for (int state : dfa.it()) {
            //make id for token
            List<String> names = dfa.names[state];
            if (names != null && dfa.isAccepting(state)) {
                if (names.size() != 1) {
                    throw new RuntimeException("only one token per state");
                }
                idArr[state] = idMap.getId(new Name(names.get(0), false));
            }
        }
        //id list
        StringBuilder idWriter = new StringBuilder();
        for (int state : dfa.it()) {
            idWriter.append(idArr[state]);
            if (state <= dfa.lastState - 1) {
                idWriter.append(",");
                if (state > 0 && state % 20 == 0) {
                    idWriter.append("\n            ");
                }
            }
        }
        template.set("id_list", idWriter.toString());

        //sort rules by id
        TreeSet<Map.Entry<Name, Integer>> tokens = new TreeSet<>(new Comparator<Map.Entry<Name, Integer>>() {
            @Override
            public int compare(Map.Entry<Name, Integer> o1, Map.Entry<Name, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<Name, Integer> entry : idMap.map.entrySet()) {
            if (entry.getKey().isRule()) {
                tokens.add(entry);
            }
        }
        //write
        StringBuilder nameWriter = new StringBuilder();
        int i = 0;
        int column = 20;
        for (Map.Entry<Name, Integer> entry : tokens) {
            if (i > 0) {
                nameWriter.append(",");
                if (i % column == 0) {
                    nameWriter.append("\n");
                }
            }
            nameWriter.append("\"" + entry.getKey().name + "\"");
            i++;
        }
        template.set("name_list", nameWriter.toString());
        template.set("ruleOffset", "" + idMap.lastTokenId);
    }
}
