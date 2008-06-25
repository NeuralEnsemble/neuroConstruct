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
        ;
    }
    
    
    String projName = "Ex5-Networks";
    File projDir = new File("examples/"+ projName);
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
      
        String projName = "TestingFrameworkProject";
        File projDir = new File("..\temp");
    
        Project proj = Project.createNewProject(projDir.getAbsolutePath(), projName, null);
        
        assertEquals(proj.getProjectName(), projName);
    }

   
//
//    /**
//     * Test of loadProject method, of class Project.
//     */
//    @Test
//    public void testLoadProject() throws Exception {
//        System.out.println("loadProject");
//        File projectFile = null;
//        ProjectEventListener projectEventListner = null;
//        Project expResult = null;
//        Project result = Project.loadProject(projectFile, projectEventListner);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//

//    /**
//     * Test of getProjectStatusAsString method, of class Project.
//     */
//    @Test
//    public void testGetProjectStatusAsString() {
//        System.out.println("getProjectStatusAsString");
//        
//        String expResult = "";
//        String result = instance.getProjectStatusAsString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
    
//
//    /**
//     * Test of saveNetworkStructureXML method, of class Project.
//     */
//    @Test
//    public void testSaveNetworkStructureXML() throws Exception {
//        System.out.println("saveNetworkStructureXML");
//        File neuroMLFile = null;
//        boolean zipped = false;
//        boolean extraComments = false;
//        String simConfig = "";
//        Project instance = null;
//        File expResult = null;
//        File result = instance.saveNetworkStructureXML(neuroMLFile, zipped, extraComments, simConfig);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveProject method, of class Project.
//     */
//    @Test
//    public void testSaveProject() throws Exception {
//        System.out.println("saveProject");
//        Project instance = null;
//        instance.saveProject();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//

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