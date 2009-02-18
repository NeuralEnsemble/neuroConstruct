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

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import static org.junit.Assert.*;
import test.MainTest;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants;
import ucl.physiol.neuroconstruct.project.*;

/**
 *
 * @author padraig
 */
public class MorphMLReaderTest {

    String projName = "TestMorphs";
    File projDir = new File("testProjects/"+ projName);
        
    ProjectManager pm = null;
    
    public MorphMLReaderTest() {
    }

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() MorphMLReaderTest");        
        
        File projFile = ProjectStructure.findProjectFile(projDir);
        
        pm = new ProjectManager();
        
        try 
        {
            pm.loadProject(projFile);
        
            System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());
            
            
        } 
        catch (ProjectFileParsingException ex) 
        {
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
    }

    @Test public void testWriteAndRead() throws MorphologyException 
    {
        System.out.println("---  testWriteAndRead...");
        
        Cell cell1 = pm.getCurrentProject().cellManager.getAllCells().get(0);
        
        cell1.setCellDescription("This is\na test\n...");
        
        MorphMLConverter mmlC = new MorphMLConverter();
        
        File savedNeuroMLDir = ProjectStructure.getNeuroMLDir(projDir);
        File morphFile = new File(savedNeuroMLDir, "test.mml");
        
        MorphMLConverter.saveCellInNeuroMLFormat(cell1, pm.getCurrentProject(), morphFile, NeuroMLConstants.NEUROML_LEVEL_3);
        
        assertTrue(morphFile.exists());
        
        System.out.println("Saved cell in NeuroML Level 3 file: "+ morphFile.getAbsolutePath());
        
        Cell cell2 = mmlC.loadFromMorphologyFile(morphFile, cell1.getInstanceName());
        String compare = CellTopologyHelper.compare(cell1, cell2, false);
        
        System.out.println("Comparison 1: "+ compare);
        
        assertTrue(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);
        
        System.out.println("Reloaded file and cells are identical");
        
    }
    
    
    public static void main(String[] args)
    {
        MorphMLReaderTest ct = new MorphMLReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}