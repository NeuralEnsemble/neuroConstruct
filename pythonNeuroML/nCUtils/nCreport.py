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
#import os.path

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
    if int(argLookup["DEBUG"]) == 1:
        print '> ', argCount, argList[argCount], arg
    argCount+=1
    if argCount >= len(argList): break

simConfig   = argLookup["simConfigName"]
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

simManager = nc.SimulationManager(projFile,
                                  int(argLookup["numConcurrentSims"]))


start = "\nSimulations started at: %s"%datetime.datetime.now()

if int(argLookup["DEBUG"]) == 0:
    simManager.generateFICurve("NEURON",
                           simConfig,
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
else:
    print 'Running in DEBUG mode, no simulation run, argLookup["DEBUG"] :',argLookup["DEBUG"]

finish = "Simulations finished at: %s\n"%datetime.datetime.now()

print start
print finish

quit()

