/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.utils;

import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.ChannelMechanism;
import ucl.physiol.neuroconstruct.cell.PostSynapticTerminalLocation;
import ucl.physiol.neuroconstruct.cell.PreSynapticTerminalLocation;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.cell.SynapticConnectionEndPoint;
import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.RectangularBox;

/**
 *
 * @author padraig
 */
public class CellTopologyHelperTest {

    public CellTopologyHelperTest() {
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

//    /**
//     * Test of getPossiblePostSynapticTerminal method, of class CellTopologyHelper.
//     */
//    @Test
//    public void testGetPossiblePostSynapticTerminal() {
//        System.out.println("getPossiblePostSynapticTerminal");
//        Cell cell = null;
//        String[] synapseType = null;
//        PostSynapticTerminalLocation expResult = null;
//        PostSynapticTerminalLocation result = CellTopologyHelper.getPossiblePostSynapticTerminal(cell, synapseType);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPossiblePreSynapticTerminal method, of class CellTopologyHelper.
//     */
//    @Test
//    public void testGetPossiblePreSynapticTerminal() {
//        System.out.println("getPossiblePreSynapticTerminal");
//        Cell cell = null;
//        String[] synapseTypes = null;
//        PreSynapticTerminalLocation expResult = null;
//        PreSynapticTerminalLocation result = CellTopologyHelper.getPossiblePreSynapticTerminal(cell, synapseTypes);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of convertSegmentDisplacement method, of class CellTopologyHelper.
     */
    @Test
    public void testConvertSegmentDisplacement() {
        System.out.println("convertSegmentDisplacement");
        Cell cell = null;
        int segmentId = 0;
        float displacementAlong = 0.0F;
        Point3f expResult = null;
        Point3f result = CellTopologyHelper.convertSegmentDisplacement(cell, segmentId, displacementAlong);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOrderedSegmentsInSection method, of class CellTopologyHelper.
     */
    @Test
    public void testGetOrderedSegmentsInSection() {
        System.out.println("getOrderedSegmentsInSection");
        Cell cell = null;
        Section section = null;
        Vector<Segment> expResult = null;
        Vector<Segment> result = CellTopologyHelper.getOrderedSegmentsInSection(cell, section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isGroupASubset method, of class CellTopologyHelper.
     */
    @Test
    public void testIsGroupASubset() {
        System.out.println("isGroupASubset");
        String groupA = "";
        String groupB = "";
        Cell cell = null;
        boolean expResult = false;
        boolean result = CellTopologyHelper.isGroupASubset(groupA, groupB, cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasExtraCellMechParams method, of class CellTopologyHelper.
     */
    @Test
    public void testHasExtraCellMechParams() {
        System.out.println("hasExtraCellMechParams");
        Cell cell = null;
        boolean expResult = false;
        boolean result = CellTopologyHelper.hasExtraCellMechParams(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of convertSectionDisplacement method, of class CellTopologyHelper.
     */
    @Test
    public void testConvertSectionDisplacement() {
        System.out.println("convertSectionDisplacement");
        Cell cell = null;
        Section section = null;
        float displacementAlong = 0.0F;
        Point3f expResult = null;
        Point3f result = CellTopologyHelper.convertSectionDisplacement(cell, section, displacementAlong);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTotalAxialResistance method, of class CellTopologyHelper.
     */
    @Test
    public void testGetTotalAxialResistance() {
        System.out.println("getTotalAxialResistance");
        Segment segment = null;
        float specAxRes = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getTotalAxialResistance(segment, specAxRes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSurroundingBox method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSurroundingBox() {
        System.out.println("getSurroundingBox");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        RectangularBox expResult = null;
        RectangularBox result = CellTopologyHelper.getSurroundingBox(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpaceConstant method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSpaceConstant() {
        System.out.println("getSpaceConstant");
        Segment segment = null;
        float specMembRes = 0.0F;
        float specAxRes = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getSpaceConstant(segment, specMembRes, specAxRes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getElectrotonicLength method, of class CellTopologyHelper.
     */
    @Test
    public void testGetElectrotonicLength() {
        System.out.println("getElectrotonicLength");
        Segment segment = null;
        float specMembRes = 0.0F;
        float specAxRes = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getElectrotonicLength(segment, specMembRes, specAxRes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSectionLength method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSectionLength() {
        System.out.println("getSectionLength");
        Cell cell = null;
        Section section = null;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getSectionLength(cell, section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getAbsolutePosSegLoc method, of class CellTopologyHelper.
     */
    @Test
    public void testGetAbsolutePosSegLoc() {
        System.out.println("getAbsolutePosSegLoc");
        Project project = null;
        String cellGroup = "";
        int cellNum = 0;
        SegmentLocation segLocation = null;
        Point3f expResult = null;
        Point3f result = CellTopologyHelper.getAbsolutePosSegLoc(project, cellGroup, cellNum, segLocation);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSynapticEndpointsDistance method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSynapticEndpointsDistance() {
        System.out.println("getSynapticEndpointsDistance");
        Project project = null;
        String sourceCellGroup = "";
        SynapticConnectionEndPoint sourceEndPoint = null;
        String targetCellGroup = "";
        SynapticConnectionEndPoint targetEndPoint = null;
        String dimension = "";
        float expResult = 0.0F;
        float result = CellTopologyHelper.getSynapticEndpointsDistance(project, sourceCellGroup, sourceEndPoint, targetCellGroup, targetEndPoint, dimension);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of translateAllPositions method, of class CellTopologyHelper.
     */
    @Test
    public void testTranslateAllPositions() {
        System.out.println("translateAllPositions");
        Cell oldCell = null;
        Vector3f translation = null;
        Cell expResult = null;
        Cell result = CellTopologyHelper.translateAllPositions(oldCell, translation);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of moveSectionsToConnPointsOnParents method, of class CellTopologyHelper.
     */
    @Test
    public void testMoveSectionsToConnPointsOnParents() {
        System.out.println("moveSectionsToConnPointsOnParents");
        Cell cell = null;
        Cell expResult = null;
        Cell result = CellTopologyHelper.moveSectionsToConnPointsOnParents(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLengthFromRoot method, of class CellTopologyHelper.
     */
    @Test
    public void testGetLengthFromRoot() {
        System.out.println("getLengthFromRoot");
        Cell cell = null;
        SegmentLocation location = null;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getLengthFromRoot(cell, location);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinLengthFromRoot method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMinLengthFromRoot() {
        System.out.println("getMinLengthFromRoot");
        Cell cell = null;
        String group = "";
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMinLengthFromRoot(cell, group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxLengthFromRoot method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMaxLengthFromRoot() {
        System.out.println("getMaxLengthFromRoot");
        Cell cell = null;
        String group = "";
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllChildSegsToBranchEnd method, of class CellTopologyHelper.
     */
    @Test
    public void testGetAllChildSegsToBranchEnd() {
        System.out.println("getAllChildSegsToBranchEnd");
        Cell cell = null;
        Segment segment = null;
        Vector<Segment> expResult = null;
        Vector<Segment> result = CellTopologyHelper.getAllChildSegsToBranchEnd(cell, segment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllChildSegments method, of class CellTopologyHelper.
     */
    @Test
    public void testGetAllChildSegments() {
        System.out.println("getAllChildSegments");
        Cell cell = null;
        Segment segment = null;
        boolean onlySameSection = false;
        Vector<Segment> expResult = null;
        Vector<Segment> result = CellTopologyHelper.getAllChildSegments(cell, segment, onlySameSection);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFractionAlongSection method, of class CellTopologyHelper.
     */
    @Test
    public void testGetFractionAlongSection() {
        System.out.println("getFractionAlongSection");
        Cell cell = null;
        Segment segment = null;
        float fractionAlongSegment = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getFractionAlongSection(cell, segment, fractionAlongSegment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFractionAlongSegment method, of class CellTopologyHelper.
     */
    @Test
    public void testGetFractionAlongSegment() {
        System.out.println("getFractionAlongSegment");
        Cell cell = null;
        Section section = null;
        float fractionAlongSection = 0.0F;
        SegmentLocation expResult = null;
        SegmentLocation result = CellTopologyHelper.getFractionAlongSegment(cell, section, fractionAlongSection);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxXExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMaxXExtent() {
        System.out.println("getMaxXExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMaxXExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxYExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMaxYExtent() {
        System.out.println("getMaxYExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMaxYExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxZExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMaxZExtent() {
        System.out.println("getMaxZExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMaxZExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinXExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMinXExtent() {
        System.out.println("getMinXExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMinXExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinYExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMinYExtent() {
        System.out.println("getMinYExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMinYExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinZExtent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetMinZExtent() {
        System.out.println("getMinZExtent");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getMinZExtent(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getXExtentOfCell method, of class CellTopologyHelper.
     */
    @Test
    public void testGetXExtentOfCell() {
        System.out.println("getXExtentOfCell");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getXExtentOfCell(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getYExtentOfCell method, of class CellTopologyHelper.
     */
    @Test
    public void testGetYExtentOfCell() {
        System.out.println("getYExtentOfCell");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getYExtentOfCell(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZExtentOfCell method, of class CellTopologyHelper.
     */
    @Test
    public void testGetZExtentOfCell() {
        System.out.println("getZExtentOfCell");
        Cell cell = null;
        boolean somaOnly = false;
        boolean inclAxArbors = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getZExtentOfCell(cell, somaOnly, inclAxArbors);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateChannelMechanisms method, of class CellTopologyHelper.
     */
    @Test
    public void testUpdateChannelMechanisms() {
        System.out.println("updateChannelMechanisms");
        Cell cell = null;
        Project project = null;
        CellTopologyHelper.updateChannelMechanisms(cell, project);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of printDetails method, of class CellTopologyHelper.
     */
    @Test
    public void testPrintDetails_Cell_Project() {
        System.out.println("printDetails");
        Cell cell = null;
        Project project = null;
        String expResult = "";
        String result = CellTopologyHelper.printDetails(cell, project);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of printDetails method, of class CellTopologyHelper.
     */
    @Test
    public void testPrintDetails_3args() {
        System.out.println("printDetails");
        Cell cell = null;
        Project project = null;
        boolean html = false;
        String expResult = "";
        String result = CellTopologyHelper.printDetails(cell, project, html);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of printDetails method, of class CellTopologyHelper.
     */
    @Test
    public void testPrintDetails_5args() {
        System.out.println("printDetails");
        Cell cell = null;
        Project project = null;
        boolean html = false;
        boolean longFormat = false;
        boolean projHtml = false;
        String expResult = "";
        String result = CellTopologyHelper.printDetails(cell, project, html, longFormat, projHtml);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnLocOnExpModParent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetConnLocOnExpModParent() {
        System.out.println("getConnLocOnExpModParent");
        Cell cell = null;
        Segment segment = null;
        SegmentLocation expResult = null;
        SegmentLocation result = CellTopologyHelper.getConnLocOnExpModParent(cell, segment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVolume method, of class CellTopologyHelper.
     */
    @Test
    public void testGetVolume() {
        System.out.println("getVolume");
        Cell cell = null;
        boolean somaOnly = false;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getVolume(cell, somaOnly);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDistToFirstExpModParent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetDistToFirstExpModParent() {
        System.out.println("getDistToFirstExpModParent");
        Cell cell = null;
        Segment segment = null;
        float fractAlongSegment = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getDistToFirstExpModParent(cell, segment, fractAlongSegment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimeToFirstExpModParent method, of class CellTopologyHelper.
     */
    @Test
    public void testGetTimeToFirstExpModParent() {
        System.out.println("getTimeToFirstExpModParent");
        Cell cell = null;
        Segment segment = null;
        float fractAlongSegment = 0.0F;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getTimeToFirstExpModParent(cell, segment, fractAlongSegment);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of printShortDetails method, of class CellTopologyHelper.
     */
    @Test
    public void testPrintShortDetails() {
        System.out.println("printShortDetails");
        Cell cell = null;
        String expResult = "";
        String result = CellTopologyHelper.printShortDetails(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkSimplyConnected method, of class CellTopologyHelper.
     */
    @Test
    public void testCheckSimplyConnected() {
        System.out.println("checkSimplyConnected");
        Cell cell = null;
        boolean expResult = false;
        boolean result = CellTopologyHelper.checkSimplyConnected(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBiophysicalValidityStatus method, of class CellTopologyHelper.
     */
    @Test
    public void testGetBiophysicalValidityStatus() {
        System.out.println("getBiophysicalValidityStatus");
        Cell cell = null;
        Project project = null;
        ValidityStatus expResult = null;
        ValidityStatus result = CellTopologyHelper.getBiophysicalValidityStatus(cell, project);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compare method, of class CellTopologyHelper.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");
        Cell cellA = null;
        Cell cellB = null;
        boolean html = false;
        String expResult = "";
        String result = CellTopologyHelper.compare(cellA, cellB, html);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValidityStatus method, of class CellTopologyHelper.
     */
    @Test
    public void testGetValidityStatus() {
        System.out.println("getValidityStatus");
        Cell cell = null;
        ValidityStatus expResult = null;
        ValidityStatus result = CellTopologyHelper.getValidityStatus(cell);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPassiveChannels method, of class CellTopologyHelper.
     */
    @Test
    public void testGetPassiveChannels() {
        System.out.println("getPassiveChannels");
        Cell cell = null;
        Project project = null;
        ArrayList<ChannelMechanism> expResult = null;
        ArrayList<ChannelMechanism> result = CellTopologyHelper.getPassiveChannels(cell, project);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentBiophysics method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSegmentBiophysics() {
        System.out.println("getSegmentBiophysics");
        Segment segment = null;
        Cell cell = null;
        Project project = null;
        boolean html = false;
        String expResult = "";
        String result = CellTopologyHelper.getSegmentBiophysics(segment, cell, project, html);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpecMembResistance method, of class CellTopologyHelper.
     */
    @Test
    public void testGetSpecMembResistance() throws Exception {
        System.out.println("getSpecMembResistance");
        Cell cell = null;
        Project project = null;
        Section section = null;
        float expResult = 0.0F;
        float result = CellTopologyHelper.getSpecMembResistance(cell, project, section);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reorderSegsParentsFirst method, of class CellTopologyHelper.
     */
    @Test
    public void testReorderSegsParentsFirst() {
        System.out.println("reorderSegsParentsFirst");
        Cell cell = null;
        CellTopologyHelper.reorderSegsParentsFirst(cell);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zeroFirstSomaSegId method, of class CellTopologyHelper.
     */
    @Test
    public void testZeroFirstSomaSegId() {
        System.out.println("zeroFirstSomaSegId");
        Cell cell = null;
        CellTopologyHelper.zeroFirstSomaSegId(cell);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class CellTopologyHelper.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        CellTopologyHelper.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}