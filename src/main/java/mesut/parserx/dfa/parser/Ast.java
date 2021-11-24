package mesut.parserx.dfa.parser;

import java.util.List;
import java.util.ArrayList;

public class Ast{
    public static class nfa{
        public startDecl startDecl;
        public Token nls;
        public finalDecl finalDecl;
        public Token nls2;
        public List<trLine> trLine = new ArrayList<>();

        public String toString(){
            StringBuilder sb = new StringBuilder("nfa{");
            sb.append(startDecl.toString());
            sb.append(",");
            sb.append("'" + nls.value + "'");
            sb.append(",");
            sb.append(finalDecl.toString());
            sb.append(",");
            sb.append("'" + nls2.value + "'");
            sb.append(",");
            if(!trLine.isEmpty()){
                sb.append('[');
                for(int i = 0;i < trLine.size();i++){
                    sb.append(trLine.get(i).toString());
                    if(i < trLine.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            return sb.append("}").toString();
        }
    }
    public static class trLine{
        public trLineg1 g1;
        public Token nls;

        public String toString(){
            StringBuilder sb = new StringBuilder("trLine{");
            sb.append(g1.toString());
            if(nls != null) sb.append(",");
            sb.append(nls == null?"":"'" + nls.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class trLineg1{
        public int which;
        public trArrow trArrow;
        public trSimple trSimple;

        public String toString(){
            StringBuilder sb = new StringBuilder("trLineg1{");
            if(which == 1){
                sb.append(trArrow.toString());
            }
            else if(which == 2){
                sb.append(trSimple.toString());
            }
            return sb.append("}").toString();
        }
    }
    public static class startDecl{
        public Token START;
        public Token EQ;
        public Token NUM;

        public String toString(){
            StringBuilder sb = new StringBuilder("startDecl{");
            sb.append("'" + START.value + "'");
            sb.append(",");
            sb.append("'" + EQ.value + "'");
            sb.append(",");
            sb.append("'" + NUM.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class finalDecl{
        public Token FINAL;
        public Token EQ;
        public finalList finalList;

        public String toString(){
            StringBuilder sb = new StringBuilder("finalDecl{");
            sb.append("'" + FINAL.value + "'");
            sb.append(",");
            sb.append("'" + EQ.value + "'");
            sb.append(",");
            sb.append(finalList.toString());
            return sb.append("}").toString();
        }
    }
    public static class finalList{
        public int which;
        public List<namedState> namedState = new ArrayList<>();
        Finallist2 finallist2;
        public static class Finallist2{
                public namedState namedState;
                public List<finalListg1> g1 = new ArrayList<>();
                public String toString(){
                        StringBuilder sb = new StringBuilder();
                        sb.append(namedState.toString());
                        if(!g1.isEmpty()) sb.append(",");
                        if(!g1.isEmpty()){
                                sb.append('[');
                                for(int i = 0;i < g1.size();i++){
                                        sb.append(g1.get(i).toString());
                                        if(i < g1.size() - 1) sb.append(",");
                                }
                                sb.append(']');
                        }
                        return sb.toString();
                }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("finalList{");
            if(which == 1){
                sb.append(namedState);
            }
            else if(which == 2){
                sb.append(finallist2);
            }
            return sb.append("}").toString();
        }
    }
    public static class finalListg1{
        public Token COMMA;
        public namedState namedState;

        public String toString(){
            StringBuilder sb = new StringBuilder("finalListg1{");
            sb.append("'" + COMMA.value + "'");
            sb.append(",");
            sb.append(namedState.toString());
            return sb.append("}").toString();
        }
    }
    public static class namedState{
        public Token NUM;
        public namedStateg1 g1;

        public String toString(){
            StringBuilder sb = new StringBuilder("namedState{");
            sb.append("'" + NUM.value + "'");
            if(g1 != null) sb.append(",");
            sb.append(g1 == null?"":g1.toString());
            return sb.append("}").toString();
        }
    }
    public static class namedStateg1{
        public Token LP;
        public Token IDENT;
        public Token RP;

        public String toString(){
            StringBuilder sb = new StringBuilder("namedStateg1{");
            sb.append("'" + LP.value + "'");
            sb.append(",");
            sb.append("'" + IDENT.value + "'");
            sb.append(",");
            sb.append("'" + RP.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class trArrow{
        public Token NUM;
        public Token ARROW;
        public Token NUM2;
        public trArrowg1 g1;

        public String toString(){
            StringBuilder sb = new StringBuilder("trArrow{");
            sb.append("'" + NUM.value + "'");
            sb.append(",");
            sb.append("'" + ARROW.value + "'");
            sb.append(",");
            sb.append("'" + NUM2.value + "'");
            if(g1 != null) sb.append(",");
            sb.append(g1 == null?"":g1.toString());
            return sb.append("}").toString();
        }
    }
    public static class trArrowg1{
        public Token COMMA;
        public INPUT INPUT;

        public String toString(){
            StringBuilder sb = new StringBuilder("trArrowg1{");
            sb.append("'" + COMMA.value + "'");
            sb.append(",");
            sb.append(INPUT.toString());
            return sb.append("}").toString();
        }
    }
    public static class trSimple{
        public Token NUM;
        public Token NUM2;
        public INPUT INPUT;

        public String toString(){
            StringBuilder sb = new StringBuilder("trSimple{");
            sb.append("'" + NUM.value + "'");
            sb.append(",");
            sb.append("'" + NUM2.value + "'");
            if(INPUT != null) sb.append(",");
            sb.append(INPUT == null?"":INPUT.toString());
            return sb.append("}").toString();
        }
    }
    public static class INPUT{
        public int which;
        public Token BRACKET;
        public Token IDENT;
        public Token ANY;

        public String toString(){
            StringBuilder sb = new StringBuilder("INPUT{");
            if(which == 1){
                sb.append("'" + BRACKET.value + "'");
            }
            else if(which == 2){
                sb.append("'" + IDENT.value + "'");
            }
            else if(which == 3){
                sb.append("'" + ANY.value + "'");
            }
            return sb.append("}").toString();
        }
    }
}
