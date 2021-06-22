package parser;

import org.junit.Test;

import java.util.Stack;

public class Lrr {
    static int[] rhs_sizes = {0, 1, 3, 3, 1};
    static int[][] table = {{2, 3, 0, 0, 0, 1},
            {0, 0, 0, 0, Integer.MAX_VALUE, 0},
            {0, 0, 4, -1, -1, 0},
            {2, 3, 0, 0, 5},
            {2, 3, 0, 0, 0, 6},
            {0, 0, 0, 7, 0, 0},
            {0, 0, 0, -2, -2, 0},
            {0, 0, 0, -3, -3, 0}};
    Stack<Symbol> stack = new Stack<>();
    Stack<Integer> states = new Stack<>();
    Symbol[] tokens = {new Symbol(0), new Symbol(2), new Symbol(0), new Symbol(4)};
    int pos = 0;

    @Test
    public void test() {
        parse();
    }

    public void parse() {
        Symbol symbol = tokens[pos++];
        states.push(0);//initial state
        int action = table[states.peek()][symbol.id];

        while (true) {
            if (action == 0) {
                System.out.println("error");
                return;
            }
            if (action == Integer.MAX_VALUE) {
                System.out.println("accept");
                return;
            }
            if (action > 0) {
                //shift
                stack.push(symbol);
                states.push(action);
                symbol = tokens[pos++];
                action = table[states.peek()][symbol.id];
            }
            else {
                //-action is rule index
                int size = rhs_sizes[-action];
                while (size-- > 0) {
                    stack.pop();
                    states.pop();
                }
                symbol = new Symbol(-action);
                stack.push(symbol);
                int from = states.peek();
                action = table[from][symbol.id];
                System.out.println("reduced " + (-action));
            }
        }
    }
}

//a token or a rule
class Symbol {
    public int id;
    //$token_class$ token;
    String name;

    public Symbol(int id) {
        this.id = id;
    }
}
