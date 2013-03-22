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
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.GenesisCompartmentalisation;
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
public class GenesisMorphologyGeneratorTest
{

    public GenesisMorphologyGeneratorTest()
    {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() GenesisMorphologyGeneratorTest");
    }
    
    private ProjectManager loadProject(String projectFile) throws ProjectFileParsingException
    {

        ProjectManager pm = new ProjectManager();
        
        pm.loadProject(new File(projectFile));
        
        return pm;
    }
    
    @Test public void testGenerateGenesis() throws ProjectFileParsingException, InterruptedException, GenesisException, IOException, SimulationDataException
    {
        assumeTrue(MainTest.testGENESIS());

        System.out.println("---  testGenerateGenesis...");
        
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
        
        if (simDir.exists())
            simDir.delete();

        proj.genesisSettings.setGraphicsMode(GenesisSettings.GraphicsMode.NO_CONSOLE);
        
        proj.genesisFileManager.setQuitAfterRun(true);
        
        proj.genesisFileManager.generateTheGenesisFiles(sc, null, new GenesisCompartmentalisation(), 1234);

        File mainFile = new File(proj.genesisFileManager.getMainGenesisFileName());
        
        
        assertTrue(mainFile.exists());
        
        System.out.println("Created files, including: "+mainFile);
        
        

        proj.genesisFileManager.runGenesisFile();

        System.out.println("Run GENESIS files");
        
        
        SimulationData simData = new SimulationData(simDir, false);
        
        File timesFile = simData.getTimesFile();
        
        while (!timesFile.exists())
        {
            System.out.println("Waiting for generation of "+ timesFile.getAbsolutePath());
            Thread.sleep(wait); // Shouldn't take longer than this
        }
        
        assertTrue(timesFile.exists());
        
        simData.initialise();

        File passFile = new File(simDir, "passed");

        // Sometimes cygwin on windows puts file here...
        File altPassFile = new File("c:\\cygwin\\home\\"+System.getProperty("user.name")+"\\", "passed");

        if (!passFile.exists())
            passFile = altPassFile;
        
        
        System.out.println("Checking existence of pass file: "+ passFile.getAbsolutePath());

        Thread.sleep(wait); // Shouldn't take longer than this
        
        assertTrue(passFile.exists());
        
        System.out.println("File exists!!! Contents:\n----------------------------\n"+
                GeneralUtils.readShortFile(passFile)+
                "\n----------------------------\n");

        
        
    }
    
    
    
    public static void main(String[] args)
    {
        GenesisMorphologyGeneratorTest ct = new GenesisMorphologyGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
    
    

}