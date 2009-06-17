#
#
#   A simple example of loading a neuroConstruct project using the Jython scripting interface
#
#   To use type 'nC.bat -python TestSimple.py' or 'nC.sh -python test.py'.
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
