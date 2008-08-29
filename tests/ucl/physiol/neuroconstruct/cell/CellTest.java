/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class CellTest {
    
    static String testGroup = "TestGroup";

    public CellTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() CellTest");
    }

    @After
    public void tearDown() {
    }
    
    // Get a cell with (almost) all features non null
    
    public static Cell getDetailedCell() throws EquationException
    {
        Cell cell = new Cell();
        
        cell.setInstanceName("CellName");
        cell.setCellDescription("Cell Desc");
        NumberGenerator ip = new NumberGenerator();
        ip.initialiseAsRandomFloatGenerator(-70, -90);
        cell.setInitialPotential(ip);
        
        
        Section sec1 = new Section("SomaSection");
        sec1.setComment("Commentary");
        sec1.setNumberInternalDivisions(2);
        sec1.setStartPointPositionX(2);
        sec1.setStartPointPositionY(2);
        sec1.setStartPointPositionZ(2);
        sec1.setStartRadius(3);
        
        sec1.addToGroup(Section.SOMA_GROUP);
        
        
        
        Segment somaSeg = cell.addFirstSomaSegment(2, 3, "SomaSeg", new Point3f(1,2,3), new Point3f(4,2,3), sec1);
        
        Segment dendSeg = cell.addDendriticSegment(2, "DendSeg", new Point3f(1,2,3), somaSeg, 0.5f, "BasalDends", true);
        dendSeg.getSection().addToGroup(testGroup);
        
        Segment axSeg = cell.addAxonalSegment(2, "AxonSeg", new Point3f(-1,-2,-3), somaSeg, 0f, "InitialSeg");
        axSeg.getSection().addToGroup(testGroup);
        
        cell.associateGroupWithSpecAxRes("all", 111);
        cell.associateGroupWithSpecCap("all", 222);
        
        cell.associateGroupWithChanMech(testGroup, new ChannelMechanism("pas", 123));
        
        ParameterisedGroup pg = new ParameterisedGroup("OneToEnd", 
                                               Section.ALL, 
                                               Metric.PATH_LENGTH_FROM_ROOT, 
                                               ProximalPref.MOST_PROX_AT_0, 
                                               DistalPref.MOST_DIST_AT_1);
        
        cell.getParameterisedGroups().add(pg);
        
        VariableMechanism vm = VariableMechanismTest.getVariableMechanism();
        
        cell.associateParamGroupWithVarMech(pg, vm);
        
        return cell;
    }
    
    
    @Test 
    public void testCloneAndEquals()  throws EquationException
    {
        System.out.println("---  testCloneAndEquals...");
        
        Cell cell1 = getDetailedCell();
        
        Cell cell2 = (Cell)cell1.clone();
        
        String compare = CellTopologyHelper.compare(cell1, cell2, false);
        
        System.out.println("Comparison 1: "+ compare);
        
        assertTrue(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);
        
        
        cell2.getAllSegments().get(0).getSection().addToGroup("fff");
        
        compare = CellTopologyHelper.compare(cell1, cell2, false);
        
        System.out.println("Comparison 2: "+ compare);
        
        
        assertFalse(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);
    }
    
    @Test 
    public void testIsGroup()  throws EquationException
    {
        System.out.println("---  testIsGroup...");
        
        Cell cell1 = getDetailedCell();
        
        assertTrue(cell1.isGroup(testGroup));
        assertTrue(cell1.isGroup(Section.ALL));
        assertTrue(cell1.isGroup(Section.SOMA_GROUP));
        assertFalse(cell1.isGroup("zhxfghdfg"));
        
    }
    
    public static void main(String[] args)
    {
        CellTest ct = new CellTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}