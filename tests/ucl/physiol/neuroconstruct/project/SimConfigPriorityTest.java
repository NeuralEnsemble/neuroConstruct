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
import org.junit.*;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;

import static org.junit.Assert.*;

/**
 *
 * @author Matteo
 */
public class SimConfigPriorityTest {
    
    ProjectManager pm = null;
    
    public SimConfigPriorityTest() 
    {
    }
    
    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SimConfigPriorityTest");
        String projName = "TestGenNetworks";
        File projDir = new File("testProjects/"+ projName);
        File projFile = ProjectStructure.findProjectFile(projDir);
        
        pm = new ProjectManager();

        try 
        {
            pm.loadProject(projFile);
        
            System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());
            
            
        } 
        catch (ProjectFileParsingException ex) 
        {
            System.out.println("FAILURE!");
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testSamePriority() throws InterruptedException, CellPackingException 
    {
        pm.getCurrentProject().cellGroupsInfo.setCellGroupPriority("SampleCellGroup", 7);
        pm.getCurrentProject().cellGroupsInfo.setCellGroupPriority("CellGroup_2", 7);
        pm.getCurrentProject().cellGroupsInfo.setCellGroupPriority("Pacemaker", 7);

        pm.doGenerate("Default Simulation Configuration", 12345);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println(pm.getCurrentProject().getProjectStatusAsString());
        
        assertTrue (pm.getCurrentProject().generatedCellPositions.getNumberInCellGroup("SampleCellGroup")>0);
        assertTrue (pm.getCurrentProject().generatedCellPositions.getNumberInCellGroup("CellGroup_2")>0);
        assertTrue (pm.getCurrentProject().generatedCellPositions.getNumberInCellGroup("Pacemaker")>0);

        System.out.println("All the cell groups are generated");

    
    }
    
    public static void main(String[] args)
    {
        SimConfigPriorityTest pt = new SimConfigPriorityTest();
        Result r = org.junit.runner.JUnitCore.runClasses(pt.getClass());
        MainTest.checkResults(r);
        
    }

}
