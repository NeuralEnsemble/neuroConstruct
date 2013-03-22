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

try:
	from java.io import File
except ImportError:
	print "Note: this file should be run using ..\\nC.bat -python XXX.py' or './nC.sh -python XXX.py'"
	print "See http://www.neuroconstruct.org/docs/python.html for more details"
	quit()

from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.cell.converters import MorphMLConverter


# Load an existing neuroConstruct project

projFile = File("TestPython/TestPython.neuro.xml")
print "Loading project from file: " + projFile.getAbsolutePath()+", exists: "+ str(projFile.exists())

pm = ProjectManager()
myProject = pm.loadProject(projFile)
print "Loaded project: " + myProject.getProjectName() 


morphDir = File("../nCexamples/Ex3_Morphology/importedMorphologies/")
morphmlFile = File(morphDir, "SimplePurkinjeCell.morph.xml")


print "Going to load morphology from: " + morphmlFile.getCanonicalPath()

converter = MorphMLConverter()
cell = converter.loadFromMorphologyFile(morphmlFile, "NewCell") 

print "Loaded cell: " + cell.getInstanceName() + " with " + str(cell.getAllSegments().size()) +" segments" 

myProject.cellManager.addCellType(cell) # Actually add it to the project
myProject.cellGroupsInfo.setCellType("SampleCellGroup", cell.getInstanceName()) # Set the type of an existing cell group to this



# Now the project can be generated as in Ex5_MultiSimGenerate.py

#      * OR *


# Save project
# Uncomment these lines to save the morphology in the project, then view the
# updated project in neuroConstruct
'''
myProject.markProjectAsEdited()
myProject.saveProject()

# Run neuroConstruct and check that the cell has been added

from ucl.physiol.neuroconstruct.gui import MainFrame
from ucl.physiol.neuroconstruct.utils import GuiUtils

frame = MainFrame()
GuiUtils.centreWindow(frame)
frame.setVisible(1)
frame.doLoadProject(projFile.getCanonicalPath())    
'''


