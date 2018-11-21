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
    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // TODO Auto-generated method stub
        State[] states = model.getStates();
        stateHashMap = new HashMap<>();
        transitionHashMap = new HashMap<>();
        backwardsTransitions = new HashMap<>();

        for (State state : states) {
            stateHashMap.put(state.getName(), state);
        }

        Transition[] transitions = model.getTransitions();
        Set<String> allActions = new HashSet<>();
        for (Transition transition : transitions) {
            Collections.addAll(allActions, transition.getActions());
            if (!transitionHashMap.containsKey(transition.getSource())) {
                transitionHashMap.put(transition.getSource(), new ArrayList<Transition>());
            }
            transitionHashMap.get(transition.getSource()).add(transition);

            if (!backwardsTransitions.containsKey(transition.getTarget())) {
                backwardsTransitions.put(transition.getTarget(), new ArrayList<Transition>());
            }
            backwardsTransitions.get(transition.getTarget()).add(transition);
        }

        //start from initial states
        HashSet<State> initialStates = new HashSet<>();
        for (State state : states) {
            if (state.isInit()) {
                initialStates.add(state);
            }
        }


        constraint = convertToENF(constraint, allActions);

        StateFormula formula = convertToENF(query, allActions);

        for (State s : initialStates) {
            if (modelChecking(formula, s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean modelChecking(StateFormula CTL, State currentState) {
        if(CTL instanceof ThereExists){
            if (((ThereExists) CTL).pathFormula instanceof Always){
                return checkAlways(((ThereExists) CTL).pathFormula, currentState, new HashMap<State, Boolean>());
            } else if (((ThereExists) CTL).pathFormula instanceof Next){
                return checkNext(((ThereExists) CTL).pathFormula, currentState);
            } else if (((ThereExists) CTL).pathFormula instanceof Until){
                return checkUntil(((ThereExists) CTL).pathFormula, currentState, new HashSet<State>());
            }
        } else if (CTL instanceof And){
            return modelChecking(((And) CTL).left, currentState) && modelChecking(((And) CTL).right, currentState);
        } else if (CTL instanceof Or){
            return modelChecking(((Or) CTL).left, currentState) || modelChecking(((Or) CTL).right, currentState);
        } else if (CTL instanceof Not){
            if(!modelChecking(((Not) CTL).stateFormula, currentState)){
                return true;
            }
            return false;
        } else if (CTL instanceof AtomicProp){
            // Check all of the labels of the current state
            String[] labels = currentState.getLabel();
            for(String l: labels){
                String stateLabel = l.trim().replaceAll("”|“", "");
                String ctlLabel = ((AtomicProp) CTL).label.trim().replaceAll("”|“", "");
                if (stateLabel.equals(ctlLabel)){
                    return true;
                }
            }
            return false;

        } else if (CTL instanceof BoolProp){
            return ((BoolProp) CTL).value;
        }
        System.out.println("Should not reach here");
        return false;
    }

    private boolean checkNext(PathFormula CTL, State currentState){

        ArrayList<Transition> transitions = transitionHashMap.get(currentState.getName());
        for(Transition t: transitions) {
            for(String action: t.getActions()){
                if (((Next) CTL).getActions().contains(action)){
                    State next = stateHashMap.get(t.getTarget());
                    if (modelChecking(((Next) CTL).stateFormula, next)){
                        return true;
                    }
                    break;
                }
            }
            if(modelChecking(((Next) CTL).stateFormula, stateHashMap.get(t.getTarget()))){
                return true;
            }
        }
        return false;
    }

    private boolean checkUntil(PathFormula CTL, State currentState, HashSet<State> seen){
        seen.add(currentState);
        ArrayList<Transition> transitions = transitionHashMap.get(currentState.getName());
        for(Transition t: transitions){
            for(String action: t.getActions()){
                if (((Until) CTL).getRightActions().contains(action)){
                    State next = stateHashMap.get(t.getTarget());
                    if (seen.contains(next)) {
                        return false;
                    } else if (modelChecking(((Until) CTL).right, next)){
                        return true;
                    }
                }

                if (((Until) CTL).getLeftActions().contains(action)){
                    State next = stateHashMap.get(t.getTarget());
                    if (seen.contains(next)) {
                        return false;
                    } else if (checkUntil(CTL, next, seen)){
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    // Loops
    private boolean checkAlways(PathFormula CTL, State currentState, HashMap<State, Boolean> seen){
        // Find the states satifying the formula
        if (seen.containsKey(currentState)){
            return seen.get(currentState);
        }

        if (modelChecking(((Always) CTL).stateFormula, currentState)) {
            seen.put(currentState, Boolean.TRUE);

            ArrayList<Transition> transitions = transitionHashMap.get(currentState.getName());
            for (Transition t : transitions) {

                for(String action: t.getActions()){
                    if (((Always) CTL).getActions().contains(action)){
                        State next = stateHashMap.get(t.getTarget());
                        if (checkAlways(CTL, next, seen)) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        seen.put(currentState, Boolean.FALSE);
        return false;
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
