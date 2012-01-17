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

argList = [ "commandLine", "ProjectFile", "simConfigName", "MpiSettings", "numConcurrentSims", "suggestedRemoteRunTime", "stimAmpLow", "stimAmpHigh","stimAmpInc","stimDel","stimDur","simDuration","startTime","stopTime","threshold" ]
argLookup = {}
argCount = 0
for arg in sys.argv:
    argLookup[argList[argCount]]=arg
    print '> ', argCount, argList[argCount], arg
    argCount+=1
    if argCount >= len(argList): break

simConfig   = argLookup["simConfigName"]
stimAmpLow  = argLookup["stimAmpLow"]
stimAmpInc  = argLookup["stimAmpInc"]
stimAmpHigh = argLookup["stimAmpHigh"]
stimDel     = argLookup["stimDel"]
stimDur     = argLookup["stimDur"]
simDuration = argLookup["simDuration"]

analyseStartTime    = argLookup["startTime"] # So it's firing at a steady rate...
analyseStopTime     = argLookup["stopTime"]
analyseThreshold    = argLookup["threshold"] # mV

mpiConfig = eval(argLookup["MpiSettings"])

#mpiConfig =            MpiSettings.LOCAL_SERIAL    # Default setting: run on one local processor
#mpiConfig =            MpiSettings.MATLEM_1PROC    # Run on one processor on UCL cluster


# logic tied to concurrent sims...
#numConcurrentSims = 4
#if mpiConfig != MpiSettings.LOCAL_SERIAL: numConcurrentSims = 30

# suggestedRemoteRunTime = 33   # mins
suggestedRemoteRunTime = argLookup["suggestedRemoteRunTime"]

# Load neuroConstruct project

# projFile = File(os.environ["NC_HOME"]+"/nCmodels/Thalamocortical/Thalamocortical.ncx")
# --- wouldn't it be more natural to supply full path to the project file?
projFile = File(os.environ["NC_HOME"]+argLookup["ProjectFile"])

simManager = nc.SimulationManager(projFile,
                                  argLookup["numConcurrentSims"])


start = "\nSimulations started at: %s"%datetime.datetime.now()

#simManager.generateFICurve("NEURON",
#                           simConfig,
#                           stimAmpLow,
#                           stimAmpInc,
#                           stimAmpHigh,
#                           stimDel,
#                           stimDur,
#                           simDuration,
#                           analyseStartTime,
#                           analyseStopTime,
#                           analyseThreshold,
#                           mpiConfig =                mpiConfig,
#                           suggestedRemoteRunTime =   suggestedRemoteRunTime)

finish = "Simulations finished at: %s\n"%datetime.datetime.now()

print start
print finish

quit()

