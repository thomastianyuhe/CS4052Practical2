package modelChecker;

import formula.pathFormula.*;
import formula.stateFormula.*;
import model.Model;
import model.State;
import model.Transition;

import javax.swing.*;
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
        ArrayList<State> initialStates = new ArrayList<>();
        for(State state: states){
            if(state.isInit()){
                initialStates.add(state);
            }
        }

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
                return newStateFormula;
            }else if(pathFormula instanceof Until){
                StateFormula left = ((Until) pathFormula).left;
                Set<String> leftActions = ((Until) pathFormula).getLeftActions();
                StateFormula right = ((Until) pathFormula).right;
                Set<String> rightActions = ((Until) pathFormula).getRightActions();
                StateFormula newStateFormula = new Or(new ThereExists(new Always(new Not(right), rightActions)), new ThereExists(new Until(new Not(right), new And(new Not(right), new Not(left)), leftActions, rightActions)));
                return newStateFormula;
            }else if(pathFormula instanceof Always){
                StateFormula subStateFormula = ((Always) pathFormula).stateFormula;
                Set<String> actions = ((Always) pathFormula).getActions();
                StateFormula newStateFormula = new Not(new ThereExists(new Eventually(new Not(subStateFormula), , )));
            }else if(pathFormula instanceof Eventually){
                StateFormula subStateFormula = ((Eventually) pathFormula).stateFormula;
                Set<String> leftActions = ((Eventually) pathFormula).getLeftActions();
                Set<String> rightActions = ((Eventually) pathFormula).getRightActions();
                StateFormula newStateFormula = new Not(new ThereExists(new Always(new Not(subStateFormula),  )));
            }

        }

    }
}
