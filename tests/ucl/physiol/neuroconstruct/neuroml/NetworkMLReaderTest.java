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
    public void testSavingLoadingNetworkML() throws InterruptedException, NeuroMLException, Hdf5Exception, EndOfSequenceException
    {
        System.out.println("---  testSavingLoadingNetworkML");
        
        Project proj = pm.getCurrentProject();
        
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
                
        pm.doGenerate(sc.getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(2000);
        }
        
        
        StringBuffer stateString1 = new StringBuffer();
        
        stateString1.append(proj.generatedCellPositions.toLongString(false));
        stateString1.append(proj.generatedNetworkConnections.details(false));
        stateString1.append(proj.generatedElecInputs.toString());
        
        
        
        
        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString1);
        
        File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);
        
        File nmlFile = new File(saveNetsDir, "test.nml");
        
        boolean zipped = false;
        
        nmlFile = ProjectManager.saveNetworkStructureXML(proj, 
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
        
        
        proj.resetGenerated();
                
        pm.doLoadNetworkML(nmlFile, true);
        
        
        StringBuffer stateString2 = new StringBuffer();
        
        stateString2.append(proj.generatedCellPositions.toLongString(false));
        stateString2.append(proj.generatedNetworkConnections.details(false));
        stateString2.append(proj.generatedElecInputs.toString());
        
        
        System.out.println("Reloaded proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString2);
        
        assertEquals(stateString1.toString(), stateString2.toString());
        
        System.out.println("Strings representing internal states equal!");
        
        
        //test the NetworkML reader on a Level3 file
        File l3File = new File(saveNetsDir, "l3test.nml");
        ProjectManager.saveLevel3NetworkXML(proj, l3File, false, false, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        assertTrue(l3File.exists());
        
        Iterator<String> groups = proj.generatedCellPositions.getNamesGeneratedCellGroups();
        ArrayList<String> cells = new ArrayList<String>();
        while (groups.hasNext()) {
            String string = groups.next();
            cells.add(proj.cellGroupsInfo.getCellType(string));
        }
        Vector<Cell> projCells = proj.cellManager.getAllCells();
        Vector cellMechs = proj.cellMechanismInfo.getAllCellMechanisms();
        Iterator<String> nets =  proj.generatedNetworkConnections.getNamesNetConnsIter();
        Vector<String> netsVector = new Vector<String>();
        while (nets.hasNext()) {
           netsVector.add(nets.next());            
        }
        ArrayList<String> inputs = proj.generatedElecInputs.getInputReferences();

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
                
        proj.resetGenerated();


        String projName2 = "TestNetworkML_reloaded";
        File projDir2 = new File(MainTest.getTempProjectDirectory()+ projName2);

        Project proj2 = Project.createNewProject(projDir2.getAbsolutePath(), projName2, null);

        pm.setCurrentProject(proj2);

        pm.doLoadNetworkML(l3File, true);

        //TODO: more thorough checks needed!!

        assertTrue(proj.cellManager.getAllCellTypeNames().containsAll(proj2.cellManager.getAllCellTypeNames()));

        assertTrue(proj.cellMechanismInfo.getAllCellMechanismNames().containsAll(proj2.cellMechanismInfo.getAllCellMechanismNames()));

        assertTrue(proj.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().containsAll(proj2.morphNetworkConnectionsInfo.getAllSimpleNetConnNames()));

        assertTrue(proj.elecInputInfo.getAllStimRefs().containsAll(proj2.elecInputInfo.getAllStimRefs()));

      

        
    }

     public static void main(String[] args)
    {
        NetworkMLReaderTest ct = new NetworkMLReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }

}