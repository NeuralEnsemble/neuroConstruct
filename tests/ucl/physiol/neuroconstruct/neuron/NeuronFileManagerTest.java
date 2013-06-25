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
import ucl.physiol.neuroconstruct.test.MainTest;

import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiConfiguration;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.SimulatorMapping;
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessManager;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimEnvHelper;
import ucl.physiol.neuroconstruct.simulation.SimulationData;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import ucl.physiol.neuroconstruct.neuron.NeuronSettings.DataSaveFormat;
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
        System.out.println("---------------   setUp() NeuronFileManagerTest...");
    }
    
    private ProjectManager loadProject(String projectFile) throws ProjectFileParsingException
    {

        ProjectManager pm = new ProjectManager();
        
        pm.loadProject(new File(projectFile));
        
        return pm;
    }
    
    @Test public void testGenerateAndRunHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException, Exception
    {
        assumeTrue(MainTest.testNEURON());

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

        File oldProjDir = new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct");
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

    /* No need for this, as it's the case being compared to...
    @Test public void testCompareHocSerText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_HOC, false, DataSaveFormat.TEXT_NC);
    }*/
    @Test public void testCompareHocParText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_HOC, true, DataSaveFormat.TEXT_NC);
    }
    @Test public void testCompareHocSerH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_HOC, false, DataSaveFormat.HDF5_NC);
    }
    @Test public void testCompareHocParH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_HOC, true, DataSaveFormat.HDF5_NC);
    }

     
    @Test public void testComparePyXSerText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_XML, false, DataSaveFormat.TEXT_NC);
    }
   
    @Test public void testComparePyXParText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_XML, true, DataSaveFormat.TEXT_NC);
    }
    @Test public void testComparePyXSerH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_XML, false, DataSaveFormat.HDF5_NC);
    }
    @Test public void testComparePyXParH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_XML, true, DataSaveFormat.HDF5_NC);
    }



       

    @Test public void testComparePyH5SerText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_HDF5, false, DataSaveFormat.TEXT_NC);
    }
 /*
    @Test public void testComparePyH5ParText() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_HDF5, true, DataSaveFormat.TEXT_NC);
    }*/
    @Test public void testComparePyH5SerH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_HDF5, false, DataSaveFormat.HDF5_NC);
    }
    /*@Test public void testComparePyH5ParH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_HDF5, true, DataSaveFormat.HDF5_NC);
    }

    */


/*

    @Test public void testComparePyXml() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_XML);
    }

    @Test public void testComparePyH5() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        compareSims(NeuronFileManager.RUN_PYTHON_HDF5);
    }*/

    private void compareSims(int runMode, boolean parallel, DataSaveFormat dsf) throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException
    {
        assumeTrue(MainTest.testPNEURON());

        System.out.println("---  compareSims "+runMode+"...");
        
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            System.out.println("*** Not running testGenerateParallelHoc, as this is a Windows system!");
            assumeTrue(!GeneralUtils.isWindowsBasedPlatform());
        }

        ProjectManager pm = loadProject("testProjects/TestParallel/TestParallel.neuro.xml");
        
        Project proj = pm.getCurrentProject();
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        MpiSettings mpiSettings = new MpiSettings();

        sc.setMpiConf(mpiSettings.getMpiConfiguration(MpiSettings.LOCAL_SERIAL));
     
        System.out.println("Running sim in serial/hoc/text data mode");
        
        pm.doGenerate(sc.getName(), 1234);
        
        int wait = 1000;
        
        while(pm.isGenerating())
        {
            Thread.sleep(wait);
        }
        
        int numGenSerial = proj.generatedCellPositions.getNumberInAllCellGroups();
        
        System.out.println("Project: "+ proj.getProjectFileName()+" loaded and "
                +numGenSerial+" cells generated using: "+ sc.getMpiConf());
        
        String simNameSerial = "Test";
        
        proj.simulationParameters.setReference(simNameSerial);
        
        File simDirSerial = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simNameSerial);
        
        if (simDirSerial.exists())
            simDirSerial.delete();

        proj.neuronSettings.setDataSaveFormat(DataSaveFormat.TEXT_NC);
        
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
        


        //////////////////////////////////////////////////////////////////////////

        String suffix = "_hoc";
        if (runMode==NeuronFileManager.RUN_PYTHON_XML)
            suffix = "_py_xml";
        else if(runMode == NeuronFileManager.RUN_PYTHON_HDF5)
            suffix = "_py_h5";

        if (parallel)
            suffix = suffix + "__par";
        else
            suffix = suffix + "__ser";

        if (dsf.equals(DataSaveFormat.TEXT_NC))
            suffix = suffix + "__textD";
        else
            suffix = suffix + "__h5D";

        System.out.println("Running SAME sim in (parallel/h5 data/python) mode");

        if (parallel)
            sc.setMpiConf(mpiSettings.getMpiConfiguration(MpiSettings.LOCAL_4PROC));


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


        String simNameParallel0 = "Test"+suffix;

        proj.simulationParameters.setReference(simNameParallel0);

        File simDirParallel0 = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simNameParallel0);

        if (simDirParallel0.exists())
            simDirParallel0.delete();
        
        proj.neuronSettings.setDataSaveFormat(dsf);

        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);

        proj.neuronFileManager.setQuitAfterRun(true);

        proj.neuronFileManager.generateTheNeuronFiles(sc, null, runMode, 1234);

        mainHoc = proj.neuronFileManager.getMainHocFile();


        assertTrue(mainHoc.exists());

        System.out.println("Created hoc files, including: "+mainHoc);


        prm = new ProcessManager(mainHoc);

        success = prm.compileFileWithNeuron(true, false);

        assertTrue(success);


        System.out.println("Compiled NEURON files: "+ success);

        pm.doRunNeuron(sc);

        System.out.println("Run NEURON files");


        SimulationData simDataParallel0 = new SimulationData(simDirParallel0, false);

        timesFile = simDataParallel0.getTimesFile();


        while (!timesFile.exists())
        {
            System.out.println("Waiting for file to be created: "+ timesFile.getAbsolutePath());
            Thread.sleep(wait);
        }

        assertTrue(timesFile.exists());

        simDataParallel0.initialise();

        while(!simDataParallel0.isDataLoaded())
        {
            System.out.println("Waiting for data to be loaded");
            Thread.sleep(wait);
        }

        numRecordings = simDataParallel0.getCellSegRefs(false).size();

        assertEquals(numRecordings, numGenParallel);

        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simDataParallel0.getSimulationDirectory().getAbsolutePath());

        for(String cg: sc.getCellGroups())
        {
            for(int j=0;j<proj.generatedCellPositions.getNumberInCellGroup(cg);j++)
            {
                String ref = SimulationData.getCellRef(cg, j);

                double[] voltsSerial = simDataSerial.getVoltageAtAllTimes(ref);
                double[] voltsParallel = simDataParallel0.getVoltageAtAllTimes(ref);

                assertEquals(voltsParallel.length, voltsSerial.length);

                double tolerance = 0;

                if (dsf.equals(DataSaveFormat.HDF5_NC))
                    tolerance = 0.0001;

                if (runMode == NeuronFileManager.RUN_PYTHON_HDF5)
                    tolerance = 0.002;

                for(int i=0;i<voltsSerial.length;i++)
                {
                    assertEquals("Checking point "+i+" of cell ref: "+ref+", tolerance: "+tolerance+"mV in "+simNameParallel0, voltsParallel[i], voltsSerial[i], tolerance);
                }
                System.out.println("Parallel data agrees exactly with serial for: "+ref);
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