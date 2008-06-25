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
public class SimpleCompartmentalisationTest {

    public SimpleCompartmentalisationTest() {
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
     * Test of generateComp method, of class SimpleCompartmentalisation.
     */
    @Test
    public void testGenerateComp() {
        System.out.println("generateComp");
        Cell origCell = null;
        SimpleCompartmentalisation instance = new SimpleCompartmentalisation();
        Cell expResult = null;
        Cell result = instance.generateComp(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class SimpleCompartmentalisation.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        SimpleCompartmentalisation.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}