@echo off

REM  As neuroConstruct requires files from the NeuroML repository as well as from its own 
REM  Subversion repository, use the following script to keep all of the necessary code up 
REM  to date. Note this script may change as the location of the repositories change


set NML_EX_DIR=templates\xmlTemplates\Examples
set NML_SC_DIR=templates\xmlTemplates\Schemata
set SBML2NEU_SC_DIR=templates\SBML2NEURON
set NEUROML2_DIR=NeuroML2
set JNEUROMLJAR_DIR=jNeuroMLJar

set OSB_SHOWCASE_DIR=osb\showcase
set NC_SHOWCASE_DIR=osb\showcase\neuroConstructShowcase



if not exist %NML_EX_DIR% (
	echo Adding NeuroML v1.x code from SourceForge in %NML_EX_DIR%
    svn co https://svn.code.sf.net/p/neuroml/code/trunk/web/NeuroMLFiles/Examples/ %NML_EX_DIR%
)
echo Updating the examples from the NeuroML Sourceforge repository...
svn update %NML_EX_DIR%


if not exist %NML_SC_DIR% (
	echo Adding NeuroML v1.x code from SourceForge in %NML_SC_DIR%
    svn co https://svn.code.sf.net/p/neuroml/code/trunk/web/NeuroMLFiles/Schemata/ %NML_SC_DIR%
)
echo Updating the schema files from the NeuroML Sourceforge repository...
svn update %NML_SC_DIR%


if not exist %NEUROML2_DIR% (
	echo Adding NeuroML v2alpha code from SourceForge in %NEUROML2_DIR%
    svn co https://svn.code.sf.net/p/neuroml/code/NeuroML2 %NEUROML2_DIR%
)
echo Updating the NeuroML 2 files from the NeuroML Sourceforge repository...
svn update %NEUROML2_DIR%

if not exist %JNEUROMLJAR_DIR% (
	echo Adding jNeuroML jar from SourceForge in %JNEUROMLJAR_DIR%
    svn co https://svn.code.sf.net/p/neuroml/code/jNeuroMLJar %JNEUROMLJAR_DIR%
)
echo Updating the jNeuroMLJar files from the NeuroML Sourceforge repository...
svn update %JNEUROMLJAR_DIR%

    

echo Updating the main neuroConstruct code...
git pull


if not exist %OSB_SHOWCASE_DIR% (
	echo Creating %OSB_SHOWCASE_DIR%
    mkdir %OSB_SHOWCASE_DIR%
)

if not exist %NC_SHOWCASE_DIR% (
	echo Cloning neuroConstruct showcase examples into %NC_SHOWCASE_DIR%
    cd %OSB_SHOWCASE_DIR%
    git clone git@github.com:OpenSourceBrain/neuroConstructShowcase.git
	cd ..\..
)

echo Updating the neuroConstruct showcase examples
cd %NC_SHOWCASE_DIR%
git pull
cd ..\..\..


