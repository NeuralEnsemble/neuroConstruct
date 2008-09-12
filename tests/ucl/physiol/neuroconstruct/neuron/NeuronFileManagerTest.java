/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.neuron;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessManager;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimulationData;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.simulation.SimulationDataException;
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
    
    @Test public void testGenerateHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException 
    {
        System.out.println("---  testGenerateHoc...");
        
        ProjectManager pm = loadProject("testProjects/TestGenNetworks/TestGenNetworks.neuro.xml");
        
        Project proj = pm.getCurrentProject();
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
                
        pm.doGenerate(sc.getName(), 1234);
        
        int wait = 2000;
        if (GeneralUtils.isWindowsBasedPlatform())
            wait = 4000;
        
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
        
        proj.neuronSettings.setGraphicsMode(false);
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
        
        
        SimulationData simData = new SimulationData(simDir, false);
        
        File timesFile = simData.getTimesFile();
        
        
        Thread.sleep(wait); // Shouldn't take longer than this
        
        assertTrue(timesFile.exists());
        
        simData.initialise();
        
        
        int numRecordings = simData.getCellSegRefs(false).size();
        
        assertEquals(numRecordings, numGen);
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());
        
        double[] volts = simData.getVoltageAtAllTimes(SimulationData.getCellRef(sc.getCellGroups().get(0), 0));
        
        assertEquals(volts.length, 1 + (sc.getSimDuration()/proj.simulationParameters.getDt()), 0);
        
        
    }
    
    
    
    @Test public void testGenerateParallelHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException 
    {
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
        
        int wait = 3000;
        
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
        
        proj.neuronSettings.setGraphicsMode(false);
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
        
        
        SimulationData simDataSerial = new SimulationData(simDirSerial, false);
        
        File timesFile = simDataSerial.getTimesFile();
        
        
        Thread.sleep(wait); // Shouldn't take longer than this
        
        assertTrue(timesFile.exists());
        
        simDataSerial.initialise();
        
        
        int numRecordings = simDataSerial.getCellSegRefs(false).size();
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simDataSerial.getSimulationDirectory().getAbsolutePath());
        
        assertEquals(numRecordings, numGenSerial);
        
        

        System.out.println("Running sim in parallel mode");
        
        MpiSettings mpis = new MpiSettings();
        sc.setMpiConf(mpis.getMpiConfiguration(MpiSettings.LOCAL_4PROC));
        
                
        pm.doGenerate(sc.getName(), 1234);
        
        wait = 4000;  // Give longer to set up parallel
        
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
        
        proj.neuronSettings.setGraphicsMode(false);
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
        
        
        Thread.sleep(wait); // Shouldn't take longer than this
        
        if (!timesFile.exists())
            Thread.sleep(wait);
        
        assertTrue(timesFile.exists());
        
        simDataParallel.initialise();
        
        
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
    
    
    
    

}