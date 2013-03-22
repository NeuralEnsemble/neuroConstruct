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

package ucl.physiol.neuroconstruct.neuroml.hdf5;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
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
        
        System.out.println("Sys prop: "+System.getProperty("java.library.path"));
        
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
    public void testSavingLoadingNetworkMLHDF5() throws InterruptedException, Hdf5Exception, NeuroMLException, EndOfSequenceException
    {
        System.out.println("---  testSavingLoadingNetworkMLHDF5");
        
        if (GeneralUtils.is64bitPlatform() && GeneralUtils.isWindowsBasedPlatform())
        {
            if (System.getProperty("os.arch").contains("64"))
            {
                System.out.println("****  Not testing NetworkML HDF5 functionality.  ****");
                System.out.println("****  64bit JDK on 64bit Windows machine: No dlls!!  ****");
                return;
            }
        }
        
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
                                                        sc,
                                                        NetworkMLConstants.UNITS_PHYSIOLOGICAL);
        
        assertTrue(nmlFile.exists());
        
        
        System.out.println("Saved NetworkML in: "+ nmlFile.getAbsolutePath());
        
        
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
        
    }
    
    
    
    public static void main(String[] args)
    {
        NetworkMLReaderTest ct = new NetworkMLReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
    


}