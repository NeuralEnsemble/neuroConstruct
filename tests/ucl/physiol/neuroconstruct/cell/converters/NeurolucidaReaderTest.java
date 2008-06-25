/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.Cell;

/**
 *
 * @author padraig
 */
public class NeurolucidaReaderTest {

    public NeurolucidaReaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadFromMorphologyFile method, of class NeurolucidaReader.
     */
    @Test
    public void testLoadFromMorphologyFile() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File morphologyFile = null;
        String name = "";
        NeurolucidaReader instance = new NeurolucidaReader();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(morphologyFile, name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of includeSomaOutline method, of class NeurolucidaReader.
     */
    @Test
    public void testIncludeSomaOutline() {
        System.out.println("includeSomaOutline");
        boolean inc = false;
        NeurolucidaReader instance = new NeurolucidaReader();
        instance.includeSomaOutline(inc);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of daughtersInherit method, of class NeurolucidaReader.
     */
    @Test
    public void testDaughtersInherit() {
        System.out.println("daughtersInherit");
        boolean adj = false;
        NeurolucidaReader instance = new NeurolucidaReader();
        instance.daughtersInherit(adj);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class NeurolucidaReader.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        NeurolucidaReader.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}