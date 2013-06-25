#!/bin/bash
#    
#     neuroConstruct run script for Linux/Mac
# 


########################################################################################

# Change this line to your install location
export NC_HOME=$HOME/neuroConstruct

#   Use an altered value below to run the application with extra memory
#   **OR** set NC_MAX_MEMORY_LOCAL in your .bashrc file
#   Type java -X for more info
export NC_MAX_MEMORY=450M 

########################################################################################

# The rest of the settings below shouldn't have to change



# Current version of neuroConstruct
export NC_VERSION=1.7.0
export LIB_NEUROML_VERSION=2.0.0
export LEMS_VERSION=0.8.3

export JNEUROML_VERSION=0.3.3



if [ -n "$NC_MAX_MEMORY_LOCAL" ]; then
    export NC_MAX_MEMORY=$NC_MAX_MEMORY_LOCAL
fi


# Check for location 
if [ ! -d $NC_HOME ]; then
    NC_HOME_VER=$NC_HOME"_"$NC_VERSION
    if [ ! -d $NC_HOME_VER ]; then
        echo "Directory $NC_HOME doesn't exist (and neither does $NC_HOME_VER). Please change the value of the NC_HOME variable in nC.sh to the correct install location"
        exit
    else
        NC_HOME=$NC_HOME_VER
    fi
fi

echo "Running neuroConstruct from: $NC_HOME with max Java heap size of $NC_MAX_MEMORY"

# Location of jars and native libraries for HDF5
H5_DIR=$NC_HOME/lib/hdf5
H5_JARS=$H5_DIR/jhdf.jar:$H5_DIR/jhdf4obj.jar:$H5_DIR/jhdf5.jar:$H5_DIR/jhdf5obj.jar:$H5_DIR/jhdfobj.jar


# Location of jars and native libraries for Java 3D
J3D_DIR=$NC_HOME/lib/j3d
J3D_JARS=$J3D_DIR/j3dcore.jar:$J3D_DIR/j3dutils.jar:$J3D_DIR/vecmath.jar

LEMS_JAR=$NC_HOME/lib/neuroml2/lems-$LEMS_VERSION.jar
NML2_JAR=$NC_HOME/lib/neuroml2/libNeuroML-$LIB_NEUROML_VERSION.jar

JNML_JAR=$NC_HOME/jNeuroMLJar/jNeuroML-$JNEUROML_VERSION-jar-with-dependencies.jar



export CLASSPATH=$NC_HOME/neuroConstruct_$NC_VERSION.jar:$H5_JARS:$J3D_JARS:$NC_HOME/lib/jython/jython.jar:$LEMS_JAR:$NML2_JAR:$JNML_JAR


# Determine 32bit or 64bit architecture for JDK
machine=`uname -a | grep 64`

if [ $? -eq 0 ]; then
	export JAVA_LIBRARY_PATH=$H5_DIR/linux64:$J3D_DIR/linux64
else
	export JAVA_LIBRARY_PATH=$H5_DIR/linux32:$J3D_DIR/linux32
fi

#echo "Classpath is" $CLASSPATH
#echo "java.library.path is" $JAVA_LIBRARY_PATH

if [ $# -eq 1 ] ; then
    if [ $1 == "-make" ]; then

        mkdir classes
        javac  -sourcepath src -d classes -classpath $CLASSPATH  src/ucl/physiol/neuroconstruct/*/*.java  src/ucl/physiol/neuroconstruct/*/*/*.java  src/ucl/physiol/neuroconstruct/*/*/*/*.java
        cp src/ucl/physiol/neuroconstruct/gui/* classes/ucl/physiol/neuroconstruct/gui  # For gifs & pngs
        jar -cf neuroConstruct_$NC_VERSION.jar -C classes/ .
        exit
    fi
fi

java -Xmx$NC_MAX_MEMORY  -classpath $CLASSPATH -Djava.library.path=$JAVA_LIBRARY_PATH  ucl.physiol.neuroconstruct.gui.MainApplication $*




