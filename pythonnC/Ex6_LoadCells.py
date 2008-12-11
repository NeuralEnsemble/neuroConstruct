#
#
#   A file which opens a neuroConstruct project, loads a cell morphology file 
#   and adds the cell to the project
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

from java.io import File
from java.lang import System

from ucl.physiol.neuroconstruct.project import Project, ProjectManager
from ucl.physiol.neuroconstruct.utils import NumberGenerator
from ucl.physiol.neuroconstruct.cell.converters import MorphMLConverter
from ucl.physiol.neuroconstruct.gui import MainFrame
from ucl.physiol.neuroconstruct.utils import GuiUtils


# Load an existing neuroConstruct project

projFile = File("TestPython/TestPython.neuro.xml")

print "Loading project from file: " + projFile.getAbsolutePath()+", exists: "+ str(projFile.exists())

pm = ProjectManager()

myProject = pm.loadProject(projFile)

print "Loaded project: " + myProject.getProjectName() 


morphDir = File("../examples/Ex3-Morphology/importedMorphologies/")
morphmlFile = File(morphDir, "SimplePurkinjeCell.morph.xml")


print "Going to load morphology from: " + morphmlFile.getCanonicalPath()

converter = MorphMLConverter()
cell = converter.loadFromMorphologyFile(morphmlFile, "NewCell") 

print "Loaded cell: " + cell.getInstanceName() + " with " + str(cell.getAllSegments().size()) +" segments" 

myProject.cellManager.addCellType(cell) # Actually add it to the project


# Save project
# Uncomment these lines to save the morphology in the project
'''
myProject.markProjectAsEdited()
myProject.saveProject()
'''

# Run neuroConstruct and check that the cell has been added

'''
frame = MainFrame()
GuiUtils.centreWindow(frame)
frame.setVisible(1)
frame.doLoadProject(projFile.getCanonicalPath())    
'''



