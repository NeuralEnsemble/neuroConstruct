/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.project;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import test.MainTest;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainExtInstanceProps;
import static org.junit.Assert.*;

/**
 *
 * @author Matteo
 */
public class RandomSpikeTrainTest {
    
ProjectManager pm = null;
    
    public RandomSpikeTrainTest() 
    {
    }


    @Before
    public void setUp() 
    {
        
        System.out.println("---------------   setUp() RandomSpikeTrainTest");
        String projName = "RandomSpikeTrainTest";
        File projDir = new File("testProjects/"+projName);
        File projFile = new File(projDir, projName+ProjectStructure.getProjectFileExtension());
        
        pm = new ProjectManager();
        
        try {

            pm.loadProject(projFile);
        } catch (ProjectFileParsingException ex) {
            Logger.getLogger(RandomSpikeTrainTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());
           
    }
    
    private void generate() throws InterruptedException 
    {       
        Project proj = pm.getCurrentProject();
        
        SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        generate(sc);
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
    
    @Test
    public void testAll() throws InterruptedException
    {
        setUp();
        
        generate();
        
        Project proj = pm.getCurrentProject();
        
        String rsi = "RSI";
        
        ArrayList<SingleElectricalInput> inputs = proj.generatedElecInputs.myElecInputs.get(rsi);
        
//        float max = proj.elecInputInfo.getStim(rsi).;
        
        for (int i = 0; i < inputs.size(); i++) {
            System.out.println("props"+inputs.get(i).getInstanceProps().details(false));
            RandomSpikeTrainExtInstanceProps rste = (RandomSpikeTrainExtInstanceProps) inputs.get(i).getInstanceProps();
            assertTrue((rste.delay>0)&&(rste.delay<1));
        }
            
    }
        
             
    
    public static void main(String[] args)
    {
        RandomSpikeTrainTest ct = new RandomSpikeTrainTest();
        org.junit.runner.Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
    
    
        

 }

    
