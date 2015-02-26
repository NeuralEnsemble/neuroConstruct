#!/bin/bash
#    
#     neuroConstruct script for Linux/Mac
# 


########################################################################################

# Change this line to your install location
export NC_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#   Use an altered value below to run the application with extra memory
#   **OR** set NC_MAX_MEMORY_LOCAL in your .bashrc file
#   Type java -X for more info
export NC_MAX_MEMORY=450M 

########################################################################################

# The rest of the settings below shouldn't have to change

# Current version of neuroConstruct
export NC_VERSION=1.7.1

export JNEUROML_VERSION=0.7.1

# These are for an old version of NML2/LEMS...
export LIB_NEUROML_VERSION=2.0.0
export LEMS_VERSION=0.8.3


if [ -n "$NC_MAX_MEMORY_LOCAL" ]; then
    export NC_MAX_MEMORY=$NC_MAX_MEMORY_LOCAL
fi


# Location of jars and native libraries for HDF5
H5_DIR=$NC_HOME/lib/hdf5
H5_JARS=$H5_DIR/jhdf.jar:$H5_DIR/jhdf4obj.jar:$H5_DIR/jhdf5.jar:$H5_DIR/jhdf5obj.jar:$H5_DIR/jhdfobj.jar


# Location of jars and native libraries for Java 3D
J3D_DIR=$NC_HOME/lib/j3d
#J3D_JARS=$J3D_DIR/j3dcore.jar:$J3D_DIR/j3dutils.jar:$J3D_DIR/vecmath.jar
J3D_JARS=$J3D_DIR/gluegen-rt.jar:$J3D_DIR/j3dcore.jar:$J3D_DIR/j3dutils.jar:$J3D_DIR/joal.jar:$J3D_DIR/jogl-all.jar:$J3D_DIR/vecmath.jar

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
