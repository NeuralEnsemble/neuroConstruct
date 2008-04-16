#
#
#   A simple example of reading in and printing the contents of a NetworkML file
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
from NetworkMLSaxHandler import NetworkMLSaxHandler

file_name = 'Test.nml'
#file_name = 'Pre1.7.1.nml'

logging.basicConfig(level=logging.DEBUG, format="%(name)-19s %(levelname)-5s - %(message)s")


print("Going to read contents of a NetworkML file: "+str(file_name))


parser = xml.sax.make_parser()   

nmlHandler = NetworkHandler()

curHandler = NetworkMLSaxHandler(nmlHandler)

parser.setContentHandler(curHandler)

parser.parse(open(file_name)) 







