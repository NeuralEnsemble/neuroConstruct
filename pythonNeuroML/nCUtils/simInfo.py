
import os

simulationInfoFilename = "simulation.props"

def getValue(line):

    if ">" in line:
        return line.split(">")[1].split("<")[0].strip()

    return line.split("=")[1].strip()

print

if not os.path.isfile(simulationInfoFilename):
    print "  File: %s doesn't exist!"%simulationInfoFilename
else:

    simulationInfo = open(simulationInfoFilename, 'r')

    for line in simulationInfo:
        if '"dt"' in line:
            print "  dt:                  %s ms"%getValue(line)
        if 'opulat' in line:
            pops = getValue(line)
            popInfo = "  Populations:         "
            total = 0
            for pop in pops.split(";"):
                total += int(pop.split(":")[1].strip())
                if not pops.startswith(pop):
                    popInfo = popInfo + "\n                      "
                popInfo = popInfo +pop

            popInfo = popInfo + "\n                         Total: %i"%total

            print popInfo
        if 'Duration' in line:
            print "  Duration:            %s ms"%getValue(line)
        if '"Parallel configuration"' in line:
            print "  Parallel config:     %s"%getValue(line)

    print
    simulatorInfoFilename = "simulator.props"

    if not os.path.isfile(simulatorInfoFilename):
        print "  File: %s doesn't exist. Simulation not yet completed..."%simulatorInfoFilename
    else:
        simulatorInfo = open(simulatorInfoFilename, 'r')

        for line in simulatorInfo:
            if 'RealSimulationTime' in line:
                 time = float(getValue(line))/60
                 print "  Sim time:            %g mins"% (time)
            if 'NumberHosts' in line:
                print "  Number of hosts:     %s"% (getValue(line))
            if 'Host=' in line:
                print "  Hosts:               %s"% (getValue(line))

print
        