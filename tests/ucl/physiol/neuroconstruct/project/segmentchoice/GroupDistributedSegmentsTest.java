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
import ucl.physiol.neuroconstruct.test.MainTest;
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