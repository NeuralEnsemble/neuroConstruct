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
public class OriginalCompartmentalisationTest {

    public OriginalCompartmentalisationTest() {
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
     * Test of generateComp method, of class OriginalCompartmentalisation.
     */
    @Test
    public void testGenerateComp() {
        System.out.println("generateComp");
        Cell origCell = null;
        OriginalCompartmentalisation instance = new OriginalCompartmentalisation();
        Cell expResult = null;
        Cell result = instance.generateComp(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}