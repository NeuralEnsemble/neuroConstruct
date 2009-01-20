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
public class MorphCompartmentalisationTest {

    public MorphCompartmentalisationTest() {
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
     * Test of getCompartmentalisation method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetCompartmentalisation() {
        System.out.println("getCompartmentalisation");
        Cell origCell = null;
        MorphCompartmentalisation instance = null;
        Cell expResult = null;
        Cell result = instance.getCompartmentalisation(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateComp method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGenerateComp() {
        System.out.println("generateComp");
        Cell origCell = null;
        MorphCompartmentalisation instance = null;
        Cell expResult = null;
        Cell result = instance.generateComp(origCell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentMapper method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetSegmentMapper() {
        System.out.println("getSegmentMapper");
        MorphCompartmentalisation instance = null;
        SegmentLocMapper expResult = null;
        SegmentLocMapper result = instance.getSegmentMapper();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class MorphCompartmentalisation.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDescription method, of class MorphCompartmentalisation.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        MorphCompartmentalisation instance = null;
        String expResult = "";
        String result = instance.getDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}