/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

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