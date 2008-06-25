/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.Point3f;
import org.junit.After;
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
    public void setUp() {
        System.out.println("Setting up the SectionTest...");
    }

    @After
    public void tearDown() {
        System.out.println("Setting down the SectionTest...");
    }
    
    
    @Test public void testCloneAndEquals() 
    {
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
        
        assertTrue(s.equals(s2));
        assertTrue(s.toString().equals(s2.toString()));
        
        
        assertTrue(s.hashCode() == s2.hashCode());
        s2.setNumberInternalDivisions(s2.getNumberInternalDivisions()*2);
        
        assertFalse(s.hashCode() == s2.hashCode());
        System.out.println("Done testCloneAndEquals...");
        
    }

  







}