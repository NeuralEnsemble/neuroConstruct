@echo off

REM  As neuroConstruct requires files from the NeuroML repository as well as from its own 
REM  Subversion repository, use the following script to keep all of the necessary code up 
REM  to date. Note this script may change as the location of the repositories change


set NML_EX_DIR=templates\xmlTemplates\Examples
set NML_SC_DIR=templates\xmlTemplates\Schemata

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


echo Updating the main neuroConstruct code...
svn update