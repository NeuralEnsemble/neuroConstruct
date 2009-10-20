#
#
#   File to preform standard tasks on a neuroConstruct project
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

from sys import *
from time import *

from java.io import File

from ucl.physiol.neuroconstruct.project import ProjectManager
from ucl.physiol.neuroconstruct.neuron import NeuronFileManager
from ucl.physiol.neuroconstruct.genesis import GenesisFileManager
from ucl.physiol.neuroconstruct.nmodleditor.processes import ProcessManager
from ucl.physiol.neuroconstruct.utils import NumberGenerator
from ucl.physiol.neuroconstruct.hpc.mpi import MpiSettings
from ucl.physiol.neuroconstruct.simulation import SimulationsInfo
from ucl.physiol.neuroconstruct.cell.utils import CellTopologyHelper
from ucl.physiol.neuroconstruct.cell.compartmentalisation import GenesisCompartmentalisation





def getUnusedSimRef(project, simRefPrefix="P_Sim_"):   
     
    index = 0
    
    while File( "%s/simulations/%s%i"%(project.getProjectMainDirectory().getCanonicalPath(), simRefPrefix,index)).exists():
        index = index+1
    
    simRef = "%s%i"%(simRefPrefix,index)
    
    return simRef
    

def generateAndRunGenesis(project, 
                         projectManager, 
                         simConfig, 
                         simRef, 
                         simulatorSeed, 
                         verbose=True, 
                         quitAfterRun=False):
    
    if verbose: print "Going to generate GENESIS files for: "+simRef
    
    project.genesisFileManager.setQuitAfterRun(quitAfterRun)
    
    compartmentalisation = GenesisCompartmentalisation()

    project.genesisFileManager.generateTheGenesisFiles(simConfig,
                                                            None,
                                                            compartmentalisation,
                                                            simulatorSeed)

    projectManager.doRunGenesis(simConfig)
    
    if verbose: print "Set running GENESIS simulation: "+simRef
    
    


def generateAndRunNeuron(project, 
                         projectManager, 
                         simConfig, 
                         simRef, 
                         simulatorSeed, 
                         verbose=True, 
                         quitAfterRun=False, 
                         runInBackground=False):
        
    if verbose: print "Going to generate NEURON files for simulation: "+simRef
    
    project.neuronFileManager.setQuitAfterRun(quitAfterRun)
    
    if runInBackground:
        project.neuronSettings.setNoConsole()
    
    project.neuronFileManager.generateTheNeuronFiles(simConfig,
                                                     None,
                                                     NeuronFileManager.RUN_HOC,
                                                     simulatorSeed)
    
    
    compileProcManager = ProcessManager(project.neuronFileManager.getMainHocFile())
    
    compileSuccess = compileProcManager.compileFileWithNeuron(0,0)
    
    if verbose: print "Compiled NEURON files for: "+simRef
    
    
    ### Set simulation running
    
    if compileSuccess:
        projectManager.doRunNeuron(simConfig)
        if verbose: print "Set running NEURON simulation: "+simRef
        
        
        