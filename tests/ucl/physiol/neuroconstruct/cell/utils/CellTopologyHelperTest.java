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

package ucl.physiol.neuroconstruct.cell.utils;

import ucl.physiol.neuroconstruct.cell.*;
import java.util.Vector;
import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class CellTopologyHelperTest {
    
    static String testGroup = "TestGroup";
    static String oldGroupStr = "OldGroup";
    private static final long serialVersionUID = -1542517048619766744L;

    public CellTopologyHelperTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() CellTopologyHelperTest");
    }

    @After
    public void tearDown() {
    }
    
    
    public static Cell getDetailedCell() throws EquationException
    {
        Cell cell = new Cell();
        
        cell.setInstanceName("CellName");
        cell.setCellDescription("Cell Desc");
        NumberGenerator ip = new NumberGenerator(0);
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
        
        
        
        Segment somaSeg = cell.addFirstSomaSegment(2, 3, "SomaSeg", new Point3f(0,0,0), new Point3f(10,0,0), sec1);
        
        Segment dendSeg = cell.addDendriticSegment(2, "DendSeg", new Point3f(0,100,0), somaSeg, 0, "BasalDends", true);
        dendSeg.getSection().addToGroup(testGroup);
        
        Segment axSeg = cell.addAxonalSegment(2, "AxonSeg", new Point3f(10,-100,0), somaSeg, 1f, "InitialSeg");
        axSeg.getSection().addToGroup(testGroup);
        
        cell.associateGroupWithSpecAxRes("all", 111);
        cell.associateGroupWithSpecCap("all", 222);
        
        cell.associateGroupWithChanMech(testGroup, new ChannelMechanism("pas", 123));
        
        ParameterisedGroup pg = new ParameterisedGroup("OneToEnd", 
                                               Section.ALL, 
                                               Metric.PATH_LENGTH_FROM_ROOT, 
                                               ProximalPref.MOST_PROX_AT_0, 
                                               DistalPref.MOST_DIST_AT_1,
                                               "ppp");
        
        cell.getParameterisedGroups().add(pg);
        
        VariableMechanism vm = VariableMechanismTest.getVariableMechanism();
        
        cell.associateParamGroupWithVarMech(pg, vm);
        
         // some changes added to test the rename group function
        
        dendSeg.getSection().addToGroup(oldGroupStr);
                
        cell.associateGroupWithSynapse(oldGroupStr, "SynType1");

        cell.associateGroupWithChanMech(oldGroupStr, new ChannelMechanism("chanMech1", 123));

        ApPropSpeed apPropSpeed1 = new ApPropSpeed();

        cell.associateGroupWithApPropSpeed(oldGroupStr, apPropSpeed1);

        cell.associateGroupWithSpecAxRes(oldGroupStr, serialVersionUID);

        cell.associateGroupWithSpecCap(oldGroupStr, serialVersionUID);

        ParameterisedGroup oldGroup = new ParameterisedGroup("ParaOldGroup", "OldGroup",
                ParameterisedGroup.Metric.PATH_LENGTH_FROM_ROOT, 
                ParameterisedGroup.ProximalPref.MOST_PROX_AT_0, 
                ParameterisedGroup.DistalPref.MOST_DIST_AT_1, "ggg");

        cell.getParameterisedGroups().add(oldGroup);
       
        return cell;
    }


    @Test
    public void testGetLengths()  throws EquationException
    {
        System.out.println("---  testGetLengths...");

        Cell cell = getDetailedCell();

        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.SOMA_GROUP), 10, 0);
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.DENDRITIC_GROUP), 100, 0);
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.AXONAL_GROUP), 110, 0);
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.ALL), 110, 0);

        SegmentLocation sl = new SegmentLocation(cell.getOnlyDendriticSegments().firstElement().getSegmentId(), 0.5f);

        assertEquals(CellTopologyHelper.getLengthFromRoot(cell, sl), 50, 0);

        SegmentLocation sl2 = new SegmentLocation(cell.getOnlyAxonalSegments().firstElement().getSegmentId(), 0.5f);

        assertEquals(CellTopologyHelper.getLengthFromRoot(cell, sl2), 60, 0);

    }
    
    
    public static void main(String[] args)
    {
        CellTopologyHelperTest ct = new CellTopologyHelperTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}