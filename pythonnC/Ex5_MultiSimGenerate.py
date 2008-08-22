#
#
#   A file which opens a neuroConstruct project and generates it, then generates a series
#   of slightly different NEURON simulations, using the same network, but different stimulations
#   The simulations can be viewed and analysed afterwards in the neuroConstruct GUI
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

from sys import *

from java.io import File
from java.lang import System

from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.neuron import NeuronFileManager
from ucl.physiol.neuroconstruct.utils import NumberGenerator
from ucl.physiol.neuroconstruct.nmodleditor.processes import ProcessManager

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


if numGenerated > 0:

    print "Generating NEURON scripts..."
    
    myProject.neuronFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
    
    myProject.neuronSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
    
    numToRun = 6
    
    for i in range(0, numToRun):
    
        simRef = "PySim_"+str(i)
        
        stim = myProject.elecInputInfo.getStim("Input_0")
        
        newAmp = i/10.0
        
        stim.setAmp(NumberGenerator(newAmp))
        
        myProject.elecInputInfo.updateStim(stim)
        
        print "Next stim: "+ str(stim)
        
        myProject.simulationParameters.setReference(simRef)
    
        myProject.neuronFileManager.generateTheNeuronFiles(simConfig, None, NeuronFileManager.RUN_HOC, simulatorSeed)
    
        compileProcess = ProcessManager(myProject.neuronFileManager.getMainHocFile())
    
        compileSuccess = compileProcess.compileFileWithNeuron(0)
    
        if compileSuccess:
            pm.doRunNeuron(simConfig)
            
            time.sleep(2) # wait a while between simulations...
            
            print "Finished running simulation: "+simRef
    
    print
    print "Finished running "+str(numToRun)+" simulations for project "+ projFile.getAbsolutePath()
    print "These can be loaded and replayed in the previous simulation browser in the GUI"
    print

System.exit(0)