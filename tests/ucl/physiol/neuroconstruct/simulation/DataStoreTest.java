/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.test.MainTest;
import org.junit.runner.Result;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class DataStoreTest {


    @Before
    public void setUp()
    {
        System.out.println("---------------   setUp() DataStoreTest");
    }

    @Test
    public void testContinuous()
    {
        double[] vals = new double[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        DataStore ds = new DataStore(vals, "fggjghj", 0,0,"fggjghj", "fggjghj","fggjghj",null);

        assertTrue(ds.getDataPoints().length == vals.length);

        assertEquals(ds.getMinVal(),0,0);
        assertEquals(ds.getMaxVal(),16,0);

        assertFalse(ds.isSpikeTimes());


    }

    @Test
    public void testSpikeTimes()
    {
        double[] spikes = new double[]{103,106.3,108};

        double start = 100;
        double end = 110;
        double step = 0.1;

        double max = 2;
        double min = -2;

        int expNum = 1+ (int)Math.floor((end-start)/step);

        DataStore ds = new DataStore(spikes, min, max, start, end, step, "fggjghj", 0,0,"fggjghj", "fggjghj","fggjghj",null);

        double[] cont =ds.getDataPoints();

        assertEquals("Compare length", cont.length,expNum);

        assertEquals("Min val", ds.getMinVal(),min,0);
        assertEquals("Max val", ds.getMaxVal(),max,0);

        assertTrue(ds.isSpikeTimes());
        
        
        int spikeNum = 0;
        for (int i=0;i<expNum;i++){
            double time = start + (i*step);

            System.out.println("Time: "+time+", volt: "+cont[i]);
            if (spikeNum<spikes.length && time >= spikes[spikeNum])
            {
                assertEquals(cont[i], max, 0);
                System.out.println("Spike match...");
                spikeNum++;
            }
            else
            {
                assertEquals(cont[i], min, 0);
            }
        }


        double[] spikes2 = new double[]{108.5,109};
        double max2 = 100;
        double min2 = 0;

        ds.setSpikeTimes(spikes2, min2, max2, start, end, step);

        cont =ds.getDataPoints();

        assertEquals("Min val", ds.getMinVal(),min2,0);
        assertEquals("Max val", ds.getMaxVal(),max2,0);

        assertTrue(ds.isSpikeTimes());


        spikeNum = 0;
        for (int i=0;i<expNum;i++){
            double time = start + (i*step);

            System.out.println("Time: "+time+", volt: "+cont[i]);
            if (spikeNum<spikes2.length && time >= spikes2[spikeNum])
            {
                assertEquals(cont[i], max2, 0);
                System.out.println("Spike match...");
                spikeNum++;
            }
            else
            {
                assertEquals(cont[i], min2, 0);
            }
        }

    }


    public static void main(String[] args)
    {
        DataStoreTest ct = new DataStoreTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }
}