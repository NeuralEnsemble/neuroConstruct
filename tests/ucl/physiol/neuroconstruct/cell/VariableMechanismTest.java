/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class VariableMechanismTest {

    public VariableMechanismTest() {
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
     * Test of getParams method, of class VariableMechanism.
     */
    @Test
    public void testGetParams() {
        System.out.println("getParams");
        VariableMechanism instance = new VariableMechanism();
        ArrayList<VariableParameter> expResult = null;
        ArrayList<VariableParameter> result = instance.getParams();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setParams method, of class VariableMechanism.
     */
    @Test
    public void testSetParams() {
        System.out.println("setParams");
        ArrayList<VariableParameter> params = null;
        VariableMechanism instance = new VariableMechanism();
        instance.setParams(params);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class VariableMechanism.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        VariableMechanism instance = new VariableMechanism();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class VariableMechanism.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        VariableMechanism instance = new VariableMechanism();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class VariableMechanism.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        VariableMechanism instance = new VariableMechanism();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class VariableMechanism.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        VariableMechanism.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}