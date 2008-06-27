/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.neuroml.hdf5;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator.EndOfSequenceException;

/**
 *
 * @author padraig
 */
public class NetworkMLReaderTest 
{

    String projName = "TestNetworkML";
    File projDir = new File("testProjects/"+ projName);
    boolean verbose = false;
        
    ProjectManager pm = null;
    
    public NetworkMLReaderTest() {
    }


    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() hdf5.NetworkMLReaderTest");
        
        File projFile = new File(projDir, projName+ProjectStructure.getProjectFileExtension());
        
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
    public void testSavingLoadingNetworkMLHDF5() throws InterruptedException, Hdf5Exception, NeuroMLException, EndOfSequenceException
    {
        System.out.println("---  testSavingLoadingNetworkMLHDF5");
        
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
        
        File nmlFile = new File(saveNetsDir, "test.h5");
        
        
        nmlFile = NetworkMLWriter.createNetworkMLH5file(nmlFile, 
                                                        proj,
                                                        NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        
        assertTrue(nmlFile.exists());
        
        
        System.out.println("Saved NetworkML in: "+ nmlFile.getAbsolutePath());
        
        
        proj.resetGenerated();
                
        pm.doLoadNetworkML(nmlFile);
        
        
        StringBuffer stateString2 = new StringBuffer();
        
        stateString2.append(proj.generatedCellPositions.toLongString(false));
        stateString2.append(proj.generatedNetworkConnections.details(false));
        stateString2.append(proj.generatedElecInputs.toString());
        
        
        System.out.println("Reloaded proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        if (verbose) 
            System.out.println(stateString2);
        
        assertEquals(stateString1.toString(), stateString2.toString());
        
    }


}