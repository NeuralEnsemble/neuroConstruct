import xml.sax
import logging

""" Using NetworkML SAX handler instead...


class NeuroMLSaxHandler(xml.sax.ContentHandler):
    
  log = logging.getLogger("NeuroMLSaxHandler")
  
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
  
  ###############################  Note: multiple syn types per connection need to be supported!!!!!
  
  isSynapseTypeElement = 0
  currentSynapseType = ""
  
  globalInternalDelay = 0   # default from NetworkML.xsd
  globalPreDelay = 0   # default from NetworkML.xsd
  globalPostDelay = 0   # default from NetworkML.xsd
  globalPropDelay = 0   # default from NetworkML.xsd
  globalWeight = 1          # default from NetworkML.xsd
  globalThreshold = 0       # default from NetworkML.xsd
  
  localInternalDelay = 0   # default from NetworkML.xsd
  localPreDelay = 0   # default from NetworkML.xsd
  localPostDelay = 0   # default from NetworkML.xsd
  localPropDelay = 0   # default from NetworkML.xsd
  localWeight = 1          # default from NetworkML.xsd
  localThreshold = 0       # default from NetworkML.xsd
  
    
  def __init__ (self, netManager): 
    self.netManager = netManager
    
    
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
      
    elif name == 'instance':
      include = 0
      
      if attrs.get('node_id',"") != "":
        self.log.debug("Found node_id: "+ attrs.get('node_id',""))
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
        
        self.log.info('Adding instance: ' +self.currentInstanceId)
        
        self.netManager.handleLocation(self.currentInstanceId, self.currentCellGroup, self.currentCellType, attrs.get('x',""),\
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
          
    elif name == 'pre':
      if self.currentProjectionName != "":
        self.preCellId = attrs.get('cell_id',"")   
        if attrs.has_key('segment_id'):
            self.preSegId = attrs.get('segment_id',"")  
        if attrs.has_key('fraction_along'):
            self.preFract = attrs.get('fraction_along',"")  
        self.log.debug("Found pre: "+ self.preCellId)         
        
    elif name == 'post':
      if self.currentProjectionName != "":
        self.postCellId = attrs.get('cell_id',"")   
        if attrs.has_key('segment_id'):
            self.postSegId = attrs.get('segment_id',"")  
        if attrs.has_key('fraction_along'):
            self.postFract = attrs.get('fraction_along',"")  
        self.log.debug("Found pre: "+ self.postCellId)          
           
    elif name == 'synapse_type':
      if self.currentProjectionName != "":    
          self.isSynapseTypeElement = 1         
          
    elif name == 'default_values':
      if self.currentProjectionName != "":  
        if attrs.has_key('internal_delay'):
            self.globalInternalDelay = attrs.get('internal_delay',"")    
        if attrs.has_key('pre_delay'):
            self.globalPreDelay = attrs.get('pre_delay',"")    
        if attrs.has_key('post_delay'):
            self.globalPostDelay = attrs.get('post_delay',"")    
        if attrs.has_key('prop_delay'):
            self.globalPropDelay = attrs.get('prop_delay',"")    
        if attrs.has_key('weight'):
            self.globalWeight = attrs.get('weight',"")    
        if attrs.has_key('threshold'):
            self.globalThreshold = attrs.get('threshold',"")    
            
            
    elif name == 'properties':
      if self.currentProjectionName != "":  
        if attrs.has_key('internal_delay'):
            self.localInternalDelay = attrs.get('internal_delay',"")    
        if attrs.has_key('pre_delay'):
            self.localPreDelay = attrs.get('pre_delay',"")    
        if attrs.has_key('post_delay'):
            self.localPostDelay = attrs.get('post_delay',"")    
        if attrs.has_key('prop_delay'):
            self.localPropDelay = attrs.get('prop_delay',"")    
        if attrs.has_key('weight'):
            self.localWeight = attrs.get('weight',"")    
        if attrs.has_key('threshold'):
            self.localThreshold = attrs.get('threshold',"")    
                                   
    
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
     self.currentSynapseType = ch
     
         
         
  def endElement(self, name): 
    if name == 'instance':
      self.currentInstanceId = -1
      
    elif name == 'cell_type':
      self.isCellTypeElement = 0    
      
    elif name == 'population':
      self.currentCellGroup = ""
      
    elif name == 'instances':
      self.log.info("Found %d location instances"% (self.totalInstances))
      
    elif name == 'projection':
      self.currentProjectionName = ""
      self.globalInternalDelay = 0   # default from NetworkML.xsd
      self.globalPreDelay = 0   # default from NetworkML.xsd
      self.globalPostDelay = 0   # default from NetworkML.xsd
      self.globalPropDelay = 0   # default from NetworkML.xsd
      self.globalWeight = 1          # default from NetworkML.xsd
      self.globalThreshold = 0       # default from NetworkML.xsd
      
    elif name == 'source':
      self.isSourceElement = 0
      
    elif name == 'target':
      self.isTargetElement = 0
      
    if name == 'connection':
      
      self.netManager.handleConnection(self.currentProjectionName, \
                                       self.currentConnId, \
                                       self.currentProjectionSource, \
                                       self.currentProjectionTarget, \
                                       self.currentSynapseType, \
                                       self.preCellId, \
                                       self.postCellId, \
                                       self.preSegId, \
                                       self.preFract, \
                                       self.postSegId, \
                                       self.postFract, \
                                       self.localInternalDelay, \
                                       self.localPreDelay, \
                                       self.localPostDelay, \
                                       self.localPropDelay, \
                                       self.localWeight, \
                                       self.localThreshold)
        
      self.currentConnId = -1
      self.localInternalDelay = 0   # default from NetworkML.xsd
      self.localPreDelay = 0   # default from NetworkML.xsd
      self.localPostDelay = 0   # default from NetworkML.xsd
      self.localPropDelay = 0   # default from NetworkML.xsd
      self.localWeight = 1          # default from NetworkML.xsd
      self.localThreshold = 0       # default from NetworkML.xsd
      
    elif name == 'synapse_type':
      self.isSynapseTypeElement = 0    
      self.log.debug("Found synapse_type: "+ self.currentSynapseType)       
      
           
            """
    