/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project.segmentchoice;


import javax.vecmath.Point3f;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.examples.SimpleCell;
import ucl.physiol.neuroconstruct.cell.Section;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import static org.junit.Assert.*;

/**
 *
 * @author Matteo
 */
public class GroupDistributedSegmentsTest {

    public GroupDistributedSegmentsTest() {
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

    @Test
    public void testMain() {
        Cell cell = new SimpleCell("TestCell");
        
        String group = Section.DENDRITIC_GROUP;        
        
        int n = 100;
        
        GroupDistributedSegments chooser = new GroupDistributedSegments(group, n);
        
        chooser.initialise(cell);
        
        /* check the number of locations generated */
        
        assertTrue(chooser.generatedSegmentIds.size() == n);
        
         /* check that all the locations are in the corrct group */
        
        for (int i = 0; i < n; i++) {
            assertTrue(cell.getSegmentsInGroup(group).contains
                    (cell.getSegmentWithId(chooser.generatedSegmentIds.get(i))));
        } 
        
        /* check that the probabiliy distribution of the locations is correct:
         a new cell (cell1) is generated with a small segment(length=10) and a big segment (length=90)
         and check if the probability distribution of 100 points is about 10% and 90% */
        
        Cell cell1 = new SimpleCell();
        Section section = new Section();
        cell1.addFirstSomaSegment(1, 1, "soma1", new Point3f(0, 0, 0), new Point3f(1, 0, 0), section);
        cell1.addDendriticSegment(1, "smallSeg", new Point3f(11, 0, 0),
                cell1.getFirstSomaSegment(), 1, "group1", false);
        cell1.addDendriticSegment(1, "bigSeg", new Point3f(101, 0, 0),
                cell1.getSegmentWithName("smallSeg", true), 1, "group1", true);
                
        String group1 = Section.DENDRITIC_GROUP; 
        
        GroupDistributedSegments chooser1 = new GroupDistributedSegments(group1, 100);
        
        chooser1.initialise(cell1);
        
        int countSmall = 0;
        int countBig = 0;
        
        for (int i = 0; i < 100; i++) {
            if (cell1.getSegmentWithId(chooser1.generatedSegmentIds.get(i)).getSegmentName().equals("smallSeg")){
                countSmall++;
            }
            else {
                countBig++;
            }
        }
        
        System.out.println(countSmall + "% locations on the small segment & "
                + countBig + "% locations on the big segment");
        
        try {
            assertTrue (((countSmall < 20) & (countSmall > 0)) & ((countBig < 100) & (countBig > 80)));
        } catch (Exception e) {
            System.out.println("WARNING: the distribution probability is unlikely");
        }
        
    }
    
    
    
    public static void main(String[] args)
    {
        GroupDistributedSegmentsTest ct = new GroupDistributedSegmentsTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }

}