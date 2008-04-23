#
#
#   A class to handle HDF5 based NetworkML files.
#   Calls the appropriate methods in NetworkHandler when cell locations,
#   network connections are found. The NetworkHandler can either print 
#   information, or if it's a class overriding NetworkHandler can create
#   the appropriate network in a simulator dependent fashion
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

import xml.sax
import logging

import tables

class NetworkMLHDF5Handler():
    
  log = logging.getLogger("NetworkMLHDF5Handler")
  
  myNodeId = -1    # If myNodeId <0, ignore node_id attribute and handle all cells, otherwise handle only cells with myNodeId = node_id
  
  currPopulation = ""
  currentCellType = ""
  totalInstances = 0
  
  currentProjectionName = ""
  currentProjectionSource = ""
  currentProjectionTarget = ""
  
  isSynapseTypeElement = 0
  
  globalSynapseProps = {}
  localSynapseProps = {}
  
  latestSynapseType = ""
    
    
    
  def setNodeId(self, node_id):
    self.myNodeId = node_id
       
    
  def __init__ (self, netHandler): 
    self.netHandler = netHandler
    
  def parse(self, filename):
    h5file=tables.openFile(filename)
    
    self.log.debug("Opened HDF5 file: "+ h5file.filename)
    
    self.parseGroup(h5file.root.networkml)
    
    h5file.close()
    
    
    
  def parseGroup(self, g):
    self.log.debug("Parsing group: "+ str(g))
    self.startGroup(g)
    
    for node in g:
        self.log.debug("Sub node: "+ str(node)+ ", class: "+ node._c_classId)
      
        if node._c_classId == 'GROUP':
          self.parseGroup(node)
        if node._c_classId == 'ARRAY':
          self.parseDataset(node)
    self.endGroup(g)
    
    
  def parseDataset(self, d):
    self.log.debug("Parsing dataset/array: "+ str(d))
    if self.currPopulation!="":
        self.log.debug("Using data for population: "+ self.currPopulation)
        self.log.debug("Size is: "+str(d.shape[0])+" rows of: "+ str(d.shape[1])+ " entries")
        
        for i in range(0, d.shape[0]):
            if self.myNodeId == -1 or (d.shape[1] > 4 and self.myNodeId == d[i,4]):
                self.netHandler.handleLocation( d[i,0],                      \
                                            self.currPopulation,     \
                                            self.currentCellType,    \
                                            d[i,1],       \
                                            d[i,2],       \
                                            d[i,3])       \
            
      
    
    
    
  def startGroup(self, g):
    self.log.debug("Going into a group: "+ g._v_hdf5name)
    
    
    if g._v_hdf5name.count('population_')>=1:
        # TODO: a better check to see if the attribute is a str or numpy.ndarray
        self.currPopulation = g._v_attrs.name[0]
        if (len(self.currPopulation)==1):          # i.e. it was a str and just took the first letter...
          self.currPopulation = g._v_attrs.name
        self.currentCellType = g._v_attrs.cell_type[0]
        if (len(self.currentCellType)==1):
          self.currentCellType = g._v_attrs.cell_type
        
        self.log.info("Found a population: "+ self.currPopulation+", cell type: "+self.currentCellType)  
      
    
  def endGroup(self, g):
    self.log.debug("Coming out of a group: "+ str(g))
  
    if g._v_hdf5name.count('population_')>=1:
        self.log.debug("End of population: "+ self.currPopulation+", cell type: "+self.currentCellType)  
        self.currPopulation =""
        self.currentCellType = ""
    
    
    
          
class SynapseProperties():

    internalDelay = 0   # default from NetworkML.xsd
    preDelay = 0        # default from NetworkML.xsd
    postDelay = 0       # default from NetworkML.xsd
    propDelay = 0       # default from NetworkML.xsd
    weight = 1          # default from NetworkML.xsd
    threshold = 0       # default from NetworkML.xsd
    
    def __str__(self):
        return ("SynapseProperties: internalDelay: %s, preDelay: %s, postDelay: %s, propDelay: %s, weight: %s, threshold: %s" \
                    % (self.internalDelay, self.preDelay, self.postDelay, self.propDelay, self.weight, self.threshold))
                    
    def copy(self):
        sp = SynapseProperties()
        sp.internalDelay = self.internalDelay
        sp.preDelay = self.preDelay
        sp.postDelay = self.postDelay
        sp.propDelay = self.propDelay
        sp.weight = self.weight
        sp.threshold = self.threshold
        
        return sp
        
        
        
        
        
        
        
        
        
        
        
    