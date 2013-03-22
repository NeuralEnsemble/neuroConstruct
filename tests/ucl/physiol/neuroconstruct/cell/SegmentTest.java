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

import javax.vecmath.Point3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class SegmentTest {

    public SegmentTest() {
    }



    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SegmentTest");
    }


    @After
    public void tearDown() {
    }

    @Test public void testCloneAndEquals() 
    {
        System.out.println("---  testCloneAndEquals...");
        
        Segment seg1 = new Segment("Segname", 1.1f, new Point3f(1,2,3), 1, null, 0.6f, null);
        
        Segment seg2 = (Segment)seg1.clone();
        
        
        assertTrue(seg1.equals(seg2));
        
        
    }
    
    
    @Test public void testGetPointAlong() 
    {
        System.out.println("---  testGetPointAlong...");
        Section sec = new Section("TestSec");
        
        sec.setStartPointPositionX(10);
        sec.setStartPointPositionY(0);
        sec.setStartPointPositionZ(10);
        
        Segment seg1 = new Segment("Segname", 1.1f, new Point3f(10,20,10), 1, null, 0.6f, sec);
        
        System.out.println("Segment: "+ seg1);
        
        assertEquals(new Point3f(10,0,10), seg1.getPointAlong(0));
        assertEquals(new Point3f(10,6,10), seg1.getPointAlong(0.3f));
        assertEquals(new Point3f(10,20,10), seg1.getPointAlong(1));
    }

    @Test public void testRound() 
    {
        System.out.println("---  testRound...");
   
        
        assertEquals(0, Segment.round(0.0001f), 0);
        assertEquals(0, Segment.round(-0.0001f), 0);
        assertEquals(0.0009f, Segment.round(0.0009f), 0);
        assertEquals(-0.0009f, Segment.round(-0.0009f), 0);
        assertEquals(10, Segment.round(10.00001f), 0);
        assertEquals(-10, Segment.round(-10.00001f), 0);
        
        assertEquals(1, Segment.round(0.9999999f), 0);
        assertEquals(10, Segment.round(9.9999999f), 0);
        
        assertEquals(0.000999f, Segment.round(0.000999f), 0);
        assertEquals(-0.000999f, Segment.round(-0.000999f), 0);
        
        assertEquals(-1, Segment.round(-0.9999999f), 0);
        assertEquals(-10, Segment.round(-9.9999999f), 0);
        
        
        
    }

}
    
    
    
    
    
    
    
    
    
    