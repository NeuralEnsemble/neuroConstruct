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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class SectionTest {

    public SectionTest() {
    }

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() SectionTest");
    }

    
    
    @Test public void testCloneAndEquals() 
    {
        System.out.println("---  testCloneAndEquals...");
        Section s = new Section("TestSection");
        s.setComment("bfghdfg");
        s.setNumberInternalDivisions(2);
        s.setStartPointPositionX(2);
        s.setStartPointPositionY(2);
        s.setStartPointPositionZ(2);
        s.setStartRadius(3);
        
        s.addToGroup(Section.AXONAL_GROUP);
        s.addToGroup("fhjghj");
        
        Section s2 = (Section)s.clone();
        Section s3 = (Section)s.clone();
        
        assertTrue(s.equals(s2));
        assertTrue(s.toString().equals(s2.toString()));
        
        
        assertTrue(s.hashCode() == s2.hashCode());
        
        
        s2.setNumberInternalDivisions(s2.getNumberInternalDivisions()*2);
        
        assertFalse(s.hashCode() == s2.hashCode());
        
        s3.addToGroup("BadGroup");
        
        assertFalse(s.equals(s3));
        
        
        
    }

  







}