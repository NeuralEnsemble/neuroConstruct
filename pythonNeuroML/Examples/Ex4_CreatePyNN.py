#
#   A simple example of reading in and instantiating cells and connections using PyNN
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the Wellcome Trust
#
#


import sys
import xml

import logging
 
sys.path.append("../NeuroMLUtils")
sys.path.append("../PyNNUtils")

# Lines from standard PyNN examples
if hasattr(sys,"argv"):     # run using python
	print sys.argv
	simulator = sys.argv[-1]
else:
    simulator = "neuron"    # run using nrngui -python
	
from NetworkHandler import NetworkHandler
from NetworkMLSaxHandler import NetworkMLSaxHandler
from PyNNUtils import NetManagerPyNN

	
exec("from pyNN.%s import *" % simulator)

file_name = 'small_pynn.nml'

logging.basicConfig(level=logging.INFO, format="%(name)-19s %(levelname)-5s - %(message)s")


print("Going to read contents of a NetworkML file: "+str(file_name))


parser = xml.sax.make_parser()   # A parser for any XML file

nmlHandler = NetManagerPyNN(simulator)	# Stores (most of) the network structure

curHandler = NetworkMLSaxHandler(nmlHandler) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetworkHandler

curHandler.setNodeId(-1) 	# Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0

parser.setContentHandler(curHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed

parser.parse(open(file_name)) # The parser opens the file and ultimately the appropriate functions in NetworkHandler get called


print("Have read in contents of file: "+str(file_name))

for popName in nmlHandler.populations.keys():
    
    population = nmlHandler.populations[popName]
    print "Population which has been created: %s with %d cells"% (popName, population.size)

    for addr in population.addresses():
        gid  = population[addr]
        print "    Cell %s (gid = %d) is at %s" % (addr, gid, population[addr].position)
        
   
for projName in nmlHandler.projections.keys():
         
    projection = nmlHandler.projections[projName]
    print "Projection which has been created: %s with %d connections"% (projName, len(projection))
        
    for conn in projection:
        if (simulator == "neuron"):
            print "    Connection: %s"% (conn)
        else:
            print "    Connection: %s from %s to %s, weight: %s, delay: %s" % (conn.port, conn.pre, conn.post, conn.weight, conn.delay)

    
exit()






