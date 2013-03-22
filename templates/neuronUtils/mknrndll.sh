
# This is modified from the NEURON source and used to allow "silent" generation of the mod files 
# in neuroConstruct on Windows


NDEMO="`$1/bin/cygpath -u $1/demo`"  # Make sure we're pointing to the c:\nrn72\demo dir
N=${NDEMO%/*}                        # parent of this dir...
PATH=$N/bin
export PATH
export N
NEURONHOME=$N
export NEURONHOME

rm -f nrnmech.dll
sh $N/lib/mknrndl2.sh        # Uses the standard mknrndl2.sh in the current distribution

echo ""
if [ -f nrnmech.dll ] ; then
	echo "nrnmech.dll was built successfully."
else
	echo "There was an error in the process of creating nrnmech.dll"
fi

if [ $# = 1 ]
	echo "Press Return key to exit"
	read a
fi

