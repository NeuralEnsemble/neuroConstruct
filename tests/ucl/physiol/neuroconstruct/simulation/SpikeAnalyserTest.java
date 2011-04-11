/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.simulation;

import java.util.ArrayList;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;

import ucl.physiol.neuroconstruct.dataset.DataSet;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import static org.junit.Assert.*;

/**
 *
 * @author Padraig
 */
public class SpikeAnalyserTest {

    public SpikeAnalyserTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SpikeAnalyserTest");
    }

    private float[] getTimesArray(float endTime, float dt)
    {
        float[] times = new float[(int)(endTime/dt)];
        for (int i=0;i<times.length;i++)
        {
            times[i]=i*dt;
        }
        return times;
    }

    private double[] getTimesArray(double endTime, double dt)
    {
        double[] times = new double[(int)(endTime/dt)];
        for (int i=0;i<times.length;i++)
        {
            times[i]=(float)i*dt;
        }
        return times;
    }

    private float[] getVoltArray(float endTime, float dt, float val)
    {
        float[] volts = new float[(int)(endTime/dt)];

        for (int i=0;i<volts.length;i++)
        {
            volts[i]=val;
        }
        return volts;
    }

    /**
     * Test of getSpikeTimes method, of class SpikeAnalyser.
     */
    @Test
    public void testGetSpikeTimes()
    {
        System.out.println("---  testGetSpikeTimes...");

        float dt = 0.01f;
        float dur = 100;

        float[] times = getTimesArray(dur, dt);
        float[] volts = getVoltArray(dur, dt, -100);

        float[] spikesIn = new float[]{3.01f,8,12, 23,45.5f,    89};

        for (float s: spikesIn)
        {
            int i = (int)(s/dt);
            volts[i] = 10;
        }

        double[] spikesOut = SpikeAnalyser.getSpikeTimes(volts, times, -10, 0, dur);

        assertEquals(spikesOut.length, spikesIn.length);

        for(int i=0;i<spikesOut.length;i++)
        {
            assertEquals(spikesOut[i], spikesIn[i],0);
        }

    }


    @Test
    public void testInterSpikeIntervals()
    {
        System.out.println("---  testInterSpikeIntervals...");

        float dt = 0.01f;
        float dur = 100;

        float[] times = getTimesArray(dur, dt);
        float[] volts = getVoltArray(dur, dt, -100);

        float[] spikesIn = new float[]{10, 25, 45, 55};

        for (float s: spikesIn)
        {
            int i = (int)(s/dt);
            volts[i] = 10;
        }

        ArrayList<Double> isi = SpikeAnalyser.getInterSpikeIntervals(volts, times, -10, 0, dur);

        System.out.println("ISIs: "+ isi);

        assertTrue(isi.size()==3);
        assertEquals(isi.get(0), 15, 0);
        assertEquals(isi.get(1), 20, 0);
        assertEquals(isi.get(2), 10, 0);


    }

    @Test
    public void testBinning()
    {
        System.out.println("---  testBinning...");

        double[] vals = new double[]{1,3,14,16,17,18,22,44,55,56,57};

        int[] bins = SpikeAnalyser.getBinnedValues(vals, 0, 10, 6);

        assertEquals(bins[0], 2);
        assertEquals(bins[1], 4);
        assertEquals(bins[2], 1);
        assertEquals(bins[3], 0);
        assertEquals(bins[4], 1);
        assertEquals(bins[5], 3);


    }

    @Test
    public void testDistHist() throws ValueNotPresentException
    {
        System.out.println("---  testDistHist...");

        DataSet ds0 = new DataSet("Test", "Test", "x", "y", "", "");

        ds0.addPoint(1, 1);
        ds0.addPoint(2, 1);
        ds0.addPoint(3, 3);
        ds0.addPoint(4, 2);
        ds0.addPoint(5, 2);
        ds0.addPoint(6, 2);
        ds0.addPoint(7, 2);

        DataSet ds = SpikeAnalyser.getDistHist(ds0, DataSet.yDim, 0.5f, 1,4);

        System.out.println("Distribution: "+ ds);


        assertEquals(ds.getYvalue(1), 2, 0);
        assertEquals(ds.getYvalue(2), 4, 0);
        assertEquals(ds.getYvalue(3), 1, 0);

    }

    @Test
    public void testSlidingSynchrony() throws ValueNotPresentException
    {
        System.out.println("---  testSlidingSynchrony...");

        double[] s1 = new double[]{5, 11, 23};
        double[] s2 = new double[]{12, 25};
        double[] s3 = new double[]{26};
        double[] s4 = new double[]{44};
        double[] s5 = new double[]{29, 89};

        ArrayList<double[]> spikeSets = new ArrayList<double[]>();
        spikeSets.add(s1);
        spikeSets.add(s2);
        spikeSets.add(s3);
        spikeSets.add(s4);
        spikeSets.add(s5);

        double[] times = getTimesArray(100, 0.1);

        DataSet ds = SpikeAnalyser.getSlidingSpikeSynchrony(spikeSets, times, 10, 10, 90);

        System.out.println("Generated sliding synch data: "+ds);

        assertEquals(ds.getYvalue(10), 2, 0);
        assertEquals(ds.getYvalue(20), 4, 0);
        assertEquals(ds.getYvalue(50), 0, 0);
        assertEquals(ds.getYvalue(88), 1, 0);



    }

    public static void main(String[] args)
    {
        SpikeAnalyserTest ct = new SpikeAnalyserTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }


}