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
import javax.vecmath.Point3f;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.SingleSynapticConnection;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.stimulation.IClampInstanceProps;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.WeightGenerator;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

/**
 *
 * @author Matteo Farinella
 */

public class ExtendedNetworkGeneratorTest 
{
    ProjectManager pm = null;
    Random r = new Random();
    
    public ExtendedNetworkGeneratorTest() 
    {
    }

            

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() ExtendedNetworkGeneretorTest");
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
    
    private void generate(Project proj, SimConfig sc, int randSeed) throws InterruptedException
    {                
        pm.doGenerate(sc.getName(), randSeed);
        
        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        
        System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells, "
            + proj.generatedNetworkConnections.getNumAllSynConns()+" net conns");
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

    /*
     * test that a set of previously calculated values for positions, connection
     * weights, etc. are reproduced if the same random seed is used
     */
    @Test
    public void testReproducability() throws InterruptedException
    {
        System.out.println("---  test testReproducability()");

        Project proj = pm.getCurrentProject();

        SimConfig sc = proj.simConfigInfo.getSimConfig("TwoCG");

        String cg1 = sc.getCellGroups().get(0);
        String cg2 = sc.getCellGroups().get(1);

        String nc1 = sc.getNetConns().get(0);
        String input1 = sc.getInputs().get(0);



        generate(proj, sc, 12345);

        ArrayList<PositionRecord> posRecs = proj.generatedCellPositions.getPositionRecords(cg2);

        Point3f aPoint = posRecs.get(19).getPoint();
        System.out.println(aPoint);

        // this was obtained by running the project through the GUI with seed = 12345
        Point3f aPointExpected = new Point3f(58.340458f, 12.1995125f, 51.59832f);

        assertEquals(aPointExpected, aPoint);


        SingleSynapticConnection synConn = proj.generatedNetworkConnections.getSynapticConnections(nc1).get(132);


        // these values were obtained by running the project through the GUI with seed = 12345
        assertEquals(synConn.sourceEndPoint.cellNumber, 19);
        assertEquals(synConn.targetEndPoint.cellNumber, 0);
        assertEquals(synConn.props.get(0).weight, 0.97659814, 0.00001);

        SingleElectricalInput inp = proj.generatedElecInputs.getInputLocations(input1).get(19);

        // this was obtained by running the project through the GUI with seed = 12345
        assertEquals(((IClampInstanceProps)inp.getInstanceProps()).getAmplitude(), 0.72577375, 0.00001);


        System.out.println("All good!");

    }
    
    
    /* test the option "soma to soma" distance for the length of the connections
     */
    @Test
    public void testSomaToSomaDistance() throws InterruptedException, CellPackingException, ProjectFileParsingException, EquationException
    {        
        System.out.println("---  test SomaToSoma distance()");       
        
        File f = new File("testProjects/TestNetworkConnsSomas/TestNetworkConnsSomas.neuro.xml");
        Project proj2 = pm.loadProject(f);
        //Project proj2 = pm.getCurrentProject();
        
        SimConfig sc = proj2.simConfigInfo.getSimConfig("TwoCG");
        
        String nc2 = proj2.morphNetworkConnectionsInfo.getNetConnNameAt(2);  
        String src = proj2.morphNetworkConnectionsInfo.getSourceCellGroup(nc2);
        String tgt = proj2.morphNetworkConnectionsInfo.getTargetCellGroup(nc2);
        
        
        ((RandomCellPackingAdapter)proj2.cellGroupsInfo.getCellPackingAdapter(src)).setMaxNumberCells(20);
        ((RandomCellPackingAdapter)proj2.cellGroupsInfo.getCellPackingAdapter(tgt)).setMaxNumberCells(20);
              
        proj2.morphNetworkConnectionsInfo.getMaxMinLength(nc2).setDimension("s");        
        proj2.morphNetworkConnectionsInfo.getConnectivityConditions(nc2).setMaxNumInitPerFinishCell(100);    
        
        String exprForWeight = "r*r";
        //String exprForWeight = "r";
        
        
        Variable[] vars = new Variable[]{new Variable("r")};
            
        EquationUnit eqn = Expression.parseExpression(exprForWeight, vars);
            
        boolean somaToSoma = Math.random()>0.5; // one or the other...
        
        WeightGenerator wg = new WeightGenerator(exprForWeight, somaToSoma);
        
        System.out.println("wg: "+wg);
        
        proj2.morphNetworkConnectionsInfo.getSynapseList(nc2).get(0).setWeightsGenerator(wg);
        
        System.out.println("Syns: "+proj2.morphNetworkConnectionsInfo.getSynapseList(nc2));
        
        NumberGenerator ngNum = new NumberGenerator(0);
        ngNum.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj2.morphNetworkConnectionsInfo.getConnectivityConditions(nc2).setNumConnsInitiatingCellGroup(ngNum);       
        
        maxLength = 100;
        minLength = 5;
        
        proj2.morphNetworkConnectionsInfo.getMaxMinLength(nc2).setMaxLength(maxLength);
        proj2.morphNetworkConnectionsInfo.getMaxMinLength(nc2).setMinLength(minLength);
        
        generate(proj2, sc, 1234);
        
        Cell sourceCellInstance = proj2.cellManager.getCell(proj2.cellGroupsInfo.getCellType(src));
        Cell targetCellInstance = proj2.cellManager.getCell(proj2.cellGroupsInfo.getCellType(tgt));
        
        Section sourceSec = sourceCellInstance.getFirstSomaSegment().getSection();
        Section targetSec = targetCellInstance.getFirstSomaSegment().getSection();

        Point3f sourceSomaPosition = CellTopologyHelper.convertSectionDisplacement(sourceCellInstance, sourceSec, (float) 0.5);
        Point3f targetSomaPosition = CellTopologyHelper.convertSectionDisplacement(targetCellInstance, targetSec, (float) 0.5);
        
        
        ArrayList<SingleSynapticConnection> synConn= proj2.generatedNetworkConnections.getSynapticConnections(nc2);
        
        for (int i = 0; i < synConn.size(); i++)
        {
            //System.out.println("-------------------------");
            
            Point3f startCell = proj2.generatedCellPositions.getOneCellPosition(src,synConn.get(i).sourceEndPoint.cellNumber);
            Point3f endCell = proj2.generatedCellPositions.getOneCellPosition(tgt,synConn.get(i).targetEndPoint.cellNumber);

            startCell.add(sourceSomaPosition);
            endCell.add(targetSomaPosition);
            
            float somaToSomaDist = startCell.distance(endCell);

            //System.out.println(i + ": connection length soma to soma = " + somaToSomaDist);
            assertTrue((somaToSomaDist>=minLength)&&somaToSomaDist<=maxLength);   
            
            if (synConn.get(i).props!=null && synConn.get(i).props.size()>0)
            {
                float weight = synConn.get(i).props.get(0).weight;

                if(somaToSoma)
                {
                    //System.out.println();
                    
                    Argument[] args = new Argument[]{new Argument("r", somaToSomaDist)};
                    
                    double test = eqn.evaluateAt(args);
                    System.out.println("Weight: "+weight+", dist: "+somaToSomaDist+", test: "+test);
                        
                    assertEquals(weight, test, weight*1e-6);
                }
                else
                {
                    
                    //System.out.println("Weight: "+weight+" in "+ synConn.get(i));
                    
                    Point3f synPre = CellTopologyHelper.getAbsolutePosSegLoc(proj2, 
                                                             src, 
                                                             synConn.get(i).sourceEndPoint.cellNumber, 
                                                             synConn.get(i).sourceEndPoint.location);
                    
                    Point3f synPost = CellTopologyHelper.getAbsolutePosSegLoc(proj2, 
                                                             tgt, 
                                                             synConn.get(i).targetEndPoint.cellNumber, 
                                                             synConn.get(i).targetEndPoint.location);
                    
                    float dist = synPre.distance(synPost);
                    
                    Argument[] args = new Argument[]{new Argument("r", dist)};
                    
                    double test = eqn.evaluateAt(args);
                    System.out.println("Weight: "+weight+", dist: "+dist+", test: "+test);
                        
                    assertEquals(weight, test, weight*1e-6);
                }
            }

        }
        
        System.out.println("All the " + synConn.size() + " connections follow the soma to soma distance costrains" );

    }
    
    
    
