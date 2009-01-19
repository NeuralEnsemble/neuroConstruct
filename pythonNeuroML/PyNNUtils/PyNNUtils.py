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
    inputCellGroups = {}  
    
    inputCount = {}
	
    simulator = "neuron"
    
    myRandNumGen = None
    
    maxSimLength = -1 # Needed for generating input spike time array...
	
	
    def __init__(self, simulator="neuron"):
        self.log.info("Using simulator: "+simulator)
        self.simulator = simulator
        exec("from pyNN.%s import *" % self.simulator)
        
        setup()
        
        
    def setRandNumGen(self, rng):
        self.myRandNumGen = rng
        
        
    def setMaxSimLength(self, length):
        self.maxSimLength = length
       
    
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
		
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Creating population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
            standardCell = IF_cond_alpha
            
            try:
                # Try importing a files named after that cell
                exec("from %s import *" % cellType)
                exec("standardCell = %s" % cellType)
                
            except ImportError:
                # Else check if it refers to a standard type
                if (cellType.count("IF_cond_alpha")>=0):
                    standardCell = IF_cond_alpha
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
        
        proj.append(connect(srcCell, tgtCell, weight=float(localWeight)/100, delay=float(delayTotal)))
        

      
        
    #
    #  Overridden from NetworkHandler
    #            
    def handleInputSource(self, inputName, cellGroup, inputProps=[], size=-1):
        self.printInputInformation(inputName, cellGroup, inputProps, size)
        
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
        
        if size<0:
            self.log.error("Error at handleInputSource! Need a size attribute in sites element to create spike source!")
            return
        
        if inputProps.keys().count("synaptic_mechanism")>0 and inputProps.keys().count("frequency")>0:
            
            freq = float(inputProps["frequency"])
            
            if (self.maxSimLength<0):
                raise ValueError, "The value of maxSimLength must be set!"
            
            numberExp = int(float(self.maxSimLength)*freq)
            
            print "Number of spikes expected in %f ms at %fHz: %d"%(self.maxSimLength, freq, numberExp)
            
            spike_times = numpy.add.accumulate(numpy.random.exponential(1/freq, size=numberExp))
            spike_times = [200,500,900,1500,1505,1510,1515,1518,1520,1522,1525,1528,1530,1532,1535,1538,1540]
            #print spike_times
            
            #TODO: check units in nml files and put correct conversion here
            input_population  = Population(size, SpikeSourceArray, {'spike_times': spike_times }, inputName)
            
            for ip in input_population:
                
                print "--------------------------"
                spikes = numpy.add.accumulate(numpy.random.exponential(1/freq, size=numberExp))
                
                #print ip.cellclass
                #print dir(ip)
                #print input_population.locate(ip)
                #print input_population[input_population.locate(ip)].__class__
                #print input_population.index(1).cellclass
                print ip.spike_times.__class__
                print ip.get_parameters()
                #print ip.cellclass.describe()
                
                ip.spike_times = spikes
                
                print ip.get_parameters()
                '''#ip.spike_times = spikes#list(ip.spike_times)
                
                #print ip.get_parameters()
                
                #print lSpikes.__class__
                
                #ip.spike_times=[]
                
                #ip.spike_times.append(33)
                
                #f = SpikeSourceArray([200,300,400])
                
                print spikes.__class__
                
                print dir(ip.spike_times)
                                
                for t in ip.spike_times:
                    ip.spike_times.remove(t)
                    
                print ip.spike_times
                
                newSpikes = []
                print newSpikes
                print newSpikes.__class__
                #ip.set_parameters(spike_times=newSpikes)
                #dir(newSpikes)
                
                #print dir(ip)
                #print input_population[input_population.locate(ip)]
                #input_population[input_population.locate(ip)].set_parameters(spike_times=spikes)
                
                #print ip.default_parameters
                print ip.get_parameters()
                print "--------------------------"'''
        
            self.inputCellGroups[inputName] = cellGroup
        
            self.inputSources[inputName] = input_population
        
        
    #
    #  Overridden from NetworkHandler
    #          
    def handleSingleInput(self, inputName, cellId, segId = 0, fract = 0.5):
        
        exec("from pyNN.%s import *" % self.simulator) # Does this really need to be imported every time?
        
        if self.inputCount.keys().count(inputName)==0:
            self.inputCount[inputName] = 0
            
        self.log.info("Associating input: %i from: %s to cell %i"%(self.inputCount[inputName],inputName,cellId))
        
        srcInput = self.inputSources[inputName][(self.inputCount[inputName],)]
       
        tgtCell = self.populations[self.inputCellGroups[inputName]][(int(cellId),)]
        #TODO use max cond*weight for weight here...
        connect(srcInput, tgtCell, weight=0.005)
            
        self.inputCount[inputName]+=1
             
        
    
        