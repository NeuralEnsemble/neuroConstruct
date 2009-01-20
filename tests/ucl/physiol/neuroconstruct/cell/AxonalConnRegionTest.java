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

package ucl.physiol.neuroconstruct.cell;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.project.Region;

/**
 *
 * @author padraig
 */
public class AxonalConnRegionTest {

    public AxonalConnRegionTest() {
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
     * Test of equals method, of class AxonalConnRegion.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object otherObj = null;
        AxonalConnRegion instance = new AxonalConnRegion();
        boolean expResult = false;
        boolean result = instance.equals(otherObj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class AxonalConnRegion.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRegion method, of class AxonalConnRegion.
     */
    @Test
    public void testGetRegion() {
        System.out.println("getRegion");
        AxonalConnRegion instance = new AxonalConnRegion();
        Region expResult = null;
        Region result = instance.getRegion();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRegion method, of class AxonalConnRegion.
     */
    @Test
    public void testSetRegion() {
        System.out.println("setRegion");
        Region region = null;
        AxonalConnRegion instance = new AxonalConnRegion();
        instance.setRegion(region);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class AxonalConnRegion.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        AxonalConnRegion instance = new AxonalConnRegion();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class AxonalConnRegion.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInfo method, of class AxonalConnRegion.
     */
    @Test
    public void testGetInfo() {
        System.out.println("getInfo");
        boolean html = false;
        AxonalConnRegion instance = new AxonalConnRegion();
        String expResult = "";
        String result = instance.getInfo(html);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class AxonalConnRegion.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        AxonalConnRegion instance = new AxonalConnRegion();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class AxonalConnRegion.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        AxonalConnRegion.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}