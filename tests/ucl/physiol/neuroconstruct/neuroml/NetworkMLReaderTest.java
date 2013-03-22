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

package ucl.physiol.neuroconstruct.neuroml;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.Hdf5Exception;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.NeuroMLVersion;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.ColourUtils.ColourRecord;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator.EndOfSequenceException;
//import static org.junit.Assume.*;

/**
 *
 * @author padraig
 */
public class NetworkMLReaderTest 
{
    String projName = "TestNetworkML";
    File projDir = new File(MainTest.getTestProjectDirectory()+ projName);
        
    ProjectManager pm = null;
    
    boolean verbose = true;
    


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() NetworkMLReaderTest");
        
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

    /*
    @Test
    public void testTruth() 
    {
        assumeTrue(false);
        assertEquals(1, 2);
    }*/


    @Test
    public void testSavingNeuroML2() throws InterruptedException, NeuroMLException
    {
        System.out.println("---  testSavingLoadingNeuroML2");

        Project proj0 = pm.getCurrentProject();
        SimConfig sc = proj0.simConfigInfo.getDefaultSimConfig();

        pm.doGenerate(sc.getName(), 1234);

        while(pm.isGenerating())
        {
            Thread.sleep(2000);
        }

        System.out.println("Generated proj with: "+ proj0.generatedCellPositions.getNumberInAllCellGroups()+" cells");


        System.out.println("------------\nGenerated plots: "+proj0.generatedPlotSaves.details());

        File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);

        boolean zipped = false;

        File nml2aFile = new File(saveNetsDir, "test2a.xml");

        nml2aFile = NeuroMLFileManager.saveNetworkStructureXML(proj0,
                                                         nml2aFile,
                                                         zipped,
                                                         true,
                                                         sc.getName(),
                                                         NetworkMLConstants.UNITS_PHYSIOLOGICAL,
                                                         NeuroMLVersion.NEUROML_VERSION_2_ALPHA);

        assertTrue(nml2aFile.exists());

        System.out.println("Saved NetworkML in: "+ nml2aFile.getAbsolutePath());

        assertTrue(NeuroMLFileManager.validateAgainstNeuroML2alphaSchema(nml2aFile));

        File nml2bFile = new File(saveNetsDir, "test2b.xml");

        nml2bFile = NeuroMLFileManager.saveNetworkStructureXML(proj0,
                                                         nml2bFile,
                                                         zipped,
                                                         true,
                                                         sc.getName(),
                                                         NetworkMLConstants.UNITS_PHYSIOLOGICAL,
                                                         NeuroMLVersion.NEUROML_VERSION_2_BETA);

        assertTrue(nml2bFile.exists());

        System.out.println("Saved NetworkML in: "+ nml2bFile.getAbsolutePath());

