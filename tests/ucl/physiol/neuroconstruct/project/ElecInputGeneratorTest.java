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

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.simulation.IClampSettings;
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
        File projFile = ProjectStructure.findProjectFile(projDir);
        
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

        System.out.println("---    testing IClampSettings.clone()");
        IClampSettings existingStim = (IClampSettings) proj.elecInputInfo.getStim(0);
        IClampSettings newStim = (IClampSettings) existingStim.clone();
        try
        {
            assertEquals(existingStim.getCellChooser().toString(), newStim.getCellChooser().toString());
        }catch (java.lang.NullPointerException ex)
        {
            System.out.println("---        no cell chooser");
        }
        assertEquals(existingStim.getCellGroup(), newStim.getCellGroup());
        assertEquals(existingStim.getSegChooser().toString(), newStim.getSegChooser().toString());
        assertEquals(existingStim.toString(), newStim.toString());
        
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