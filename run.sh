#    
#   Use an altered version of the line below to run the application with extra memory; type java -X for more...
# 


export CLASSPATH=neuroConstruct_1.1.0.jar:lib/hdf5/jhdf.jar:lib/hdf5/jhdf4obj.jar:lib/hdf5/jhdf5.jar:lib/hdf5/jhdf5obj.jar:lib/hdf5/jhdfobj.jar:lib/jython/jython.jar

machine=`uname -a | grep 64`

if [ $? -eq 0 ]; then
	export JAVA_LIBRARY_PATH=lib/hdf5/linux
else
	export JAVA_LIBRARY_PATH=lib/hdf5/linux32
fi

java -Xmx700M  -classpath $CLASSPATH -Djava.library.path=$JAVA_LIBRARY_PATH  ucl.physiol.neuroconstruct.gui.MainApplication $1 $2 $3 $4 $5
