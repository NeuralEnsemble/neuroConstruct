#
#   A simple example of reading in and printing the contents of a NetworkML file
#   saved in HDF5 format.
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

import logging
import time

sys.path.append("../NeuroMLUtils")

from NetworkHolder import NetworkHolder


from NetworkMLHDF5Handler import NetworkMLHDF5Handler

file_name = 'GranCellLayer.h5'
#file_name = 'Generated.net.h5'

logging.basicConfig(level=logging.INFO, format="%(name)-19s %(levelname)-5s - %(message)s")


start = time.time()
print("Going to read contents of a HDF5 NetworkML file: "+str(file_name))


nmlHandler = NetworkHolder()    # Stores (most of) the network structure

curHandler = NetworkMLHDF5Handler(nmlHandler) # The HDF5 handler knows of the structure of NetworkML and calls appropriate functions in NetworkHandler

curHandler.setNodeId(-1)    # Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0

curHandler.parse(file_name)


end = time.time()
print("Have read in contents of file: %s in %f seconds"%(file_name, (end-start)))

print (str(nmlHandler.nmlNet))

