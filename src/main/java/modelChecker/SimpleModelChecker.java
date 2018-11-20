package modelChecker;

import formula.pathFormula.Next;
import formula.pathFormula.PathFormula;
import formula.stateFormula.*;
import model.Model;
import model.State;
import model.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class SimpleModelChecker implements ModelChecker {

    private HashMap<String, State> stateHashMap;
    private HashMap<String, ArrayList<Transition>> transitionHashMap;
    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // TODO Auto-generated method stub
        State[] states = model.getStates();
        stateHashMap = new HashMap<>();
        transitionHashMap = new HashMap<>();
        for(State state: states){
            stateHashMap.put(state.getName(), state);
        }
        Transition[] transitions = model.getTransitions();
        for(Transition transition: transitions){
            if(!transitionHashMap.containsKey(transition.getSource())){
                transitionHashMap.put(transition.getSource(), new ArrayList<>());
            }
            transitionHashMap.get(transition.getSource()).add(transition);
        }

        //start from initial states
        State[] initialStates =  Arrays.stream(states).filter(x -> x.isInit()).toArray(State[]::new);
        for(State initialState: initialStates){
            recursiveExplorer(initialState.getName());
        }

        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean recursiveExplorer(String currentStateName){ //may extend the param

        ArrayList<Transition> transitions = transitionHashMap.get(currentStateName);
        for(Transition transition: transitions){
            String[] transitionActions = transition.getActions();
            State currentState = stateHashMap.get(currentStateName);
            String[] currentStateLabels = currentState.getLabel();
            //TODO do the checks here maybe??

            String nextStateName = transition.getTarget();
            State nextState = stateHashMap.get(nextStateName);
            return recursiveExplorer(nextStateName);
        }

        return true;
    }

//    private boolean evaluateFormula(StateFormula formula, State state, String[] actions){
//        if(formula instanceof And){
//            return evaluateFormula(((And) formula).left, state, actions) && evaluateFormula(((And) formula).right, state, actions);
//        }else if(formula instanceof AtomicProp){
//            for (String label: state.getLabel()){
//                if(label.equals(((AtomicProp) formula).label)){
//                    return true;
//                }
//            }
//            return false;
//        }else if(formula instanceof Not){
//            return !evaluateFormula(formula, state, actions);
//        }
//        return false;
//    }

    private StateFormula convertToENF(StateFormula stateFormula){
        if(stateFormula instanceof ForAll){
            PathFormula pathFormula = ((ForAll) stateFormula).pathFormula;
            if(pathFormula instanceof Next){
                StateFormula subStateFormula = ((Next) pathFormula).stateFormula;
                Set<String> actions = ((Next) pathFormula).getActions();
                StateFormula newStateFormula = new Not(new ThereExists(new Next(new Not(subStateFormula), actions)));
            }
        }

    }
}
