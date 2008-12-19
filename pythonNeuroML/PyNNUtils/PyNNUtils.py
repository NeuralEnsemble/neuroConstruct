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
        
    populations = {}
    projections = {}  # Note: not yet PyNN "Projections"
    inputSources = {}  
	
    simulator = "neuron"
	
	
    def __init__(self, simulator="neuron"):
        self.log.info("Using simulator: "+simulator)
        self.simulator = simulator
        exec("from pyNN.%s import *" % self.simulator)
        
        setup()
       
    
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
		
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Creating population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
            standardCell = IF_cond_alpha
            
            if (cellType.count("IF_cond_alpha")>=0):
                standardCell = IF_cond_alpha
            # Todo: add more...
            
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
        
        

        
        #self.log.info("Have just created cell: "+ newCell.reference+" at ("+str(x)+", "+str(y)+", "+str(z)+")")
        
        
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
        
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
        
        self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId, localWeight)
        
        if (self.projections.keys().count(projName)==0):
            self.projections[projName] = []
            
        proj = self.projections[projName]
          
        delayTotal = float(localInternalDelay) + float(localPreDelay) + float(localPostDelay) + float(localPropDelay)
        
        self.log.info("Delay: "+str(delayTotal)+", weight: "+ str(localWeight)+", threshold: "+ str(localThreshold))
        
        srcCell = self.populations[source][int(preCellId)]
        tgtCell = self.populations[target][int(postCellId)]
        
        proj.append(connect(srcCell, tgtCell, weight=float(localWeight), delay=float(delayTotal)))
        

        
        
    #
    #  Overridden from NetworkHandler
    #            
    def handleInputSource(self, inputName, cellGroup, synapseType, size=-1):
        
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
        
        if size<0:
            self.log.error("Error at handleInputSource! Need a size attribute in sites element to create spike source!")
            return
        
        input_population  = Population(size, SpikeSourcePoisson, {'rate': 3 }, inputName)
        
        self.inputSources[inputName] = input_population
        