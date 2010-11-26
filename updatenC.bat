@echo off

REM  As neuroConstruct requires files from the NeuroML repository as well as from its own 
REM  Subversion repository, use the following script to keep all of the necessary code up 
REM  to date. Note this script may change as the location of the repositories change


set NML_EX_DIR=templates\xmlTemplates\Examples
set NML_SC_DIR=templates\xmlTemplates\Schemata
set SBML2NEU_SC_DIR=templates\SBML2NEURON
set LEMS_SC_DIR=lems

set NC_EXAMPLES=nCexamples
set NC_MODELS=nCmodels


if not exist %NML_EX_DIR% (
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/trunk/web/NeuroMLFiles/Examples/ %NML_EX_DIR%
)
echo Updating the examples from the NeuroML Sourceforge repository...
svn update %NML_EX_DIR%


if not exist %NML_SC_DIR% (
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/trunk/web/NeuroMLFiles/Schemata/ %NML_SC_DIR%
)
echo Updating the schema files from the NeuroML Sourceforge repository...
svn update %NML_SC_DIR%


if not exist %SBML2NEU_SC_DIR% (
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/SBML2NEURON %SBML2NEU_SC_DIR%
)
echo Updating the SBML2NEURON files from the NeuroML Sourceforge repository...
svn update %SBML2NEU_SC_DIR%


if not exist %LEMS_SC_DIR% (
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/DemoVer2.0/lems %LEMS_SC_DIR%
)
echo Updating the LEMS/NeuroML 2 files from the NeuroML Sourceforge repository...
svn update %LEMS_SC_DIR%




echo Updating the main neuroConstruct code...
svn update


if not exist %NC_EXAMPLES% (
    svn co svn://87.106.103.176:3999/models/examples/trunk/nCexamples %NC_EXAMPLES%
)

echo Updating the neuroConstruct core functionality examples
svn update %NC_EXAMPLES%


if not exist %NC_MODELS% (
    svn co svn://87.106.103.176:3999/models/models/trunk/nCmodels %NC_MODELS%
)


echo Updating the neuroConstruct detailed model examples
svn update %NC_MODELS%
