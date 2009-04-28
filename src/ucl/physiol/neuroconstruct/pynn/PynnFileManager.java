/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.pynn;

import java.io.*;
import java.util.*;


import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLElement;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLEntity;


/**
 * Main file for generating the PyNN files
 *
 * @author Padraig Gleeson
 *  
 */

public class PynnFileManager
{
    private static ClassLogger logger = new ClassLogger("PynnFileManager");

    Project project = null;

    File mainFile = null;

    int randomSeed = 0;
    
    /*
     * Note: PyNN functionality is in testing stage and this will quickly turn off 
     * PyNN support in the GUI for releasable versions
     */
    public static boolean enablePynnFunc()
    {
        //return false;
        return true;
    }
    
    public enum PynnSimulator 
    {
        NEURON  ("NEURON 2", "neuron2"),
        NEST2   ("NEST 2", "nest2"),
        PCSIM   ("PCSIM", "pcsim"),
        BRIAN   ("Brian", "brian"),
        PYMOOSE ("PyMOOSE", "pymoose");
        
        
        public final String moduleName;
        public final String name;
        
        PynnSimulator(String name, String packageName)
        {
            this.name = name;
            this.moduleName = packageName;
        }
        
        @Override
        public String toString()
        {
            return name;
        }
    }

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    ArrayList<String> cellTemplatesGenAndIncl = new ArrayList<String>();
        
    ArrayList<String> includedChanMechNames = new ArrayList<String>();
    
    String theOneCellGroup = null;
    
    PynnSimulator simulator = null;


    private Hashtable<String, Integer> nextColour = new Hashtable<String, Integer>();

    //private static boolean addComments = true;



    ArrayList<String> graphsCreated = new ArrayList<String>();

    SimConfig simConfig = null;
    
        
    //private boolean quitAfterRun = false;
        
    static
    {
        logger.setThisClassVerbose(false);
    }


    private PynnFileManager()
    {
    }


    public PynnFileManager(Project project)
    {
        this.project = project;
    }
    
    
   


    public void reset()
    {
        cellTemplatesGenAndIncl = new ArrayList<String>();
        graphsCreated = new ArrayList<String>();
        nextColour = new Hashtable<String, Integer>(); // reset it...
        includedChanMechNames = new ArrayList<String>();

    }
    



    public void generateThePynnFiles(SimConfig simConfig,
                                     PynnSimulator simulator,
                                     int seed) throws PynnException, IOException
    {
        logger.logComment("Starting generation of the files...");

        long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;
        
        this.simulator = simulator;


        this.removeAllPreviousFiles();


        randomSeed = seed;

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed


        FileWriter fw = null;
        nextColour = new Hashtable<String, Integer>(); // reset it...
        
        if (project.generatedCellPositions.getNumberInAllCellGroups()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please generate a network containing at least one " +
                    "cell before generating the PyNN scripts", null, null);
            return;
        }
        
        

        try
        {

            File dirForPynnFiles = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());


            mainFile = new File(dirForPynnFiles, project.getProjectName() + ".py");

            logger.logComment("generating: "+ mainFile);
            

            File networkFile = new File(dirForPynnFiles, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML);

            File pyNmlUtils = ProjectStructure.getPythonNeuroMLUtilsDir(project.getProjectMainDirectory());

            File pynnUtils = ProjectStructure.getPynnUtilsDir(project.getProjectMainDirectory());

            File toDir1 = new File(dirForPynnFiles, pyNmlUtils.getName());

            GeneralUtils.copyDirIntoDir(pyNmlUtils, toDir1, true, true);

            File toDir2 = new File(dirForPynnFiles, pynnUtils.getName());
            GeneralUtils.copyDirIntoDir(pynnUtils, toDir2, true, true);
            
