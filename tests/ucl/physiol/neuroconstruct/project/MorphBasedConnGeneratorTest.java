/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import java.util.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
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
    
    private void generate(Project proj, SimConfig sc) throws InterruptedException 
    {                
        pm.doGenerate(sc.getName(), 1234);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
    }
    


    private void generate(Project proj) throws InterruptedException 
    {       
        
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        generate(proj, sc);
    }
    
    
    @Test
    public void testRandom() throws InterruptedException, CellPackingException 
    {
        System.out.println("---  testRandom()");
        
        
        Project proj = pm.getCurrentProject();
        
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(2);
        
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        
        String tgt = proj.morphNetworkConnectionsInfo.getTargetCellGroup(nc1);
        
        int numPre = 20;
        int numPost = 20;
        
        SimConfig sc = proj.simConfigInfo.getSimConfig("TwoCG");
        
        
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPre);
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(tgt)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPost);
        
        int minPre = 3;
        int maxPre = 10;
        int meanPre = 7;
        int stdPre = 1;
        
        int maxPost = 9;
        
        float minLength = 10;
        float maxLength = 50;
        
        NumberGenerator nb = new NumberGenerator();
        nb.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(nb);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(true);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setMaxNumInitPerFinishCell(maxPost);
        
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMaxLength(maxLength);
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMinLength(minLength);
        
        generate(proj, sc);
        
        for(int i=0;i<numPre;i++)
        {
            System.out.println("Checking conns for: "+ src+", "+i);
            ArrayList<SingleSynapticConnection> conns = proj.generatedNetworkConnections.getConnsFromSource(nc1, i);
            //System.out.println(conns);
            int num = conns.size();
            assertTrue(num<=maxPre);
            assertTrue(num>=minPre);
            ArrayList<Integer> uniq = new ArrayList<Integer>();
            
            for(SingleSynapticConnection ssc: conns)
            {
                if (!uniq.contains(ssc.targetEndPoint.cellNumber))
                    uniq.add(ssc.targetEndPoint.cellNumber);
            }
            assertEquals(uniq.size(), conns.size());
        }
        
        for(int i=0;i<numPost;i++)
        {
            //System.out.println("Checking conns for: "+ tgt+", "+i);
            ArrayList<SingleSynapticConnection> conns = proj.generatedNetworkConnections.getConnsToTarget(nc1, i);
            //System.out.println(conns.size()+": "+ conns);
            int num = conns.size();
            assertTrue(num<=maxPost);
        }
        
        ArrayList<SingleSynapticConnection> allConns = proj.generatedNetworkConnections.getSynapticConnections(nc1);
        for(SingleSynapticConnection conn: allConns)
        {
            float dist = CellTopologyHelper.getSynapticEndpointsDistance(proj, src, conn.sourceEndPoint, tgt, conn.targetEndPoint, MaxMinLength.RADIAL);
            
            assertTrue(dist<=maxLength);
            assertTrue(dist>=minLength);
        }
        
        
    }
    

    /**
     * Test of run method, of class MorphBasedConnGenerator.
     */
    @Test
    public void testAustapses() throws InterruptedException 
    {
        
        System.out.println("---  testAustapses()");
        
        Project proj = pm.getCurrentProject();
        
        generate(proj);        
        
        System.out.println("Testing autapses...");
       
        
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(0);
        
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        
        int numInSrc = proj.generatedCellPositions.getNumberInCellGroup(src);
        
        ConnectivityConditions cc = proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1);
        
        cc.setAllowAutapses(false);
        
        cc.setOnlyConnectToUniqueCells(true);
        
        cc.setNumConnsInitiatingCellGroup(new NumberGenerator(numInSrc-1));
        
        generate(proj); 
        
        int somePre = r.nextInt(numInSrc-1);
        
        assertFalse(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
        cc.setAllowAutapses(true);
        
        cc.setNumConnsInitiatingCellGroup(new NumberGenerator(numInSrc));
        
        generate(proj); 
               
        assertTrue(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
    }
    
   
    
    @Test
    public void testPrePostSynLocs() throws InterruptedException, CellPackingException
    {
        System.out.println("---  testPrePostSynLocs()");
        
        Project proj = pm.getCurrentProject();
        
        String nc2 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(1);
        
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc2);
        
        Cell cell = proj.cellManager.getCell(proj.cellGroupsInfo.getCellType(src));
        
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, 11);
        
        generate(proj, proj.simConfigInfo.getSimConfig("TestPrePost"));        
        
        ConnectivityConditions cc = proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc2);
        
        PrePostAllowedLocs pp = new PrePostAllowedLocs();
        pp.setSomaAllowedPre(false);
        pp.setAxonsAllowedPre(false);
        pp.setDendritesAllowedPre(true);
        
        pp.setSomaAllowedPost(true); 
        pp.setAxonsAllowedPost(false);
        pp.setDendritesAllowedPost(false);
        
        cc.setPrePostAllowedLoc(pp);        
        
        generate(proj, proj.simConfigInfo.getSimConfig("TestPrePost"));
        
        assertTrue(proj.generatedNetworkConnections.getSynapticConnections(nc2).size()>0);
        
        //System.out.println("Conns: "+ proj.generatedNetworkConnections.toNiceString());
        
        for(SingleSynapticConnection conn: proj.generatedNetworkConnections.getSynapticConnections(nc2))
        {
            assertEquals(conn.sourceEndPoint.location.getSegmentId(), cell.getOnlyDendriticSegments().firstElement().getSegmentId());
            assertEquals(conn.targetEndPoint.location.getSegmentId(), cell.getOnlySomaSegments().firstElement().getSegmentId());
        }
        
        
               
        //assertTrue(proj.generatedNetworkConnections.areConnected(nc1, somePre, somePre));
        
        
        
    }
    
    
    
    public static void main(String[] args)
    {
        MorphBasedConnGeneratorTest ct = new MorphBasedConnGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
    
    
    
    
    
    
    
    
    
    

}