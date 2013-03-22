#
#    NOTE: work in progress...
#
#    a script to help automating generation of data from arbitrary project
#
#    make sure to run with no arguments to get a sample command line use
#

if [ $# -eq 0 ] ; then
    echo "Usage: $0 DEBUG setVisible verboseSims simulator ProjectFile SimConfigName MpiSettings numConcurrentSims suggestedRemoteRunTime stimAmpLow stimAmpHigh stimAmpInc stimDel stimDur simDuration startTime stopTime threshold curveType plotAllTraces outDirName"
    echo -e "Main Curve F-I:\n"
    echo -e "\nEg (parallel full):\n $0 0 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 -0.2 1.0 0.05 0 2000 2000 500 2000 -20 F-I 0 \"none\""
    echo -e "\nEg (parallel simple debug):\n $0 1 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 0.0 1.0 0.5 0 2000 2000 500 2000 -20 F-I 0 \"none\""
    echo -e "\nEg (serial simple):\n $0 0 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.LOCAL_SERIAL\" 1 33 0.0 1.0 0.5 0 2000 2000 500 2000 -20 F-I 0 \"none\""
    echo -e "\nEg (serial simple debug):\n $0 1 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.LOCAL_SERIAL\" 1 33 0.0 1.0 0.5 0 2000 2000 500 2000 -20 F-I 0 \"none\""
    echo -e "Alternate Curve SS-I:\n"
    echo -e "\nEg (parallel full):\n $0 0 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 -2.0 0.0 0.05 0 2000 2000 1000 2000 2 SS-I 0 \"none\""
    echo -e "\nEg (serial simple):\n $0 0 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.LOCAL_SERIAL\" 1 33 -2.0 0.0 1.0 0 2000 2000 1000 2000 2 SS-I 0 \"none\""
    echo -e "\nEg (serial simple debug):\n $0 1 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.LOCAL_SERIAL\" 1 33 -2.0 0.0 1.0 0 2000 2000 1000 2000 2 SS-I 0 \"none\""
    echo -e "\n"
    echo -e "To set output directory name:\n"
    echo -e "\nEg (parallel full):\n $0 0 1 1 \"NEURON\" \"/nCmodels/Thalamocortical/Thalamocortical.ncx\" \"Cell6-spinstell-FigA3-333\" \"MpiSettings.MATLEM_1PROC\" 4 33 -2.0 0.0 0.05 0 2000 2000 1000 2000 2 SS-I 0 \"Cell6-spinstell_SS-I\" "
    exit 1
fi

../../nC.sh -python nCreport.py $*
#