            try
            {
                
                ProjectManager.saveNetworkStructureXML(project, 
                                                       networkFile,
                                                       false,
                                                       false,
                                                       simConfig.getName(),
                                                       NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            }
            catch (NeuroMLException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving network in NeuroML XML file: "+networkFile , ex1, null);
                return;
            }
            
            
            fw = new FileWriter(mainFile);

            fw.write(getFileHeader());
            
            fw.write("my_simulator = '"+simulator.moduleName+"'\n\n");
            
            fw.write("print \'Generating PyNN script for simulator: \'+my_simulator\n\n");
            
            
            
            fw.write("try:\n");
            fw.write("    exec(\"from pyNN.%s import *\" % my_simulator)\n");
            fw.write("except ImportError:\n");
            fw.write("    print \'There was a problem importing the module: pyNN.%s\' % my_simulator\n");
            fw.write("    print \'Please make sure the PyNN implementation of %s is correctly installed\' % my_simulator\n");
            fw.write("    exit()\n\n");
            
            
            
            fw.write("import sys\n");
            fw.write("import xml\n");
            fw.write("import time\n");
            fw.write("import logging\n\n");
            fw.write("startTime = time.time()\n\n");
            
            //fw.write("from pyNN.random import NumpyRNG\n");
            //fw.write("rng = NumpyRNG(seed="+randomSeed+")\n\n");
            fw.write("numpy.random.seed("+randomSeed+") # Is this the only place the seed needs to be set??\n\n");
            
            
            fw.write("sys.path.append(\"NeuroMLUtils\")\n");
            fw.write("sys.path.append(\"PyNNUtils\")\n\n");
            
            fw.write("from NetworkHandler import NetworkHandler\n");
            fw.write("from NetworkMLSaxHandler import NetworkMLSaxHandler\n");
            fw.write("from PyNNUtils import NetManagerPyNN\n\n");
            
            ArrayList<String> cellTypeDefsToInclude = new ArrayList<String>();
            
            ArrayList<String> cellGroups = project.generatedCellPositions.getNonEmptyCellGroups();
            for(String cg: cellGroups)
            {
                String cellType = project.cellGroupsInfo.getCellType(cg);
                
                if (!cellTypeDefsToInclude.contains(cellType)) 
                {
                    cellTypeDefsToInclude.add(cellType);
                    
                    generateFileForCell(project.cellManager.getCell(cellType), dirForPynnFiles);
                }
            }
            for (String cellType: cellTypeDefsToInclude)
            {
                fw.write("from "+cellType+" import *\n\n");
            }
            
            fw.write("tstop = "+simConfig.getSimDuration()+"\n\n");
            fw.write("dt = "+project.simulationParameters.getDt()+"\n\n");
            
            fw.write("setup(timestep=dt)\n\n");
            
            fw.write("netFileName = '"+networkFile.getName()+"'\n");

            fw.write("logging.basicConfig(level=logging.INFO, format=\"%(name)-19s %(levelname)-5s - %(message)s\")\n");


            fw.write("print(\"Going to read contents of a NetworkML file: \"+netFileName)\n");


            fw.write("parser = xml.sax.make_parser()   # A parser for any XML file\n");

            fw.write("pynnNetMgr = NetManagerPyNN(my_simulator)	# Stores (most of) the network structure\n");
            
            fw.write("pynnNetMgr.setMaxSimLength(tstop)  # Needed for generating input spike time array...\n");

            fw.write("curHandler = NetworkMLSaxHandler(pynnNetMgr) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetManagerPyNN\n");

            //fw.write("curHandler.setNodeId(-1) 	# Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0\n");

            fw.write("parser.setContentHandler(curHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed\n");

            fw.write("parser.parse(open(netFileName)) # The parser opens the file and ultimately the appropriate functions in NetworkHandler get called\n");


            fw.write("print(\"Have read in contents of file: \"+netFileName)\n\n");
            fw.write("print(\"Have created: %s populations, %s projections and %s input sources\" % " +
                    "(len(pynnNetMgr.populations.keys())," +
                    "len(pynnNetMgr.projections.keys())," +
                    "len(pynnNetMgr.inputSources.keys())))\n\n");
            
            //ArrayList<String> plotSaves = simConfig.getPlots();
            ArrayList<PlotSaveDetails> plotSaves = project.generatedPlotSaves.getAllPlotSaves();
            
            for (PlotSaveDetails psd: plotSaves)
            {
                if(psd.simPlot.toBeSaved())  // No plotting just yet...
                {
                    fw.write("print(\"Going to save details of: %s cells in population: %s\" % ("+psd.cellNumsToPlot.size()
                            +", \""+psd.simPlot.getCellGroup()+"\"))\n");
                    
                    if (psd.allCellsInGroup)
                    {
                        if (psd.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                        {
                            fw.write("pynnNetMgr.populations[\""+psd.simPlot.getCellGroup()+"\"].record_v()\n\n");
                        }
                        else
                        {
                            fw.write("print(\"Recording anything besides voltage not implemented yet!\"\n");
                        }
                        
                    }
                    else
                    {
                        fw.write("print(\"Recording of individual cells not implemented yet!\"\n");
                    }
                }
            }
            
            
            

            fw.write("preRunTime = time.time()\n\n");
            fw.write("print(\"Running simulation for %sms\"%tstop)\n");
            fw.write("run(tstop)\n");
            fw.write("postRunTime = time.time()\n\n");
            fw.write("print(\"Finished simulation. Setup time: %f secs, run time: %f secs\" % (preRunTime-startTime, postRunTime-preRunTime))\n\n");
            
            for (PlotSaveDetails psd: plotSaves)
            {
                if(psd.simPlot.toBeSaved())  // No plotting just yet...
                {                    
                    if (psd.allCellsInGroup)
                    {
                        if (psd.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                        {
                            fw.write("pynnNetMgr.populations[\""+psd.simPlot.getCellGroup()+"\"].print_v(\""+psd.simPlot.getCellGroup()+".dat\")\n");
                        }
                        
                    }
                }
            }
            
            fw.write("\ntimeFile = open(\"time.dat\", mode=\"w\")\n");
            //fw.write("for i in range(0,int(tstop/dt)+1):\n");
            fw.write("for i in range(1,int(tstop/dt)):\n");
            fw.write("    timeFile.write(str(i*dt)+\"\\n\")\n");
            fw.write("timeFile.close()\n\n");
            
            fw.write("postSaveTime = time.time()\n\n");
            
            fw.write("simFile = open(\"simulator.props\", mode=\"w\")\n");
            fw.write("simFile.write(\"#This file contains properties generated during the PyNN script execution\\n\")\n");
            fw.write("simFile.write(\"RealSimulationTime=%f\\n\"% (postRunTime-preRunTime))\n");
            fw.write("simFile.write(\"SimulationSetupTime=%f\\n\"% (preRunTime-startTime))\n");
            fw.write("simFile.write(\"SimulationSaveTime=%f\\n\"% (postSaveTime-postRunTime))\n");
            fw.write("simFile.write(\"TotalTime=%f\\n\"% (postSaveTime-startTime))\n");
            fw.write("simFile.close()\n\n");
            
            
            fw.flush();
            fw.close();


        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (Exception ex1)
            {
                throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex1);
            }
            throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex);

        }

