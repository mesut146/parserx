package dfa;

import java.util.ArrayList;
import java.util.List;

public class Transition {
    int state;//from
    List<Integer> states = new ArrayList<>();//to
    int symbol;//input index
}
