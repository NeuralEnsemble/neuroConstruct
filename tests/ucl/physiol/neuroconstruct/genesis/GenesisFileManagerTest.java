/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.genesis;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
        
        int wait = 2000;
        if (GeneralUtils.isWindowsBasedPlatform())
            wait = 6000;
        
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
        
        proj.genesisSettings.setGraphicsMode(false);
        
        proj.genesisFileManager.setQuitAfterRun(true);
   
        proj.genesisFileManager.generateTheGenesisFiles(sc, null, new GenesisCompartmentalisation(), 1234);
        
           
        File mainFile = new File(proj.genesisFileManager.getMainGenesisFileName());
        
        
        assertTrue(mainFile.exists());
        
        System.out.println("Created files, including: "+mainFile);
         
        
    
        
        proj.genesisFileManager.runGenesisFile(true);
        
        System.out.println("Run GENESIS files");
        
         
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