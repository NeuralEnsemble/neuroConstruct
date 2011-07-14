#
#
#   A class to handle events from the NeuroMLSaxHandler, NetworkMLHDF5Handler, etc.
#   This should be overridden by simulator specific implementations.
#   File parsing classes, e.g. NetworkMLSaxHandler should call the appropriate
#   function here when a cell location, connection, etc. is encountered.
#
#   Use of this handler class should mean that the network setup is 
#   independent of the source of the network info (XML or HDF5 based NeuroML
#   files for example) and the instantiator of the network objects (NetManagerNEURON
#   or PyNN based setup class)
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
    
    isParallel = 0

    #
    #  Internal info method, can be reused in overriding classes for debugging
    #
    def printLocationInformation(self, id, cellGroup, cellType, x, y, z):
        position = "(%s, %s, %s)" % (x, y, z)
        self.log.debug("Location "+str(id)+" of cell group: "+cellGroup+", cell type: "+cellType+": "+position)
        

    #
    #  Internal info method, can be reused in overriding classes for debugging
    #        
    def printConnectionInformation(self,  projName, id, source, target, synapseType, preCellId, postCellId, weight):
        self.log.debug("Connection "+str(id)+" of: "+projName+": cell "+str(preCellId)+" in "+source \
                              +" -> cell "+str(postCellId)+" in "+target+", syn: "+ str(synapseType)+", weight: "+str(weight))
        
         
    #
    #  Internal info method, can be reused in overriding classes for debugging
    #        
    def printInputInformation(self, inputName, cellGroup, inputProps, size=-1):
        sizeInfo = " size: "+ str(size)+ " cells"
        self.log.debug("Input Source: "+inputName+", on population: "+cellGroup+sizeInfo+" with props: "+ str(inputProps))
        
    

    #
    #  Should be overridden to create cell group/population array
    #  
    def handlePopulation(self, cellGroup, cellType, size=-1):
      
        sizeInfo = " as yet unspecified size"
        if (size>=0):
            sizeInfo = " size: "+ str(size)+ " cells"
            
        self.log.info("Population: "+cellGroup+", cell type: "+cellType+sizeInfo)
        
        
    #
    #  Should be overridden to create specific cell instance
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
        


    #
    #  Should be overridden to create cell group/population array
    #
    def handleProjection(self, projName, source, target, synapseTypes, size=-1):

        sizeInfo = " as yet unspecified size"
        if (size>=0):
            sizeInfo = " size: "+ str(size)+ " connections"

        self.log.info("Projection: "+projName+" from "+source+" to "+target+" with syns: "+str(synapseTypes)+" with "+sizeInfo)


    #
    #  Should be overridden to handle network connection
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
        if preSegId != 0 or postSegId!=0 or preFract != 0.5 or postFract != 0.5:
            self.log.debug("Src cell: %d, seg: %f, fract: %f -> Tgt cell %d, seg: %f, fract: %f" % (preCellId,preSegId,preFract,postCellId,postSegId,postFract))
        
    #
    #  Should be overridden to handle end of network connection
    #  
    def finaliseProjection(self, projName, source, target):
   
        self.log.info("Projection: "+projName+" from "+source+" to "+target+" completed")
        
        
    #
    #  Should be overridden to create input source array
    #  
    def handleInputSource(self, inputName, cellGroup, inputProps=[], size=-1):
        self.printInputInformation(inputName, cellGroup, inputProps, size)
        
        if size<0:
            self.log.error("Error! Need a size attribute in sites element to create spike source!")
            return
             
        
    #
    #  Should be overridden to to connect each input to the target cell
    #  
    def handleSingleInput(self, inputName, cellId, segId = 0, fract = 0.5):
        self.log.debug("Input : %s, cellId: %i, seg: %i, fract: %f" % (inputName,cellId,segId,fract))
        
        
    #
    #  Should be overridden to to connect each input to the target cell
    #  
    def finaliseInputSource(self, inputName):
        self.log.info("Input : %s completed" % inputName)
        
        

    #
    #  To signify network is distributed over parallel nodes
    #    
    def setParallelStatus(self, val):
        
        self.log.debug("Parallel status (0=serial mode, 1=parallel distributed): "+str(val))
        self.isParallel = val
        