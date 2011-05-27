#
#
#   A class to assist loading NetworkML networks into PyNN supporting simulators
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the Wellcome Trust
#
#

import sys
import os
import logging
from NeuroTools.stgen import StGen

if sys.path.count(os.getcwd())==0:
    sys.path.append(os.getcwd())
    
sys.path.append("../NeuroMLUtils")

from NetworkHandler import NetworkHandler
    
#
#
#   PyNN version of the NetworkHandler for handling network events,
#   e.g. cell locations, connections, etc. These events can come from
#   a SAX parsed NetworkML file, or a parsed HDF5 file, etc.
#
#

class NetManagerPyNN(NetworkHandler):
    
    log = logging.getLogger("NetManagerPyNN")
    
    logging.basicConfig(level=logging.INFO, format="%(name)-19s %(levelname)-5s - %(message)s")
        
    populations = {}
    projections = {}  
    
    projWeightFactor = {}  
    projSynapseDynamics = {}  
    projConns = []
    
    input_populations = {}  
    input_projections = {}  
    
    inputCellGroups = {}  
    inputWeights = {}  
    inputSynapseDynamics = {}  
    inputConns = []
    
    inputCount = {}
	
    my_simulator = "neuron"
    
    my_stgen = StGen()
    
    maxSimLength = -1 # Needed for generating input spike time array...
	
	
    def __init__(self, my_simulator="neuron"):
        self.log.info("Using simulator: "+my_simulator)
        self.my_simulator = my_simulator
        exec("from pyNN.%s import *" % self.my_simulator)
        
        #setup()
        
                
    def setSeed(self, seed):
        self.my_stgen.seed(seed)
        
        
    def setMaxSimLength(self, length):
        self.maxSimLength = length
       
    
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
		
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Creating population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
            standardCell = IF_cond_alpha
            
            try:
                # Try importing a files named after that cell
                
                exec("from %s import *" % cellGroup)
                self.log.info("-- Imported cell props from: "+ cellGroup)
                exec("standardCell = %s" % cellGroup)
                
            except ImportError:
                self.log.info("-- Could not find file for: "+ cellGroup)
                # Else check if it refers to a standard type
                if (cellType.count("IF_cond_alpha")>=0):
                    standardCell = IF_cond_alpha
                if (cellType.count("IF_cond_exp")>=0):
                    standardCell = IF_cond_exp
                #TODO: more...
            
            pop = Population((size,), standardCell, label=cellGroup)
            self.populations[cellGroup] = pop

        else:
                
            self.log.error("Population: "+cellGroup+", cell type: "+cellType+" specifies no size. Will lead to errors!")
        
  
    #
    #  Overridden from NetworkHandler
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
        addr = int(id)
        self.populations[cellGroup][addr].position = (x, y, z)
        
        
        
    #
    #  Overridden from NetworkHandler
    #    
    def handleConnection(self, projName, id, source, target, synapseType, \
                                                    preCellId, \
                                                    postCellId, \
                                                    preSegId = 0, \
                                                    preFract = 0.5, \
                                                    postSegId = 0, \
                                                    postFract = 0.5, \
                                                    localInternalDelay = 0, \
                                                    localPreDelay = 0, \
                                                    localPostDelay = 0, \
                                                    localPropDelay = 0, \
                                                    localWeight = 1, \
                                                    localThreshold = 0):
        
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
        
        self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId, localWeight)
        
        if (len(self.projConns)==0):
            
            try:
                exec("from %s import synapse_dynamics as sd" % synapseType)
                exec("from %s import gmax" % synapseType)
                
            except ImportError:
                sd = None
                gmax = 1e-7
                
            self.projWeightFactor[projName] = gmax*1e3  # TODO: Check correct units!!!
            self.projSynapseDynamics[projName] = sd 
          
        delayTotal = float(localInternalDelay) + float(localPreDelay) + float(localPostDelay) + float(localPropDelay)
        
        
        srcCell = self.populations[source][int(preCellId)]
        tgtCell = self.populations[target][int(postCellId)]
        weight = float(localWeight)*self.projWeightFactor[projName]
        
        self.log.info("-- Conn id: "+str(id)+", delay: "+str(delayTotal)+", localWeight: "+ str(localWeight)+", weight: "+ str(weight)+", threshold: "+ str(localThreshold))
        
        self.projConns.append([self.populations[source].id_to_index(srcCell),self.populations[target].id_to_index(tgtCell),weight,delayTotal])
        

        
    #
    #  Overridden from NetworkHandler
    #    
    def finaliseProjection(self, projName, source, target):
        
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
        
        connector=FromListConnector(self.projConns)
        proj = Projection(self.populations[source], self.populations[target], connector, target='excitatory', label=projName, synapse_dynamics=self.projSynapseDynamics[projName]) 
        self.projections[projName] = proj
        self.projConns = []
            
            
    #
    #  Overridden from NetworkHandler
    #            
    def handleInputSource(self, inputName, cellGroup, inputProps=[], size=-1):
        self.printInputInformation(inputName, cellGroup, inputProps, size)
        
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
        
        if size<0:
            self.log.error("Error at handleInputSource! Need a size attribute in sites element to create spike source!")
            return
        
        if inputProps.keys().count("synaptic_mechanism")>0 and inputProps.keys().count("frequency")>0:
            
            freq = float(inputProps["frequency"])
            
            if (self.maxSimLength<0):
                raise ValueError, "The value of maxSimLength must be set!"
            
            numberExp = int(float(self.maxSimLength)*freq)
            
            print "Number of spikes expected in %f ms at %fHz: %d"%(self.maxSimLength, freq, numberExp)
            
            spike_times = self.my_stgen.poisson_generator(rate=freq*1000, t_stop=self.maxSimLength, array=True)
            
            #TODO: check units in nml files and put correct conversion here
            input_population  = Population(size, SpikeSourceArray, {'spike_times': spike_times }, label=inputName)
            
            for ip in input_population:
                
                spikes = self.my_stgen.poisson_generator(rate=freq*1000, t_stop=self.maxSimLength, array=True)
                ip.spike_times = spikes
                #print "--------------------------"
                #print "Spike times: "+ str(ip.spike_times)
                #print "--------------------------"
        
            self.inputCellGroups[inputName] = cellGroup
            self.input_populations[inputName] = input_population
            
            synaptic_mechanism = inputProps["synaptic_mechanism"]
            
            
            try:
                exec("from %s import synapse_dynamics as sd" % synaptic_mechanism)
                exec("from %s import gmax" % synaptic_mechanism)
                
            except ImportError:
                sd = None
                gmax = 1e-7  # TODO: Check correct units!!!
                
            self.inputWeights[inputName] = gmax*1e3  # TODO: Check correct units!!!
            self.inputSynapseDynamics[inputName] = sd
            
            
        
        
    #
    #  Overridden from NetworkHandler
    #          
    def handleSingleInput(self, inputName, cellId, segId = 0, fract = 0.5):
        
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
        
        if self.inputCount.keys().count(inputName)==0:
            self.inputCount[inputName] = 0
            
        weight = float(self.inputWeights[inputName])
            
        self.log.warn("Associating input: %i from: %s to cell %i, weight: %f"%(self.inputCount[inputName],inputName,cellId, weight))
        
        srcInput = self.input_populations[inputName][(self.inputCount[inputName],)]
       
        tgtCell = self.populations[self.inputCellGroups[inputName]][(int(cellId),)]
        
        #TODO use max cond*weight for weight here...
        connTuple = (self.input_populations[inputName].id_to_index(srcInput)[0],self.populations[self.inputCellGroups[inputName]].id_to_index(tgtCell)[0],weight,0.1)
        self.inputConns.append(connTuple)
            
        self.inputCount[inputName]+=1
             
       
        
    #
    #  Overridden from NetworkHandler
    #           
    def finaliseInputSource(self, inputName):
    
        exec("from pyNN.%s import *" % self.my_simulator) # Does this really need to be imported every time?
        
        label = "%s_projection"%inputName
        
        input_population = self.input_populations[inputName]
        cellGroup = self.inputCellGroups[inputName]
        print self.inputConns
        connector=FromListConnector(self.inputConns)
        sd = self.inputSynapseDynamics[inputName]
        
        self.log.info("-- Adding connections for %s: %s from %s to %s with %s" % (inputName, str(connector), str(input_population), str(self.populations[cellGroup]), str(sd)))
            
        input_proj = Projection(input_population, self.populations[cellGroup], connector, target='excitatory', label=label ,synapse_dynamics=self.inputSynapseDynamics[inputName])

        self.input_projections[inputName] = input_proj
        self.inputConns = []
        
        
        
        
        
        
        
        
        
        
        
        