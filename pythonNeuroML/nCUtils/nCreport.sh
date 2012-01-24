#
#    NOTE: work in progress...
#
#    a script to help automating generation of data from arbitrary project
#
#    make sure to run with no arguments to get a sample command line use
#

if [ $# -eq 0 ] ; then
    echo "Usage: $0 DEBUG setVisible verboseSims simulator ProjectFile SimConfigName MpiSettings numConcurrentSims suggestedRemoteRunTime stimAmpLow stimAmpHigh stimAmpInc stimDel stimDur stimDuration startTime stopTime threshold"
    echo "Eg: $0 1 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 -0.2 1.0 0.05 0 2000 2000 500 2000 -20"
    echo "Eg: $0 1 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 0.0 1.0 0.5 0 2000 2000 500 2000 -20"
    exit 1
fi

../../nC.sh -python nCreport.py $*
