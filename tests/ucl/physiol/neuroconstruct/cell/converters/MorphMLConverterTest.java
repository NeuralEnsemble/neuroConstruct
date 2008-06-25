/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.MorphCompartmentalisation;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.SimConfig;

/**
 *
 * @author padraig
 */
public class MorphMLConverterTest {

    public MorphMLConverterTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadFromJavaXMLFile method, of class MorphMLConverter.
     */
    @Test
    public void testLoadFromJavaXMLFile() throws Exception {
        System.out.println("loadFromJavaXMLFile");
        File javaXMLFile = null;
        Cell expResult = null;
        Cell result = MorphMLConverter.loadFromJavaXMLFile(javaXMLFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWarnings method, of class MorphMLConverter.
     */
    @Test
    public void testGetWarnings() {
        System.out.println("getWarnings");
        MorphMLConverter instance = new MorphMLConverter();
        String expResult = "";
        String result = instance.getWarnings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadFromJavaObjFile method, of class MorphMLConverter.
     */
    @Test
    public void testLoadFromJavaObjFile() throws Exception {
        System.out.println("loadFromJavaObjFile");
        File objFile = null;
        Cell expResult = null;
        Cell result = MorphMLConverter.loadFromJavaObjFile(objFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadFromMorphologyFile method, of class MorphMLConverter.
     */
    @Test
    public void testLoadFromMorphologyFile() throws Exception {
        System.out.println("loadFromMorphologyFile");
        File morphologyFile = null;
        String name = "";
        MorphMLConverter instance = new MorphMLConverter();
        Cell expResult = null;
        Cell result = instance.loadFromMorphologyFile(morphologyFile, name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveCellInJavaXMLFormat method, of class MorphMLConverter.
     */
    @Test
    public void testSaveCellInJavaXMLFormat() throws Exception {
        System.out.println("saveCellInJavaXMLFormat");
        Cell cell = null;
        File javaXMLFile = null;
        boolean expResult = false;
        boolean result = MorphMLConverter.saveCellInJavaXMLFormat(cell, javaXMLFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveCellInJavaObjFormat method, of class MorphMLConverter.
     */
    @Test
    public void testSaveCellInJavaObjFormat() throws Exception {
        System.out.println("saveCellInJavaObjFormat");
        Cell cell = null;
        File javaObjFile = null;
        boolean expResult = false;
        boolean result = MorphMLConverter.saveCellInJavaObjFormat(cell, javaObjFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveCellInMorphMLFormat method, of class MorphMLConverter.
     */
    @Test
    public void testSaveCellInMorphMLFormat() throws Exception {
        System.out.println("saveCellInMorphMLFormat");
        Cell cell = null;
        Project project = null;
        File morphMLFile = null;
        String level = "";
        MorphMLConverter.saveCellInMorphMLFormat(cell, project, morphMLFile, level);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveAllCellsInNeuroML method, of class MorphMLConverter.
     */
    @Test
    public void testSaveAllCellsInNeuroML() throws Exception {
        System.out.println("saveAllCellsInNeuroML");
        Project project = null;
        MorphCompartmentalisation mc = null;
        String level = "";
        SimConfig simConfig = null;
        File destDir = null;
        ArrayList<Cell> expResult = null;
        ArrayList<Cell> result = MorphMLConverter.saveAllCellsInNeuroML(project, mc, level, simConfig, destDir);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class MorphMLConverter.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        MorphMLConverter.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}