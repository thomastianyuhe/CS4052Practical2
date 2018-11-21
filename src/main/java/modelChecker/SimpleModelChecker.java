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
        Set<String> allActions= new HashSet<>();
        for(Transition transition: transitions){
            Collections.addAll(allActions, transition.getActions());
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
