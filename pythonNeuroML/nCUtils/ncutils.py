# -*- coding: utf-8 -*-
#
#
#   File to preform some standard tasks on a neuroConstruct project
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
import subprocess

from java.io import File


from ucl.physiol.neuroconstruct.cell.utils import CellTopologyHelper
from ucl.physiol.neuroconstruct.cell.compartmentalisation import GenesisCompartmentalisation
from ucl.physiol.neuroconstruct.cell.compartmentalisation import OriginalCompartmentalisation

from ucl.physiol.neuroconstruct.gui.plotter import PlotManager
from ucl.physiol.neuroconstruct.gui.plotter import PlotCanvas

from ucl.physiol.neuroconstruct.dataset import DataSet

from ucl.physiol.neuroconstruct.neuron import NeuronFileManager
from ucl.physiol.neuroconstruct.neuron.NeuronSettings import DataSaveFormat
from ucl.physiol.neuroconstruct.nmodleditor.processes import ProcessManager

from ucl.physiol.neuroconstruct.neuroml import NeuroMLConstants
from ucl.physiol.neuroconstruct.neuroml import LemsConstants

from ucl.physiol.neuroconstruct.project import SimPlot
from ucl.physiol.neuroconstruct.project import ProjectManager

from ucl.physiol.neuroconstruct.simulation import SimulationData
from ucl.physiol.neuroconstruct.simulation import SpikeAnalyser

from ucl.physiol.neuroconstruct.utils.units import UnitConverter
from ucl.physiol.neuroconstruct.utils import NumberGenerator

from ucl.physiol.neuroconstruct.hpc.mpi import MpiSettings

from ucl.physiol.neuroconstruct.pynn.PynnFileManager import PynnSimulator



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
                        runInBackground=False):

    prefix = "--- PSICS gen:    "

    if verbose: print prefix+"Going to generate PSICS files for: "+simRef


    project.psicsFileManager.generateThePsicsFiles(simConfig,
                                                   simulatorSeed)


    success = projectManager.doRunPsics(simConfig, (not runInBackground))

    if success:
        print prefix+"Set running PSICS simulation: "+simRef
    else:
        print prefix+"Problem running PSICS simulation: "+simRef

    return success


def generateAndRunLems(project,
                        projectManager,
                        simConfig,
                        simRef,
                        simulatorSeed,
                        verbose=True,
                        runInBackground=False):

    prefix = "--- LEMS/NeuroML 2 gen:    "

    if verbose: print prefix+"Going to generate LEMS/NeuroML 2 files for: "+simRef

    compartmentalisation = OriginalCompartmentalisation()

    project.neuromlFileManager.generateNeuroMLFiles(simConfig,
                                                    NeuroMLConstants.NeuroMLVersion.NEUROML_VERSION_2_ALPHA,
                                                    LemsConstants.LemsOption.EXECUTE_MODEL,
                                                    compartmentalisation,
                                                    simulatorSeed,
                                                    False,
                                                    False,
                                                    runInBackground)



    return 1  # Call above will throw error if it fails


def generateAndRunPyNN(pynnSim,
                        project,
                        projectManager,
                        simConfig,
                        simRef,
                        simulatorSeed,
                        verbose=True,
                        runInBackground=False):

    prefix = "--- PyNN_"+pynnSim+" gen:    "

    if verbose: print prefix+"Going to generate PyNN_"+pynnSim+" files for: "+simRef

    pynnSimulator = None

    if "NEST" in pynnSim:
        pynnSimulator = PynnSimulator.NEST
    elif "NEURON" in pynnSim:
        pynnSimulator = PynnSimulator.NEURON
    elif "BRIAN" in pynnSim:
        pynnSimulator = PynnSimulator.BRIAN
    else:
        print pynnSim

    #if verbose: print prefix+"Going to generate PyNN_"+str(pynnSimulator)+" files for: "+simRef

    project.pynnFileManager.generateThePynnFiles(simConfig,
                                                    pynnSimulator,
                                                    simulatorSeed)


    project.pynnFileManager.runFile(True)
    
    return 1
    
    


