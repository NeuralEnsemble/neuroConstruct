import os.path

import os
import sys

import glob

dirs = ["./"]

args = sys.argv
justTime = False

if len(args) > 1 and sys.argv[1] == '-t':
    args = sys.argv[1:]
    justTime = True

if len(args) > 1:
    dirs = []
    for match in args[1:]:
        #print "match: %s"%match
        if match.endswith("/"): match = match[:-1]
        #print "Searching in folder(s) matching: %s"%match

        matched = glob.glob(match)
        #print matched
        for m in matched: dirs.append("./"+m+"/")


simulationInfoFilename = "simulation.props"



def getValue(line):

    if ">" in line:
        return line.split(">")[1].split("<")[0].strip()

    return line.split("=")[1].strip()

def getName(line):

    if 'key="' in line:
        return line.split('key="')[1].split('"')[0].strip()

    return line.split("=")[0].strip()


def timeInfo(secVal):
    time = float(secVal)
    if time<60:
        return "%g secs"% (time)
    elif time<3600:
        return "%g mins     (%g secs)"% (time/60.0, time)
    else:
        return "%g hours    (%g secs)"% (time/3600.0, time)

print

numProcs = []
times = []

for currDir in dirs:

    if len(dirs) > 1: print "------------------------------------------------------------------\n"

    if not os.path.isfile(currDir+simulationInfoFilename):
        print "  File: %s doesn't exist in %s! This doesn't seem to be a neuroConstruct simulation directory"%(simulationInfoFilename, currDir)
    else:

        simulationInfo = open(currDir+simulationInfoFilename, 'r')


        print "  Simulation:          %s\n"% os.path.basename(os.path.abspath(currDir))

        for line in simulationInfo:
            if '"dt"' in line and not justTime:
                print "  dt:                  %s ms"%getValue(line)
            if 'opulat' in line and not justTime:
                pops = getValue(line)
                popInfo = "  Populations:         "
                total = 0
                for pop in pops.split(";"):
                    if ":" in pop:
                        total += int(pop.split(":")[1].strip())
                        if not pops.startswith(pop):
                            popInfo = popInfo + "\n                      "
                        popInfo = popInfo +pop

                popInfo = popInfo + "\n                         Total cells:         %i"%total

                allFiles = os.listdir(currDir)

                netInfoFiles = ["NetworkConnections.dat", "ElectricalInputs.dat", "CellPositions.dat", "time.dat"]

                dataFilesFound = 0
                dataFilesSize = 0
                spikeFilesFound = 0
                spikeFilesSize = 0
                h5FilesFound = 0
                h5FilesSize = 0

                for file in allFiles:
                    if file.endswith(".dat") and file not in netInfoFiles:
                        dataFilesFound+=1
                        dataFilesSize += os.path.getsize(currDir+file)
                    elif file.endswith(".spike"):
                        spikeFilesFound+=1
                        spikeFilesSize += os.path.getsize(currDir+file)
                        #print "%f, tot %f"%(os.path.getsize(currDir+file),spikeFilesSize)
                    elif file.endswith(".h5") and not file == "Generated.net.h5":
                        h5FilesFound+=1
                        h5FilesSize += os.path.getsize(currDir+file)

                
                popInfo = popInfo + "\n                         Data files found:    %i, %i bytes"%(dataFilesFound, dataFilesSize)
                popInfo = popInfo + "\n                         Spike files found:   %i, %i bytes"%(spikeFilesFound, spikeFilesSize)
                popInfo = popInfo + "\n                         HDF5 files found:    %i, %i bytes"%(h5FilesFound, h5FilesSize)


                print popInfo
            if 'Duration' in line and not justTime:
                print "  Duration:            %s ms"%getValue(line)
            if 'Script format' in line and not justTime:
                print "  Script format:       %s"%getValue(line)
            if 'Net connections' in line and not justTime:
                print "  Net connections:     %s"%getValue(line)
            if '"Parallel configuration"' in line and not justTime:
                print "  Parallel config:     %s"%getValue(line)
            if 'caling' in line and not justTime:
                # e.g. gabaWeightScaling
                print "  %s:            %s"%(getName(line),getValue(line))

        if  not justTime: print
        simulatorInfoFilename = "simulator.props"

        if not os.path.isfile(currDir+simulatorInfoFilename):
            print "  File: %s doesn't exist. Simulation not yet completed..."% (currDir+simulatorInfoFilename)
        else:
            simulatorInfo = open(currDir+simulatorInfoFilename, 'r')

            for line in simulatorInfo:
                if 'RealSimulationTime' in line:
                    print "  Sim time:            %s"% timeInfo(getValue(line))
                    times.append(float(getValue(line)))
                if 'SimulationSetupTime' in line:
                    print "  Setup time:          %s"% timeInfo(getValue(line))
                if 'NumberHosts' in line:
                    print "  Number of hosts:     %s"% (getValue(line))
                    numProcs.append(int(getValue(line)))
                if 'Host=' in line and not justTime:
                    print "  Hosts:               %s"% (getValue(line))

    print

if justTime:
    numProcsO = []
    for np in numProcs:
        numProcsO.append(np)
    numProcsO.sort()
    timesO = []
    for np in numProcsO:
        timesO.append(times[numProcs.index(np)])
    print "numProcs = "+str(numProcsO)
    #print "numProcs = "+str(numProcs)
    print "times = "+str(timesO)

    