    /* test the option "soma to soma" distance for the length of the connections
     */
    @Test
    public void testSomaToSomaDistanceAA() throws InterruptedException, CellPackingException, ProjectFileParsingException, EquationException
    {        
        System.out.println("---  testSomaToSomaDistanceAA()");       
        
        File f = new File("testProjects/TestNetworkConnsSomas/TestNetworkConnsSomas.neuro.xml");
        Project proj2 = pm.loadProject(f);
        //Project proj2 = pm.getCurrentProject();
        
        SimConfig sc = proj2.simConfigInfo.getSimConfig("AATest");
        
        String nc2 = proj2.volBasedConnsInfo.getAllAAConnNames().get(0);  
        String src = proj2.volBasedConnsInfo.getSourceCellGroup(nc2);
        String tgt = proj2.volBasedConnsInfo.getTargetCellGroup(nc2);
        
        
        ((RandomCellPackingAdapter)proj2.cellGroupsInfo.getCellPackingAdapter(src)).setMaxNumberCells(20);
                 
        proj2.volBasedConnsInfo.getConnectivityConditions(nc2).setMaxNumInitPerFinishCell(100);    
        
        String exprForWeight = "r*r";
        //String exprForWeight = "r";
        
        
        Variable[] vars = new Variable[]{new Variable("r")};
            
        EquationUnit eqn = Expression.parseExpression(exprForWeight, vars);
            
        boolean somaToSoma = Math.random()>0.5; // one or the other...
        
        WeightGenerator wg = new WeightGenerator(exprForWeight, somaToSoma);
        
        System.out.println("wg: "+wg);
        
        proj2.volBasedConnsInfo.getSynapseList(nc2).get(0).setWeightsGenerator(wg);
        
        System.out.println("Syns: "+proj2.volBasedConnsInfo.getSynapseList(nc2));
        
        NumberGenerator ngNum = new NumberGenerator(0);
        ngNum.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj2.volBasedConnsInfo.getConnectivityConditions(nc2).setNumConnsInitiatingCellGroup(ngNum);       
        
        
        generate(proj2, sc, 1234);
        
        Cell sourceCellInstance = proj2.cellManager.getCell(proj2.cellGroupsInfo.getCellType(src));
        Cell targetCellInstance = proj2.cellManager.getCell(proj2.cellGroupsInfo.getCellType(tgt));
        
        Section sourceSec = sourceCellInstance.getFirstSomaSegment().getSection();
        Section targetSec = targetCellInstance.getFirstSomaSegment().getSection();

        Point3f sourceSomaPosition = CellTopologyHelper.convertSectionDisplacement(sourceCellInstance, sourceSec, (float) 0.5);
        Point3f targetSomaPosition = CellTopologyHelper.convertSectionDisplacement(targetCellInstance, targetSec, (float) 0.5);
        
        
        ArrayList<SingleSynapticConnection> synConn= proj2.generatedNetworkConnections.getSynapticConnections(nc2);
        
        for (int i = 0; i < synConn.size(); i++)
        {
            //System.out.println("-------------------------");
            
            Point3f startCell = proj2.generatedCellPositions.getOneCellPosition(src,synConn.get(i).sourceEndPoint.cellNumber);
            Point3f endCell = proj2.generatedCellPositions.getOneCellPosition(tgt,synConn.get(i).targetEndPoint.cellNumber);

            startCell.add(sourceSomaPosition);
            endCell.add(targetSomaPosition);
            
            float somaToSomaDist = startCell.distance(endCell);
            
            if (synConn.get(i).props!=null && synConn.get(i).props.size()>0)
            {
                float weight = synConn.get(i).props.get(0).weight;

                if(somaToSoma)
                {
                    //System.out.println();
                    
                    Argument[] args = new Argument[]{new Argument("r", somaToSomaDist)};
                    
                    double test = eqn.evaluateAt(args);
                    System.out.println("Weight: "+weight+", somaToSomaDist: "+somaToSomaDist+", test: "+test);
                        
                    assertEquals(weight, test, weight*1e-6);
                }
                else
                {
                    
                    //System.out.println("Weight: "+weight+" in "+ synConn.get(i));
                    
                    Point3f synPre = CellTopologyHelper.getAbsolutePosSegLoc(proj2, 
                                                             src, 
                                                             synConn.get(i).sourceEndPoint.cellNumber, 
                                                             synConn.get(i).sourceEndPoint.location);
                    
                    Point3f synPost = CellTopologyHelper.getAbsolutePosSegLoc(proj2, 
                                                             tgt, 
                                                             synConn.get(i).targetEndPoint.cellNumber, 
                                                             synConn.get(i).targetEndPoint.location);
                    
                    float dist = synPre.distance(synPost);
                    
                    Argument[] args = new Argument[]{new Argument("r", dist)};
                    
                    double test = eqn.evaluateAt(args);
                    System.out.println("Weight: "+weight+", dist: "+dist+", test: "+test);
                        
                    assertEquals(weight, test, weight*1e-6);
                }
            }

        }
        
        System.out.println("All the " + synConn.size() + " connections follow the soma to soma distance costrains" );

    }
    
    
    
    
    
