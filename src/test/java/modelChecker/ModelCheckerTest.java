package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import model.State;
import model.Transition;
import org.junit.Test;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import modelChecker.ModelChecker;
import modelChecker.SimpleModelChecker;
import model.Model;

public class ModelCheckerTest {

    /*q
     * An example of how to set up and call the model building methods and make
     * a call to the model checker itself. The contents of model.json,
     * constraint1.json and ctl.json are just examples, you need to add new
     * models and formulas for the mutual exclusion task.
     */
    @Test
    public void buildAndCheckModel() {
        try {
            Model model = Model.parseModel("src/test/resources/model1.json");

            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraint1.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/ctl1.json").parse();

            ModelChecker mc = new SimpleModelChecker();


            Transition[] transitions = model.getTransitions();
            Set<String> allActions= new HashSet<>();
            for(Transition transition: transitions){
                Collections.addAll(allActions, transition.getActions());
            }
            System.out.println(allActions.toArray());
            StateFormula formula = ((SimpleModelChecker) mc).convertToENF(query, allActions);
            String test = formula.toString();
            System.out.println(test);
            // TO IMPLEMENT
            assertFalse(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
//
//    public void main(String[] args){
//        buildAndCheckModel();
//    }

    @Test
    public void modelOneDoesNotSatisfyCTLOne(){
        try{
            Model model = Model.parseModel("src/test/resources/model1.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraint1.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/ctl1.json").parse();
            ModelChecker mc = new SimpleModelChecker();
            boolean result =  mc.check(model, fairnessConstraint, query);
            assertFalse(result);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelTwoDoesSatisfyCTLTwo() {
        try{
            Model model = Model.parseModel("src/test/resources/model2.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraint1.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/ctl2.json").parse();
            ModelChecker mc = new SimpleModelChecker();
            boolean result =  mc.check(model, fairnessConstraint, query);
            assertFalse(result);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }


}
