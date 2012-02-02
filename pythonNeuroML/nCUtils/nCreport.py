#
#
#   A file which generates a frequency vs current curve for cell in Thalamocortical project
#
#   Author: Padraig Gleeson, Yates Buckley
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#
import sys
import os
import time
from subprocess import Popen

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

from ucl.physiol.neuroconstruct.gui.plotter import PlotManager
from ucl.physiol.neuroconstruct.gui.plotter import PlotCanvas
# from ucl.physiol.neuroconstruct.gui.plotter import PlotterFrame

from ucl.physiol.neuroconstruct.dataset import DataSet
from ucl.physiol.neuroconstruct.simulation import SimulationData
from ucl.physiol.neuroconstruct.simulation import SpikeAnalyser
from ucl.physiol.neuroconstruct.project import Expand


argList = [ "commandLine", "DEBUG", "setVisible", "verboseSims", "simulator","ProjectFile", "simConfigName", "MpiSettings", "numConcurrentSims", "suggestedRemoteRunTime", "stimAmpLow", "stimAmpHigh","stimAmpInc","stimDel","stimDur","simDuration","startTime","stopTime","threshold" ]
argLookup = {}
argCount = 0
for arg in sys.argv:
    argLookup[argList[argCount]]=arg
    print '> ', argCount, argList[argCount], arg
    argCount+=1
    if argCount >= len(argList): break

setVisible   = int(argLookup["setVisible"])
verboseSims   = argLookup["verboseSims"]
simulator     = argLookup["simulator"]
simConfigName = argLookup["simConfigName"]
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
    simManager.generateFICurve(simulator,
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
                           simPrefix        = 'FI_',
                           verboseSims      = verboseSims,
                           mpiConfig        = mpiConfig,
                           suggestedRemoteRunTime = suggestedRemoteRunTime)
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
#no                        simDt =                   None,
#no                        simPrefix =               'FI_',
#no                        neuroConstructSeed =      1234,
#no                        plotAllTraces =           False,
#added                        verboseSims =             True,
#no                        varTimestepNeuron =       None,
#                        mpiConfig =               MpiSettings.LOCAL_SERIAL,
#                        suggestedRemoteRunTime =  -1):
#
# creating simManager and simConfig for later access to project details
simManager = nc.SimulationManager(projFile, int(argLookup["numConcurrentSims"]))
simConfig = simManager.project.simConfigInfo.getSimConfig(simConfigName)

plotFrameFI = PlotManager.getPlotterFrame("Plot Title Entry", 0, setVisible)
# plotFrameFI.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)

dataSet = DataSet("info 1", "info 2", "nA", "Hz", "Current injected", "Firing frequency")
dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)

simList = []

stimAmp = stimAmpLow
while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors
    simRefGlobalPrefix =      'FI_'
    simRefGlobalSuffix =      ("_"+str(float(stimAmp)))
    simList.append(simRefGlobalPrefix+simConfigName+"__"+simulator[0]+simRefGlobalSuffix)

    stimAmp = stimAmp + stimAmpInc
    if abs(stimAmp) < stimAmpInc/1e9: stimAmp = 0

simList.sort()
print simList

#simList = [u'FI_Cell6-spinstell-FigA3-333__N_0.0',u'FI_Cell6-spinstell-FigA3-333__N_0.5', u'FI_Cell6-spinstell-FigA3-333__N_1.0']

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
    projectFullDir = basefolder+"/simulations/"+sim
    if not os.path.exists(projectFullDir):
        print("--- project not found, directory missing %s ---"%projectFullDir)
        quit()

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
          avgFreq = len(spikeTimes) / ((analyseStopTime - analyseStartTime)/1000.0)
          dataSet.addPoint(stimAmp,avgFreq)
      else:
          dataSet.addPoint(stimAmp,0)

    except:
        print("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
        print(sys.exc_info()[0])
        raise


plotFrameFI.addDataSet(dataSet)
#plotFrameFI.setMaxMinScaleValues(2,0,100,0)

# if setMapplotlibDir is set then no file chooser should show
nowdatetime = datetime.datetime.now()
timestamp = nowdatetime.strftime("_%Y%m%d-%H%M%S")

basefolderMatplotlib = basefolder+"/simulations/"+"render_output_folder"+timestamp+"/"
plotFrameFI.setMatplotlibDir(basefolderMatplotlib)
# maybe should clear the set folder after run
plotFrameFI.generateMatplotlib()
#
while (not os.path.exists(basefolderMatplotlib+"generateEps.py")):
    print("Checking if Matplotlib is done generating...")
    print("File: "+basefolderMatplotlib+"generateEps.py"+" doesn't exist..")
    time.sleep(1) # wait a while...
#
# - fork a process to run the python transformation of the main output to a file
#   http://www.jython.org/docs/library/subprocess.html
#   http://www.jython.org/docs/library/cmd.html
#

print basefolderMatplotlib+"generateEps.py"
Popen(["python",basefolderMatplotlib+"generateEps.py"], cwd=basefolderMatplotlib)

#
# - should consider a more advanced templating structure?
# - need to warn if the project is already there.. or add an overwrite flag?

expander = Expand()
expanderPageHtml = expander.generateSimplePage("Page Title","plot.png")

expanderPageFile = open(basefolderMatplotlib+'plot.html','w')
expanderPageFile.write(expanderPageHtml)
expanderPageFile.close()
# generateSimplePage