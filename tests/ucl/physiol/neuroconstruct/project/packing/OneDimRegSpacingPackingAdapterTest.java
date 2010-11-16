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

package ucl.physiol.neuroconstruct.project.packing;

import java.awt.Color;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.project.*;
import static org.junit.Assert.*;

/**
 *
 * @author Padraig
 */
public class OneDimRegSpacingPackingAdapterTest {

    public OneDimRegSpacingPackingAdapterTest() {
    }


    @Before
    public void setUp() {
        System.out.println("---------------   setUp() OneDimRegSpacingPackingAdapterTest");
    }

    /**
     * Test of getNumberCells method, of class OneDimRegSpacingPackingAdapter.
     */
    @Test
    public void testGenerating() throws NamingException, InterruptedException, CellPackingException, NoProjectLoadedException
    {
        System.out.println("---  testGetNumberCells()");
        
        ProjectManager pm = new ProjectManager();
        
        String projName = "TestingFrameworkProject";
    
        File projDir = new File(MainTest.getTempProjectDirectory()+"/"+projName);// won't be saved...
    
        Project proj = Project.createNewProject(projDir.getAbsolutePath(), projName, null);

        System.out.println("Created proj: "+proj.getProjectFullFileName());
        
        pm.setCurrentProject(proj);
        
        String regionName = "DummyRegion";
        String cellType = "Dummy";
        String cellGroup = "ADummyGroup";
        
        int numInGroup = 17;
         
        Cell dummyCell = new OneSegment(cellType);
        RectangularBox box = new RectangularBox(0, 0, 0, 100, 100, 100);
        
        proj.regionsInfo.addRow(regionName, box, Color.white);
        
        proj.cellManager.addCellType(dummyCell);
        
        OneDimRegSpacingPackingAdapter oneDim = new OneDimRegSpacingPackingAdapter();

        System.out.println("Packing: "+ oneDim);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.NUMBER_PARAM_NAME, numInGroup);
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_NAME, OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_EXTEND);
        
        proj.cellGroupsInfo.addRow(cellGroup, cellType, regionName, Color.red, oneDim, 1);


        System.out.println("CGs: "+ proj.cellGroupsInfo.getAllCellGroupNames());
        
        proj.simConfigInfo.getDefaultSimConfig().addCellGroup(cellGroup);
        
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_X);

        System.out.println("Packing 2: "+ oneDim);

        System.out.println("CGs: "+ proj.cellGroupsInfo.getAllCellGroupNames());
        
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        assertEquals(box.getLowestXValue(), proj.generatedCellPositions.getOneCellPosition(cellGroup, 0).x,0);
        assertEquals(box.getHighestXValue(), proj.generatedCellPositions.getOneCellPosition(cellGroup, proj.generatedCellPositions.getNumberInAllCellGroups()-1).x,0);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_Y);

        System.out.println("Packing 3: "+ oneDim);
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_Z);
        
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        
        
        // Change edge policy
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_NAME, OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_NO_EXTEND);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_X);
        
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_Y);
        
        generate(pm);   
        
        float radius = dummyCell.getFirstSomaSegment().getRadius();
        
        assertEquals(box.getLowestYValue()+radius, proj.generatedCellPositions.getOneCellPosition(cellGroup, 0).y,0);
        //System.out.println(proj.generatedCellPositions.getAllPositionRecords());
        assertEquals(box.getHighestYValue()-radius, proj.generatedCellPositions.getOneCellPosition(cellGroup, proj.generatedCellPositions.getNumberInAllCellGroups()-1).y,0);
           
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_Z);
        
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        
    }
    
    public void generate(ProjectManager pm) throws InterruptedException
    {
        pm.doGenerate(pm.getCurrentProject().simConfigInfo.getDefaultSimConfig().getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println("Generated proj with: "+ pm.getCurrentProject().generatedCellPositions.getNumberInAllCellGroups()+" cells");
    }

    
    public static void main(String[] args)
    {
        OneDimRegSpacingPackingAdapterTest ct = new OneDimRegSpacingPackingAdapterTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }

}