/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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