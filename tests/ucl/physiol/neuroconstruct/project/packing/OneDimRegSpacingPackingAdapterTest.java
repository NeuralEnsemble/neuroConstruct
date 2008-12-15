/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project.packing;

import java.awt.Color;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
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
    }

    /**
     * Test of getNumberCells method, of class OneDimRegSpacingPackingAdapter.
     */
    @Test
    public void testGenerating() throws NamingException, InterruptedException, CellPackingException
    {
        System.out.println("---  testGetNumberCells()");
        
        ProjectManager pm = new ProjectManager();
        
        String projName = "TestingFrameworkProject";
    
        File projDir = new File("..\\temp");// won't be saved...
    
        Project proj = Project.createNewProject(projDir.getAbsolutePath(), projName, null);
        
        pm.setCurrentProject(proj);
        
        String regionName = "DummyRegion";
        String cellType = "Dummy";
        String cellGroup = "DummyGroup";
        
        int numInGroup = 17;
         
        Cell dummyCell = new OneSegment(cellType);
        RectangularBox box = new RectangularBox(0, 0, 0, 100, 100, 100);
        
        proj.regionsInfo.addRow(regionName, box, Color.white);
        
        proj.cellManager.addCellType(dummyCell);
        
        OneDimRegSpacingPackingAdapter oneDim = new OneDimRegSpacingPackingAdapter();
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.NUMBER_PARAM_NAME, numInGroup);
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_NAME, OneDimRegSpacingPackingAdapter.EDGE_POLICY_PARAM_EXTEND);
        
        proj.cellGroupsInfo.addRow(cellGroup, cellType, regionName, Color.red, oneDim, 1);
        
        proj.simConfigInfo.getDefaultSimConfig().addCellGroup(cellGroup);
        
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_X);
        
        generate(pm);      
        
        assertEquals(proj.generatedCellPositions.getNumberInAllCellGroups(), numInGroup);
        assertEquals(box.getLowestXValue(), proj.generatedCellPositions.getOneCellPosition(cellGroup, 0).x,0);
        assertEquals(box.getHighestXValue(), proj.generatedCellPositions.getOneCellPosition(cellGroup, proj.generatedCellPositions.getNumberInAllCellGroups()-1).x,0);
        
        oneDim.setParameter(OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_NAME, OneDimRegSpacingPackingAdapter.DIMENSION_PARAM_Y);
        
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