import os

try:
    HOME = os.environ['HOME']
except KeyError:
    raise Exception("Please add an HOME environment variable corresponding\
                     to the location of the user's home directory.")

try:
	NC_HOME = os.environ['NC_HOME']
except KeyError:
	raise Exception("Please add an NC_HOME environment variable corresponding\
					 to the location of the neuroConstruct directory.")

try:
    JYTHON_HOME = os.environ['JYTHON_HOME']
except KeyError:
    raise Exception("Please add an JYTHON_HOME environment variable corresponding\
                     to the location of the jython binary directory.")

#os.path.dirname(os.path.realpath(__file__))
#NC_HOME = os.path.dirname(NEUROUNIT_HOME)
try:
	with open(os.path.join(NC_HOME,'nC.sh')): pass
except IOError:
   raise Exception("The NC_HOME environment variable does not correspond to\
   					the location of a neuroConstruct installation (no nC.sh).")
SIMULATORS = ["NEURON"]
OSB_MODELS = os.path.join(NC_HOME,"osb")
NEUROML2_MODELS = os.path.join(HOME,"NeuroML2")
SIM_CONFIGS = ["Default Simulation Configuration"]
CELL_TYPE = "hippocampus/CA1_pyramidal_neuron"
MODEL_NAME = "CA1PyramidalCell"
POPULATION_NAME = "CG_CML_0"
EXECNET_SOCKET_PORT = 8889
AUTOMATIC_SOCKET = True
MAX_TRIES = 100
