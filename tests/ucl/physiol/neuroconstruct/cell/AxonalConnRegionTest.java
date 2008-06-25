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
import ucl.physiol.neuroconstruct.project.Region;

/**
 *
 * @author padraig
 */
public class AxonalConnRegionTest {

    public AxonalConnRegionTest() {
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
     * Test of equals method, of class AxonalConnRegion.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object otherObj = null;
        AxonalConnRegion instance = new AxonalConnRegion();
        boolean expResult = false;
        boolean result = instance.equals(otherObj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class AxonalConnRegion.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRegion method, of class AxonalConnRegion.
     */
    @Test
    public void testGetRegion() {
        System.out.println("getRegion");
        AxonalConnRegion instance = new AxonalConnRegion();
        Region expResult = null;
        Region result = instance.getRegion();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRegion method, of class AxonalConnRegion.
     */
    @Test
    public void testSetRegion() {
        System.out.println("setRegion");
        Region region = null;
        AxonalConnRegion instance = new AxonalConnRegion();
        instance.setRegion(region);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class AxonalConnRegion.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        AxonalConnRegion instance = new AxonalConnRegion();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class AxonalConnRegion.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInfo method, of class AxonalConnRegion.
     */
    @Test
    public void testGetInfo() {
        System.out.println("getInfo");
        boolean html = false;
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.getInfo(html);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class AxonalConnRegion.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        AxonalConnRegion instance = new AxonalConnRegion();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class AxonalConnRegion.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        AxonalConnRegion.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}