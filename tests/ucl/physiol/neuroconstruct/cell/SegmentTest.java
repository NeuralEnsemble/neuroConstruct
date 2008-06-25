/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.Vector;
import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class SegmentTest {

    public SegmentTest() {
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
     * Test of clone method, of class Segment.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        Segment instance = new Segment();
        Object expResult = null;
        Object result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndPointPosition method, of class Segment.
     */
    @Test
    public void testGetEndPointPosition() {
        System.out.println("getEndPointPosition");
        Segment instance = new Segment();
        Point3f expResult = null;
        Point3f result = instance.getEndPointPosition();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartPointPosition method, of class Segment.
     */
    @Test
    public void testGetStartPointPosition() {
        System.out.println("getStartPointPosition");
        Segment instance = new Segment();
        Point3f expResult = null;
        Point3f result = instance.getStartPointPosition();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentStartRadius method, of class Segment.
     */
    @Test
    public void testGetSegmentStartRadius() {
        System.out.println("getSegmentStartRadius");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getSegmentStartRadius();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentSurfaceArea method, of class Segment.
     */
    @Test
    public void testGetSegmentSurfaceArea() {
        System.out.println("getSegmentSurfaceArea");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getSegmentSurfaceArea();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentLength method, of class Segment.
     */
    @Test
    public void testGetSegmentLength() {
        System.out.println("getSegmentLength");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getSegmentLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentVolume method, of class Segment.
     */
    @Test
    public void testGetSegmentVolume() {
        System.out.println("getSegmentVolume");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getSegmentVolume();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getParentSegment method, of class Segment.
     */
    @Test
    public void testGetParentSegment() {
        System.out.println("getParentSegment");
        Segment instance = new Segment();
        Segment expResult = null;
        Segment result = instance.getParentSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRadius method, of class Segment.
     */
    @Test
    public void testGetRadius() {
        System.out.println("getRadius");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getRadius();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRadius method, of class Segment.
     */
    @Test
    public void testSetRadius() {
        System.out.println("setRadius");
        float radius = 0.0F;
        Segment instance = new Segment();
        instance.setRadius(radius);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAxonalSegment method, of class Segment.
     */
    @Test
    public void testIsAxonalSegment() {
        System.out.println("isAxonalSegment");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isAxonalSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDendriticSegment method, of class Segment.
     */
    @Test
    public void testIsDendriticSegment() {
        System.out.println("isDendriticSegment");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isDendriticSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSomaSegment method, of class Segment.
     */
    @Test
    public void testIsSomaSegment() {
        System.out.println("isSomaSegment");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isSomaSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isRootSegment method, of class Segment.
     */
    @Test
    public void testIsRootSegment() {
        System.out.println("isRootSegment");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isRootSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toShortString method, of class Segment.
     */
    @Test
    public void testToShortString() {
        System.out.println("toShortString");
        Segment instance = new Segment();
        String expResult = "";
        String result = instance.toShortString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Segment.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Segment instance = new Segment();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHTMLString method, of class Segment.
     */
    @Test
    public void testToHTMLString() {
        System.out.println("toHTMLString");
        boolean includeTabs = false;
        Segment instance = new Segment();
        String expResult = "";
        String result = instance.toHTMLString(includeTabs);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGroups method, of class Segment.
     */
    @Test
    public void testGetGroups() {
        System.out.println("getGroups");
        Segment instance = new Segment();
        Vector<String> expResult = null;
        Vector<String> result = instance.getGroups();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class Segment.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Segment.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentName method, of class Segment.
     */
    @Test
    public void testGetSegmentName() {
        System.out.println("getSegmentName");
        Segment instance = new Segment();
        String expResult = "";
        String result = instance.getSegmentName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getComment method, of class Segment.
     */
    @Test
    public void testGetComment() {
        System.out.println("getComment");
        Segment instance = new Segment();
        String expResult = "";
        String result = instance.getComment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isFiniteVolume method, of class Segment.
     */
    @Test
    public void testIsFiniteVolume() {
        System.out.println("isFiniteVolume");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isFiniteVolume();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFiniteVolume method, of class Segment.
     */
    @Test
    public void testSetFiniteVolume() {
        System.out.println("setFiniteVolume");
        boolean finiteVolume = false;
        Segment instance = new Segment();
        instance.setFiniteVolume(finiteVolume);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSegmentName method, of class Segment.
     */
    @Test
    public void testSetSegmentName() {
        System.out.println("setSegmentName");
        String segmentName = "";
        Segment instance = new Segment();
        instance.setSegmentName(segmentName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setComment method, of class Segment.
     */
    @Test
    public void testSetComment() {
        System.out.println("setComment");
        String comment = "";
        Segment instance = new Segment();
        instance.setComment(comment);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndPointPositionX method, of class Segment.
     */
    @Test
    public void testSetEndPointPositionX() {
        System.out.println("setEndPointPositionX");
        float val = 0.0F;
        Segment instance = new Segment();
        instance.setEndPointPositionX(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndPointPositionY method, of class Segment.
     */
    @Test
    public void testSetEndPointPositionY() {
        System.out.println("setEndPointPositionY");
        float val = 0.0F;
        Segment instance = new Segment();
        instance.setEndPointPositionY(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndPointPositionZ method, of class Segment.
     */
    @Test
    public void testSetEndPointPositionZ() {
        System.out.println("setEndPointPositionZ");
        float val = 0.0F;
        Segment instance = new Segment();
        instance.setEndPointPositionZ(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndPointPositionX method, of class Segment.
     */
    @Test
    public void testGetEndPointPositionX() {
        System.out.println("getEndPointPositionX");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getEndPointPositionX();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndPointPositionY method, of class Segment.
     */
    @Test
    public void testGetEndPointPositionY() {
        System.out.println("getEndPointPositionY");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getEndPointPositionY();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndPointPositionZ method, of class Segment.
     */
    @Test
    public void testGetEndPointPositionZ() {
        System.out.println("getEndPointPositionZ");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getEndPointPositionZ();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setParentSegment method, of class Segment.
     */
    @Test
    public void testSetParentSegment() {
        System.out.println("setParentSegment");
        Segment parentSegment = null;
        Segment instance = new Segment();
        instance.setParentSegment(parentSegment);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentId method, of class Segment.
     */
    @Test
    public void testGetSegmentId() {
        System.out.println("getSegmentId");
        Segment instance = new Segment();
        int expResult = 0;
        int result = instance.getSegmentId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSegmentId method, of class Segment.
     */
    @Test
    public void testSetSegmentId() {
        System.out.println("setSegmentId");
        int segmentId = 0;
        Segment instance = new Segment();
        instance.setSegmentId(segmentId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isFirstSectionSegment method, of class Segment.
     */
    @Test
    public void testIsFirstSectionSegment() {
        System.out.println("isFirstSectionSegment");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isFirstSectionSegment();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fullEquals method, of class Segment.
     */
    @Test
    public void testFullEquals() {
        System.out.println("fullEquals");
        Object obj = null;
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.fullEquals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Segment.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class Segment.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Segment instance = new Segment();
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSegmentShape method, of class Segment.
     */
    @Test
    public void testGetSegmentShape() {
        System.out.println("getSegmentShape");
        Segment instance = new Segment();
        int expResult = 0;
        int result = instance.getSegmentShape();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSpherical method, of class Segment.
     */
    @Test
    public void testIsSpherical() {
        System.out.println("isSpherical");
        Segment instance = new Segment();
        boolean expResult = false;
        boolean result = instance.isSpherical();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSection method, of class Segment.
     */
    @Test
    public void testGetSection() {
        System.out.println("getSection");
        Segment instance = new Segment();
        Section expResult = null;
        Section result = instance.getSection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSection method, of class Segment.
     */
    @Test
    public void testSetSection() {
        System.out.println("setSection");
        Section section = null;
        Segment instance = new Segment();
        instance.setSection(section);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFractionAlongParent method, of class Segment.
     */
    @Test
    public void testGetFractionAlongParent() {
        System.out.println("getFractionAlongParent");
        Segment instance = new Segment();
        float expResult = 0.0F;
        float result = instance.getFractionAlongParent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFractionAlongParent method, of class Segment.
     */
    @Test
    public void testSetFractionAlongParent() {
        System.out.println("setFractionAlongParent");
        float fractionAlongParent = 0.0F;
        Segment instance = new Segment();
        instance.setFractionAlongParent(fractionAlongParent);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}