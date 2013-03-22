#
#
#   A class which stores the structure of a network in a NetworkML friendly way
# 
#   Should move this to a more PyNN friendly interface over time...
# 
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council
#
#

    
genForVer2compat = 1  # Incorporates changes in v1.7.1 to clean up for v2.0


class NetworkMLNet:
    
    currNeuroMLVersion = "1.7.1"
    
    projUnits="Physiological Units"
    
    notes = "A NetworkML file generated from a simple Python interface"
    
    
    def __init__(self):
        
        print("NetworkMLNet object created")
        self.populations = []
        self.projections = []
        
        
    def addPopulation(self, populationName, cellType):
        
        newPop = Population(populationName, cellType)
        self.populations.append(newPop)
        return newPop
        
        
    def getPopulation(self, populationName):
        for population in self.populations:
            if population.popName == populationName:
                return population
        return None
    
    
    def setProjectionUnits(self, projUnits):
        self.projUnits = projUnits
    
        
    def addProjection(self, projectionName, source, target, projSize=-1):
        
        newProj = Projection(projectionName, source, target, projSize)
        self.projections.append(newProj)
        return newProj
        
                
    def getProjection(self, projectionName):
        for projection in self.projections:
            if projection.projName == projectionName:
                return projection
        return None
        
        
    
    def __str__(self):
        info =  "NetworkMLNet with "+str(len(self.populations))+" populations and "+str(len(self.projections))+" projections:"
        
        for population in self.populations:
          info = info + "\n    Population: "+ population.popName +" with "+ str(len(population.instances)) +" cells"
        
        for projection in self.projections:
          info = info + "\n    Projection: "+ projection.projName +" with "+ str(len(projection.connections)) +" connections"
          
        return info
    
        
    def writeXML(self, filename):
        
        print("NetworkMLNet going to be written to XML file: "+ str(filename))
        
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
        
        
        
    def writeHDF5(self, filename):
        
        print("NetworkMLNet going to be written to HDF5 file: "+ str(filename))
        
        import tables
        
        h5file = tables.openFile(filename, mode = "w", title = "Test file")
        
        rootGroup = h5file.createGroup("/", 'networkml', 'Root NetworkML group')
        
        rootGroup._f_setAttr("notes", self.notes)
        
        popsGroup = h5file.createGroup(rootGroup, 'populations', 'Populations group')
        
        for population in self.populations:
            population.generateHDF5(h5file, popsGroup)
        
        
        projsGroup = h5file.createGroup(rootGroup, 'projections', 'Projections group')
        projsGroup._f_setAttr("units", self.projUnits)
        
        for projection in self.projections:
            projection.generateHDF5(h5file, projsGroup)
            
        
        h5file.close()  # Close (and flush) the file
        
        
        
class Population:
    
    def __init__(self, populationName, cellType):
        self.popName = populationName
        self.cellType = cellType
        self.instances = []
        
        
    def addInstance(self, x, y, z, node_id=-1):
        self.instances.append(Instance(len(self.instances), x, y, z, node_id))
        
        
    def generateXML(self, out_file, indent):
        
        if genForVer2compat:
            out_file.write(indent+"<population name=\""+self.popName+"\" cell_type=\""+self.cellType+"\">\n")
        else:
            out_file.write(indent+"<population name=\""+self.popName+"\">\n")
            out_file.write(indent+"    <cell_type>"+self.cellType+"</cell_type>\n")
        
        
        out_file.write(indent+"    <instances size=\""+str(len(self.instances))+"\">\n")
        
        for instance in self.instances:
            instance.generateXML(out_file, indent+"        ")
                    
        out_file.write(indent+"    </instances>\n")
        
        out_file.write(indent+"</population>\n")
        
        
    def generateHDF5(self, h5file, popsGroup):
        
        import numpy
        
        popGroup = h5file.createGroup(popsGroup, 'population_'+self.popName)
        popGroup._f_setAttr("name", self.popName)
        popGroup._f_setAttr("cell_type", self.cellType)
        
        includeNodeIds = 0 
        
        for instance in self.instances:
          if instance.node_id >= 0:
            includeNodeIds = 1
            
        colCount = 4
        if includeNodeIds == 1:
          colCount = 5
        
        a = numpy.ones([len(self.instances), colCount], numpy.float32)
        
        count=0
        for instance in self.instances:
          a[count,0] = instance.id
          a[count,1] = instance.x
          a[count,2] = instance.y
          a[count,3] = instance.z
          if includeNodeIds == 1:
            a[count,4] = instance.node_id
          
          count=count+1
        
        h5file.createArray(popGroup, self.popName, a, "Locations of cells in "+ self.popName)
        
        
class Instance:
    
    def __init__(self, id, x, y, z, node_id=-1):
        self.id = id
        self.x = x
        self.y = y
        self.z = z
        self.node_id = node_id
        
        
    def generateXML(self, out_file, indent):
        
        nodeString = ""
        if self.node_id >= 0:
            nodeString = " node_id=\""+str(self.node_id)+"\""
            
        out_file.write(indent+"<instance id=\""+str(self.id)+"\""+nodeString+">\n")
        out_file.write(indent+"    <location x=\""+str(self.x)+"\" y=\""+str(self.y)+"\" z=\""+str(self.z)+"\"/>\n")
        out_file.write(indent+"</instance>\n")
        
        

        
