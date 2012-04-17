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



def getSimList(simConfigName,
               simRefGlobalPrefix,
               stimAmpLow,
               stimAmpHigh,
               stimAmpInc,
               simulatorUsed):

    simList = []
    stimAmp = stimAmpLow
    while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors
        simRefGlobalSuffix =      ("_"+str(float(stimAmp)))
        simList.append(simRefGlobalPrefix+simConfigName+"__"+simulatorUsed+simRefGlobalSuffix)

        stimAmp = stimAmp + stimAmpInc
        if abs(stimAmp) < stimAmpInc/1e9: stimAmp = 0

    return simList

def checkPullsims(simList,
                projectFileName):
    slashparts = projectFileName.split('/')
    basefolder = '/'.join(slashparts[:-1]) + '/'
    for sim in simList:
        projectFullDir = basefolder+"/simulations/"+sim
        pullFileName = projectFullDir+"/pullsim.sh"
        pullFile = File(basefolder, "simulations/"+sim+"/pullsim.sh")
        # and not self.tempSimConfig.getMpiConf().isRemotelyExecuted()
        if ( pullFile.exists() ) :
            print "Warning, found file from a previous parallel execution - please delete:"+pullFileName


argList = [ "commandLine", "DEBUG", "setVisible", "verboseSims", "simulator","ProjectFile", "simConfigName", "MpiSettings", "numConcurrentSims", "suggestedRemoteRunTime", "stimAmpLow", "stimAmpHigh","stimAmpInc","stimDel","stimDur","simDuration","startTime","stopTime","threshold", "curveType", "plotAllTraces", "outDirName" ]
argLookup = {}
argCount = 0
for arg in sys.argv:
    argLookup[argList[argCount]]=arg
    print '> ', argCount, argList[argCount], arg
    argCount+=1
    if argCount >= len(argList): break

# the DEBUG flag runs the graphs generation without computing data
DEBUG         = int(argLookup["DEBUG"])
setVisible    = int(argLookup["setVisible"])
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

mpiConfig           = eval(argLookup["MpiSettings"])
numConcurrentSims   = int(argLookup["numConcurrentSims"])

outDirName          = argLookup["outDirName"]

curveType           = argLookup["curveType"]
# note this used to be 'FI_' now will be 'F-I_'
simRefGlobalPrefix  = curveType+"_"

if (curveType <> "F-I" and curveType <> "SS-I") :
    print "Unknown curveType: stopping execution, can be either F-I or SS-I (for steady state subthreshold)"
    quit()

plotAllTraces       = argLookup["plotAllTraces"]


# Load neuroConstruct project whether given direct ref to file or relative to nConstruct home

if os.path.exists(argLookup["ProjectFile"]):
    projFile = File(argLookup["ProjectFile"])
elif os.path.exists(os.environ["NC_HOME"]+argLookup["ProjectFile"]):
    projFile = File(os.environ["NC_HOME"]+argLookup["ProjectFile"])
else:
    print "Critical error: bad argument for project file passed, please check input:",argLookup["ProjectFile"]
    quit()

#mpiConfig =            MpiSettings.LOCAL_SERIAL    # Default setting: run on one local processor
#mpiConfig =            MpiSettings.MATLEM_1PROC    # Run on one processor on UCL cluster


# suggestedRemoteRunTime = 33   # mins
suggestedRemoteRunTime = int(argLookup["suggestedRemoteRunTime"])

simList = getSimList(simConfigName,
                    simRefGlobalPrefix,
                    stimAmpLow,
                    stimAmpHigh,
                    stimAmpInc,
                    simulator[0])
    
if (DEBUG == 0):
    checkPullsims(simList, os.environ["NC_HOME"]+argLookup["ProjectFile"])
    simManager = nc.SimulationManager(projFile, numConcurrentSims)
    start = "\nSimulations started at: %s"%datetime.datetime.now()
    simManager.generateBatchCurve(simulator,
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
                           simPrefix        = simRefGlobalPrefix,
                           verboseSims      = verboseSims,
                           mpiConfig        = mpiConfig,
                           suggestedRemoteRunTime = suggestedRemoteRunTime,
                           curveType = curveType)
    finish = "Simulations finished at: %s\n"%datetime.datetime.now()
    print start
    print finish
