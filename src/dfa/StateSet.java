package dfa;

import java.util.*;
public class StateSet
{
    public List<Integer> states;
    
    public StateSet(){
        states=new ArrayList<>();
    }
    
    public void addState(int state){
        states.add(state);
    }
    
    public int get(int index){
        return states.get(index);
    }
    
    public void remove(int state){
        states.remove((Integer)state);
    }
    
    public void clear(){
        states.clear();
    }
}
