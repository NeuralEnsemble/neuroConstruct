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