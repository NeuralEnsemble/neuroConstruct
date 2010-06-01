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

package ucl.physiol.neuroconstruct.neuron;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;

import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.SimulatorMapping;
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessManager;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimEnvHelper;
import ucl.physiol.neuroconstruct.simulation.SimulationData;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.simulation.SimulationDataException;
import ucl.physiol.neuroconstruct.simulation.SpikeAnalyser;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;

/**
 *
 * @author padraig
 */
public class NeuronFileManagerTest {

    public NeuronFileManagerTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() NeuronFileManagerTest");
    }
    
    private ProjectManager loadProject(String projectFile) throws ProjectFileParsingException
    {

        ProjectManager pm = new ProjectManager();
        
        pm.loadProject(new File(projectFile));
        
        return pm;
    }
    
    @Test public void testGenerateAndRunHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException, Exception
    {
        if (!MainTest.testOnNEURON()) return;

        System.out.println("---  testGenerateAndRunHoc...");
        
        //ProjectManager pm = loadProject("testProjects/TestGenNetworks/TestGenNetworks.neuro.xml");

        String projName = "TestingGranCell";
        File projDir = new File(MainTest.getTempProjectDirectory(), projName);

        if (projDir.exists())
        {
            GeneralUtils.removeAllFiles(projDir, false, true, true);
        }

        System.out.println("Ex "+projDir.getCanonicalFile()+": "+ projDir.exists());
        projDir.mkdir();


        File projFile = new File(projDir, projName+".ncx");

        File oldProjDir = new File(ProjectStructure.getnCModelsDir(), "GranuleCell");
        File oldProj = new File(oldProjDir, "GranuleCell.ncx");

        ProjectManager pm = new ProjectManager();

        Project proj = pm.copyProject(oldProj, projFile);


        System.out.println("Created project at: "+ proj.getProjectFile().getCanonicalPath());


        assertEquals(proj.getProjectName(), projName);

        assertTrue(projFile.exists());

        GeneralProperties.setNeuroMLVersionString(GeneralProperties.getLatestNeuroMLVersionString());

        String validity = pm.getValidityReport(false);

        assertTrue(validity, validity.indexOf(ValidityStatus.PROJECT_IS_VALID)>=0);

        System.out.println("Project is valid!! Using NeuroML "+ GeneralProperties.getNeuroMLVersionString());



        Vector<CellMechanism> cellMechs = proj.cellMechanismInfo.getAllCellMechanisms();

        File xslDir = GeneralProperties.getChannelMLSchemataDir();

        for (CellMechanism cellMech: cellMechs)
        {
            ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism) cellMech;
            File implFile = cmlMech.getXMLFile(proj);

            for (SimulatorMapping map: cmlMech.getSimMappings())
            {
                String simEnv = map.getSimEnv();
                File oldXsl = new File (implFile.getParent(), map.getMappingFile());
                File newXsl = null;

                if (simEnv.equals(SimEnvHelper.NEURON))
                {
                    newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_NEURONmod.xsl");
                }
                else if (simEnv.equals(SimEnvHelper.GENESIS))
                {
                    newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_GENESIStab.xsl");
                }
                else if (simEnv.equals(SimEnvHelper.PSICS))
                {
                    newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_PSICS.xsl");
                }


                GeneralUtils.copyFileIntoDir(newXsl, implFile.getParentFile());
                map.setMappingFile(newXsl.getName());


            }
        }

        proj.markProjectAsEdited();
        proj.saveProject();





        SimConfig sc = proj.simConfigInfo.getSimConfig("OnlyVoltage");

        proj.simulationParameters.setDt(0.01f);

                
        pm.doGenerate(sc.getName(), 1234);
        
        int wait = 1000;
        if (GeneralUtils.isWindowsBasedPlatform())
            wait = 2000;
        
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
        
        if (simDir.exists())
            simDir.delete();
        
        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        File mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        ProcessManager prm = new ProcessManager(mainHoc);
        
        boolean success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files");

        Thread.sleep(wait);
        
        SimulationData simData = new SimulationData(simDir, false);
        
        File timesFile = simData.getTimesFile();
        
        Thread.sleep(wait); // Shouldn't take longer than this

        while (!timesFile.exists())
        {
            System.out.println("Waiting for file to be created: "+ timesFile.getAbsolutePath());
            Thread.sleep(wait);
        }
        
        assertTrue(timesFile.exists());

        System.out.println("Times file exists");

        Thread.sleep(wait); // Wait for all files to be written...
        
        simData.initialise();

        while(!simData.isDataLoaded())
        {
            System.out.println("Waiting for data to be loaded");
            Thread.sleep(wait);
        }
        Thread.sleep(wait);
        
        
        int numRecordings = simData.getCellSegRefs(false).size();
        
        assertEquals(numRecordings, numGen);
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());
        
        double[] someVolts = simData.getVoltageAtAllTimes(SimulationData.getCellRef(sc.getCellGroups().get(0), 0));
        
        assertEquals(someVolts.length, 1 + (sc.getSimDuration()/proj.simulationParameters.getDt()), 0.1);

        File simDataDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);

        SimulationData sd = new SimulationData(simDataDir);

        sd.initialise();
        
        while(!sd.isDataLoaded())
        {
            System.out.println("Waiting for data to be loaded");
            Thread.sleep(wait);
        }

        System.out.println("Data saved: "+ sd.getCellSegRefs(true));

        String ref = "Gran_0";

        double[] volts = sd.getVoltageAtAllTimes(ref);
        double[] times = sd.getAllTimes();

        double[] spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, -20, 0, (float)times[times.length-1]);

        System.out.println("Num spikeTimes: "+ spikeTimes.length);

        int expectedNum = 20; // As checked through gui

        assertEquals(expectedNum, spikeTimes.length);

        
        
    }
    
    
    
    @Test public void testGenerateParallelHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        if (!MainTest.testOnPNEURON()) return;
        System.out.println("---  testGenerateParallelHoc...");
        
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            System.out.println("*** Not running testGenerateParallelHoc, as this is a Windows system!");
            return;
        }
        
        ProjectManager pm = loadProject("testProjects/TestParallel/TestParallel.neuro.xml");
        
        Project proj = pm.getCurrentProject();
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        
        System.out.println("Running sim in serial mode");
        
                
        pm.doGenerate(sc.getName(), 1234);
        
        int wait = 1000;
        
        while(pm.isGenerating())
        {
            Thread.sleep(wait);
        }
        
        int numGenSerial = proj.generatedCellPositions.getNumberInAllCellGroups();
        
        System.out.println("Project: "+ proj.getProjectFileName()+" loaded and "
                +numGenSerial+" cells generated using: "+ sc.getMpiConf());
        
        String simNameSerial = "TestSim";
        
        proj.simulationParameters.setReference(simNameSerial);
        
        File simDirSerial = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simNameSerial);
        
        if (simDirSerial.exists())
            simDirSerial.delete();
        
        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        File mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        ProcessManager prm = new ProcessManager(mainHoc);
        
        boolean success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files using: "+ sc.getMpiConf());

        Thread.sleep(wait);
        
        SimulationData simDataSerial = new SimulationData(simDirSerial, false);
        
        File timesFile = simDataSerial.getTimesFile();
        
        
        while (!timesFile.exists())
        {
            System.out.println("Waiting for: "+ timesFile.getAbsolutePath());
            Thread.sleep(wait);
        }
        
        assertTrue(timesFile.exists());
        
        simDataSerial.initialise();

        while(!simDataSerial.isDataLoaded())
        {
            System.out.println("Waiting for data to be loaded");
            Thread.sleep(wait);
        }
        
        int numRecordings = simDataSerial.getCellSegRefs(false).size();
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simDataSerial.getSimulationDirectory().getAbsolutePath());
        
        assertEquals(numRecordings, numGenSerial);
        
        

        System.out.println("Running sim in parallel mode");
        
        MpiSettings mpis = new MpiSettings();
        sc.setMpiConf(mpis.getMpiConfiguration(MpiSettings.LOCAL_4PROC));
        
                
        pm.doGenerate(sc.getName(), 1234);
        
        wait = 1000;  // Give longer to set up parallel
        
        while(pm.isGenerating())
        {
            Thread.sleep(wait);
        }
        
        int numGenParallel = proj.generatedCellPositions.getNumberInAllCellGroups();
        
        System.out.println("Project: "+ proj.getProjectFileName()+" loaded and "
                +numGenParallel+" cells generated using: "+ sc.getMpiConf());
        
        assertEquals(numGenParallel, numGenSerial);
        
        
        String simNameParallel = "TestSimParallel";
        
        proj.simulationParameters.setReference(simNameParallel);
        
        File simDirParallel = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simNameParallel);
        
        if (simDirParallel.exists())
            simDirParallel.delete();

        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        prm = new ProcessManager(mainHoc);
        
        success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files");
        
        
        SimulationData simDataParallel = new SimulationData(simDirParallel, false);
        
        timesFile = simDataParallel.getTimesFile();
        

        while (!timesFile.exists())
        {
            System.out.println("Waiting for file to be created: "+ timesFile.getAbsolutePath());
            Thread.sleep(wait);
        }
        
        assertTrue(timesFile.exists());
        
        simDataParallel.initialise();

        while(!simDataParallel.isDataLoaded())
        {
            System.out.println("Waiting for data to be loaded");
            Thread.sleep(wait);
        }
        
        numRecordings = simDataParallel.getCellSegRefs(false).size();
        
        assertEquals(numRecordings, numGenParallel);
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simDataParallel.getSimulationDirectory().getAbsolutePath());
        
        for(String cg: sc.getCellGroups())
        {
            for(int j=0;j<proj.generatedCellPositions.getNumberInCellGroup(cg);j++)
            {
                String ref = SimulationData.getCellRef(cg, j);
                
                double[] voltsSerial = simDataSerial.getVoltageAtAllTimes(ref);
                double[] voltsParallel = simDataParallel.getVoltageAtAllTimes(ref);

                assertEquals(voltsParallel.length, voltsSerial.length);

                for(int i=0;i<voltsSerial.length;i++)
                {
                    assertEquals(voltsParallel[i], voltsSerial[i], 0);
                }
                System.out.println("Parallel data agrees with serial for: "+ref);
            }
        }
        
        
    }
    

     public static void main(String[] args)
     {
        NeuronFileManagerTest ct = new NeuronFileManagerTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

     }
    
    

}