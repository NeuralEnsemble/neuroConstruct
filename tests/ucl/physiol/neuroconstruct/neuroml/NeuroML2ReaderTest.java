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
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package ucl.physiol.neuroconstruct.neuroml;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.Result;
import org.neuroml.model.Cell;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.IonChannel;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.OriginalCompartmentalisation;
import ucl.physiol.neuroconstruct.project.NoProjectLoadedException;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.project.ProjectStructure;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

public class NeuroML2ReaderTest
{

    private void loadNML2File(File nml2File) throws NeuroMLException, InterruptedException, NoProjectLoadedException, IOException, org.neuroml.model.util.NeuroMLException
    {
        ProjectManager pm = new ProjectManager(null, null);
        Project testNeuroML2Proj = null;
        if (pm.getCurrentProject()==null)
        {  
           String projectName = nml2File.getName().indexOf(".")>1 ? nml2File.getName().substring(0, nml2File.getName().indexOf(".")) : nml2File.getName();
           System.out.println("Will make a new project: "+projectName);

           File projDir = new File(nml2File.getParentFile().getAbsolutePath(), "temp");

           System.out.println("Project in: "+projDir.getAbsolutePath());

           testNeuroML2Proj=Project.createNewProject(projDir.getAbsolutePath(),
                                   projectName,
                                   Project.getDummyProjectEventListener());

           pm.setCurrentProject(testNeuroML2Proj);

           pm.getCurrentProject().saveProject();
           System.out.println("Project in: "+testNeuroML2Proj.getProjectFullFileName());

        }

        if (pm.getCurrentProject() != null)
        {
           System.out.println("Test project is set in "+System.getProperty("user.home")+"/nC_projects/");
           pm.doLoadNeuroML2Network(nml2File, false);
        }

        Thread.sleep(5);
        System.out.println("Waiting...");
        
        
		NeuroMLConverter nmlc = new NeuroMLConverter();
    	NeuroMLDocument nmlDocument = nmlc.loadNeuroML(nml2File, true, true);
        
        System.out.println("NeuroML 2 summary:\n"+nmlc.summary(nmlDocument));
        
        if (!nmlDocument.getNetwork().isEmpty())
        {
            Network nml2net = nmlDocument.getNetwork().get(0);

            assertTrue(testNeuroML2Proj.cellGroupsInfo.getAllCellGroupNames().size()==nml2net.getPopulation().size());
            assertTrue(testNeuroML2Proj.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().size()==nml2net.getProjection().size());
          
        }
        //ArrayList<String> ics = new ArrayList<String>();
        for (IonChannel ic:nmlDocument.getIonChannel())
        {   
            System.out.println("Checking: "+ic);
            assertTrue(testNeuroML2Proj.cellMechanismInfo.getAllCellMechanismNames().contains(ic.getId()));
        }
        for (ExpTwoSynapse syn:nmlDocument.getExpTwoSynapse())
        {
            System.out.println("Checking: "+syn);
            assertTrue(testNeuroML2Proj.cellMechanismInfo.getAllCellMechanismNames().contains(syn.getId()));
        }
        for (Cell cell: nmlDocument.getCell())
        {
            System.out.println("Checking: "+cell);
            assertTrue(testNeuroML2Proj.cellManager.getAllCellTypeNames().contains(cell.getId()));
        }
        /************** To do...
        File nmlFile = File.createTempFile("nml2", nml2File.getName());

        NeuroMLFileManager nmlfm = new NeuroMLFileManager(testNeuroML2Proj);
        
        nmlfm.generateNeuroMLFiles(pm.getCurrentProject().simConfigInfo.getAllSimConfigs().get(0),
                                                          NeuroMLConstants.NeuroMLVersion.getLatestVersion(),
                                                          LemsConstants.LemsOption.NONE,
                                                          new OriginalCompartmentalisation(),
                                                          123,
                                                         false,
                                                         true,
                                                         new File(testNeuroML2Proj.getProjectMainDirectory(),"generatedNeuroML2"),
                                                         UnitConverter.getUnitSystemDescription(UnitConverter.GENESIS_SI_UNITS),
                                                         false);*/
        
    	//NeuroMLDocument nmlDocumentReloaded = nmlc.loadNeuroML(nmlFile, true, true);
        
        //System.out.println("NeuroML 2 summary (reloaded from "+nmlFile.getAbsolutePath()+"):\n"+nmlc.summary(nmlDocumentReloaded));
    }

    @Test
    public void testTwoCell() throws NeuroMLException, InterruptedException, NoProjectLoadedException, IOException, org.neuroml.model.util.NeuroMLException
    {
        loadNML2File(new File("testProjects/TestNeuroML2Files/ACnet2/TwoCell.net.nml"));
        
    }
/*
    @Test
    public void testACNet2() throws NeuroMLException, InterruptedException, NoProjectLoadedException, IOException, org.neuroml.model.util.NeuroMLException
    {
        loadNML2File(new File("testProjects/TestNeuroML2Files/ACnet2/MediumNet.net.nml"));
        
    }
    @Test
    public void testPyrCell() throws NeuroMLException, InterruptedException, NoProjectLoadedException, IOException, org.neuroml.model.util.NeuroMLException
    {
        loadNML2File(new File("testProjects/TestNeuroML2Files/ACnet2/pyr_4_sym.cell.nml"));
        
    }*/
    
    
    public static void main(String[] args)
    {
        NeuroML2ReaderTest ct = new NeuroML2ReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }
}
