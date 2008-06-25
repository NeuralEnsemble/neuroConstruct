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

/**
 *
 * @author padraig
 */
public class MechParameterTest {

    public MechParameterTest() {
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
     * Test of clone method, of class MechParameter.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        MechParameter instance = new MechParameter();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MechParameter.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MechParameter instance = new MechParameter();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class MechParameter.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        MechParameter instance = new MechParameter();
        float expResult = 0.0F;
        float result = instance.getValue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class MechParameter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        MechParameter instance = new MechParameter();
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class MechParameter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        MechParameter instance = new MechParameter();
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class MechParameter.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        MechParameter instance = new MechParameter();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class MechParameter.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        float value = 0.0F;
        MechParameter instance = new MechParameter();
        instance.setValue(value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class MechParameter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MechParameter instance = new MechParameter();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class MechParameter.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        MechParameter.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}