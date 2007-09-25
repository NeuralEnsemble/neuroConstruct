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
    
        
    def handlePopulation(self, cellGroup, cellType, size):
      
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Population: "+cellGroup+", cell type: "+cellType+sizeInfo)
            
            self.h("n_"+cellGroup+" = "+ str(size))
            self.h("objectvar a_"+cellGroup+"[n_"+cellGroup+"]")

            #objectvar a_sm1[n_sm1]
        else:
                
            self.log.error("Population: "+cellGroup+", cell type: "+cellType+" specifies no size. Will lead to errors!")
        
  
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
        
        
        
          
        
        
        
        