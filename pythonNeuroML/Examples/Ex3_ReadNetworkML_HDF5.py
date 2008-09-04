#
#
#   A simple example of reading in and printing the contents of a NetworkML file
#   saved in HDF5 format, not fully there yet...
#
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

 
import sys
import os
import math
import xml
import xml.sax

import logging
 
sys.path.append("../NeuroMLUtils")

from NetworkHandler import NetworkHandler
from NetworkMLHDF5Handler import NetworkMLHDF5Handler

#file_name = 'small.h5'
file_name = 'test.h5'
#file_name = '../../../temp/test.h5'

logging.basicConfig(level=logging.DEBUG, format="%(name)-19s %(levelname)-5s - %(message)s")


print("Going to read contents of a HDF5 NetworkML file: "+str(file_name))


nmlHandler = NetworkHandler()   # The base NetworkHandler class just prints out details of the network

curHandler = NetworkMLHDF5Handler(nmlHandler) # The HDF5 handler knows of the structure of NetworkML and calls appropriate functions in NetworkHandler

curHandler.setNodeId(-1)    # Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0

curHandler.parse(file_name)


