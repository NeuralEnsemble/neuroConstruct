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

import neuron
from neuron import hoc


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

   
#
#
#   NEURON version of the NetworkHandler for handling network events,
#   e.g. cell locations, connections, etc. These events can come from
#   a SAX parsed NetworkML file, or a parsed HDF5 file, etc.
#
#

class NetManagerNEURON(NetworkHandler):
    
    log = logging.getLogger("NetManagerNEURON")
  
    h = hoc.HocObject()
        
    globalPreSynId = 10000000
    
    preSectionsVsGids = dict()
        
    #
    #  Overridden from NetworkHandler
    #    
    def handlePopulation(self, cellGroup, cellType, size):
      
        if (size>=0):
            sizeInfo = ", size "+ str(size)+ " cells"
            
            self.log.info("Creating population: "+cellGroup+", cell type: "+cellType+sizeInfo)

            self.executeHoc("{ n_"+cellGroup+" = "+ str(size)+" }")
            self.executeHoc("{ n_"+cellGroup+"_local = 0 } ")
            self.executeHoc("objectvar a_"+cellGroup+"[n_"+cellGroup+"]")

        else:
                
            self.log.error("Population: "+cellGroup+", cell type: "+cellType+" specifies no size. Will lead to errors!")
        
  
    #
    #  Overridden from NetworkHandler
    #    
    def handleLocation(self, id, cellGroup, cellType, x, y, z):
        self.printLocationInformation(id, cellGroup, cellType, x, y, z)
                
        newCellName = cellGroup+"_"+str(id)
        
        createCall = "new "+cellType+"(\""+newCellName+"\", \"" +cellType+"\", \"New Cell: "+newCellName+" of type: "+cellType+"\")"
        
        cellInArray = "a_"+cellGroup+"["+str(id)+"]"
        
        setupCreate = "obfunc newCell() { {"+cellInArray+" = "+createCall+"} return "+cellInArray+" }"
        
        self.executeHoc(setupCreate)
        
        newCell = self.h.newCell()
        
        newCell.position(float(x), float(y), float(z))
        
        self.h.allCells.append(newCell)

        self.executeHoc("{n_"+cellGroup+"_local = n_"+cellGroup+"_local + 1}")
        
        self.log.debug("Have just created cell: "+ newCell.reference+" at ("+str(x)+", "+str(y)+", "+str(z)+")")
        
        if self.isParallel == 1:
            self.executeHoc("{ pnm.register_cell(getCellGlobalId(\""+cellGroup+"\", "+id+"), "+cellInArray+") }")

    #
    #  Should be overridden to create cell group/population array
    #
    def handleProjection(self, projName, source, target, synapseTypes, size=-1):

        if (size>=0):
            sizeInfo = " size: "+ str(size)+ " connections."
        else:
            raise Exception('Note: XML & H5 NetworkML files MUST specify the size of the connections so that NEURON can create arrays of synapse objects!')


        self.log.info("Projection: "+projName+" from "+source+" to "+target+" with "+sizeInfo)
        for synapseType in synapseTypes.keys():

            synObjName = projName+"_"+synapseType+"["+str(size)+"]"
            self.log.info("Creating array for syns: "+synObjName)

            self.executeHoc("objref "+synObjName)

        
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
        
        #self.printConnectionInformation(projName, id, source, target, synapseType, preCellId, postCellId, localWeight)
          

        self.log.debug("\n           --------------------------------------------------------------")
        self.log.debug("Going to create a connection of type " +projName+", id: "+str(id)+", synapse type: "+synapseType)
        self.log.debug("From: "+source+", id: "+str(preCellId)+", segment: "+str(preSegId)+", fraction: "+str(preFract))
        self.log.debug("To  : "+target+", id: "+str(postCellId)+", segment: "+str(postSegId)+", fraction: "+str(postFract))
        
            
        delayTotal = float(localInternalDelay) + float(localPreDelay) + float(localPostDelay) + float(localPropDelay)
        
        
        self.log.debug("Delay: "+str(delayTotal)+", weight: "+ str(localWeight)+", threshold: "+ str(localThreshold))
        
        targetCell = "a_"+target+"["+str(postCellId)+"]"
        sourceCell = "a_"+source+"["+str(preCellId)+"]"
        
        
        if self.isParallel == 1:
            self.executeHoc("localSynapseId = -2")
            self.executeHoc("globalPreSynId = "+str(self.globalPreSynId))  # provisional gid for NetCon
            
            
        # Create post syn object    
            
        if self.h.isCellOnNode(str(target), int(postCellId)) == 1:
            self.log.debug("++++++++++++ PostCell: "+targetCell+" is on this host...")
        
            synObjName = projName+"_"+synapseType+"["+str(id)+"]"
            
            ##########self.executeHoc("objref "+synObjName)
            self.executeHoc("{ "+targetCell+".accessSectionForSegId("+str(postSegId)+") }")
                
            self.executeHoc("{ "+"fractSecPost = "+targetCell+".getFractAlongSection("+str(postFract)+", "+str(postSegId)+") }")
            
            self.log.debug("Synapse object at: "+str(h.fractSecPost) +" on sec: "+h.secname()+", or: "+str(postFract)+" on seg id: "+ str(postSegId))
            
            self.executeHoc("{ "+synObjName+" = new "+synapseType+"(fractSecPost) }")
            
            self.executeHoc("{ "+targetCell+".synlist.append("+synObjName+") }")
            
            self.executeHoc("{ "+"localSynapseId = "+targetCell+".synlist.count()-1 }")
            
        else:
            self.log.debug("------------ PostCell: "+targetCell+" is not on this host...")
            
        
        # Create pre syn object  
        
        if self.isParallel == 0:
        
            self.executeHoc("{ "+sourceCell+".accessSectionForSegId("+str(preSegId)+") }")
            self.executeHoc("{ fractSecPre = "+sourceCell+".getFractAlongSection("+str(preFract)+", "+str(preSegId)+") }")
        
            self.log.debug("NetCon object at: "+str(h.fractSecPre) +" on sec: "+h.secname()+", or: "+str(preFract)+" on seg id: "+ str(preSegId))
        
            self.executeHoc("{"+sourceCell+".synlist.append(new NetCon(&v(fractSecPre), " \
                      +synObjName+", "+str(localThreshold)+", "+str(delayTotal)+", "+str(localWeight)+")) }")
        
        else:
          
            netConRef = "NetCon_"+str(self.globalPreSynId)
            netConRefTemp = netConRef+"_temp"
            
            
            preCellSegRef = str(sourceCell+"_"+str(preSegId))
            
            gidToUse = self.globalPreSynId
            
            if  preCellSegRef in self.preSectionsVsGids:
                gidToUse = self.preSectionsVsGids[preCellSegRef]
                self.log.debug("Using *existing* NetCon with gid for pre syn: "+str(gidToUse)+"")
            else:
                self.log.debug("Using new gid for pre syn: "+str(gidToUse)+"")
                self.preSectionsVsGids[preCellSegRef] = self.globalPreSynId
                
                
            if self.h.isCellOnNode(str(source), int(preCellId)) == 1: 
                self.log.debug("++++++++++++ PreCell: "+sourceCell+" is here!!")

                self.executeHoc("objref "+netConRef)
                if  gidToUse == self.globalPreSynId:  # First time use of gid so create NetCon
                    
                    self.executeHoc("{ "+sourceCell+".accessSectionForSegId("+str(preSegId)+")"+" }")
                    
                    self.executeHoc("{ "+"pnm.pc.set_gid2node("+str(gidToUse)+", hostid)"+" }")
                    self.executeHoc("{ "+netConRef+" = new NetCon(&v("+str(preFract) +"), nil)"+" }")
                    
                    self.executeHoc("{ "+netConRef+".delay = "+str(delayTotal)+" }")
                    self.executeHoc("{ "+netConRef+".weight = "+str(localWeight)+" }")
                    self.executeHoc("{ "+netConRef+".threshold = "+str(localThreshold)+" }")
                    
                    self.executeHoc("{ "+"pnm.pc.cell("+str(gidToUse)+", "+netConRef+")"+" }")
                    
          
                
            else: 
                self.log.debug("------------ PreCell: "+sourceCell+" not on this host...")
                
            
            # Connect pre to post  
            
            
            if self.isParallel == 1 and self.h.isCellOnNode(str(target), int(postCellId)) == 1:
                self.executeHoc("objref "+netConRefTemp)
                self.executeHoc(netConRefTemp+" = pnm.pc.gid_connect("+str(gidToUse)+","+targetCell+".synlist.object(localSynapseId))")
           
                self.executeHoc("{ "+netConRefTemp+".delay = "+str(delayTotal)+" }")
                self.executeHoc("{ "+netConRefTemp+".weight = "+str(localWeight)+" }")
                self.executeHoc("{ "+netConRefTemp+".threshold = "+str(localThreshold)+" }")
                
            
            #self.executeHoc("netConInfoParallel("+netConRef+")")
            #self.executeHoc("netConInfoParallel("+netConRefTemp+")")    
                
        self.globalPreSynId+=1
        
        
        
#
#   Helper function for printing hoc before executing it
#
    def executeHoc(self, command):
    
        cmdPrefix = "hoc >>>>>>>>>>: "
        
        if (len(command)>0):
            self.log.debug(cmdPrefix+command)
            self.h(command)
        
        
        
        
        