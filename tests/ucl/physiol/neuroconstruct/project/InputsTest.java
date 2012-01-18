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
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessManager;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainExtInstanceProps;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 *
 * @author Matteo
 */
public class InputsTest {
    
ProjectManager pm = null;
    
    public InputsTest() 
    {
    }


    @Before
    public void setUp() 
    {
        
        System.out.println("---------------   setUp() TestInputGeneration");
        String projName = "TestInputGeneration";
        File projDir = new File(MainTest.getTestProjectDirectory()+projName);
        File projFile = ProjectStructure.findProjectFile(projDir);
        
        pm = new ProjectManager();
        
        try {

            pm.loadProject(projFile);
        } catch (ProjectFileParsingException ex) {
            Logger.getLogger(InputsTest.class.getName()).log(Level.SEVERE, null, ex);
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
    public void testAll() throws InterruptedException, NeuronException, IOException, SimulationDataException
    {
        assumeTrue(MainTest.testNEURON());

        setUp();
        
        Project proj = pm.getCurrentProject();


        int wait = 1000;
        if (GeneralUtils.isWindowsBasedPlatform())
            wait = 4000;
        
        //test a simple Random Spike Input with random delay
        
        String rsi = "RSI";
        String sampleCell = "ManyCellsGroup";
        ArrayList<String> addInput = new ArrayList<String>();
        ArrayList<String> addGroup = new ArrayList<String>();
        addInput.add(rsi);
        addGroup.add(sampleCell);
        proj.simConfigInfo.getDefaultSimConfig().setInputs(addInput);
        proj.simConfigInfo.getDefaultSimConfig().setCellGroups(addGroup);        
        
        System.out.println(proj.simConfigInfo.getDefaultSimConfig().toLongString());

        generate();
        
        ArrayList<SingleElectricalInput> inputs = proj.generatedElecInputs.getInputLocations(rsi);

        for (int i = 0; i < inputs.size(); i++) {
            System.out.println("props"+inputs.get(i).getInstanceProps().details(false));
            RandomSpikeTrainExtInstanceProps rste = (RandomSpikeTrainExtInstanceProps) inputs.get(i).getInstanceProps();
            assertTrue((rste.getDelay()>0)&&(rste.getDelay()<1));
        }
        
        
//test the repeat mode in the RSI
        
        String rsiRep = "RSIrep";
        sampleCell = "SampleCellGroup";
        addInput = new ArrayList<String>();
        addGroup = new ArrayList<String>();
        addInput.add(rsiRep);
        addGroup.add(sampleCell);
        proj.simConfigInfo.getDefaultSimConfig().setInputs(addInput);
        proj.simConfigInfo.getDefaultSimConfig().setCellGroups(addGroup);
        System.out.println(proj.simConfigInfo.getDefaultSimConfig().toLongString());
        
        generate();
        
        
         String simName = "TestSim";
         
         SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
        
        proj.simulationParameters.setReference(simName);
        
        File simDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);
        
        if (simDir.exists())
            simDir.delete();
        
        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        File mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        ProcessManager prm = new ProcessManager(mainHoc);
        
        boolean success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files");
        
        SimulationData simData = new SimulationData(simDir, false);
        File timesFile = simData.getTimesFile();

        while (!timesFile.exists())
        {
            Thread.sleep(wait); // Shouldn't take longer than this
        }

        Thread.sleep(wait); // Shouldn't take longer than this
        
        simData.initialise();
        Thread.sleep(wait); // Shouldn't take longer than this
        
        
        assertTrue(timesFile.exists());
        
        
        int numRecordings = simData.getCellSegRefs(false).size();
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());
        
        double[] voltages = simData.getVoltageAtAllTimes(SimulationData.getCellRef(sc.getCellGroups().get(0), 0));
        double[] times = simData.getAllTimes();        
        
        float startTime = (float) times[0];
        float stopTime = (float) times[times.length-1];
      
        double[] events = SpikeAnalyser.getSpikeTimes(voltages, times, -20, startTime, stopTime);
        StimulationSettings stimSet = proj.elecInputInfo.getStim(rsiRep);
        int startPulse = 100;
        int endPulse = 200;        
       
        for (int i = 0; i < events.length; i++) {
            double event = events[i];
            if (event>endPulse){
                startPulse = startPulse + 200;
                endPulse = endPulse + 200;
            }
            assertTrue((event>startPulse)&&(event<endPulse));            
        }
        
//test the IC with variable amplitude
        
        String varClamp = "VariableCurrent";        
        addInput = new ArrayList<String>();
        addInput.add(varClamp);
        proj.simConfigInfo.getDefaultSimConfig().setInputs(addInput);
        System.out.println(proj.simConfigInfo.getDefaultSimConfig().toLongString());

        
        generate();
        
        
         simName = "TestSim";
         
         sc = proj.simConfigInfo.getDefaultSimConfig();
        
        proj.simulationParameters.setReference(simName);
        
        simDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);
        