        assertTrue(NeuroMLFileManager.validateAgainstNeuroML2betaSchema(nml2bFile));
    }
    
    @Test
    public void testSavingLoadingNetworkML() throws InterruptedException, NeuroMLException, Hdf5Exception, EndOfSequenceException, NoProjectLoadedException
    {
        System.out.println("---  testSavingLoadingNetworkML");
        
        Project proj0 = pm.getCurrentProject();
        SimConfig sc = proj0.simConfigInfo.getDefaultSimConfig();
                
        pm.doGenerate(sc.getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(2000);
        }

        StringBuilder stateString1 = new StringBuilder();
        
        stateString1.append(proj0.generatedCellPositions.toLongString(false));
        stateString1.append(proj0.generatedNetworkConnections.details(false));
        stateString1.append(proj0.generatedElecInputs.toString());

        StringBuilder plotsString1 = new StringBuilder();
        plotsString1.append(proj0.generatedPlotSaves.details());

        String input0 = proj0.elecInputInfo.getAllStims().get(0).getElectricalInput().toString();
        String input1 = proj0.elecInputInfo.getAllStims().get(1).getElectricalInput().toString();
        
        System.out.println("Generated proj with: "+ proj0.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString1);

        System.out.println("------------\nGenerated plots: "+proj0.generatedPlotSaves.details());
        
        File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);
        
        File nmlFile = new File(saveNetsDir, "test.xml");
        
        boolean zipped = false;
        
        nmlFile = ProjectManager.saveNetworkStructureXML(proj0,
                                                         nmlFile, 
                                                         zipped, 
                                                         true, 
                                                         sc.getName(),
                                                         NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        
        assertTrue(nmlFile.exists());
        
        System.out.println("Saved NetworkML in: "+ nmlFile.getAbsolutePath());
        
        assertTrue(NeuroMLFileManager.validateAgainstLatestNeuroML1Schema(nmlFile));


        String validity1 = pm.getValidityReport(false);
        validity1 = GeneralUtils.replaceAllTokens(validity1, proj0.getProjectFile()+"", "<proj_path>");
        
        proj0.resetGenerated();
                
        //pm.doLoadNetworkML(nmlFile, true);
        pm.doLoadNetworkMLAndGeneratePlots(nmlFile, true);

        while(pm.isGenerating())
        {
            Thread.sleep(2000);
        }

        System.out.println("------------\nGenerated plots: "+proj0.generatedPlotSaves.details());
       
        StringBuffer stateString2 = new StringBuffer();
        
        stateString2.append(proj0.generatedCellPositions.toLongString(false));
        stateString2.append(proj0.generatedNetworkConnections.details(false));
        stateString2.append(proj0.generatedElecInputs.toString());
        //proj0.generatedNetworkConnections.getSynapticConnections(projName)
        String input1_0 = proj0.elecInputInfo.getAllStims().get(0).getElectricalInput().toString();
        String input1_1 = proj0.elecInputInfo.getAllStims().get(1).getElectricalInput().toString();


        StringBuilder plotsString2 = new StringBuilder();
        plotsString2.append(proj0.generatedPlotSaves.details());
        
        
        System.out.println("Reloaded proj with: "+ proj0.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString2);
        
        assertEquals(stateString1.toString(), stateString2.toString());
        System.out.println("Strings representing internal states equal!");

        assertEquals(input0, input1_0);
        assertEquals(input1, input1_1);

        assertEquals(plotsString1.toString(), plotsString2.toString());

        //test the NetworkML reader on a Level3 file
        File l3File = new File(saveNetsDir, "Level3Test.xml");
        ProjectManager.saveLevel3NetworkXML(proj0, l3File, false, false, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        assertTrue(l3File.exists());
        
        Iterator<String> groups = proj0.generatedCellPositions.getNamesGeneratedCellGroups();
        ArrayList<String> cells = new ArrayList<String>();
        while (groups.hasNext()) {
            String string = groups.next();
            cells.add(proj0.cellGroupsInfo.getCellType(string));
        }
        Iterator<String> nets =  proj0.generatedNetworkConnections.getNamesNetConnsIter();
        Vector<String> netsVector = new Vector<String>();
        while (nets.hasNext()) {
           netsVector.add(nets.next());            
        }

        System.out.println("Saved Level 3 Network  in: "+ l3File.getAbsolutePath());

        assertTrue(NeuroMLFileManager.validateAgainstLatestNeuroML1Schema(l3File));
        //proj.resetGenerated();


        String projName2 = "TestNetworkML_reloaded";
        File projDir2 = new File(MainTest.getTempProjectDirectory()+ projName2);

        Project proj2 = Project.createNewProject(projDir2.getAbsolutePath(), projName2, null);

        pm.setCurrentProject(proj2);

        pm.doLoadNetworkML(l3File, true);

        proj2.saveProject();

        //TODO: more thorough checks needed!!

        assertTrue(proj0.cellManager.getAllCellTypeNames().containsAll(proj2.cellManager.getAllCellTypeNames()));

        assertTrue(proj0.cellMechanismInfo.getAllCellMechanismNames().containsAll(proj2.cellMechanismInfo.getAllCellMechanismNames()));

        assertTrue(proj0.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().containsAll(proj2.morphNetworkConnectionsInfo.getAllSimpleNetConnNames()));

        assertTrue(proj0.elecInputInfo.getAllStimRefs().containsAll(proj2.elecInputInfo.getAllStimRefs()));

        String validity2 = pm.getValidityReport(false);

        validity2 = GeneralUtils.replaceAllTokens(validity2, proj2.getProjectFile().getAbsolutePath(), "<proj_path>");

        System.out.println("..."+ GeneralUtils.replaceAllTokens(validity1, "\n", " "));
        System.out.println("..."+ GeneralUtils.replaceAllTokens(validity2, "\n", " "));

        assertEquals(validity1, validity2);

        String input2_0 = proj2.elecInputInfo.getAllStims().get(0).getElectricalInput().toString();
        String input2_1 = proj2.elecInputInfo.getAllStims().get(1).getElectricalInput().toString();

        System.out.println("---- pre:  "+input0);
        System.out.println("---- post: "+input2_0);



        assertEquals(input0, input2_0);
        assertEquals(input1, input2_1);
        
   
        
        
        //test the NetworkML reader on a Level3 file with annotations
        System.out.println("\n\n*** LEVEL3 WITH ANNOTATIONS TEST ***\n");
        File l3FileAnnotations = new File(saveNetsDir, "l3testAnnotations.nml");

        //pm.setCurrentProject(proj);
   
        //proj = pm.getCurrentProject();
        boolean annotations = true;
        
        ProjectManager.saveLevel3NetworkXML(proj0, l3FileAnnotations, false, false, annotations, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        
        assertTrue(l3FileAnnotations.exists());
        assertTrue(NeuroMLFileManager.validateAgainstLatestNeuroML1Schema(l3FileAnnotations));
 
       
        /*
        pm.doLoadNetworkMLAndGeneratePlots(l3FileAnnotations, true);


        System.out.println("-------3-----\nGenerated plots: "+pm.getCurrentProject().generatedPlotSaves.details());

        assertTrue(pm.isGenerating());*/


        String projName3 = "TestNetworkML_reloaded_withAnnotations";
        File projDir3 = new File(MainTest.getTempProjectDirectory()+ projName3);
        projDir3.mkdir();
        
        Project proj3 = Project.createNewProject(projDir3.getAbsolutePath(), projName3, null);   

        pm.setCurrentProject(proj3);
        pm.doLoadNetworkML(l3FileAnnotations, true);

        proj3.saveProject();

        System.out.println("Comparing info in project: "+proj0.getProjectFullFileName()+" to "+proj3.getProjectFullFileName()+"...");

 
        System.out.println("comparing basic info...");
        assertNotSame(proj0.getProjectName(), proj3.getProjectName());
        //assertEquals(proj.getProjectDescription(), proj3.getProjectDescription());
        //assertEquals(proj.getProjectFileVersion(), proj3.getProjectFileVersion());
        System.out.println("OK");

        System.out.println("comparing regionsInfo...");
        assertTrue(compareAbstractTableObjects(proj0.regionsInfo, proj3.regionsInfo));
        System.out.println("OK");
        
        System.out.println("comparing cellGroupsInfo...");
        assertTrue(compareAbstractTableObjects(proj0.cellGroupsInfo, proj3.cellGroupsInfo));
        System.out.println("OK");
        
        System.out.println("comparing morphNetworkConnectionsInfo...");
        assertTrue(compareAbstractTableObjects(proj0.morphNetworkConnectionsInfo, proj3.morphNetworkConnectionsInfo));
        System.out.println("OK");
        
        System.out.println("comparing volBasedConnsInfo...");
        assertTrue(compareAbstractTableObjects(proj0.volBasedConnsInfo, proj3.volBasedConnsInfo));
        System.out.println("OK");
        
        System.out.println("comparing cellMechanismInfo...");
        System.out.println("Proj 1: ");
        for (CellMechanism cm: proj0.cellMechanismInfo.getAllCellMechanisms())
        {
            System.out.println(cm);
        }
        System.out.println("Proj other: ");
        for (CellMechanism cm: proj3.cellMechanismInfo.getAllCellMechanisms())
        {
            System.out.println(cm);
        }
        assertTrue(compareAbstractTableObjects(proj0.cellMechanismInfo, proj3.cellMechanismInfo));
        System.out.println("OK");
        
        System.out.println("comparing simPlotInfo...");
        assertTrue(compareAbstractTableObjects(proj0.simPlotInfo, proj3.simPlotInfo));
        System.out.println("OK");
        
        System.out.println("comparing simConfigInfo...");
        proj0.simConfigInfo.equals(proj3.simConfigInfo);
        System.out.println("OK");


        String validity3 = pm.getValidityReport(false);

        validity3 = GeneralUtils.replaceAllTokens(validity3, proj3.getProjectFile().getAbsolutePath(), "<proj_path>");

        assertEquals(validity1, validity3);


        String input3_0 = proj3.elecInputInfo.getAllStims().get(0).getElectricalInput().toString();
        String input3_1 = proj3.elecInputInfo.getAllStims().get(1).getElectricalInput().toString();

        assertEquals(input0, input3_0);
        assertEquals(input1, input3_1);
       
    }

    
    public boolean compareAbstractTableObjects(AbstractTableModel object1, AbstractTableModel object2)
    {
        boolean compare = true;
        int col1 = object1.getColumnCount();
        int row1 = object1.getRowCount();
        int col2 = object2.getColumnCount();
        int row2 = object2.getRowCount();

        if (col1==col2 && row1==row2)
        {
            for (int i = 0; i < col1; i++)
            {
                for (int j = 0; j < row1; j++) 
                {
                    if (!object1.getValueAt(j,i).equals(object2.getValueAt(j,i)) && object1.getColumnClass(i).equals(ColourRecord.class))
                    {
                        compare = false;
                        System.out.println("Difference: \n"+object1.getValueAt(j,i)+" in object "+object1.getColumnName(i)+"\n"+object2.getValueAt(j,i)+" in object "+object2.getColumnName(i));                        
                    }
                }
            }
        }
        else
        {
            compare = false;
            System.out.println("Dimensions mismatch: \ntable1=["+col1+"*"+row1+"]\ntable1=["+col2+"*"+row2+"]");                        
        }
        return compare;
    }

     public static void main(String[] args)
    {
        NetworkMLReaderTest ct = new NetworkMLReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }

}