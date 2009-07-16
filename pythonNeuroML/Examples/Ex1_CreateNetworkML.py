#
#   A simple example of using the NetworkML helper file to create a network and save it
#   in a format which can be loaded into neuroConstruct.
#
#   Note, HDF5 functionality only fully works if you have Python/pytables/HDF5
#   set up correctly. Only tested on Linux so far.
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

import sys
import math
import random
 
sys.path.append("../NeuroMLUtils")
from NetworkMLNet import NetworkMLNet

print("Going to create a NetworkML file...")

nmlNet = NetworkMLNet()

newPop = nmlNet.addPopulation("SampleCellGroup", "SampleCell") # Names chosen for easy import into neuroConstruct...
popSize = 40
compNodes = 4 # Number of processors to generate for

newProj = nmlNet.addProjection("NetConn_SampleCellGroup_SampleCellGroup", "SampleCellGroup", "SampleCellGroup")
newProj.addSynapse("DoubExpSyn", 1, -20, 5)


for i in range(popSize):

    ###    Alter these lines for any "shape" network you want
    x = 200 * math.sin(i/4.0)
    y = i*4
    z = 100 * math.cos(i/4.0)
    
    newPop.addInstance(x,y,z, random.randint(0, compNodes-1))
    
    if i>0:
        newProj.addConnection(i-1, i)
    
  
filenameX = "random.nml"
nmlNet.writeXML(filenameX)     # Create XML based NetworkML file


###############  Put back in these lines to save in HDF5 format. Note: pytables should be installed and tested before this!
#filenameH = "random.h5"
#nmlFile.writeHDF5(filenameH)     # Create HDF5 based NetworkML file
#print("All done! File with "+str(popSize)+" cells saved to: "+ filenameH)
###############

print("All done! File with "+str(popSize)+" cells saved to: "+ filenameX)
print("")

print("To load this generated network into neuroConstruct, create a new project and accept the kind offer to add")
print("some sample elements. Add a network connection with source and target being the project's only cell group.")
print("Go to tab Generate, press Load NetworkML and locate the generated file. ")
print(" ")
print("Try changing the shape and population size above. You will need a pretty high spec video card to view ")
print("large networks, though.")



