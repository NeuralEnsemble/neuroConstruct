#
#
#   A class to handle SAX events, specifically for a NetworkML file.
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

from SynapseProperties import SynapseProperties

class NetworkMLSaxHandler(xml.sax.ContentHandler):
    
  log = logging.getLogger("NetworkMLSaxHandler")
  
  myNodeId = -1    # If myNodeId <0, ignore node_id attribute and handle all cells, otherwise handle only cells with myNodeId = node_id
  
  currentElementList = []
  
  
  isCellTypeElement = 0
  currentInstanceId = -1
  currPopulation = ""
  currentCellType = ""
  totalInstances = 0
  
  currentProjectionName = ""
  isSourceElement = 0
  isTargetElement = 0
  currentProjectionSource = ""
  currentProjectionTarget = ""
  
  currentConnId = -1
  
  
  currentInputName = ""
  currentInputProps = {}
  currentInputTarget = ""
  currentInputId = -1
  
  
  preCellId = -1
  preSegId = 0
  preFract = -1
  postCellId = -1
  postSegId = 0
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

    self.currentElementList.append(name)
    
    #for el in self.currentElementList:
    #    print "<"+el+">"
    
    if name == 'networkml':     
      self.log.debug("Found main networkml element")
      self.log.debug("lengthUnits: "+ attrs.get('lengthUnits',""))
      
      
    elif name == 'notes' or name == 'meta:notes':
      self.log.debug("Found notes element")
      
      
    elif name == 'population':
      self.currPopulation = attrs.get('name',"")
      self.log.debug("Found population/cellGroup element: "+ self.currPopulation)
      if attrs.get('cell_type',"") != "":
          self.currentCellType = attrs.get('cell_type',"")     # post NeuroML v1.7.1 form
      
      
    elif name == 'instances':
      size = -1
      if attrs.get('size',"") != "":
          size = int(attrs.get('size',""))
      self.netHandler.handlePopulation(self.currPopulation, self.currentCellType, size)
      
      
    elif name == 'instance':
      includeInstance = 0
	  
      if self.myNodeId == -1:
		self.log.debug("Ignoring nodeIds, handling all cells...")
		includeInstance = 1
		self.netHandler.setParallelStatus(0)
		
      elif attrs.get('node_id',"") != "":
        self.log.debug("Found node_id: "+ attrs.get('node_id',""))
        if self.netHandler.isParallel!=1: self.netHandler.setParallelStatus(1)
        if int(attrs.get('node_id',"")) == self.myNodeId:
            self.log.debug("Including...")
            includeInstance = 1
        else:
            self.log.debug("Excluding from host id:  %d..."%self.myNodeId)
            includeInstance = 0
			
      else:
        self.log.debug("Found no node_id...")
        includeInstance = 1
        
      if includeInstance == 1:
        self.currentInstanceId = attrs.get('id',"")
        self.log.debug("Found instance element: "+ self.currentInstanceId)
      
      
    elif name == 'cell_type':   		# pre NeuroML v1.7.1 form
      self.isCellTypeElement = 1
      
      
    elif name == 'location':
      if self.currentInstanceId != -1:
	 
        #self.log.debug('Found location: ('+ str(attrs.get('x',''))+', '+ str(attrs.get('y',''))+', '+ str(attrs.get('z','')), ')')

        self.totalInstances+=1
        nodeInfo = ""
        if self.myNodeId >= 0:
            nodeInfo = " on node id: %d"%self.myNodeId 
        
        self.log.debug('Adding instance: ' +self.currentInstanceId+" of group: "+self.currPopulation+nodeInfo)
        
        self.netHandler.handleLocation(self.currentInstanceId, self.currPopulation, self.currentCellType, attrs.get('x',""),\
                                    attrs.get('y',""), \
                                    attrs.get('z',""))
    


    elif name == 'projection':
      self.currentProjectionName = attrs.get('name',"")   
      self.log.debug("Found projection element: "+ self.currentProjectionName)   

      if attrs.has_key('source'):
          self.currentProjectionSource = attrs.get('source',"") 


      if attrs.has_key('target'):
          self.currentProjectionTarget = attrs.get('target',"") 
          self.log.info("Projection: "+ self.currentProjectionName+" is from: "+ self.currentProjectionSource +" to: "+ self.currentProjectionTarget)           
      
      
    elif name == 'source':
      if self.currentProjectionName != "":
          self.isSourceElement = 1
           
           
    elif name == 'target' and not attrs.has_key('cell_group') and not attrs.has_key('population'):
      if self.currentProjectionName != "":    
          self.isTargetElement = 1  


    elif name == 'connections':
      if attrs.get('size',"") != "":
          size = int(attrs.get('size',""))
          self.netHandler.handleProjection(self.currentProjectionName,
                                           self.currentProjectionSource,
                                           self.currentProjectionTarget,
                                           self.globalSynapseProps,
                                           size)
      else:
          self.netHandler.handleProjection(self.currentProjectionName,
                                           self.currentProjectionSource,
                                           self.currentProjectionTarget,
                                           self.globalSynapseProps)


          
    elif name == 'connection':
      if self.currentProjectionName != "":
        self.currentConnId = attrs.get('id',"")   
        self.log.debug("Found connection element: "+ self.currentConnId)  

        if attrs.has_key('pre_cell_id'):
            self.preCellId = attrs.get('pre_cell_id',"") 
        if attrs.has_key('pre_segment_id'):
            self.preSegId = attrs.get('pre_segment_id',"") 
        else:
            self.preSegId = 0
            
        if attrs.has_key('pre_fraction_along'):
            self.preFract = attrs.get('pre_fraction_along',"")  
        else:
            self.preFract = 0.5


        if attrs.has_key('post_cell_id'):
            self.postCellId = attrs.get('post_cell_id',"") 
        if attrs.has_key('post_segment_id'):
            self.postSegId = attrs.get('post_segment_id',"") 
        else:
            self.postSegId = 0
            
        if attrs.has_key('post_fraction_along'):
            self.postFract = attrs.get('post_fraction_along',"")  
        else:
            self.postFract = 0.5
        
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
        self.log.debug("Found post: "+ self.postCellId)          
           
	   
          
    elif name == 'synapse_props':
      if self.currentProjectionName != "":  
        newSynapseProps = SynapseProperties()
	
        if attrs.has_key('synapse_type'):
            self.latestSynapseType = str(attrs.get('synapse_type'))

            self.log.debug("synapse_type is: "+self.latestSynapseType)

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
                      
            self.log.info("Changed vals of local syn props: "+ synapse_type+": "+ str(synProps))      
	            
    elif name == 'input':
      self.currentInputName = attrs.get('name',"")   
      self.log.info("Found input element: "+ self.currentInputName)   
      
           
    elif name == 'random_stim':
        
      self.currentInputProps["synaptic_mechanism"] = attrs.get('synaptic_mechanism',"")
      self.currentInputProps["frequency"] = attrs.get('frequency',"") 
      
      self.log.info("Found input properties: "+ str(self.currentInputProps))   
      
      

    elif name == 'target':
      self.currentInputTarget = attrs.get('cell_group',"")
      if len(self.currentInputTarget) == 0:
        self.currentInputTarget = attrs.get('population',"")
        
      self.log.info("Input: "+ self.currentInputName+" is to population: "+self.currentInputTarget)   
      
           
    elif name == 'sites':
      size = -1
      if attrs.has_key('size'):
          size = int(attrs.get('size',""))     
      
      
      self.netHandler.handleInputSource(self.currentInputName, self.currentInputTarget, self.currentInputProps, size)
      
           
    elif name == 'site':
        cell_id = int(attrs.get('cell_id',""))
        segment_id = 0
        fract = 0.5
        if attrs.has_key('segment_id'):
            segment_id = int(attrs.get('segment_id',""))
                
        if attrs.has_key('fract'):
            fract = float(attrs.get('fract',""))
            
        self.netHandler.handleSingleInput(self.currentInputName, cell_id, segment_id, fract)

			       
    else:
      others = ["populations", "projections", "connections", "inputs",
                "meta:tag", "meta:value", "meta:property", "meta:properties"]
      if (others.count(name) == 0):
      	print 'Unhandled, unknown element: '+ name
      
    return
    
    
    
  def characters (self, ch):
   if self.isCellTypeElement== 1:    # pre NeuroML v1.7.1 form
     self.currentCellType = ch
     
   if self.isSourceElement== 1:
     self.currentProjectionSource = ch
     
   if self.isTargetElement== 1:
     self.currentProjectionTarget = ch
     self.log.debug("Projection: "+ self.currentProjectionName+" is from: "+ self.currentProjectionSource +" to: "+ self.currentProjectionTarget)        
     
   if self.isSynapseTypeElement== 1:
     self.latestSynapseType = ch
     
         
         
  def endElement(self, name):
      
    self.currentElementList.pop()
    
    if name == 'instance':
      self.currentInstanceId = -1
      
      
    elif name == 'cell_type':
      self.isCellTypeElement = 0    
      
      
    elif name == 'population':
      self.currPopulation = ""
      self.currentCellType = ""
      
      
    elif name == 'instances':
      self.log.debug("Dealt with %d location instances"% (self.totalInstances))
      
      
    elif name == 'projection':
      self.netHandler.finaliseProjection(self.currentProjectionName, self.currentProjectionSource, self.currentProjectionTarget)
      self.currentProjectionName = ""
      self.currentProjectionSource = ""
      self.currentProjectionTarget = ""
      self.globalSynapseProps.clear()
      
      
      
    elif name == 'source':
      self.isSourceElement = 0
      
      
    elif name == 'target':
      self.isTargetElement = 0
      
      
    elif name == 'connection':
	    
      self.log.debug(" Gathered all details of connection: " + self.currentConnId)
      
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
      
    elif name == 'input':        
        self.netHandler.finaliseInputSource(self.currentInputName) 
        currentInputName = ""
        currentInputProps = {}
        currentInputTarget = ""
        currentInputId = -1
           

        
        
        
        
        
        
        
        
        
        
    