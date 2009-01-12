
#  As neuroConstruct requires files from the NeuroML repository as well as from its own 
#  Subversion repository, use the following script to keep all of the necessary code up 
#  to date. Note this script may change as the location of the repositories change


export NML_EX_DIR=templates/xmlTemplates/Examples
export NML_SC_DIR=templates/xmlTemplates/Schemata


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