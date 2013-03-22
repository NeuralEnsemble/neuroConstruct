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

import java.beans.*;
import java.io.*;
import javax.vecmath.Point3f;
import org.junit.*;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.GenesisCompartmentalisation;
import ucl.physiol.neuroconstruct.cell.examples.OneSegment;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;

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
    ParameterisedGroup pg5 = null;
    
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
        d2 = cell.addDendriticSegment(2, "d2", new Point3f(20,0,0), d1, 1, "Sec2", false);
        d3 = cell.addDendriticSegment(7, "d3", new Point3f(20,10,0), d2, 1, "Sec3", false);

        d3.getSection().setStartRadius(3);

        d1.getSection().setNumberInternalDivisions(4);
        d2.getSection().setNumberInternalDivisions(3);
        d3.getSection().setNumberInternalDivisions(5);

        d3.getSection().addToGroup(tipSection);
        
        
        
        pg1 = new ParameterisedGroup("ZeroToOne", 
                                   Section.DENDRITIC_GROUP, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.MOST_DIST_AT_1,"p1");
        
        pg2 = new ParameterisedGroup("StartToEnd", 
                                   Section.DENDRITIC_GROUP, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.NO_TRANSLATION, 
                                   DistalPref.NO_NORMALISATION,"p2");
        
        pg3 = new ParameterisedGroup("TipZeroToOne", 
                                   tipSection, 
                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                   ProximalPref.MOST_PROX_AT_0, 
                                   DistalPref.MOST_DIST_AT_1,"p3");

        pg4 = new ParameterisedGroup("TipToEnd",
                                   tipSection,
                                   Metric.PATH_LENGTH_FROM_ROOT,
                                   ProximalPref.MOST_PROX_AT_0,
                                   DistalPref.NO_NORMALISATION,"p4");


        pg5 = new ParameterisedGroup("TipToEnd_PathFromRoot",
                                   tipSection,
                                   Metric.PATH_LENGTH_FROM_ROOT,
                                   ProximalPref.NO_TRANSLATION,
                                   DistalPref.NO_NORMALISATION,"p5");
        
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
        
        //SegmentLocation loc1 = new PostSynapticTerminalLocation(d2.getSegmentId(), 0.5f);
        
        assertEquals(0.5, pg1.evaluateAt(cell, d2, 0.5f), 0);
        assertEquals(15, pg2.evaluateAt(cell, d2, 0.5f), 0);
        
        ////////assertEquals(15, pg5.evaluateAt(cell, loc1), 0);
        
        //SegmentLocation loc2 = new PostSynapticTerminalLocation(d3.getSegmentId(), 0.5f);
        
        assertEquals(5d/6d, pg1.evaluateAt(cell, d3, 0.5f), 1e-7);
        assertEquals(25, pg2.evaluateAt(cell, d3, 0.5f), 0);
        
        assertEquals(0.5, pg3.evaluateAt(cell, d3, 0.5f), 0);

        assertEquals(0, pg4.evaluateAt(cell, d3, 0), 0);
        assertEquals(5, pg4.evaluateAt(cell, d3, 0.5f), 0);

        SegmentLocation sl1 = new SegmentLocation(d3.getSegmentId(), 0.5f);

        assertEquals(25, pg5.evaluateAt(cell, cell.getSegmentWithId(sl1.getSegmentId()), sl1.getFractAlong()), 0);

        assertEquals(30, pg5.evaluateAt(cell, d3, 1), 0);

        GenesisCompartmentalisation g = new GenesisCompartmentalisation();

        Cell gCell = g.getCompartmentalisation(cell);

        System.out.println(CellTopologyHelper.printDetails(cell, null));

        System.out.println("Changed from morph: "+cell.getMorphSummary()+", to morph: "+gCell.getMorphSummary());

        System.out.println("getSegmentMapper: " + g.getSegmentMapper());
        

        System.out.println(CellTopologyHelper.printDetails(gCell, null));

        SegmentLocation sl1_g = g.getSegmentMapper().mapSegmentLocation(sl1);

        System.out.println("Mapped: "+sl1+" to "+ sl1_g);


        assertEquals(25, pg5.evaluateAt(gCell, gCell.getSegmentWithId(sl1_g.getSegmentId()), sl1_g.getFractAlong()), 0);

        
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