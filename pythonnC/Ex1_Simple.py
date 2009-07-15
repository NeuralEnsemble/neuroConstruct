#
#
#   A simple example of loading a neuroConstruct project using the Jython scripting interface
#
#   To execute this type of file, type '..\nC.bat -python Ex1_Simple.py' (Windows)
#   or '../nC.sh -python Ex1_Simple.py' (Linux/Mac)
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

from java.io import File
from java.lang import System

from ucl.physiol.neuroconstruct.project import ProjectManager

file = File("../nCexamples/Ex1_Simple/Ex1_Simple.ncx")
print 'Loading project file: ', file.getAbsolutePath()

pm = ProjectManager()
myProject = pm.loadProject(file)

print pm.status()

#  Remove this line to remain in interactive mode
System.exit(0)
