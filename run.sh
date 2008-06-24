#    
#   Use an altered value with -Xmx in the line below to run the application with extra memory; type java -X for more...
# 


# Change this line to your install location
export NC_HOME=$HOME/neuroConstruct
export NC_VERSION=1.1.1


export H5_JAR_DIR=$NC_HOME/lib/hdf5
export H5_JARS=$H5_JAR_DIR/jhdf.jar:$H5_JAR_DIR/jhdf4obj.jar:$H5_JAR_DIR/jhdf5.jar:$H5_JAR_DIR/jhdf5obj.jar:$H5_JAR_DIR/jhdfobj.jar

export CLASSPATH=$NC_HOME/neuroConstruct_$NC_VERSION.jar:$H5_JARS:$NC_HOME/lib/jython/jython.jar

machine=`uname -a | grep 64`

if [ $? -eq 0 ]; then
	export JAVA_LIBRARY_PATH=lib/hdf5/linux
else
	export JAVA_LIBRARY_PATH=lib/hdf5/linux32
fi


echo $CLASSPATH

java -Xmx700M  -classpath $CLASSPATH -Djava.library.path=$JAVA_LIBRARY_PATH  ucl.physiol.neuroconstruct.gui.MainApplication $1 $2 $3 $4 $5
