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
import ucl.physiol.neuroconstruct.cell.Cell;

/**
 *
 * @author padraig
 */
public class MorphCompartmentalisationTest {

    public MorphCompartmentalisationTest() {
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
     * Test of getCompartmentalisation method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetCompartmentalisation() {
        System.out.println("getCompartmentalisation");
        Cell origCell = null;
        MorphCompartmentalisation instance = null;
        Cell expResult = null;
        Cell result = instance.getCompartmentalisation(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateComp method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGenerateComp() {
        System.out.println("generateComp");
        Cell origCell = null;
        MorphCompartmentalisation instance = null;
        Cell expResult = null;
        Cell result = instance.generateComp(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentMapper method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetSegmentMapper() {
        System.out.println("getSegmentMapper");
        MorphCompartmentalisation instance = null;
        SegmentLocMapper expResult = null;
        SegmentLocMapper result = instance.getSegmentMapper();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class MorphCompartmentalisation.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDescription method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.getDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}