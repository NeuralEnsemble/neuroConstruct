/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class ChannelMechanismTest {

    public ChannelMechanismTest() {
        System.out.println("Creating the ChannelMechanismTest...");
    }

    @Before
    public void setUp() 
    {
        System.out.println("Setting up the ChannelMechanismTest...");
    }

    @After
    public void tearDown() 
    {
        
        System.out.println("Tearing down the ChannelMechanismTest...");
    }
    
    @Test public void testDumb() 
    {
        System.out.println("Carrying out a dumb test...");
        fail("D'oh!");
        System.out.println("Carrying out a dumb test...");
        String h = null;
        h.length();
        
        assertTrue(2==3);
    }


}