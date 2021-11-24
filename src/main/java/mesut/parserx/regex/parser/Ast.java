package mesut.parserx.regex.parser;

import java.util.List;
import java.util.ArrayList;

public class Ast{
    public static class rhs{
        public seq seq;
        public List<rhsg1> g1 = new ArrayList<>();

        public String toString(){
            StringBuilder sb = new StringBuilder("rhs{");
            sb.append(seq.toString());
            if(!g1.isEmpty()) sb.append(",");
            if(!g1.isEmpty()){
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            return sb.append("}").toString();
        }
    }
    public static class rhsg1{
        public Token BAR;
        public seq seq;

        public String toString(){
            StringBuilder sb = new StringBuilder("rhsg1{");
            sb.append("'" + BAR.value + "'");
            sb.append(",");
            sb.append(seq.toString());
            return sb.append("}").toString();
        }
    }
    public static class seq{
        public List<regex> regex = new ArrayList<>();

        public String toString(){
            StringBuilder sb = new StringBuilder("seq{");
            if(!regex.isEmpty()){
                sb.append('[');
                for(int i = 0;i < regex.size();i++){
                    sb.append(regex.get(i).toString());
                    if(i < regex.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            return sb.append("}").toString();
        }
    }
    public static class regex{
        public simple simple;
        public regexg1 g1;

        public String toString(){
            StringBuilder sb = new StringBuilder("regex{");
            sb.append(simple.toString());
            if(g1 != null) sb.append(",");
            sb.append(g1 == null?"":g1.toString());
            return sb.append("}").toString();
        }
    }
    public static class regexg1{
        public int which;
        public Token QUES;
        public Token STAR;
        public Token PLUS;

        public String toString(){
            StringBuilder sb = new StringBuilder("regexg1{");
            if(which == 1){
                sb.append("'" + QUES.value + "'");
            }
            else if(which == 2){
                sb.append("'" + STAR.value + "'");
            }
            else if(which == 3){
                sb.append("'" + PLUS.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class simple{
        public int which;
        public normalChar normalChar;
        public bracket bracket;
        Simple3 simple3;
        public static class Simple3{
                public Token LPAREN;
                public rhs rhs;
                public Token RPAREN;
                public String toString(){
                        StringBuilder sb = new StringBuilder();
                        sb.append("'" + LPAREN.value + "'");
                        sb.append(",");
                        sb.append(rhs.toString());
                        sb.append(",");
                        sb.append("'" + RPAREN.value + "'");
                        return sb.toString();
                }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("simple{");
            if(which == 1){
                sb.append(normalChar.toString());
            }
            else if(which == 2){
                sb.append(bracket.toString());
            }
            else if(which == 3){
                sb.append(simple3);
            }
            return sb.append("}").toString();
        }
    }
    public static class bracket{
        public Token BOPEN;
        public Token XOR;
        public List<range> range = new ArrayList<>();
        public Token BCLOSE;

        public String toString(){
            StringBuilder sb = new StringBuilder("bracket{");
            sb.append("'" + BOPEN.value + "'");
            if(XOR != null) sb.append(",");
            sb.append(XOR == null?"":"'" + XOR.value + "'");
            sb.append(",");
            if(!range.isEmpty()){
                sb.append('[');
                for(int i = 0;i < range.size();i++){
                    sb.append(range.get(i).toString());
                    if(i < range.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + BCLOSE.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class range{
        public rangeChar rangeChar;
        public rangeg1 g1;

        public String toString(){
            StringBuilder sb = new StringBuilder("range{");
            sb.append(rangeChar.toString());
            if(g1 != null) sb.append(",");
            sb.append(g1 == null?"":g1.toString());
            return sb.append("}").toString();
        }
    }
    public static class rangeg1{
        public Token MINUS;
        public rangeChar rangeChar;

        public String toString(){
            StringBuilder sb = new StringBuilder("rangeg1{");
            sb.append("'" + MINUS.value + "'");
            sb.append(",");
            sb.append(rangeChar.toString());
            return sb.append("}").toString();
        }
    }
    public static class normalChar{
        public int which;
        public Token CHAR;
        public Token ESCAPED;
        public Token MINUS;
        public Token DOT;

        public String toString(){
            StringBuilder sb = new StringBuilder("normalChar{");
            if(which == 1){
                sb.append("'" + CHAR.value + "'");
            }
            else if(which == 2){
                sb.append("'" + ESCAPED.value + "'");
            }
            else if(which == 3){
                sb.append("'" + MINUS.value + "'");
            }
            else if(which == 4){
                sb.append("'" + DOT.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class rangeChar{
        public int which;
        public Token CHAR;
        public Token ESCAPED;
        public Token STAR;
        public Token PLUS;
        public Token QUES;
        public Token BAR;
        public Token DOT;
        public Token LPAREN;
        public Token RPAREN;
        public Token XOR;
        public Token MINUS;
        public Token BOPEN;

        public String toString(){
            StringBuilder sb = new StringBuilder("rangeChar{");
            if(which == 1){
                sb.append("'" + CHAR.value + "'");
            }
            else if(which == 2){
                sb.append("'" + ESCAPED.value + "'");
            }
            else if(which == 3){
                sb.append("'" + STAR.value + "'");
            }
            else if(which == 4){
                sb.append("'" + PLUS.value + "'");
            }
            else if(which == 5){
                sb.append("'" + QUES.value + "'");
            }
            else if(which == 6){
                sb.append("'" + BAR.value + "'");
            }
            else if(which == 7){
                sb.append("'" + DOT.value + "'");
            }
            else if(which == 8){
                sb.append("'" + LPAREN.value + "'");
            }
            else if(which == 9){
                sb.append("'" + RPAREN.value + "'");
            }
            else if(which == 10){
                sb.append("'" + XOR.value + "'");
            }
            else if(which == 11){
                sb.append("'" + MINUS.value + "'");
            }
            else if(which == 12){
                sb.append("'" + BOPEN.value + "'");
            }
            return sb.append("}").toString();
        }
    }
}
