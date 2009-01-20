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

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author padraig
 */
public class ProjectTest 
{
    static
    {
        //System.out.println("ProjectTest static: "+ProjectStructure.getCMLExamplesDir());

    }
    
    
    String projName = "Ex5_Networks";
    File projDir = new File(ProjectStructure.getnCExamplesDir()+ "/"+projName);
    File projFile = null;
    
    Project projLoaded = null;
    
    public ProjectTest() 
    {
    }


    @Before
    public void setUp() 
    {
        
        System.out.println("---------------   setUp() ProjectTest");
        projFile = new File(projDir, projName+ProjectStructure.getProjectFileExtension());
        
        try 
        {
            projLoaded = Project.loadProject(projFile, null);
        
            System.out.println("Proj status: "+ projLoaded.getProjectStatusAsString());
            
            
        } 
        catch (ProjectFileParsingException ex) 
        {
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
   
        
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createNewProject method, of class Project.
     */
    @Test
    public void testCreateNewProject() {
        System.out.println("---  createNewProject");
      
        String projName2 = "TestingFrameworkProject";
        File projDir2 = new File("..\temp");
    
        Project proj = Project.createNewProject(projDir2.getAbsolutePath(), projName2, null);
        
        assertEquals(proj.getProjectName(), projName2);
    }


    /**
     * Test of getProjectFileName method, of class Project.
     */
    @Test
    public void testGetProjectFileName() {
        System.out.println("---  getProjectFileName");
     
        String expResult = projName+ProjectStructure.getProjectFileExtension();
        String result = projLoaded.getProjectFileName();
        assertEquals(expResult, result);
        
    }





}