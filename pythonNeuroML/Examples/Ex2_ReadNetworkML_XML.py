#
#   A simple example of reading in and instantiating Python objects for a NetworkML file
#   containing XML
#
#   Beta version!!
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
import xml.sax
import time

import logging
 
sys.path.append("../NeuroMLUtils")

from NetworkHolder import NetworkHolder
from NetworkMLSaxHandler import NetworkMLSaxHandler

file_name = 'small.nml'
#file_name = 'Generated.net.xml'

logging.basicConfig(level=logging.INFO, format="%(name)-19s %(levelname)-5s - %(message)s")

start = time.time()

print("Going to read contents of a NetworkML file: "+str(file_name))


parser = xml.sax.make_parser()   # A parser for any XML file

nmlHolder = NetworkHolder()	# Stores (most of) the network structure

curHandler = NetworkMLSaxHandler(nmlHolder) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetworkHolder

curHandler.setNodeId(-1) 	# Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0

parser.setContentHandler(curHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed

parser.parse(open(file_name)) # The parser opens the file and ultimately the appropriate functions in NetworkHolder get called

end = time.time()
print("Have read in contents of file: %s in %f seconds"%(file_name, (end-start)))

print (str(nmlHolder.nmlNet))





