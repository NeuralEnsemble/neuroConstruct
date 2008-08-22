#
#
#   A file which opens a neuroConstruct project, adds some cells and network connections
#   and then saves a NetworkML file with the net structure
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

from java.io import File
from java.lang import System

from ucl.physiol.neuroconstruct.project import ProjectManager

from math import *


# Load an existing neuroConstruct project

projFile = File("TestPython/TestPython.neuro.xml")

print "Loading project from file: " + projFile.getAbsolutePath()+", exists: "+ str(projFile.exists())

pm = ProjectManager()
myProject = pm.loadProject(projFile)

print "Loaded project: " + myProject.getProjectName() 



# Add a number of cells to the generatedCellPositions, connections to generatedNetworkConnections
# and electrical inputs to generatedElecInputs
numCells = 12

for i in range(0, numCells) :
    x = 100 * sin(i * 2 *pi / numCells)
    y = 100 * cos(i * 2 *pi / numCells)
    myProject.generatedCellPositions.addPosition("SampleCellGroup", i, x,y,0)
    
    if i != numCells-1 : 
        myProject.generatedNetworkConnections.addSynapticConnection("NC1", i, i+1)


# Print details
print myProject.generatedCellPositions
print myProject.generatedNetworkConnections



# Save to a NetworkML file
myNetworkMLFile = File("TestPython/savedNetworks/nmlt.nml")

simConfig = myProject.simConfigInfo.getDefaultSimConfig() 

pm.saveNetworkStructureXML(myProject, myNetworkMLFile, 0, 0, simConfig.getName(), "Physiological Units")

print "Network structure saved to file: "+ myNetworkMLFile.getAbsolutePath()


System.exit(0)


