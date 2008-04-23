#
#   A simple example of using the NetworkML helper file to create a network and save it
#   in a format which can be loaded into neuroConstruct
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#
 
import sys, os, math, random, xml
 
sys.path.append("../NeuroMLUtils")
from NetworkMLUtils import NetworkMLFile

print("Going to create a NetworkML file...")

nmlFile = NetworkMLFile()

newPop = nmlFile.addPopulation("SampleCellGroup", "SampleCell") # Names chosen for easy import into neuroConstruct...
popSize = 20
compNodes = 4 # Number of processors to generate for

newProj = nmlFile.addProjection("NetConn_1", "SampleCellGroup", "SampleCellGroup")
newProj.addSynapse("DoubExpSyn", 1, -20, 5)


for i in range(popSize):

    x = 200 * math.sin(i/4.0)
    y = i*2
    z = 100 * math.cos(i/4.0)
    
    newPop.addInstance(x,y,z, random.randint(0, compNodes-1))
    if i>0:
        newProj.addConnection(i-1, i)
    
  
filenameX = "../../../temp/test.nml"
nmlFile.writeXML(filenameX)     # Create XML based NetworkML file

filenameH = "../../../temp/test.h5"
nmlFile.writeHDF5(filenameH)     # Create HDF5 based NetworkML file

print("All done! File saved to: "+ filenameX+ " and to "+ filenameH)