#
#
#   File to preform standard tasks on a neuroConstruct project
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

import sys
import time

from java.io import File


from ucl.physiol.neuroconstruct.neuron import NeuronFileManager

from ucl.physiol.neuroconstruct.nmodleditor.processes import ProcessManager
from ucl.physiol.neuroconstruct.cell.compartmentalisation import GenesisCompartmentalisation
from ucl.physiol.neuroconstruct.utils.units import UnitConverter

from ucl.physiol.neuroconstruct.simulation import SimulationData
from ucl.physiol.neuroconstruct.gui.plotter import PlotManager
from ucl.physiol.neuroconstruct.project import SimPlot
from ucl.physiol.neuroconstruct.cell.utils import CellTopologyHelper





def getUnusedSimRef(project, simRefPrefix="P_Sim_"):   
     
    index = 0
    
    while File( "%s/simulations/%s%i"%(project.getProjectMainDirectory().getCanonicalPath(), simRefPrefix,index)).exists():
        index = index+1
    
    simRef = "%s%i"%(simRefPrefix,index)
    
    return simRef


def generateAndRunGenesis(project,
                         projectManager,
                         simConfig,
                         simRef,
                         simulatorSeed,
                         verbose=True,
                         quitAfterRun=False,
                         runInBackground=False,
                         units=-1,
                         symmetricComps=False):

    prefix = "--- GENESIS gen:  "

    if verbose: print prefix+"Going to generate GENESIS files for: "+simRef

    if runInBackground:
        project.genesisSettings.setNoConsole()

    if units == UnitConverter.GENESIS_SI_UNITS or units == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS:
        project.genesisSettings.setUnitSystemToUse(units) # else leave it as the units set in the proj

    project.genesisSettings.setMooseCompatMode(False)

    project.genesisSettings.setSymmetricCompartments(symmetricComps)

    project.genesisFileManager.setQuitAfterRun(quitAfterRun)

    compartmentalisation = GenesisCompartmentalisation()

    project.genesisFileManager.generateTheGenesisFiles(simConfig,
                                                            None,
                                                            compartmentalisation,
                                                            simulatorSeed)

    success = projectManager.doRunGenesis(simConfig)

    if success:
        print prefix+"Set running GENESIS simulation: "+simRef
    else:
        print prefix+"Problem running GENESIS simulation: "+simRef

    return success



def generateAndRunMoose(project,
                         projectManager,
                         simConfig,
                         simRef,
                         simulatorSeed,
                         verbose=True,
                         quitAfterRun=False,
                         runInBackground=False,
                         units=-1):

    prefix = "--- MOOSE gen:    "

    if verbose: print prefix+"Going to generate MOOSE files for: "+simRef

    if runInBackground:
        project.genesisSettings.setNoConsole()

    project.genesisFileManager.setQuitAfterRun(quitAfterRun)

    if units == UnitConverter.GENESIS_SI_UNITS or units == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS:
        project.genesisSettings.setUnitSystemToUse(units) # else leave it as the units set in the proj

    project.genesisSettings.setMooseCompatMode(True)

    compartmentalisation = GenesisCompartmentalisation()

    project.genesisFileManager.generateTheGenesisFiles(simConfig,
                                                            None,
                                                            compartmentalisation,
                                                            simulatorSeed)

    success = projectManager.doRunGenesis(simConfig)

    if success:
        print prefix+"Set running MOOSE simulation: "+simRef
    else:
        print prefix+"Problem running MOOSE simulation: "+simRef

    return success


def generateAndRunPsics(project,
                         projectManager,
                         simConfig,
                         simRef,
                         simulatorSeed,
                         verbose=True,
                         quitAfterRun=False,
                         runInBackground=False):

    prefix = "--- PSICS gen:    "

    if verbose: print prefix+"Going to generate PSICS files for: "+simRef

    if runInBackground:
        print prefix+"*********   Cannot run PSICS in background yet!!!!   ***********"


    project.psicsFileManager.generateThePsicsFiles(simConfig,
                                                   simulatorSeed)


    success = projectManager.doRunPsics(simConfig)

    if success:
        print prefix+"Set running PSICS simulation: "+simRef
    else:
        print prefix+"Problem running PSICS simulation: "+simRef

    return success
    
    


def generateAndRunNeuron(project, 
                         projectManager, 
                         simConfig, 
                         simRef, 
                         simulatorSeed, 
                         verbose=True, 
                         quitAfterRun=False, 
                         runInBackground=False,
                         varTimestep=False):

    prefix = "--- NEURON gen:   "

    if verbose: print prefix+"Going to generate NEURON files for simulation: "+simRef
    
    project.neuronFileManager.setQuitAfterRun(quitAfterRun)
    
    if runInBackground:
        project.neuronSettings.setNoConsole()

    project.neuronSettings.setVarTimeStep(varTimestep)
    
    project.neuronFileManager.generateTheNeuronFiles(simConfig,
                                                     None,
                                                     NeuronFileManager.RUN_HOC,
                                                     simulatorSeed)
    
    
    compileProcManager = ProcessManager(project.neuronFileManager.getMainHocFile())
    
    compileSuccess = compileProcManager.compileFileWithNeuron(0,0)
    
    if verbose: print prefix+"Compiled NEURON files for: "+simRef
    
    
    ### Set simulation running
    
    if compileSuccess:

        success = projectManager.doRunNeuron(simConfig)

        if success:
            print prefix+"Set running NEURON simulation: "+simRef
        else:
            print prefix+"Problem running NEURON simulation: "+simRef

        return success

    else:
        return False



        
