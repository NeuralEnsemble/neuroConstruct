#
#
#   A file which generates a frequency vs current curve for cell in Thalamocortical project
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

import sys
import os
import datetime

try:
	from java.io import File
except ImportError:
	print "Note: this file should be run using ..\\..\\..\\nC.bat -python XXX.py' or '../../../nC.sh -python XXX.py'"
	print "See http://www.neuroconstruct.org/docs/python.html for more details"
	quit()

from math import *

sys.path.append(os.environ["NC_HOME"]+"/pythonNeuroML/nCUtils")

import ncutils as nc
from ucl.physiol.neuroconstruct.hpc.mpi import MpiSettings

argList = [ "commandLine", "DEBUG", "ProjectFile", "simConfigName", "MpiSettings", "numConcurrentSims", "suggestedRemoteRunTime", "stimAmpLow", "stimAmpHigh","stimAmpInc","stimDel","stimDur","simDuration","startTime","stopTime","threshold" ]
argLookup = {}
argCount = 0
for arg in sys.argv:
    argLookup[argList[argCount]]=arg
    print '> ', argCount, argList[argCount], arg
    argCount+=1
    if argCount >= len(argList): break

simConfigName   = argLookup["simConfigName"]
stimAmpLow  = float(argLookup["stimAmpLow"])
stimAmpInc  = float(argLookup["stimAmpInc"])
stimAmpHigh = float(argLookup["stimAmpHigh"])
stimDel     = float(argLookup["stimDel"])
stimDur     = float(argLookup["stimDur"])
simDuration = float(argLookup["simDuration"])

analyseStartTime    = float(argLookup["startTime"]) # So it's firing at a steady rate...
analyseStopTime     = float(argLookup["stopTime"])
analyseThreshold    = float(argLookup["threshold"]) # mV

mpiConfig = eval(argLookup["MpiSettings"])

#mpiConfig =            MpiSettings.LOCAL_SERIAL    # Default setting: run on one local processor
#mpiConfig =            MpiSettings.MATLEM_1PROC    # Run on one processor on UCL cluster


# logic tied to concurrent sims...
#numConcurrentSims = 4
#if mpiConfig != MpiSettings.LOCAL_SERIAL: numConcurrentSims = 30

# suggestedRemoteRunTime = 33   # mins
suggestedRemoteRunTime = int(argLookup["suggestedRemoteRunTime"])

# Load neuroConstruct project whether given direct ref to file or relative to nConstruct home

if os.path.exists(argLookup["ProjectFile"]):
    projFile = File(argLookup["ProjectFile"])
elif os.path.exists(os.environ["NC_HOME"]+argLookup["ProjectFile"]):
    projFile = File(os.environ["NC_HOME"]+argLookup["ProjectFile"])
else:
    print "Critical error: bad argument for project file passed, please check input:",argLookup["ProjectFile"]
    quit()

if int(argLookup["DEBUG"]) == 0:
    simManager = nc.SimulationManager(projFile, int(argLookup["numConcurrentSims"]))
    start = "\nSimulations started at: %s"%datetime.datetime.now()
    simManager.generateFICurve("NEURON",
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
                           mpiConfig =                mpiConfig,
                           suggestedRemoteRunTime =   suggestedRemoteRunTime)
    finish = "Simulations finished at: %s\n"%datetime.datetime.now()
    print start
    print finish
else:
    print 'Running in DEBUG mode, no simulation run, argLookup["DEBUG"] :',argLookup["DEBUG"]
#    quit()


#
# note not all parameters are passed
#
# def generateFICurve(self,
#                        simulator,
#                        simConfigName,
#                        stimAmpLow,
#                        stimAmpInc,
#                        stimAmpHigh,
#                        stimDel,
#                        stimDur,
#                        simDuration,
#                        analyseStartTime,
#                        analyseStopTime,
#                        analyseThreshold,
#                        simDt =                   None,
#no                        simPrefix =               'FI_',
#no                        neuroConstructSeed =      1234,
#no                        plotAllTraces =           False,
#no                        verboseSims =             True,
#no                        varTimestepNeuron =       None,
#                        mpiConfig =               MpiSettings.LOCAL_SERIAL,
#                        suggestedRemoteRunTime =  -1):

from ucl.physiol.neuroconstruct.gui.plotter import PlotManager
from ucl.physiol.neuroconstruct.gui.plotter import PlotCanvas
from ucl.physiol.neuroconstruct.dataset import DataSet
from ucl.physiol.neuroconstruct.simulation import SimulationData
from ucl.physiol.neuroconstruct.simulation import SpikeAnalyser

# creating simManager and simConfig for later access to project details
simManager = nc.SimulationManager(projFile, int(argLookup["numConcurrentSims"]))
simConfig = simManager.project.simConfigInfo.getSimConfig(simConfigName)

plotFrameFI = PlotManager.getPlotterFrame("Fnordly Fnord!", 0, 1)
plotFrameFI.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)

