/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.beans.*;
import java.io.*;
import javax.vecmath.Point3f;
import org.junit.*;
import org.junit.runner.Result;
import test.MainTest;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.examples.OneSegment;

/**
 *
 * @author padraig
 */
public class ParameterisedGroupTest 
{

    ParameterisedGroup pg1 = null;
    ParameterisedGroup pg2 = null;
    ParameterisedGroup pg3 = null;
    ParameterisedGroup pg4 = null;
    //ParameterisedGroup pg5 = null;
    
    Cell cell = null;
    Segment d1 = null;
    Segment d2 = null;
    Segment d3 = null;
    
    String tipSection = "tipSection";

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() ParameterisedGroupTest");
        
        cell = new OneSegment("Simple");
        
        
        d1 = cell.addDendriticSegment(1, "d1", new Point3f(10,0,0), cell.getFirstSomaSegment(), 1, "Sec1", false);
        d2 = cell.addDendriticSegment(1, "d2", new Point3f(20,0,0), d1, 1, "Sec2", false);
        d3 = cell.addDendriticSegment(1, "d3", new Point3f(20,10,0), d2, 1, "Sec3", false);
        
        d3.getSection().addToGroup(tipSection);
        
        
        
        pg1 = new ParameterisedGroup("ZeroToOne", 
                                   Section.DENDRITIC_GROUP, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.MOST_DIST_AT_1);
        
        pg2 = new ParameterisedGroup("StartToEnd", 
                                   Section.DENDRITIC_GROUP, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.NO_TRANSLATION, 
                                   DistalPref.NO_NORMALISATION);
        
        pg3 = new ParameterisedGroup("TipZeroToOne", 
                                   tipSection, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.MOST_DIST_AT_1);
        
        pg4 = new ParameterisedGroup("TipToEnd", 
                                   tipSection, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.NO_NORMALISATION);
        
        /*
        pg5 = new ParameterisedGroup("3DDistZeroToOne", 
                                   Section.ALL, 
                                   Metric.THREE_D_RADIAL_POSITION, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.MOST_DIST_AT_1);*/
        
    }


    /**
     * Test of evaluateAt method, of class ParameterisedGroup.
     */
    @Test
    public void testEvaluateAt() throws ParameterException 
    {
        System.out.println("---  testEvaluateAt...");
        
        SegmentLocation loc1 = new PostSynapticTerminalLocation(d2.getSegmentId(), 0.5f);
        
        assertEquals(0.5, pg1.evaluateAt(cell, loc1), 0);
        assertEquals(15, pg2.evaluateAt(cell, loc1), 0);
        
        ////////assertEquals(15, pg5.evaluateAt(cell, loc1), 0);
        
        SegmentLocation loc2 = new PostSynapticTerminalLocation(d3.getSegmentId(), 0.5f);
        
        assertEquals(5d/6d, pg1.evaluateAt(cell, loc2), 1e-7);
        assertEquals(25, pg2.evaluateAt(cell, loc2), 0);
        
        assertEquals(0.5, pg3.evaluateAt(cell, loc2), 0);
        assertEquals(5, pg4.evaluateAt(cell, loc2), 0);
        
    }
    
    
    

    /**
     * Test of getMinValue method, of class ParameterisedGroup.
     */
    @Test
    public void testGetMinValue() throws ParameterException
    {
        System.out.println("---  testGetMinValue...");
        
        
        assertEquals(0, pg1.getMinValue(cell), 0);
        assertEquals(0, pg2.getMinValue(cell), 0);
    }

    /**
     * Test of getMaxValue method, of class ParameterisedGroup.
     */
    @Test
    public void testGetMaxValue()  throws ParameterException
    {
        System.out.println("---  testGetMaxValue...");
        
        assertEquals(1, pg1.getMaxValue(cell), 0);
        assertEquals(30, pg2.getMaxValue(cell), 0);
    }
    
    @Test
    public void testCloneEquals() 
    {
        System.out.println("---  testCloneEquals...");
        
        ParameterisedGroup pg1a = (ParameterisedGroup)pg1.clone();
        
        System.out.println("Checking: "+ pg1a);
        System.out.println("equals: "+ pg1a);
        
        assertEquals(pg1a, pg1);
        
        pg1.setProximalPref(ProximalPref.NO_TRANSLATION);
        
        
        System.out.println("Checking: "+ pg1a);
        System.out.println("NOT equals: "+ pg1a);
        
        assertNotSame(pg1, pg1a);
        
    }
    
    @Test
    public void testSaveLoad()  throws FileNotFoundException, IOException
    {
        System.out.println("---  testSaveLoad...");
        XMLEncoder xmlEncoder = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
        File f = new File("../temp/Test.ser");

        System.out.println("Saving to: "+f.getCanonicalPath());
        
        fos = new FileOutputStream(f);
        bos = new BufferedOutputStream(fos);
        xmlEncoder = new XMLEncoder(bos);
        
        //Metric m = Metric.PATH_LENGTH_FROM_ROOT;
        Object o1 = pg1;
        
        System.out.println("Pre: "+ o1);
        
        //ProximalPref p = ProximalPref.MOST_PROX_AT_0;
        
        xmlEncoder.writeObject(o1);
        xmlEncoder.close();
        
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        XMLDecoder xd = new XMLDecoder(bis);
        
        Object o2 = xd.readObject();
        
        System.out.println("Post: "+ o2);
        
        
        assertEquals(o2, o1);
        
     
    }


    public static void main(String[] args)
    {
        ParameterisedGroupTest ct = new ParameterisedGroupTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}