else:
    print 'Running in DEBUG mode, no simulation run, argLookup["DEBUG"] :',DEBUG
#    quit()


# creating simManager and simConfig for later access to project details
simManager = nc.SimulationManager(projFile, numConcurrentSims)
simConfig = simManager.project.simConfigInfo.getSimConfig(simConfigName)

plotFrame           = PlotManager.getPlotterFrame("Plot for "+curveType, 0, setVisible)
plotFrameAlltraces  = PlotManager.getPlotterFrame("Plot All Traces", 0, setVisible)
plotFrame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)
plotFrameAlltraces.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW)


simList = getSimList(simConfigName,
                    simRefGlobalPrefix,
                    stimAmpLow,
                    stimAmpHigh,
                    stimAmpInc,
                    simulator[0])
print simList

#simList = [u'FI_Cell6-spinstell-FigA3-333__N_0.0',u'FI_Cell6-spinstell-FigA3-333__N_0.5', u'FI_Cell6-spinstell-FigA3-333__N_1.0']
#simList = ['SS-I_Cell6-spinstell-FigA3-333__N_-1.0', 'SS-I_Cell6-spinstell-FigA3-333__N_-2.0', 'SS-I_Cell6-spinstell-FigA3-333__N_0.0']


# initialize vars
dataSet = ""
i00 = 0
stimAmp = stimAmpLow
simRefsVsStims = {}
while (stimAmp - stimAmpHigh) < (stimAmpInc/1e9): # to avoid floating point errors
    # print " stimAmp:",stimAmp," stimAmpInc:",stimAmpInc," stimAmp - stimAmpHigh:",stimAmp - stimAmpHigh," i00:",i00
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

        dataSetV = DataSet(traceInfo, traceInfo, "ms", "mV", "Time", "Membrane potential")
        for i in range(len(times)):
            dataSetV.addPoint(times[i], volts[i])

        if plotAllTraces:
            plotFrameAlltraces.addDataSet(dataSetV)
            # needs adjusting for a comprehensive maxx maxy minx miny
            plotFrameAlltraces.setMaxMinScaleValues(dataSetV.getMaxX()[0],dataSetV.getMinX()[0],dataSetV.getMaxY()[1],dataSetV.getMinY()[1])

        if (curveType == "F-I") :
            print "F-I analisys..."
            # initialize the dataSet for the graph
            if (dataSet == "") :
                dataSet = DataSet("info 1", "info 2", "nA", "Hz", "Current injected", "Firing frequency")
                dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)
            spikeTimes = SpikeAnalyser.getSpikeTimes(volts, times, analyseThreshold, analyseStartTime, analyseStopTime)
            stimAmp = simRefsVsStims[sim]
            print("Number of spikes at %f nA in sim %s: %i"%(stimAmp, sim, len(spikeTimes)))
            avgFreq = 0
            if len(spikeTimes)>1:
                avgFreq = len(spikeTimes) / ((analyseStopTime - analyseStartTime)/1000.0)
                dataSet.addPoint(stimAmp,avgFreq)
            else:
                dataSet.addPoint(stimAmp,0)

        elif (curveType == "SS-I") :
            # initialize the dataSet for the graph
            if (dataSet == "") :
                dataSet = DataSet("info 1", "info 2", "nA", "mV", "Current injected", "Steady state Voltage")
                dataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT)
            print "SS-I analisys..."
            stimAmp = simRefsVsStims[sim]
            steadyStateVoltageFound = False
            minVolt = 99999999
            maxVolt = -99999999
            for i in range(len(volts)) :
                if times[i] >= analyseStartTime and times[i] <= analyseStopTime :
                    if steadyStateVoltageFound == False:
                        print("Data start time found for SS-I")
                        minVolt = volts[i]
                        maxVolt = volts[i]
                        print(" i:", i, " times_i:",times[i]," minVolt:",minVolt," maxVolt:",maxVolt," delta:",maxVolt - minVolt," threshold:",analyseThreshold)
                        steadyStateVoltageFound = True

                    if volts[i] < minVolt :
                        minVolt = volts[i]
                    elif volts[i] > maxVolt :
                        maxVolt = volts[i]

                    if (maxVolt - minVolt) > analyseThreshold :
                        print("Data outside the threshold for steady state voltage, Error")
                        print(" i:", i, " times_i:",times[i]," minVolt:",minVolt," maxVolt:",maxVolt," delta:",maxVolt - minVolt," threshold:",analyseThreshold)
                        steadyStateVoltageFound = False
                        break
            if (steadyStateVoltageFound) :
                midVoltage = (minVolt + maxVolt) / 2
                dataSet.addPoint(stimAmp,midVoltage)

    except:
        print("Error analysing simulation data from: %s"%simDir.getCanonicalPath())
        print(sys.exc_info()[0])
        raise