dataSet = DataSet("info 1", "info 2", "nA", "Hz", "Current injected", "Firing frequency")
dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)

simList = [u'FI_Cell6-spinstell-FigA3-333__N_0.5', u'FI_Cell6-spinstell-FigA3-333__N_1.0', u'FI_Cell6-spinstell-FigA3-333__N_0.0']

# get the x-values for current amp
i00 = 0
stimAmp = stimAmpLow
simRefsVsStims = {}
while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors
    simRefsVsStims[simList[i00]] = stimAmp
    stimAmp = stimAmp + stimAmpInc
    i00+=1
    if abs(stimAmp) < stimAmpInc/1e9: stimAmp = 0

projectFileName = os.environ["NC_HOME"]+argLookup["ProjectFile"]
slashparts = projectFileName.split('/')
basefolder = '/'.join(slashparts[:-1]) + '/'
print basefolder
for sim in simList:
    simDir = File(basefolder, "/simulations/"+sim)
    print("--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath())

    try:
      simData = SimulationData(simDir)
      simData.initialise()
      print("Data loaded: ")
      print(simData.getAllLoadedDataStores())

      times = simData.getAllTimes()
      # simConfig was just grabbed for use here, it was CGspinstell_0
      cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"

      volts = simData.getVoltageAtAllTimes(cellSegmentRef)

      traceInfo = "Voltage at: %s in simulation: %s"%(cellSegmentRef, sim)

      dataSetV = DataSet(traceInfo, traceInfo, "mV", "ms", "Membrane potential", "Time")
      for i in range(len(times)):
          dataSetV.addPoint(times[i], volts[i])

#      if plotAllTraces:
#        plotFrameVolts.addDataSet(dataSetV)

      spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)

      # {u'FI_Cell6-spinstell-FigA3-333__N_0.5': 0.0, u'FI_Cell6-spinstell-FigA3-333__N_0.0': 1.0, u'FI_Cell6-spinstell-FigA3-333__N_1.0': 0.5}
      stimAmp = simRefsVsStims[sim]
      print("Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes)))

      avgFreq = 0
      if len(spikeTimes)>1:
          avgFreq = len(spikeTimes)/ ((analyseStopTime - analyseStartTime)/1000.0)
          dataSet.addPoint(stimAmp,avgFreq)
      else:
          dataSet.addPoint(stimAmp,0)

    except:
        print("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
        print(sys.exc_info()[0])
        raise


plotFrameFI.addDataSet(dataSet)




#            plotFrameFI = PlotManager.getPlotterFrame("F-I curve from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, 1)
#            plotFrameVolts = PlotManager.getPlotterFrame("Voltage traces from project: "+str(self.project.getProjectFile())+" on "+simulator , 0, plotAllTraces)
#            plotFrameFI.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)
#            info = "F-I curve for Simulation Configuration: "+str(simConfig)
#            dataSet = DataSet(info, info, "nA", "Hz", "Current injected", "Firing frequency")
#            dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)
#            simList = simRefsVsStims.keys()
#            simList.sort()
#
# MAZINGA - simList = [u'FI_Cell6-spinstell-FigA3-333__N_0.5', u'FI_Cell6-spinstell-FigA3-333__N_1.0', u'FI_Cell6-spinstell-FigA3-333__N_0.0']
#
#            for sim in simList:
#              simDir = File(self.project.getProjectMainDirectory(), "/simulations/"+sim)
#              self.printver("--- Reloading data from simulation in directory: %s"%simDir.getCanonicalPath())
#
#              try:
#                  simData = SimulationData(simDir)
#                  simData.initialise()
#                  self.printver("Data loaded: ")
#                  self.printver(simData.getAllLoadedDataStores())
#
#                  times = simData.getAllTimes()
#                  cellSegmentRef = simConfig.getCellGroups().get(0)+"_0"
#                  volts = simData.getVoltageAtAllTimes(cellSegmentRef)
#
#                  traceInfo = "Voltage at: %s in simulation: %s"%(cellSegmentRef, sim)
#
#                  dataSetV = DataSet(traceInfo, traceInfo, "mV", "ms", "Membrane potential", "Time")
#                  for i in range(len(times)):
#                      dataSetV.addPoint(times[i], volts[i])
#
#                  if plotAllTraces:
#                    plotFrameVolts.addDataSet(dataSetV)
#
#                  spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)
#                  stimAmp = simRefsVsStims[sim]
#                  self.printver("Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes)))
#
#                  avgFreq = 0
#                  if len(spikeTimes)>1:
#                      avgFreq = len(spikeTimes)/ ((analyseStopTime - analyseStartTime)/1000.0)
#                      dataSet.addPoint(stimAmp,avgFreq)
#                  else:
#                      dataSet.addPoint(stimAmp,0)
#
#              except:
#                  self.printver("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
#                  self.printver(sys.exc_info()[0])
#
#
#            plotFrameFI.addDataSet(dataSet)
#
