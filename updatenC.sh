
#  As neuroConstruct requires files from the NeuroML repository as well as from its own 
#  Subversion repository, use the following script to keep all of the necessary code up 
#  to date. Note this script may change as the location of the repositories change


export NML_EX_DIR=templates/xmlTemplates/Examples
export NML_SC_DIR=templates/xmlTemplates/Schemata

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



echo Updating the main neuroConstruct code...
svn update



if [ ! -d $NC_EXAMPLES ]; then
    svn co svn://87.106.103.176:3999/models/examples/trunk/nCexamples $NC_EXAMPLES
fi

echo Updating the neuroConstruct core functionality examples
svn update $NC_EXAMPLES


if [ ! -d $NC_MODELS ]; then
    svn co svn://87.106.103.176:3999/models/examples/trunk/nCmodels $NC_MODELS
fi

echo Updating the neuroConstruct detailed model examples
svn update $NC_MODELS



