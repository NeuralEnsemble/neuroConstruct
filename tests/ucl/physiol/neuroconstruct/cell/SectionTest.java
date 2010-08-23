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
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
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
        

        Section s4 = new Section("TestSection4");
        s4.setStartPointPositionX(2);
        s4.setStartPointPositionY(2);
        s4.setStartPointPositionZ(2);
        s4.setStartRadius(3);

        Section s5 = (Section)s4.clone();
        Section s6 = (Section)s4.clone();
        
        s4.addToGroup("G1");
        s4.addToGroup("G2");

        s5.addToGroup("G2");
        s5.addToGroup("G1");
        
        s6.addToGroup("G1");
        s6.addToGroup("G2");


        System.out.println("s4: "+s4);
        System.out.println("s5: "+s5);

        assertFalse(s4.equals(s5));

        assertFalse(s4.equalsG(s5, false));  // boolean ignoreGroupOrder

        assertTrue(s5.equalsG(s4, true));    // boolean ignoreGroupOrder

        s5.addToGroup("G3");


        assertFalse(s4.equalsG(s5, false));

        assertFalse(s5.equalsG(s4, true));


        assertTrue(s6.equalsG(s4, true));    // boolean ignoreGroupOrder
        assertTrue(s6.equals(s4));    // boolean ignoreGroupOrder
        
    }


    public static void main(String[] args)
    {
        SectionTest ct = new SectionTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }


  







}