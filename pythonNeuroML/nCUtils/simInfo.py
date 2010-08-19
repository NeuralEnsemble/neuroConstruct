
import os

simulationInfoFilename = "simulation.props"

def getValue(line):

    if ">" in line:
        return line.partition(">")[2].partition("<")[0]

    return line.partition("=")[2].strip()

print

if not os.path.isfile(simulationInfoFilename):
    print "%s doesn't exist!"%simulationInfoFilename
    exit()
else:

    simulationInfo = open(simulationInfoFilename, 'r')

    for line in simulationInfo:
        if '"dt"' in line:
            print "dt:                  %s ms"%getValue(line)
        if 'opulat' in line:
            print "Populations:         %s"%getValue(line)
        if 'Duration' in line:
            print "Duration:            %s ms"%getValue(line)

    print
    simulatorInfoFilename = "simulator.props"

    if not os.path.isfile(simulatorInfoFilename):
        print "%s doesn't exist. Simulation not yet completed..."%simulatorInfoFilename
        exit()
    else:
        simulatorInfo = open(simulatorInfoFilename, 'r')

        for line in simulatorInfo:
            if 'RealSimulationTime' in line:
                 time = float(getValue(line))/60
                 print "Sim time:            %g mins"% (time)
            if 'NumberHosts' in line:
                print "Number of hosts:     %s"% (getValue(line))
            if 'Host=' in line:
                print "Hosts:               %s"% (getValue(line))



print
        