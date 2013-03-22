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

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
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
public class CellTest {
    
    static String testGroup = "TestGroup";
    static String oldGroupStr = "OldGroup";
    private static final long serialVersionUID = -1542517048619766744L;

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


        IonProperties ion2 = new IonProperties("na", 10, 100);
        IonProperties ion3 = new IonProperties("k", -77);
        IonProperties ion4 = new IonProperties("ca", 80);

        cell.associateGroupWithIonProperties(testGroup, ion3);
        cell.associateGroupWithIonProperties(Section.ALL, ion2);
        cell.associateGroupWithIonProperties(Section.ALL, ion4);
       
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
    
    
    
    @Test 
    public void testRenameGroup()  throws EquationException
    {
        
        System.out.println("---  testRenameGroup...");
        
        Cell cell1 = getDetailedCell(); 
        
        //System.out.println(CellTopologyHelper.printDetails(cell1, null));

        // testing the rename group function
        
        cell1.renameGroup(oldGroupStr, "NewGroup");
        
        assertTrue(cell1.isGroup("NewGroup"));
        
        assertFalse(cell1.isGroup(oldGroupStr));
        
        assertTrue(cell1.getSynapsesForGroup("NewGroup").contains("SynType1"));
        
        
        boolean foundGroup = false;
        
        for(Vector<String> groups: cell1.getSynapsesVsGroups().values())
        {
            assertFalse(groups.contains(oldGroupStr));
            if (groups.contains("NewGroup"))
                foundGroup = true;
        }
        assertTrue(foundGroup);
        
        
        foundGroup = false;
        
        for(Vector<String> groups: cell1.getSpecAxResVsGroups().values())
        {
            assertFalse(groups.contains(oldGroupStr));
            if (groups.contains("NewGroup"))
                foundGroup = true;
        }
        assertTrue(foundGroup);
        
        
        foundGroup = false;        
        
        for(Vector<String> groups: cell1.getSpecCapVsGroups().values())
        {
            assertFalse(groups.contains(oldGroupStr));
            if (groups.contains("NewGroup"))
                foundGroup = true;
        }
        assertTrue(foundGroup);
        
        
        foundGroup = false;        
        
        for(Vector<String> groups: cell1.getSpecCapVsGroups().values())
        {
            assertFalse(groups.contains(oldGroupStr));
            if (groups.contains("NewGroup"))
                foundGroup = true;
        }
        assertTrue(foundGroup);
        
        
        foundGroup = false;               
        
        
//        foundGroup = false;
//        
//        for (int i = 0; i < cell1.getParameterisedGroups().size(); i++) {      
//            ParameterisedGroup group = cell1.getParameterisedGroups().elementAt(i);
//            assertFalse(group.getGroup().equals(OldGroup));
//            if (group.getGroup().equals("NewGroup"))
//                foundGroup = true;
//        }
//        assertTrue(foundGroup);
        
        System.out.println("parameterisedGroups: "+cell1.getParameterisedGroups().toString());
    }

    @Test
    public void testChanMechGroup()  throws EquationException
    {
        System.out.println("---  testChanMechGroup...");

        Cell cell1 = getDetailedCell();
        ChannelMechanism cm = new ChannelMechanism("channer1", 678);

        cell1.associateGroupWithChanMech(testGroup, cm);

        ArrayList<ChannelMechanism> grps = cell1.getChanMechsForGroup(testGroup);

        assertTrue(grps.contains(cm));

        System.out.println(cell1.getChanMechsVsGroups());
        
        cell1.disassociateGroupFromChanMech(testGroup+"xx", cm);

        grps = cell1.getChanMechsForGroup(testGroup);

        assertTrue(grps.contains(cm));



        cell1.disassociateGroupFromChanMech(testGroup, cm);
        grps = cell1.getChanMechsForGroup(testGroup);

        System.out.println(cell1.getChanMechsVsGroups());

        assertFalse(grps.contains(cm));

    }


    @Test
    public void testIonPropsGroup()  throws EquationException
    {
        System.out.println("---  testIonPropsGroup...");

        Cell cell1 = getDetailedCell();
        IonProperties ip1 = new IonProperties("mg1", 2348);
        IonProperties ip2 = new IonProperties("mg2", 2345);


        cell1.associateGroupWithIonProperties(testGroup, ip1);
        cell1.associateGroupWithIonProperties(testGroup, ip2);

        System.out.println(cell1.getIonPropertiesVsGroups());

        ArrayList<IonProperties> ips = cell1.getIonPropertiesForGroup(testGroup);
        
        System.out.println("In "+testGroup+": "+ips);

        assertTrue(ips.contains(ip1));
        assertTrue(ips.contains(ip2));

        ips = cell1.getIonPropertiesForGroup(Section.ALL);

        assertFalse(ips.contains(ip1));

        cell1.disassociateGroupFromIonProperties(testGroup, ip2);
        ips = cell1.getIonPropertiesForGroup(testGroup);

        System.out.println("In "+testGroup+": "+ips);
        assertFalse(ips.contains(ip2));
        assertTrue(ips.contains(ip1));



    }
    
    public static void main(String[] args)
    {
        CellTest ct = new CellTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}