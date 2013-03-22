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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessManager;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimulationData;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import ucl.physiol.neuroconstruct.simulation.SimulationDataException;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;

/**
 *
 * @author padraig
 */
public class NeuronTemplateGeneratorTest {

    public NeuronTemplateGeneratorTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() NeuronTemplateGeneratorTest");
    }
    
    private ProjectManager loadProject(String projectFile) throws ProjectFileParsingException
    {

        ProjectManager pm = new ProjectManager();
        
        pm.loadProject(new File(projectFile));
        
        return pm;
    }
    
    @Test public void testGenerateHoc() throws ProjectFileParsingException, InterruptedException, NeuronException, IOException, SimulationDataException 
    {
        assumeTrue(MainTest.testNEURON());

        System.out.println("---  testGenerateHoc...");
        
        ProjectManager pm = loadProject("testProjects/TestDetailedMorphs/TestDetailedMorphs.neuro.xml");
        
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
        
        System.out.println("Going to delete: "+ simDir.getAbsolutePath());
        
        if (simDir.exists())
        {
            for(File f: simDir.listFiles())
            {
                f.delete();
            }
        }

        
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
        
        
        SimulationData simData = new SimulationData(simDir, false);
        
        File timesFile = simData.getTimesFile();
        
        
        Thread.sleep(wait); // Shouldn't take longer than this

        if (!timesFile.exists())
        {
            System.out.println("Waiting for file to be created: "+ timesFile.getAbsolutePath());
            Thread.sleep(wait*3);
        }
        
        assertTrue(timesFile.exists());
        
        simData.initialise();
        
        File passFile = new File(simDir, "passed");
        
        
        System.out.println("Checking existence of pass file: "+ passFile.getAbsolutePath());
    
        
        assertTrue(passFile.exists());
        
        System.out.println("File exists!!! Contents:\n----------------------------\n"+
                GeneralUtils.readShortFile(passFile)+
                "\n----------------------------\n");

        
    }
    
    
    
    public static void main(String[] args)
    {
        NeuronTemplateGeneratorTest ct = new NeuronTemplateGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }

    

}