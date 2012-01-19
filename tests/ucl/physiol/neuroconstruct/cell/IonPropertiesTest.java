/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import org.neuroml.test.MainTest;
import org.junit.runner.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class IonPropertiesTest {

    public IonPropertiesTest() {
    }

    @Test
    public void testCloneAndEquals() 
    {
        System.out.println("---  testCloneAndEquals...");
        IonProperties ip1 = new IonProperties("na", 55);
        IonProperties ip2 = new IonProperties("ca", 100, 10);

        IonProperties ip3 = (IonProperties)ip1.clone();
        IonProperties ip4 = (IonProperties)ip2.clone();

        assertEquals(ip1, ip3);
        assertEquals(ip4, ip2);

        assertFalse(ip1.equals(ip4));
        assertFalse(ip2.equals(ip3));

        ip1.setName("Na");
        assertFalse(ip1.equals(ip3));
        ip1.setName("na");
        ip1.setReversalPotential(1);
        assertFalse(ip1.equals(ip3));

        ip2.setInternalConcentration(2);
        assertFalse(ip2.equals(ip4));

    }


    public static void main(String[] args)
    {
        IonPropertiesTest ct = new IonPropertiesTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }
}