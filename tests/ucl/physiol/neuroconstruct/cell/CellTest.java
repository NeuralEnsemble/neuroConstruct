/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;

/**
 *
 * @author padraig
 */
public class CellTest {

    public CellTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getAllSegments method, of class Cell.
     */
    @Test
    public void testGetAllSegments() {
        System.out.println("getAllSegments");
        Cell instance = new Cell();
        Vector<Segment> expResult = null;
        Vector<Segment> result = instance.getAllSegments();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAxonalArbours method, of class Cell.
     */
    @Test
    public void testGetAxonalArbours() {
        System.out.println("getAxonalArbours");
        Cell instance = new Cell();
        Vector<AxonalConnRegion> expResult = null;
        Vector<AxonalConnRegion> result = instance.getAxonalArbours();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAxonalArbour method, of class Cell.
     */
    @Test
    public void testDeleteAxonalArbour() {
        System.out.println("deleteAxonalArbour");
        String aaName = "";
        Cell instance = new Cell();
        instance.deleteAxonalArbour(aaName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAxonalArbours method, of class Cell.
     */
    @Test
    public void testSetAxonalArbours() {
        System.out.println("setAxonalArbours");
        Vector<AxonalConnRegion> axonalArbours = null;
        Cell instance = new Cell();
        instance.setAxonalArbours(axonalArbours);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAxonalArbour method, of class Cell.
     */
    @Test
    public void testAddAxonalArbour() {
        System.out.println("addAxonalArbour");
        AxonalConnRegion axonalArbour = null;
        Cell instance = new Cell();
        instance.addAxonalArbour(axonalArbour);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateAxonalArbour method, of class Cell.
     */
    @Test
    public void testUpdateAxonalArbour() {
        System.out.println("updateAxonalArbour");
        AxonalConnRegion axonalArbour = null;
        Cell instance = new Cell();
        instance.updateAxonalArbour(axonalArbour);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getParameterisedGroups method, of class Cell.
     */
    @Test
    public void testGetParameterisedGroups() {
        System.out.println("getParameterisedGroups");
        Cell instance = new Cell();
        Vector<ParameterisedGroup> expResult = null;
        Vector<ParameterisedGroup> result = instance.getParameterisedGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteParameterisedGroup method, of class Cell.
     */
    @Test
    public void testDeleteParameterisedGroup() {
        System.out.println("deleteParameterisedGroup");
        String name = "";
        Cell instance = new Cell();
        instance.deleteParameterisedGroup(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setParameterisedGroups method, of class Cell.
     */
    @Test
    public void testSetParameterisedGroups() {
        System.out.println("setParameterisedGroups");
        Vector<ParameterisedGroup> pgs = null;
        Cell instance = new Cell();
        instance.setParameterisedGroups(pgs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addParameterisedGroup method, of class Cell.
     */
    @Test
    public void testAddParameterisedGroup() {
        System.out.println("addParameterisedGroup");
        ParameterisedGroup pg = null;
        Cell instance = new Cell();
        instance.addParameterisedGroup(pg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateParameterisedGroup method, of class Cell.
     */
    @Test
    public void testUpdateParameterisedGroup() {
        System.out.println("updateParameterisedGroup");
        ParameterisedGroup pg = null;
        Cell instance = new Cell();
        instance.updateParameterisedGroup(pg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExplicitlyModelledSegments method, of class Cell.
     */
    @Test
    public void testGetExplicitlyModelledSegments() {
        System.out.println("getExplicitlyModelledSegments");
        Cell instance = new Cell();
        Vector<Segment> expResult = null;
        Vector<Segment> result = instance.getExplicitlyModelledSegments();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOnlySomaSegments method, of class Cell.
     */
    @Test
    public void testGetOnlySomaSegments() {
        System.out.println("getOnlySomaSegments");
        Cell instance = new Cell();
        Vector<Segment> expResult = null;
        Vector<Segment> result = instance.getOnlySomaSegments();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOnlyAxonalSegments method, of class Cell.
     */
    @Test
    public void testGetOnlyAxonalSegments() {
        System.out.println("getOnlyAxonalSegments");
        Cell instance = new Cell();
        Vector<Segment> expResult = null;
        Vector<Segment> result = instance.getOnlyAxonalSegments();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOnlyDendriticSegments method, of class Cell.
     */
    @Test
    public void testGetOnlyDendriticSegments() {
        System.out.println("getOnlyDendriticSegments");
        Cell instance = new Cell();
        Vector<Segment> expResult = null;
        Vector<Segment> result = instance.getOnlyDendriticSegments();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addDendriticSegment method, of class Cell.
     */
    @Test
    public void testAddDendriticSegment() {
        System.out.println("addDendriticSegment");
        float radius = 0.0F;
        String name = "";
        Point3f endPointPosn = null;
        Segment parent = null;
        float fractionAlongParentSegment = 0.0F;
        String sectionName = "";
        boolean inheritParentsRadius = false;
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.addDendriticSegment(radius, name, endPointPosn, parent, fractionAlongParentSegment, sectionName, inheritParentsRadius);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAxonalSegment method, of class Cell.
     */
    @Test
    public void testAddAxonalSegment() {
        System.out.println("addAxonalSegment");
        float radius = 0.0F;
        String name = "";
        Point3f endPointPosn = null;
        Segment parent = null;
        float fractionAlongParentSegment = 0.0F;
        String sectionName = "";
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.addAxonalSegment(radius, name, endPointPosn, parent, fractionAlongParentSegment, sectionName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addSomaSegment method, of class Cell.
     */
    @Test
    public void testAddSomaSegment() {
        System.out.println("addSomaSegment");
        float radius = 0.0F;
        String name = "";
        Point3f endPointPosn = null;
        Segment parent = null;
        Section section = null;
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.addSomaSegment(radius, name, endPointPosn, parent, section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addFirstSomaSegment method, of class Cell.
     */
    @Test
    public void testAddFirstSomaSegment() {
        System.out.println("addFirstSomaSegment");
        float startRadius = 0.0F;
        float endRadius = 0.0F;
        String name = "";
        Point3f startPointPosn = null;
        Point3f endPointPosn = null;
        Section section = null;
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.addFirstSomaSegment(startRadius, endRadius, name, startPointPosn, endPointPosn, section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllSegmentsInSection method, of class Cell.
     */
    @Test
    public void testGetAllSegmentsInSection() {
        System.out.println("getAllSegmentsInSection");
        Section section = null;
        Cell instance = new Cell();
        LinkedList<Segment> expResult = null;
        LinkedList<Segment> result = instance.getAllSegmentsInSection(section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllSections method, of class Cell.
     */
    @Test
    public void testGetAllSections() {
        System.out.println("getAllSections");
        Cell instance = new Cell();
        ArrayList<Section> expResult = null;
        ArrayList<Section> result = instance.getAllSections();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSectionsInGroup method, of class Cell.
     */
    @Test
    public void testGetSectionsInGroup() {
        System.out.println("getSectionsInGroup");
        String group = "";
        Cell instance = new Cell();
        ArrayList<Section> expResult = null;
        ArrayList<Section> result = instance.getSectionsInGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentsInGroup method, of class Cell.
     */
    @Test
    public void testGetSegmentsInGroup() {
        System.out.println("getSegmentsInGroup");
        String group = "";
        Cell instance = new Cell();
        ArrayList<Segment> expResult = null;
        ArrayList<Segment> result = instance.getSegmentsInGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFirstSomaSegment method, of class Cell.
     */
    @Test
    public void testGetFirstSomaSegment() {
        System.out.println("getFirstSomaSegment");
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.getFirstSomaSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentWithId method, of class Cell.
     */
    @Test
    public void testGetSegmentWithId() {
        System.out.println("getSegmentWithId");
        int id = 0;
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.getSegmentWithId(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentWithName method, of class Cell.
     */
    @Test
    public void testGetSegmentWithName() {
        System.out.println("getSegmentWithName");
        String segName = "";
        boolean allowSimFriendlyName = false;
        Cell instance = new Cell();
        Segment expResult = null;
        Segment result = instance.getSegmentWithName(segName, allowSimFriendlyName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNextSegmentId method, of class Cell.
     */
    @Test
    public void testGetNextSegmentId() {
        System.out.println("getNextSegmentId");
        Cell instance = new Cell();
        int expResult = 0;
        int result = instance.getNextSegmentId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCellDescription method, of class Cell.
     */
    @Test
    public void testGetCellDescription() {
        System.out.println("getCellDescription");
        Cell instance = new Cell();
        String expResult = "";
        String result = instance.getCellDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInstanceName method, of class Cell.
     */
    @Test
    public void testGetInstanceName() {
        System.out.println("getInstanceName");
        Cell instance = new Cell();
        String expResult = "";
        String result = instance.getInstanceName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllAllowedSynapseTypes method, of class Cell.
     */
    @Test
    public void testGetAllAllowedSynapseTypes() {
        System.out.println("getAllAllowedSynapseTypes");
        Cell instance = new Cell();
        ArrayList<String> expResult = null;
        ArrayList<String> result = instance.getAllAllowedSynapseTypes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllChannelMechanisms method, of class Cell.
     */
    @Test
    public void testGetAllChannelMechanisms() {
        System.out.println("getAllChannelMechanisms");
        boolean removeRepeats = false;
        Cell instance = new Cell();
        ArrayList<ChannelMechanism> expResult = null;
        ArrayList<ChannelMechanism> result = instance.getAllChannelMechanisms(removeRepeats);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefinedSpecAxResistances method, of class Cell.
     */
    @Test
    public void testGetDefinedSpecAxResistances() {
        System.out.println("getDefinedSpecAxResistances");
        Cell instance = new Cell();
        ArrayList<Float> expResult = null;
        ArrayList<Float> result = instance.getDefinedSpecAxResistances();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefinedSpecCaps method, of class Cell.
     */
    @Test
    public void testGetDefinedSpecCaps() {
        System.out.println("getDefinedSpecCaps");
        Cell instance = new Cell();
        ArrayList<Float> expResult = null;
        ArrayList<Float> result = instance.getDefinedSpecCaps();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllApPropSpeeds method, of class Cell.
     */
    @Test
    public void testGetAllApPropSpeeds() {
        System.out.println("getAllApPropSpeeds");
        Cell instance = new Cell();
        ArrayList<ApPropSpeed> expResult = null;
        ArrayList<ApPropSpeed> result = instance.getAllApPropSpeeds();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllGroupNames method, of class Cell.
     */
    @Test
    public void testGetAllGroupNames() {
        System.out.println("getAllGroupNames");
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getAllGroupNames();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Cell.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Cell instance = new Cell();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroupsWithSynapse method, of class Cell.
     */
    @Test
    public void testGetGroupsWithSynapse() {
        System.out.println("getGroupsWithSynapse");
        String synapseType = "";
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getGroupsWithSynapse(synapseType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroupsWithSpecAxRes method, of class Cell.
     */
    @Test
    public void testGetGroupsWithSpecAxRes() {
        System.out.println("getGroupsWithSpecAxRes");
        float specAxRes = 0.0F;
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getGroupsWithSpecAxRes(specAxRes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroupsWithSpecCap method, of class Cell.
     */
    @Test
    public void testGetGroupsWithSpecCap() {
        System.out.println("getGroupsWithSpecCap");
        float specCap = 0.0F;
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getGroupsWithSpecCap(specCap);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroupsWithChanMech method, of class Cell.
     */
    @Test
    public void testGetGroupsWithChanMech() {
        System.out.println("getGroupsWithChanMech");
        ChannelMechanism chanMech = null;
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getGroupsWithChanMech(chanMech);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroupsWithApPropSpeed method, of class Cell.
     */
    @Test
    public void testGetGroupsWithApPropSpeed() {
        System.out.println("getGroupsWithApPropSpeed");
        ApPropSpeed appv = null;
        Cell instance = new Cell();
        Vector expResult = null;
        Vector result = instance.getGroupsWithApPropSpeed(appv);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getApPropSpeedForGroup method, of class Cell.
     */
    @Test
    public void testGetApPropSpeedForGroup() {
        System.out.println("getApPropSpeedForGroup");
        String group = "";
        Cell instance = new Cell();
        ApPropSpeed expResult = null;
        ApPropSpeed result = instance.getApPropSpeedForGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChanMechsForGroup method, of class Cell.
     */
    @Test
    public void testGetChanMechsForGroup() {
        System.out.println("getChanMechsForGroup");
        String group = "";
        Cell instance = new Cell();
        ArrayList<ChannelMechanism> expResult = null;
        ArrayList<ChannelMechanism> result = instance.getChanMechsForGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSynapsesForGroup method, of class Cell.
     */
    @Test
    public void testGetSynapsesForGroup() {
        System.out.println("getSynapsesForGroup");
        String group = "";
        Cell instance = new Cell();
        Vector<String> expResult = null;
        Vector<String> result = instance.getSynapsesForGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecCapForGroup method, of class Cell.
     */
    @Test
    public void testGetSpecCapForGroup() {
        System.out.println("getSpecCapForGroup");
        String group = "";
        Cell instance = new Cell();
        float expResult = 0.0F;
        float result = instance.getSpecCapForGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecAxResForGroup method, of class Cell.
     */
    @Test
    public void testGetSpecAxResForGroup() {
        System.out.println("getSpecAxResForGroup");
        String group = "";
        Cell instance = new Cell();
        float expResult = 0.0F;
        float result = instance.getSpecAxResForGroup(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecAxResForSection method, of class Cell.
     */
    @Test
    public void testGetSpecAxResForSection() {
        System.out.println("getSpecAxResForSection");
        Section section = null;
        Cell instance = new Cell();
        float expResult = 0.0F;
        float result = instance.getSpecAxResForSection(section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecCapForSection method, of class Cell.
     */
    @Test
    public void testGetSpecCapForSection() {
        System.out.println("getSpecCapForSection");
        Section section = null;
        Cell instance = new Cell();
        float expResult = 0.0F;
        float result = instance.getSpecCapForSection(section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChanMechsForSegment method, of class Cell.
     */
    @Test
    public void testGetChanMechsForSegment() {
        System.out.println("getChanMechsForSegment");
        Segment segment = null;
        Cell instance = new Cell();
        ArrayList<ChannelMechanism> expResult = null;
        ArrayList<ChannelMechanism> result = instance.getChanMechsForSegment(segment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChanMechsForSection method, of class Cell.
     */
    @Test
    public void testGetChanMechsForSection() {
        System.out.println("getChanMechsForSection");
        Section section = null;
        Cell instance = new Cell();
        ArrayList<ChannelMechanism> expResult = null;
        ArrayList<ChannelMechanism> result = instance.getChanMechsForSection(section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getApPropSpeedForSegment method, of class Cell.
     */
    @Test
    public void testGetApPropSpeedForSegment() {
        System.out.println("getApPropSpeedForSegment");
        Segment segment = null;
        Cell instance = new Cell();
        ApPropSpeed expResult = null;
        ApPropSpeed result = instance.getApPropSpeedForSegment(segment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getApPropSpeedForSection method, of class Cell.
     */
    @Test
    public void testGetApPropSpeedForSection() {
        System.out.println("getApPropSpeedForSection");
        Section section = null;
        Cell instance = new Cell();
        ApPropSpeed expResult = null;
        ApPropSpeed result = instance.getApPropSpeedForSection(section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateGroupWithSynapse method, of class Cell.
     */
    @Test
    public void testAssociateGroupWithSynapse() {
        System.out.println("associateGroupWithSynapse");
        String group = "";
        String synapseType = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateGroupWithSynapse(group, synapseType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateParamGroupWithVarMech method, of class Cell.
     */
    @Test
    public void testAssociateParamGroupWithVarMech() {
        System.out.println("associateParamGroupWithVarMech");
        ParameterisedGroup paraGrp = null;
        VariableMechanism varMech = null;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateParamGroupWithVarMech(paraGrp, varMech);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateGroupWithChanMech method, of class Cell.
     */
    @Test
    public void testAssociateGroupWithChanMech() {
        System.out.println("associateGroupWithChanMech");
        String group = "";
        ChannelMechanism chanMech = null;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateGroupWithChanMech(group, chanMech);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateGroupWithSpecCap method, of class Cell.
     */
    @Test
    public void testAssociateGroupWithSpecCap() {
        System.out.println("associateGroupWithSpecCap");
        String group = "";
        float specCap = 0.0F;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateGroupWithSpecCap(group, specCap);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateGroupWithSpecAxRes method, of class Cell.
     */
    @Test
    public void testAssociateGroupWithSpecAxRes() {
        System.out.println("associateGroupWithSpecAxRes");
        String group = "";
        float specAxRes = 0.0F;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateGroupWithSpecAxRes(group, specAxRes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of associateGroupWithApPropSpeed method, of class Cell.
     */
    @Test
    public void testAssociateGroupWithApPropSpeed() {
        System.out.println("associateGroupWithApPropSpeed");
        String group = "";
        ApPropSpeed apPropSpeed = null;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.associateGroupWithApPropSpeed(group, apPropSpeed);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromApPropSpeeds method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromApPropSpeeds() {
        System.out.println("disassociateGroupFromApPropSpeeds");
        String group = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromApPropSpeeds(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromSynapse method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromSynapse() {
        System.out.println("disassociateGroupFromSynapse");
        String group = "";
        String synapseType = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromSynapse(group, synapseType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromSpecCap method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromSpecCap() {
        System.out.println("disassociateGroupFromSpecCap");
        String group = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromSpecCap(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromSpecAxRes method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromSpecAxRes() {
        System.out.println("disassociateGroupFromSpecAxRes");
        String group = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromSpecAxRes(group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromChanMech method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromChanMech_String_ChannelMechanism() {
        System.out.println("disassociateGroupFromChanMech");
        String group = "";
        ChannelMechanism chanMech = null;
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromChanMech(group, chanMech);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disassociateGroupFromChanMech method, of class Cell.
     */
    @Test
    public void testDisassociateGroupFromChanMech_String_String() {
        System.out.println("disassociateGroupFromChanMech");
        String group = "";
        String chanMechName = "";
        Cell instance = new Cell();
        boolean expResult = false;
        boolean result = instance.disassociateGroupFromChanMech(group, chanMechName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class Cell.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        Cell instance = new Cell();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class Cell.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Cell.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCellDescription method, of class Cell.
     */
    @Test
    public void testSetCellDescription() {
        System.out.println("setCellDescription");
        String cellDescription = "";
        Cell instance = new Cell();
        instance.setCellDescription(cellDescription);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAllSegments method, of class Cell.
     */
    @Test
    public void testSetAllSegments() {
        System.out.println("setAllSegments");
        Vector<Segment> allSegments = null;
        Cell instance = new Cell();
        instance.setAllSegments(allSegments);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInstanceName method, of class Cell.
     */
    @Test
    public void testSetInstanceName() {
        System.out.println("setInstanceName");
        String instanceName = "";
        Cell instance = new Cell();
        instance.setInstanceName(instanceName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSynapsesVsGroups method, of class Cell.
     */
    @Test
    public void testGetSynapsesVsGroups() {
        System.out.println("getSynapsesVsGroups");
        Cell instance = new Cell();
        Hashtable<String, Vector<String>> expResult = null;
        Hashtable<String, Vector<String>> result = instance.getSynapsesVsGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSynapsesVsGroups method, of class Cell.
     */
    @Test
    public void testSetSynapsesVsGroups() {
        System.out.println("setSynapsesVsGroups");
        Hashtable<String, Vector<String>> synapsesVsGroups = null;
        Cell instance = new Cell();
        instance.setSynapsesVsGroups(synapsesVsGroups);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChanMechsVsGroups method, of class Cell.
     */
    @Test
    public void testGetChanMechsVsGroups() {
        System.out.println("getChanMechsVsGroups");
        Cell instance = new Cell();
        Hashtable<ChannelMechanism, Vector<String>> expResult = null;
        Hashtable<ChannelMechanism, Vector<String>> result = instance.getChanMechsVsGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setVarMechsVsParaGroups method, of class Cell.
     */
    @Test
    public void testSetVarMechsVsParaGroups() {
        System.out.println("setVarMechsVsParaGroups");
        Hashtable<VariableMechanism, ParameterisedGroup> varMechsVsParaGroups = null;
        Cell instance = new Cell();
        instance.setVarMechsVsParaGroups(varMechsVsParaGroups);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVarMechsVsParaGroups method, of class Cell.
     */
    @Test
    public void testGetVarMechsVsParaGroups() {
        System.out.println("getVarMechsVsParaGroups");
        Cell instance = new Cell();
        Hashtable<VariableMechanism, ParameterisedGroup> expResult = null;
        Hashtable<VariableMechanism, ParameterisedGroup> result = instance.getVarMechsVsParaGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecCapVsGroups method, of class Cell.
     */
    @Test
    public void testGetSpecCapVsGroups() {
        System.out.println("getSpecCapVsGroups");
        Cell instance = new Cell();
        Hashtable<Float, Vector<String>> expResult = null;
        Hashtable<Float, Vector<String>> result = instance.getSpecCapVsGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecAxResVsGroups method, of class Cell.
     */
    @Test
    public void testGetSpecAxResVsGroups() {
        System.out.println("getSpecAxResVsGroups");
        Cell instance = new Cell();
        Hashtable<Float, Vector<String>> expResult = null;
        Hashtable<Float, Vector<String>> result = instance.getSpecAxResVsGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getApPropSpeedsVsGroups method, of class Cell.
     */
    @Test
    public void testGetApPropSpeedsVsGroups() {
        System.out.println("getApPropSpeedsVsGroups");
        Cell instance = new Cell();
        Hashtable<ApPropSpeed, Vector<String>> expResult = null;
        Hashtable<ApPropSpeed, Vector<String>> result = instance.getApPropSpeedsVsGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setChanMechsVsGroups method, of class Cell.
     */
    @Test
    public void testSetChanMechsVsGroups() {
        System.out.println("setChanMechsVsGroups");
        Hashtable<ChannelMechanism, Vector<String>> chanMechsVsGroups = null;
        Cell instance = new Cell();
        instance.setChanMechsVsGroups(chanMechsVsGroups);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSpecCapVsGroups method, of class Cell.
     */
    @Test
    public void testSetSpecCapVsGroups() {
        System.out.println("setSpecCapVsGroups");
        Hashtable<Float, Vector<String>> specCapVsGroups = null;
        Cell instance = new Cell();
        instance.setSpecCapVsGroups(specCapVsGroups);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSpecAxResVsGroups method, of class Cell.
     */
    @Test
    public void testSetSpecAxResVsGroups() {
        System.out.println("setSpecAxResVsGroups");
        Hashtable<Float, Vector<String>> sp = null;
        Cell instance = new Cell();
        instance.setSpecAxResVsGroups(sp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setApPropSpeedsVsGroups method, of class Cell.
     */
    @Test
    public void testSetApPropSpeedsVsGroups() {
        System.out.println("setApPropSpeedsVsGroups");
        Hashtable<ApPropSpeed, Vector<String>> ap = null;
        Cell instance = new Cell();
        instance.setApPropSpeedsVsGroups(ap);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOldGlobalSpecAxRes method, of class Cell.
     */
    @Test
    public void testGetOldGlobalSpecAxRes() {
        System.out.println("getOldGlobalSpecAxRes");
        Cell instance = new Cell();
        NumberGenerator expResult = null;
        NumberGenerator result = instance.getOldGlobalSpecAxRes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInitialPotential method, of class Cell.
     */
    @Test
    public void testGetInitialPotential() {
        System.out.println("getInitialPotential");
        Cell instance = new Cell();
        NumberGenerator expResult = null;
        NumberGenerator result = instance.getInitialPotential();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInitialPotential method, of class Cell.
     */
    @Test
    public void testSetInitialPotential() {
        System.out.println("setInitialPotential");
        NumberGenerator initPot = null;
        Cell instance = new Cell();
        instance.setInitialPotential(initPot);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOldGlobalSpecCapacitance method, of class Cell.
     */
    @Test
    public void testGetOldGlobalSpecCapacitance() {
        System.out.println("getOldGlobalSpecCapacitance");
        Cell instance = new Cell();
        NumberGenerator expResult = null;
        NumberGenerator result = instance.getOldGlobalSpecCapacitance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}