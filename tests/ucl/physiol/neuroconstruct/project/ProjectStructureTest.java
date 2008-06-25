/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class ProjectStructureTest {

    public ProjectStructureTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception 
    {
        System.out.println("ProjectStructure setUpClass......done");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        System.out.println("ProjectStructure setUp......");
    }

    @After
    public void tearDown() {
    }
    
    
    @Test
    public void testSomeDirs() {
        System.out.println("testSomeDirs");
        File cmDir = ProjectStructure.getCMLExamplesDir();
        assertTrue(cmDir.exists());
        
    }

}