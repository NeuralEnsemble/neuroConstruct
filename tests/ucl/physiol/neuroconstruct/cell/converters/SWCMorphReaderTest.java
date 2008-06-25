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
public class SWCMorphReaderTest {

    public SWCMorphReaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadFromMorphologyFile method, of class SWCMorphReader.
     */
    @Test
    public void testLoadFromMorphologyFile() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File morphologyFile = null;
        String name = "";
        SWCMorphReader instance = new SWCMorphReader();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(morphologyFile, name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of daughtersInherit method, of class SWCMorphReader.
     */
    @Test
    public void testDaughtersInherit() {
        System.out.println("daughtersInherit");
        boolean adj = false;
        SWCMorphReader instance = new SWCMorphReader();
        instance.daughtersInherit(adj);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of includeAnatFeatures method, of class SWCMorphReader.
     */
    @Test
    public void testIncludeAnatFeatures() {
        System.out.println("includeAnatFeatures");
        boolean adj = false;
        SWCMorphReader instance = new SWCMorphReader();
        instance.includeAnatFeatures(adj);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class SWCMorphReader.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        SWCMorphReader.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}