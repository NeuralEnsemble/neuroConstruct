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
public class NeuronMorphReaderTest {

    public NeuronMorphReaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadFromMorphologyFile method, of class NeuronMorphReader.
     */
    @Test
    public void testLoadFromMorphologyFile_4args() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File morphologyFile = null;
        String name = "";
        boolean moveSomaToOrigin = false;
        boolean moveDendsToConnectionPoint = false;
        NeuronMorphReader instance = new NeuronMorphReader();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(morphologyFile, name, moveSomaToOrigin, moveDendsToConnectionPoint);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBetterSectionName method, of class NeuronMorphReader.
     */
    @Test
    public void testGetBetterSectionName() {
        System.out.println("getBetterSectionName");
        String origSecName = "";
        String expResult = "";
        String result = NeuronMorphReader.getBetterSectionName(origSecName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadFromMorphologyFile method, of class NeuronMorphReader.
     */
    @Test
    public void testLoadFromMorphologyFile_File_String() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File oldFile = null;
        String name = "";
        NeuronMorphReader instance = new NeuronMorphReader();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(oldFile, name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIgnoredLines method, of class NeuronMorphReader.
     */
    @Test
    public void testGetIgnoredLines() {
        System.out.println("getIgnoredLines");
        NeuronMorphReader instance = new NeuronMorphReader();
        String expResult = "";
        String result = instance.getIgnoredLines();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class NeuronMorphReader.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        NeuronMorphReader.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}