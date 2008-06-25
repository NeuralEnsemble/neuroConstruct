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
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.DistalPref;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.Metric;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.ProximalPref;

/**
 *
 * @author padraig
 */
public class ParameterisedGroupTest {

    public ParameterisedGroupTest() {
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
     * Test of evaluateAt method, of class ParameterisedGroup.
     */
    @Test
    public void testEvaluateAt() throws Exception {
        System.out.println("evaluateAt");
        Cell cell = null;
        SegmentLocation location = null;
        ParameterisedGroup instance = null;
        double expResult = 0.0;
        double result = instance.evaluateAt(cell, location);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinValue method, of class ParameterisedGroup.
     */
    @Test
    public void testGetMinValue() throws Exception {
        System.out.println("getMinValue");
        Cell cell = null;
        ParameterisedGroup instance = null;
        double expResult = 0.0;
        double result = instance.getMinValue(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxValue method, of class ParameterisedGroup.
     */
    @Test
    public void testGetMaxValue() throws Exception {
        System.out.println("getMaxValue");
        Cell cell = null;
        ParameterisedGroup instance = null;
        double expResult = 0.0;
        double result = instance.getMaxValue(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class ParameterisedGroup.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        ParameterisedGroup instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class ParameterisedGroup.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        ParameterisedGroup instance = null;
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroup method, of class ParameterisedGroup.
     */
    @Test
    public void testGetGroup() {
        System.out.println("getGroup");
        ParameterisedGroup instance = null;
        String expResult = "";
        String result = instance.getGroup();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGroup method, of class ParameterisedGroup.
     */
    @Test
    public void testSetGroup() {
        System.out.println("setGroup");
        String group = "";
        ParameterisedGroup instance = null;
        instance.setGroup(group);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMetric method, of class ParameterisedGroup.
     */
    @Test
    public void testGetMetric() {
        System.out.println("getMetric");
        ParameterisedGroup instance = null;
        Metric expResult = null;
        Metric result = instance.getMetric();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMetric method, of class ParameterisedGroup.
     */
    @Test
    public void testSetMetric() {
        System.out.println("setMetric");
        Metric metric = null;
        ParameterisedGroup instance = null;
        instance.setMetric(metric);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDistalPref method, of class ParameterisedGroup.
     */
    @Test
    public void testGetDistalPref() {
        System.out.println("getDistalPref");
        ParameterisedGroup instance = null;
        DistalPref expResult = null;
        DistalPref result = instance.getDistalPref();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDistalPref method, of class ParameterisedGroup.
     */
    @Test
    public void testSetDistalPref() {
        System.out.println("setDistalPref");
        DistalPref distalPref = null;
        ParameterisedGroup instance = null;
        instance.setDistalPref(distalPref);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProximalPref method, of class ParameterisedGroup.
     */
    @Test
    public void testGetProximalPref() {
        System.out.println("getProximalPref");
        ParameterisedGroup instance = null;
        ProximalPref expResult = null;
        ProximalPref result = instance.getProximalPref();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProximalPref method, of class ParameterisedGroup.
     */
    @Test
    public void testSetProximalPref() {
        System.out.println("setProximalPref");
        ProximalPref proximalPref = null;
        ParameterisedGroup instance = null;
        instance.setProximalPref(proximalPref);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class ParameterisedGroup.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        ParameterisedGroup instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class ParameterisedGroup.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        ParameterisedGroup.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}