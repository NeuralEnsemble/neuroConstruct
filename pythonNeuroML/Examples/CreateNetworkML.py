#
#
#   A simple example of using the NetworkML helper file to create a network and save it
#   in a format which can be loaded into neuroConstruct
#
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

 
# Standard imports
import sys
import os
import logging
import math
 
# Import functionality from NeuroMLUtils
sys.path.append("../NeuroMLUtils")
from NetworkMLUtils import NetworkMLFile


print("Going to create a NetworkML file...")

myFile = NetworkMLFile()

newPop = myFile.addPopulation("SampleCellGroup", "SampleCell")
popSize = 10 

newProj = myFile.addProjection("NetConn_1", "SampleCellGroup", "SampleCellGroup")
newProj.addSynapse("DoubExpSyn", 1, -20, 5)


for i in range(popSize):

    x = 100 * math.sin(i/4.0)
    y = i*20
    z = 100 * math.cos(i/4.0)
    newPop.addInstance(x,y,z)
    if i>0:
        newProj.addConnection(i-1, i)
    

myFile.writeToFile("../../../temp/test.nml")


print("All done!")