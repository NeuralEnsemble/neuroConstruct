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

package ucl.physiol.neuroconstruct.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.textui.TestRunner;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import static org.junit.Assert.*;

/**
 *
 * Test core behaviour of neuroConstruct
 *
 * @author Padraig Gleeson
 * 
 */
public class ModelTest 
{
    @Test public void testEx1()
    {
        String projFileName = "nCexamples/Ex1_Simple/Ex1_Simple.ncx";
        checkProject(projFileName);
    }

    @Test public void testGranuleCell()
    {
        String projFileName = "nCmodels/GranuleCell/GranuleCell.ncx";
        checkProject(projFileName);
    }
    @Test public void testGranCellLayer()
    {
        String projFileName = "nCmodels/GranCellLayer/GranCellLayer.ncx";
        checkProject(projFileName);
    }


    private void checkProject(String projFileName)
    {
        File projFile = new File(projFileName);
        assertTrue("Problem finding file: "+projFile.getAbsolutePath(), projFile.exists());
    }


    public static void main(String[] args)
            
    {
        System.out.println("Running the main nC model tests...");


        Result r = null;

        
        r = org.junit.runner.JUnitCore.runClasses(ucl.physiol.neuroconstruct.test.ModelTest.class);
        
        MainTest.checkResults(r);

    }
    
}