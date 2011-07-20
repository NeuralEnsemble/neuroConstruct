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
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.*;
import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.ProjectFileParsingException;
import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.project.SimConfig;
import static org.junit.Assert.*;

/**
 *
 * Test core behaviour of neuroConstruct example models
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
        String projFileName = "osb/models/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/GranuleCell.ncx";
        checkProject(projFileName);
    }
    @Test public void testGranCellLayer()
    {
        String projFileName = "osb/models/cerebellum/networks/GranCellLayer/neuroConstruct/GranCellLayer.ncx";
        checkProject(projFileName);
    }
    @Test public void testCA1PyramidalCell()
    {
        String projFileName = "osb/models/hippocampus/CA1_pyramidal_neuron/CA1PyramidalCell/neuroConstruct/CA1PyramidalCell.ncx";
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
        String projFileName = "osb/models/cerebral_cortex/neocortical_pyramidal_neuron/MainenEtAl_PyramidalCell/neuroConstruct/MainenEtAl_PyramidalCell.ncx";
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

    @Test public void testSolinasEtAl_GolgiCell()
    {
        String projFileName = "osb/models/cerebellum/cerebellar_golgi_cell/SolinasEtAl_GolgiCell/neuroConstruct/SolinasEtAl_GolgiCell.ncx";

        ArrayList<String> cellsToIgnore = new ArrayList<String>();

        checkProject(projFileName, cellsToIgnore, false);
    }
  /*
    @Test public void testVervaekeEtAl_GolgiCellNetwork()
    {
        String projFileName = "nCmodels/VervaekeEtAl-GolgiCellNetwork/VervaekeEtAl-GolgiCellNetwork.ncx";

        ArrayList<String> cellsToIgnore = new ArrayList<String>();

        checkProject(projFileName, cellsToIgnore, false);
    }
     * 
     */

  
    @Test public void testThalamocortical()
    {
        String projFileName = "osb/models/cerebral_cortex/networks/Thalamocortical/neuroConstruct/Thalamocortical.ncx";
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
        MpiSettings ms = new MpiSettings();

        for(SimConfig sc:project.simConfigInfo.getAllSimConfigs())
        {
            assertTrue("All MPI Confis need to be set to LOCAL_SERIAL for core projects! It's not in sim conf "+sc+" in: "+project,
                sc.getMpiConf().equals(ms.getMpiConfiguration(MpiSettings.LOCAL_SERIAL)));
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