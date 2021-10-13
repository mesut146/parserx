package mesut.parserx.gen.ll;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Template;
import mesut.parserx.gen.Writer;
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
        LexerGenerator lexerGenerator = new LexerGenerator(tree);
        lexerGenerator.generate();
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
        Writer transWriter = new Writer();
        String indent = "        ";
        transWriter.print("\"");
        transWriter.print(LexerGenerator.makeOctal(idMap.lastTokenId));
        transWriter.print("\" +");
        transWriter.print("\n");
        for (int state = 0; state <= dfa.lastState; state++) {
            List<Transition> list = dfa.trans[state];
            transWriter.print(indent);
            transWriter.print("\"");
            if (list == null || list.isEmpty()) {
                transWriter.print(LexerGenerator.makeOctal(0));
            }
            else {
                transWriter.print(LexerGenerator.makeOctal(list.size()));
                for (Transition transition : list) {
                    transWriter.print(LexerGenerator.makeOctal(transition.input));
                    transWriter.print(LexerGenerator.makeOctal(transition.target));
                }
            }
            transWriter.print("\"");
            if (state <= dfa.lastState - 1) {
                transWriter.print(" +\n");
            }
        }
        template.set("trans", transWriter.getString());
    }

    private void makeids() {
        int[] idArr = new int[dfa.lastState + 1];//state->id
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
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
        Writer idWriter = new Writer();
        for (int state = dfa.initial; state <= dfa.lastState; state++) {
            idWriter.print(idArr[state]);
            if (state <= dfa.lastState - 1) {
                idWriter.print(",");
                if (state > dfa.initial && state % 20 == 0) {
                    idWriter.print("\n            ");
                }
            }
        }
        template.set("id_list", idWriter.getString());

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
        Writer nameWriter = new Writer();
        int i = 0;
        int column = 20;
        for (Map.Entry<Name, Integer> entry : tokens) {
            if (i > 0) {
                nameWriter.print(",");
                if (i % column == 0) {
                    nameWriter.print("\n");
                }
            }
            nameWriter.print("\"" + entry.getKey().name + "\"");
            i++;
        }
        template.set("name_list", nameWriter.getString());

        template.set("ruleOffset", "" + idMap.lastTokenId);
    }
}