class Projection:

    def __init__(self, projectionName, source, target, size=-1):
        self.projName = projectionName
        self.source = source
        self.target = target
        self.synapses = []

        if size>0:
            self.connections = [None]*size
            print "Pre allocating.."
        else:
            self.connections = []
        
        
    def addSynapse(self, synapseType, weight, threshold, internalDelay=0, preDelay=0, postDelay=0, propDelay=0):
        self.synapses.append(SynapseProps(synapseType, weight, threshold, internalDelay, preDelay, postDelay, propDelay))
        
        
    def addConnection(self, preCellId, postCellId):
        self.connections.append(Connection(len(self.connections), preCellId, postCellId))
        
        
    def generateXML(self, out_file, indent):
        
        
        if genForVer2compat:
            out_file.write(indent+"<projection name=\""+self.projName+"\" source=\""+self.source+"\" target=\""+self.target+"\">\n")
        else:
            out_file.write(indent+"<projection name=\""+self.projName+"\">\n")
            out_file.write(indent+"    <source>"+self.source+"</source>\n")
            out_file.write(indent+"    <target>"+self.target+"</target>\n")
        
        for synapse in self.synapses:
            synapse.generateDefaultXML(out_file, indent+"    ")
            
        out_file.write(indent+"    <connections>\n")
        
        for connection in self.connections:
            connection.generateXML(out_file, indent+"        ")
            
        out_file.write(indent+"    </connections>\n")
            
        out_file.write(indent+"</projection>\n")
        
        
    def generateHDF5(self, h5file, projsGroup):
        
        import numpy
        
        projGroup = h5file.createGroup(projsGroup, 'projection_'+self.projName)
        projGroup._f_setAttr("name", self.projName)
        projGroup._f_setAttr("source", self.source)
        projGroup._f_setAttr("target", self.target)
        
        
        for synapse in self.synapses:
            synapse.generateDefaultHDF5(h5file, projGroup)
        
        colCount = 3
        
        a = numpy.ones([len(self.connections), colCount], numpy.float32)
        
        count=0
        for connection in self.connections:
          a[count,0] = connection.id
          a[count,1] = connection.preCellId
          a[count,2] = connection.postCellId          
          count=count+1
        
        array = h5file.createArray(projGroup, self.projName, a, "Connections of cells in "+ self.projName)
        array._f_setAttr("column_0", "id")
        array._f_setAttr("column_1", "pre_cell_id")
        array._f_setAttr("column_2", "post_cell_id")
        
        
class SynapseProps:
    
    def __init__(self, synapseType, weight, threshold, internalDelay=0, preDelay=0, postDelay=0, propDelay=0):
        self.synapseType = synapseType
        self.weight = weight
        self.threshold = threshold
        self.internalDelay = internalDelay
        self.preDelay = preDelay
        self.postDelay = postDelay
        self.propDelay = propDelay

    def __str__(self):
        return "SynapseProps["+synapseType+", "+getValuesString(self)+"]"
        
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
        
        if genForVer2compat:
            out_file.write(indent+"<synapse_props synapse_type=\""+self.synapseType+"\""+self.getValuesString()+"/>\n")
        else:
            out_file.write(indent+"<synapse_props>\n")
            out_file.write(indent+"    <synapse_type>"+self.synapseType+"</synapse_type>\n")
            out_file.write(indent+"    <default_values"+self.getValuesString()+"/>\n")
            out_file.write(indent+"</synapse_props>\n")
            
    def generateDefaultHDF5(self, h5file, projGroup):
        
        synPropsGroup = h5file.createGroup(projGroup, 'synapse_props_'+self.synapseType)
        synPropsGroup._f_setAttr("synapse_type", self.synapseType)
        synPropsGroup._f_setAttr("internal_delay", str(self.internalDelay))
        synPropsGroup._f_setAttr("weight", str(self.weight))
        synPropsGroup._f_setAttr("threshold", str(self.threshold))
        
        if self.preDelay!=0:
          synPropsGroup._f_setAttr("pre_delay", self.preDelay)
        if self.postDelay!=0:
          synPropsGroup._f_setAttr("post_delay", self.postDelay)
        if self.propDelay!=0:
          synPropsGroup._f_setAttr("prop_delay", self.propDelay)
        
        
        
class Connection:
  
    # NOTE: no pre_segment_id etc. yet!!!
    
    def __init__(self, id, preCellId, postCellId):
        self.id = id
        self.preCellId = preCellId
        self.postCellId = postCellId
        
       
    def generateXML(self, out_file, indent): 
        
        if genForVer2compat:
            out_file.write(indent+"<connection id=\""+str(self.id)+"\" pre_cell_id=\""+str(self.preCellId)+"\" post_cell_id=\""+str(self.postCellId)+"\"/>\n")
        else:
            out_file.write(indent+"<connection id=\""+str(self.id)+"\">\n")
            out_file.write(indent+"    <pre cell_id=\""+str(self.preCellId)+"\"/>\n")
            out_file.write(indent+"    <post cell_id=\""+str(self.postCellId)+"\"/>\n")
            out_file.write(indent+"</connection>\n")
        
                
                
                
                
                
                
                
                
        
        