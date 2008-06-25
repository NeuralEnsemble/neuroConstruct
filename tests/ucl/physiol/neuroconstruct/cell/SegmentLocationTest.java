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
public class SegmentLocationTest {

    public SegmentLocationTest() {
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
     * Test of getFractAlong method, of class SegmentLocation.
     */
    @Test
    public void testGetFractAlong() {
        System.out.println("getFractAlong");
        SegmentLocation instance = null;
        float expResult = 0.0F;
        float result = instance.getFractAlong();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentId method, of class SegmentLocation.
     */
    @Test
    public void testGetSegmentId() {
        System.out.println("getSegmentId");
        SegmentLocation instance = null;
        int expResult = 0;
        int result = instance.getSegmentId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SegmentLocation.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SegmentLocation instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}