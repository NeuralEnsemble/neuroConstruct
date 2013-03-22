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
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.SingleSynapticConnection;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class VolumeBasedConnGeneratorTest 
{

    ProjectManager pm = null;
    Random r = new Random();



    int nCseed = 1234;

    int numPreCells = 20;
    int numPostCells = 30;

    int numPreConns1 = 10;
    int maxPostConns1 = 5;

    int numPreConns2 = 10;
    int maxPostConns2 = 5;


    float weightMin = 0.5f;
    float weightMax = 0.8f;
    
    
    public VolumeBasedConnGeneratorTest() 
    {
    }

            

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() VolumeBasedConnGeneratorTest");
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
        pm.doGenerate(sc.getName(), nCseed);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }

        Thread.sleep(800);

        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
        for (String nc: proj.generatedNetworkConnections.getNamesNonEmptyNetConns())
        {
            System.out.println("with "+ proj.generatedNetworkConnections.getSynapticConnections(nc).size()+" conns in "+ nc);
        }
    }
    
    
    @Test
    public void testRandom() throws InterruptedException, CellPackingException 
    {
        System.out.println("---  testRandom()");
        
        Project proj = pm.getCurrentProject();

        String nc1 = proj.volBasedConnsInfo.getConnNameAt(0);
        String nc2 = proj.volBasedConnsInfo.getConnNameAt(1);

        String src = proj.volBasedConnsInfo.getSourceCellGroup(nc1);
        String tgt = proj.volBasedConnsInfo.getTargetCellGroup(nc1);
        

        String simConf = "VolBasedTest";
        SimConfig sc = proj.simConfigInfo.getSimConfig(simConf);
        
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPreCells);
        ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(tgt)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPostCells);
        
  
        
        NumberGenerator ngNum1 = new NumberGenerator(numPreConns1);
        NumberGenerator ngNum2 = new NumberGenerator(numPreConns2);

        proj.volBasedConnsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(ngNum1);
        proj.volBasedConnsInfo.getConnectivityConditions(nc1).setMaxNumInitPerFinishCell(maxPostConns1);

        proj.volBasedConnsInfo.getConnectivityConditions(nc2).setNumConnsInitiatingCellGroup(ngNum2);
        proj.volBasedConnsInfo.getConnectivityConditions(nc2).setMaxNumInitPerFinishCell(maxPostConns2);
        
        
        NumberGenerator ngWeight = new NumberGenerator(0);
        ngWeight.initialiseAsRandomFloatGenerator(weightMax, weightMin);

        proj.volBasedConnsInfo.getSynapseList(nc1).get(0).setWeightsGenerator(ngWeight);
        proj.volBasedConnsInfo.getSynapseList(nc2).get(0).setWeightsGenerator(ngWeight);
        
            
        System.out.println("Generating network with random connectivity");


        proj.volBasedConnsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(false);
        generate(proj, sc);

        testTheNet(proj, false);

        proj.volBasedConnsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(true);
        generate(proj, sc);

        testTheNet(proj, true);
        
        

        
        
    }
    
  
    private void testTheNet(Project proj, boolean testUniq)
    {
        String nc1 = proj.volBasedConnsInfo.getConnNameAt(0);
        String nc2 = proj.volBasedConnsInfo.getConnNameAt(1);
        
        
        for(int i=0;i<numPreCells;i++)
        {
            ArrayList<SingleSynapticConnection> conns1 = proj.generatedNetworkConnections.getConnsFromSource(nc1, i);
            int num1 = conns1.size();
            System.out.println("Checking conns for: "+i+", num: "+num1);
            assertTrue(num1<=numPreConns1);

            ArrayList<SingleSynapticConnection> conns2 = proj.generatedNetworkConnections.getConnsFromSource(nc2, i);
            int num2 = conns2.size();
            System.out.println("Checking conns for: "+i+", num: "+num2);
            assertTrue(num2<=numPreConns2);
            
         
            ArrayList<Integer> uniq = new ArrayList<Integer>();

            for(SingleSynapticConnection ssc: conns1)
            {
                if (!uniq.contains(ssc.targetEndPoint.cellNumber))
                    uniq.add(ssc.targetEndPoint.cellNumber);

                assertTrue(ssc.props.get(0).weight >= weightMin);
                assertTrue(ssc.props.get(0).weight <= weightMax);

                assertEquals(ssc.targetEndPoint.location.getSegmentId(), 4); // due to 3d structure of cell & region pos

            }
            if(testUniq) assertEquals(uniq.size(), conns1.size());

            uniq = new ArrayList<Integer>();

            for(SingleSynapticConnection ssc: conns2)
            {
                if (!uniq.contains(ssc.targetEndPoint.cellNumber))
                    uniq.add(ssc.targetEndPoint.cellNumber);

                assertTrue(ssc.props.get(0).weight >= weightMin);
                assertTrue(ssc.props.get(0).weight <= weightMax);

                assertEquals(ssc.targetEndPoint.location.getSegmentId(), 2); // due to 3d structure of cell & region pos

            }
            if(testUniq) assertEquals(uniq.size(), conns2.size());

            
        }
        
        for(int i=0;i<numPostCells;i++)
        {
            ArrayList<SingleSynapticConnection> conns1 = proj.generatedNetworkConnections.getConnsToTarget(nc1, i);
            int num1 = conns1.size();
            assertTrue(num1<=maxPostConns1);

            ArrayList<SingleSynapticConnection> conns2 = proj.generatedNetworkConnections.getConnsToTarget(nc2, i);
            int num2 = conns2.size();
            assertTrue(num2<=maxPostConns2);
        }
        
        ArrayList<SingleSynapticConnection> cs1 = proj.generatedNetworkConnections.getSynapticConnections(nc1);
        ArrayList<SingleSynapticConnection> cs2 = proj.generatedNetworkConnections.getSynapticConnections(nc2);

        SingleSynapticConnection c1 = cs1.get(cs1.size()-1);
        SingleSynapticConnection c2 = cs2.get(cs2.size()-1);

        System.out.println("Last conn1: "+ c1);
        System.out.println("Last conn2: "+ c2);

        if (nCseed==1234)
        {
            if (!testUniq)
            {
                assertEquals(0.869498, c1.targetEndPoint.location.getFractAlong(), 0.00001);
                assertEquals(16, c1.targetEndPoint.cellNumber);
                assertEquals(0.6452075, c1.props.get(0).weight, 0.00001);

                assertEquals(0.53307045, c2.targetEndPoint.location.getFractAlong(), 0.00001);
                assertEquals(29, c2.targetEndPoint.cellNumber);
                assertEquals(0.62672365, c2.props.get(0).weight, 0.00001);


            }
            else
            {
                assertEquals(0.2927873, c1.targetEndPoint.location.getFractAlong(), 0.00001);
                assertEquals(0, c1.targetEndPoint.cellNumber);
                assertEquals(0.5749074, c1.props.get(0).weight, 0.00001);
                
                assertEquals(0.53307045, c2.targetEndPoint.location.getFractAlong(), 0.00001);
                assertEquals(29, c2.targetEndPoint.cellNumber);
                assertEquals(0.62672365, c2.props.get(0).weight, 0.00001);
                
            }

            System.out.println("Passed regenerate same network tests!");
        }
        
        
    }

    
    
    public static void main(String[] args)
    {
        VolumeBasedConnGeneratorTest ct = new VolumeBasedConnGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
    
    
    
    
    
    
    
    
    
    

}