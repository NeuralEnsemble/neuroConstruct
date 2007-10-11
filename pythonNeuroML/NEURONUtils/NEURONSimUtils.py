#
#
#   A class with a number of utilities for use in Python based NEURON
#   simulations run in neuroConstruct
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

import sys
import os
import logging

import hoc


if sys.path.count(os.getcwd())==0:
    sys.path.append(os.getcwd())
    
sys.path.append("../NeuroMLUtils")

from NetworkHandler import NetworkHandler

    
    
h = hoc.HocObject()


def createSimpleGraph(variable, dur, maxy = 50.0, miny = -90):
    h = hoc.HocObject()
    h("objref SampleGraph")
    h("SampleGraph = new Graph(0)")
    h.SampleGraph.size(0,dur,-90.0, maxy)
    h("graphList[0].append(SampleGraph)")
    
    h.SampleGraph.view(0, miny, dur, maxy-miny, 80, 330, 330, 250)
    h.SampleGraph.addexpr(variable, variable, 1, 1, 0.8, 0.9, 2)
    
    
def forp():
  h('forall psection()')
    
def psection():
  h('psection()')

   

class NetManagerNEURON(NetworkHandler):
    
    log = logging.getLogger("NetManagerNEURON")
  
    h = hoc.HocObject()
    
        
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
            self.h("n_"+cellGroup+" = "+ str(size))
            self.h("objectvar a_"+cellGroup+"[n_"+cellGroup+"]")

            #objectvar a_sm1[n_sm1]
        else:
                
            self.log.error("Population: "+cellGroup+", cell type: "+cellType+" specifies no size. Will lead to errors!")
        
  
    #
    #  Overridden from NetworkHandler
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
                
        
        newCellName = cellGroup+"_"+id
        
        
        createCall = "new "+cellType+"(\""+newCellName+"\", \"" +cellType+"\", \"New Cell: "+newCellName+" of type: "+cellType+"\")"
        
        cellInArray = "a_"+cellGroup+"["+id+"]"
        
        setupCreate = "obfunc newCell() { {"+cellInArray+" = "+createCall+"} return "+cellInArray+" }"
        
        self.h(setupCreate)
        newCell = self.h.newCell()
        
        newCell.position(float(x), float(y), float(z))
        
        print newCell.toString()
        
        self.h.allCells.append(newCell)
        
        
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
        
        self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId)
          
        
        self.log.info("Going to create a connection of type " +projName+", id: "+id+", synapse type: "+synapseType)
        self.log.info("From: "+source+", id: "+str(preCellId)+", segment: "+str(preSegId)+", fraction: "+str(preFract))
        self.log.info("To  : "+target+", id: "+str(postCellId)+", segment: "+str(postSegId)+", fraction: "+str(postFract))
        
        
        
        synObjName = projName+"_"+synapseType+"_"+id
        
        objRefCommand = "objref "+synObjName
        
        self.log.info("objRefCommand: "+objRefCommand)
        
        self.h(objRefCommand)
        
        
        
        accessPostCommand = "a_"+target+"["+str(postCellId)+"].accessSectionForSegId("+postSegId+")"
        
        self.log.info("accessPostCommand: "+accessPostCommand)
        
        self.h(accessPostCommand)
        
        
        
        fractPostCommand = "fractSecPost = a_"+target+"["+str(postCellId)+"].getFractAlongSection("+str(postFract)+", "+str(postSegId)+")"
        
        self.log.info("fractPostCommand: "+fractPostCommand)
        
        self.h(fractPostCommand)
        self.log.info("Synapse object at: "+str(h.fractSecPost) +" on sec: "+h.secname()+", or: "+str(postFract)+" on seg id: "+ str(postSegId))
        
        
        createCommand = synObjName+" = new "+synapseType+"(fractSecPost)"
        
        self.log.info("createCommand: "+createCommand)
        
        self.h(createCommand)
        
        
        
        accessPreCommand = "a_"+source+"["+str(preCellId)+"].accessSectionForSegId("+preSegId+")"
        
        self.log.info("accessPreCommand: "+accessPreCommand)
        
        self.h(accessPreCommand)
        
        
        
        fractPreCommand = "fractSecPre = a_"+source+"["+str(preCellId)+"].getFractAlongSection("+str(preFract)+", "+str(preSegId)+")"
        
        self.log.info("fractPreCommand: "+fractPreCommand)
        
        self.h(fractPreCommand)
        self.log.info("NetCon object at: "+str(h.fractSecPre) +" on sec: "+h.secname()+", or: "+str(preFract)+" on seg id: "+ str(preSegId))
        
        
        
        
        delayTotal = float(localInternalDelay) + float(localPreDelay) + float(localPostDelay) + float(localPropDelay)
        
        connectCommand = "a_"+source+"["+str(preCellId)+"].synlist.append(new NetCon(&v(fractSecPre), "+synObjName+", "+localThreshold+", "+str(delayTotal)+", "+localWeight+"))"
               
        self.log.info("connectCommand: "+connectCommand)
        
        self.h(connectCommand)
        
        
        
        