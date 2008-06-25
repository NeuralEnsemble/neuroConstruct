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
public class GenesisMorphReaderTest {

    public GenesisMorphReaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadFromMorphologyFile method, of class GenesisMorphReader.
     */
    @Test
    public void testLoadFromMorphologyFile() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File morphologyFile = null;
        String name = "";
        GenesisMorphReader instance = new GenesisMorphReader();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(morphologyFile, name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class GenesisMorphReader.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        GenesisMorphReader.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}