/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.simulation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.dataset.DataSet;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;


/**
 *
 * @author Padraig
 */
public class SimulationDataTest
{


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SimulationDataTest");
    }


    @Test
    public void testTextHDF5Load() throws IOException, SimulationDataException
    {
        System.out.println("---  testTextHDF5Load...");

        ArrayList<File> files = new ArrayList<File>();

        files.add(new File("testProjects/TestHDF5/simulations/TestText"));
        files.add(new File("testProjects/TestHDF5/simulations/TestH5"));
        files.add(new File("testProjects/TestHDF5/simulations/TestH5Parallel"));
        files.add(new File("testProjects/TestHDF5/simulations/TestSpikesText"));
        files.add(new File("testProjects/TestHDF5/simulations/TestSpikesHDF5"));
        files.add(new File("testProjects/TestHDF5/simulations/TestSpikesHDF5Parallel"));

        SimulationData simulationData1 = null;

        for (File f: files)
        {
            GeneralUtils.timeCheck("Going to load data from: "+ f.getCanonicalPath(), true);

            simulationData1 = new SimulationData(f, true);
            simulationData1.initialise();

            GeneralUtils.timeCheck("Loaded data from: "+ f.getCanonicalPath(), true);

            ArrayList<DataStore> dss= simulationData1.getAllLoadedDataStores();

            GeneralUtils.timeCheck("Grabbed data from: "+ f.getCanonicalPath(), true);

            int numTimeSteps = simulationData1.getNumberTimeSteps();

            System.out.println("Total number of data stores: "+dss.size());

            int expected = 20;
            if (f.getName().indexOf("Spike")>0)
                expected = 80;

            assertEquals(dss.size(), expected);

            for(int i=0;i<Math.min(20, dss.size());i++)
            {
                DataStore ds = dss.get(i);
                System.out.println(ds);

                DataSet dataSet = simulationData1.getDataSet(ds.getCellSegRef(), ds.getVariable(), true);
                System.out.println(dataSet);

                assertEquals(numTimeSteps, dataSet.getNumberPoints());
            }
        }

        System.out.println("Tests completed!");


    }

    @Test
    public void testConvertSpikeTimesToContinuous1()
    {
        double[] times = new double[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        double[] spikes = new double[]{3,6,8,12};

        int non = 0;
        int spi = 1;

        double[] volts = SimulationData.convertSpikeTimesToContinuous(spikes, times, non, spi, 1);

        int spikeNum = 0;
        for (int i=0;i<times.length;i++){
            System.out.println("Time: "+times[i]+", volt: "+volts[i]);
            if (spikes[spikeNum] == times[i])
            {
                assertEquals(volts[i], spi, 0);
                System.out.println("Spike match...");
                if (spikeNum < spikes.length-1)
                    spikeNum++;
            }
            else
            {
                assertEquals(volts[i], non, 0);
            }
        }

    }

    @Test
    public void testConvertSpikeTimesToContinuous2()
    {
        double[] spikes = new double[]{100.333,103,106.999,108};

        int non = 0;
        int spi = 1;
        double start = 100;
        double end = 110;
        double step = 0.1;

        int expNum = 1+ (int)Math.floor((end-start)/step);


        double[] volts = SimulationData.convertSpikeTimesToContinuous(spikes, start, end, step, non, spi);


        assertEquals(volts.length, expNum, 0);

        int spikeNum = 0;
        for (int i=0;i<expNum;i++){
            double time = start + (i*step);

            System.out.println("Time: "+time+/*", next spike: "+spikes[spikeNum]+*/" volt: "+volts[i]);
            if (spikeNum<spikes.length && time >=spikes[spikeNum])
            {
                assertEquals(volts[i], spi, 0);
                System.out.println("Spike match...");
                spikeNum++;
            }
            else
            {
                assertEquals(volts[i], non, 0);
            }
        }

    }



    public static void main(String[] args)
    {
        SimulationDataTest ct = new SimulationDataTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }



}