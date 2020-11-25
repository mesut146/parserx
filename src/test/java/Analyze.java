import dfa.Alphabet;
import dfa.DFA;
import dfa.Transition;

import java.util.*;

public class Analyze {
    DFA dfa;

    public Analyze(DFA dfa) {
        this.dfa = dfa;
    }

    public void analyze() {
        Set<InputTarget> inputTargets = new HashSet<>();
        int count = 0;
        Map<Integer, Integer> inputFreq = new HashMap<>();
        for (int state = 0; state <= dfa.lastState; state++) {
            if (dfa.hasTransitions(state)) {
                List<Transition> list = dfa.trans[state];
                count += list.size();
                for (Transition transition : list) {
                    int x = 0;
                    if (inputFreq.containsKey(transition.input)) {
                        x = inputFreq.get(transition.input);
                    }
                    inputFreq.put(transition.input, x + 1);
                    inputTargets.add(new InputTarget(transition.input, transition.target, dfa.getAlphabet()));
                }
            }
        }
        List<Map.Entry<Integer, Integer>> vals = new ArrayList<>(inputFreq.entrySet());
        Collections.sort(vals, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });
        for (Map.Entry<Integer, Integer> entry : vals) {
            System.out.printf("input=%s count=%d\n", dfa.getAlphabet().getRange(entry.getKey()), entry.getValue());
        }
        System.out.println("input target=" + inputTargets);

        System.out.println("transitions = " + count);
    }

    static class InputTarget {
        int input;
        int target;
        Alphabet alphabet;

        public InputTarget(int input, int target, Alphabet alphabet) {
            this.input = input;
            this.target = target;
            this.alphabet = alphabet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InputTarget that = (InputTarget) o;
            return input == that.input &&
                    target == that.target;
        }

        @Override
        public int hashCode() {
            return Objects.hash(input, target);
        }

        public String toString() {
            return alphabet.getRange(input) + "->" + target;
        }
    }
}
