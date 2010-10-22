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
    public void testTextHDF5Load() throws IOException
    {
        System.out.println("---  testTextHDF5Load...");

        ArrayList<File> files = new ArrayList<File>();

        files.add(new File("testProjects/TestHDF5/simulations/TestText"));
        files.add(new File("testProjects/TestHDF5/simulations/TestH5"));
        files.add(new File("testProjects/TestHDF5/simulations/TestSpikesText"));
        //files.add(new File("testProjects/TestHDF5/simulations/TestSpikesHDF5"));

        SimulationData simulationData1 = null;
        try
        {
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
        catch (SimulationDataException ex)
        {
            System.out.println("Error..."+ ex.getMessage());
        }
    }


    public static void main(String[] args)
    {
        SimulationDataTest ct = new SimulationDataTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }



}