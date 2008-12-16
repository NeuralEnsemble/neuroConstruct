/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.spi.DirStateFactory.Result;
import org.junit.Before;
import org.junit.Test;
import test.MainTest;
import ucl.physiol.neuroconstruct.utils.WeightGenerator;
import ucl.physiol.neuroconstruct.utils.equation.Argument;
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