        this.mainFileGenerated = true;

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("... Created Main Pynn file: " + mainFile
                +" in "+genTime+" seconds. ");
        
        
        

    }

    
    


    public int getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    public float getCurrentGenTime()
    {
        return this.genTime;
    }



    private void removeAllPreviousFiles()
    {
        cellTemplatesGenAndIncl.clear();

        File codeDir = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(codeDir, false, true, true);


    }


    public static String getFileHeader()
    {
        StringBuffer response = new StringBuffer();
        response.append("'''\n");
        response.append("******************************************************\n");
        response.append("\n");
        response.append("     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append(" \n");
        response.append("******************************************************\n");

        response.append("'''\n\n");
        return response.toString();
    }



    public void generateFileForCell(Cell cell, File dir) throws PynnException
    {
        File f = new File(dir, cell.getInstanceName()+".py");
        
        FileWriter fw = null;
        
        try
        {
            fw = new FileWriter(f);
               
            fw.write(getFileHeader());
            
            
            fw.write("my_simulator = '"+simulator.moduleName+"'\n\n");
            
            
            fw.write("try:\n");
            fw.write("    exec(\"from pyNN.%s import *\" % my_simulator)\n");
            fw.write("except ImportError:\n");
            fw.write("    print \'There was a problem importing the module: pyNN.%s\' % my_simulator\n");
            fw.write("    print \'Please make sure the PyNN implementation of %s is correctly installed\' % my_simulator\n");
            fw.write("    exit()\n\n");
            
            if (cell.getAllSegments().size()>1)
            {
                throw new PynnException("Error, PyNN does not support multi compartmental cells yet!");
                
            }
            
            Hashtable<String, Float> cellParams = new Hashtable<String, Float>();
            
            cellParams.put("v_init", cell.getInitialPotential().getNominalNumber());
            
            float cellArea = cell.getAllSegments().get(0).getSegmentSurfaceArea();
            float specCap = cell.getSpecCapForGroup(Section.ALL);
            
            ////////// TODO: use correct units, etc.
            
            cellParams.put("cm", cellArea*specCap*1e5f); // ????????????
            
            
            Enumeration<ChannelMechanism> cms = cell.getChanMechsVsGroups().keys();
            while(cms.hasMoreElements())
            {
                ChannelMechanism cm = cms.nextElement();
                try 
                {
                    ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)project.cellMechanismInfo.getCellMechanism(cm.getName());
                
                    cmlMech.initialise(project, false);
                    
                    if (cmlMech.isChannelMechanism()&& !cmlMech.isPassiveNonSpecificCond())
                    {
                            throw new PynnException("Error, only passive channels and Integrate & Fire mechanisms are allowed in PyNN at the moment!\n" +
                                    "Channels: "+ cell.getChanMechsVsGroups());
                    }
              

                    Vector<String> groups = cell.getChanMechsVsGroups().get(cm);
                    for(String group: groups)
                    {
                        if (!(group.equals(Section.ALL) || group.equals(Section.SOMA_GROUP)))
                        {
                            throw new PynnException("Error, only channels on soma group or all allowed!\n" +
                                    "Channels: "+ cell.getChanMechsVsGroups());
                        }
                        if (cmlMech.isPassiveNonSpecificCond())
                        {
                            try
                            {
                                float condDens = Float.parseFloat(cmlMech.getValue(ChannelMLConstants.getPostV1_7_3CondDensXPath()));
                            

                                float totRes = (1.0f/condDens) * cellArea;
                                float membTimeConst = (totRes * cellArea * specCap);

                                cellParams.put("tau_m", membTimeConst*1000);


                                float revPot = Float.parseFloat(cmlMech.getValue(ChannelMLConstants.getIonRevPotXPath()));

                                // TODO: check units!!
                                cellParams.put("v_rest", revPot);
                            }
                            catch (NumberFormatException ex) 
                            {
                                throw new PynnException("Error initialising channel mechanism: "+cm+". Please ensure this is a valid ChannelML mechanim, and that it is in the post v1.7.3 format (i.e. no <ohmic> sub element in <current_voltage_relation>)", ex);

                            }
                            catch (NullPointerException ex) 
                            {
                                throw new PynnException("Error initialising channel mechanism: "+cm+". Please ensure this is a valid ChannelML mechanim, and that it is in the post v1.7.3 format (i.e. no <ohmic> sub element in <current_voltage_relation>)", ex);

                            }
                        }
                        if (cmlMech.isPointProcess())
                        {
                            try
                            {
                                SimpleXMLEntity[] ents = cmlMech.getXMLDoc().getXMLEntities(ChannelMLConstants.getIandFXPath());
                                if (ents!=null && ents.length==1)
                                {
                                    SimpleXMLElement ifElement = (SimpleXMLElement) ents[0];
                                    float thresh = Float.parseFloat(ifElement.getAttributeValue(ChannelMLConstants.I_AND_F_THRESHOLD));
                                    float tRefrac = Float.parseFloat(ifElement.getAttributeValue(ChannelMLConstants.I_AND_F_T_REFRAC));
                                    float vReset = Float.parseFloat(ifElement.getAttributeValue(ChannelMLConstants.I_AND_F_V_RESET));

                                    // TODO: check units!!
                                    cellParams.put("v_thresh", thresh);
                                    cellParams.put("tau_refrac", tRefrac);
                                    cellParams.put("v_reset", vReset);
                                    
                                    
                                }
                            }
                            catch (NullPointerException ex) 
                            {
                                throw new PynnException("Error initialising channel mechanism: "+cm+". Please ensure this is a valid ChannelML mechanim, and that it is in the post v1.7.3 format (i.e. no <ohmic> sub element in <current_voltage_relation>)", ex);

                            }
                        }
                    }
                } 
                catch (ChannelMLException ex) 
                {
                    throw new PynnException("Error initialising channel mechanism: "+cm+". Please ensure this is a valid ChannelML mechanim", ex);
                    
                }
                catch (ClassCastException ex) 
                {
                    throw new PynnException("Error initialising channel mechanism: "+cm+". Please ensure this is a valid ChannelML mechanims\n" +
                        "Note: File Based Cell Mechanisms are not supported for PyNN", ex);
                    
                }
            }
            
            
            
            fw.write("class "+cell.getInstanceName()+"(IF_cond_exp):\n\n");
            fw.write("    def __init__ (self, parameters): \n");
            Enumeration<String> names = cellParams.keys();
            
            fw.write("        if parameters == None:\n");
            fw.write("            parameters = {}\n\n");
            while(names.hasMoreElements())
            {
                String name = names.nextElement();
                float val = cellParams.get(name);
                
                fw.write("        if not parameters.has_key('"+name+"'): \n");
                fw.write("            parameters['"+name+"'] = "+val+" \n\n");
                
            }
                
            fw.write("        IF_cond_exp.__init__ (self, parameters)\n");
            fw.write("        print \"Created new "+cell.getInstanceName()+"...\"\n");
            
            
            
            fw.close();
            
        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (Exception ex1)
            {
                throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex1);
            }
            throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex);

        }
        
    }


    




    private File getDirectoryForSimulationFiles()
    {
        File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

        File dirForDataFiles = new File(dirForSims,
                                        project.simulationParameters.getReference());


        if (!dirForDataFiles.isDirectory() || !dirForDataFiles.exists())
        {
            dirForDataFiles.mkdir();
        }
        return dirForDataFiles;
    }



    private float getSimDuration()
    {
        if (simConfig.getSimDuration()==0) // shouldn't be...
        {
            return project.simulationParameters.getDuration();
        }
        else
            return simConfig.getSimDuration();
    }






    public void runFile(boolean copyToSimDataDir) throws PynnException
    {
        logger.logComment("Trying to run the mainFile...");

        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PynnException("PyNN files not yet generated");
        }


        File dirForSimDataFiles = getDirectoryForSimulationFiles();
        File dirToRunFrom = null;
        File genDir = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());
        
        if (copyToSimDataDir)
        {
            dirToRunFrom = dirForSimDataFiles;
            
            try
            {
                GeneralUtils.copyDirIntoDir(genDir, dirForSimDataFiles, true, true);
            }
            catch (Exception e)
            {
                throw new PynnException("Problem copying the Pynn files from "+genDir+" to "+ dirForSimDataFiles, e);
                
            }
        }
        else
        {
            dirToRunFrom = genDir; 
        }
        

        File positionsFile = new File(dirForSimDataFiles, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(dirForSimDataFiles, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(dirForSimDataFiles, SimulationData.ELEC_INPUT_DATA_FILE);
        

        
        if (dirToRunFrom.getAbsolutePath().indexOf(" ")>=0)
        {
            throw new PynnException("PyNN files cannot be run in a directory like: "+ dirToRunFrom
                    + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run PyNN.\n" +
                    "Arguments need to be given to this executable and spaces in filenames cause problems.\n\n"
                    +"Try saving the project (File -> Copy Project (Save As)...) in a directory without spaces.");
        }

        try
        {
            project.generatedCellPositions.saveToFile(positionsFile);
            project.generatedNetworkConnections.saveToFile(netConnsFile);
            project.generatedElecInputs.saveToFile(elecInputFile);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem saving generated positions in file: " + positionsFile.getAbsolutePath(),
                                      ex, null);
            return;
        }
        
        // Saving summary of the simulation params
        try
        {
            SimulationsInfo.recordSimulationSummary(project, simConfig, dirForSimDataFiles, "PyNN_"+simulator.moduleName, null);
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "+ dirForSimDataFiles +
                                      "\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
        }


        
        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;

        try
        {
            //String executable = null;
            
            String pyEx = "python -i";
            if (simulator.equals(PynnSimulator.NEURON))
            {
                pyEx = "nrniv -python";
            }
            
            File fullFileToRun = new File(dirToRunFrom, mainFile.getName());
            
            
            
            String title = "PyNN_"+simulator.moduleName+"__" + project.simulationParameters.getReference();
            

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                String setDir = "";
                //String setDir = " -Duser.dir="+dirToRunFrom.getAbsolutePath();
                
                commandToExecute = "cmd /K start \""+title+"\"  " +  pyEx +setDir+" "+fullFileToRun;

                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have executed command: " + commandToExecute/*+" in woriking dir: "+ dirToRunFrom*/, true);

            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                

                //File dirToRunIn = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());

                String basicCommLine = GeneralProperties.getExecutableCommandLine();

                String executable = "";
                String extraArgs = "";
                String titleOption = "";
                String workdirOption = "";
                String noClose = " --noclose";

                if (basicCommLine.indexOf("konsole")>=0)
                {
                    logger.logComment("Assume we're using KDE");
                    titleOption = " --title="+title;
                    workdirOption = " --workdir="+ dirToRunFrom.getAbsolutePath();
                    extraArgs = "-e /bin/bash ";
                    executable = basicCommLine.trim();
                }
                else if (basicCommLine.indexOf("gnome")>=0)
                {
                    logger.logComment("Assume we're using Gnome");
                    titleOption = " --title="+title;
                    workdirOption = " --working-directory="+ dirToRunFrom.getAbsolutePath();

                    if (basicCommLine.trim().indexOf(" ")>0) // case where basicCommLine is gnome-terminal -x
                    {
                        extraArgs = basicCommLine.substring(basicCommLine.trim().indexOf(" ")).trim();

                        executable = basicCommLine.substring(0, basicCommLine.trim().indexOf(" ")).trim();
                    }
                    else
                    {
                        executable = basicCommLine;
                        extraArgs = "-x";
                    }

                }
                else
                {
                    logger.logComment("Unknown console command, going with the flow...");
                    executable = basicCommLine.trim();
                }

                String scriptText = pyEx+ " "+fullFileToRun;

                File scriptFile = new File(ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory()),
                                           "runsim.sh");
                
                FileWriter fw = new FileWriter(scriptFile);

                fw.write(scriptText);
                fw.close();

                // bit of a hack...
                rt.exec("chmod u+x " + scriptFile.getAbsolutePath());
                try
                {
                    // This is to make sure the file permission is updated..
                    Thread.sleep(600);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }

                commandToExecute = executable
                    + " "
                    + noClose
                    + " "
                    + titleOption
                    + " "
                    + workdirOption
                    + " "
                    + extraArgs
                    + " " +
                    scriptFile.getAbsolutePath();



                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have successfully executed command: " + commandToExecute);
                
               
            }
            
            
        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new PynnException("Error executing the PyNN file: " + mainFile, ex);
        }
    }



    public String getMainPyNNFileName() throws PynnException
    {
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PynnException("PyNN file not yet generated");
        }

        return this.mainFile.getAbsolutePath();

    }
    
    
    



    public static void main(String[] args)
    {

        try
        {

            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File("models/PyNNTest/PyNNTest.neuro.xml"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);
            
            while(pm.isGenerating())
            {
                Thread.sleep(200);
            }
            System.out.println("Num cells generated: "+ p.generatedCellPositions.getAllPositionRecords().size());
            
            PynnFileManager gen = new PynnFileManager(p);


            gen.generateThePynnFiles(p.simConfigInfo.getDefaultSimConfig(), PynnFileManager.PynnSimulator.NEST2, 12345);
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
