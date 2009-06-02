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
import javax.xml.validation.*;
import javax.xml.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import ucl.physiol.neuroconstruct.cell.Cell;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.Hdf5Exception;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.ColourUtils.ColourRecord;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator.EndOfSequenceException;

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
    
    public NetworkMLReaderTest() 
    {
        
    }



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

    @After
    public void tearDown() {
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
        
        
        StringBuffer stateString1 = new StringBuffer();
        
        stateString1.append(proj0.generatedCellPositions.toLongString(false));
        stateString1.append(proj0.generatedNetworkConnections.details(false));
        stateString1.append(proj0.generatedElecInputs.toString());
        
        
        
        
        System.out.println("Generated proj with: "+ proj0.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString1);
        
        File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);
        
        File nmlFile = new File(saveNetsDir, "test.nml");
        
        boolean zipped = false;
        
        nmlFile = ProjectManager.saveNetworkStructureXML(proj0,
                                                         nmlFile, 
                                                         zipped, 
                                                         true, 
                                                         sc.getName(),
                                                         NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        
        assertTrue(nmlFile.exists());
        
        System.out.println("Saved NetworkML in: "+ nmlFile.getAbsolutePath());
        
        File schemaFile = GeneralProperties.getNeuroMLSchemaFile();

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        

        Source schemaFileSource = new StreamSource(schemaFile);
        try
        {
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(nmlFile);

            validator.validate(xmlFileSource);
        } 
        catch (Exception ex)
        {
            fail("Unable to validate saved NetworkML file: "+ nmlFile+"\n"+ex.toString());
        }
        System.out.println(nmlFile.getAbsolutePath()+" is valid according to: "+ schemaFile);
        
         
        proj0.resetGenerated();

                
        pm.doLoadNetworkML(nmlFile, true);
        
       
        StringBuffer stateString2 = new StringBuffer();
        
        stateString2.append(proj0.generatedCellPositions.toLongString(false));
        stateString2.append(proj0.generatedNetworkConnections.details(false));
        stateString2.append(proj0.generatedElecInputs.toString());
        
        
        System.out.println("Reloaded proj with: "+ proj0.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString2);
        
        assertEquals(stateString1.toString(), stateString2.toString());
        
        System.out.println("Strings representing internal states equal!");
        
        
        //test the NetworkML reader on a Level3 file
        File l3File = new File(saveNetsDir, "l3test.nml");
        ProjectManager.saveLevel3NetworkXML(proj0, l3File, false, false, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        assertTrue(l3File.exists());
        
        Iterator<String> groups = proj0.generatedCellPositions.getNamesGeneratedCellGroups();
        ArrayList<String> cells = new ArrayList<String>();
        while (groups.hasNext()) {
            String string = groups.next();
            cells.add(proj0.cellGroupsInfo.getCellType(string));
        }
        Vector<Cell> projCells = proj0.cellManager.getAllCells();
        Vector cellMechs = proj0.cellMechanismInfo.getAllCellMechanisms();
        Iterator<String> nets =  proj0.generatedNetworkConnections.getNamesNetConnsIter();
        Vector<String> netsVector = new Vector<String>();
        while (nets.hasNext()) {
           netsVector.add(nets.next());            
        }
        ArrayList<String> inputs = proj0.generatedElecInputs.getInputReferences();

        System.out.println("Saved Level 3 Network  in: "+ l3File.getAbsolutePath());

        try
        {
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(l3File);

            validator.validate(xmlFileSource);
        } 
        catch (Exception ex)
        {
            fail("Unable to validate saved Level 3 file: "+ nmlFile+"\n"+ex.toString());
        }
        
        System.out.println("-----------------  "+l3File.getAbsolutePath()+" is valid according to: "+ schemaFile+"  -----------------");
               
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
        
   
        
        
        //test the NetworkML reader on a Level3 file with annotations
        System.out.println("\n\n*** LEVEL3 WITH ANNOTATIONS TEST ***\n");
        File l3FileAnnotations = new File(saveNetsDir, "l3testAnnotations.nml");

        //pm.setCurrentProject(proj);
   
        //proj = pm.getCurrentProject();
        boolean annotations = true;
        
        ProjectManager.saveLevel3NetworkXML(proj0, l3FileAnnotations, false, false, annotations, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        assertTrue(l3FileAnnotations.exists());
 
        try
        {
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(l3FileAnnotations);

            validator.validate(xmlFileSource);
        }
        catch (Exception ex)
        {
            fail("Unable to validate saved Level 3 file: "+ nmlFile+"\n"+ex.toString());
        }

        System.out.println("-----------------  "+l3FileAnnotations.getAbsolutePath()+" is valid according to: "+ schemaFile+"  -----------------");

        pm.doLoadNetworkMLAndGenerate(l3FileAnnotations, true);
        assertTrue(pm.isGenerating());
        
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
        assertTrue(compareAbstractTableObjects(proj0.cellMechanismInfo, proj3.cellMechanismInfo));
        System.out.println("OK");
        
        System.out.println("comparing simPlotInfo...");
        assertTrue(compareAbstractTableObjects(proj0.simPlotInfo, proj3.simPlotInfo));
        System.out.println("OK");
        
        System.out.println("comparing simConfigInfo...");
        proj0.simConfigInfo.equals(proj3.simConfigInfo);
        System.out.println("OK");
       
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