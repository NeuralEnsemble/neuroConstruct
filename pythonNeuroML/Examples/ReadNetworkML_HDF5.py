#
#
#   A simple example of reading in and printing the contents of a NetworkML file, saved in HDF5 format
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

file_name = 'small.h5'

logging.basicConfig(level=logging.DEBUG, format="%(name)-19s %(levelname)-5s - %(message)s")


print("Going to read contents of a NetworkML file: "+str(file_name))

import tables
from tables.nodes import filenode

h5file=tables.openFile(file_name)

print h5file.root.networkml.projections.projection_NetConn_1.NetConn_1.attrs.error_column_0


fnode = filenode.openNode(h5file.root.networkml, 'a+')


print "Nodes in file:"
for node in h5file:
    print node
print

h5file.close()




