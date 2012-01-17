#
#    A convenient script for plotting data files, etc. using the neuroConstruct
#    graphing window. Can be used with any file format which can be imported into
#    neuroConstruct via Tools -> Import Data for Plot, i.e. time series data in
#    one or more columns
#
#    Might be useful to associate some file types with nCplot.bat, e,g, *.dat so
#    That double clicking on them plots them
#

#? was ~/neuroConstruct/nC.sh -python nCreport.py $*

# + projectName
#
#simManager.generateFICurve("NEURON",
#                           simConfig,   -
#                           stimAmpLow,  -
#                           stimAmpInc,  -
#                           stimAmpHigh, -
#                           stimDel,
#                           stimDur,
#                           simDuration,
#                           analyseStartTime,
#                           analyseStopTime,
#                           analyseThreshold,
#                           mpiConfig =                mpiConfig,
#                           suggestedRemoteRunTime =   suggestedRemoteRunTime)
#
# analyseStartTime = stimDel + 500 # So it's firing at a steady rate...
# analyseStopTime = simDuration
# analyseThreshold = -20 # mV


if [ $# -eq 0 ] ; then
    echo "Usage: $0 ProjectFile SimConfigName MpiSettings numConcurrentSims suggestedRemoteRunTime stimAmpLow stimAmpHigh stimAmpInc stimDel stimDur stimDuration startTime stopTime threshold"
    echo "Eg: $0 \"nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 -0.2 1.0 0.05 0 2000 2000 500 2000 -20"
    exit 1
fi

./nC.sh -python nCreport.py $*

