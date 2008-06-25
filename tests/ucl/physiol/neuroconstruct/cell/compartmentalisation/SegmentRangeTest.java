/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

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
public class SegmentRangeTest {

    public SegmentRangeTest() {
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
     * Test of getSegmentId method, of class SegmentRange.
     */
    @Test
    public void testGetSegmentId() {
        System.out.println("getSegmentId");
        SegmentRange instance = null;
        int expResult = 0;
        int result = instance.getSegmentId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTotalSegmentLength method, of class SegmentRange.
     */
    @Test
    public void testGetTotalSegmentLength() {
        System.out.println("getTotalSegmentLength");
        SegmentRange instance = null;
        float expResult = 0.0F;
        float result = instance.getTotalSegmentLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartFract method, of class SegmentRange.
     */
    @Test
    public void testGetStartFract() {
        System.out.println("getStartFract");
        SegmentRange instance = null;
        float expResult = 0.0F;
        float result = instance.getStartFract();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndFract method, of class SegmentRange.
     */
    @Test
    public void testGetEndFract() {
        System.out.println("getEndFract");
        SegmentRange instance = null;
        float expResult = 0.0F;
        float result = instance.getEndFract();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRangeLength method, of class SegmentRange.
     */
    @Test
    public void testGetRangeLength() {
        System.out.println("getRangeLength");
        SegmentRange instance = null;
        float expResult = 0.0F;
        float result = instance.getRangeLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SegmentRange.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SegmentRange instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}