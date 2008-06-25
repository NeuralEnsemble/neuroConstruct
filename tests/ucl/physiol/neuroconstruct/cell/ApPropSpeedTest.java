/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

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
public class ApPropSpeedTest {

    public ApPropSpeedTest() {
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
     * Test of equals method, of class ApPropSpeed.
     */
    @Test
    public void testEquals() {
        System.out.println("ApPropSpeedTest equals");
        Object otherObj = null;
        ApPropSpeed instance = new ApPropSpeed(123);
        boolean expResult = false;
        boolean result = instance.equals(otherObj);
        assertEquals(expResult, result);
        
        ApPropSpeed instance2 = new ApPropSpeed(123);
        
        
        assertEquals(instance, instance2);
        
    }

    /**
     * Test of hashCode method, of class ApPropSpeed.
     */
    @Test
    public void testHashCode() {
        System.out.println("ApPropSpeedTest hashCode");
        
        ApPropSpeed instance = new ApPropSpeed(0);
        ApPropSpeed instance2 = new ApPropSpeed(0);
        
        int result = instance.hashCode();
        int result2 = instance2.hashCode();
        
        assertEquals(result, result2);
        
    }




   




}