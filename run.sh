#    
#     neuroConstruct run script for Linux/Mac
# 


########################################################################################
# Change this line to your install location
export NC_HOME=$HOME/neuroConstruct

#   Use an altered value below to run the application with extra memory; 
#   type java -X for more info
export NC_MAX_MEMORY=450M 
#
# The rest of the settings below shouldn't have to change
########################################################################################



# Current version of neuroConstruct
export NC_VERSION=1.1.4


# Location of jars and native libraries for HDF5
H5_DIR=$NC_HOME/lib/hdf5
H5_JARS=$H5_DIR/jhdf.jar:$H5_DIR/jhdf4obj.jar:$H5_DIR/jhdf5.jar:$H5_DIR/jhdf5obj.jar:$H5_DIR/jhdfobj.jar


# Location of jars and native libraries for Java 3D
J3D_DIR=$NC_HOME/lib/j3d
J3D_JARS=$J3D_DIR/j3dcore.jar:$J3D_DIR/j3dutils.jar:$J3D_DIR/vecmath.jar


export CLASSPATH=$NC_HOME/neuroConstruct_$NC_VERSION.jar:$H5_JARS:$J3D_JARS:$NC_HOME/lib/jython/jython.jar


# Determine 32bit or 64bit
machine=`uname -a | grep 64`

if [ $? -eq 0 ]; then
	export JAVA_LIBRARY_PATH=$H5_DIR/linux:$J3D_DIR/linux64
else
	export JAVA_LIBRARY_PATH=$H5_DIR/linux32:$J3D_DIR/linux32
fi


echo "Classpath is" $CLASSPATH
echo "java.library.path is" $JAVA_LIBRARY_PATH

java -Xmx$NC_MAX_MEMORY  -classpath $CLASSPATH -Djava.library.path=$JAVA_LIBRARY_PATH  ucl.physiol.neuroconstruct.gui.MainApplication $1 $2 $3 $4 $5
