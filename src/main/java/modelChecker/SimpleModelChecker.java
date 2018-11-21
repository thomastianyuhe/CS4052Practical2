package modelChecker;

import com.sun.org.apache.xpath.internal.operations.Bool;
import formula.pathFormula.*;
import formula.stateFormula.*;
import model.Model;
import model.State;
import model.Transition;

import java.util.*;

public class SimpleModelChecker implements ModelChecker {

    private HashMap<String, State> stateHashMap;
    private HashMap<String, ArrayList<Transition>> transitionHashMap;
    private HashMap<String, ArrayList<Transition>> backwardsTransitions;
    private HashMap<State, Boolean> checkedStates;
    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // TODO Auto-generated method stub
        State[] states = model.getStates();
        stateHashMap = new HashMap<>();
        transitionHashMap = new HashMap<>();
        backwardsTransitions = new HashMap<>();

        for(State state: states){
            stateHashMap.put(state.getName(), state);
        }

        Transition[] transitions = model.getTransitions();
        for(Transition transition: transitions){
            if(!transitionHashMap.containsKey(transition.getSource())){
                transitionHashMap.put(transition.getSource(), new ArrayList<Transition>());
            }
            transitionHashMap.get(transition.getSource()).add(transition);

            if(!backwardsTransitions.containsKey(transition.getTarget())){
                backwardsTransitions.put(transition.getTarget(), new ArrayList<Transition>());
            }
            backwardsTransitions.get(transition.getTarget()).add(transition);
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

//    private HashSet<State> modelChecking(StateFormula CTL, HashSet<State> currentStates, State[] checkedStates) {
//        if(CTL instanceof ThereExists){
//            if (((ThereExists) CTL).pathFormula instanceof Always){
//                return checkAlways(((ThereExists) CTL).pathFormula, currentStates, checkedStates);
//            } else if (((ThereExists) CTL).pathFormula instanceof Next){
//                return checkNext(((ThereExists) CTL).pathFormula, currentStates, checkedStates);
//            } else if (((ThereExists) CTL).pathFormula instanceof Until){
//                return checkUntil(((ThereExists) CTL).pathFormula, currentStates, checkedStates, false);
//            }
//        } else if (CTL instanceof And){
//            HashSet newStates = modelChecking(((Or) CTL).left, currentStates, checkedStates);
//            newStates.retainAll(modelChecking(((Or) CTL).right, currentStates, checkedStates));
//            return newStates;
//        } else if (CTL instanceof Or){
//            HashSet newStates = modelChecking(((Or) CTL).left, currentStates, checkedStates);
//            newStates.addAll(modelChecking(((Or) CTL).right, currentStates, checkedStates));
//            return newStates;
//        } else if (CTL instanceof Not){
//            HashSet nextStates = modelChecking(((Not) CTL).stateFormula, currentStates, checkedStates);
//            currentStates.removeAll(nextStates);
//            return currentStates;
//        } else if (CTL instanceof AtomicProp){
//            HashSet<State> correctStates = new HashSet<>();
//            for(State s: currentStates){
//                String[] labels = s.getLabel();
//                for(String l: labels){
//                    if (l.equals(((AtomicProp) CTL).label)){
//                        correctStates.add(s);
//                    }
//                }
//            }
//            return correctStates;
//
//        } else if (CTL instanceof BoolProp){
//            if (((BoolProp) CTL).value){
//                return currentStates;
//            } else {
//                return new HashSet<>();
//            }
//        }
//    }

    private boolean modelChecking(StateFormula CTL, HashSet<State> currentStates) {
        if(CTL instanceof ThereExists){
            if (((ThereExists) CTL).pathFormula instanceof Always){
                return checkAlways(((ThereExists) CTL).pathFormula, currentStates, new HashSet<State>());
            } else if (((ThereExists) CTL).pathFormula instanceof Next){
                return checkNext(((ThereExists) CTL).pathFormula, currentStates);
            } else if (((ThereExists) CTL).pathFormula instanceof Until){
                return checkUntil(((ThereExists) CTL).pathFormula, currentStates, new HashSet<State>(), false);
            }
        } else if (CTL instanceof And){
            return modelChecking(((Or) CTL).left, currentStates) && modelChecking(((Or) CTL).right, currentStates;
        } else if (CTL instanceof Or){
            return modelChecking(((Or) CTL).left, currentStates) || modelChecking(((Or) CTL).right, currentStates);
        } else if (CTL instanceof Not){
            HashSet<State> notStates = new HashSet<>();
            for(State s: currentStates){
                if(!stateCheck(((Not) CTL).stateFormula, s){
                    return true;
                }
            }
        } else if (CTL instanceof AtomicProp){
            for(State s: currentStates){
                String[] labels = s.getLabel();
                for(String l: labels){
                    if (l.equals(((AtomicProp) CTL).label)){
                        return true;
                    }
                }
            }
            return false;

        } else if (CTL instanceof BoolProp){
            return ((BoolProp) CTL).value;
        }
    }

    private boolean stateCheck(StateFormula CTL, State state){
        HashSet<State> stateHashset = new HashSet<State>();
        stateHashset.add(state);
        return modelChecking(CTL, stateHashset);
    }

    private boolean checkNext(PathFormula CTL, HashSet<State> currentStates){
        // Finding the next states
        HashSet<State> nextStates = new HashSet<>();
        for(State s: currentStates){
            ArrayList<Transition> transitions = transitionHashMap.get(s.getName());
            for(Transition t: transitions){
                // TODO
                // Add check actions
                t.getActions();
                nextStates.add(stateHashMap.get(t.getTarget()));
            }
        }
        // Will check if they satisfy the formula
        return modelChecking(((Next) CTL).stateFormula, nextStates);
    }

    private boolean checkUntil(PathFormula CTL, HashSet<State> currentStates, HashSet<State> seen, boolean leftSatisfied){
        // If previous states satisfy the left formula
        HashSet<State> correctStates = new HashSet<>();
        if(leftSatisfied) {
            // Check if any of the current states evaluate to true for right formula, return true if so
            // Else find the current states that satisfy the left formula
            for (State s : currentStates) {
                if (!seen.contains(s)) {
                    if (stateCheck(((Until) CTL).right, s)) {
                        return true;
                    }

                    if (stateCheck(((Until) CTL).left, s)) {
                        correctStates.add(s);
                    }

                    seen.add(s);
                }
            }
        } else {
            for(State s: currentStates){
                if(stateCheck(((Until) CTL).left, s)){
                    correctStates.add(s);
                }

                seen.add(s);
            }
        }

        // If no states satisfy the either formula
        if (correctStates.isEmpty()) {
            return false;
        }

        // Find the next states from the correct states
        HashSet<State> nextStates = new HashSet<>();
        for(State s: correctStates){
            ArrayList<Transition> transitions = transitionHashMap.get(s.getName());
            for(Transition t: transitions){
                // TODO
                // Add check actions

                t.getActions();

                State next = stateHashMap.get(t.getTarget());
                nextStates.add(next);
            }
        }
        return checkUntil(CTL, nextStates, seen, true);
    }

    // Loops
    private boolean checkAlways(PathFormula CTL, HashSet<State> currentStates, HashMap<State, Boolean> seen){
        // Find the states satifying the formula
        HashSet<State> correctStates = new HashSet<>();
        HashSet<State> incorrectStates = new HashSet<>();
        for(State s: currentStates){
            // If the state has been seen before and evaluated to true you have found a correct loop
            // If a state has been found incorrect, ignore it
            if (seen.containsKey(s)){
                if (seen.get(s)){
                    return true;
                }
            } else if(stateCheck(((Always) CTL).stateFormula, s)){
                correctStates.add(s);
            } else {
                incorrectStates.add(s);
            }
        }

        // If there are no correct states then the formula will not always be true
        if (correctStates.isEmpty()){
            return false;
        } else {
            for(State s: correctStates){
                seen.put(s, Boolean.TRUE);
            }
            for(State s: incorrectStates){
                seen.put(s, Boolean.FALSE);
            }
        }

        // Find the next states from the correct states
        HashSet<State> nextStates = new HashSet<>();
        for (State s : correctStates) {
            ArrayList<Transition> transitions = transitionHashMap.get(s.getName());
            for (Transition t : transitions) {
                // TODO
                // Add check actions
                t.getActions();

                State next = stateHashMap.get(t.getTarget());
                nextStates.add(next);
            }
        }
        // If you reach a terminal state and all previous have evaluated to true
        if (nextStates.isEmpty()){
            return true;
        } else {
            return checkAlways(CTL, correctStates, seen);
        }
    }


    private boolean recursiveExplorer(String currentStateName){

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
