/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
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
     * Test of saveCellInNeuroMLFormat method, of class MorphMLConverter.
     */
    @Test
    public void testSaveCellInMorphMLFormat() throws Exception {
        System.out.println("saveCellInMorphMLFormat");
        Cell cell = null;
        Project project = null;
        File morphMLFile = null;
        String level = "";
        MorphMLConverter.saveCellInNeuroMLFormat(cell, project, morphMLFile, level);
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