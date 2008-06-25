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

/**
 *
 * @author padraig
 */
public class CompartmentalisationManagerTest {

    public CompartmentalisationManagerTest() {
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
     * Test of getOrigMorphCompartmentalisation method, of class CompartmentalisationManager.
     */
    @Test
    public void testGetOrigMorphCompartmentalisation() {
        System.out.println("getOrigMorphCompartmentalisation");
        MorphCompartmentalisation expResult = null;
        MorphCompartmentalisation result = CompartmentalisationManager.getOrigMorphCompartmentalisation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllMorphProjections method, of class CompartmentalisationManager.
     */
    @Test
    public void testGetAllMorphProjections() {
        System.out.println("getAllMorphProjections");
        ArrayList<MorphCompartmentalisation> expResult = null;
        ArrayList<MorphCompartmentalisation> result = CompartmentalisationManager.getAllMorphProjections();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class CompartmentalisationManager.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        CompartmentalisationManager.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}