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
 
import sys, os, math, random, xml, time
 
sys.path.append("../NeuroMLUtils")
from NetworkMLUtils import NetworkMLFile

print("Going to create a NetworkML file...")

start = time.time()


nmlFile = NetworkMLFile()

newPop = nmlFile.addPopulation("SampleCellGroup", "SampleCell") # Names chosen for easy import into neuroConstruct...
popSize = 100
projSize = 10000
compNodes = 4 # Number of processors to generate for

newProj = nmlFile.addProjection("NetConn_1", "SampleCellGroup", "SampleCellGroup")
newProj.addSynapse("DoubExpSyn", 1, -20, 5)


for i in range(popSize):

    x = 1000.0 * random.random()
    y = 1000.0 * random.random()
    z = 1000.0 * random.random()
    
    newPop.addInstance(x,y,z, random.randint(0, compNodes-1))
    

for i in range(projSize):
        newProj.addConnection(random.randint(0, popSize-1), random.randint(0, popSize-1))
    

mid = time.time()

print("                                                 Created Python obj containg net in " +str(mid-start)+" seconds")
  
#filenameX = "../../../temp/test.nml"
#nmlFile.writeXML(filenameX)     # Create XML based NetworkML file

filenameH = "../../../temp/test.h5"
nmlFile.writeHDF5(filenameH)     # Create HDF5 based NetworkML file

end = time.time()

print("All done! File with "+str(popSize)+" cells, "+str(projSize)+" conns saved to: "+ filenameH +"               in " +str(end-mid)+" seconds")