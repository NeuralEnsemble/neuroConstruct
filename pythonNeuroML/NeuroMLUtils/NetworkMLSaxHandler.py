#
#
#   A class to handle SAX events, specifically for a NetworkML file.
#   Calls the appropriate methods in NetworkHandler when cell locations,
#   network connections are found
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

import xml.sax
import logging

class NetworkMLSaxHandler(xml.sax.ContentHandler):
    
  log = logging.getLogger("NetworkMLSaxHandler")
  
  myNodeId = -1
  
  isCellTypeElement = 0
  currentInstanceId = -1
  currentCellGroup = ""
  currentCellType = ""
  totalInstances = 0
  
  currentProjectionName = ""
  isSourceElement = 0
  isTargetElement = 0
  currentProjectionSource = ""
  currentProjectionTarget = ""
  
  currentConnId = -1
  
  preCellId = -1
  preSegId = -1
  preFract = -1
  postCellId = -1
  postSegId = -1
  postFract = -1
  
  
  isSynapseTypeElement = 0
  
  globalSynapseProps = {}
  localSynapseProps = {}
  
  latestSynapseType = ""
    
    
  def __init__ (self, netHandler): 
    self.netHandler = netHandler
    
    
  def setNodeId(self, node_id):
    self.myNodeId = node_id
       
    
    
  def startElement(self, name, attrs):

    if name == 'networkml':     
      self.log.debug("Found main networkml element")
      self.log.debug("lengthUnits: "+ attrs.get('lengthUnits',""))
      
      
    elif name == 'notes':
      self.log.debug("Found notes element")
      
      
    elif name == 'population':
      self.currentCellGroup = attrs.get('name',"")
      self.log.debug("Found population/cellGroup element: "+ self.currentCellGroup)
      
      
    elif name == 'instances':
      size = -1
      if attrs.get('size',"") != "":
          size = int(attrs.get('size',""))
      self.netHandler.handlePopulation(self.currentCellGroup, self.currentCellType, size)
      
      
    elif name == 'instance':
      include = 0
      if attrs.get('node_id',"") != "":
        self.log.debug("Found node_id: "+ attrs.get('node_id',""))
        if self.netHandler.isParallel!=1: self.netHandler.setParallelStatus(1)
        if int(attrs.get('node_id',"")) == self.myNodeId:
            self.log.debug("Including...")
            include = 1
        else:
            self.log.debug("Excluding from host id:  %d..."%self.myNodeId)
            include = 0
      else:
        self.log.debug("Found no node_id...")
        include = 1
        
      if include == 1:
        self.currentInstanceId = attrs.get('id',"")
        self.log.debug("Found instance element: "+ self.currentInstanceId)
      
      
    elif name == 'cell_type':
      self.isCellTypeElement = 1
      
      
    elif name == 'location':
      if self.currentInstanceId != -1:
        self.log.debug("Found location: "+ attrs.get('x',""))
        self.totalInstances+=1
        nodeInfo = ""
        if self.myNodeId >= 0:
            nodeInfo = " on node id: %d"%self.myNodeId 
        
        self.log.info('Adding instance: ' +self.currentInstanceId+" of group: "+self.currentCellGroup+nodeInfo)
        
        self.netHandler.handleLocation(self.currentInstanceId, self.currentCellGroup, self.currentCellType, attrs.get('x',""),\
                                    attrs.get('y',""), \
                                    attrs.get('z',""))
    
    
    elif name == 'projection':
      self.currentProjectionName = attrs.get('name',"")   
      self.log.debug("Found projection element: "+ self.currentProjectionName)        
      
      
    elif name == 'source':
      if self.currentProjectionName != "":
          self.isSourceElement = 1
           
           
    elif name == 'target':
      if self.currentProjectionName != "":    
          self.isTargetElement = 1  
          
          
    elif name == 'connection':
      if self.currentProjectionName != "":
        self.currentConnId = attrs.get('id',"")   
        self.log.debug("Found connection element: "+ self.currentConnId)  
        
        self.localSynapseProps.clear()
        synTypes = self.globalSynapseProps.keys()
        for synType in synTypes:
            self.localSynapseProps[synType] = self.globalSynapseProps[synType].copy()
        
          
          
    elif name == 'pre':
      if self.currentProjectionName != "":
        self.preCellId = attrs.get('cell_id',"")   
        if attrs.has_key('segment_id'):
            self.preSegId = attrs.get('segment_id',"") 
        else:
            self.preSegId = 0
        if attrs.has_key('fraction_along'):
            self.preFract = attrs.get('fraction_along',"")  
        else:
            self.preFract = 0.5
        self.log.debug("Found pre: "+ self.preCellId)         
        
        
    elif name == 'post':
      if self.currentProjectionName != "":
        self.postCellId = attrs.get('cell_id',"")   
        if attrs.has_key('segment_id'):
            self.postSegId = attrs.get('segment_id',"")  
        else:
            self.postSegId = 0
        if attrs.has_key('fraction_along'):
            self.postFract = attrs.get('fraction_along',"")  
        else:
            self.postFract = 0.5
        self.log.debug("Found pre: "+ self.postCellId)          
           
           
    elif name == 'synapse_type':
      if self.currentProjectionName != "":    
          self.isSynapseTypeElement = 1         
          
          
    elif name == 'default_values':
      if self.currentProjectionName != "":  
        newSynapseProps = SynapseProperties()
        
        if attrs.has_key('internal_delay'):
            newSynapseProps.internalDelay = attrs.get('internal_delay',"")    
        if attrs.has_key('pre_delay'):
            newSynapseProps.preDelay = attrs.get('pre_delay',"")    
        if attrs.has_key('post_delay'):
            newSynapseProps.postDelay = attrs.get('post_delay',"")    
        if attrs.has_key('prop_delay'):
            newSynapseProps.propDelay = attrs.get('prop_delay',"")    
        if attrs.has_key('weight'):
            newSynapseProps.weight = attrs.get('weight',"")    
        if attrs.has_key('threshold'):
            newSynapseProps.threshold = attrs.get('threshold',"")
                
        self.globalSynapseProps[self.latestSynapseType] = newSynapseProps
            
        for synProp in self.globalSynapseProps.keys():
            self.log.debug("globalSynapseProp "+synProp+": "+ str(self.globalSynapseProps[synProp]))      
            
            
    elif name == 'properties':
      if self.currentProjectionName != "":  
        
        synapse_type = ""
        
        if attrs.has_key('synapse_type'):
            synapse_type = attrs.get('synapse_type',"")  
        elif len(self.localSynapseProps) == 1:
            synapse_type = self.localSynapseProps.keys()[0]
            
        if synapse_type != "":
            synProps = self.localSynapseProps[synapse_type]
            self.log.debug("Changing values of local syn props of: "+ synapse_type+", was: "+ str(synProps))          
        
            if attrs.has_key('internal_delay'):
                synProps.internalDelay = attrs.get('internal_delay',"")    
            if attrs.has_key('pre_delay'):
                synProps.preDelay = attrs.get('pre_delay',"")    
            if attrs.has_key('post_delay'):
                synProps.postDelay = attrs.get('post_delay',"")    
            if attrs.has_key('prop_delay'):
                synProps.propDelay = attrs.get('prop_delay',"")    
            if attrs.has_key('weight'):
                synProps.weight = attrs.get('weight',"")    
            if attrs.has_key('threshold'):
                synProps.threshold = attrs.get('threshold',"")    
                      
            self.log.info("......................   Changed values of local syn props: "+ synapse_type+": "+ str(synProps))                         
    
    return
    
    
    
  def characters (self, ch):
   if self.isCellTypeElement== 1:
     self.currentCellType = ch
     
   if self.isSourceElement== 1:
     self.currentProjectionSource = ch
     
   if self.isTargetElement== 1:
     self.currentProjectionTarget = ch
     self.log.debug("Projection: "+ self.currentProjectionName+" is from: "+ self.currentProjectionSource +" to: "+ self.currentProjectionTarget)        
     
   if self.isSynapseTypeElement== 1:
     self.latestSynapseType = ch
     
         
         
  def endElement(self, name):
      
    if name == 'instance':
      self.currentInstanceId = -1
      
      
    elif name == 'cell_type':
      self.isCellTypeElement = 0    
      
      
    elif name == 'population':
      self.currentCellGroup = ""
      
      
    elif name == 'instances':
      self.log.info("Dealt with %d location instances"% (self.totalInstances))
      
      
    elif name == 'projection':
      self.currentProjectionName = ""
      self.globalSynapseProps.clear()
      
      
    elif name == 'source':
      self.isSourceElement = 0
      
      
    elif name == 'target':
      self.isTargetElement = 0
      
      
    elif name == 'connection':
      
      for synType in self.localSynapseProps.keys():
      
        synProps = self.localSynapseProps[synType]
        
        self.netHandler.handleConnection(self.currentProjectionName, \
                                        self.currentConnId, \
                                        self.currentProjectionSource, \
                                        self.currentProjectionTarget, \
                                        synType, \
                                        self.preCellId, \
                                        self.postCellId, \
                                        self.preSegId, \
                                        self.preFract, \
                                        self.postSegId, \
                                        self.postFract, \
                                        synProps.internalDelay, \
                                        synProps.preDelay, \
                                        synProps.postDelay, \
                                        synProps.propDelay, \
                                        synProps.weight, \
                                        synProps.threshold)
        
      self.currentConnId = -1
      self.localSynapseProps.clear()
      
      
    elif name == 'synapse_type':
      self.isSynapseTypeElement = 0    
      self.log.debug("Found end of synapse_type: "+ self.latestSynapseType)       
      
           
          
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
        
        
        
        
        
        
        
        
        
        
        
    