/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationUnit;

/**
 *
 * @author padraig
 */
public class VariableParameterTest {

    public VariableParameterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of evaluateAt method, of class VariableParameter.
     */
    @Test
    public void testEvaluateAt() throws Exception {
        System.out.println("evaluateAt");
        double paramVal = 0.0;
        VariableParameter instance = null;
        double expResult = 0.0;
        double result = instance.evaluateAt(paramVal);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class VariableParameter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        VariableParameter instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class VariableParameter.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        VariableParameter instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class VariableParameter.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        VariableParameter instance = null;
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExpression method, of class VariableParameter.
     */
    @Test
    public void testGetExpression() {
        System.out.println("getExpression");
        VariableParameter instance = null;
        EquationUnit expResult = null;
        EquationUnit result = instance.getExpression();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExpression method, of class VariableParameter.
     */
    @Test
    public void testSetExpression() {
        System.out.println("setExpression");
        EquationUnit expression = null;
        VariableParameter instance = null;
        instance.setExpression(expression);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}