plotFrame.addDataSet(dataSet)
# 10% space around the main data points
plotHandleX = ( dataSet.getMaxX()[0] - dataSet.getMinX()[0] )/10
plotHandleY = ( dataSet.getMaxY()[1] - dataSet.getMinY()[1] )/10
plotFrame.setMaxMinScaleValues(dataSet.getMaxX()[0]+plotHandleX,dataSet.getMinX()[0]-plotHandleX,dataSet.getMaxY()[1]+plotHandleY,dataSet.getMinY()[1]-plotHandleY)

print("dataSet.getMaxX()[0]: ",dataSet.getMaxX()[0],"dataSet.getMinX()[0]: ",dataSet.getMinX()[0],"plotHandleX: ",plotHandleX, \
    "dataSet.getMaxY()[1]", dataSet.getMaxY()[1], "dataSet.getMinY()[1]: ", dataSet.getMinY()[1], "plotHandleY: ", plotHandleY)

pageTitle = ""
if (outDirName <> "none"):
    basefolderMatplotlib = basefolder+"/simulations/"+outDirName+"/"
    pageTitle = outDirName
else :
    # if setMapplotlibDir is set then no file chooser should show
    nowdatetime = datetime.datetime.now()
    timestamp = nowdatetime.strftime("_%Y%m%d-%H%M%S")
    basefolderMatplotlib = basefolder+"/simulations/"+"render_output_folder"+timestamp+"/"
    pageTitle = simConfigName+":"+timestamp

plotFrame.setMatplotlibDir(basefolderMatplotlib)
plotFrame.setMatplotlibTitle(pageTitle)

# maybe should clear the set folder after run
plotFrame.generateMatplotlib()
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
Popen(["python",basefolderMatplotlib+"generateEps.py","-noshow"], cwd=basefolderMatplotlib)

#
# - should consider a more advanced templating structure?
# - need to warn if the project is already there.. or add an overwrite flag?

expander = Expand()
expanderPageHtml = expander.generateSimplePage(pageTitle,"plot.png")

expanderPageFile = open(basefolderMatplotlib+'plot.html','w')
expanderPageFile.write(expanderPageHtml)
expanderPageFile.close()
# generateSimplePage

pageTitleAlltraces = ""
if plotAllTraces:
    if (outDirName <> "none"):
        basefolderMatplotlibAlltraces = basefolder+"/simulations/"+outDirName+"_Alltraces/"
        pageTitleAlltraces = outDirName
    else :
        basefolderMatplotlibAlltraces = basefolder+"/simulations/"+"render_output_folder"+timestamp+"_Alltraces/"
        pageTitleAlltraces = simConfigName+"_Alltraces:"+timestamp

    plotFrameAlltraces.setMatplotlibDir(basefolderMatplotlibAlltraces)
    plotFrameAlltraces.setMatplotlibTitle(pageTitleAlltraces)
    plotFrameAlltraces.generateMatplotlib()
    while (not os.path.exists(basefolderMatplotlibAlltraces+"generateEps.py")):
        print("Checking if Matplotlib is done generating alltraces...")
        print("File: "+basefolderMatplotlibAlltraces+"generateEps.py"+" doesn't exist..")
        time.sleep(1) # wait a while...
    print basefolderMatplotlibAlltraces+"generateEps.py"
    Popen(["python",basefolderMatplotlibAlltraces+"generateEps.py","-noshow"], cwd=basefolderMatplotlibAlltraces)
    expanderAlltraces = Expand()
    expanderPageHtmlAlltraces = expanderAlltraces.generateSimplePage(pageTitleAlltraces,"plot.png")
    expanderPageFileAlltraces = open(basefolderMatplotlibAlltraces+'plot.html','w')
    expanderPageFileAlltraces.write(expanderPageHtmlAlltraces)
    expanderPageFileAlltraces.close()


quit()

