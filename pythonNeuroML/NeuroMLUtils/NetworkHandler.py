#
#
#   A class to handle events from the NeuroMLSaxHandler, etc.
#   This should be overridden by simulator specific implementations
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#



import logging


class NetworkHandler:
        
    log = logging.getLogger("NetworkHandler")

    #
    #  Internal info method
    #
    def printLocationInformation(self, id, cellGroup, cellType, x, y, z):
        position = "(%s, %s, %s)" % (x, y, z)
        self.log.debug("Location "+str(id)+" of cell group: "+cellGroup+", cell type: "+cellType+": "+position)
        

    #
    #  Internal info method
    #        
    def printConnectionInformation(self,  projName, id, source, target, synapseType, preCellId, postCellId):
        self.log.debug("Connection "+str(id)+" of in net conn: "+projName+": cell "+preCellId+" in "+source \
                              +" to cell "+postCellId+" in "+target+", syn: "+ synapseType)
        
        

    #
    #  Should be overridden
    #  
    def handlePopulation(self, cellGroup, cellType, size):
      
        sizeInfo = " as yet unspecified size"
        if (size>=0):
            sizeInfo = " size "+ str(size)+ " cells"
            
        self.log.info("Population: "+cellGroup+", cell type: "+cellType+sizeInfo)
        
        
    #
    #  Should be overridden
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
        
            
    #
    #  Should be overridden
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
        
        self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId)
        