        if (simDir.exists())
            simDir.delete();
        
        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        prm = new ProcessManager(mainHoc);
        
        success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files");
        
        
        simData = new SimulationData(simDir, false);
        timesFile = simData.getTimesFile();

        while (!timesFile.exists())
        {
            Thread.sleep(wait); // Shouldn't take longer than this
        }

        Thread.sleep(wait); // Shouldn't take longer than this

        simData.initialise();
        Thread.sleep(wait); // Shouldn't take longer than this
        
        
        numRecordings = simData.getCellSegRefs(false).size();
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());
        
        voltages = simData.getVoltageAtAllTimes(SimulationData.getCellRef(sc.getCellGroups().get(0), 0));
        times = simData.getAllTimes();        
        
        startTime = (float) times[0];
        stopTime = (float) times[times.length-1];
      
        events = SpikeAnalyser.getSpikeTimes(voltages, times, -20, startTime, stopTime);
        stimSet = proj.elecInputInfo.getStim(rsiRep);
        int startBin = 190; //the depolarization (form rest potential) begin at 200 but since the cell has been previously hyperpolarized the threshold is lowered
        int endBin = 290;        
       
        for (int i = 0; i < events.length; i++) {
            double event = events[i];
            if (event>endBin){
                startBin = startBin + 200;
                endBin = endBin + 200;
            }
            assertTrue((event>startBin)&&(event<endBin));            
        }
        
//test the RSI with variable frequency
        
        String RSIvar = "VariableSpikeRate";        
        addInput = new ArrayList<String>();
        addInput.add(RSIvar);
        proj.simConfigInfo.getDefaultSimConfig().setInputs(addInput);
        System.out.println(proj.simConfigInfo.getDefaultSimConfig().toLongString());
        
        generate();        
        
         simName = "TestSim";
         
         sc = proj.simConfigInfo.getDefaultSimConfig();
        
        proj.simulationParameters.setReference(simName);
        
        simDir = new File(ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory()), simName);
        
        if (simDir.exists())
            simDir.delete();
        
        proj.neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);
        proj.neuronSettings.setVarTimeStep(false);
        
        proj.neuronFileManager.setQuitAfterRun(true);
        
        
        proj.neuronFileManager.generateTheNeuronFiles(sc, null, NeuronFileManager.RUN_HOC, 1234);
        
        mainHoc = proj.neuronFileManager.getMainHocFile();
        
        
        assertTrue(mainHoc.exists());
        
        System.out.println("Created hoc files, including: "+mainHoc);
        
        
        prm = new ProcessManager(mainHoc);
        
        success = prm.compileFileWithNeuron(true, false);
        
        assertTrue(success);
        
        
        System.out.println("Compiled NEURON files: "+ success);
        
        pm.doRunNeuron(sc);
        
        System.out.println("Run NEURON files");
        
        
        simData = new SimulationData(simDir, false);
        timesFile = simData.getTimesFile();

        while (!timesFile.exists())
        {
            Thread.sleep(wait); // Shouldn't take longer than this
        }

        Thread.sleep(wait); // Shouldn't take longer than this

        simData.initialise();
        Thread.sleep(wait); // Shouldn't take longer than this
        
        
        numRecordings = simData.getCellSegRefs(false).size();
        
        System.out.println("Have found "+ numRecordings+" recordings in dir: "+ simData.getSimulationDirectory().getAbsolutePath());
        
        voltages = simData.getVoltageAtAllTimes(SimulationData.getCellRef(sc.getCellGroups().get(0), 0));
        times = simData.getAllTimes();        
        
        startTime = (float) times[0];
        stopTime = (float) times[times.length-1];
      
        events = SpikeAnalyser.getSpikeTimes(voltages, times, -20, startTime, stopTime);
        stimSet = proj.elecInputInfo.getStim(rsiRep);
        startBin = 290; 
        endBin = 490;
        //best test so far: check that the spikes during the max firing rate period are more than the spikes during the low firing rate period
        int max = 0;
        int min = 0;

       
        for (int i = 0; i < events.length; i++) {
            double event = events[i];
            if (event>endBin){
                min++;
                startBin = startBin + 400;
                endBin = endBin + 400;
            } else {
                max++;
            }          
        }
        
       assertTrue(max>min);            
    }
        
             
    
    public static void main(String[] args)
    {
        InputsTest ct = new InputsTest();
        org.junit.runner.Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);        
    }            
 }

    
