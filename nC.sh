#!/bin/bash
#    
#     neuroConstruct script for Linux/Mac
# 

export NC_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $NC_HOME/nCenv.sh

echo "Running neuroConstruct from: $NC_HOME with max Java heap size of $NC_MAX_MEMORY"


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




