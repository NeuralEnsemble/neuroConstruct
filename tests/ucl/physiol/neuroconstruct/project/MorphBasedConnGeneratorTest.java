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
import java.util.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
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
    
    int numPre = 20;
    int numPost = 20;

    int minPre = 3;
    int maxPre = 10;
    int meanPre = 7;
    int stdPre = 1;

    int maxPost = 9;

    float minLength = 10;
    float maxLength = 50;

    float weightMin = 0.5f;
    float weightMax = 0.8f;
    
    
    @Test
    public void testRandom() throws InterruptedException, CellPackingException 
    {
        System.out.println("---  testRandom()");
        
        Project proj = pm.getCurrentProject();
        
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(2);
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        String tgt = proj.morphNetworkConnectionsInfo.getTargetCellGroup(nc1);
        
        
        SimConfig sc = proj.simConfigInfo.getSimConfig("TwoCG");
        
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPre);
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(tgt)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPost);
        
  
        
        NumberGenerator ngNum = new NumberGenerator(0);
        ngNum.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(ngNum);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(true);
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setMaxNumInitPerFinishCell(maxPost);
        
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMaxLength(maxLength);
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMinLength(minLength);
        
        NumberGenerator ngWeight = new NumberGenerator(0);
        ngWeight.initialiseAsRandomFloatGenerator(weightMax, weightMin);
        proj.morphNetworkConnectionsInfo.getSynapseList(nc1).get(0).setWeightsGenerator(ngWeight);
        
            
        System.out.println("Generating network with random connectivity");
        
        generate(proj, sc);
        
        testTheNet(proj, true, true);
        
        
        
        System.out.println("Generating network with random/close connectivity");
        
        SearchPattern spRandClose = SearchPattern.getRandomCloseSearchPattern(numPost); // use all possible post cells as potential targets
        
        proj.morphNetworkConnectionsInfo.setSearchPattern(nc1, spRandClose);
        
        generate(proj, sc);
        
        testTheNet(proj, true, true);
        
        /* 
        ngNum.initialiseAsFixedIntGenerator(maxPre);
        */
        
        NumberGenerator ngNum2 = new NumberGenerator(0);
        ngNum2.initialiseAsGaussianIntGenerator(maxPre*100, minPre, meanPre, stdPre);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(ngNum2);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(false);
        
        generate(proj, sc);
       
        testTheNet(proj, true, false);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(ngNum);
        
        
        System.out.println("Generating network with closest connectivity");
        
        SearchPattern spClosest = SearchPattern.getClosestSearchPattern();
        
        proj.morphNetworkConnectionsInfo.setSearchPattern(nc1, spClosest);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(true);
        
        generate(proj, sc);
        
        testTheNet(proj, false, true); // max/min length ignored for closest option
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(false);
        
        generate(proj, sc);
        
        testTheNet(proj, false, false); // max/min length ignored for closest option
        
    }
    
    
    private void testTheNet(Project proj, boolean testLength, boolean testUniq)
    {
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(2);
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        String tgt = proj.morphNetworkConnectionsInfo.getTargetCellGroup(nc1);
        
        float speed = proj.morphNetworkConnectionsInfo.getAPSpeed(nc1);
        
        for(int i=0;i<numPre;i++)
        {
            ArrayList<SingleSynapticConnection> conns = proj.generatedNetworkConnections.getConnsFromSource(nc1, i);
            int num = conns.size();
            
            System.out.println("Checking conns for: "+i+", num: "+num);
            
            assertTrue(num<=maxPre);
            assertTrue(num>=minPre);
            
            if(testUniq)
            {
                ArrayList<Integer> uniq = new ArrayList<Integer>();

                for(SingleSynapticConnection ssc: conns)
                {
                    if (!uniq.contains(ssc.targetEndPoint.cellNumber))
                        uniq.add(ssc.targetEndPoint.cellNumber);

                    assertTrue(ssc.props.get(0).weight >= weightMin);
                    assertTrue(ssc.props.get(0).weight <= weightMax);

                }
                assertEquals(uniq.size(), conns.size());
            }
            
            
        }
        
        for(int i=0;i<numPost;i++)
        {
            ArrayList<SingleSynapticConnection> conns = proj.generatedNetworkConnections.getConnsToTarget(nc1, i);
            int num = conns.size();
            assertTrue(num<=maxPost);
        }
        
        if (testLength)
        {
            ArrayList<SingleSynapticConnection> allConns = proj.generatedNetworkConnections.getSynapticConnections(nc1);
            for(SingleSynapticConnection conn: allConns)
            {
                float dist = CellTopologyHelper.getSynapticEndpointsDistance(proj, src, conn.sourceEndPoint, tgt, conn.targetEndPoint, MaxMinLength.RADIAL);

                assertTrue(dist<=maxLength);
                assertTrue(dist>=minLength);
                if (speed<Float.MAX_VALUE)
                {
                    assertTrue(conn.apPropDelay>= minLength/speed);
                    assertTrue(conn.apPropDelay<= maxLength/speed);
                }
            }
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