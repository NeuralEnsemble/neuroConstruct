/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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









