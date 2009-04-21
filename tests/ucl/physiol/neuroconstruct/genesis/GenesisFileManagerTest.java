/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.genesis;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.GenesisCompartmentalisation;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class GenesisFileManagerTest {

    public GenesisFileManagerTest() 
    {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() GenesisFileManagerTest");
    }
    
    private ProjectManager loadProject(String projectFile) throws ProjectFileParsingException
    {

        ProjectManager pm = new ProjectManager();
        
        pm.loadProject(new File(projectFile));
        
        return pm;
    }
    
    
    @Test public void testGenerateGenScripts() throws ProjectFileParsingException, InterruptedException,  IOException, SimulationDataException, GenesisException 
    {
        System.out.println("---  testGenerateGenScripts...");
        
        //if (gene)
        
        ProjectManager pm = loadProject("testProjects/TestGenNetworks/TestGenNetworks.neuro.xml");
        
        Project proj = pm.getCurrentProject();
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
                
        pm.doGenerate(sc.getName(), 1234);
        
        int wait = 1000;
        
        if (GeneralUtils.isWindowsBasedPlatform())
            wait = 8000;
        
        while(pm.isGenerating())
        {
            Thread.sleep(wait);
        }
        
        int numGen = proj.generatedCellPositions.getNumberInAllCellGroups();
        
        System.out.println("Project: "+ proj.getProjectFileName()+" loaded and "
                +numGen+" cells generated");
        
        String simName = "TestSim";
        
        proj.simulationParameters.setReference(simName);
        
        File simDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);
        
        //if (simDir.exists())
        //    simDir.delete();
        
        proj.genesisSettings.setGraphicsMode(false);
        
        proj.genesisFileManager.setQuitAfterRun(true);

        for(int i=0;i<=1;i++)
        {
            proj.genesisSettings.setCopySimFiles((i==0));

            proj.genesisFileManager.generateTheGenesisFiles(sc, null, new GenesisCompartmentalisation(), 1234);

            File mainFile = new File(proj.genesisFileManager.getMainGenesisFileName());

            assertTrue(mainFile.exists());

            System.out.println("Created files, including: "+mainFile);

            proj.genesisFileManager.runGenesisFile();

            System.out.println("Run GENESIS files");


            SimulationData simData = new SimulationData(simDir, false);

            File timesFile = simData.getTimesFile();

            Thread.sleep(wait*5); // Shouldn't take longer than this


            if (!timesFile.exists())
            {
                System.out.println("Waiting for file to be created: "+ timesFile.getAbsolutePath());
                Thread.sleep(wait*3);
            }

            assertTrue(timesFile.exists());

            simData.initialise();


            int numRecordings = simData.getCellSegRefs(false).size();

            assertEquals(numRecordings, numGen);


            System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());


            File simDataDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);

            SimulationData sd = new SimulationData(simDataDir);

            sd.initialise();

            while(!sd.isDataLoaded())
            {
                System.out.println("Waiting for data to be loaded");
                Thread.sleep(1000);
            }

            System.out.println("Data saved: "+ sd.getCellSegRefs(true));

            String ref = "Pacemaker_0";

            double[] volts = sd.getVoltageAtAllTimes(ref);
			double[] times = sd.getAllTimes();

            
            assertEquals(volts.length, 1 + (sc.getSimDuration()/proj.simulationParameters.getDt()), 0);

            double[] spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, 0, 0, (float)times[times.length-1]);

            System.out.println("Num spikeTimes: "+ spikeTimes.length);

            int expectedNum = 76; // As checked through gui

            assertEquals(expectedNum, spikeTimes.length);


            
            /*
							print "Data loaded: "
							print simData.getAllLoadedDataStores()
							times = simData.getAllTimes()
							cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"
							volts = simData.getVoltageAtAllTimes(cellSegmentRef)

							traceInfo = "Voltage at: %s in simulation: %s"%(cellSegmentRef, sim)

							dataSetV = DataSet(traceInfo, traceInfo, "mV", "ms", "Membrane potential", "Time")
							for i in range(len(times)):
									dataSetV.addPoint(times[i], volts[i])

							plotFrameVolts.addDataSet(dataSetV)

							spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)*/

            
        }
        
    }
    

     public static void main(String[] args)
     {
        GenesisFileManagerTest ct = new GenesisFileManagerTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

     }

}