    /* test the option x dimention option for the length of the connections (valid also for y and z dimensions)
     */
    @Test
    public void testXDimension() throws ProjectFileParsingException, InterruptedException
    {        
        System.out.println("---  test X dimension()");       
        
//        File f = new File("C:\\neuroConstruct\\testProjects\\TestNetworkConnsSomas\\TestNetworkConns.neuro.xml");
//        Project proj2 = pm.loadProject(f);
        
        Project proj2 = pm.getCurrentProject();
        
        SimConfig sc = proj2.simConfigInfo.getSimConfig("TwoCG");
        
        String nc2 = proj2.morphNetworkConnectionsInfo.getNetConnNameAt(2);  
        String src = proj2.morphNetworkConnectionsInfo.getSourceCellGroup(nc2);
        String trg = proj2.morphNetworkConnectionsInfo.getTargetCellGroup(nc2);
              
        proj2.morphNetworkConnectionsInfo.getMaxMinLength(nc2).setDimension("x");        
        proj2.morphNetworkConnectionsInfo.getConnectivityConditions(nc2).setMaxNumInitPerFinishCell(100);        
        
        NumberGenerator ngNum = new NumberGenerator(0);
        ngNum.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj2.morphNetworkConnectionsInfo.getConnectivityConditions(nc2).setNumConnsInitiatingCellGroup(ngNum);       
        
        maxLength = 50;
        minLength = 10;
        
        MaxMinLength maxMin = proj2.morphNetworkConnectionsInfo.getMaxMinLength(nc2);
        
        maxMin.setMaxLength(maxLength);
        maxMin.setMinLength(minLength);
        
        generate(proj2, sc, 1234);
        
        ArrayList<SingleSynapticConnection> synConn= proj2.generatedNetworkConnections.getSynapticConnections(nc2);
        
        for (int i = 0; i < synConn.size(); i++)
        {
        
            float distanceApart = CellTopologyHelper.getSynapticEndpointsDistance(proj2,
                                                                                    src,
                                                                                    synConn.get(i).sourceEndPoint,
                                                                                    trg,
                                                                                    synConn.get(i).targetEndPoint,
                                                                                    maxMin.getDimension());
        
            assertTrue((distanceApart>=minLength)&&(distanceApart<=maxLength));        
        
        }
        
        System.out.println("All the " + synConn.size() + " connections follow the x dimension distance costrains" );
    }
  
    
    /* test the option in wich no recurrent connections between two cells are allowed (GAP junctions)
     */
    @Test
    public void testGAPjunctionNetwork() throws InterruptedException
    {
        System.out.println("---  test non recurrent connections (GAP junctions condition)");
        
        Project proj = pm.getCurrentProject();
        
        SimConfig sc = proj.simConfigInfo.getSimConfig("TwoCG");
        
        String nc1 = proj.morphNetworkConnectionsInfo.getNetConnNameAt(2);
        String src = proj.morphNetworkConnectionsInfo.getSourceCellGroup(nc1);
        proj.morphNetworkConnectionsInfo.setTargetCellGroup(nc1, src); //source = target
        
        NumberGenerator ngNum = new NumberGenerator(0);
        ngNum.initialiseAsGaussianIntGenerator(maxPre, minPre, meanPre, stdPre);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNumConnsInitiatingCellGroup(ngNum);
        
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setOnlyConnectToUniqueCells(true);
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setNoRecurrent(true);
        proj.morphNetworkConnectionsInfo.getConnectivityConditions(nc1).setMaxNumInitPerFinishCell(100);
        
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMaxLength(500);
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMinLength(0);
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setDimension("s");
        
        // test the case of a COMPLETE_RANDOM search pattern
        proj.morphNetworkConnectionsInfo.setSearchPattern(nc1, SearchPattern.getRandomSearchPattern());
       
        generate(proj, sc, 1234);
              
        int [][] mat = proj.generatedNetworkConnections.getConnectionMatrix(nc1, proj);
        for (int i = 0; i < mat.length; i++)
        {
            for (int j = 0; j < mat.length; j++)
            {
                assertTrue(mat[i][j]+mat[j][i]<=1); // max one single synapse between two cells (in both directions)
            }
        }
        
        System.out.println("All the " + proj.generatedNetworkConnections.getSynapticConnections(nc1).size() + " connections are not recurrent connections (RANDOM pattern)");
        
        // test the case of a RANDOM_BUT_CLOSE search pattern
        proj.morphNetworkConnectionsInfo.setSearchPattern(nc1, SearchPattern.getRandomCloseSearchPattern(10));
        
        generate(proj, sc, 1234);
              
        int [][] mat1 = proj.generatedNetworkConnections.getConnectionMatrix(nc1, proj);
        for (int i = 0; i < mat1.length; i++)
        {
            for (int j = 0; j < mat1.length; j++)
            {
                assertTrue(mat1[i][j]+mat1[j][i]<=1); // max one single synapse between two cells (in both directions)
            }
        }

        System.out.println("All the " + proj.generatedNetworkConnections.getSynapticConnections(nc1).size() + " connections are not recurrent connections (RANDOM but CLOSE pattern)");
        
         // test the case of a CLOSEST search pattern
        proj.morphNetworkConnectionsInfo.setSearchPattern(nc1, SearchPattern.getClosestSearchPattern());
        
        // the CLOSEST case is not compatible with the MAX MIN costrains
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMaxLength(maxLength);
        proj.morphNetworkConnectionsInfo.getMaxMinLength(nc1).setMinLength(0);
        
        generate(proj, sc, 1234);
              
        int [][] mat2 = proj.generatedNetworkConnections.getConnectionMatrix(nc1, proj);
        for (int i = 0; i < mat2.length; i++)
        {
            for (int j = 0; j < mat2.length; j++)
            {
                assertTrue(mat2[i][j]+mat2[j][i]<=1); // max one single synapse between two cells (in both directions)
            }
        }
        
        System.out.println("All the " + proj.generatedNetworkConnections.getSynapticConnections(nc1).size() + " connections are not recurrent connections (CLOSEST pattern)");

    
    }
    
    public static void main(String[] args)
    {
        ExtendedNetworkGeneratorTest ct = new ExtendedNetworkGeneratorTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
    }
}
