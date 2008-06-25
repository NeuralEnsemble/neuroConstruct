/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;

/**
 *
 * @author padraig
 */
public class SegmentLocMapperTest {

    public SegmentLocMapperTest() {
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
     * Test of reset method, of class SegmentLocMapper.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        SegmentLocMapper instance = new SegmentLocMapper();
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMapping method, of class SegmentLocMapper.
     */
    @Test
    public void testAddMapping() {
        System.out.println("addMapping");
        SegmentRange from = null;
        SegmentRange[] to = null;
        SegmentLocMapper instance = new SegmentLocMapper();
        instance.addMapping(from, to);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllSegIdsMappedFrom method, of class SegmentLocMapper.
     */
    @Test
    public void testGetAllSegIdsMappedFrom() {
        System.out.println("getAllSegIdsMappedFrom");
        SegmentLocMapper instance = new SegmentLocMapper();
        ArrayList<Integer> expResult = null;
        ArrayList<Integer> result = instance.getAllSegIdsMappedFrom();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllSegIdsMappedTo method, of class SegmentLocMapper.
     */
    @Test
    public void testGetAllSegIdsMappedTo() {
        System.out.println("getAllSegIdsMappedTo");
        SegmentLocMapper instance = new SegmentLocMapper();
        ArrayList<Integer> expResult = null;
        ArrayList<Integer> result = instance.getAllSegIdsMappedTo();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFromSegmentId method, of class SegmentLocMapper.
     */
    @Test
    public void testGetFromSegmentId() {
        System.out.println("getFromSegmentId");
        int toSegmentId = 0;
        SegmentLocMapper instance = new SegmentLocMapper();
        int expResult = 0;
        int result = instance.getFromSegmentId(toSegmentId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mapSegmentLocation method, of class SegmentLocMapper.
     */
    @Test
    public void testMapSegmentLocation() {
        System.out.println("mapSegmentLocation");
        SegmentLocation oldSegLoc = null;
        SegmentLocMapper instance = new SegmentLocMapper();
        SegmentLocation expResult = null;
        SegmentLocation result = instance.mapSegmentLocation(oldSegLoc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class SegmentLocMapper.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SegmentLocMapper instance = new SegmentLocMapper();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class SegmentLocMapper.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        SegmentLocMapper.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}