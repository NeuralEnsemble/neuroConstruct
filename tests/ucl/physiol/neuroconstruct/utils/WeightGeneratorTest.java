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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;

/**
 *
 * @author Matteo
 */
public class WeightGeneratorTest {
    
    public WeightGeneratorTest() {
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
        
        float f1 = 6;
        float f2 = 14;
        float i1 = 3;
        float i2 = 7;
        
        WeightGenerator wg1;
        WeightGenerator wg2;
        
        try {
            
            wg1 = new WeightGenerator("r*2", false);
            
            assertEquals(wg1.getNextNumber(i1), f1, 0);
            assertEquals(false, wg1.isSomaToSoma());
                        
            wg2 = new WeightGenerator(wg1.toShortString(), false);
            
            assertEquals(wg2, wg1);
            assertEquals(wg2.getNextNumber(i2), f2, 0);
            
            
        } catch (EquationException ex) {
            Logger.getLogger(WeightGeneratorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args)
    {
        WeightGeneratorTest wt = new WeightGeneratorTest();
        org.junit.runner.Result r = org.junit.runner.JUnitCore.runClasses(wt.getClass());
        MainTest.checkResults(r);
    }

}
