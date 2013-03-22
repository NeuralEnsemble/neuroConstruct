#
#
#   A class which stores the contents of a NeuroML file
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

import sys
import logging


sys.path.append("../NeuroMLUtils")

from NetworkHandler import NetworkHandler
from NetworkMLNet import NetworkMLNet
    


class NetworkHolder(NetworkHandler):
    
    log = logging.getLogger("NetworkHolder")
  
    nmlNet = NetworkMLNet()
   
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        newPop = self.nmlNet.addPopulation(cellGroup, cellType)
        
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Creating population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
        else:
                
            self.log.error("Population: "+cellGroup+", cell type: "+cellType+" specifies no size. May lead to errors!")
        
  
    #
    #  Overridden from NetworkHandler
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
        
        self.nmlNet.getPopulation(cellGroup).addInstance(x,y,z)
          
    #
    #  Overridden from NetworkHandler
    #
    def handleProjection(self, projName, source, target, synapseTypes, size=-1):

        proj = self.nmlNet.addProjection(projName, source, target, size)

        sizeInfo = " as yet unspecified size"
        if (size>=0):
            sizeInfo = " size: "+ str(size)+ " connections"

        self.log.info("Projection: "+projName+" from "+source+" to "+target+" with syns: "+str(synapseTypes.keys())+" with "+sizeInfo)
     
        
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
        
        self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId, localWeight)
          
        
        proj = self.nmlNet.getProjection(projName)
        if proj == None:
            proj = self.nmlNet.addProjection(projName, source, target)
            
            
        # NOTE: segment ID, fractalong, synapse props not supported yet in NetworkMLFile!!
        proj.addConnection(preCellId, postSegId)
        
        
