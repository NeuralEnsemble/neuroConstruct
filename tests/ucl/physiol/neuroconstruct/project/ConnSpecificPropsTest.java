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

package ucl.physiol.neuroconstruct.project;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class ConnSpecificPropsTest {

    public ConnSpecificPropsTest() {
    }

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() ConnSpecificPropsTest");
    }
   

    /**
     * Test of equals method, of class ConnSpecificProps.
     */
    @Test
    public void testEquals()
    {
        System.out.println("--- equals");
        
        ConnSpecificProps c1 = new ConnSpecificProps("s1");
        c1.internalDelay = 9;
        c1.weight = 4;
        
        ConnSpecificProps c2 = new ConnSpecificProps("s1");
        c2.internalDelay = 9;
        c2.weight = 4;
        
        
        boolean expResult = true;
        boolean result = c1.equals(c2);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class ConnSpecificProps.
     */
    @Test
    public void testHashCode()
    {
        System.out.println("--- hashCode");
        
        ConnSpecificProps c1 = new ConnSpecificProps("s1");
        c1.internalDelay = 9;
        c1.weight = 4;
        
        ConnSpecificProps c2 = new ConnSpecificProps("s1");
        c2.internalDelay = 9;
        c2.weight = 4;
        
        assertEquals(c1.hashCode(), c2.hashCode());
        
    }

   

}