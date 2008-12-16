/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class ElecInputGeneratorTest {

    ProjectManager pm = null;
    
    public ElecInputGeneratorTest() 
    {
    }


    @Before
    public void setUp() 
    {
        
        System.out.println("---------------   setUp() ElecInputGeneratorTest");
        String projName = "TestStims";
        File projDir = new File("testProjects/"+ projName);
        File projFile = new File(projDir, projName+ProjectStructure.getProjectFileExtension());
        
        System.out.println("File: "+ projFile.getAbsolutePath());
        
        pm = new ProjectManager();
        
        try 
        {
            pm.loadProject(projFile);
        
            System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());
            
            
        } 
        catch (ProjectFileParsingException ex) 
        {
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
    }
    
    
    private void generate() throws InterruptedException 
    {       
        Project proj = pm.getCurrentProject();
        
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        generate(sc);
    }
    
    
    private void generate(SimConfig sc) throws InterruptedException 
    {
        Project proj = pm.getCurrentProject();
                
        pm.doGenerate(sc.getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
    }

    
    @Test
    public void testAll() throws InterruptedException
    {
        
        System.out.println("---  testAll()");
        
        Project proj = pm.getCurrentProject();
        
        
        proj.elecInputInfo.getAllStims().get(0).setCellChooser(new AllCells());
        
        proj.elecInputInfo.getAllStims().get(1).setCellChooser(new FixedNumberCells(4));
        proj.elecInputInfo.getAllStims().get(2).setCellChooser(new IndividualCells("0,1,2"));
        proj.elecInputInfo.getAllStims().get(3).setCellChooser(new PercentageOfCells(50));
        proj.elecInputInfo.getAllStims().get(4).setCellChooser(new RegionAssociatedCells(proj.regionsInfo.getAllRegionNames()[0]));
        
        generate();  
        
        String cg1 = proj.cellGroupsInfo.getCellGroupNameAt(0);
         
        int numCells = proj.generatedCellPositions.getNumberInCellGroup(cg1);
        
        int numStims0 = proj.generatedElecInputs.getNumberSingleInputs(proj.elecInputInfo.getAllStimRefs().get(0));
        
        assertEquals(numCells, numStims0);
        
        int numStims1 = proj.generatedElecInputs.getNumberSingleInputs(proj.elecInputInfo.getAllStimRefs().get(1));
        
        assertEquals(4, numStims1);
        
        int numStims2 = proj.generatedElecInputs.getNumberSingleInputs(proj.elecInputInfo.getAllStimRefs().get(2));
        
        assertEquals(3, numStims2);
        
        int numStims3 = proj.generatedElecInputs.getNumberSingleInputs(proj.elecInputInfo.getAllStimRefs().get(3));
        
        assertEquals(numCells/2, numStims3);        
        
        int numStims4 = proj.generatedElecInputs.getNumberSingleInputs(proj.elecInputInfo.getAllStimRefs().get(4));
        
        assertEquals(numCells, numStims4);
        
        
    }    
    
    
    
    public static void main(String[] args)
    {
        ElecInputGeneratorTest ct = new ElecInputGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
    }

}