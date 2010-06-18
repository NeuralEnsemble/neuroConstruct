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
import java.util.ArrayList;
import java.util.Date;
import junit.textui.TestRunner;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.ProjectFileParsingException;
import ucl.physiol.neuroconstruct.project.ProjectManager;
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
    @Test public void testEx2()
    {
        String projFileName = "nCexamples/Ex2_Packing/Ex2_Packing.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx3()
    {
        String projFileName = "nCexamples/Ex3_Morphology/Ex3_Morphology.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx4()
    {
        String projFileName = "nCexamples/Ex4_HHcell/Ex4_HHcell.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx5()
    {
        String projFileName = "nCexamples/Ex5_Networks/Ex5_Networks.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx6()
    {
        String projFileName = "nCexamples/Ex6_CerebellumDemo/Ex6_CerebellumDemo.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx7()
    {
        String projFileName = "nCexamples/Ex7_PSICSDemo/Ex7_PSICSDemo.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx8()
    {
        String projFileName = "nCexamples/Ex8_PyNNDemo/Ex8_PyNNDemo.ncx";
        checkProject(projFileName);
    }
    @Test public void testEx9()
    {
        String projFileName = "nCexamples/Ex9_Synapses/Ex9_Synapses.ncx";
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
    @Test public void testCA1PyramidalCell()
    {
        String projFileName = "nCmodels/CA1PyramidalCell/CA1PyramidalCell.ncx";
        checkProject(projFileName);
    }
    @Test public void testDentateGyrus()
    {
        String projFileName = "nCmodels/DentateGyrus/DentateGyrus.ncx";

        ArrayList<String> cellsToIgnore = new ArrayList<String>();
        cellsToIgnore.add("PerforantPath");  // as it has a low spec cap to give fast response to input

        checkProject(projFileName, cellsToIgnore, true);
    }
    @Test public void testMainenEtAl_PyramidalCell()
    {
        String projFileName = "nCmodels/MainenEtAl_PyramidalCell/MainenEtAl_PyramidalCell.ncx";
        ArrayList<String> cellsToIgnore = new ArrayList<String>();
        cellsToIgnore.add("MainenCellMod");

        checkProject(projFileName, cellsToIgnore, true);
    }

    /*
    @Test public void testPurkinjeCell()
    {
        String projFileName = "nCmodels/PurkinjeCell/PurkinjeCell.ncx";
        //ArrayList<String> cellsToIgnore = new ArrayList<String>();
        //cellsToIgnore.add("MainenCellMod");

        checkProject(projFileName);
    }
    @Test public void testRothmanEtAl_KoleEtAl_PyrCell()
    {
        String projFileName = "nCmodels/RothmanEtAl_KoleEtAl_PyrCell/RothmanEtAl_KoleEtAl_PyrCell.ncx";
        //ArrayList<String> cellsToIgnore = new ArrayList<String>();
        //cellsToIgnore.add("MainenCellMod");

        checkProject(projFileName);
    }*/

    @Test public void testThalamocortical()
    {
        String projFileName = "nCmodels/Thalamocortical/Thalamocortical.ncx";
        ArrayList<String> cellsToIgnore = new ArrayList<String>();
        cellsToIgnore.add("pyrFRB_orig");

        checkProject(projFileName, cellsToIgnore, false);
    }



    private void checkProject(String projFileName)
    {
        checkProject(projFileName, null, false);
    }


    private void checkProject(String projFileName, ArrayList<String> cellsToIgnore, boolean ignoreDisconnectedSegments)
    {
        File projFile = new File(projFileName);
        System.out.println("Going to check project: "+ projFile.getAbsolutePath());

        assertTrue("Problem finding file: "+projFile.getAbsolutePath(), projFile.exists());


        ProjectManager pm = new ProjectManager();
        Project project = null;
        try
        {
            project = pm.loadProject(projFile);
        }
        catch (ProjectFileParsingException e)
        {
            fail("Problem loading proj: "+ projFile+"\n"+ e.getMessage());
        }

        assertTrue("Insufficiently long project description in: "+project,
                project.getProjectDescription().length()>300);


        String validity = pm.getValidityReport(false,
                                               true,
                                               true,
                                               true,
                                               cellsToIgnore,
                                               ignoreDisconnectedSegments,
                                               true,
                                               true);

        //System.out.println("Validity: "+ validity);

        if (validity.indexOf(ValidityStatus.PROJECT_IS_VALID)<0)
        {
            fail("Project was not valid: "+ project+"\n"+validity);
        }

        



    }


    public static void main(String[] args)
            
    {
        System.out.println("Running the main nC model tests...");


        Result r = null;

        
        r = org.junit.runner.JUnitCore.runClasses(ucl.physiol.neuroconstruct.test.ModelTest.class);
        
        MainTest.checkResults(r);

    }
    
}