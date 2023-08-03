package mesut.parserx.dfa.parser;

import java.util.ArrayList;
import java.util.List;

public class Ast {
    public static class nfa {
        public Token nls;
        public startDecl startDecl;
        public Token nls2;
        public finalDecl finalDecl;
        public Token nls3;
        public List<trLine> trLine = new ArrayList<>();

        public String toString() {
            StringBuilder sb = new StringBuilder("nfa{");
            boolean first = true;
            if (nls != null) {
                sb.append("'").append(nls.value.replace("'", "\'")).append("'");
                first = false;
            }
            if (!first) {
                sb.append(", ");
            }
            sb.append(startDecl.toString());
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(nls2.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append(finalDecl.toString());
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(nls3.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append(trLine.toString());
            if (!trLine.isEmpty()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append('[');
                for (int i = 0; i < trLine.size(); i++) {
                    sb.append(trLine.get(i).toString());
                    if (i < trLine.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class trLine {
        public trLineg1 g1;
        public Token nls;

        public String toString() {
            StringBuilder sb = new StringBuilder("trLine{");
            boolean first = true;
            sb.append(g1.toString());
            first = false;
            if (nls != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("'").append(nls.value.replace("'", "\'")).append("'");
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class trLineg1 {
        public int which;
        Trlineg11 trArrow;
        Trlineg12 trSimple;

        public String toString() {
            StringBuilder sb = new StringBuilder("trLineg1#" + which + "{");
            if (which == 1) {
                sb.append(trArrow);
            } else if (which == 2) {
                sb.append(trSimple);
            }
            return sb.append("}").toString();
        }

        public static class Trlineg11 {
            public trArrow trArrow;
            trLineg1 holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(trArrow.toString());
                return sb.toString();
            }
        }

        public static class Trlineg12 {
            public trSimple trSimple;
            trLineg1 holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(trSimple.toString());
                return sb.toString();
            }
        }
    }

    public static class startDecl {
        public Token START;
        public Token EQ;
        public Token NUM;

        public String toString() {
            StringBuilder sb = new StringBuilder("startDecl{");
            boolean first = true;
            sb.append("'").append(START.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(EQ.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(NUM.value.replace("'", "\'")).append("'");
            return sb.append("}").toString();
        }
    }

    public static class finalDecl {
        public Token FINAL;
        public Token EQ;
        public finalList finalList;

        public String toString() {
            StringBuilder sb = new StringBuilder("finalDecl{");
            boolean first = true;
            sb.append("'").append(FINAL.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(EQ.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append(finalList.toString());
            return sb.append("}").toString();
        }
    }

    public static class finalList {
        public namedState namedState;
        public List<finalListg1> g1 = new ArrayList<>();

        public String toString() {
            StringBuilder sb = new StringBuilder("finalList{");
            boolean first = true;
            sb.append(namedState.toString());
            first = false;
            if (!g1.isEmpty()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append('[');
                for (int i = 0; i < g1.size(); i++) {
                    sb.append(g1.get(i).toString());
                    if (i < g1.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class finalListg1 {
        public Token COMMA;
        public namedState namedState;

        public String toString() {
            StringBuilder sb = new StringBuilder("finalListg1{");
            boolean first = true;
            sb.append("'").append(COMMA.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append(namedState.toString());
            return sb.append("}").toString();
        }
    }

    public static class namedState {
        public Token NUM;
        public namedStateg1 g1;

        public String toString() {
            StringBuilder sb = new StringBuilder("namedState{");
            boolean first = true;
            sb.append("'").append(NUM.value.replace("'", "\'")).append("'");
            first = false;
            if (g1 != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(g1.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class namedStateg1 {
        public Token LP;
        public Token IDENT;
        public Token RP;

        public String toString() {
            StringBuilder sb = new StringBuilder("namedStateg1{");
            boolean first = true;
            sb.append("'").append(LP.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(IDENT.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(RP.value.replace("'", "\'")).append("'");
            return sb.append("}").toString();
        }
    }

    public static class trArrow {
        public Token NUM;
        public Token ARROW;
        public Token NUM2;
        public trArrowg1 g1;

        public String toString() {
            StringBuilder sb = new StringBuilder("trArrow{");
            boolean first = true;
            sb.append("'").append(NUM.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(ARROW.value.replace("'", "\'")).append("'");
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(NUM2.value.replace("'", "\'")).append("'");
            if (g1 != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(g1.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class trArrowg1 {
        public Token COMMA;
        public INPUT INPUT;

        public String toString() {
            StringBuilder sb = new StringBuilder("trArrowg1{");
            boolean first = true;
            sb.append("'").append(COMMA.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append(INPUT.toString());
            return sb.append("}").toString();
        }
    }

    public static class trSimple {
        public Token NUM;
        public Token NUM2;
        public INPUT INPUT;

        public String toString() {
            StringBuilder sb = new StringBuilder("trSimple{");
            boolean first = true;
            sb.append("'").append(NUM.value.replace("'", "\'")).append("'");
            first = false;
            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(NUM2.value.replace("'", "\'")).append("'");
            if (INPUT != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(INPUT.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }

    public static class INPUT {
        public int which;
        Input1 BRACKET;
        Input2 IDENT;
        Input3 ANY;
        Input4 NUM;

        public String toString() {
            StringBuilder sb = new StringBuilder("INPUT#" + which + "{");
            if (which == 1) {
                sb.append(BRACKET);
            } else if (which == 2) {
                sb.append(IDENT);
            } else if (which == 3) {
                sb.append(ANY);
            } else if (which == 4) {
                sb.append(NUM);
            }
            return sb.append("}").toString();
        }

        public static class Input1 {
            public Token BRACKET;
            INPUT holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(BRACKET.value.replace("'", "\'")).append("'");
                return sb.toString();
            }
        }

        public static class Input2 {
            public Token IDENT;
            INPUT holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(IDENT.value.replace("'", "\'")).append("'");
                return sb.toString();
            }
        }

        public static class Input3 {
            public Token ANY;
            INPUT holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(ANY.value.replace("'", "\'")).append("'");
                return sb.toString();
            }
        }

        public static class Input4 {
            public Token NUM;
            INPUT holder;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(NUM.value.replace("'", "\'")).append("'");
                return sb.toString();
            }
        }
    }
}
