/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class ChannelMechanismTest {

    public ChannelMechanismTest() {
        System.out.println("Creating the ChannelMechanismTest...");
    }

    @Before
    public void setUp() 
    {
        System.out.println("Setting up the ChannelMechanismTest...");
    }

    @After
    public void tearDown() 
    {
        
        System.out.println("Tearing down the ChannelMechanismTest...");
    }
    
    @Test public void testDumb() 
    {
        System.out.println("Carrying out a dumb test...");
        fail("D'oh!");
        System.out.println("Carrying out a dumb test...");
        String h = null;
        h.length();
        
        assertTrue(2==3);
    }

    /**
     * Test of clone method, of class ChannelMechanism.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        ChannelMechanism instance = new ChannelMechanism();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class ChannelMechanism.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object otherObj = null;
        ChannelMechanism instance = new ChannelMechanism();
        boolean expResult = false;
        boolean result = instance.equals(otherObj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class ChannelMechanism.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toNiceString method, of class ChannelMechanism.
     */
    @Test
    public void testToNiceString() {
        System.out.println("toNiceString");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.toNiceString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUniqueName method, of class ChannelMechanism.
     */
    @Test
    public void testGetUniqueName() {
        System.out.println("getUniqueName");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.getUniqueName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtraParamsDesc method, of class ChannelMechanism.
     */
    @Test
    public void testGetExtraParamsDesc() {
        System.out.println("getExtraParamsDesc");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.getExtraParamsDesc();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtraParamsBracket method, of class ChannelMechanism.
     */
    @Test
    public void testGetExtraParamsBracket() {
        System.out.println("getExtraParamsBracket");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.getExtraParamsBracket();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExtraParam method, of class ChannelMechanism.
     */
    @Test
    public void testSetExtraParam() {
        System.out.println("setExtraParam");
        String name = "";
        float value = 0.0F;
        ChannelMechanism instance = new ChannelMechanism();
        instance.setExtraParam(name, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class ChannelMechanism.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        ChannelMechanism.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDensity method, of class ChannelMechanism.
     */
    @Test
    public void testGetDensity() {
        System.out.println("getDensity");
        ChannelMechanism instance = new ChannelMechanism();
        float expResult = 0.0F;
        float result = instance.getDensity();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class ChannelMechanism.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        ChannelMechanism instance = new ChannelMechanism();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDensity method, of class ChannelMechanism.
     */
    @Test
    public void testSetDensity() {
        System.out.println("setDensity");
        float density = 0.0F;
        ChannelMechanism instance = new ChannelMechanism();
        instance.setDensity(density);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class ChannelMechanism.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        ChannelMechanism instance = new ChannelMechanism();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtraParameter method, of class ChannelMechanism.
     */
    @Test
    public void testGetExtraParameter() {
        System.out.println("getExtraParameter");
        String paramName = "";
        ChannelMechanism instance = new ChannelMechanism();
        MechParameter expResult = null;
        MechParameter result = instance.getExtraParameter(paramName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtraParameters method, of class ChannelMechanism.
     */
    @Test
    public void testGetExtraParameters() {
        System.out.println("getExtraParameters");
        ChannelMechanism instance = new ChannelMechanism();
        ArrayList<MechParameter> expResult = null;
        ArrayList<MechParameter> result = instance.getExtraParameters();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExtraParameters method, of class ChannelMechanism.
     */
    @Test
    public void testSetExtraParameters() {
        System.out.println("setExtraParameters");
        ArrayList<MechParameter> params = null;
        ChannelMechanism instance = new ChannelMechanism();
        instance.setExtraParameters(params);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}