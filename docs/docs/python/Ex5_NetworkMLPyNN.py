#
#   A simple example of reading in a NetworkML file and instantiating cells and
#   connections using PyNN
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#


import sys
import xml

import logging
import time
 
sys.path.append("../NeuroMLUtils")
sys.path.append("../PyNNUtils")

# Lines from standard PyNN examples
if hasattr(sys,"argv"):     # run using python
	my_simulator = sys.argv[-1]
else:
    my_simulator = "neuron2"    # run using nrngui -python
	

logging.basicConfig(level=logging.INFO, format="%(name)-19s %(levelname)-5s - %(message)s")

from NetworkMLSaxHandler import NetworkMLSaxHandler
from PyNNUtils import NetManagerPyNN

exec("from pyNN.%s import *" % my_simulator)

f = open("my_simulator", mode='w')
f.write(my_simulator)
f.close()


startTime = time.time()

dt = 0.01
tstop = 200.0
seed = 123456

setup(timestep=dt, debug=False)

file_name = 'small_pynn.nml'


print("Going to read contents of a NetworkML file: "+str(file_name))


parser = xml.sax.make_parser()   # A parser for any XML file

nmlHandler = NetManagerPyNN(my_simulator)	# Stores (most of) the network structure

nmlHandler.setSeed(seed)

nmlHandler.setMaxSimLength(tstop)

curHandler = NetworkMLSaxHandler(nmlHandler) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetworkHandler

curHandler.setNodeId(-1) 	# Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0

parser.setContentHandler(curHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed

parser.parse(open(file_name)) # The parser opens the file and ultimately the appropriate functions in NetworkHandler get called


print("Have read in contents of file: "+str(file_name))

for popName in nmlHandler.populations.keys():
    
    population = nmlHandler.populations[popName]
    print population.describe()
    
    population.record_v()
    population.record_gsyn()

    for addr in population.addresses():
        gid  = population[addr]
        print "    Cell %s (gid = %d) is at %s" % (addr, gid, population[addr].position)
        
   
for projName in nmlHandler.projections.keys():
         
    projection = nmlHandler.projections[projName]
    print projection.describe()
        
    
for inputName in nmlHandler.input_populations.keys():
    
    input_population = nmlHandler.input_populations[inputName]
    
    input_population.record()
    
    print "Input source which has been created: %s with %d connections"% (inputName, input_population.size)
    print input_population.describe()


for inputName in nmlHandler.input_projections.keys():
    proj = nmlHandler.input_projections[inputName]
    print proj.describe()
    '''
    p = proj.connections.targets
    print dir(p)
    print p.__class__
    print p'''



preRunTime = time.time()
print "---- Running the simulation ----"
run(tstop)
postRunTime = time.time()

print "Finished simulation. Setup time: %f secs, run time: %f secs"%(preRunTime-startTime, postRunTime-preRunTime)


for popName in nmlHandler.populations.keys():
    population = nmlHandler.populations[popName]
    population.print_v("%s.dat"%population.label)
    population.print_gsyn("%s.gsyn"%population.label)
    
for projName in nmlHandler.projections.keys():
    projection = nmlHandler.projections[projName]
    
for inputName in nmlHandler.input_populations.keys():
    input_population = nmlHandler.input_populations[inputName]
    input_population.printSpikes("inputs.dat")



print "Time step: " + str(get_time_step())

exit()







