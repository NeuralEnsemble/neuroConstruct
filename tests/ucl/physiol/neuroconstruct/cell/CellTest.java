/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class CellTest {

    public CellTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SectionTest");
    }

    @After
    public void tearDown() {
    }
    
    // Get a cell with (almost) all features non null
    
    private Cell getDetailedCell()
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
        
        String group = "TestGroup";
        
        
        Segment somaSeg = cell.addFirstSomaSegment(2, 3, "SomaSeg", new Point3f(1,2,3), new Point3f(4,2,3), sec1);
        
        Segment dendSeg = cell.addDendriticSegment(2, "DendSeg", new Point3f(1,2,3), somaSeg, 0.5f, "BasalDends", true);
        dendSeg.getSection().addToGroup(group);
        
        Segment axSeg = cell.addAxonalSegment(2, "AxonSeg", new Point3f(-1,-2,-3), somaSeg, 0f, "InitialSeg");
        axSeg.getSection().addToGroup(group);
        
        cell.associateGroupWithSpecAxRes("all", 111);
        cell.associateGroupWithSpecCap("all", 222);
        
        cell.associateGroupWithChanMech(group, new ChannelMechanism("pas", 123));
        
        
        return cell;
    }
    
    
    @Test public void testCloneAndEquals() 
    {
        System.out.println("---  testCloneAndEquals...");
        
        Cell cell1 = getDetailedCell();
        
        Cell cell2 = (Cell)cell1.clone();
        //System.out.println("Cell 1: "+CellTopologyHelper.printDetails(cell1, null));
        //System.out.println("Cell 2: "+CellTopologyHelper.printDetails(cell2, null));
        
        //cell2.associateGroupWithSpecCap("all", 2222);
        
        String compare = CellTopologyHelper.compare(cell1, cell2, false);
        
        System.out.println("Comparison 1: "+ compare);
        
        assertTrue(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);
        
        
        cell2.getAllSegments().get(0).getSection().addToGroup("fff");
        
        compare = CellTopologyHelper.compare(cell1, cell2, false);
        
        System.out.println("Comparison 2: "+ compare);
        
        
        assertFalse(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);
    }


}