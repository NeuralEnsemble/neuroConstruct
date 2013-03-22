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
import java.util.HashMap;
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
        
        /*
         * cell topology:            dendrite
         *              o      soma o-----o
         *              |           |     |
         *              o-----------o     o 
         *              |   axon      
         *              |
         *              o
         */
        
        
        
        
        Segment somaSeg = cell.addFirstSomaSegment(2, 3, "SomaSeg", new Point3f(0,0,0), new Point3f(10,0,0), sec1);
        
        Segment dendSeg = cell.addDendriticSegment(2, "DendSeg", new Point3f(0,70,0), somaSeg, 0, "BasalDends", true);
        dendSeg.getSection().addToGroup(testGroup);
        
        Segment dendSeg2 = cell.addDendriticSegment(2, "DendSeg2", new Point3f(0,70,20), dendSeg, 1, "BasalDends", true);
        dendSeg2.getSection().addToGroup(testGroup);
        
        Segment axSeg = cell.addAxonalSegment(2, "AxonSeg", new Point3f(10,-100,0), somaSeg, 1f, "InitialSeg");
        axSeg.getSection().addToGroup(testGroup);
        
        Segment axSeg2 = cell.addAxonalSegment(2, "AxonSeg2", new Point3f(10,-100,30), axSeg, 1f, "InitialSeg");
        axSeg2.getSection().addToGroup(testGroup);
        
        Segment axSeg3 = cell.addAxonalSegment(2, "AxonSeg3", new Point3f(10,-100,-15), axSeg, 1f, "InitialSeg");
        axSeg3.getSection().addToGroup(testGroup);
        
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
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.DENDRITIC_GROUP), 90, 0);
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.AXONAL_GROUP), 140, 0);
        assertEquals(CellTopologyHelper.getMaxLengthFromRoot(cell, Section.ALL), 140, 0);
        
        HashMap<Integer, Float> expectedDistancesRootAll = new HashMap<Integer, Float>();
        expectedDistancesRootAll.put(0, 0f);
        expectedDistancesRootAll.put(1, 0f);
        expectedDistancesRootAll.put(2, 70f);
        expectedDistancesRootAll.put(3, 10f);
        expectedDistancesRootAll.put(4, 110f);
        expectedDistancesRootAll.put(5, 110f);
        assertEquals(expectedDistancesRootAll, CellTopologyHelper.getSegmentDistancesFromRoot(cell, Section.ALL));      
        HashMap<Integer, Float> expectedDistancesRootDend = new HashMap<Integer, Float>();
        expectedDistancesRootDend.put(1, 0f);
        expectedDistancesRootDend.put(2, 70f);
        assertEquals(expectedDistancesRootDend, CellTopologyHelper.getSegmentDistancesFromRoot(cell, Section.DENDRITIC_GROUP));

        HashMap<Integer, Float> expectedDistancesSomaAll = new HashMap<Integer, Float>();
        expectedDistancesSomaAll.put(0, 0f);
        expectedDistancesSomaAll.put(1, 0f);
        expectedDistancesSomaAll.put(2, 70f);
        expectedDistancesSomaAll.put(3, 0f);
        expectedDistancesSomaAll.put(4, 100f);
        expectedDistancesSomaAll.put(5, 100f);
        assertEquals(expectedDistancesSomaAll, CellTopologyHelper.getSegmentDistancesFromSoma(cell, Section.ALL));      
        HashMap<Integer, Float> expectedDistancesSomaDend = new HashMap<Integer, Float>();
        expectedDistancesSomaDend.put(1, 0f);
        expectedDistancesSomaDend.put(2, 70f);
        assertEquals(expectedDistancesSomaDend, CellTopologyHelper.getSegmentDistancesFromSoma(cell, Section.DENDRITIC_GROUP));
     
        SegmentLocation sl = new SegmentLocation(cell.getOnlyDendriticSegments().firstElement().getSegmentId(), 0.5f);

        assertEquals(CellTopologyHelper.getLengthFromRoot(cell, sl), 35, 0);

        SegmentLocation sl2 = new SegmentLocation(cell.getOnlyAxonalSegments().firstElement().getSegmentId(), 0.5f);

        assertEquals(CellTopologyHelper.getLengthFromRoot(cell, sl2), 60, 0);
        
    }
    
    @Test
    public void testGetDistancesFromAncestor() throws EquationException
    {
        System.out.println("--- testGetDistancesFromAncestor ---");
        Cell cell = getDetailedCell();
        HashMap <Integer, Float> expectedDistancesFromSeg4 = new HashMap<Integer, Float>();
        expectedDistancesFromSeg4.put(3, 0.0f);
        expectedDistancesFromSeg4.put(0, 100.0f);
        assertEquals(expectedDistancesFromSeg4, CellTopologyHelper.getDistancesFromAncestorSegments(cell, 4));
        
        HashMap <Integer, Float> expectedDistancesFromSeg5 = new HashMap<Integer, Float>();
        expectedDistancesFromSeg5.put(3, 0.0f);
        expectedDistancesFromSeg5.put(0, 100.0f);
        assertEquals(expectedDistancesFromSeg5, CellTopologyHelper.getDistancesFromAncestorSegments(cell, 5));
        
    }
    
    
    public static void main(String[] args)
    {
        CellTopologyHelperTest ct = new CellTopologyHelperTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}