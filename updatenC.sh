
#  As neuroConstruct requires files from the NeuroML repository as well as from its own 
#  Subversion repository, use the following script to keep all of the necessary code up 
#  to date. Note this script may change as the location of the repositories change


export NML_EX_DIR=templates/xmlTemplates/Examples
export NML_SC_DIR=templates/xmlTemplates/Schemata

export SBML2NEU_SC_DIR=templates/SBML2NEURON

export NEUROML2_DIR=NeuroML2
export JNEUROMLJAR_DIR=jNeuroMLJar

if ! type -p svn > /dev/null; then
    # svn is not installed on the system
    echo "svn not found on this machine. please install."
    exit 1
fi

if ! type -p git > /dev/null; then
    # git is not installed on the system
    echo "git not found on this machine. please install."
    exit 1
fi

if [ ! -d $NML_EX_DIR ]; then
    svn co https://svn.code.sf.net/p/neuroml/code/trunk/web/NeuroMLFiles/Examples/ $NML_EX_DIR
fi
echo "Updating the examples from the NeuroML Sourceforge repository..."
svn update $NML_EX_DIR


if [ ! -d $NML_SC_DIR ]; then
    svn co https://svn.code.sf.net/p/neuroml/code/trunk/web/NeuroMLFiles/Schemata/ $NML_SC_DIR
fi
echo "Updating the schema files from the NeuroML Sourceforge repository..."
svn update $NML_SC_DIR



if [ ! -d $NEUROML2_DIR ]; then
    svn co https://svn.code.sf.net/p/neuroml/code/NeuroML2 $NEUROML2_DIR
fi
echo "Updating the NeuroML 2 files from the NeuroML Sourceforge repository..."
svn update $NEUROML2_DIR


if [ ! -d $JNEUROMLJAR_DIR ]; then
    svn co https://svn.code.sf.net/p/neuroml/code/jNeuroMLJar $JNEUROMLJAR_DIR
fi
echo "Updating the jNeuroML Jar from the NeuroML Sourceforge repository..."
svn update $JNEUROMLJAR_DIR


echo Updating the main neuroConstruct code...
git pull



if [ ! -d osb/showcase ]; then
    mkdir osb/showcase
fi


if [ ! -d osb/showcase/neuroConstructShowcase ]; then
    cd osb/showcase
    git clone git://github.com/OpenSourceBrain/neuroConstructShowcase.git
    cd -
fi


echo "Updating the neuroConstruct showcase examples..."
cd osb/showcase/neuroConstructShowcase
git pull
cd -



