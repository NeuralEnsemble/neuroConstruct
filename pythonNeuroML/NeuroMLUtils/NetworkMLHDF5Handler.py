#
#
#   A class to handle HDF5 based NetworkML files.
#   Calls the appropriate methods in NetworkHandler when cell locations,
#   network connections are found. The NetworkHandler can either print 
#   information, or if it's a class overriding NetworkHandler can create
#   the appropriate network in a simulator dependent fashion
#
#   NOTE: Inputs not currently supported
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#
  
import logging

from SynapseProperties import SynapseProperties

import tables   # pytables for HDF5 support

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
    
    # Note this ensures groups are parsed before datasets. Important for synapse props
    
    for node in g:
        self.log.debug("Sub node: "+ str(node)+ ", class: "+ node._c_classId)
      
        if node._c_classId == 'GROUP':
          self.parseGroup(node)
          
    for node in g:
        self.log.debug("Sub node: "+ str(node)+ ", class: "+ node._c_classId)
      
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
                self.netHandler.handleLocation( int(d[i,0]),                      \
                                            self.currPopulation,     \
                                            self.currentCellType,    \
                                            float(d[i,1]),       \
                                            float(d[i,2]),       \
                                            float(d[i,3]))       \
            
      
    elif self.currentProjectionName!="":
        self.log.debug("Using data for proj: "+ self.currentProjectionName)
        self.log.debug("Size is: "+str(d.shape[0])+" rows of: "+ str(d.shape[1])+ " entries")

        self.netHandler.handleProjection(self.currentProjectionName,
                                         self.currentProjectionSource,
                                         self.currentProjectionTarget,
                                         self.globalSynapseProps,
                                         d.shape[0])
        

        indexId = -1
        indexPreCellId = -1
        indexPreSegId = -1
        indexPreFractAlong= -1
        indexPostCellId = -1
        indexPostSegId = -1
        indexPostFractAlong= -1
        
        id = -1
        preCellId = -1
        preSegId = 0
        preFractAlong= 0.5
        postCellId = -1
        postSegId = 0
        postFractAlong= 0.5
        
        extraParamIndices = {}
        
        
        for attrName in d.attrs._v_attrnames:
            val = d.attrs.__getattr__(attrName)
            
            self.log.debug("Val of attribute: "+ attrName + " is "+ str(val))
            
            if val == 'id' or val[0] == 'id':
                indexId = int(attrName[len('column_'):])
            elif val == 'pre_cell_id' or val[0] == 'pre_cell_id':
                indexPreCellId = int(attrName[len('column_'):])
            elif val == 'pre_segment_id' or val[0] == 'pre_segment_id':
                indexPreSegId = int(attrName[len('column_'):])
            elif val == 'pre_fraction_along' or val[0] == 'pre_fraction_along':
                indexPreFractAlong = int(attrName[len('column_'):])
            elif val == 'post_cell_id' or val[0] == 'post_cell_id':
                indexPostCellId = int(attrName[len('column_'):])
            elif val == 'post_segment_id' or val[0] == 'post_segment_id':
                indexPostSegId = int(attrName[len('column_'):])
            elif val == 'post_fraction_along' or val[0] == 'post_fraction_along':
                indexPostFractAlong = int(attrName[len('column_'):])
            
            for synType in self.globalSynapseProps.keys():
                if str(val).count(synType) >0:
                    extraParamIndices[str(val)] = int(attrName[len('column_'):])
                
        
        self.log.debug("Cols: Id: %d precell: %d, postcell: %d, pre fract: %d, post fract: %d" % (indexId, indexPreCellId, indexPostCellId, indexPreFractAlong, indexPostFractAlong))
        
        self.log.debug("Extra cols: "+str(extraParamIndices) )
        #print d[5,:]
        
        
        for i in range(0, d.shape[0]):
            row = d[i,:]
            localSynapseProps = {}
            synTypes = self.globalSynapseProps.keys()
            for synType in synTypes:
                localSynapseProps[synType] = self.globalSynapseProps[synType].copy()
            
            id =  int(row[indexId])
            
            preCellId =  row[indexPreCellId]
            
            if indexPreSegId >= 0:
              preSegId = int(row[indexPreSegId])
            if indexPreFractAlong >= 0:
              preFractAlong = row[indexPreFractAlong]
            
            postCellId =  row[indexPostCellId]
            
            if indexPostSegId >= 0:
              postSegId = int(row[indexPostSegId])
            if indexPostFractAlong >= 0:
              postFractAlong = row[indexPostFractAlong]

            if len(extraParamIndices)>0:
                for synType in localSynapseProps:
                   for paramName in extraParamIndices.keys():
                     if paramName.count(synType)>0:
                       self.log.debug(paramName +"->"+synType)
                       if paramName.count('weight')>0:
                          localSynapseProps[synType].weight = row[extraParamIndices[paramName]]
                       if paramName.count('internal_delay')>0:
                          localSynapseProps[synType].internalDelay = row[extraParamIndices[paramName]]
                       if paramName.count('pre_delay')>0:
                          localSynapseProps[synType].preDelay = row[extraParamIndices[paramName]]
                       if paramName.count('post_delay')>0:
                          localSynapseProps[synType].postDelay = row[extraParamIndices[paramName]]
                       if paramName.count('prop_delay')>0:
                          localSynapseProps[synType].propDelay = row[extraParamIndices[paramName]]
                       if paramName.count('threshold')>0:
                          localSynapseProps[synType].threshold = row[extraParamIndices[paramName]]
               
            
            
            self.log.debug("Connection %d from %f to %f" % (id, preCellId, postCellId))
        
            for synType in localSynapseProps.keys():
              
                synProps = localSynapseProps[synType]
                
                self.netHandler.handleConnection(self.currentProjectionName, \
                                                id, \
                                                self.currentProjectionSource, \
                                                self.currentProjectionTarget, \
                                                synType, \
                                                preCellId, \
                                                postCellId, \
                                                preSegId, \
                                                preFractAlong, \
                                                postSegId, \
                                                postFractAlong,
                                                synProps.internalDelay, \
                                                synProps.preDelay, \
                                                synProps.postDelay, \
                                                synProps.propDelay, \
                                                synProps.weight, \
                                                synProps.threshold)
    
    
  def startGroup(self, g):
    self.log.debug("Going into a group: "+ g._v_name)
    
    if g._v_name.count('population_')>=1:
        # TODO: a better check to see if the attribute is a str or numpy.ndarray
        self.currPopulation = g._v_attrs.name[0]
        if (len(self.currPopulation)==1):          # i.e. it was a str and just took the first letter...
          self.currPopulation = g._v_attrs.name
        self.currentCellType = g._v_attrs.cell_type[0]
        if (len(self.currentCellType)==1):
          self.currentCellType = g._v_attrs.cell_type
        
        size = -1
        # Peek ahead for size...
        for node in g:
            if node._c_classId == 'ARRAY' and node.name == self.currPopulation:
              size = node.shape[0]
              
        self.log.debug("Found a population: "+ self.currPopulation+", cell type: "+self.currentCellType+", size: "+ str(size))
        
        self.netHandler.handlePopulation(self.currPopulation, self.currentCellType, size)
        
    if g._v_name.count('projection_')>=1:
      
        self.currentProjectionName = g._v_attrs.name[0]
        self.currentProjectionSource = g._v_attrs.source[0]
        self.currentProjectionTarget = g._v_attrs.target[0]
        self.globalSynapseProps = {}
          
        self.log.debug("------    Found a projection: "+ self.currentProjectionName+ ", from "+ self.currentProjectionSource+" to "+ self.currentProjectionTarget)
    
    if g._v_name.count('synapse_props_')>=1:
      
        newSynapseProps = SynapseProperties()
        name = g._v_name[len('synapse_props_'):]
        
        #print dir(g._v_attrs) 
        for attrName in g._v_attrs._v_attrnames:
            val = g._v_attrs.__getattr__(attrName)
            self.log.debug("Val of attribute: "+ attrName + " is "+ str(val))
            
            if attrName == "internal_delay":
               newSynapseProps.internalDelay = val[0]
            if attrName == "pre_delay":
               newSynapseProps.preDelay = val[0]
            if attrName == "post_delay":
               newSynapseProps.postDelay = val[0]
            if attrName == "prop_delay":
               newSynapseProps.propDelay = val[0]
            if attrName == "weight":
               newSynapseProps.weight = val[0]
            if attrName == "threshold":
               newSynapseProps.threshold = val[0]
               
        self.log.debug(str(newSynapseProps) )
        
        self.globalSynapseProps[name] = newSynapseProps
          
        self.log.debug("Global syn props: "+ str(self.globalSynapseProps) )
    
    
  def endGroup(self, g):
    self.log.debug("Coming out of a group: "+ str(g))
  
    if g._v_name.count('population_')>=1:
        self.log.debug("End of population: "+ self.currPopulation+", cell type: "+self.currentCellType)  
        self.currPopulation =""
        self.currentCellType = ""
    
    if g._v_name.count('projection_')>=1:
        self.netHandler.finaliseProjection(self.currentProjectionName, self.currentProjectionSource, self.currentProjectionTarget)
        self.currentProjectionName = ""
        self.currentProjectionSource = ""
        self.currentProjectionTarget = ""
        
    if g._v_name.count('input_')>=1:
        # TODO: support inputs in HDF5
        self.log.debug("Not implemented yet!")
    
    

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
        
        
        
        
        
        
        
        
        
        
        
    