def generateAndRunNeuron(project, 
                         projectManager, 
                         simConfig, 
                         simRef, 
                         simulatorSeed, 
                         verbose=               True,
                         quitAfterRun=          False,
                         runInBackground=       False,
                         varTimestep=           False,
                         varTimestepTolerance=  None,
                         saveAsHdf5 =           False,
                         runMode =              NeuronFileManager.RUN_HOC):

    prefix = "--- NEURON gen:   "

    if verbose: print prefix+"Going to generate NEURON files for simulation: "+simRef
    
    project.neuronFileManager.setQuitAfterRun(quitAfterRun)
    
    if runInBackground:
        project.neuronSettings.setNoConsole()

    if saveAsHdf5:
        project.neuronSettings.setDataSaveFormat(DataSaveFormat.HDF5_NC)
    else:
        project.neuronSettings.setDataSaveFormat(DataSaveFormat.TEXT_NC)


    project.neuronSettings.setVarTimeStep(varTimestep)

    if varTimestepTolerance is not None:
        project.neuronSettings.setVarTimeAbsTolerance(varTimestepTolerance)
    
    project.neuronFileManager.generateTheNeuronFiles(simConfig,
                                                     None,
                                                     runMode,
                                                     simulatorSeed)
    

    if verbose: print prefix+"Generated hoc files for simulation: "+simRef
    
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

    knownSimulators = ["NEURON", "GENESIS", "GENESIS_SI", "GENESIS_PHYS", "MOOSE", "MOOSE_PHYS", "MOOSE_SI", "PSICS", "LEMS", "PYNN_NEST", "PYNN_NEURON", "PYNN_BRIAN"]

    plotFrames = {}
    dataSets = {}

    def __init__(self,
                 projFile,
                 numConcurrentSims = 1,
                 verbose =           True):


        self.allRunningSims = []
        self.allRecentlyFinishedSims = []
        self.allFinishedSims = []

        self.projectManager = ProjectManager()
        self.project = self.projectManager.loadProject(projFile)
        
        self.numConcurrentSims = numConcurrentSims
        self.verbose = verbose

        self.printver("Starting Simulation Manager for project: "+self.project.getProjectFullFileName(), True)
        self.printver("This will run up to %i simulations concurrently"%numConcurrentSims)



    def printver(self, message, forcePrint=False):
        if self.verbose or forcePrint:
            print "--- SimMgr:       "+ str(message)



    def updateSimsRunning(self):
        self.updateSimsRunningR(True)

    def updateSimsRunningR(self, checkRemote):

        remoteChecked = False
        for sim in self.allRunningSims:

                completed = False

                timeFile = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/time.dat")
                timeFile2 = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/time.txt") # for PSICS...

                self.printver("Checking file: "+timeFile.getCanonicalPath() +", exists: "+ str(timeFile.exists()))

                if (timeFile.exists()):
                        self.allFinishedSims.append(sim)
                        self.allRecentlyFinishedSims.append(sim)
                        self.allRunningSims.remove(sim)
                        completed = True
                else:
                    self.printver("Checking file: "+timeFile2.getCanonicalPath() +", exists: "+ str(timeFile2.exists()))
                    if (timeFile2.exists()):
                        self.allFinishedSims.append(sim)
                        self.allRecentlyFinishedSims.append(sim)
                        self.allRunningSims.remove(sim)
                        completed = True

                if checkRemote and not completed:
                    pullFile = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/pullsim.sh")
                    checkingRemoteFile = File(self.project.getProjectMainDirectory(), "simulations/"+sim+"/checkingRemote")

                    if pullFile.exists() and not checkingRemoteFile.exists():
                        pullCmd = ''+pullFile.getAbsolutePath()
                        self.printver("Going to run: "+pullCmd)

                        subprocess.call(pullCmd,shell=True)
                        remoteChecked = True

        if remoteChecked:
            self.printver("Waiting while remote simulations are checked...")
            time.sleep(5)
            self.updateSimsRunningR(False)
        else:
            self.printver("allRecentlyFinishedSims: "+str(self.allRecentlyFinishedSims))
            self.printver("allFinishedSims: "+str(self.allFinishedSims))
            self.printver("allRunningSims: "+str(self.allRunningSims))


    def doCheckNumberSims(self):

        self.printver("%i simulations out of max %s currently running: %s"%(len(self.allRunningSims), self.numConcurrentSims, str(self.allRunningSims)))

        while (len(self.allRunningSims)>=self.numConcurrentSims):
            self.printver("Waiting for another simulation slot to become available...")
            time.sleep(4) # wait a while...
            self.updateSimsRunning()



    def reloadSims(self,
                   waitForAllSimsToFinish =    True,
                   plotSims =                  True,
                   analyseSims =               True,
                   plotVoltageOnly =           False):

        self.printver("Trying to reload simulations: "+str(self.allFinishedSims))

        plottedSims = []

        for simRef in self.allRecentlyFinishedSims:

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

                    if plotSims:

                        simConfigName = simData.getSimulationProperties().getProperty("Sim Config")

                        if simConfigName.find('(')>=0:
                            simConfigName = simConfigName[0:simConfigName.find('(')]

                        for dataStore in simData.getAllLoadedDataStores():

                            ds = simData.getDataSet(dataStore.getCellSegRef(), dataStore.getVariable(), False)

                            #self.printver("Found data store: "+str(dataStore)+", plotting volts only: "+str(plotVoltageOnly))

                            if not plotVoltageOnly or dataStore.getVariable() == SimPlot.VOLTAGE:

                                plotFrame = PlotManager.getPlotterFrame("Behaviour of "+dataStore.getVariable() \
                                    +" for sim config: %s"%(simConfigName))

                                plotFrame.addDataSet(ds)


                                if analyseSims:

                                    volts = ds.getYValues()

                                    analyseStartTime = 0
                                    analyseStopTime = times[-1]
                                    analyseThreshold = 0 # mV

                                    spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)

                                    self.printver("Spike times in %s for sim %s: %s"%(dataStore.getCellSegRef(), simRef, str(spikeTimes)), True)


                        plottedSims.append(simRef)


                except:
                    self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath(), True)
                    self.printver(sys.exc_info(), True)

        for simRef in plottedSims:
            self.allRecentlyFinishedSims.remove(simRef)


        if waitForAllSimsToFinish and len(self.allRunningSims)>0:

            self.printver("Waiting for sims: %s to finish..."%str(self.allRunningSims))

            time.sleep(1) # wait a while...
            self.updateSimsRunning()
            
            self.reloadSims(waitForAllSimsToFinish,
                            plotSims,
                            analyseSims,
                            plotVoltageOnly)


    def checkSims(self,
                  spikeTimesToCheck = {},
                  spikeTimeAccuracy = 0.01,
                  threshold = 0 ): # mV

        self.updateSimsRunning()

        self.printver( "Trying to check simulations: %s against: %s, with threshold: %s" % (str(self.allFinishedSims), str(spikeTimesToCheck), str(threshold)))

        report = ""
        numPassed = 0
        numFailed = 0
        checksUnused = spikeTimesToCheck.keys()

        for simRef in self.allFinishedSims:

            simDir = File(self.project.getProjectMainDirectory(), "/simulations/"+simRef)
            try:
                simData = SimulationData(simDir)
                simData.initialise()
                times = simData.getAllTimes()

                simConfigName = simData.getSimulationProperties().getProperty("Sim Config")

                if simConfigName.find('(')>=0:
                    simConfigName = simConfigName[0:simConfigName.find('(')]

                for dataStore in simData.getAllLoadedDataStores():

                    self.printver("Checking dataStore: "+str(dataStore))
                    ds = simData.getDataSet(dataStore.getCellSegRef(), dataStore.getVariable(), False)

                    if dataStore.getVariable() == SimPlot.VOLTAGE:

                        if spikeTimesToCheck is not None:

                            volts = ds.getYValues()

                            analyseStartTime = 0
                            analyseStopTime = times[-1]

                            threshToUse = threshold

                            if type(threshold) is dict:
                                threshToUse = float(threshold[dataStore.getCellSegRef()])

                            spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, threshToUse, analyseStartTime, analyseStopTime)

                            self.printver("Spike times (crossing %f) from %f to %f in %s for sim %s: %s"%(threshToUse, analyseStartTime, analyseStopTime, dataStore.getCellSegRef(), simRef, str(spikeTimes)))

                            if spikeTimesToCheck.has_key(dataStore.getCellSegRef()):
                                self.printver("Removing %s from %s"%(str(dataStore.getCellSegRef()), str(checksUnused)))
                                if dataStore.getCellSegRef() in checksUnused:
                                    checksUnused.remove(dataStore.getCellSegRef())
                                fail = False
                                spikeTimesTarget = spikeTimesToCheck[dataStore.getCellSegRef()]

                                if len(spikeTimes) != len(spikeTimesTarget):
                                    report = report + "ERROR: Number of spikes of %s (%i) not same as target list for %s (%i)!\n"% \
                                        (dataStore.getCellSegRef(), len(spikeTimes), simRef, len(spikeTimesTarget))
                                    fail = True

                                for spikeNum in range(0, min(len(spikeTimesTarget),len(spikeTimes))):
                                    delta = spikeTimesTarget[spikeNum] - spikeTimes[spikeNum]
                                    if float(abs(delta)) > float(spikeTimeAccuracy):
                                        report = report + "ERROR: Spike time: %f not within %f of %f (delta = %f) for %s in %s!\n" % \
                                                  (spikeTimes[spikeNum], spikeTimeAccuracy, spikeTimesTarget[spikeNum], delta, dataStore.getCellSegRef(), simRef) 
                                        fail = True
                                if fail:
                                    numFailed=numFailed+1
                                else:
                                    numPassed=numPassed+1



            except:
                self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
                raise
                self.printver(sys.exc_info())
                numFailed=numFailed+1

        
        ignored = "" if len(checksUnused) == 0 else ", %i test conditions ignored"%(len(checksUnused))


        report = report+"\n  %i tests passed, %i tests failed%s!\n"%(numPassed, numFailed, ignored)

        return report

    

    def runMultipleSims(self,
                        simConfigs =                ["Default Simulation Configuration"],
                        maxElecLens =               [-1],
                        simDt =                     None,
                        simDtOverride =             None,
                        simDuration =               None,
                        neuroConstructSeed =        12345,
                        simulatorSeed =             11111,
                        simulators =                ["NEURON", "GENESIS_PHYS"],
                        runSims =                   True,
                        verboseSims =               True,
                        runInBackground =           False,
                        varTimestepNeuron =         None,
                        varTimestepTolerance =      None,
                        simRefGlobalSuffix =        '',
                        simRefGlobalPrefix =        '',
                        mpiConfig =                 MpiSettings.LOCAL_SERIAL,
                        mpiConfigs =                [],
                        suggestedRemoteRunTime =    -1,
                        saveAsHdf5 =                False,
                        saveOnlySpikes =            False,
                        saveAllContinuous =         False,
                        runMode =                   NeuronFileManager.RUN_HOC):

        for sim in simulators:
            if sim not in self.knownSimulators:
                print "Unknown simulator: "+sim+"!"
                print "Known simulators: "+str(self.knownSimulators)
                sys.exit(1)
                
        allSimsSetRunning = []

        for simConfigName in simConfigs:

          simConfig = self.project.simConfigInfo.getSimConfig(simConfigName)

          self.printver("Going to generate network for Simulation Configuration: "+str(simConfig))

          if saveOnlySpikes:
              for simPlotName in simConfig.getPlots():
                  simPlot = self.project.simPlotInfo.getSimPlot(simPlotName)
                  if simPlot.getValuePlotted() == SimPlot.VOLTAGE:
                      simPlot.setValuePlotted(SimPlot.SPIKE)
                      
          if saveAllContinuous:
              for simPlotName in simConfig.getPlots():
                  simPlot = self.project.simPlotInfo.getSimPlot(simPlotName)
                  #print simPlot
                  if SimPlot.SPIKE in simPlot.getValuePlotted():
                      simPlot.setValuePlotted(SimPlot.VOLTAGE)
                  #print simPlot
          
          if len(mpiConfigs) == 0:
              mpiConfigs = [mpiConfig]

          for mpiConfigToUse in mpiConfigs:

              mpiSettings = MpiSettings()
              simConfig.setMpiConf(mpiSettings.getMpiConfiguration(mpiConfigToUse))

              self.printver("Using Parallel Configuration: "+ str(simConfig.getMpiConf()))

              if suggestedRemoteRunTime > 0:
                    self.project.neuronFileManager.setSuggestedRemoteRunTime(suggestedRemoteRunTime)
                    self.project.genesisFileManager.setSuggestedRemoteRunTime(suggestedRemoteRunTime)

              for maxElecLen in maxElecLens:

                if simDt is not None:
                    self.project.simulationParameters.setDt(simDt)
                else:
                    simDt = self.project.simulationParameters.getDt() # for later if simDtOverride used...


                if simDuration is not None:
                    simConfig.setSimDuration(simDuration)

                recompSuffix = ""

                if maxElecLen > 0:
                    cellGroup = simConfig.getCellGroups().get(0)
                    cell = self.project.cellManager.getCell(self.project.cellGroupsInfo.getCellType(cellGroup))

                    self.printver("Recompartmentalising cell in: "+cellGroup+" which is: "+str(cell))

                    info = CellTopologyHelper.recompartmentaliseCell(cell, maxElecLen, self.project)
                    self.printver("*** Recompartmentalised cell: "+info)

                if len(maxElecLens) > 1 or maxElecLen > 0 : recompSuffix = "_"+str(maxElecLen)

                self.projectManager.doGenerate(simConfig.getName(), neuroConstructSeed)

                while self.projectManager.isGenerating():
                        self.printver("Waiting for the project to be generated with Simulation Configuration: "+str(simConfig))
                        time.sleep(15)


                self.printver("Generated network with %i cell(s)" % self.project.generatedCellPositions.getNumberInAllCellGroups())

                simRefPrefix = (simConfigName+"_").replace(' ', '').replace(':', '')

                if len(mpiConfigs) > 1:
                    simRefPrefix = simRefPrefix+(mpiConfigToUse+"_").replace(' ', '').replace('(', '_').replace(')', '_')

                self.doCheckNumberSims()

                self.printver("Going to generate for simulators: "+str(simulators))

                if simulators.count("NEURON")>0:

                    if simDtOverride is not None:
                        if simDtOverride.has_key("NEURON"):
                            self.project.simulationParameters.setDt(simDtOverride["NEURON"])
                        else:
                            self.project.simulationParameters.setDt(simDt)

                    simRef = simRefGlobalPrefix + simRefPrefix+"_N"+recompSuffix + simRefGlobalSuffix
                    self.project.simulationParameters.setReference(simRef)

                    if varTimestepNeuron is None:
                        varTimestepNeuron = self.project.neuronSettings.isVarTimeStep()

                    if varTimestepTolerance is None:
                        varTimestepTolerance = self.project.neuronSettings.getVarTimeAbsTolerance()

                    if runSims:
                        success = generateAndRunNeuron(self.project,
                                             self.projectManager,
                                             simConfig,
                                             simRef,
                                             simulatorSeed,
                                             verbose=               verboseSims,
                                             runInBackground=       runInBackground,
                                             varTimestep=           varTimestepNeuron,
                                             varTimestepTolerance=  varTimestepTolerance,
                                             saveAsHdf5 =           saveAsHdf5,
                                             runMode =              runMode)
                                             
                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)


                self.doCheckNumberSims()

                if simulators.count("PSICS")>0:

                    if simDtOverride is not None:
                        if simDtOverride.has_key("PSICS"):
                            self.project.simulationParameters.setDt(simDtOverride["PSICS"])
                        else:
                            self.project.simulationParameters.setDt(simDt)

                    simRef = simRefGlobalPrefix + simRefPrefix+"_P"+recompSuffix + simRefGlobalSuffix
                    self.project.simulationParameters.setReference(simRef)

                    if runSims:
                        success = generateAndRunPsics(self.project,
                                            self.projectManager,
                                            simConfig,
                                            simRef,
                                            simulatorSeed,
                                            verbose=verboseSims,
                                            runInBackground=runInBackground)

                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)

                self.doCheckNumberSims()

                if simulators.count("LEMS")>0:

                    if simDtOverride is not None:
                        if simDtOverride.has_key("LEMS"):
                            self.project.simulationParameters.setDt(simDtOverride["LEMS"])
                        else:
                            self.project.simulationParameters.setDt(simDt)

                    simRef = simRefGlobalPrefix + simRefPrefix+"_L"+recompSuffix + simRefGlobalSuffix
                    self.project.simulationParameters.setReference(simRef)

                    if runSims:
                        success = generateAndRunLems(self.project,
                                            self.projectManager,
                                            simConfig,
                                            simRef,
                                            simulatorSeed,
                                            verbose=verboseSims,
                                            runInBackground=runInBackground)

                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)

                self.doCheckNumberSims()

                for sim in simulators:
                  if "PYNN_" in sim:

                        if simDtOverride is not None:
                            if simDtOverride.has_key(sim):
                                self.project.simulationParameters.setDt(simDtOverride[sim])
                            else:
                                self.project.simulationParameters.setDt(simDt)

                        pynnSim = sim[5:]
                        simRef = simRefGlobalPrefix + simRefPrefix+"_Py_"+pynnSim+recompSuffix + simRefGlobalSuffix
                        self.project.simulationParameters.setReference(simRef)

                        if runSims:
                            success = generateAndRunPyNN(pynnSim,
                                                self.project,
                                                self.projectManager,
                                                simConfig,
                                                simRef,
                                                simulatorSeed,
                                                verbose=verboseSims,
                                                runInBackground=runInBackground)

                            if success:
                                self.allRunningSims.append(simRef)
                                allSimsSetRunning.append(simRef)
                        else:
                            allSimsSetRunning.append(simRef)

                  self.printver("Waiting a while before running next sim...")
                  time.sleep(2) # wait a while before running PyNN...


                self.doCheckNumberSims()



                for sim in simulators:
                  if "MOOSE" in sim:

                    if simDtOverride is not None:
                        if simDtOverride.has_key(sim):
                            self.project.simulationParameters.setDt(simDtOverride[sim])
                        else:
                            self.project.simulationParameters.setDt(simDt)

                    simRef = simRefGlobalPrefix + simRefPrefix+"_M"+recompSuffix + simRefGlobalSuffix

                    units = -1 # leave as what's set in project

                    if "_SI" in sim:
                        simRef = simRef+"_SI"
                        units = UnitConverter.GENESIS_SI_UNITS
                    if "_PHYS" in sim:
                        simRef = simRef+"_PHYS"
                        units = UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS

                    self.project.simulationParameters.setReference(simRef)


                    if runSims:
                        success = generateAndRunMoose(self.project,
                                            self.projectManager,
                                            simConfig,
                                            simRef,
                                            simulatorSeed,
                                            verbose=verboseSims,
                                            quitAfterRun=runInBackground,
                                            runInBackground=runInBackground,
                                            units=units)

                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)

                    time.sleep(1) # wait a while before running GENESIS...

                self.doCheckNumberSims()

                for sim in simulators:
                  if "GENESIS" in sim:

                    if simDtOverride is not None:
                        if simDtOverride.has_key(sim):
                            self.project.simulationParameters.setDt(simDtOverride[sim])
                        else:
                            self.project.simulationParameters.setDt(simDt)

                    simRef = simRefGlobalPrefix + simRefPrefix+"_G"+recompSuffix + simRefGlobalSuffix

                    units = -1 # leave as what's set in project

                    if "_SI" in sim:
                        simRef = simRef+"_SI"
                        units = UnitConverter.GENESIS_SI_UNITS
                    if "_PHYS" in sim:
                        simRef = simRef+"_PHYS"
                        units = UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS

                    self.project.simulationParameters.setReference(simRef)

                    if runSims:
                        success = generateAndRunGenesis(self.project,
                                              self.projectManager,
                                              simConfig,
                                              simRef,
                                              simulatorSeed,
                                              verbose=verboseSims,
                                              quitAfterRun=runInBackground,
                                              runInBackground=runInBackground,
                                              units=units,
                                              symmetricComps=False)

                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)

                    time.sleep(1) # wait a while before running GENESISsym...

                self.doCheckNumberSims()

                if simulators.count("GENESISsym")>0:

                    simRef = simRefGlobalPrefix + simRefPrefix+"_Gs"+recompSuffix + simRefGlobalSuffix
                    self.project.simulationParameters.setReference(simRef)

                    if runSims:
                        success = generateAndRunGenesis(self.project,
                                                 self.projectManagerm,
                                                 simConfig,
                                                 simRef,
                                                 simulatorSeed,
                                                 verbose=verboseSims,
                                                 quitAfterRun=runInBackground,
                                                 runInBackground=runInBackground,
                                                 symmetricComps=True)

                        if success:
                            self.allRunningSims.append(simRef)
                            allSimsSetRunning.append(simRef)
                    else:
                        allSimsSetRunning.append(simRef)

                self.updateSimsRunningR(False)

              self.printver("Finished setting running all simulations for ParallelConfig: "+mpiConfigToUse)

          self.printver("Finished setting running all simulations for sim config: "+simConfigName)
              
            

        return allSimsSetRunning



    def generateFICurve(self,
                        simulator,
                        simConfigName,
                        stimAmpLow,
                        stimAmpInc,
                        stimAmpHigh,
                        stimDel,
                        stimDur,
                        simDuration,
                        analyseStartTime,
                        analyseStopTime,
                        analyseThreshold,
                        simDt =                   None,
                        simPrefix =               'FI_',
                        neuroConstructSeed =      1234,
                        plotAllTraces =           False,
                        verboseSims =             True,
                        varTimestepNeuron =       None,
                        mpiConfig =               MpiSettings.LOCAL_SERIAL,
                        suggestedRemoteRunTime =  -1):
                             
        simConfig = self.project.simConfigInfo.getSimConfig(simConfigName)

        self.printver("Going to generate F-I curve on %s for sim config: %s with amplitude of stim: (%f -> %f ; %f)" % (simulator, simConfigName, stimAmpLow, stimAmpHigh, stimAmpInc))

        if simConfig == None: 
            raise NameError('No such Simulation configuration as: '+ simConfigName+'. \nExisting sim configs: '+str(self.project.simConfigInfo.getAllSimConfigNames()))


        simConfig.setSimDuration(simDuration)

        self.projectManager.doGenerate(simConfig.getName(), neuroConstructSeed)

        while self.projectManager.isGenerating():
            self.printver("Waiting for the project to be generated with Simulation Configuration: "+str(simConfig))
            time.sleep(1)

        numGenerated = self.project.generatedCellPositions.getNumberInAllCellGroups()

        self.printver("Number of cells generated: " + str(numGenerated))

        if numGenerated > 0:

            self.printver("Generating scripts for simulator: %s..."%simulator)

            if simulator == 'NEURON':
                self.project.neuronFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
                self.project.neuronSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
                self.project.neuronSettings.setGraphicsMode(0) # 0 hides graphs during execution

            if simulator.count('GENESIS')>0 or simulator.count('MOOSE')>0:
                self.project.genesisFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
                self.project.genesisSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
                self.project.genesisSettings.setGraphicsMode(0) # 0 hides graphs during execution


            stimAmp = stimAmpLow
            
            simRefsVsStims = {}

            while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors

                ########  Adjusting the amplitude of the current clamp ###############

                stim = self.project.elecInputInfo.getStim(simConfig.getInputs().get(0))

                if stim.getElectricalInput().getType() != "IClamp":
                    raise Exception('Simulation config: '+ simConfigName+' has a non IClamp input: '+str(stim)+'!')

                if simConfig.getInputs()>1:
                    for stimIndex in range(1, simConfig.getInputs().size()):
                        stimOther = self.project.elecInputInfo.getStim(simConfig.getInputs().get(stimIndex))

                        if stimOther.getElectricalInput().getType() != "IClamp":
                            raise Exception('Simulation config: '+ simConfigName+' has a non IClamp input: '+str(stimOther)+'!')
                        else:
                            stimOther.setAmp(NumberGenerator(0))
                            stimOther.setDel(NumberGenerator(0))
                            stimOther.setDur(NumberGenerator(0))



                stim.setAmp(NumberGenerator(stimAmp))
                stim.setDel(NumberGenerator(stimDel))
                stim.setDur(NumberGenerator(stimDur))

                self.project.elecInputInfo.updateStim(stim)

                self.printver("Next stim: "+ str(stim))


                simRefs = self.runMultipleSims(simConfigs =              [simConfig.getName()],
                                               simulators =              [simulator],
                                               simDt =                   simDt,
                                               verboseSims =             verboseSims,
                                               runInBackground =         True,
                                               simRefGlobalPrefix =      simPrefix,
                                               simRefGlobalSuffix =      ("_"+str(float(stimAmp))),
                                               varTimestepNeuron =       varTimestepNeuron,
                                               mpiConfig =               mpiConfig,
                                               suggestedRemoteRunTime =  suggestedRemoteRunTime)
                                               
                simRefsVsStims[simRefs[0]] = stimAmp # should be just one simRef returned...

                stimAmp = stimAmp + stimAmpInc
                if abs(stimAmp) < stimAmpInc/1e9: stimAmp = 0

            while (len(self.allRunningSims)>0):
                self.printver("Waiting for all simulations to finish...")
                time.sleep(1) # wait a while...
                self.updateSimsRunning()


            self.printver("Going to plot traces from recorded sims: %s"%str(simRefsVsStims))
                    

            plotFrameFI = PlotManager.getPlotterFrame("F-I curve from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, 1)
            
            plotFrameVolts = PlotManager.getPlotterFrame("Voltage traces from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, plotAllTraces)

            plotFrameFI.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)

            info = "F-I curve for Simulation Configuration: "+str(simConfig)
            
            dataSet = DataSet(info, info, "nA", "Hz", "Current injected", "Firing frequency")
            dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)


            simList = simRefsVsStims.keys()
            simList.sort()

            for sim in simList:

              simDir = File(self.project.getProjectMainDirectory(), "/simulations/"+sim)
              self.printver("--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath())

              try:
                  simData = SimulationData(simDir)
                  simData.initialise()
                  self.printver("Data loaded: ")
                  self.printver(simData.getAllLoadedDataStores())

                  times = simData.getAllTimes()
                  cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"
                  volts = simData.getVoltageAtAllTimes(cellSegmentRef)

                  traceInfo = "Voltage at: %s in simulation: %s"%(cellSegmentRef, sim)

                  dataSetV = DataSet(traceInfo, traceInfo, "mV", "ms", "Membrane potential", "Time")
                  for i in range(len(times)):
                      dataSetV.addPoint(times[i], volts[i])

                  if plotAllTraces:
                    plotFrameVolts.addDataSet(dataSetV)

                  spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)
                  stimAmp = simRefsVsStims[sim]
                  self.printver("Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes)))

                  avgFreq = 0
                  if len(spikeTimes)>1:
                      avgFreq = len(spikeTimes)/ ((analyseStopTime - analyseStartTime)/1000.0)
                      dataSet.addPoint(stimAmp,avgFreq)
                  else:
                      dataSet.addPoint(stimAmp,0)

              except:
                  self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
                  self.printver(sys.exc_info()[0])


            plotFrameFI.addDataSet(dataSet)
            

    def generateBatchCurve(self,
                        simulator,
                        simConfigName,
                        stimAmpLow,
                        stimAmpInc,
                        stimAmpHigh,
                        stimDel,
                        stimDur,
                        simDuration,
                        analyseStartTime,
                        analyseStopTime,
                        analyseThreshold,
                        simDt =                   None,
                        simPrefix =               'FI_',
                        neuroConstructSeed =      1234,
                        plotAllTraces =           False,
                        verboseSims =             True,
                        varTimestepNeuron =       None,
                        mpiConfig =               MpiSettings.LOCAL_SERIAL,
                        suggestedRemoteRunTime =  -1,
                        curveType =               'F-I'):


        simConfig = self.project.simConfigInfo.getSimConfig(simConfigName)

        self.printver("Going to generate %s curve on %s for sim config: %s with amplitude of stim: (%f -> %f ; %f)" % (curveType, simulator, simConfigName, stimAmpLow, stimAmpHigh, stimAmpInc))
        # can generate differetn categories of simulationType F-I also SS-I

        if simConfig == None:
            raise NameError('No such Simulation configuration as: '+ simConfigName+'. \nExisting sim configs: '+str(self.project.simConfigInfo.getAllSimConfigNames()))


        simConfig.setSimDuration(simDuration)

        self.projectManager.doGenerate(simConfig.getName(), neuroConstructSeed)

        while self.projectManager.isGenerating():
            self.printver("Waiting for the project to be generated with Simulation Configuration: "+str(simConfig))
            time.sleep(1)

        numGenerated = self.project.generatedCellPositions.getNumberInAllCellGroups()

        self.printver("Number of cells generated: " + str(numGenerated))

        if numGenerated > 0:

            self.printver("Generating scripts for simulator: %s..."%simulator)

            if simulator == 'NEURON':
                self.project.neuronFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
                self.project.neuronSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
                self.project.neuronSettings.setGraphicsMode(0) # 0 hides graphs during execution

            if simulator.count('GENESIS')>0 or simulator.count('MOOSE')>0:
                self.project.genesisFileManager.setQuitAfterRun(1) # Remove this line to leave the NEURON sim windows open after finishing
                self.project.genesisSettings.setCopySimFiles(1) # 1 copies hoc/mod files to PySim_0 etc. and will allow multiple sims to run at once
                self.project.genesisSettings.setGraphicsMode(0) # 0 hides graphs during execution


            stimAmp = stimAmpLow

            simRefsVsStims = {}

            while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors

                ########  Adjusting the amplitude of the current clamp ###############

                stim = self.project.elecInputInfo.getStim(simConfig.getInputs().get(0))


                if stim.getElectricalInput().getType() != "IClamp":
                    raise Exception('Simulation config: '+ simConfigName+' has a non IClamp input: '+str(stim)+'!')

                if simConfig.getInputs()>1:
                    for stimIndex in range(1, simConfig.getInputs().size()):
                        stimOther = self.project.elecInputInfo.getStim(simConfig.getInputs().get(stimIndex))

                        if stimOther.getElectricalInput().getType() != "IClamp":
                            raise Exception('Simulation config: '+ simConfigName+' has a non IClamp input: '+str(stimOther)+'!')
                        else:
                            stimOther.setAmp(NumberGenerator(0))
                            stimOther.setDel(NumberGenerator(0))
                            stimOther.setDur(NumberGenerator(0))



                stim.setAmp(NumberGenerator(stimAmp))
                stim.setDel(NumberGenerator(stimDel))
                stim.setDur(NumberGenerator(stimDur))

                self.project.elecInputInfo.updateStim(stim)

                self.printver("Next stim: "+ str(stim))


                simRefs = self.runMultipleSims(simConfigs =              [simConfig.getName()],
                                               simulators =              [simulator],
                                               simDt =                   simDt,
                                               verboseSims =             verboseSims,
                                               runInBackground =         True,
                                               simRefGlobalPrefix =      simPrefix,
                                               simRefGlobalSuffix =      ("_"+str(float(stimAmp))),
                                               varTimestepNeuron =       varTimestepNeuron,
                                               mpiConfig =               mpiConfig,
                                               suggestedRemoteRunTime =  suggestedRemoteRunTime)

                simRefsVsStims[simRefs[0]] = stimAmp # should be just one simRef returned...

                stimAmp = stimAmp + stimAmpInc
                if abs(stimAmp) < stimAmpInc/1e9: stimAmp = 0

            while (len(self.allRunningSims)>0):
                self.printver("Waiting for all simulations to finish...")
                time.sleep(1) # wait a while...
                self.updateSimsRunning()

            self.generatePlotAnalisys(simulator,simConfigName,analyseStartTime,analyseStopTime,analyseThreshold,plotAllTraces,curveType,simRefsVsStims)


    def generatePlotAnalisys(self,
                        simulator,
                        simConfigName,
                        analyseStartTime,
                        analyseStopTime,
                        analyseThreshold,
                        plotAllTraces,
                        curveType,
                        simRefsVsStims):

        simConfig = self.project.simConfigInfo.getSimConfig(simConfigName)

        self.printver("Going to plot traces from recorded sims: %s"%str(simRefsVsStims))
        self.plotFrames[curveType] = PlotManager.getPlotterFrame(curveType+" curve from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, 1)

        self.plotFrames["Volts"] = PlotManager.getPlotterFrame("Voltage traces from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, plotAllTraces)

        self.plotFrames[curveType].setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)

        info = curveType+" curve for Simulation Configuration: "+str(simConfig)

        if (curveType == "F-I") :
            self.dataSets[curveType] = DataSet(info, info, "nA", "Hz", "Current injected", "Firing frequency")
        elif (curveType == "SS-I") :
            self.dataSets[curveType] = DataSet(info, info, "nA", "V", "Current injected", "Steady state Voltage")

        self.dataSets[curveType].setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)


        simList = simRefsVsStims.keys()
        simList.sort()

        for sim in simList:

            simDir = File(self.project.getProjectMainDirectory(), "/simulations/"+sim)
            self.printver("--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath())

            try:
                simData = SimulationData(simDir)
                simData.initialise()
                self.printver("Data loaded: ")
                self.printver(simData.getAllLoadedDataStores())

                times = simData.getAllTimes()
                cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"
                volts = simData.getVoltageAtAllTimes(cellSegmentRef)

                traceInfo = "Voltage at: %s in simulation: %s"%(cellSegmentRef, sim)

                self.dataSets["V"] = DataSet(traceInfo, traceInfo, "mV", "ms", "Membrane potential", "Time")
                for i in range(len(times)):
                    self.dataSets["V"].addPoint(times[i], volts[i])

                if plotAllTraces:
                    self.plotFrames["V"].addDataSet(self.dataSets["V"])

                if (curveType == "F-I") :
                    spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)
                    stimAmp = simRefsVsStims[sim]
                    self.printver("Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes)))

                    avgFreq = 0
                    if len(spikeTimes)>1:
                        avgFreq = len(spikeTimes)/ ((analyseStopTime - analyseStartTime)/1000.0)
                        self.dataSets["F-I"].addPoint(stimAmp,avgFreq)
                    else:
                        self.dataSets["F-I"].addPoint(stimAmp,0)

                elif (curveType == "SS-I") :
                    # check within analyseStartTime and analyseStopTime if we deviate by more than +/- analyseThreshold
                    steadyStateVoltageFound = False
                    stimAmp = simRefsVsStims[sim]
                    minVolt = 99999999
                    maxVolt = -99999999
                    for i in range(len(volts)) :
                        if times[i] >= analyseStartTime and times[i] <= analyseStopTime :
                            if steadyStateVoltageFound == False:
                                self.printver("Data start time found for SS-I")
                                minVolt = volts[i]
                                maxVolt = volts[i]
                                self.printver(" i:", i, " times_i:",times[i]," minVolt:",minVolt," maxVolt:",maxVolt," delta:",maxVolt - minVolt," threshold:",analyseThreshold)
                                steadyStateVoltageFound = True

                            if volts[i] < minVolt :
                                minVolt = volts[i]
                            elif volts[i] > maxVolt :
                                maxVolt = volts[i]

                            if (maxVolt - minVolt) > analyseThreshold :
                                self.printver("Data outside the threshold for steady state voltage, Error")
                                self.printver(" i:", i, " times_i:",times[i]," minVolt:",minVolt," maxVolt:",maxVolt," delta:",maxVolt - minVolt," threshold:",analyseThreshold)
                                steadyStateVoltageFound = False
                                break
                    if (steadyStateVoltageFound) :
                        midVoltage = (minVolt + maxVolt) / 2
                        self.dataSets["SS-I"].addPoint(stimAmp,midVoltage)


            except:
                self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
                self.printver(sys.exc_info()[0])

            self.plotFrames[curveType].addDataSet(self.dataSets[curveType])


