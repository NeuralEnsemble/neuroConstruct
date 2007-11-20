#
#
#   A class to allow easy generation of NetworkML files from Python.
# 
#   Should move to a more PyNN like interface over time...
# 
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#



import logging


logformat = "%(name)-19s %(levelname)-5s- %(message)s"
logging.basicConfig(level=logging.INFO, format=logformat)
    
log = logging.getLogger("NetworkMLUtils")
    



class NetworkMLFile():
    
    currNeuroMLVersion = 1.7
    
    projUnits="Physiological Units"
    
    notes = "A NetworkML file generated from a simple Python interface"
    
    
    def __init__(self):
        
        log.info("NetworkMLFile object created")
        self.populations = []
        self.projections = []
        
        
    def addPopulation(self, populationName, cellType):
        
        newPop = Population(populationName, cellType)
        self.populations.append(newPop)
        return newPop
    
    
    def setProjectionUnits(self, projUnits):
        self.projUnits = projUnits
    
        
    def addProjection(self, projectionName, source, target):
        
        newProj = Projection(projectionName, source, target)
        self.projections.append(newProj)
        return newProj
    
        
    def writeToFile(self, filename):
        
        log.info("NetworkMLFile going to be written to: "+ str(filename))
        
        out_file = open(filename, "w")
        
        out_file.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        out_file.write("<networkml xmlns=\"http://morphml.org/networkml/schema\"\n")
        out_file.write("    xmlns:meta=\"http://morphml.org/metadata/schema\"\n")
        out_file.write("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
        out_file.write("    xsi:schemaLocation=\"http://morphml.org/networkml/schema  NeuroML.xsd\"\n")
        out_file.write("    lengthUnits=\"micron\">\n")


        out_file.write("\n")
        out_file.write("    <meta:notes>"+self.notes+"</meta:notes>\n\n")
        
        
        out_file.write("    <populations>\n")
        
        for population in self.populations:
            population.generateXML(out_file, "        ")
            
        out_file.write("    </populations>\n")
        
        
        out_file.write("    <projections units=\""+self.projUnits+"\">\n")
        
        for projection in self.projections:
            projection.generateXML(out_file, "        ")
            
        out_file.write("    </projections>\n")
        
        
        out_file.write("</networkml>\n")
        
        out_file.close()
        
        
        
class Population():
    
    def __init__(self, populationName, cellType):
        self.popName = populationName
        self.cellType = cellType
        self.instances = []
        
        
    def addInstance(self, x, y, z, node_id=-1):
        self.instances.append(Instance(len(self.instances), x, y, z, node_id))
        
        
    def generateXML(self, out_file, indent):
        
        out_file.write(indent+"<population name=\""+self.popName+"\">\n")
        out_file.write(indent+"    <cell_type>"+self.cellType+"</cell_type>\n")
        
        out_file.write(indent+"    <instances>\n")
        
        for instance in self.instances:
            instance.generateXML(out_file, indent+"        ")
                    
        out_file.write(indent+"    </instances>\n")
        
        out_file.write(indent+"</population>\n")
        
        
        
        
class Instance():
    
    def __init__(self, id, x, y, z, node_id=-1):
        self.id = id
        self.x = x
        self.y = y
        self.z = z
        self.node_id = node_id
        
        
    def generateXML(self, out_file, indent):
        
        nodeString = ""
        if self.node_id >= 0:
            nodeString = " node_id=\""+self.node_id+"\""
            
        out_file.write(indent+"<instance id=\""+str(self.id)+"\""+nodeString+">\n")
        out_file.write(indent+"    <location x=\""+str(self.x)+"\" y=\""+str(self.y)+"\" z=\""+str(self.z)+"\"/>\n")
        out_file.write(indent+"</instance>\n")
        
        

        
class Projection():
    
    def __init__(self, projectionName, source, target):
        self.projName = projectionName
        self.source = source
        self.target = target
        self.synapses = []
        self.connections = []
        
        
    def addSynapse(self, synapseType, weight, threshold, internalDelay=0, preDelay=0, postDelay=0, propDelay=0):
        self.synapses.append(SynapseProps(synapseType, weight, threshold, internalDelay, preDelay, postDelay, propDelay))
        
        
    def addConnection(self, preCellId, postCellId):
        self.connections.append(Connection(len(self.connections), preCellId, postCellId))
        
        
    def generateXML(self, out_file, indent):
        
        out_file.write(indent+"<projection name=\""+self.projName+"\">\n")
        out_file.write(indent+"    <source>"+self.source+"</source>\n")
        out_file.write(indent+"    <target>"+self.target+"</target>\n")
        
        for synapse in self.synapses:
            synapse.generateDefaultXML(out_file, indent+"    ")
            
        out_file.write(indent+"    <connections>\n")
        
        for connection in self.connections:
            connection.generateXML(out_file, indent+"    ")
            
        out_file.write(indent+"    </connections>\n")
            
        out_file.write(indent+"</projection>\n")
        
        
        
class SynapseProps():
    
    def __init__(self, synapseType, weight, threshold, internalDelay=0, preDelay=0, postDelay=0, propDelay=0):
        self.synapseType = synapseType
        self.weight = weight
        self.threshold = threshold
        self.internalDelay = internalDelay
        self.preDelay = preDelay
        self.postDelay = postDelay
        self.propDelay = propDelay
        
    def getValuesString(self):
        vals = ""
        if self.internalDelay!=0:
            vals = vals+" internal_delay=\""+str(self.internalDelay)+"\""
        if self.preDelay!=0:
            vals = vals+" pre_delay=\""+str(self.preDelay)+"\""
        if self.postDelay!=0:
            vals = vals+" post_delay=\""+str(self.postDelay)+"\""
        if self.propDelay!=0:
            vals = vals+" prop_delay=\""+str(self.propDelay)+"\""
    
        vals = vals+" weight=\""+str(self.weight)+"\""
        vals = vals+" threshold=\""+str(self.threshold)+"\""
        
        return vals
    
       
    def generateDefaultXML(self, out_file, indent): 
        
        out_file.write(indent+"<synapse_props>\n")
        out_file.write(indent+"    <synapse_type>"+self.synapseType+"</synapse_type>\n")
        out_file.write(indent+"    <default_values"+self.getValuesString()+"/>\n")
        out_file.write(indent+"</synapse_props>\n")
        
        
class Connection():
    
    def __init__(self, id, preCellId, postCellId):
        self.id = id
        self.preCellId = preCellId
        self.postCellId = postCellId
        
       
    def generateXML(self, out_file, indent): 
        
        out_file.write(indent+"<connection id=\""+str(self.id)+"\">\n")
        out_file.write(indent+"    <pre cell_id=\""+str(self.preCellId)+"\"/>\n")
        out_file.write(indent+"    <post cell_id=\""+str(self.postCellId)+"\"/>\n")
        out_file.write(indent+"</connection>\n")
        
                
                
                
                
                
                
                
                
        
        