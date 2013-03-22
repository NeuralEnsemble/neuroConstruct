/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import javax.vecmath.Point3f;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.OneSegment;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class GenesisCompartmentalisationTest
{

    Cell cell = null;
    //Segment d1 = null;
    //Segment d2 = null;
    //Segment d3 = null;

    String tipSection = "tipSection";

    @Before
    public void setUp()
    {
        System.out.println("---------------   setUp() GenesisCompartmentalisationTest");

        cell = new OneSegment("Simple");

        Segment d1 = cell.addDendriticSegment(1, "d1", new Point3f(10,0,0), cell.getFirstSomaSegment(), 1, "Sec1", false);
        
        d1.getSection().setStartRadius(2);
        d1.getSection().setNumberInternalDivisions(5);
      
        Segment d2 = cell.addDendriticSegment(2, "d2", new Point3f(20,0,0), d1, 1, "Sec2", false);
        
        d2.getSection().setStartRadius(4);
        d2.getSection().setNumberInternalDivisions(4);
        
        Segment d3 = cell.addDendriticSegment(7, "d3", new Point3f(20,10,0), d2, 1, "Sec3", false);
        
        d3.getSection().setStartRadius(3);
        d3.getSection().setNumberInternalDivisions(5);
        d3.getSection().addToGroup(tipSection); /* */



    }



    @Test
    public void testGenComp() throws ParameterException
    {
        System.out.println("---  testGenComp...");

        GenesisCompartmentalisation g = new GenesisCompartmentalisation();

        //cell = new PurkinjeCell("Purky");

        Cell cellG = g.getCompartmentalisation(cell);

        System.out.println(CellTopologyHelper.printDetails(cell, null));

        System.out.println("Changed from morph: "+cell.getMorphSummary()+", to morph: "+cellG.getMorphSummary());

        System.out.println(CellTopologyHelper.printDetails(cellG, null));

        float totLen = 0;
        float totArea = 0;
        for(Segment s: cell.getAllSegments())
        {
            totLen += s.getSegmentLength();
            totArea += s.getSegmentSurfaceArea();
            //System.out.println("totArea: "+totArea);
        }
        
        float totLenG = 0;
        float totAreaG = 0;
        for(Segment sG: cellG.getAllSegments())
        {
            totLenG += sG.getSegmentLength();
            totAreaG += sG.getSegmentSurfaceArea();
            //System.out.println("totAreaG: "+totAreaG);
        }

        System.out.println("Len of old cell: "+totLen+", len new: "+ totLenG);

        assertEquals(totLen, totLenG,totLen/10000);
        assertEquals(totArea, totAreaG,(totArea/10000));

        System.out.println("getSegmentMapper: " + g.getSegmentMapper());

        Segment firstSeg = cell.getAllSegments().get(1);

        SegmentLocation sl1 = new SegmentLocation(firstSeg.getSegmentId(), 0.5f);

        SegmentLocation sl1G = g.getSegmentMapper().mapSegmentLocation(sl1);

        System.out.println("Mapped: "+sl1+" to "+ sl1G);

        float lenToEnd = CellTopologyHelper.getLengthFromRoot(cell, sl1);
        float lenToEndG = CellTopologyHelper.getLengthFromRoot(cellG, sl1G);

        assertEquals(lenToEnd, lenToEndG,0);

        if(cell.getAllSegments().size()>2)
        {
            Segment lastSeg = cell.getAllSegments().get(cell.getAllSegments().size()-1);

            sl1 = new SegmentLocation(lastSeg.getSegmentId(), 0.5f);

            sl1G = g.getSegmentMapper().mapSegmentLocation(sl1);

            System.out.println("Mapped: "+sl1+" to "+ sl1G);

            lenToEnd = CellTopologyHelper.getLengthFromRoot(cell, sl1);
            lenToEndG = CellTopologyHelper.getLengthFromRoot(cellG, sl1G);

            assertEquals(lenToEnd, lenToEndG,lenToEnd/10000);
        }


        

    }

    public static void main(String[] args)
    {
        GenesisCompartmentalisationTest ct = new GenesisCompartmentalisationTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }


}