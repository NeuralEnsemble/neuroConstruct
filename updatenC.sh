
#  As neuroConstruct requires files from the NeuroML repository as well as from its own 
#  Subversion repository, use the following script to keep all of the necessary code up 
#  to date. Note this script may change as the location of the repositories change


export NML_EX_DIR=templates/xmlTemplates/Examples
export NML_SC_DIR=templates/xmlTemplates/Schemata

export SBML2NEU_SC_DIR=templates/SBML2NEURON

export LEMS_DIR=lems
export NEUROML2_DIR=NeuroML2

export NC_EXAMPLES=nCexamples
export NC_MODELS=nCmodels


if [ ! -d $NML_EX_DIR ]; then
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/trunk/web/NeuroMLFiles/Examples/ $NML_EX_DIR
fi
echo "Updating the examples from the NeuroML Sourceforge repository..."
svn update $NML_EX_DIR


if [ ! -d $NML_SC_DIR ]; then
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/trunk/web/NeuroMLFiles/Schemata/ $NML_SC_DIR
fi
echo "Updating the schema files from the NeuroML Sourceforge repository..."
svn update $NML_SC_DIR


if [ ! -d $SBML2NEU_SC_DIR ]; then
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/SBML2NEURON/ $SBML2NEU_SC_DIR
fi
echo "Updating the SBML2NEURON files from the NeuroML Sourceforge repository..."
svn update $SBML2NEU_SC_DIR


if [ -d $LEMS_DIR ]; then
    echo "Moving old lems directory to lems_old, as everything needed for NeuroML 2/LEMS is in the NeuroML2 folder"
    mv $LEMS_DIR lems_old
fi

echo "Updating the LEMS/NeuroML 2 files from the NeuroML Sourceforge repository..."
svn update $LEMS_DIR


if [ ! -d $NEUROML2_DIR ]; then
    svn co https://neuroml.svn.sourceforge.net/svnroot/neuroml/NeuroML2 $NEUROML2_DIR
fi

echo "Updating the NeuroML 2 files from the NeuroML Sourceforge repository..."
svn update $NEUROML2_DIR


echo Updating the main neuroConstruct code...
svn update



if [ ! -d $NC_EXAMPLES ]; then
    svn co svn://87.106.103.176:3999/models/examples/trunk/nCexamples $NC_EXAMPLES
fi

echo Updating the neuroConstruct core functionality examples
svn update $NC_EXAMPLES


if [ ! -d $NC_MODELS ]; then
    svn co svn://87.106.103.176:3999/models/models/trunk/nCmodels $NC_MODELS
fi

echo Updating the neuroConstruct detailed model examples
svn update $NC_MODELS



