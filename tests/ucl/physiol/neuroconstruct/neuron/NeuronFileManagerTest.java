/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.neuron;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
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

}