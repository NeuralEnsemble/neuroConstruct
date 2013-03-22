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

package ucl.physiol.neuroconstruct.utils;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class NumberGeneratorTest {

    public NumberGeneratorTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() ElecInputGeneratorTest");
    }

    @Test
    public void testAll() 
    {
        System.out.println("---  testAll()");
        
        float f1 = 3.3f;
        float f2 = 6.6f;
        int i1 = 3;
        int i2 = 7;
        
        NumberGenerator ng1 = new NumberGenerator(f1);
        
        assertEquals(f1, ng1.getNextNumber(), 0);
        
        NumberGenerator ng2 = new NumberGenerator(i1);
        
        assertEquals(i1, ng2.getNextNumber(), 0);
        
        ng2.initialiseAsRandomIntGenerator(i2, i1);
        
        float num = ng2.getNextNumber();
        
        assertTrue((int)num == num && num<=i2 && num >=i1);
        
        ng2.initialiseAsGaussianFloatGenerator(f2, f1, f1, f2);
        
        assertTrue(ng2.getNextNumber()>=f1 && ng2.getNextNumber()<=f2);
        
        NumberGenerator ng3 = (NumberGenerator)ng2.clone();
        
        assertEquals(ng3, ng2);
        assertEquals(ng3.toShortString(), ng2.toShortString());
        assertEquals(ng3.toString(), ng2.toString());
        assertEquals(ng3.hashCode(), ng2.hashCode());
        
    }

}









