/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import java.util.*;
import org.junit.*;
import org.junit.Test;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.SingleSynapticConnection;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class MorphBasedConnGeneratorTest 
{

    ProjectManager pm = null;
    Random r = new Random();
    
    public MorphBasedConnGeneratorTest() 
    {
    }



    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() MorphBasedConnGeneratorTest");
        String projName = "TestNetworkConns";
        File projDir = new File("testProjects/"+ projName);
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
    
    private void generate(SimConfig sc) throws InterruptedException 
    {
        Project proj = pm.getCurrentProject();
                
        pm.doGenerate(sc.getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
    }
    


    private void generate() throws InterruptedException 
    {       
        Project proj = pm.getCurrentProject();
        
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        generate(sc);
    }
    
    
    

    /**
     * Test of run method, of class MorphBasedConnGenerator.
     */
    @Test
    public void testAustapses() throws InterruptedException {
        
        
        System.out.println("---  testAustapses()");
        
        Project proj = pm.getCurrentProject();
        
        generate();        
        
        System.out.println("Testing autapses...");
        
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(0);
        
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        
        int numInSrc = proj.generatedCellPositions.getNumberInCellGroup(src);
        
        ConnectivityConditions cc = proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1);
        
        cc.setAllowAutapses(false);
        
        cc.setOnlyConnectToUniqueCells(true);
        
        cc.setNumConnsInitiatingCellGroup(new NumberGenerator(numInSrc-1));
        
        generate(); 
        
        int somePre = r.nextInt(numInSrc-1);
        
        assertFalse(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
        cc.setAllowAutapses(true);
        
        cc.setNumConnsInitiatingCellGroup(new NumberGenerator(numInSrc));
        
        generate(); 
               
        assertTrue(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
        
    }
    
    
    
    @Test
    public void testPrePostSynLocs() throws InterruptedException, CellPackingException {
        
        
        System.out.println("---  testPrePostSynLocs()");
        
        Project proj = pm.getCurrentProject();
        
        String nc2 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(1);
        
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc2);
        
        Cell cell = proj.cellManager.getCell(proj.cellGroupsInfo.getCellType(src));
        
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, 11);
        
        generate(proj.simConfigInfo.getSimConfig("TestPrePost"));        
        
        
        int numInSrc = proj.generatedCellPositions.getNumberInCellGroup(src);
        
        ConnectivityConditions cc = proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc2);
        
        PrePostAllowedLocs pp = new PrePostAllowedLocs();
        pp.setSomaAllowedPre(false);
        pp.setAxonsAllowedPre(false);
        pp.setDendritesAllowedPre(true);
        
        pp.setSomaAllowedPost(true);
        pp.setAxonsAllowedPost(false);
        pp.setDendritesAllowedPost(false);
        
        cc.setPrePostAllowedLoc(pp);
        
        //proj.morphNetworkConnectionsInfo.setConnectivityConditions(nc2, cc)
        
        
        generate(proj.simConfigInfo.getSimConfig("TestPrePost"));
        
        assertTrue(proj.generatedNetworkConnections.getSynapticConnections(nc2).size()>0);
        
        //System.out.println("Conns: "+ proj.generatedNetworkConnections.toNiceString());
        
        for(SingleSynapticConnection conn: proj.generatedNetworkConnections.getSynapticConnections(nc2))
        {
            assertEquals(conn.sourceEndPoint.location.getSegmentId(), cell.getOnlyDendriticSegments().firstElement().getSegmentId());
            assertEquals(conn.targetEndPoint.location.getSegmentId(), cell.getOnlySomaSegments().firstElement().getSegmentId());
        }
        
        
               
        //assertTrue(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    

}