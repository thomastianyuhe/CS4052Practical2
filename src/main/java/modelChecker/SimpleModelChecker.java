package modelChecker;

import com.sun.org.apache.xpath.internal.operations.Bool;
import formula.pathFormula.*;
import formula.stateFormula.*;
import model.Model;
import model.State;
import model.Transition;

import javax.swing.*;
import java.security.AllPermission;
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
        Set<String> allActions= new HashSet<>();
        for(Transition transition: transitions){
            Collections.addAll(allActions, transition.getActions());
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
        HashSet<State> initialStates = new HashSet<>();
        for(State state: states){
            if(state.isInit()){
                initialStates.add(state);
            }
        }

        StateFormula formula = new And(query, constraint);

        return modelChecking(formula, initialStates);
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean modelChecking(StateFormula CTL, HashSet<State> currentStates) {
        if(CTL instanceof ThereExists){
            if (((ThereExists) CTL).pathFormula instanceof Always){
                return checkAlways(((ThereExists) CTL).pathFormula, currentStates, new HashMap<State, Boolean>());
            } else if (((ThereExists) CTL).pathFormula instanceof Next){
                return checkNext(((ThereExists) CTL).pathFormula, currentStates);
            } else if (((ThereExists) CTL).pathFormula instanceof Until){
                return checkUntil(((ThereExists) CTL).pathFormula, currentStates, new HashSet<State>(), false);
            }
        } else if (CTL instanceof And){
            return modelChecking(((And) CTL).left, currentStates) && modelChecking(((Or) CTL).right, currentStates);
        } else if (CTL instanceof Or){
            return modelChecking(((Or) CTL).left, currentStates) || modelChecking(((Or) CTL).right, currentStates);
        } else if (CTL instanceof Not){
            HashSet<State> notStates = new HashSet<>();
            for(State s: currentStates){
                if(!stateCheck(((Not) CTL).stateFormula, s)){
                    return true;
                }
            }
        } else if (CTL instanceof AtomicProp){
            for(State s: currentStates){
                String[] labels = s.getLabel();
                for(String l: labels){
                    String stateLabel = l.trim().replaceAll("”|“", "");
                    String ctlLabel = ((AtomicProp) CTL).label.trim().replaceAll("”", "");
                    if (stateLabel.equals(ctlLabel)){
                        return true;
                    }
                }
            }
            return false;

        } else if (CTL instanceof BoolProp){
            return ((BoolProp) CTL).value;
        }
        System.out.println("Should not reach here");
        return false;
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

    public StateFormula convertToENF(StateFormula stateFormula, Set<String> allActions){
//        StateFormula ENFFormula = null;
//        StateFormula newStateFormula = null;
        if(stateFormula instanceof ForAll){
            PathFormula pathFormula = ((ForAll) stateFormula).pathFormula;
            if(pathFormula instanceof Next){
                StateFormula subStateFormula = ((Next) pathFormula).stateFormula;
                Set<String> actions = ((Next) pathFormula).getActions();
                Set<String> compActions = new HashSet<>(allActions);
                compActions.removeAll(actions);
                System.out.println(pathFormula.toString());
                System.out.println(subStateFormula);
                return new And(
                                                    new Not(new ThereExists(new Next(new BoolProp(true), compActions))),
                                                    new Not(new ThereExists(new Next(new Not(convertToENF(subStateFormula, allActions)), actions))));
            }else if(pathFormula instanceof Until){
                StateFormula left = ((Until) pathFormula).left;
                Set<String> leftActions = ((Until) pathFormula).getLeftActions();
                StateFormula right = ((Until) pathFormula).right;
                Set<String> rightActions = ((Until) pathFormula).getRightActions();
                Set<String> compRightActions = new HashSet<>(allActions);
                compRightActions.removeAll(rightActions);
                Set<String> compLeftActions = new HashSet<>(allActions);
                compLeftActions.removeAll(leftActions);
                 return new And(
                                                    new Not(new ThereExists(new Until(left, right, leftActions, compRightActions))),
                                                    new And(
                                                            new Not(new ThereExists(new Until(left, new Not(right), leftActions, rightActions))),
                                                            new Not(new ThereExists(new Until(left, new Not(left), leftActions, leftActions)))
                                                    )

                );
            }else if(pathFormula instanceof Always){
                StateFormula subStateFormula = ((Always) pathFormula).stateFormula;
                Set<String> actions = ((Always) pathFormula).getActions();
                Set<String> compActions = new HashSet<>(allActions);
                compActions.removeAll(actions);
                return new And(
                                                    new Not(new ThereExists(new Until(new BoolProp(true), new BoolProp(true), actions, compActions))),
                                                    new Not(new ThereExists(new Until(new BoolProp(true), new Not(convertToENF(subStateFormula,allActions)), actions, actions)))
                );
            }else if(pathFormula instanceof Eventually){
                StateFormula subStateFormula = ((Eventually) pathFormula).stateFormula;
                Set<String> rightActions = ((Eventually) pathFormula).getRightActions();
                Set<String> leftActions = ((Eventually) pathFormula).getLeftActions();
                Set<String> compRightActions = new HashSet<>(allActions);
                compRightActions.removeAll(rightActions);
                return new And(
                                                    new Not(new ThereExists(new Until(new BoolProp(true), new Not(convertToENF(subStateFormula, allActions)), leftActions, rightActions))),
                                                    new Not(new ThereExists(new Until(new BoolProp(true), new BoolProp(true), leftActions, compRightActions)))

                );

            }
        }else{
            if(stateFormula instanceof And){
                StateFormula left = ((And) stateFormula).left;
                StateFormula right = ((And) stateFormula).right;
                return new And(convertToENF(left, allActions), convertToENF(right, allActions));
            }else if(stateFormula instanceof Not){
                return new Not(convertToENF(((Not) stateFormula).stateFormula, allActions));
            }else if(stateFormula instanceof Or){
                StateFormula left = ((Or) stateFormula).left;
                StateFormula right = ((Or) stateFormula).right;
                return new Or(convertToENF(left, allActions), convertToENF(right, allActions));
            }else if(stateFormula instanceof BoolProp){
                return stateFormula;
            }else if(stateFormula instanceof AtomicProp){
                return stateFormula;
            }else if(stateFormula instanceof ThereExists){
                PathFormula pathFormula = ((ThereExists) stateFormula).pathFormula;
                if(pathFormula instanceof Always){
                    StateFormula subStateFormula = ((Always) pathFormula).stateFormula;
                    Set<String> actions = ((Always) pathFormula).getActions();
                    return new ThereExists(new Always(convertToENF(subStateFormula, allActions), actions));
                }else if(pathFormula instanceof Eventually) {
                    StateFormula subStateFormula = ((Eventually) pathFormula).stateFormula;
                    Set<String> leftActions = ((Eventually) pathFormula).getLeftActions();
                    Set<String> rightActions = ((Eventually) pathFormula).getRightActions();
                    return new ThereExists(new Eventually(convertToENF(subStateFormula, allActions), leftActions, rightActions));
                }else if(pathFormula instanceof Next) {
                    StateFormula subStateFormula = ((Next) pathFormula).stateFormula;
                    Set<String> actions = ((Next) pathFormula).getActions();
                    return new ThereExists(new Next(convertToENF(subStateFormula, allActions), actions));
                }else if(pathFormula instanceof Until){
                    StateFormula left = ((Until) pathFormula).left;
                    StateFormula right = ((Until) pathFormula).right;
                    Set<String> leftActions = ((Until) pathFormula).getLeftActions();
                    Set<String> rightActions = ((Until) pathFormula).getRightActions();
                    return new ThereExists(new Until(convertToENF(left, allActions), convertToENF(right, allActions),leftActions, rightActions));
                }

            }
        }
        return null;
    }
//
//    public static void main(String[] args){
//
//    }
}
