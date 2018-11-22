package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import model.Transition;
import org.junit.Test;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
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
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void convertToENFTest(){
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
            String enf = formula.toString();
            assertEquals(enf, "");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

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
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model2.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraint2.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/ctl2.json").parse();
            boolean result =  mc.check(model, fairnessConstraint, query);
            assertTrue(result);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeAGTest() {
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail = new FormulaParser("src/test/resources/model3/model3AGFail.json").parse();
            boolean resultFail =  mc.check(model, fairnessConstraint, queryFail);
            assertFalse(resultFail);
            StateFormula queryPass = new FormulaParser("src/test/resources/model3/model3AGPass.json").parse();
            boolean resultPass =  mc.check(model, fairnessConstraint, queryPass);
            assertTrue(resultPass);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeAUTest() {
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail = new FormulaParser("src/test/resources/model3/model3AUFail.json").parse();
            boolean resultFail =  mc.check(model, fairnessConstraint, queryFail);
            assertFalse(resultFail);
            StateFormula queryPass = new FormulaParser("src/test/resources/model3/model3AUPass.json").parse();
            boolean resultPass =  mc.check(model, fairnessConstraint, queryPass);
            assertTrue(resultPass);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeAXTest() {
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail1 = new FormulaParser("src/test/resources/model3/model3AXFail1.json").parse();
            boolean resultFail1 =  mc.check(model, fairnessConstraint, queryFail1);
            assertFalse(resultFail1);
            StateFormula queryFail2 = new FormulaParser("src/test/resources/model3/model3AXFail2.json").parse();
            boolean resultFail2 =  mc.check(model, fairnessConstraint, queryFail2);
            assertFalse(resultFail2);
            StateFormula queryPass1 = new FormulaParser("src/test/resources/model3/model3AXPass1.json").parse();
            boolean resultPass1 =  mc.check(model, fairnessConstraint, queryPass1);
            assertTrue(resultPass1);
            StateFormula queryPass2 = new FormulaParser("src/test/resources/model3/model3AXPass2.json").parse();
            boolean resultPass2 =  mc.check(model, fairnessConstraint, queryPass2);
            assertTrue(resultPass2);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeEFTest(){
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail = new FormulaParser("src/test/resources/model3/model3EFFail.json").parse();
            boolean resultFail =  mc.check(model, fairnessConstraint, queryFail);
            assertFalse(resultFail);
            StateFormula queryPass = new FormulaParser("src/test/resources/model3/model3EFPass.json").parse();
            boolean resultPass =  mc.check(model, fairnessConstraint, queryPass);
            assertTrue(resultPass);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeEGTest(){
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail = new FormulaParser("src/test/resources/model3/model3EGFail.json").parse();
            boolean resultFail =  mc.check(model, fairnessConstraint, queryFail);
            assertFalse(resultFail);
            StateFormula queryPass = new FormulaParser("src/test/resources/model3/model3EGPass.json").parse();
            boolean resultPass =  mc.check(model, fairnessConstraint, queryPass);
            assertTrue(resultPass);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeEUTest() {
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail1 = new FormulaParser("src/test/resources/model3/model3EUFail1.json").parse();
            boolean resultFail1 =  mc.check(model, fairnessConstraint, queryFail1);
            assertFalse(resultFail1);
            StateFormula queryFail2 = new FormulaParser("src/test/resources/model3/model3EUFail2.json").parse();
            boolean resultFail2 =  mc.check(model, fairnessConstraint, queryFail2);
            assertFalse(resultFail2);
            StateFormula queryPass = new FormulaParser("src/test/resources/model3/model3EXPass.json").parse();
            boolean resultPass =  mc.check(model, fairnessConstraint, queryPass);
            assertTrue(resultPass);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void modelThreeEXTest() {
        ModelChecker mc = new SimpleModelChecker();
        try{
            Model model = Model.parseModel("src/test/resources/model3.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraintTrue.json").parse();
            StateFormula queryFail1 = new FormulaParser("src/test/resources/model3/model3EXFail1.json").parse();
            boolean resultFail1 =  mc.check(model, fairnessConstraint, queryFail1);
            assertFalse(resultFail1);
            StateFormula queryFail2 = new FormulaParser("src/test/resources/model3/model3EXFail2.json").parse();
            boolean resultFail2 =  mc.check(model, fairnessConstraint, queryFail2);
            assertFalse(resultFail2);
            StateFormula queryPass1 = new FormulaParser("src/test/resources/model3/model3EXPass1.json").parse();
            boolean resultPass1 =  mc.check(model, fairnessConstraint, queryPass1);
            assertTrue(resultPass1);
            StateFormula queryPass2 = new FormulaParser("src/test/resources/model3/model3EXPass2.json").parse();
            boolean resultPass2 =  mc.check(model, fairnessConstraint, queryPass2);
            assertTrue(resultPass2);
        }catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
//    @Test
//    public void modelMutualExclusionDoes


}
