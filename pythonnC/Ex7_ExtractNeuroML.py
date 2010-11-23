
#
#
#   A file which opens a neuroConstruct project, and saves cells in NeuroML format
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
	
import sys

from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.cell.converters import MorphMLConverter
from ucl.physiol.neuroconstruct.cell.compartmentalisation import OriginalCompartmentalisation

from ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants import NeuroMLLevel, NeuroMLVersion


if len(sys.argv) != 3:
    print "Usage: \n    ../nC.sh -python Ex7_ExtractNeuroML.py neuroConstruct_project_file folder_for_NeuroML"
    print "Example: \n    ../nC.sh -python Ex7_ExtractNeuroML.py TestPython/TestPython.neuro.xml /tmp"
    print "or: \n    ..\\nC.bat -python Ex7_ExtractNeuroML.py TestPython\\TestPython.neuro.xml c:\\temp"
    sys.exit(1)

# Load neuroConstruct project

projFile = File(sys.argv[1])
print "Loading project from file: " + projFile.getAbsolutePath()+", exists: "+ str(projFile.exists())

pm = ProjectManager()
myProject = pm.loadProject(projFile)
print "Loaded project: " + myProject.getProjectName()

saveDir = File(sys.argv[2])
print "Going to save NeuroML files to: " + saveDir.getAbsolutePath()

level = NeuroMLLevel.NEUROML_LEVEL_3
version = NeuroMLVersion.NEUROML_VERSION_1

MorphMLConverter.saveAllCellsInNeuroML(myProject,
                                       OriginalCompartmentalisation(),
                                       level,
                                       version,
                                       None,
                                       saveDir)

# Note: look also in Java class ucl.physiol.neuroconstruct.neuroml.NeuroMLPythonFileManager
# for the method generateNeuroMLFiles() which can be used to export all cells, channels and
# generated network structure to individual NeuroML files or as one large Level 3 file.

print "Done!"




