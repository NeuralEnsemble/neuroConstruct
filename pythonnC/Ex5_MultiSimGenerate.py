#
#
#   A file which opens a neuroConstruct project and generates it, then generates a series
#   of slightly different NEURON simulations, using the same network, but different stimulations
#   The simulations can be viewed and analysed afterwards in the neuroConstruct GUI
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
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
    
    myProject.neuronFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
    myProject.neuronSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
    

    # Note same network structure will be used for each!
    numSimulationsToRun = 12
    # Change this number to the number of processors you wish to use on your local machine
    maxNumSimultaneousSims = 4
    
    for i in range(0, numSimulationsToRun):

        while (len(simsRunning)>=maxNumSimultaneousSims):
            print "Sims currently running: "+str(simsRunning)
            print "Waiting..."
            time.sleep(2) # wait a while...
            updateSimsRunning()

    
        simRef = "PySim_"+str(i)

        print "Going to run simulation: "+simRef
        
        ########  Adjusting the amplitude of the current clamp ###############
        
        stim = myProject.elecInputInfo.getStim("Input_0")
        newAmp = i/10.0
        stim.setAmp(NumberGenerator(newAmp))
        myProject.elecInputInfo.updateStim(stim)
        
        print "Next stim: "+ str(stim)
        
        #######################################################################
        
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
    
        compileSuccess = compileProcess.compileFileWithNeuron(0)

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

System.exit(0)