class SimulationManager():

    def __init__(self,
                 project,
                 projectManager,
                 numConcurrentSims = 2,
                 verbose =           True):
                     
        self.project = project
        self.projectManager = projectManager
        self.numConcurrentSims = numConcurrentSims
        self.verbose = verbose

        self.printver("Starting Simulation Manager for project: "+self.project.getProjectFullFileName())
        self.printver("This will run up to %i simulations concurrently"%numConcurrentSims)


    def printver(self, message):
        if self.verbose:
            print "--- SimMgr:       "+ str(message)

    

    allRunningSims = []
    allFinishedSims = []

    def updateSimsRunning(self):

        for sim in self.allRunningSims:
                timeFile = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/time.dat")
                timeFile2 = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/time.txt") # for PSICS...

                self.printver("Checking file: "+timeFile.getAbsolutePath() +", exists: "+ str(timeFile.exists()))

                if (timeFile.exists()):
                        self.allFinishedSims.append(sim)
                        self.allRunningSims.remove(sim)
                else:
                    self.printver("Checking file: "+timeFile2.getAbsolutePath() +", exists: "+ str(timeFile2.exists()))
                    if (timeFile2.exists()):
                        self.allFinishedSims.append(sim)
                        self.allRunningSims.remove(sim)

        self.printver("allFinishedSims: "+str(self.allFinishedSims))
        self.printver("allRunningSims: "+str(self.allRunningSims))


    def doCheckNumberSims(self):

        self.printver("Simulations (out of max %s) currently running: %s"%(self.numConcurrentSims, str(self.allRunningSims)))

        while (len(self.allRunningSims)>=self.numConcurrentSims):
            self.printver("Waiting...")
            time.sleep(4) # wait a while...
            self.updateSimsRunning()



    def reloadSims(self,
                  waitForAllSimsToFinish =    True,
                  plotSims =                  True,
                  analyseSims =               True,
                  plotVoltageOnly =           False):


        self.printver("Trying to reload sims: "+str(self.allFinishedSims))

        plottedSims = []

        for simRef in self.allFinishedSims:

            simDir = File(self.project.getProjectMainDirectory(), "/simulations/"+simRef)
            timeFile = File(simDir, "time.dat")
            timeFile2 = File(simDir,"time.txt") # for PSICS...


            if timeFile.exists() or timeFile2.exists():
                self.printver("--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath())
                time.sleep(1) # wait a while...

                try:
                    simData = SimulationData(simDir)
                    simData.initialise()
                    times = simData.getAllTimes()

                    if analyseSims:


                        '''
                        volts = simData.getVoltageAtAllTimes(cellSegmentRef)

                        if verbose: print "Got "+str(len(volts))+" data points on cell seg ref: "+cellSegmentRef

                        analyseStartTime = 0
                        analyseStopTime = simConfig.getSimDuration()
                        analyseThreshold = -20 # mV

                        spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)

                        print "Spike times in %s for sim %s: %s"%(cellSegmentRef, simRef, str(spikeTimes))
                        '''
                    if plotSims:

                        simConfigName = simData.getSimulationProperties().getProperty("Sim Config")

                        if simConfigName.find('(')>=0:
                            simConfigName = simConfigName[0:simConfigName.find('(')]

                        for dataStore in simData.getAllLoadedDataStores():

                            ds = simData.getDataSet(dataStore.getCellSegRef(), dataStore.getVariable(), False)

                            if not plotVoltageOnly or dataStore.getVariable() == SimPlot.VOLTAGE:

                                plotFrame = PlotManager.getPlotterFrame("Behaviour of "+dataStore.getVariable() \
                                    +" for sim config: %s"%(simConfigName))

                                plotFrame.addDataSet(ds)


                        plottedSims.append(simRef)


                except:
                    self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
                    self.printver(sys.exc_info())

        for simRef in plottedSims:
            self.allFinishedSims.remove(simRef)


        if waitForAllSimsToFinish and len(self.allRunningSims)>0:

            self.printver("Waiting for sims: %s to finish..."%str(self.allRunningSims))

            time.sleep(2) # wait a while...
            self.updateSimsRunning()
            self.reloadSims(True)

    

    def runMultipleSims(self,
                        simConfigs =            ["Default Simulation Configuration"],
                        maxElecLens =           [-1],
                        simDt =                 -1,
                        neuroConstructSeed =    12345,
                        simulatorSeed =         11111,
                        simulators =            ["NEURON", "GENESIS_PHYS"],
                        runSims =               True,
                        verboseSims =           True,
                        runInBackground =       False,
                        varTimestepNeuron =     -1):


        for simConfigName in simConfigs:

          for maxElecLen in maxElecLens:

            if simDt != -1:
                self.project.simulationParameters.setDt(simDt)

            simConfig = self.project.simConfigInfo.getSimConfig(simConfigName)

            recompSuffix = ""

            if maxElecLen > 0:
                cellGroup = simConfig.getCellGroups().get(0)
                cell = self.project.cellManager.getCell(self.project.cellGroupsInfo.getCellType(cellGroup))

                self.printver("Recompartmentalising cell in: "+cellGroup+" which is: "+str(cell))

                info = CellTopologyHelper.recompartmentaliseCell(cell, maxElecLen, self.project)
                self.printver("*** Recompartmentalised cell: "+info)

                recompSuffix = "_"+str(maxElecLen)

            self.projectManager.doGenerate(simConfig.getName(), neuroConstructSeed)

            while self.projectManager.isGenerating():
                    self.printver("Waiting for the project to be generated with Simulation Configuration: "+str(simConfig))
                    time.sleep(1)


            self.printver("Generated network with %i cell(s)" % self.project.generatedCellPositions.getNumberInAllCellGroups())

            
            simRefPrefix = (simConfigName+"_").replace(' ', '')

            self.doCheckNumberSims()

            self.printver("Going to generate for simulators: "+str(simulators))

            if simulators.count("NEURON")>0:

                simRef = simRefPrefix+"_N"+recompSuffix
                self.project.simulationParameters.setReference(simRef)

                if varTimestepNeuron == -1:
                    varTimestepNeuron = self.project.neuronSettings.isVarTimeStep()

                if runSims:
                    generateAndRunNeuron(self.project,
                                         self.projectManager,
                                         simConfig,
                                         simRef,
                                         simulatorSeed,
                                         verbose=verboseSims,
                                         runInBackground=runInBackground,
                                         varTimestep=varTimestepNeuron)

                self.allRunningSims.append(simRef)


            self.doCheckNumberSims()

            if simulators.count("PSICS")>0:

                simRef = simRefPrefix+"_P"+recompSuffix
                project.simulationParameters.setReference(simRef)

                if runSims:
                    generateAndRunPsics(self.project,
                                        self.projectManager,
                                        simConfig,
                                        simRef,
                                        simulatorSeed,
                                        verbose=verboseSims,
                                        runInBackground=runInBackground)

                self.allRunningSims.append(simRef)


            self.doCheckNumberSims()

            for sim in simulators:
              if "MOOSE" in sim:

                simRef = simRefPrefix+"_M"+recompSuffix

                units = -1 # leave as what's set in project

                if "_SI" in sim:
                    simRef = simRef+"_SI"
                    units = UnitConverter.GENESIS_SI_UNITS
                if "_PHYS" in sim:
                    simRef = simRef+"_PHYS"
                    units = UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS

                self.project.simulationParameters.setReference(simRef)


                if runSims:
                    generateAndRunMoose(self.project,
                                        self.projectManager,
                                        simConfig,
                                        simRef,
                                        simulatorSeed,
                                        verbose=verboseSims,
                                        quitAfterRun=runInBackground,
                                        runInBackground=runInBackground,
                                        units=units)

                self.allRunningSims.append(simRef)

                time.sleep(2) # wait a while before running GENESIS...

            self.doCheckNumberSims()

            for sim in simulators:
              if "GENESIS" in sim:

                simRef = simRefPrefix+"_G"+recompSuffix

                units = -1 # leave as what's set in project

                if "_SI" in sim:
                    simRef = simRef+"_SI"
                    units = UnitConverter.GENESIS_SI_UNITS
                if "_PHYS" in sim:
                    simRef = simRef+"_PHYS"
                    units = UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS

                self.project.simulationParameters.setReference(simRef)

                if runSims:
                    generateAndRunGenesis(self.project,
                                          self.projectManager,
                                          simConfig,
                                          simRef,
                                          simulatorSeed,
                                          verbose=verboseSims,
                                          quitAfterRun=runInBackground,
                                          runInBackground=runInBackground,
                                          units=units,
                                          symmetricComps=False)

                self.allRunningSims.append(simRef)

                time.sleep(2) # wait a while before running GENESISsym...

            self.doCheckNumberSims()

            if simulators.count("GENESISsym")>0:

                simRef = simRefPrefix+"_Gs"+recompSuffix
                self.project.simulationParameters.setReference(simRef)

                if runSims:
                    nc.generateAndRunGenesis(self.project,
                                             self.projectManagerm,
                                             simConfig,
                                             simRef,
                                             simulatorSeed,
                                             verbose=verboseSims,
                                             quitAfterRun=runInBackground,
                                             runInBackground=runInBackground,
                                             symmetricComps=True)

                self.allRunningSims.append(simRef)


            self.updateSimsRunning()
            




        #if not plotSims:
        #    sys.exit()
