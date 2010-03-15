#
#
#   A file which opens a neuroConstruct project and generates it, then generates a series
#   of slightly different NEURON simulations, using the same network, but different stimulations.
#   An input current vs firing frequency plot is generated afterwards.
#   The simulations can also be viewed and analysed afterwards in the neuroConstruct GUI
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

from sys import *

try:
	from java.io import File
except ImportError:
	print "Note: this file should be run using ..\\nC.bat -python XXX.py' or './nC.sh -python XXX.py'"
	print "See http://www.neuroconstruct.org/docs/python.html for more details"
	quit()

from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.neuron import NeuronFileManager
from ucl.physiol.neuroconstruct.utils import NumberGenerator
from ucl.physiol.neuroconstruct.nmodleditor.processes import ProcessManager
from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.gui.plotter import PlotManager
from ucl.physiol.neuroconstruct.gui.plotter import PlotCanvas
from ucl.physiol.neuroconstruct.dataset import DataSet
from ucl.physiol.neuroconstruct.simulation import SimulationData
from ucl.physiol.neuroconstruct.simulation import SpikeAnalyser

from math import *
import time

neuroConstructSeed = 1234
simulatorSeed = 4321

# Load an existing neuroConstruct project

projFile = File("TestPython/TestPython.neuro.xml")
print "Loading project from file: " + projFile.getAbsolutePath()+", exists: "+ str(projFile.exists())

pm = ProjectManager()
myProject = pm.loadProject(projFile)

simConfig = myProject.simConfigInfo.getSimConfig("SingleCell")


pm.doGenerate(simConfig.getName(), neuroConstructSeed)

while pm.isGenerating():
    print "Waiting for the project to be generated..."
    time.sleep(2)
    
numGenerated = myProject.generatedCellPositions.getNumberInAllCellGroups()

print "Number of cells generated: " + str(numGenerated)

simsRunning = []



def updateSimsRunning():

    simsFinished = []

    for sim in simsRunning:
        timeFile = File(myProject.getProjectMainDirectory(), "simulations/"+sim+"/time.dat")
        #print "Checking file: "+timeFile.getAbsolutePath() +", exists: "+ str(timeFile.exists())
        if (timeFile.exists()):
            simsFinished.append(sim)

    if(len(simsFinished)>0):
        for sim in simsFinished:
            simsRunning.remove(sim)

            

if numGenerated > 0:

    print "Generating NEURON scripts..."
    
    
    myProject.neuronSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
    myProject.neuronSettings.setNoConsole() # Calling this means no console/terminal is opened when each simulation is run. Sim runs in background & exists on completion
    #myProject.neuronFileManager.setQuitAfterRun(1) # Might be needed if above line was not used
    
    modCompileConfirmation = 0 # 0 means do not pop up console or confirmation dialog when mods have compiled
    

    # Note same network structure will be used for each!
    numSimulationsToRun = 12
    # Change this number to the number of processors you wish to use on your local machine
    maxNumSimultaneousSims = 4
    
    simReferences = {}
    
    for i in range(0, numSimulationsToRun):

        while (len(simsRunning)>=maxNumSimultaneousSims):
            print "Sims currently running: "+str(simsRunning)
            print "Waiting..."
            time.sleep(2) # wait a while...
            updateSimsRunning()

    
        simRef = "PySim_"+str(i)

        print "Going to run simulation: "+simRef
        
        ########  Adjusting the amplitude of the current clamp #######
        
        stim = myProject.elecInputInfo.getStim("Input_0")
        newAmp = i/10.0
        stim.setAmp(NumberGenerator(newAmp))

        simReferences[simRef] = newAmp

        myProject.elecInputInfo.updateStim(stim)
        
        print "Next stim: "+ str(stim)
        
        
        '''
        ######### This code would adjust the density of one of the channels ########

				cell = myProject.cellManager.getCell('SampleCell')

				print "Channels present: "+str(cell.getChanMechsVsGroups())

				dens = i*1e-7

				# Should be put at start...
				from ucl.physiol.neuroconstruct.cell import *

				chanMech = ChannelMechanism("KConductance", dens)

				cell.associateGroupWithChanMech("all", chanMech)
				        
        print "Channels present: "+str(cell.getChanMechsVsGroups())
        
        ############################################################################
        
        '''
        
        myProject.simulationParameters.setReference(simRef)
    
        myProject.neuronFileManager.generateTheNeuronFiles(simConfig, None, NeuronFileManager.RUN_HOC, simulatorSeed)

        print "Generated NEURON files for: "+simRef
    
        compileProcess = ProcessManager(myProject.neuronFileManager.getMainHocFile())
    
        compileSuccess = compileProcess.compileFileWithNeuron(0,modCompileConfirmation)

        print "Compiled NEURON files for: "+simRef
    
        if compileSuccess:
            pm.doRunNeuron(simConfig)
            print "Set running simulation: "+simRef
            simsRunning.append(simRef)
            
        time.sleep(1) # Wait for sim to be kicked off
    
    print
    print "Finished running "+str(numSimulationsToRun)+" simulations for project "+ projFile.getAbsolutePath()
    print "These can be loaded and replayed in the previous simulation browser in the GUI"
    print

    ########  Generating a current versus firing rate plot  #######
    
    plotFrameFI = PlotManager.getPlotterFrame("F-I curve from project: "+str(myProject.getProjectFile()) , 1, 1)

    plotFrameFI.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)

    info = "F-I curve for Simulation Configuration: "+str(simConfig)

    dataSet = DataSet(info, info, "nA", "Hz", "Current injected", "Firing frequency")
    dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)

    simList = simReferences.keys()
    simList.sort()

    for sim in simList:

        simDir = File(projFile.getParentFile(), "/simulations/"+sim)
        print
        print "--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath()
        try:
            simData = SimulationData(simDir)
            simData.initialise()
            print "Data loaded: "
            print simData.getAllLoadedDataStores()
            times = simData.getAllTimes()
            cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"
            volts = simData.getVoltageAtAllTimes(cellSegmentRef)

            print "Got "+str(len(volts))+" data points on cell seg ref: "+cellSegmentRef

            analyseStartTime = 100 # So it's firing at a steady rate...
            analyseStopTime = simConfig.getSimDuration()
            analyseThreshold = -20 # mV

            spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)
            
            stimAmp = simReferences[sim]
            print "Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes))
            avgFreq = 0
            if len(spikeTimes)>1:
                avgFreq = len(spikeTimes)/ ((analyseStopTime - analyseStartTime)/1000.0)
                dataSet.addPoint(stimAmp,avgFreq)
        except:
            print "Error analysing simulation data from: %s"%simDir.getCanonicalPath()
            print exc_info()[0]


    plotFrameFI.addDataSet(dataSet)


