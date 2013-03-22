/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import static org.junit.Assert.*;

/**
 *
 * @author Padraig
 */
public class SWCMorphReaderTest {

    File swcFile = new File("nCexamples/Ex3_Morphology/importedMorphologies/l22.swc");



    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() MorphMLReaderTest");
    }

    /**
     * Test of loadFromMorphologyFile method, of class SWCMorphReader.
     */
    @Test
    public void testLoadFromMorphologyFile() throws Exception
    {
        System.out.println("---  testLoadFromMorphologyFile()...");

        SWCMorphReader swcReader = new SWCMorphReader();

        Cell swcCell = swcReader.loadFromMorphologyFile(swcFile, "SWCCelll");

        int expectedSegs = 1646;     // As found previously through GUI
        float expectedLen = 8735;  // As found previously through GUI

        System.out.println("Loaded cell: "+ swcCell);

        //System.out.println(CellTopologyHelper.printDetails(swcCell, null));

        assertEquals(expectedSegs, swcCell.getAllSegments().size());

        ValidityStatus valStatus = CellTopologyHelper.getValidityStatus(swcCell);

        System.out.println("valStatus: "+ valStatus);

        assertNotSame(valStatus.getValidity(), ValidityStatus.VALIDATION_ERROR);

        float totLen = 0;
        for(Segment seg: swcCell.getAllSegments())
        {
            totLen+=seg.getSegmentLength();
        }

        assertEquals(totLen, expectedLen, 1);

    }

    public static void main(String[] args)
    {
        SWCMorphReaderTest ct = new SWCMorphReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }

}