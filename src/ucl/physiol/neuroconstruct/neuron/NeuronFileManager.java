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
 *  GNU General Public License for more details.getd

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.neuron;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.python.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.hpc.mpi.*;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings.KnownSimulators;
import ucl.physiol.neuroconstruct.hpc.utils.ProcessFeedback;
import ucl.physiol.neuroconstruct.hpc.utils.ProcessManager;
import ucl.physiol.neuroconstruct.neuroml.hdf5.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;

import org.lemsml.sim.*;
import org.neuroml.exporters.*;
import org.neuroml.Utils;
import org.lemsml.type.Component;

/**
 * Main file for generating the script files for NEURON
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuronFileManager
{
    private static ClassLogger logger = new ClassLogger("NeuronFileManager");

    /**
     * Various options for running the generated code: Generate hoc
     */
    public static final int RUN_HOC = 0;
    
    /**
     * Various options for running the generated code: Generate condor code (semi deprecated)
     */
    public static final int RUN_VIA_CONDOR = 1;
    
    /**
     * Various options for running the generated code: Generate hoc/Python
     */
    public static final int RUN_PYTHON_XML = 3;
    
    /**
     * Various options for running the generated code: Generate hoc/Python
     */
    public static final int RUN_PYTHON_HDF5 = 4;

    /**
     * The random seed placed into the generated NEURON codeinitiali
     */
    private long randomSeed = 0;

    /**
     * The runMode used in the generated NEURON code
     */
    private int genRunMode = -1;
    
    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    
    private Project project = null;

    private File mainHocFile = null;
    
    private File mainPythonFile = null;
    
    private File runPythonFile = null;

    private boolean hocFileGenerated = false;

    /**
     * A list of the *.mod files which will be needed by the cells in the simulation
     */
    private Vector<String> cellMechFilesGenAndIncl = new Vector<String>();

    /**
     * A list of the *.mod files which will be needed by the cells in the simulation
     */
    private Vector<String> stimModFilesRequired = new Vector<String>();

    /**
     * A list of the NEURON template files which will be needed by the cells in the simulation
     */
    private Vector<String> cellTemplatesGenAndIncluded = new Vector<String>();
    
    /**
     * To manage multiple runs, as specified through the GUI. Will prob be removed in favour of easier 
     * Python script based project opening/generation/simulation running.
     */
    private MultiRunManager multiRunManager = null;
    
    
    private Hashtable<String, Integer> nextColour = new Hashtable<String, Integer>();

    private Vector<String> graphsCreated = new Vector<String>();

    private static boolean addComments = true;

    private SimConfig simConfig = null;


    File utilsFile = ProjectStructure.getNeuronUtilsFile();
    
    File cellCheckFile = ProjectStructure.getNeuronCellCheckFile();
    

    public static final String EXT_CURR_CLAMP_MOD = "CurrentClampExt.mod";
    public static final String CURR_CLAMP_VAR_MOD = "CurrentClampVariable.mod";
    public static final String RAND_STIM_VAR_MOD = "NetStimVariable.mod";

    public static final String CURR_CLAMP_VAR_AMP_EXPR = "%AMPLITUDE_EXPRESSION%";
    public static final String RAND_STIM_VAR_RATE_EXPR = "%RATE_EXPRESSION%";
    
    public static final String FORCE_REGENERATE_MODS_FILENAME = "regenerateMods";
    
    /*
     * Will recompile mods at least once  
     */
    private boolean firstRecompileComplete = false;
    
    
    private boolean quitAfterRun = false;
            
    private NeuronFileManager()
    {

    }

    public NeuronFileManager(Project project)
    {
        this.project = project;
        addComments = project.neuronSettings.isGenerateComments();

        //logger.setThisClassVerbose(true);

    }

    public static boolean addComments()
    {
        return addComments;
    }

    public void reset()
    {
        cellTemplatesGenAndIncluded = new Vector<String>();
        cellMechFilesGenAndIncl = new Vector<String>();
        stimModFilesRequired =  new Vector<String>();
        
        graphsCreated = new Vector<String>();
        
        nextColour = new Hashtable<String, Integer>(); // reset it...
        
        addComments = project.neuronSettings.isGenerateComments();
        
        genRunMode = -1;
        genTime = -1;

    }
    

    public void generateTheNeuronFiles(SimConfig simConfig,
                                       MultiRunManager multiRunManager,
                                       int runMode,
                                       long randomSeed) throws NeuronException, IOException
    {
        logger.logComment("****  Starting generation of the hoc files...  ****");
        
        reset();

        long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;
        
        this.genRunMode = runMode;

        this.multiRunManager = multiRunManager;

        this.removeAllPreviousFiles();

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed
        ProjectManager.reinitialiseRandomGenerator();

        this.randomSeed = randomSeed;

        FileWriter hocWriter = null;
        FileWriter pythonWriter = null;
        FileWriter pythonRunWriter = null;
 
        try
        {
            File dirForNeuronFiles = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());

            if (isRunModePythonBased(runMode) || project.neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC))
            {
                runPythonFile = new File(dirForNeuronFiles, makePythonFriendly("run_"+project.getProjectName() + ".py"));
                pythonRunWriter = new FileWriter(runPythonFile);
                
                pythonRunWriter.write(PythonUtils.getFileHeader());
                pythonRunWriter.write(generatePythonRunFile());
            }

            if (isRunModePythonBased(runMode))
            {
                File pyNmlUtils = ProjectStructure.getPythonNeuroMLUtilsDir(project.getProjectMainDirectory());
                //System.out.println("pyNmlUtils: "+pyNmlUtils.getAbsolutePath());
                //System.out.println("pyNmlUtils: "+pyNmlUtils.getCanonicalPath());

                File pyNeuUtils = ProjectStructure.getPythonNeuronUtilsDir(project.getProjectMainDirectory());
                
                File toDir1 = new File(dirForNeuronFiles, pyNmlUtils.getName());

                GeneralUtils.copyDirIntoDir(pyNmlUtils, toDir1, true, true);
                
                File toDir2 = new File(dirForNeuronFiles, pyNeuUtils.getName());
                GeneralUtils.copyDirIntoDir(pyNeuUtils, toDir2, true, true);
                
                
                if (runMode== RUN_PYTHON_XML)
                {
                    File networkFile = new File(dirForNeuronFiles, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML);

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
                    }
                } 
                else if (runMode == RUN_PYTHON_HDF5)
                {
                    
                    File networkFile = new File(dirForNeuronFiles, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_HDF5);
                    try
                    {
                        NetworkMLWriter.createNetworkMLH5file(networkFile, 
                                                              project,
                                                              simConfig,
                                                              NetworkMLConstants.UNITS_PHYSIOLOGICAL);
                    }
                    catch (Hdf5Exception ex1)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem saving network in NeuroML HDF5 file: "+ networkFile, ex1, null);
                    }
                }
                    


                mainPythonFile = new File(dirForNeuronFiles, makePythonFriendly(project.getProjectName() + ".py"));
                
                pythonWriter = new FileWriter(mainPythonFile);
                
                
                
                pythonWriter.write(PythonUtils.getFileHeader());
                
                pythonWriter.write(generatePythonIncludes());
                
                StringBuilder mainScript = new StringBuilder();
                
                //mainScript.append(generateWelcomeComments());
                
                mainScript.append(initialPythonSetup());

                mainScript.append(initialisePythonLogging());

                mainScript.append(generateCellGroups());
                
                mainScript.append(loadNetworkMLStructure());
                
                
                pythonWriter.write(PythonUtils.addMethodDef("loadNetwork", "",
                        mainScript.toString(), "This is the main function which will be called by the hoc file.\nSubject to change..."));


            }

            mainHocFile = new File(dirForNeuronFiles, project.getProjectName() + ".hoc");

            hocWriter = new FileWriter(mainHocFile);

            hocWriter.write(getHocFileHeader());


            //if (!simConfig.getMpiConf().isParallel())

            
            hocWriter.write(generateGUIInclude());
            
            hocWriter.write(generateWelcomeComments());

            hocWriter.write(generateHocIncludes());

            hocWriter.write(getHostname());

            hocWriter.write(initialiseParallel());
            
            hocWriter.write(generateRandomise());

            hocWriter.write(generateNeuronCodeBlock(NativeCodeLocation.BEFORE_CELL_CREATION));
            
            hocWriter.write(associateCellsWithNodes());
            
            
            hocWriter.flush();
            
            if (isRunModePythonBased(runMode))
            {
                hocWriter.write(getHocPythonStartup(project));
                
                hocWriter.write(generateInitialParameters());
            }
            
            if (!isRunModePythonBased(runMode))
            {

                hocWriter.write(generateCellGroups());
                
                hocWriter.flush();

                hocWriter.write(generateInitialParameters());
                hocWriter.flush();
    
                generateNetworkConnections(hocWriter);
            }
           
            hocWriter.write(generateStimulations());
            
            hocWriter.flush();

            hocWriter.write(generateAccess());
            
            hocWriter.write(generateRunSettings());
            
            if (runMode != RUN_VIA_CONDOR && !simConfig.getMpiConf().isParallelOrRemote()) // No gui if it's condor or parallel...
            {
                if (project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.ALL_SHOW))
                {
                    hocWriter.write(generatePlots());

                    if (project.neuronSettings.isShowShapePlot())
                    {
                        hocWriter.write(generateShapePlot());
                    }

                }

            }
            
            hocWriter.write(generateInitHandlers());
            
            hocWriter.write(generateNeuronSimulationRecording());
                
            
            // Finishing up...
            
            if (!simConfig.getMpiConf().isParallelOrRemote() && !project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                hocWriter.write(generateGUIForRerunning());
           
            hocWriter.write(generateNeuronCodeBlock(NativeCodeLocation.AFTER_SIMULATION));
            
            if (simConfig.getMpiConf().isParallelNet())
                hocWriter.write(finishParallel());
            
            if (runMode == RUN_VIA_CONDOR ||
                   quitAfterRun ||
                   project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                hocWriter.write(generateQuit());
            

            hocWriter.flush();
            hocWriter.close();
            
            if (isRunModePythonBased(runMode))
            {
                pythonWriter.flush();
                pythonWriter.close();
            }

            if (isRunModePythonBased(runMode) || project.neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC))
            {
                pythonRunWriter.flush();
                pythonRunWriter.close();
            }

            if (utilsFile.getAbsoluteFile().exists())
            {
                GeneralUtils.copyFileIntoDir(utilsFile.getAbsoluteFile(), dirForNeuronFiles);
            }
            else
            {
                logger.logComment("File doesn't exist: "+ utilsFile.getAbsolutePath());
            }
            
            if (cellCheckFile.getAbsoluteFile().exists())
            {
                GeneralUtils.copyFileIntoDir(cellCheckFile.getAbsoluteFile(), dirForNeuronFiles);
            }
            else
            {
                logger.logComment("File doesn't exist: "+ utilsFile.getAbsolutePath());
            }

        }
        catch (IOException ex)
        {

            try
            {
                hocWriter.close();
            }
            catch (IOException ex1)
            {
            }
            catch (NullPointerException ex1)
            {
            }

            throw new NeuronException("Error writing to file: " + mainHocFile
                                      + "\n" + ex.getMessage()
                                      +
                "\n\nEnsure the NEURON files you are trying to generate are not currently being used.\n" +
                "Note: try selecting the \"Copy files to simulations dir\" option in the NEURON tab to prevent this.\n" +
                "Hover over that check box for more details.\n ");
        }
        //generatedRunMode = runMode;
        this.hocFileGenerated = true;
        

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("****  Created Main hoc file: " + mainHocFile+" in "+genTime+" seconds. **** \n");
        
        return;

    }

    
    /*
     * Forces recompiling of mod files on next time needed
     */
    public void forceNextModRecompile()
    {
        firstRecompileComplete = false;
    }


    /*
     * this could be used as a workaround for the "recompile mods at least once" default policy
     */
    public void forceNextModNotRecompile()
    {
        firstRecompileComplete = true;
    }
    
    public static String makePythonFriendly(String name)
    {
        return GeneralUtils.replaceAllTokens(name, "-", "_"); 
    }
    
    private static String getHocPythonStartup(Project project)
    {
        StringBuilder response = new StringBuilder();

        addMajorHocComment(response,"Setting up Python to allow loading in of NetworkML ");
        
        response.append("nrnpython(\"import sys\")\n");
        response.append("nrnpython(\"import os\")\n\n");
        
        addHocComment(response, "Adding current path to Python path");

        response.append("nrnpython(\"if sys.path.count(os.getcwd())==0: sys.path.append(os.getcwd())\")\n");
        

        response.append("nrnpython(\"import neuron\")\n");
        response.append("nrnpython(\"from neuron import hoc\")\n");
        response.append("nrnpython(\"import nrn\")\n\n");


        response.append("objref py\n");
        
        response.append("py = new PythonObject()\n\n");

        response.append("nrnpython(\"h = hoc.HocObject()\")\n\n");

        addHocComment(response, "Importing main Python file: "+project.getProjectName());
        
        String mainPackage = makePythonFriendly(project.getProjectName());
        response.append("nrnpython(\"import "+mainPackage+"\")\n\n");
        response.append("nrnpython(\""+mainPackage+".loadNetwork()\")\n\n");

        response.append("\n");
        return response.toString();
    }
    
    
    public void setQuitAfterRun(boolean quit)
    {
        this.quitAfterRun = quit;
    }
    


    /** @todo Put option on NEURON frame for this... */

    private void removeAllPreviousFiles()
    {
        cellTemplatesGenAndIncluded.removeAllElements();
        cellMechFilesGenAndIncl.removeAllElements();

        File hocFileDir = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());

        //GeneralUtils.removeAllFiles(hocFileDir, false, true, true);
        File[] allFiles = hocFileDir.listFiles();
        File modsDir = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());
        File forceRegenerateFile = new File(modsDir, NeuronFileManager.FORCE_REGENERATE_MODS_FILENAME);
        
        if (allFiles!=null)
        {
            for (int i = 0; i < allFiles.length; i++)
            {
                if (firstRecompileComplete &&
                    !forceRegenerateFile.exists() &&
                    !project.neuronSettings.isForceModFileRegeneration() && 
                    (allFiles[i].getName().endsWith(".mod") ||
                    allFiles[i].getName().endsWith(".dll") ||
                    allFiles[i].getName().equals(GeneralUtils.DIR_64BIT) ||
                    allFiles[i].getName().equals(GeneralUtils.DIR_I686) ||
                    allFiles[i].getName().equals(GeneralUtils.DIR_I386) ||
                    allFiles[i].getName().equals(GeneralUtils.DIR_POWERPC)))
                {
                    logger.logComment("Leaving in place file: "+ allFiles[i]);
                }
                else
                {
                    if (allFiles[i].isDirectory())
                    {
                        GeneralUtils.removeAllFiles(allFiles[i], false, true, true);
                    }
                    else
                    {
                        allFiles[i].delete();
                    }
                }
            }
        }

    }

    public ArrayList<String> getGeneratedSimReferences()
    {
        if (multiRunManager== null)
            return new ArrayList<String>();
        return this.multiRunManager.getGeneratedSimReferences();
    }

    public Vector getGeneratedFilenames()
    {
        try
        {
            Vector<String> allFiles = new Vector<String>();
            for (int i = 0; i < cellTemplatesGenAndIncluded.size(); i++)
            {
                allFiles.add( (new File( cellTemplatesGenAndIncluded.get(i))).getName());
            }
            allFiles.add(getMainHocFile().getName());
            return allFiles;
        }
        catch (NeuronException e)
        {
            logger.logError("Files not yet generated!", e);
            return null;
        }
    }

    private static String getHocFileHeader()
    {
        StringBuilder response = new StringBuilder();
        response.append("//  ******************************************************\n");
        response.append("// \n");
        response.append("//     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append("// \n");
        response.append("//  ******************************************************\n");

        response.append("\n");
        return response.toString();
    }

    private String generateGUIInclude()
    {
        StringBuilder response = new StringBuilder();

        if (project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
        {
            response.append("{load_file(\"stdlib.hoc\")}" + "\n");
            response.append("{load_file(\"stdgui.hoc\")}" + "\n");
        }
        else
        {
            response.append("{load_file(\"nrngui.hoc\")}" + "\n");
        }
        
        addHocComment(response, "Initialising stopwatch for timing setup");
        response.append("{startsw()}\n\n");
        return response.toString();
    }

    private String generateNeuronCodeBlock(NativeCodeLocation ncl)
    {
        StringBuilder response = new StringBuilder();

        String text = project.neuronSettings.getNativeBlock(ncl);

        text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());

        logger.logComment("Cleaned up to: "+ text);

        if (text == null || text.trim().length() == 0)
        {
            return "";
        }
        else
        {
            addHocComment(response, "Hoc commands to run at location: " + ncl.toString());
            response.append(text + "\n");
            addHocComment(response, "End of hoc commands to run at location: " + ncl.toString());

            return response.toString();
        }
    }

    private boolean warnedOfNewObjInBlock = false;

    private String generateInitHandlers()
    {
        StringBuilder response = new StringBuilder();

        NativeCodeLocation[] neuronFInitNcls = new NativeCodeLocation[]
            {NativeCodeLocation.BEFORE_INITIAL,
            NativeCodeLocation.AFTER_INITIAL,
            NativeCodeLocation.BEFORE_FINITIALIZE_RETURNS,
            NativeCodeLocation.START_FINITIALIZE};

        StringBuilder nativeBlocks = new StringBuilder();

        for (int i = 0; i < neuronFInitNcls.length; i++)
        {
            String text = project.neuronSettings.getNativeBlock(neuronFInitNcls[i]);

            text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());
            logger.logComment("Cleaned up to: "+ text);

            if (!warnedOfNewObjInBlock && (text.indexOf("objref")>=0 || text.indexOf("objvar")>=0))
            {
                GuiUtils.showWarningMessage(logger, "Warning! The text for NEURON code block Type "+neuronFInitNcls[i].getPositionReference()+" seems to be creating new objects.\n" +
                    "New objects should only be created in blocks Type -1 or Type 10, as the other blocks are placed in functions\n" +
                    "which are called using FInitializeHandler, and new objects cannot be created inside functions.", null);

                warnedOfNewObjInBlock = true;
            }


            if (text != null && text.trim().length() > 0)
            {
                int ref = (int)neuronFInitNcls[i].getPositionReference();

                String objName = "fih_" + ref;
                String procName = "callfi" + ref;
                addHocComment(nativeBlocks, "Hoc commands to run at location: " + neuronFInitNcls[i].toString());
                nativeBlocks.append("objref " + objName + "\n");
                nativeBlocks.append(objName + " = new FInitializeHandler(" + neuronFInitNcls[i].getPositionReference() +
                                ", \"" +
                                procName + "()\")" + "\n");
                nativeBlocks.append("proc " + procName + "() {" + "\n");
                nativeBlocks.append(text + "\n");
                nativeBlocks.append("}" + "\n");

                addHocComment(nativeBlocks, "End of hoc commands to run at location: " + neuronFInitNcls[i].toString());

            }

        }

        if (nativeBlocks.length()>0)
        {
            addMajorHocComment(response, "Adding blocks of native NEURON code");
            response.append(nativeBlocks.toString());
        }


        return response.toString();
    }


    private String generateInitialParameters()
    {
        StringBuilder responseMain = new StringBuilder();
        StringBuilder responseType0 = new StringBuilder();
        StringBuilder responseType1 = new StringBuilder();

        addMajorHocComment(responseMain, "Setting initial parameters");
        
        responseMain.append("strdef simConfig\n");
        responseMain.append("{simConfig = \""+this.simConfig.getName()+"\"}\n");

        responseMain.append("{celsius = " + project.simulationParameters.getTemperature() + "}\n\n");

        responseType0.append("proc initialiseValues0() {\n\n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        int lineCount = 0;
        int lineCountType1 = 0;
        int extraFuncCount = 0;
        int extraFuncCountType1 = 0;
        ArrayList<String> extraFuncNames = new ArrayList<String>();

        for (int cellGroupIndex = 0; cellGroupIndex < cellGroupNames.size(); cellGroupIndex++)
        {
            String cellGroupName = cellGroupNames.get(cellGroupIndex);

            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
            if (numInCellGroup > 0)
            {

                String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
                Cell cell = project.cellManager.getCell(cellType);

                String nameOfNumberOfTheseCells = "n_" + cellGroupName;
                String nameOfArrayOfTheseCells = "a_" + cellGroupName;

                ArrayList cellGroupPositions = project.generatedCellPositions.getPositionRecords(cellGroupName);

                addHocComment(responseType0, "Setting initial vals in cell group: " + cellGroupName
                           + " which has " + cellGroupPositions.size() + " cells");

                for (int cellIndex = 0; cellIndex < cellGroupPositions.size(); cellIndex++)
                {
                    PositionRecord posRecord
                        = (PositionRecord) cellGroupPositions.get(cellIndex);

                    if (posRecord.hasUniqueInitV())
                    {
                        if (lineCount>NeuronTemplateGenerator.MAX_NUM__LINES_IN_PROC)
                        {
                            String extraFunc = "initialiseValues0_"+extraFuncCount;
                            extraFuncNames.add(extraFunc);

                            responseType0.append("    "+extraFunc+"()\n}\n");
                            responseType0.append("proc "+extraFunc+"() {\n\n");
                            extraFuncCount++;
                            lineCount = 0;
                        }
                        double initVolt = posRecord.getInitV();


                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            responseType0.append("    if(isCellOnNode(\""+cellGroupName+"\", "
                                                                        + posRecord.cellNumber + ")) {");
                        }

                        responseType0.append("    forsec " + nameOfArrayOfTheseCells + "[" + posRecord.cellNumber + "].all {   v = " + initVolt + "  }");

                        if (simConfig.getMpiConf().isParallelNet())
                            responseType0.append("  }\n");
                        else
                            responseType0.append(" \n");

                        lineCount++;
                    }

                }

                if (cell.getInitialPotential().getDistributionType() == NumberGenerator.FIXED_NUM &&
                    cellGroupPositions.size() > 0)
                {
                    if (lineCount > NeuronTemplateGenerator.MAX_NUM__LINES_IN_PROC) {
                        String extraFunc = "initialiseValues0_" + extraFuncCount;
                        extraFuncNames.add(extraFunc);

                        responseType0.append("    " + extraFunc + "()\n}\n");
                        responseType0.append("proc " + extraFunc + "() {\n\n");
                        extraFuncCount++;
                        lineCount = 0;
                    }

                    double initVolt = UnitConverter.getVoltage(cell.getInitialPotential().getNextNumber(),
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               UnitConverter.NEURON_UNITS);

                    addHocComment(responseType0, "Giving all cells an initial potential of: " + initVolt);

                    responseType0.append("    for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");
                    responseType0.append("        ");

                    if (simConfig.getMpiConf().isParallelNet()) responseType0.append("if(isCellOnNode(\""+cellGroupName
                                                                        +"\", i)) ");

                    responseType0.append("forsec " + nameOfArrayOfTheseCells + "[i].all "
                                    + " v = " + initVolt + "\n\n");
                    responseType0.append("    }" + "\n\n");


                    lineCount+=4;

                }
                if (cell.getIonPropertiesVsGroups().size()>0)
                {
                    if (lineCountType1 > NeuronTemplateGenerator.MAX_NUM__LINES_IN_PROC) {
                        String extraFunc = "initialiseValues1_" + extraFuncCountType1;
                        extraFuncNames.add(extraFunc);

                        responseType1.append("    " + extraFunc + "()\n}\n");
                        responseType1.append("proc " + extraFunc + "() {\n\n");
                        extraFuncCountType1++;
                        lineCountType1 = 0;
                    }
                    lineCountType1+=10; // Note lines from this section shouldn't contribute much to the overall line count.
                    
                    NeuronFileManager.addHocComment(responseMain, "    Note: the following values are from IonProperties in Cell");

                    Enumeration<IonProperties> ips = cell.getIonPropertiesVsGroups().keys();

                    responseType1.append("    for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");

                    if (simConfig.getMpiConf().isParallelNet()) responseType1.append("      if(isCellOnNode(\""+cellGroupName
                                                                        +"\", i)) {\n");
                    while (ips.hasMoreElements())
                    {
                        IonProperties ip = ips.nextElement();
                        Vector<String> groups = cell.getIonPropertiesVsGroups().get(ip);
                        for (String group: groups)
                        {
                            if (ip.revPotSetByConcs())
                            {
                                float concFactor = (float)UnitConverter.getConcentration(1,
                                        UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.NEURON_UNITS);

                                responseType1.append("        forsec "+nameOfArrayOfTheseCells + "[i]." + group + " {\n" +
                                        "            "+ip.getName()+"i = "+ip.getInternalConcentration()*concFactor+"\n"+
                                        "            "+ip.getName()+"o = "+ip.getExternalConcentration()*concFactor+"\n" +
                                        "        }\n");
                            }
                            else
                            {
                                responseType1.append("        forsec "+nameOfArrayOfTheseCells + "[i]." + group + " { e"+ip.getName()+" = "+
                                        ip.getReversalPotential()+"}\n"); // Note NEURON & nC units of volts are same...
                            }
                        }
                    }

                    if (simConfig.getMpiConf().isParallelNet()) responseType1.append("      }\n");
                    responseType1.append("    }\n\n");
                }

                responseMain.append("\n");
            }
        }



        responseType0.append("}\n\n");

        for (String extraFunc: extraFuncNames)
        {
            //responseMain.append(extraFunc+"()\n");
        }
        responseMain.append("\n");

        responseMain.append(responseType0.toString());

        responseMain.append("objref fih0\n");
        responseMain.append("{fih0 = new FInitializeHandler(0, \"initialiseValues0()\")}\n\n\n");

        if (responseType1.length()>0)
        {

            responseMain.append("proc initialiseValues1() {\n\n");
            responseMain.append(responseType1.toString());
            responseMain.append("}\n\n");

            responseMain.append("objref fih1\n");
            responseMain.append("{fih1 = new FInitializeHandler(1, \"initialiseValues1()\")}\n\n\n");
        }

        return responseMain.toString();
    }

    private String generateAccess()
    {
        StringBuilder response = new StringBuilder();
        response.append("\n");

        if (simConfig.getMpiConf().isParallelNet())
        {
            addHocComment(response, "Cycling through cells and setting access to first one on this node");
            response.append("test_gid = 0\n");
            response.append("while (test_gid < ncell) {\n");
            response.append("    if (pnm.gid_exists(test_gid)) {\n");
           // if (addComments)
            //    response.append("        //print \"Setting access on host \", host, \", host id \", hostid, \", to cell gid: \", test_gid, \":\"\n");
            response.append("        objectvar accessCell\n");
          //  response.append("        //print pnm.pc.gid2cell(test_gid).Soma\n");
            response.append("        test_gid = ncell\n");
            response.append("    } else {\n");
            response.append("        test_gid = test_gid + 1\n");
            response.append("    } \n");
            response.append("}\n");

            return response.toString();
        }


        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        if (cellGroupNames.isEmpty())
        {
            logger.logError("There are no cell groups!!", null);
            return "";
        }
        Cell cellToWatch = null;
        String cellGroupToWatch = null;
        int cellNumToWatch = -1;

        cellGroupToWatch = null;
        int cellGroupCount = 0;
        ArrayList<String> allCellGroups = project.cellGroupsInfo.getAllCellGroupNames();

        while (cellGroupToWatch == null)
        {
            String nextCellGroup = allCellGroups.get(cellGroupCount);

            if (project.generatedCellPositions.getNumberInCellGroup(nextCellGroup) > 0)
            {
                cellGroupToWatch = nextCellGroup;
            }
            cellGroupCount++;
        }

        cellNumToWatch = 0;
        cellToWatch = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroupToWatch));

        //this.addHocFileComment(response, "Accessing cell by it's type.. ");
        response.append("access " + cellToWatch.getInstanceName() + "[" + cellNumToWatch + "]."
                        + getHocSectionName(cellToWatch.getFirstSomaSegment().getSection().getSectionName()));
        response.append("\n");

        return response.toString();
    }

    private String generateRandomise()
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response, "Initializes random-number generator");
        response.append("{use_mcell_ran4(1)}\n\n");
        if (!simConfig.getMpiConf().isParallelNet())
        {
            response.append("{mcell_ran4_init(" + this.randomSeed + ")}\n");
        }
        else
        {
            addHocComment(response, "As the simulation is being run in parallel, initialising differently on each host.\n"
                                +"Adding the hostid to the rand seed allows reproducability of the sim, as long as the\n"
                                +"same network distribution is used.");
            
            response.append("{mcell_ran4_init(" + this.randomSeed + " + hostid)}\n");
        }
        
        return response.toString();

    }

    private String getHostname()
    {
        StringBuilder response = new StringBuilder();

        if (this.savingHostname()) // temporarily disabled for win, will it ever be needed?
        {
            addHocComment(response, "Getting hostname");
    
            response.append("objref strFuncs\n");
            response.append("{strFuncs = new StringFunctions()}\n");
    
            response.append("strdef host\n");
    
            if (!generateLinuxBasedScripts())
                response.append("{system(\"C:/WINDOWS/SYSTEM32/hostname.exe\", host)}\n");
            else
            {
                if (!(simConfig.getMpiConf().getQueueInfo()!=null && simConfig.getMpiConf().getQueueInfo().getQueueType().equals(QueueInfo.QueueType.LL)))
                {
                    response.append("{system(\"hostname\", host)}\n");
                }
            }

            response.append("if (strFuncs.len(host)>0) {\n");
            response.append("    strFuncs.left(host, strFuncs.len(host)-1) \n");
            response.append("} else {\n");
            response.append("    host = \"????\" \n");
            response.append("}\n\n");
        }
        
        return response.toString();
    }



    private String initialiseParallel()
    {
        StringBuilder response = new StringBuilder();
        
        if (simConfig.getMpiConf().isParallelNet())
        {
            addMajorHocComment(response, "Initialising parallelization");
    
    
            response.append("ncell = " + project.generatedCellPositions.getNumberInAllCellGroups() + "\n\n");
    
            addHocComment(response, "Parallel NEURON setup");
    
            response.append("{load_file(\"netparmpi.hoc\")}\n");
            response.append("objref pnm\n");
            response.append("{pnm = new ParallelNetManager(ncell)}\n\n");
    
            response.append("{hostid = pnm.pc.id}\n\n");
    
            if (addComments) response.append("print \"Set up ParallelNetManager managing \",ncell,\"cells in total on: \", host, \" with hostid: \", hostid\n");
    
    
            //response.append("pnm.round_robin()\n");
    
            response.append("\n");
        }
        else
        {
            addHocComment(response, "Simulation running in serial mode, setting default host id");
            response.append("{hostid = 0}\n\n");
        }

        return response.toString();

    }


    private String associateCellsWithNodes()
    {
        StringBuilder response = new StringBuilder();
        
        if (!simConfig.getMpiConf().isParallelNet())
        {
            if (!isRunModePythonBased(genRunMode))
            {
                return ""; // nothing to do
            }
            else
            {
                response.append("\n\nfunc isCellOnNode() {\n");
                response.append("    return 1 // serial mode, so yes...\n");
                response.append("}\n");
                
                return response.toString();
            }
        }

        addMajorHocComment(response, "Associating cells with nodes");

        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");
        
        MpiConfiguration mpiConfig = simConfig.getMpiConf();

        int totalProcs = mpiConfig.getTotalNumProcessors();
        

        addHocComment(response, "MPI Configuration: "+ mpiConfig.toString().trim());


        response.append("func getCellGlobalId() {\n\n");

        int currentGid = 0;

        for (int cellGroupIndex = 0; cellGroupIndex < cellGroupNames.size(); cellGroupIndex++)
        {
            String cellGroupName = cellGroupNames.get(cellGroupIndex);
            response.append("    if (strcmp($s1,\""+cellGroupName+"\")==0) {\n");

            addHocComment(response, "There are " + project.generatedCellPositions.getNumberInCellGroup(cellGroupName)
                            + " cells in this Cell Group", "        ", false);

            response.append("        cgid = "+currentGid+" + $2\n");
            currentGid+=project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
            response.append("    }\n\n");
        }


        response.append("    return cgid\n");
        response.append("}\n\n");
        
        
        
     

        StringBuilder gidToNodeInfo = new StringBuilder();
        
        for (int cellGroupIndex = 0; cellGroupIndex < cellGroupNames.size(); cellGroupIndex++)
        {
            String cellGroupName = cellGroupNames.get(cellGroupIndex);
            ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(cellGroupName);

            ////////////response.append("    if (strcmp($s1,\""+cellGroupName+"\")==0) {\n");

            for (PositionRecord pr: posRecs)
            {

                gidToNodeInfo.append("{pnm.set_gid2node(getCellGlobalId(\"" + cellGroupName
                        + "\", " + pr.cellNumber + "), " + pr.getNodeId() + ")}\n");
                

                ///////////response.append("        if ($2 == "+pr.cellNumber+") return (hostid == " + pr.nodeId + ")\n");

            }
            ////////////response.append("    }\n\n");
        }
        //////////////response.append("    return 0\n");

        response.append(gidToNodeInfo.toString()+"\n"); 
    
    
        
        

        addHocComment(response, "Returns 0 or 1 depending on whether the gid for cell group $s1, id $2\n"+
                "is on this node i.e. via set_gid2node() or register_cell()", false);
        response.append("func isCellOnNode() {\n\n");
        response.append("    cellgid = getCellGlobalId($s1, $2)\n");

        response.append("    return pnm.gid_exists(cellgid)!=0\n");

        response.append("}\n\n");

        response.append("\n");

        return response.toString();

    }
 /*
    private String runworkerCutoff()
    {

        StringBuilder response = new StringBuilder();

        this.addComment(response,
                               "Everything before this will be run by all workers, after, only the master runs");

        response.append("pnm.pc.runworker()\n");
        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        if (cellGroupNames.size() == 0)
        {
            logger.logComment("There are no cell groups!!");

            addMajorComment(response, "There were no cell groups specified in the project...");
            return response.toString();
        }
        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            ArrayList cellGroupPositions = project.generatedCellPositions.getPositionRecords(cellGroupName);
            if (project.generatedCellPositions.getNumberInCellGroup(cellGroupName) == 0)
            {
                logger.logComment("No cells generated in that group. Ignoring...");
            }
            else
            {

                String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);

                addComment(response, "Adding " + cellGroupPositions.size()
                                  + " cells of type " + cellTypeName);

                String nameOfNumberOfTheseCells = "n_" + cellGroupName;

                response.append("for i=0, " + nameOfNumberOfTheseCells + " - 1 {\n");

                response.append("pnm.pc.submit(\"addCell_" + cellGroupName + "\", i)\n");

                response.append("}\n");
            }
        }
        response.append("while (pnm.pc.working) {\n");

        response.append("ret =  pnm.pc.retval    // the return value for the executed function\n");

        response.append("print \"Returned value: \", ret\n");

        response.append("}\n");

        return response.toString();


    }*/

    private String finishParallel()
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response, "Shutting down parallelisation");

        ///response.append("forall psection()\n");
        response.append("\n");
        response.append("\n");
        response.append("\n");


        response.append("{pnm.pc.done}\n");
       // response.append("quit()\n");



        return response.toString();

    }

    public long getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    public long getCurrentRunMode()
    {
        return this.genRunMode;
    }

    public float getCurrentGenTime()
    {
        return genTime;
    }

    private String generateWelcomeComments()
    {
        StringBuilder response = new StringBuilder();
        if (!project.neuronSettings.isGenerateComments()) return "";
        
        /*if (simConfig.getMpiConf().isParallel())
        {
            response.append("if (hostid == 0) {\n");
        }*/

        String indent = "    ";

        response.append("print \"\"\n");
        response.append("print \"*****************************************************\"\n");
        response.append("print \"\"\n");
        response.append("print \""+indent+"neuroConstruct generated NEURON simulation \"\n");
        response.append("print \""+indent+"for project: " + project.getProjectFile().getAbsolutePath() + " \"\n");
      
        response.append("print \"\"\n");



        String desc = new String();

        if (project.getProjectDescription() != null) desc = project.getProjectDescription();
        
        desc = GeneralUtils.replaceAllTokens(desc, "\"", "");
        desc = GeneralUtils.replaceAllTokens(desc, "\n", "\"\nprint \""+indent);
        
        

        response.append("print \""+indent+"Description: " + desc + "\"\n");
        
        response.append("print \"\"\n");

        response.append("print \""+indent+"Simulation Configuration: " + simConfig + " \"\n");
        if (simConfig.getDescription().trim().length()>0)
        {
            desc = simConfig.getDescription();

            desc = GeneralUtils.replaceAllTokens(desc, "\"", "");
            desc = GeneralUtils.replaceAllTokens(desc, "\n", "\"\nprint \""+indent);
            
            response.append("print \""+indent + desc + " \"\n");
        }

        response.append("print \" \"\n");
        response.append("print  \"*****************************************************\"\n\n");
        
        if ((GeneralUtils.isLinuxBasedPlatform() || GeneralUtils.isMacBasedPlatform()) 
                && !(simConfig.getMpiConf().getQueueInfo()!=null && simConfig.getMpiConf().getQueueInfo().getQueueType().equals(QueueInfo.QueueType.LL)))
        {
            response.append("strdef pwd\n");
    
            response.append("{system(\"pwd\", pwd)}\n");
            
            response.append("print \"\"\n");
            response.append("print \"Current working dir: \", pwd\n\n");
        }
        
        /*if (simConfig.getMpiConf().isParallel())
        {
            response.append("}\n");
        }*/
        return response.toString();
    };

    private String generateHocIncludes()
    {
        StringBuilder response = new StringBuilder();

        response.append("objectvar allCells\n");
        response.append("{allCells = new List()}\n\n");

        addHocComment(response, "A flag to signal simulation was generated by neuroConstruct ");
        response.append("{nC = 1}\n\n");
        
        addHocComment(response, "Including neuroConstruct utilities file ");
        response.append("{load_file(\""+utilsFile.getName()+"\")}\n");
        addHocComment(response, "Including neuroConstruct cell check file ");
        response.append("{load_file(\""+cellCheckFile.getName()+"\")}\n");

        return response.toString();
    }
    
    
    private String generatePythonRunFile()
    {
        StringBuilder response = new StringBuilder();
        
        response.append("import neuron\n");
        response.append("from neuron import hoc\n");
        response.append("import nrn\n\n");
        
        
        PythonUtils.addComment(response, "Note: As neuroConstruct already generates hoc, much of this is reused and not (yet) converted \n" +
                "to pure Python. It is mainly the cell and network creation that will benefit from the Python parsing of XML/HDF5", addComments);
        
        response.append("hoc.execute('load_file(\""+project.getProjectName()+".hoc\")')\n");
        
        return response.toString();
    }


    private String generatePythonIncludes()
    {
        StringBuilder response = new StringBuilder();


        PythonUtils.addComment(response, "Including some standard Python modules ", addComments);
        
        response.append("import sys\n");
        response.append("import os\n\n");
        
        response.append("import xml.sax\n");
        response.append("import time\n\n");

        response.append("import logging\n\n");


        PythonUtils.addComment(response, "Adding working dir to Python path", addComments);
        
        response.append("if sys.path.count(os.getcwd())==0: sys.path.append(os.getcwd())\n\n");

        PythonUtils.addComment(response, "Including NEURON specifics", addComments);
        
        
        response.append("import neuron\n");
        response.append("from neuron import hoc\n");
        response.append("import nrn\n\n");
        

        response.append("sys.path.append(\"NeuroMLUtils\")\n");
        response.append("sys.path.append(\"NEURONUtils\")\n\n");

        if (genRunMode== RUN_PYTHON_XML) 
            response.append("import NetworkMLSaxHandler\n");
        
        if (genRunMode== RUN_PYTHON_HDF5) 
            response.append("import NetworkMLHDF5Handler\n");
        
        response.append("import NEURONSimUtils\n\n");



        //response.append("from NetworkMLSaxHandler import NetworkMLSaxHandler\n");
        //response.append("from NetworkHandler import NetworkHandler\n\n");

        return response.toString();
    }

    
    private String initialisePythonLogging()
    {
        StringBuilder response = new StringBuilder();
        
        String logLevel = "WARN";
        if (addComments) logLevel ="INFO";

        response.append("logformat = \"%(name)-19s %(levelname)-5s -\"+str(int(h.hostid))+\"- %(message)s\"\n");

        response.append("logging.basicConfig(level=logging."+logLevel+", format=logformat)\n");
        
        response.append("log = logging.getLogger(\""+project.getProjectName()+"\")\n\n");
        
        return response.toString();
    }
    

    private String initialPythonSetup()
    {
        StringBuilder response = new StringBuilder();
        
        response.append("h = hoc.HocObject()\n\n");
        
        /*
        if (simConfig.getMpiConf().isParallel())
        {
            response.append("h.load_file(\"netparmpi.hoc\")\n");
            
            response.append("h(\"objref pnm\")\n");
            response.append("h(\"pnm = new ParallelNetManager(20)\")\n");
            
            response.append("h(\"hostid = pnm.pc.id\")\n");

            response.append("print \"My host id: %d\" % (h.hostid)\n\n");
        }
        else
        {
            response.append("h(\"hostid = 0\")\n");
        }*/
        
        return response.toString();
    }
    
    
    /*
     * Only used when Python is script...
     * 
     */
    private String loadNetworkMLStructure()
    {
        StringBuilder response = new StringBuilder();
        
        String nmlFile = null;
        
        if (genRunMode== RUN_PYTHON_XML)
        {
            nmlFile = NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML;
        } 
        else if (genRunMode == RUN_PYTHON_HDF5)
        {
            nmlFile = NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_HDF5;
        }
        
        PythonUtils.addPrintedComment(response, 
                "Loading cell positions and connections from: "+ nmlFile, 
                PythonUtils.LOG_LEVEL_INFO,
                true);

        response.append("file_name = '"+nmlFile+"'\n\n");

        response.append("beforeLoad = time.time()\n");
        
        response.append("nmlHandler = NEURONSimUtils.NetManagerNEURON()\n");
        
        if (genRunMode== RUN_PYTHON_XML)
        {
            response.append("parser = xml.sax.make_parser()\n");   

            response.append("curHandler = NetworkMLSaxHandler.NetworkMLSaxHandler(nmlHandler)\n");

            if (simConfig.getMpiConf().isParallelNet())
                response.append("curHandler.setNodeId(h.hostid)\n");
            else
                response.append("curHandler.setNodeId(-1) \n");
                

            response.append("parser.setContentHandler(curHandler)\n");

            response.append("parser.parse(open(file_name)) \n");
        }
        else if (genRunMode == RUN_PYTHON_HDF5)
        {
            response.append("curHandler = NetworkMLHDF5Handler.NetworkMLHDF5Handler(nmlHandler)\n");

            if (simConfig.getMpiConf().isParallelNet())
                response.append("curHandler.setNodeId(h.hostid)\n");
            else
                response.append("curHandler.setNodeId(-1) \n");


            response.append("curHandler.parse(file_name) \n");
        }
        

        response.append("afterLoad = time.time()\n");

              
        
        PythonUtils.addPrintedComment(response,
                "\"Loaded file in \"+ str(afterLoad-beforeLoad)+ \" seconds on host: %d\" % (int(h.hostid))", 
                PythonUtils.LOG_LEVEL_INFO, false);
        
        
        return response.toString();
    }
    
    

    private String generateRunSettings()
    {
        StringBuilder response = new StringBuilder();
        addMajorHocComment(response, "Settings for running the demo");
        response.append("\n");
        response.append("tstop = " + getSimDuration() + "\n");

        /** @todo See why this is necessary (dt = 0.1 etc wasn't enough...) */
        response.append("dt = " + project.simulationParameters.getDt() + "\n");
        //response.append("steps_per_ms = " + Math.round(1d / (double) project.simulationParameters.getDt()) + "\n");
        response.append("steps_per_ms = " + (float)(1d / (double) project.simulationParameters.getDt()) + "\n");

        if (simConfig.getMpiConf().isParallelNet())
        {
            response.append("{pnm.set_maxstep(5)}\n\n");
            return response.toString();
        }


        return response.toString();
    }

    private float getSimDuration()
    {
        if (simConfig.getSimDuration() == 0) // shouldn't be...
        {
            return project.simulationParameters.getDuration();
        }
        else
            return simConfig.getSimDuration();
    }

    private String getStimArrayName(String stimRef)
    {
        return "stim_" + stimRef;
    }

    private String getStimArraySizeName(String stimRef)
    {
        return "n_stim_" + stimRef;
    }

    private String generateStimulations() throws NeuronException
    {
        int totalStims = project.generatedElecInputs.getNumberSingleInputs();

        StringBuilder response = new StringBuilder(totalStims*800);  // initial cap

        ArrayList<String> allStims = this.simConfig.getInputs();

        addMajorHocComment(response, "Adding " + allStims.size() + " stimulation(s)");

        for (int k = 0; k < allStims.size(); k++)
        {
            //StimulationSettings nextStim = project.generatedElecInputs.getStim();
            
            logger.logComment("++++++++++++     Checking for stim ref: " + allStims.get(k));

            ArrayList<SingleElectricalInput> allInputLocs =
                project.generatedElecInputs.getInputLocations(allStims.get(k));
            
            if (allInputLocs.size() > 0)
            {
                logger.logComment("Going to add stim to " + allInputLocs.size() + " cells in input group: " +
                                  allStims.get(k));

                for (int j = 0; j < allInputLocs.size(); j++)
                {
                    SingleElectricalInput nextInput = allInputLocs.get(j);
                    
                    if (!project.cellGroupsInfo.getAllCellGroupNames().contains(nextInput.getCellGroup()))
                    {
                        throw new NeuronException("The Cell Group specified for the Stimulation: " + allStims.get(k) +
                                                  " does not exist!");
                    }

                    String stimCellType = project.cellGroupsInfo.getCellType(nextInput.getCellGroup());
                    Cell stimCell = project.cellManager.getCell(stimCellType);

                    Segment segToStim = stimCell.getSegmentWithId(nextInput.getSegmentId());
                    
                    logger.logComment("Going to add stim to seg " + nextInput.getSegmentId() + ": "+ segToStim);

                    float fractionAlongSegment = nextInput.getFractionAlong();

                    float fractionAlongSection
                        = CellTopologyHelper.getFractionAlongSection(stimCell,
                                                                     segToStim,
                                                                     fractionAlongSegment); // assume centre of segment...

                    if (nextInput.getElectricalInputType().equals(IClamp.TYPE))
                    {
                        String stimObjectFilename = ProjectStructure.getModTemplatesDir().getAbsolutePath()+"/"+ EXT_CURR_CLAMP_MOD;

                        if (!stimModFilesRequired.contains(stimObjectFilename))
                        {
                            stimModFilesRequired.add(stimObjectFilename);

                            try
                            {
                                File soFile = new File(stimObjectFilename);
                                long lastMod = soFile.lastModified();

                                File copied = GeneralUtils.copyFileIntoDir(soFile,
                                                             ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()));

                                copied.setLastModified(lastMod);

                            }
                            catch(IOException io)
                            {
                                GuiUtils.showErrorMessage(logger, "Problem copying mod file for stimulation: " + stimObjectFilename, io, null);
                                return null;
                            }
                        }

                        String stimName = getStimArrayName(allStims.get(k));

                        IClampSettings iClamp = (IClampSettings) project.elecInputInfo.getStim(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);

                        if (j == 0) // define array...
                        {
                            String sizeName = getStimArraySizeName(allStims.get(k));
                            response.append("{ "+sizeName + " = " + allInputLocs.size() + " }\n");
                            response.append("objectvar " + stimName + "[" + sizeName + "]\n\n");
                        }

                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (isCellOnNode(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + ")) {\n");
                        }

                        addHocComment(response, "Note: the stimulation was specified as being at a point "
                                          + fractionAlongSegment + " along segment: " + segToStim.getSegmentName(),prefix, false);
                        addHocComment(response, "in section: " + getHocSectionName(segToStim.getSection().getSectionName()) +
                                          ". For NEURON, this translates to a point " + fractionAlongSection +
                                          " along section: " +
                                          getHocSectionName(segToStim.getSection().getSectionName()),prefix,true);

                        response.append(prefix+"a_" + nextInput.getCellGroup()
                                        + "[" + nextInput.getCellNumber() + "]"
                                        + "." + getHocSectionName(segToStim.getSection().getSectionName()) + " {\n");

                        String stimObjectName = EXT_CURR_CLAMP_MOD.substring(0, EXT_CURR_CLAMP_MOD.indexOf(".mod"));

                        response.append(prefix+"    "+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection +
                                        ")\n");

                        float del = -1, dur = -1, amp = -1;
                        if (nextInput.getInstanceProps()!=null)
                        {
                            IClampInstanceProps icip = (IClampInstanceProps)nextInput.getInstanceProps();
                            del = icip.getDelay();
                            dur = icip.getDuration();
                            amp = icip.getAmplitude();
                        }
                        else
                        {
                            del = iClamp.getDel().getNominalNumber(); //should be a fixed num generator anyway...
                            dur = iClamp.getDur().getNominalNumber(); //should be a fixed num generator anyway...
                            amp = iClamp.getAmp().getNominalNumber(); //should be a fixed num generator anyway...
                        }

                        response.append(prefix+"    "+stimName + "[" + j + "].del = " + del + "\n");
                        response.append(prefix+"    "+stimName + "[" + j + "].dur = " + dur + "\n");
                        response.append(prefix+"    "+stimName + "[" + j + "].amp = " + amp + "\n");

                        int repeat = iClamp.isRepeat() ? 1:0;

                        response.append(prefix+"    "+stimName + "[" + j + "].repeat = " + repeat + "\n");

                        response.append(prefix+"}" + "\n");
                        response.append(post);
                        response.append("\n");

                    }
                    else if (nextInput.getElectricalInputType().equals(IClampVariable.TYPE))
                    {
                        String stimObjectFilename = ProjectStructure.getModTemplatesDir().getAbsolutePath()+"/"+ CURR_CLAMP_VAR_MOD;


                        IClampVariableSettings iClampVariable = (IClampVariableSettings) project.elecInputInfo.getStim(allStims.get(k));

                        if (!stimModFilesRequired.contains(stimObjectFilename))
                        {
                            stimModFilesRequired.add(stimObjectFilename);

                            try
                            {
                                File soFile = new File(stimObjectFilename);
                                FileReader fr = new FileReader(soFile);

                                LineNumberReader reader = new LineNumberReader(fr);
                                String nextLine = null;

                                StringBuilder sb = new StringBuilder();

                                while ((nextLine = reader.readLine()) != null)
                                {
                                    sb.append(GeneralUtils.replaceAllTokens(nextLine, CURR_CLAMP_VAR_AMP_EXPR, iClampVariable.getAmp())+ "\n");
                                }
                                reader.close();
                                fr.close();

                                File targetFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()), soFile.getName());
                                FileWriter fw = new FileWriter(targetFile);
                                fw.write(sb.toString());
                                fw.close();

                            }
                            catch(IOException io)
                            {
                                GuiUtils.showErrorMessage(logger, "Problem copying mod file for stimulation: " + stimObjectFilename, io, null);
                                return null;
                            }
                        }

                        String stimName = getStimArrayName(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);

                        if (j == 0) // define array...
                        {
                            String sizeName = getStimArraySizeName(allStims.get(k));
                            response.append("{ "+sizeName + " = " + allInputLocs.size() + " }\n");
                            response.append("objectvar " + stimName + "[" + sizeName + "]\n\n");
                        }

                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (isCellOnNode(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + ")) {\n");
                        }

                        addHocComment(response, "Note: the stimulation was specified as being at a point "
                                          + fractionAlongSegment + " along segment: " + segToStim.getSegmentName(),prefix, false);
                        addHocComment(response, "in section: " + getHocSectionName(segToStim.getSection().getSectionName()) +
                                          ". For NEURON, this translates to a point " + fractionAlongSection +
                                          " along section: " +
                                          getHocSectionName(segToStim.getSection().getSectionName()),prefix,true);

                        response.append(prefix+"a_" + nextInput.getCellGroup()
                                        + "[" + nextInput.getCellNumber() + "]"
                                        + "." + getHocSectionName(segToStim.getSection().getSectionName()) + " {\n");

                        String stimObjectName = CURR_CLAMP_VAR_MOD.substring(0, CURR_CLAMP_VAR_MOD.indexOf(".mod"));

                        response.append(prefix+"    "+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection +
                                        ")\n");

                        float del = -1, dur = -1;

                        if (nextInput.getInstanceProps()!=null)
                        {
                            IClampVariableInstanceProps icip = (IClampVariableInstanceProps)nextInput.getInstanceProps();
                            del = icip.getDelay();
                            dur = icip.getDuration();
                        }
                        else
                        {
                            del = iClampVariable.getDel().getNominalNumber(); //should be a fixed num generator anyway...
                            dur = iClampVariable.getDur().getNominalNumber(); //should be a fixed num generator anyway...
                        }

                        response.append(prefix+"    "+stimName + "[" + j + "].del = " + del + "\n");
                        response.append(prefix+"    "+stimName + "[" + j + "].dur = " + dur + "\n");


                        response.append(prefix+"}" + "\n");
                        response.append(post);
                        response.append("\n");

                    }
                    else if (nextInput.getElectricalInputType().equals(RandomSpikeTrain.TYPE))
                    {
                        // to make the NetStim more randomish...
                        int increaseFactor = 100;
                        float noise = 1f;

                        RandomSpikeTrainSettings rndTrain =
                            (RandomSpikeTrainSettings) project.elecInputInfo.getStim(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);


                        String inputNumName = "n_" + allStims.get(k);

                        String stimName = "spikesource_" + allStims.get(k);
                        String synapseName = "synapse_" + allStims.get(k);
                        String connectionName = "connection_" + allStims.get(k);

                        if (j == 0) // define arrays...
                        {
                            String comm = addComments?"// number of individual inputs in "+allStims.get(k):"";

                            response.append(inputNumName + " = " + allInputLocs.size() + " "+comm+"\n\n");

                            response.append("objref " + stimName + "[" + inputNumName + "]\n\n");
                            response.append("objref " + synapseName + "[" + inputNumName + "]\n");
                            response.append("objref " + connectionName + "[" + inputNumName + "]\n");
                            response.append("thresh = -20\n");
                            response.append("delay = 0\n");
                            response.append("weight = 1\n\n");

                        }

                        /*  This is right!!!!
                                                  response.append("access a_"
                                        + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");
                         */

                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (isCellOnNode(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + ")) {\n");
                        }

                        response.append(prefix+"access "
                                        + "a_" + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");

                        response.append(prefix+stimName + "[" + j + "] = new NetStim(" +
                                        fractionAlongSection + ")\n");

                        float rate = -1;

                        if (nextInput.getInstanceProps()!=null)
                        {
                            RandomSpikeTrainInstanceProps icip = (RandomSpikeTrainInstanceProps)nextInput.getInstanceProps();
                            rate = icip.getRate();
                        }
                        else
                        {
                            rate = rndTrain.getRate().getNominalNumber(); //should be a fixed num generator anyway...
                        }

                        addHocComment(response,
                                          "NOTE: This is a very rough way to get an average rate of " + rate +
                                          " kHz!!!", prefix, false);

                        float expectedNumber = getSimDuration()
                            * rate
                            * increaseFactor; // no units...

                        double interval = UnitConverter.getTime(1f / rate,
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                UnitConverter.NEURON_UNITS);

                        response.append(prefix+stimName + "[" + j + "].number = " + expectedNumber +
                                        "\n");
                        response.append(prefix+stimName + "[" + j + "].interval = " + interval + "\n");

                        response.append(prefix+stimName + "[" + j + "].noise = " + noise + " \n");
                        response.append(prefix+stimName + "[" + j + "].start = 0 \n");

                        response.append(prefix+synapseName + "[" + j + "] = new " +
                                        rndTrain.getSynapseType() +
                                        "(" + fractionAlongSection +
                                        ") \n");

                        addHocComment(response, " Inserts synapse 0.5 of way down",prefix, true);

                        response.append(prefix+connectionName + "["
                                        + j
                                        + "] = new NetCon("
                                        + stimName + "["
                                        + j +
                                        "], " + synapseName + "["
                                        + j +
                                        "], thresh, delay, weight)\n");

                        response.append(post);
                        response.append("\n\n");

                    }

                    else if (nextInput.getElectricalInputType().equals(RandomSpikeTrainExt.TYPE))
                    {
                        String stimObjectName = "NetStimExt";
                        String stimObjectFilename = ProjectStructure.getModTemplatesDir().getAbsolutePath()+"/"+ stimObjectName + ".mod";

                        if (!stimModFilesRequired.contains(stimObjectFilename))
                        {
                            stimModFilesRequired.add(stimObjectFilename);

                            try
                            {
                                GeneralUtils.copyFileIntoDir(new File(stimObjectFilename),
                                                             ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()));
                            }
                            catch(IOException io)
                            {
                                GuiUtils.showErrorMessage(logger, "Problem copying mod file for stimulation: " + stimObjectFilename, io, null);
                                return null;
                            }
                        }

                        // to make the NetStim more randomish...
                        int increaseFactor = 100;
                        float noise = 1f;

                        RandomSpikeTrainExtSettings rndTrainExt =
                            (RandomSpikeTrainExtSettings) project.elecInputInfo.getStim(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);

                        String stimName = "spikesource_" + allStims.get(k);
                        String synapseName = "synapse_" + allStims.get(k);
                        String connectionName = "connection_" + allStims.get(k);

                        if (j == 0) // define arrays...
                        {
                            response.append("objref " + stimName + "[" + allInputLocs.size() + "]\n\n");
                            response.append("objref " + synapseName + "[" + allInputLocs.size() + "]\n");
                            response.append("objref " + connectionName + "[" + allInputLocs.size() + "]\n");
                            response.append("thresh = -20\n");
                            response.append("delay = 0\n");
                            response.append("weight = 1\n\n");

                        }

                        /*  This is right!!!!
                                                  response.append("access a_"
                                        + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName(() + " \n");
                         */



                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (isCellOnNode(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + ")) {\n");
                        }

                        response.append(prefix+"access "
                                        + "a_" + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");

                        response.append(prefix+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection + ")\n");



                        float del = -1, dur = -1, rate = -1;

                        if (nextInput.getInstanceProps()!=null)
                        {
                            RandomSpikeTrainExtInstanceProps icip = (RandomSpikeTrainExtInstanceProps)nextInput.getInstanceProps();
                            del = icip.getDelay();
                            dur = icip.getDuration();
                            rate = icip.getRate();
                        }
                        else
                        {
                            del = rndTrainExt.getDelay().getNominalNumber(); //should be a fixed num generator anyway...
                            dur = rndTrainExt.getDuration().getNominalNumber(); //should be a fixed num generator anyway...
                            rate = rndTrainExt.getRate().getNominalNumber(); //should be a fixed num generator anyway...
                        }


                        addHocComment(response,
                                          "NOTE: This is a very rough way to get an average rate of " + rate +
                                          " kHz!!!", prefix, false);

                        float expectedNumber = getSimDuration()
                            * rate
                            * increaseFactor; // no units...

                        double interval = UnitConverter.getTime(1f / rate,
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                UnitConverter.NEURON_UNITS);

                        response.append(prefix+stimName + "[" + j + "].number = " + expectedNumber +
                                        "\n");
                        response.append(prefix+stimName + "[" + j + "].interval = " + interval + "\n");

                        response.append(prefix+stimName + "[" + j + "].noise = " + noise + " \n");
                        response.append(prefix+stimName + "[" + j + "].del = "+ del +" \n");
                        response.append(prefix+stimName + "[" + j + "].dur = "+ dur +" \n");

                        int repeat = rndTrainExt.isRepeat() ? 1:0;

                        response.append(prefix+stimName + "[" + j + "].repeat = "+ repeat +" \n");

                        response.append(prefix+synapseName + "[" + j + "] = new " +
                                        rndTrainExt.getSynapseType() +
                                        "(" + fractionAlongSection +
                                        ") \n");

                        addHocComment(response, " Inserts synapse 0.5 of way down",prefix, true);

                        response.append(prefix+connectionName + "["
                                        + j
                                        + "] = new NetCon("
                                        + stimName + "["
                                        + j +
                                        "], " + synapseName + "["
                                        + j +
                                        "], thresh, delay, weight)\n");

                        response.append(post);
                        response.append("\n\n");

                    }


                    else if (nextInput.getElectricalInputType().equals(RandomSpikeTrainVariable.TYPE))
                    {
                        String stimObjectFilename = ProjectStructure.getModTemplatesDir().getAbsolutePath()+"/"+ RAND_STIM_VAR_MOD;
                        String stimObjectName = RAND_STIM_VAR_MOD.substring(0, RAND_STIM_VAR_MOD.indexOf(".mod"));

                        RandomSpikeTrainVariableSettings rstvs = (RandomSpikeTrainVariableSettings) project.elecInputInfo.getStim(allStims.get(k));

                        if (!stimModFilesRequired.contains(stimObjectFilename))
                        {
                            stimModFilesRequired.add(stimObjectFilename);

                            try
                            {
                                File soFile = new File(stimObjectFilename);
                                FileReader fr = new FileReader(soFile);

                                LineNumberReader reader = new LineNumberReader(fr);
                                String nextLine = null;

                                StringBuilder sb = new StringBuilder();

                                while ((nextLine = reader.readLine()) != null)
                                {
                                    sb.append(GeneralUtils.replaceAllTokens(nextLine, RAND_STIM_VAR_RATE_EXPR, rstvs.getRate())+ "\n");
                                }
                                reader.close();
                                fr.close();

                                File targetFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()), soFile.getName());
                                FileWriter fw = new FileWriter(targetFile);
                                fw.write(sb.toString());
                                fw.close();

                            }
                            catch(IOException io)
                            {
                                GuiUtils.showErrorMessage(logger, "Problem copying mod file for stimulation: " + stimObjectFilename, io, null);
                                return null;
                            }
                        }


                        // to make the NetStim more randomish...
                        int increaseFactor = 100;
                        float noise = 1f;

                        RandomSpikeTrainVariableSettings rt =
                            (RandomSpikeTrainVariableSettings) project.elecInputInfo.getStim(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);

                        String stimName = "spikesource_" + allStims.get(k);
                        String synapseName = "synapse_" + allStims.get(k);
                        String connectionName = "connection_" + allStims.get(k);

                        if (j == 0) // define arrays...
                        {
                            response.append("objref " + stimName + "[" + allInputLocs.size() + "]\n\n");
                            response.append("objref " + synapseName + "[" + allInputLocs.size() + "]\n");
                            response.append("objref " + connectionName + "[" + allInputLocs.size() + "]\n");
                            response.append("thresh = -20\n");
                            response.append("delay = 0\n");
                            response.append("weight = 1\n\n");

                        }

                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (isCellOnNode(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + ")) {\n");
                        }

                        response.append(prefix+"access "
                                        + "a_" + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");

                        response.append(prefix+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection + ")\n");

                        float del = -1, dur = -1;

                        if (nextInput.getInstanceProps()!=null)
                        {
                            RandomSpikeTrainVarInstanceProps icip = (RandomSpikeTrainVarInstanceProps)nextInput.getInstanceProps();
                            del = icip.getDelay();
                            dur = icip.getDuration();
                        }
                        else
                        {
                            del = rstvs.getDelay().getNominalNumber(); //should be a fixed num generator anyway...
                            dur = rstvs.getDuration().getNominalNumber(); //should be a fixed num generator anyway...
                        }


                        response.append(prefix+stimName + "[" + j + "].noise = " + noise + " \n");
                        response.append(prefix+stimName + "[" + j + "].del = "+ del +" \n");
                        response.append(prefix+stimName + "[" + j + "].dur = "+ dur +" \n");


                        response.append(prefix+synapseName + "[" + j + "] = new " +
                                        rstvs.getSynapseType() +
                                        "(" + fractionAlongSection +
                                        ") \n");

                        addHocComment(response, " Inserts synapse 0.5 of way down",prefix, true);

                        response.append(prefix+connectionName + "["
                                        + j
                                        + "] = new NetCon("
                                        + stimName + "["
                                        + j +
                                        "], " + synapseName + "["
                                        + j +
                                        "], thresh, delay, weight)\n");

                        response.append(post);
                        response.append("\n\n");

                    }

                }
            }
            else
            {
                addHocComment(response, "No electrical inputs generated for: " + allStims.get(k));
            }
        }

        return response.toString();
    }

    private String generateMultiRunPreScript()
    {
        StringBuilder response = new StringBuilder();

        if (multiRunManager!=null)
        {
            response.append(multiRunManager.getMultiRunPreScript(SimEnvHelper.NEURON));
        }

        return response.toString();
    }

    private String generateMultiRunPostScript()
    {
        StringBuilder response = new StringBuilder();

        if (multiRunManager!=null)
        {
            response.append(multiRunManager.getMultiRunPostScript(SimEnvHelper.NEURON));
        /*
                for (String nextLoop: multiRunLoops)
                {
                    this.addHocFileComment( response,"End of loop for: "+nextLoop);
                    response.append("}\n\n");
                }*/
        }

        return response.toString();
    }


    public static String getHocSectionName(String secname)
    {
        String newName = GeneralUtils.replaceAllTokens(secname,
                ".",
                "_");
        newName = GeneralUtils.replaceAllTokens(newName,
                "[",
                "_");
        newName = GeneralUtils.replaceAllTokens(newName,
                "]",
                "_");
        
        return newName;
    }
    

    public static String getHocSegmentName(String secname)
    {
        return getHocSectionName(secname);
    }

    public String getHocFriendlyFilename(String filename)
    {
        logger.logComment("filename: " + filename);
        filename = GeneralUtils.replaceAllTokens(filename, "\\", "/");

        filename = GeneralUtils.replaceAllTokens(filename,
                                                 "Program Files",
                                                 "Progra~1");

        filename = GeneralUtils.replaceAllTokens(filename,
                                                 "Documents and Settings",
                                                 "Docume~1");

        if (!generateLinuxBasedScripts())
        {
            boolean canFix = true;
            // Can catch spaces if a dir is called c:\Padraig Gleeson and change it to c:\Padrai~1
            while (filename.indexOf(" ") > 0 && canFix)
            {
                int indexOfSpace = filename.indexOf(" ");

                int prevSlash = filename.substring(0,indexOfSpace).lastIndexOf("/");
                int nextSlash = filename.indexOf("/", indexOfSpace);

                String spacedWord = filename.substring(prevSlash+1, nextSlash);

                logger.logComment("spacedWord: " + spacedWord);

                if (spacedWord.indexOf(" ")<6) canFix = false;
                else
                {
                    String shortened = spacedWord.substring(0,6)+"~1";
                    filename = GeneralUtils.replaceAllTokens(filename, spacedWord, shortened);
                    logger.logComment("filename now: " + filename);
                }
            }
        }


        logger.logComment("filename now: " + filename);

        return filename;

    }

    /**
     *
     * Creates the vectors to store the data generated, runs the simulation, and writes the data to file
     * @param runMode int The run mode
     * @return String for hoc file
     */
    private String generateNeuronSimulationRecording()
    {
        StringBuilder response = new StringBuilder();

        response.append("\n");

        int numStepsTotal = Math.round(getSimDuration() / project.simulationParameters.getDt()) + 1;

        addMajorHocComment(response,
                        "This will run a full simulation of " + numStepsTotal +
                        " steps when the hoc file is executed");

        ArrayList<PlotSaveDetails> recordings = project.generatedPlotSaves.getSavedPlotSaves();

        addHocComment(response, "Recording " + recordings.size() + " variable(s)");

        boolean recordingSomething = !recordings.isEmpty();


        response.append("objref v_time\n");
        response.append("objref f_time\n");
        response.append("objref propsFile\n\n");

        if (recordingSomething)
        {
            String prefix = "";
            String post = "";

            if (simConfig.getMpiConf().isParallelNet())
            {
                prefix = "    ";
                post = "}" + "\n";

                response.append("if (hostid == 0) {\n");
            }

            response.append(prefix+"v_time = new Vector()\n");
            response.append(prefix+"{ v_time.record(&t) }\n");
            response.append(prefix+"{ v_time.resize(" + numStepsTotal + ") }\n");

            response.append(prefix+"f_time = new File()\n");

            response.append(post);
            response.append("\n");


        }

        for (PlotSaveDetails record : recordings)
        {
            String cellGroupName = record.simPlot.getCellGroup();

            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

            String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
            Cell cell = project.cellManager.getCell(cellType);

            String whatToRecord = convertToNeuronVarName(record.simPlot.getValuePlotted());

            if (whatToRecord==null) return null;

            boolean isSpikeRecording = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;

            if (numInCellGroup > 0)
            {
                addHocComment(response, record.getDescription(true, false));

                for (Integer segId : record.segIdsToPlot)
                {
                    Segment segToRecord = cell.getSegmentWithId(segId);

                    float lenAlongSection
                        = CellTopologyHelper.getFractionAlongSection(cell,
                                                                     segToRecord,
                                                                     0.5f);

                    if (record.allCellsInGroup && !record.simPlot.isSynapticMechanism())
                    {
                        addHocComment(response,
                                   "Creating vector for segment: " + segToRecord.getSegmentName() + "(ID: " +
                                   segToRecord.getSegmentId() + ")");

                        String objName = this.getObjectName(record, -1, getHocSegmentName(segToRecord.getSegmentName()));

                        if (isSpikeRecording) objName = objName + "_spike";

                        String vectorObj = "v_" + objName;
                        String fileObj = "f_" + objName;
                        String apCountObj = "apc_" + objName;
                        
                        vectorObj = GeneralUtils.replaceAllTokens(vectorObj, ".", "_");
                        fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");

                        response.append("objref " + vectorObj + "[" + numInCellGroup + "]\n");
                        if (isSpikeRecording) response.append("objref " + apCountObj + "[" + numInCellGroup + "]\n");

                        response.append("for i=0, " + (numInCellGroup - 1) + " {\n");

                        String prefix = "";
                        String post = "";

                        if (simConfig.getMpiConf().isParallelNet())
                        {
                            prefix = "    ";
                            post = "    }" + "\n";
                            response.append("    if (isCellOnNode(\""
                                            + cellGroupName + "\", i)) {\n");
                        }


                        response.append(prefix+"    { " + vectorObj + "[i] = new Vector() }\n");

                        if (!isSpikeRecording)
                        {
                            response.append(prefix+"    { " + vectorObj + "[i].record(&a_" + cellGroupName + "[i]"
                                            + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + "." + whatToRecord + "(" +
                                            lenAlongSection +
                                            "))} \n");
                            response.append(prefix+"    { " + vectorObj + "[i].resize(" + numStepsTotal + ") }\n");
                        }
                        else
                        {
                            response.append(prefix+"    a_" + cellGroupName + "[i]"
                                            + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + " " + apCountObj
                                            + "[i] = new APCount(" + lenAlongSection + ")\n");

                            if (record.simPlot.getValuePlotted().indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
                            {
                                String threshold = record.simPlot.getValuePlotted().substring(record.simPlot.
                                    getValuePlotted().indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) + 1);
                                response.append(prefix+"    " + apCountObj + "[i].thresh = " + threshold + "\n");

                            }
                            else
                            {
                                response.append(prefix+"    " + apCountObj + "[i].thresh = " + SimPlot.DEFAULT_THRESHOLD + "\n");
                            }
                            response.append(prefix+"    " + apCountObj + "[i].record(" + vectorObj + "[i])\n");

                        }

                        response.append(post);
                        response.append("}\n");

                        response.append("objref " + fileObj + "[" + numInCellGroup + "]\n\n");
                    }
                    else
                    {
                        for (Integer cellNum : record.cellNumsToPlot)
                        {
                            if (record.simPlot.isSynapticMechanism())
                            {
                                String neuronVar = this.convertToNeuronVarName(record.simPlot.getValuePlotted());

                                String netConn = SimPlot.getNetConnName(record.simPlot.getValuePlotted());
                                String synType = SimPlot.getSynapseType(record.simPlot.getValuePlotted());

                                /** @todo Make more efficient, as most synObjs for seg ids will be empty... */

                                ArrayList<PostSynapticObject> synObjs = project.generatedNetworkConnections.getSynObjsPresent(netConn, synType, cellNum, segId);

                                logger.logComment("Syn objs for: " + netConn + ", " + synType + ", cellNum: "
                                                  + cellNum + ", segId: " + segId + ": " + synObjs);

                                for (PostSynapticObject synDetail : synObjs)
                                {
                                    String synObjName = this.getSynObjName(synDetail);

                                    String var = synObjName + "." + neuronVar;
                                    String varForFileName = GeneralUtils.replaceAllTokens(synObjName, "[", "_");
                                    varForFileName = GeneralUtils.replaceAllTokens(varForFileName, "]", "_");

                                    String vectorObj = "v_" + varForFileName +"_"+neuronVar;
                                    String fileObj = "f_" + varForFileName +"_"+neuronVar;
                                    vectorObj = GeneralUtils.replaceAllTokens(vectorObj, ".", "_");
                                    fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");

                                    response.append("objref " + vectorObj + "\n");

                                    String prefix = "";
                                    String post = "";

                                    if (simConfig.getMpiConf().isParallelNet())
                                    {
                                        prefix = "    ";
                                        post = "}" + "\n";
                                        response.append("if (isCellOnNode(\""
                                                        + cellGroupName + "\", " + cellNum + ")) {\n");
                                    }

                                    response.append(prefix + "{"+vectorObj + " = new Vector() }\n");

                                    response.append(prefix + "{"+vectorObj + ".record(&"+var+") }\n");

                                    response.append(prefix + "{"+vectorObj + ".resize(" + numStepsTotal + ") }\n");

                                    response.append(post);
                                    response.append("objref " + fileObj + "\n");
                                    response.append("\n");
                                }
                            }
                            else
                            {

                                addHocComment(response,
                                           "Creating vector for segment: " + segToRecord.getSegmentName() +
                                           "(ID: " + segToRecord.getSegmentId() + ") in cell number: " + cellNum);

                                String objName = this.getObjectName(record, cellNum, getHocSegmentName(segToRecord.getSegmentName()));

                                if (isSpikeRecording) objName = objName + "_spike";

                                String vectorObj = "v_" + objName;
                                String fileObj = "f_" + objName;

                                vectorObj = GeneralUtils.replaceAllTokens(vectorObj, ".", "_");
                                fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");
                                    
                                String apCountObj = "apc_" + objName;

                                response.append("objref " + vectorObj + "\n");

                                String prefix = "";
                                String post = "";

                                if (simConfig.getMpiConf().isParallelNet())
                                {
                                    prefix = "    ";
                                    post = "}" + "\n";
                                    response.append("if (isCellOnNode(\""
                                                    + cellGroupName + "\", " + cellNum + ")) {\n");
                                }

                                if (isSpikeRecording) response.append(prefix + "objref " + apCountObj + "\n");

                                response.append(prefix + "{ "+vectorObj + " = new Vector() }\n");

                                if (!isSpikeRecording)
                                {
                                    response.append(prefix + "{ "+vectorObj + ".record(&a_" + cellGroupName + "[" + cellNum +
                                                    "]"
                                                    + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + "." +
                                                    whatToRecord + "(" + lenAlongSection + ")) }\n");

                                    response.append(prefix +  "{ "+vectorObj + ".resize(" + numStepsTotal + ") }\n");
                                }
                                else
                                {
                                    response.append(prefix + "    a_" + cellGroupName + "[" + cellNum + "]"
                                                    + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + " " +
                                                    apCountObj
                                                    + " = new APCount(" + lenAlongSection + ")\n");

                                    if (record.simPlot.getValuePlotted().indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
                                    {
                                        String threshold = record.simPlot.getValuePlotted().substring(record.simPlot.
                                            getValuePlotted().indexOf(SimPlot.
                                                                      PLOTTED_VALUE_SEPARATOR) + 1);

                                        response.append(prefix + "    " + apCountObj + ".thresh = " + threshold + "\n");
                                    }
                                    else
                                    {
                                        response.append(prefix + "    " + apCountObj + ".thresh = " +
                                                        SimPlot.DEFAULT_THRESHOLD + "\n");
                                    }

                                    response.append(prefix + "    " + apCountObj + ".record(" + vectorObj + ")\n");

                                }

                                response.append(post);
                                response.append("objref " + fileObj + "\n");
                                response.append("\n");
                            }
                        }

                    }
                }

            }

        }

        File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

        String dataFileDirName = dirForSims.getAbsolutePath() + System.getProperty("file.separator");


        response.append("strdef simReference\n");
        response.append("simReference = \"" + project.simulationParameters.getReference() + "\"\n\n");

        if (simConfig.getMpiConf().isRemotelyExecuted())
        {
            response.append("strdef targetDir\n");
            response.append("targetDir = \"./\"\n\n");
        }
        else
        {
            String hocFriendlyDirName = getHocFriendlyFilename(dataFileDirName);

            response.append("\n\nstrdef simsDir\n");
            response.append("simsDir = \"" + hocFriendlyDirName + "\"\n\n");

            addHocComment(response, "Note: to change location of the generated simulation files, just change value of targetDir\ne.g. targetDir=\"\" or targetDir=\"aSubDir/\"");

            response.append("strdef targetDir\n");
            response.append("{ sprint(targetDir, \"%s%s/\", simsDir, simReference)}\n\n");
        }

        

        response.append(generateMultiRunPreScript());

        response.append(generateRunMechanism());

        boolean hdf5Format = project.neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC);

        if (recordingSomething)
        {
            response.append("print \"Storing the data...\"\n\n");
            response.append("strdef timeFilename\n");


            if (hdf5Format)
            {
                response.append("nrnpython(\"import numpy\")\n");
                response.append("nrnpython(\"import tables\")\n\n");
                response.append("nrnpython(\"from neuron import *\")\n\n\n");
		String hostInfo = "";
		if (simConfig.getMpiConf().isParallelNet())
		    {
			hostInfo = ".host'+str(int(h.hostid))+'";
		    }
		String h5Filename = project.simulationParameters.getReference()+"_"+hostInfo+"."+SimPlot.H5_EXT;
		String varName = GeneralUtils.replaceAllTokens(h5Filename, ".", "_");
		response.append("nrnpython(\"h5file = tables.openFile(h.targetDir+'"+h5Filename+"', mode = 'w', title = 'Generated via neuroConstruct')\")\n\n");

            }

            String prefix = "";
            String post = "";


            for (PlotSaveDetails record : recordings)
            {
                String cellGroupName = record.simPlot.getCellGroup();

                int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
                String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
                Cell cell = project.cellManager.getCell(cellType);

                boolean isSpikeRecording = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;
		
                if (numInCellGroup > 0)
                {
                    addHocComment(response, record.getDescription(true, false));

                    for (Integer segId : record.segIdsToPlot)
                    {
                        Segment segToRecord = cell.getSegmentWithId(segId);

                        if (record.allCellsInGroup && !record.simPlot.isSynapticMechanism())
                        {


                            if (simConfig.getMpiConf().isParallelNet())
                            {
                                response.append("if  (n_"+cellGroupName+"_local>0) { // No point in saving if no cells here...\n\n ");
                            }

                            addHocComment(response, "Saving vector for segment: "
                                       + segToRecord.getSegmentName() + "(ID: " + segToRecord.getSegmentId() + ")");

                            String objName = this.getObjectName(record, -1, getHocSegmentName(segToRecord.getSegmentName()));

                            if (isSpikeRecording) objName = objName + "_spike";

                            String vectObj = "v_" + objName;

                            if (hdf5Format)
                            {
                               
                                String hostInfo = "";
                                if (simConfig.getMpiConf().isParallelNet())
                                {
                                    hostInfo = ".host'+str(int(h.hostid))+'";
                                }

                                //String h5Filename = cellGroupName+"."+record.simPlot.getSafeVarName()+hostInfo+"."+SimPlot.H5_EXT;
                                //String varName = GeneralUtils.replaceAllTokens(h5Filename, ".", "_");

                                //response.append("nrnpython(\"h5file = tables.openFile(h.targetDir+'"+h5Filename+"', mode = 'a', title = 'Arrays of recordings of "+record.simPlot.getValuePlotted()+" from NEURON')\")\n\n");

                                int mode = 4;

                                response.append("print \"Using HDF5 save mode: "+mode+"\"\n");

                                if (mode == 3)
                                {

                                    response.append("nrnpython(\"group = h5file.createGroup('/', 'voltage', 'Arrays of recordings of "+record.simPlot.getValuePlotted()
                                            +" from cell group: "+cellGroupName+" ')\")\n");

                                    response.append("for i=0, n_"+cellGroupName+"-1 {\n");
                                    response.append("    nrnpython(\"volts = h."+vectObj+"[int(h.i)].to_python()\")\n");
                                    response.append("    nrnpython(\"h5file.createArray(group, 'Cell_'+str(int(h.i)), volts, 'Values of "+record.simPlot.getValuePlotted()
                                            +" from cell group: "+cellGroupName+"')\")\n");
                                    response.append("}\n");

                                }
                                else if (mode == 4)
                                {
                                    // int lengthTable = numStepsTotal;

                                    if (isSpikeRecording)
					{
					    int lengthTable = 0;
                                        lengthTable = (int)getSimDuration() * 3; // i.e. max constant firing freq of 3000Hz...
				    
					response.append("{nrnpython(\"allData = numpy.ones( ("+lengthTable+", h.n_"+cellGroupName+"_local ) , dtype=numpy.float32 )\")}\n");}
				    else
					{response.append("{nrnpython(\"allData = numpy.ones( (h.v_time.size(), h.n_"+cellGroupName+"_local ) , dtype=numpy.float32 )\")}\n");}		
				    response.append("{nrnpython(\"time_data = numpy.array(h.v_time.to_python()) \")}\n");
                                    response.append("{nrnpython(\"allData = allData * -1\")}\n");

                                    response.append("{nrnpython(\"print allData.shape\")}\n\n");
                                    response.append("{nrnpython(\"columnsVsCellNums = {}\")}\n\n");
                                    response.append("{nrnpython(\"columnIndex = 0\")}\n\n");

                                    if (isSpikeRecording)
                                    {
                                        response.append("{nrnpython(\"maxNumSpikes = 0\")}\n\n");
                                    }

                                    response.append("for cellNum=0, n_"+cellGroupName+"-1 {\n");

                                    if (simConfig.getMpiConf().isParallelNet())
                                    {
                                        response.append("  if (isCellOnNode(\""+ cellGroupName + "\", cellNum)) {\n");
                                    }
                                    response.append("    { print \"Adding data for cell number \",cellNum,\" in "+cellGroupName+" on host \", hostid}\n");

                                    if (!isSpikeRecording)
                                    {
                                        response.append("    { nrnpython(\"allData[:,columnIndex] = h."+vectObj+"[int(h.cellNum)].to_python()\")}\n");
                                    }
                                    else
                                    {
                                        response.append("    { nrnpython(\"for i in range(len(h."+vectObj+"[int(h.cellNum)])): allData[i,columnIndex] = h."+vectObj+"[int(h.cellNum)].to_python()[i]\")}\n");
                                        response.append("    {nrnpython(\"maxNumSpikes = max(maxNumSpikes, len(h."+vectObj+"[int(h.cellNum)]))\")}\n");
                                    }


                                    response.append("    {nrnpython(\"columnsVsCellNums[columnIndex] = int(h.cellNum)\")}\n\n");
                                    response.append("    {nrnpython(\"columnIndex += 1\")}\n\n");


                                    if (simConfig.getMpiConf().isParallelNet())
                                    {
                                        response.append("  }\n");
                                    }
                                    response.append("}\n\n");
                                    response.append("{nrnpython(\"print columnsVsCellNums\")}\n\n");


                                    if (isSpikeRecording)
                                    {
                                        response.append("{nrnpython(\"allData = numpy.resize(allData, (maxNumSpikes, h.n_"+cellGroupName+"_local ) )\")}\n");
                                    }

				    // response.append("{nrnpython(\"group = h5file.createGroup('/', '"+cellGroupName+"', '"+cellGroupName+"')\")}\n");

                                    // response.append("{nrnpython(\"group._v_attrs."+Hdf5Constants.NEUROCONSTRUCT_POPULATION+" = '"+cellGroupName+"'\")}\n");
				    response.append("{nrnpython(\"group1 = h5file.createGroup('/', '"+vectObj+"')\")}\n");

                                    response.append("{nrnpython(\"hArray = h5file.createArray(group1, '"+record.simPlot.getSafeVarName()+"', allData, 'Values of "+record.simPlot.getValuePlotted()
                                            +" from cell group: "+cellGroupName+"')\")}\n");


                                    response.append("{nrnpython(\"hArray.setAttr('"+Hdf5Constants.NEUROCONSTRUCT_VARIABLE+"', '"+record.simPlot.getValuePlotted()+"')\")}\n");

				    response.append("{nrnpython(\"hArray = h5file.createArray('/', 'time',time_data , 'Values of time points')\")}\n");
                                    response.append("{nrnpython(\"for columnIndex in columnsVsCellNums.keys(): " +
                                            "hArray.setAttr('"+Hdf5Constants.NEUROCONSTRUCT_COLUMN_PREFIX+"'+str(columnIndex), " +
                                            "'"+Hdf5Constants.NEUROCONSTRUCT_CELL_NUM_PREFIX+"'+ str(columnsVsCellNums[columnIndex]))\")}\n");


                                }
                                else if (mode == 5)
                                {
                                    response.append("nrnpython(\"group = h5file.createGroup('/', '"+cellGroupName+"', '"+cellGroupName+"')\")\n");

                                    response.append("nrnpython(\"group._v_attrs."+Hdf5Constants.NEUROCONSTRUCT_POPULATION+" = '"+cellGroupName+"'\")\n");

                                    response.append("nrnpython(\"atom = tables.Float32Atom()\")\n");
                                    //response.append("nrnpython(\"filters_comp = tables.Filters(complevel=1, complib='zlib', fletcher32=True)\")\n");
                                    response.append("nrnpython(\"filters_comp = None\")\n");

                                    response.append("nrnpython(\"hArray = h5file.createCArray(group, '"+record.simPlot.getSafeVarName()+"', atom, (h.v_time.size(), "
                                            +numInCellGroup+"), title='Values of "+record.simPlot.getValuePlotted()
                                            +" from cell group: "+cellGroupName+"', filters=filters_comp)\")\n");

                                    response.append("nrnpython(\"hArray.setAttr('"+Hdf5Constants.NEUROCONSTRUCT_VARIABLE+"', '"+record.simPlot.getValuePlotted()+"')\")\n");

                                    response.append("for i=0, n_"+cellGroupName+"-1 {\n");
                                    response.append("    nrnpython(\"hArray[:,int(h.i)] = h."+vectObj+"[int(h.i)].to_python()\")\n");
                                    response.append("}\n");

                                }

                                //response.append("{nrnpython(\"print 'Closing file: '+h5file.filename\")}\n");
                                //response.append("{nrnpython(\"h5file.close()\")}\n");
                            }
                            else
                            {

                                String fileObj = "f_" + objName;

                                vectObj = GeneralUtils.replaceAllTokens(vectObj, ".", "_");
                                fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");

                                response.append("for i=0, " + (numInCellGroup - 1) + " {\n");

                                prefix = "";
                                post = "";

                                if (simConfig.getMpiConf().isParallelNet())
                                {
                                    prefix = "    ";
                                    post = "    }" + "\n";
                                    response.append("    if (isCellOnNode(\""
                                                    + cellGroupName + "\", i)) {\n");
                                }


                                response.append(prefix+"    " + fileObj + "[i] = new File()\n");
                                response.append(prefix+"    strdef filename\n");

                                String fileName = SimPlot.getFilename(record, segToRecord, "%d");

                                response.append(prefix+"    {sprint(filename, \"%s" + fileName + "\", targetDir, i)}\n");
                                response.append(prefix+"    " + fileObj + "[i].wopen(filename)\n");
                                response.append(prefix+"    " + vectObj + "[i].printf(" + fileObj + "[i])\n");
                                response.append(prefix+"    " + fileObj + "[i].close()\n");
                                response.append(post);
                                response.append("}\n\n");
                            }
                            if (simConfig.getMpiConf().isParallelNet())
                            {
                                response.append("} // END:  if  (n_"+cellGroupName+"_local>0) \n\n");
                            }
                        }
                        else
                        {
                            if (hdf5Format)
                            {
                                GuiUtils.showWarningMessage(logger, "Cannot currently save in HDF5 format when not recording all of the cells in a cell group", null);
                            }
                            for (Integer cellNum : record.cellNumsToPlot)
                            {
                                if (record.simPlot.isSynapticMechanism())
                                {
                                    String neuronVar = this.convertToNeuronVarName(record.simPlot.getValuePlotted());

                                    String netConn = SimPlot.getNetConnName(record.simPlot.getValuePlotted());
                                    String synType = SimPlot.getSynapseType(record.simPlot.getValuePlotted());

                                    /** @todo Make more efficient, as most synObjs for seg ids will be empty... */

                                    ArrayList<PostSynapticObject> synObjs
                                        = project.generatedNetworkConnections.getSynObjsPresent(netConn,
                                        synType,
                                        cellNum,
                                        segId);

                                    logger.logComment("Syn objs for: " + netConn + ", " + synType + ", cellNum: "
                                                      + cellNum + ", segId: " + segId + ": " + synObjs);

                                    for (PostSynapticObject postSynObj : synObjs)
                                    {
                                        String synObjName = this.getSynObjName(postSynObj);

                                        //String var = synObjName + "." + neuronVar;
                                        
                                        String varForFileName = GeneralUtils.replaceAllTokens(synObjName, "[", "_");
                                        varForFileName = GeneralUtils.replaceAllTokens(varForFileName, "]", "_");

                                        String vectorObj = "v_" + varForFileName + "_" + neuronVar;
                                        String fileObj =   "f_" + varForFileName + "_" + neuronVar;
                                        
                                        vectorObj = GeneralUtils.replaceAllTokens(vectorObj, ".", "_");
                                        fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");

                                        prefix = "";
                                        post = "";

                                        if (simConfig.getMpiConf().isParallelNet())
                                        {
                                            prefix = "    ";
                                            post = "}" + "\n";
                                            response.append("if (isCellOnNode(\""
                                                            + cellGroupName + "\", " + cellNum + ")) {\n");
                                        }

                                        response.append(prefix + fileObj + " = new File()\n");
                                        response.append(prefix + "strdef filename\n");

                                        String fileName = SimPlot.getFilename(record, postSynObj, "%d");

                                        response.append(prefix + "{sprint(filename, \"%s" + fileName + "\", targetDir, " +
                                                        cellNum + ")}\n");

                                        response.append(prefix +"{"+ fileObj + ".wopen(filename)}\n");
                                        response.append(prefix +"{"+ vectorObj + ".printf(" + fileObj + ")}\n");
                                        response.append(prefix +"{"+ fileObj + ".close()}\n\n");
                                        response.append(post);

                                    }
                                }
                                else
                                {

                                    addHocComment(response,
                                               "Saving vector for segment: " + segToRecord.getSegmentName() +
                                               "(ID: " + segToRecord.getSegmentId() + ") in cell number: " +
                                               cellNum);

                                    String objName = this.getObjectName(record, cellNum, getHocSegmentName(segToRecord.getSegmentName()));

                                    if (isSpikeRecording) objName = objName + "_spike";

                                    String vectObj = "v_" + objName;
                                    String fileObj = "f_" + objName;
                                    
                             
                                    vectObj = GeneralUtils.replaceAllTokens(vectObj, ".", "_");
                                    fileObj = GeneralUtils.replaceAllTokens(fileObj, ".", "_");

                                    prefix = "";
                                    post = "";

                                    if (simConfig.getMpiConf().isParallelNet())
                                    {
                                        prefix = "    ";
                                        post = "}" + "\n";
                                        response.append("if (isCellOnNode(\""
                                                        + cellGroupName + "\", " + cellNum + ")) {\n");
                                    }

                                    response.append(prefix + fileObj + " = new File()\n");
                                    response.append(prefix + "strdef filename\n");

                                    String fileName = SimPlot.getFilename(record, segToRecord, "%d");

                                    response.append(prefix +"{"+ "sprint(filename, \"%s" + fileName + "\", targetDir, " +
                                                    cellNum + ")}\n");
                                    response.append(prefix +"{"+ fileObj + ".wopen(filename)}\n");
                                    response.append(prefix +"{"+ vectObj + ".printf(" + fileObj + ")}\n");
                                    response.append(prefix +"{"+ fileObj + ".close()}\n\n");
                                    response.append(post);
                                }
                            }
                        }
                    }
                }

            }
            response.append("\n");


            if (simConfig.getMpiConf().isParallelNet())
            {
                prefix = "    ";
                post = "}" + "\n";
                response.append("if (hostid == 0) {\n");
            }
            
            response.append(prefix+"{ sprint(timeFilename, \"%s%s\", targetDir, \"" + SimulationData.getStandardTimesFilename() + "\")}\n");
            response.append(prefix+"{ f_time.wopen(timeFilename) }\n");
            response.append(prefix+"{ v_time.printf(f_time) }\n");
            response.append(prefix+"{ f_time.close() }\n");

            response.append(post);
            response.append("\n");


            response.append("{savetime = stopsw()}\n\n");

            prefix = "";
            post = "";

            if (simConfig.getMpiConf().isParallelNet())
            {
                prefix = "    ";
                post = "}" + "\n";

                response.append("if (hostid == 0) {\n");
            }

            response.append(prefix+"propsFile = new File()\n");
            response.append(prefix+"strdef propsFilename\n");
            response.append(prefix+"{sprint(propsFilename, \"%s" + SimulationsInfo.simulatorPropsFileName + "\", targetDir)}\n");
            response.append(prefix+"{propsFile.wopen(propsFilename)}\n");
            response.append(prefix+
                "{propsFile.printf(\"#This is a list of properties generated by NEURON during the simulation run\\n\")}\n");



            if (this.savingHostname()) response.append(prefix+"{propsFile.printf(\"Host=%s\\n\", host)}\n");

            response.append(prefix+"{propsFile.printf(\"RealSimulationTime=%g\\n\", realruntime)}\n");
            response.append(prefix+"{propsFile.printf(\"SimulationSaveTime=%g\\n\", savetime)}\n");
            response.append(prefix+"{propsFile.printf(\"SimulationSetupTime=%g\\n\", setuptime)}\n");
            response.append(prefix+"{propsFile.printf(\"NEURONversion=%s\\n\", nrnversion())}\n");

            if (simConfig.getMpiConf().isParallelNet())
            {
                response.append(prefix+"{propsFile.printf(\"NumberHosts=%g\\n\", pnm.pc.nhost)}\n");
            }

            response.append(generateMultiRunPostScript());
            response.append(prefix+"{propsFile.close()}\n");
            response.append(post);
            response.append("\n");

            response.append(prefix+"print \"Data stored in \",savetime, \"secs in directory: \", targetDir\n\n");
        }
	if (hdf5Format)
	    {
		response.append("{nrnpython(\"print 'Closing file: '+h5file.filename\")}\n");
		response.append("{nrnpython(\"h5file.close()\")}\n");
	    }

        return response.toString();

    }
    
    private boolean savingHostname()
    {
        // There have been some problems getting C:/WINDOWS/SYSTEM32/hostname.exe to run on win
        // so temporarily disabling it. It's only needed for parallel running of sims, which is 
        // unlikely on win for the forseeable future.
        
        return generateLinuxBasedScripts();
    }

    private String getObjectName(PlotSaveDetails record, int cellNum, String segName)
    {

        String variable = "_" + this.convertToNeuronVarName(record.simPlot.getValuePlotted());


        if (cellNum<0)
        {
            return SimEnvHelper.getSimulatorFriendlyName(record.simPlot.getCellGroup() + "_seg_" + segName+ variable);
        }

        return SimEnvHelper.getSimulatorFriendlyName(record.simPlot.getCellGroup()+"_cn"+cellNum
                                                     + "_seg_" + segName+ variable);
    }



    public String generateCellGroups() throws NeuronException
    {
        StringBuilder response = new StringBuilder();
        //response.append("\n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        
        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        if (cellGroupNames.isEmpty())
        {
            logger.logComment("There are no cell groups!!");

            addMajorComment(response, "There were no cell groups specified in the project...");
            return response.toString();
        }


        GeneralUtils.timeCheck("Starting gen of cell groups");
        
        String prefix = "";
        if (isRunModePythonBased(genRunMode)) prefix = "h.";


        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii
                              + ", called: " + cellGroupName);

            if (project.generatedCellPositions.getNumberInCellGroup(cellGroupName) == 0)
            {
                logger.logComment("No cells generated in that group. Ignoring...");
            }
            else
            {

                String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);

                addMajorComment(response, "Cell group "
                                       + ii
                                       + ": "
                                       + cellGroupName
                                       + " has cells of type: "
                                       + cellTypeName);

                Cell cell = project.cellManager.getCell(cellTypeName);

                // better create the hoc file for this...

                File dirForNeuronFiles = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());

                logger.logComment("Dir for NeuronFiles: " + dirForNeuronFiles);

                ArrayList<Object> cellMechanisms = new ArrayList<Object>();
                cellMechanisms.addAll(cell.getAllChanMechNames(true));

                //Vector allSyns = cell.getAllAllowedSynapseTypes();

                Iterator allNetConns = project.generatedNetworkConnections.getNamesNetConnsIter();

                while (allNetConns.hasNext())
                {
                    String netConnName = (String) allNetConns.next();

                    if (project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION) > 0)
                    {
                        if (project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName) != null
                            && cellGroupName.equals(project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName)))
                        {
                            Vector synapses = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                            for (int i = 0; i < synapses.size(); i++)
                            {
                                SynapticProperties next = (SynapticProperties) synapses.elementAt(i);
                                cellMechanisms.add(next.getSynapseType());
                            }

                        }
                        if (project.volBasedConnsInfo.getTargetCellGroup(netConnName) != null
                            && cellGroupName.equals(project.volBasedConnsInfo.getTargetCellGroup(netConnName)))
                        {
                            Vector synapses = project.volBasedConnsInfo.getSynapseList(netConnName);
                            for (int i = 0; i < synapses.size(); i++)
                            {
                                SynapticProperties next = (SynapticProperties) synapses.elementAt(i);
                                cellMechanisms.add(next.getSynapseType());
                            }

                        }

                    }
                }

                Vector allStims = project.elecInputInfo.getAllStims();

                logger.logComment("All stims: " + allStims);

                for (int stimNo = 0; stimNo < allStims.size(); stimNo++)
                {
                    StimulationSettings next = (StimulationSettings) allStims.elementAt(stimNo);
                    if (next.getCellGroup().equals(cellGroupName))
                    {
                        if (next instanceof RandomSpikeTrainSettings)
                        {
                            RandomSpikeTrainSettings spikeSettings = (RandomSpikeTrainSettings) next;
                            if (!cellMechanisms.contains(spikeSettings.getSynapseType()))
                            {
                                cellMechanisms.add(spikeSettings.getSynapseType());
                            }
                        }
                        else if (next instanceof RandomSpikeTrainExtSettings)
                        {
                            RandomSpikeTrainExtSettings spikeSettings = (RandomSpikeTrainExtSettings) next;
                            if (!cellMechanisms.contains(spikeSettings.getSynapseType()))
                            {
                                cellMechanisms.add(spikeSettings.getSynapseType());
                            }
                        }
                        else if (next instanceof RandomSpikeTrainVariableSettings)
                        {
                            RandomSpikeTrainVariableSettings spikeSettings = (RandomSpikeTrainVariableSettings) next;
                            if (!cellMechanisms.contains(spikeSettings.getSynapseType()))
                            {
                                cellMechanisms.add(spikeSettings.getSynapseType());
                            }
                        }


                    }
                }

                logger.logComment("------------    All cell mechs for "+cellGroupName+": " + cellMechanisms, false);

                for (int i = 0; i < cellMechanisms.size(); i++)
                {

                    CellMechanism cellMechanism = null;

                    if (cellMechanisms.get(i) instanceof String)
                    {
                        cellMechanism = project.cellMechanismInfo.getCellMechanism( (String) cellMechanisms.get(i));
                    }
                    else if (cellMechanisms.get(i) instanceof ChannelMechanism)
                    {
                        logger.logComment("Is a ChannelMechanism...");
                        ChannelMechanism nextCellMech = (ChannelMechanism) cellMechanisms.get(i);
                        cellMechanism = project.cellMechanismInfo.getCellMechanism(nextCellMech.getName());
                    }

                    if (cellMechanism == null)
                    {
                        throw new NeuronException("Problem generating file for cell mech: " + cellMechanisms.get(i)
                            + "\nPlease ensure there is an implementation for that mechanism in NEURON");
                        
                    }

                    logger.logComment("Looking at cell mechanism: " + cellMechanism.getInstanceName());

                    if (!testForInbuiltModFile(cellMechanism.getInstanceName(), dirForNeuronFiles))
                    {
                        if (!cellMechFilesGenAndIncl.contains(cellMechanism.getInstanceName()))
                        {
                            logger.logComment("Cell mechanism: " + cellMechanism.getInstanceName()+" was not handled already");
                            boolean success = true;
                            boolean regenerate = project.neuronSettings.isForceModFileRegeneration();
                            
                            File sourceFilesDir = new File(ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory()),cellMechanism.getInstanceName());
                            
                            File[] sourceFiles = sourceFilesDir.listFiles();
                            File[] targetFiles = dirForNeuronFiles.listFiles();
                            
                            if (targetFiles.length == 0)
                                regenerate = true;
                            
                            if (!regenerate)
                            {
                                for(File sourceFile: sourceFiles)
                                {
                                    boolean foundTarget = false;
                                    
                                    for(File targetFile: targetFiles)
                                    {
                                        if (targetFile.getName().indexOf(".")>0)
                                        {
                                            String targetPossMechName = targetFile.getName().substring(0, targetFile.getName().indexOf("."));

                                            if (targetPossMechName.equals(cellMechanism.getInstanceName()))
                                            {
                                                foundTarget = true;
                                                boolean sourceNewer = sourceFile.lastModified() > targetFile.lastModified();
                                                logger.logComment("Is "+sourceFile+" newer than "+ targetFile+"? "+sourceNewer );
                                                if (sourceNewer) 
                                                    regenerate = true;
                                            }
                                        }
                                    }
                                    if (!foundTarget)
                                        regenerate = true;
                                }
                            }
                            if (regenerate || !firstRecompileComplete)
                            {
                                firstRecompileComplete = true;
                                logger.logComment("Regenerating " + cellMechanism+" ("+cellMechanism.getClass()+")..." );

                                if (cellMechanism instanceof AbstractedCellMechanism)
                                {
                                    File newMechFile = new File(dirForNeuronFiles,
                                                                cellMechanism.getInstanceName() + ".mod");

                                    success = ( (AbstractedCellMechanism) cellMechanism).createImplementationFile(SimEnvHelper.
                                        NEURON,
                                        UnitConverter.NEURON_UNITS,
                                        newMechFile,
                                        project,
                                        true,
                                        addComments,
                                        project.neuronSettings.isForceCorrectInit(),
                                        simConfig.getMpiConf().isParallelNet());
                                }
                                else if (cellMechanism instanceof XMLCellMechanism)
                                {
                                    XMLCellMechanism xmlMechanism = (XMLCellMechanism) cellMechanism;
                                    File newMechFile = null;

                                    logger.logComment("Cell mechanism: " + cellMechanism.getInstanceName()+" is XML...");

                                    if (xmlMechanism.isNeuroML2())
                                    {
                                        logger.logComment("Cell mechanism: " + cellMechanism.getInstanceName()+" is NeuroML 2...");
                                        File nml2File = xmlMechanism.getXMLFile(project);
                                        String contents = GeneralUtils.readShortFile(nml2File);
                                        contents = Utils.convertNeuroML2ToLems(contents);

                                        logger.logComment("Starting Lems with: "+contents);

                                        try {
                                            StringInclusionReader.addSearchPath(ProjectStructure.getNeuroML2Dir());
                                            LemsProcess sim = new Sim(contents);

                                            sim.readModel();
                                            
                                            Component comp = sim.getLems().getComponent(cellMechanism.getInstanceName());
                                            //comp.getComponentType("MOD_"+cellMechanism.getInstanceName());
                                            logger.logComment("Found component: " + comp, true);

                                            String modFile = NeuronWriter.generateModFile(comp);

                                            String origName = comp.getComponentType().getName();
                                            String newName = "MOD_"+cellMechanism.getInstanceName();

                                            if (cellMechanism.isSynapticMechanism())
                                            {
                                                newName = cellMechanism.getInstanceName();
                                            }
                                            modFile = modFile.replaceAll(origName, newName);

                                            newMechFile = new File(dirForNeuronFiles,
                                                                   cellMechanism.getInstanceName() + ".mod");

                                            GeneralUtils.writeShortFile(newMechFile, modFile);
                                            
                                            logger.logComment("Written to file: " + newMechFile);

                                        } catch (Exception ex) {
                                            throw new NeuronException("Problem generating mod file from NeuroML 2/LEMS description", ex);
                                        }
                                    }
                                    else
                                    {

                                        logger.logComment("Sim map: " + xmlMechanism.getSimMapping(SimEnvHelper.NEURON));

                                        if (xmlMechanism.getSimMapping(SimEnvHelper.NEURON).isRequiresCompilation())
                                        {
                                            newMechFile = new File(dirForNeuronFiles,
                                                                   cellMechanism.getInstanceName() + ".mod");
                                        }
                                        else
                                        {
                                            newMechFile = new File(dirForNeuronFiles,
                                                                   cellMechanism.getInstanceName() + ".hoc");

                                            response.append("{load_file(\"" + cellMechanism.getInstanceName() + ".hoc\")}\n");

                                        }
                                        success = xmlMechanism.createImplementationFile(SimEnvHelper.NEURON,
                                            UnitConverter.NEURON_UNITS,
                                            newMechFile,
                                            project,
                                            xmlMechanism.getSimMapping(SimEnvHelper.NEURON).isRequiresCompilation(),
                                            addComments,
                                            project.neuronSettings.isForceCorrectInit(),
                                            simConfig.getMpiConf().isParallelNet());

                                    }
                                }
                            }

                            if (!success)
                            {
                                throw new NeuronException("Problem generating file for cell mechanism: " + cellMechanisms.get(i)
                                                          +"\nPlease ensure there is an implementation for that mechanism in NEURON");

                            }

                            cellMechFilesGenAndIncl.add(cellMechanism.getInstanceName());
                        }
                    }

                }

                logger.logComment("------    needsGrowthFunctionality: " + needsGrowthFunctionality(cellGroupName));
                
                boolean addSegIdFunctions = false;
                if (isRunModePythonBased(genRunMode)) addSegIdFunctions = true;

                NeuronTemplateGenerator cellTemplateGen
                    = new NeuronTemplateGenerator(project,
                                                  cell,
                                                  dirForNeuronFiles,
                                                  needsGrowthFunctionality(cellGroupName),
                                                  addSegIdFunctions);

                String filenameToBeGenerated = cellTemplateGen.getHocFilename();

                logger.logComment("Will need a cell template file called: " +
                                  filenameToBeGenerated);

                if (cellTemplatesGenAndIncluded.contains(filenameToBeGenerated))
                {
                    addComment(response, "Cell template file: "+cellTemplateGen.getHocShortFilename()
                            +" for cell group "+cellGroupName+" has already been included");
                }
                else
                {
                    logger.logComment("Generating it...");
                    try
                    {
                        cellTemplateGen.generateFile();

                        cellTemplatesGenAndIncluded.add(filenameToBeGenerated);
                        
                        logger.logComment("Adding include for the file to the main hoc file...");

                        StringBuilder fileNameBuffer = new StringBuilder(filenameToBeGenerated);

                        for (int j = 0; j < fileNameBuffer.length(); j++)
                        {
                            char c = fileNameBuffer.charAt(j);
                            if (c == '\\')
                                fileNameBuffer.replace(j, j + 1, "/");
                        }

                        addComment(response, "Adding cell template file: "+cellTemplateGen.getHocShortFilename()
                                +" for cell group "+cellGroupName+"");
                        if (prefix.length()==0)
                        {
                            response.append("{ load_file(\"" + cellTemplateGen.hocFile.getName() + "\") }\n");
                        }
                        else
                        {
                            response.append(""+prefix+"load_file(\"" + cellTemplateGen.hocFile.getName() + "\")\n");
                        }
                    }
                    catch (NeuronException ex)
                    {
                        logger.logError("Problem generating one of the template files...", ex);
                        throw ex;
                    }
                }

                
                
                // now we've got the includes
                
                if (!isRunModePythonBased(genRunMode))
                {
                    String currentRegionName = project.cellGroupsInfo.getRegionName(cellGroupName);
    
                    ArrayList cellGroupPositions = project.generatedCellPositions.getPositionRecords(cellGroupName);
    
                    addHocComment(response, "Adding " + cellGroupPositions.size()
                                      + " cells of type " + cellTypeName
                                      + " in region " + currentRegionName);
    
                    String nameOfNumberOfTheseCells = "n_" + cellGroupName;
                    String nameOfArrayOfTheseCells = "a_" + cellGroupName;
    
                    if (cellGroupPositions.size() > 0)
                    {
                        response.append("{"+nameOfNumberOfTheseCells + " = " + cellGroupPositions.size() + "}\n\n");
                        response.append("{"+nameOfNumberOfTheseCells + "_local = 0 } // actual number created on this host\n\n");
    
                        response.append("objectvar " + nameOfArrayOfTheseCells + "[" + nameOfNumberOfTheseCells + "]" +
                                        "\n\n");
    
                        if (!simConfig.getMpiConf().isParallelNet())
                        {
                            response.append("proc addCell_" + cellGroupName + "() {\n");
    
                            response.append("    strdef reference\n");
                            response.append("    sprint(reference, \"" + cellGroupName + "_%d\", $1)\n");
    
                            String desc = GeneralUtils.replaceAllTokens(cell.getCellDescription(), "\n", " ");
                            response.append("    " + nameOfArrayOfTheseCells + "[$1] = new " + cellTypeName +
                                            "(reference, \""
                                            + cellTypeName + "\", \"" + desc + "\")" + "\n");
    
                            response.append("    allCells.append(" + nameOfArrayOfTheseCells + "[$1])\n");
    
                            response.append("}" + "\n\n");
    
                            response.append("for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");
    
                            response.append("    addCell_" + cellGroupName + "(i)" + "\n");
                            response.append("    "+nameOfNumberOfTheseCells + "_local = "+nameOfNumberOfTheseCells + "_local +1 \n\n");
    
                            response.append("}" + "\n\n");
                        }
                        else
                        {
    
                            response.append("for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");
    
                            //response.append("addCell_" + cellGroupName + "(i)" + "\n\n");
    
                            response.append("    if(isCellOnNode(\""+cellGroupName+"\", i)) {\n");
    
                            response.append("        strdef reference, type, description\n");
                            response.append("        sprint(reference, \"" + cellGroupName + "_%d\", i)\n");
                            response.append("        sprint(type, \"" + cellTypeName + "\")\n");
                            response.append("        sprint(description, \"" + GeneralUtils.replaceAllTokens(cell.getCellDescription(), "\n", " ") + "\")\n");
    
                            //response.append("        strdef command\n");
                            //response.append("        sprint(command, \"new " + cellTypeName + "(reference, type, description)\")\n");
    
                            if (addComments) response.append("        print \"Going to create cell: \", reference, \" on host \", host, \", id: \", hostid\n");
    
                            //response.append("    pnm.create_cell(i, command)\n");
                            response.append( "        a_"+cellGroupName+"[i] = new "+cellTypeName+"(reference, type, description)\n");
    
    
                            response.append("        pnm.register_cell(getCellGlobalId(\""+cellGroupName+"\", i), a_"+cellGroupName+"[i])\n");

                            response.append("        allCells.append(" + nameOfArrayOfTheseCells + "[i])\n");
                            response.append("        "+nameOfNumberOfTheseCells + "_local = "+nameOfNumberOfTheseCells + "_local + 1 \n\n");
    
                            response.append("    }\n");
    
                            response.append("}" + "\n\n");
                        }
                    }
    
    
    
                    Region regionInfo = project.regionsInfo.getRegionObject(currentRegionName);
                    CellPackingAdapter packer = project.cellGroupsInfo.getCellPackingAdapter(cellGroupName);
    
                    //     float yDisplacementOfThisRegion = project.regionsInfo.getYDisplacementOfRegion(currentRegionName);
    
                    addHocComment(response, "Placing these cells in a region described by: " + regionInfo);
                    addHocComment(response, "Packing has been generated by: " + packer.toString());
    
                    for (int j = 0; j < cellGroupPositions.size(); j++)
                    {
                        PositionRecord posRecord
                            = (PositionRecord) cellGroupPositions.get(j);
    
                        logger.logComment("Moving cell number: " + j + " into place");
    
                        if (j != posRecord.cellNumber)
                        {
                            // not really a problem, but best to highlight it...
                            logger.logComment("-------------------------                Position number " + j +
                                              " doesn't match cell number: " + posRecord);
                            // continue...
                        }
    
                        String parallelCheck = "";
                        if (simConfig.getMpiConf().isParallelNet())
                            parallelCheck = "if (isCellOnNode(\""+cellGroupName+"\", "+posRecord.cellNumber+")) ";
    
                        response.append(parallelCheck+ "{"+nameOfArrayOfTheseCells + "[" + posRecord.cellNumber + "].position("
                                        + posRecord.x_pos + "," + posRecord.y_pos + "," + posRecord.z_pos + ")}\n");
    
                    }
                }


                logger.logComment("***  Finished looking at cell group number " + ii + ", called: " + cellGroupName);

            }

            response.append("\n");

        }
        
        boolean genAllModFiles = project.neuronSettings.isGenAllModFiles(); 
        
        
        if (genAllModFiles)
        {
            
                File dirForNeuronFiles = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());
        	ArrayList<String> allAvailableMods = project.cellMechanismInfo.getAllCellMechanismNames();
        	
        	for(String cellMech:  allAvailableMods)
        	{
                    if (!testForInbuiltModFile(cellMech, dirForNeuronFiles))
                    {
                        if (!cellMechFilesGenAndIncl.contains(cellMech))
                        {
                            CellMechanism cellMechanism = project.cellMechanismInfo.getCellMechanism(cellMech);

                            boolean success = false;
                            if (cellMechanism instanceof AbstractedCellMechanism)
                            {
                                File newMechFile = new File(dirForNeuronFiles,
                                                            cellMechanism.getInstanceName() + ".mod");

                                success = ( (AbstractedCellMechanism) cellMechanism).createImplementationFile(SimEnvHelper.
                                    NEURON,
                                    UnitConverter.NEURON_UNITS,
                                    newMechFile,
                                    project,
                                    true,
                                    addComments,
                                    project.neuronSettings.isForceCorrectInit(),
                                    simConfig.getMpiConf().isParallelNet());
                            }
                            else if (cellMechanism instanceof ChannelMLCellMechanism)
                            {
                                ChannelMLCellMechanism cmlMechanism = (ChannelMLCellMechanism) cellMechanism;
                                File newMechFile = null;

                                logger.logComment("Sim map: " + cmlMechanism.getSimMapping(SimEnvHelper.NEURON));

                                if (cmlMechanism.getSimMapping(SimEnvHelper.NEURON).isRequiresCompilation())
                                {
                                    newMechFile = new File(dirForNeuronFiles,
                                                           cellMechanism.getInstanceName() + ".mod");
                                }
                                else
                                {
                                    newMechFile = new File(dirForNeuronFiles,
                                                           cellMechanism.getInstanceName() + ".hoc");

                                    response.append("{load_file(\"" + cellMechanism.getInstanceName() + ".hoc\")}\n");

                                }
                                success = cmlMechanism.createImplementationFile(SimEnvHelper.
                                    NEURON,
                                    UnitConverter.NEURON_UNITS,
                                    newMechFile,
                                    project,
                                    cmlMechanism.getSimMapping(SimEnvHelper.NEURON).isRequiresCompilation(),
                                    addComments,
                                    project.neuronSettings.isForceCorrectInit(),
                                    simConfig.getMpiConf().isParallelNet());
                            }

                            if (!success)
                            {
                                throw new NeuronException("Problem generating file for cell mechanism: "
                                                          + cellMechanism
                                                          +
                                                          "\nPlease ensure there is an implementation for that mechanism in NEURON");

                            }

                            cellMechFilesGenAndIncl.add(cellMechanism.getInstanceName());
                        }
                    }
        	}
        }

        GeneralUtils.timeCheck("Finished gen of cell groups");

        return response.toString();

    }
    
    private boolean testForInbuiltModFile(String mechName, File targetDir)
    {
        ArrayList<String> inbuiltMechs = new ArrayList<String>();
        
        //@todo There are more to add...
        
        inbuiltMechs.add("pas");
        inbuiltMechs.add("hh");
        inbuiltMechs.add("extracellular");
        inbuiltMechs.add("fastpas");
        
        if (inbuiltMechs.contains(mechName))
        {
            File warnFile = new File(targetDir, mechName+".mod.WARNING");
            
            if (!warnFile.exists())
            {
                try
                {
                    FileWriter fwReadme = new FileWriter(warnFile);
                    fwReadme.write("Warning: there is a mechanism "+mechName+" in the neuroConstruct project, which uses the same name as\n"
                        +"a built in mechanism in NEURON. This is possibly due to export of a Level 2 morphology from NEURON.\n"+
                        "Using the NEURON mechanism instead of that from neuroConstruct!! Hopefully the functionality is the same!!");
                    fwReadme.close();
                }
                catch (IOException ex)
                {
                    logger.logError("Exception creating "+warnFile+"...");
                }
            }
            return true;
        }
        return false;
    }

    private boolean needsGrowthFunctionality(String cellGroup)
    {
        Vector allNetConnNames = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();

        for (int i = 0; i < allNetConnNames.size(); i++)
        {
            String netConnName = (String) allNetConnNames.elementAt(i);
            logger.logComment("Checking: " + netConnName + " for growth func for cell group : " + cellGroup);
            if (project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName).equals(cellGroup))
            {
                logger.logComment("The cellGroup is source");
                if (project.morphNetworkConnectionsInfo.getGrowMode(netConnName).getType()
                    != GrowMode.GROW_MODE_JUMP)
                    return true;
            }
            if (project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName).equals(cellGroup))
            {
                logger.logComment("The cellGroup is target");
                if (project.morphNetworkConnectionsInfo.getGrowMode(netConnName).getType()
                    != GrowMode.GROW_MODE_JUMP)
                    return true;
            }
        }
        return false;

    }



    private String getSynObjName(PostSynapticObject synDetails)
    {
        String objectVarName = "syn_" + synDetails.getNetConnName()
            + "_" + synDetails.getSynapseType()
            + "[" + synDetails.getSynapseIndex()+"]";

        return objectVarName;

    }
    private String getSynObjArrayName(PostSynapticObject synDetails)
    {
        String objectVarName = "syn_" + synDetails.getNetConnName()
            + "_" + synDetails.getSynapseType();

        return objectVarName;

    }

    private void generateNetworkConnections(FileWriter hocWriter) throws IOException
    {

        logger.logComment("Starting generation of the net conns");
        
        //int totNetConns = project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION);

        StringBuilder responsePre = new StringBuilder();

        responsePre.append("\n");

        addMajorHocComment(responsePre, "Adding Network Connections");
        
        if (simConfig.getMpiConf().isParallelNet())
        {
            responsePre.append("objectvar allCurrentNetConns\n");
            responsePre.append("allCurrentNetConns = new List()\n\n");
        }
        
        if (addComments && simConfig.getMpiConf().isParallelNet())
        {
            //response.append("for i=0, (20000000 *hostid)  { rr = i*i }\n"); 
            responsePre.append("print \" -----------------------   Starting generation of net conns on host: \", hostid\n\n"); 

        }
        hocWriter.write(responsePre.toString());
        
        Hashtable<String, Integer> preSectionsVsGids = new Hashtable<String, Integer>();
        
        boolean containsGapJunctions = false;

        Iterator allNetConnNames = project.generatedNetworkConnections.getNamesNetConnsIter();

        if (!allNetConnNames.hasNext())
        {
            logger.logComment("There are no synaptic connections");
            return;
        }

        GeneralUtils.timeCheck("Starting gen of syn conns");


        // refresh iterator...
        allNetConnNames = project.generatedNetworkConnections.getNamesNetConnsIter();

        int globalPreSynId = 100000000; // Will cause errors when there are more syns than this...
        
        ArrayList<String> synObjvarArraysCreated = new ArrayList<String>();
        
        // Adding specific network connections...
        
        while (allNetConnNames.hasNext())
        {
            String netConnName = (String) allNetConnNames.next();
            
            boolean warnedReZeroDelayParallel = false;

            GeneralUtils.timeCheck("Generating net conn: "+ netConnName);

            String sourceCellGroup = null;
            String targetCellGroup = null;
            Vector<SynapticProperties> synPropList = null;

            if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
            {
                sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                synPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
            }

            else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
            {
                sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                synPropList = project.volBasedConnsInfo.getSynapseList(netConnName);
            }
           
            String targetCellName = project.cellGroupsInfo.getCellType(targetCellGroup);
            Cell targetCell = project.cellManager.getCell(targetCellName);

            String sourceCellName = project.cellGroupsInfo.getCellType(sourceCellGroup);
            Cell sourceCell = project.cellManager.getCell(sourceCellName);

            HashMap<Integer, SegmentLocation> substituteConnPoints
                = new HashMap<Integer, SegmentLocation> (); // used for storing alternate connection locations
            // when ApPropSpeed on sections..

            if (sourceCell.getApPropSpeedsVsGroups().size() > 0) // are there any?
            {
                ArrayList<Section> allSecs = sourceCell.getAllSections();

                for (int j = 0; j < allSecs.size(); j++)
                {
                    Section nextSec = allSecs.get(j);

                    if (sourceCell.getApPropSpeedForSection(nextSec) != null)
                    {
                        LinkedList<Segment> segs = sourceCell.getAllSegmentsInSection(nextSec);

                        SegmentLocation synconloc = CellTopologyHelper.getConnLocOnExpModParent(sourceCell,
                            segs.getFirst());
                        //Segment subsSeg = sourceCell.getSegmentWithId(synconloc.segmentId);

                        for (int k = 0; k < segs.size(); k++)
                        {
                            int id = segs.get(k).getSegmentId();
                            substituteConnPoints.put(new Integer(id), synconloc);
                        }
                    }
                }
            }
            
            boolean isGapJunction = false;
            
            if (synPropList.size()==1)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(synPropList.get(0).getSynapseType());
                if (cm.getMechanismType().equals(CellMechanism.GAP_JUNCTION))
                    isGapJunction = true;
            }

            ArrayList<SingleSynapticConnection> allSynapses = project.generatedNetworkConnections.getSynapticConnections(netConnName);
            
            hocWriter.write("\n");
            if (addComments())
                hocWriter.write( "// Adding NetConn: "
                              + netConnName
                              + " from: "
                              + sourceCellGroup
                              + " to: "
                              + targetCellGroup+" with "+allSynapses.size()+" connections\n// each with syn(s): "+synPropList);

            hocWriter.write("\n");
            
            if (simConfig.getMpiConf().isParallelNet())
            {
                //////////response.append("allCurrentNetConns.remove_all()   // Empty list \n");
            }


            GeneralUtils.timeCheck("Have all info for net conn: "+ netConnName);

            for (int singleConnIndex = 0; singleConnIndex < allSynapses.size(); singleConnIndex++)
            {
                
                StringBuilder responseConn = new StringBuilder();
                
                GeneratedNetworkConnections.SingleSynapticConnection synConn = allSynapses.get(singleConnIndex);
                //System.out.println("synConn: "+synConn);

                for (int synPropIndex = 0; synPropIndex < synPropList.size(); synPropIndex++)
                {
                    SynapticProperties synProps = synPropList.elementAt(synPropIndex);

                    PostSynapticObject synObj = new PostSynapticObject(netConnName,
                                                         synProps.getSynapseType(),
                                                         synConn.targetEndPoint.cellNumber,
                                                         synConn.targetEndPoint.location.getSegmentId(),
                                                         singleConnIndex);


                    //String objectVarName = getSynObjName(synObj);
                    String objectVarArrayName = getSynObjArrayName(synObj);
                    
                    if (isGapJunction) objectVarArrayName = "elec"+objectVarArrayName;

                    /** @todo Remove the need for this... Revise how inbuilt synapses are stored/checked.. */

                    String synapseType = null;
                    if (synProps.getSynapseType().indexOf(" ") > 0)
                    {
                        synapseType = synProps.getSynapseType().substring(0, synProps.getSynapseType().indexOf(" "));
                    }
                    else
                    {
                        synapseType = synProps.getSynapseType();
                    }

                    double threshold = synProps.getThreshold();

                    //NumberGenerator delayGenerator = synProps.delayGenerator;
                    //NumberGenerator weightsGenerator = synProps.weightsGenerator;

                    Segment targetSegment
                        = targetCell.getSegmentWithId(synConn.targetEndPoint.location.getSegmentId());

                    logger.logComment("Target segment: " + targetSegment);

                    float fractTgtSection
                        = CellTopologyHelper.getFractionAlongSection(targetCell,
                                                                     targetSegment,
                                                                     synConn.targetEndPoint.location.getFractAlong());

                    Segment sourceSegment = null;
                    float fractionAlongSrcSeg = -1;
                    
                    int origId = synConn.sourceEndPoint.location.getSegmentId();

                    float apSegmentPropDelay = 0;

                    if (substituteConnPoints.size() == 0 || // there is no ApPropSpeed on cell
                        !substituteConnPoints.containsKey(origId)) // none on this segment
                    {
                        sourceSegment = sourceCell.getSegmentWithId(origId);
                        fractionAlongSrcSeg = synConn.sourceEndPoint.location.getFractAlong();
                        ///if (sourceSegment.isSpherical()) fractionAlongSrcSeg = 1; // as it doesn't really matter
                    }
                    else
                    {
                        Segment realSource = sourceCell.getSegmentWithId(origId);

                        SegmentLocation subsSynConLoc = substituteConnPoints.get(new Integer(origId));

                        sourceSegment = sourceCell.getSegmentWithId(subsSynConLoc.getSegmentId());
                        fractionAlongSrcSeg = subsSynConLoc.getFractAlong();

                        apSegmentPropDelay = CellTopologyHelper.getTimeToFirstExpModParent(sourceCell,
                                                                                    realSource,
                                                                                    synConn.sourceEndPoint.location.getFractAlong());

                        addHocComment(responseConn,
                                   "Instead of point " + synConn.sourceEndPoint.location.getFractAlong() + " along seg: "
                                   + realSource.toShortString() + " connecting to point " +
                                   fractionAlongSrcSeg + " along seg: "
                                   + sourceSegment.toShortString() + "");

                    }

                    logger.logComment("source segment: " + sourceSegment);

                    float fractSrcSection
                        = CellTopologyHelper.getFractionAlongSection(sourceCell,
                                                                     sourceSegment,
                                                                     fractionAlongSrcSeg);
                    

                    logger.logComment("fractAlongSourceSection: " + fractSrcSection);

                    float synInternalDelay = -1;
                    float weight = -1;
                    
                    if (synConn.props==null || synConn.props.isEmpty())
                    {
                        logger.logComment("Generating weight from: "+ synProps.getWeightsGenerator());
                        
                        synInternalDelay = synProps.getDelayGenerator().getNominalNumber();
                        weight = synProps.getWeightsGenerator().getNominalNumber();
                    }
                    else
                    {
                        logger.logComment("Generating weight from: "+ synConn.props);
                        
                        boolean found = false;
                        
                        for (ConnSpecificProps prop:synConn.props)
                        {
                            if (prop.synapseType.equals(synProps.getSynapseType()))
                            {
                                found = true;
                                synInternalDelay = prop.internalDelay;
                                weight = prop.weight;
                            }
                        }
                        if (!found)
                        {
                            logger.logComment("Generating weight from: "+ synProps.getWeightsGenerator());

                            synInternalDelay = synProps.getDelayGenerator().getNominalNumber();
                            weight = synProps.getWeightsGenerator().getNominalNumber();
                        }
                    }

                    String tgtCellName = "a_" + targetCellGroup + "[" + synConn.targetEndPoint.cellNumber + "]";
                    String tgtSecName = getHocSectionName(targetSegment.getSection().getSectionName());
                    String tgtSecNameFull = tgtCellName+ "." + tgtSecName;

                    String srcCellName = "a_" + sourceCellGroup + "[" + synConn.sourceEndPoint.cellNumber + "]";
                    String srcSecName = getHocSectionName(sourceSegment.getSection().getSectionName());
                    String srcSecNameFull = srcCellName + "."+ srcSecName;

                    float apSpaceDelay = synConn.apPropDelay;

                    float totalDelay = synInternalDelay + apSegmentPropDelay + apSpaceDelay;
                    
                    if (totalDelay==0 && simConfig.getMpiConf().isParallelNet() && !warnedReZeroDelayParallel && !isGapJunction)
                    {
                        GuiUtils.showWarningMessage(logger, "Warning, zero delay for at least one synaptic connection for synapse type: " +
                                synProps.getSynapseType()+" on "+netConnName+"\nAs this is a parallel simulation, this will probably throw errors when NEURON is run.", null);
                        warnedReZeroDelayParallel = true;
                    }
                    
                    addHocComment(responseConn, "Syn conn (type: "+synProps.getSynapseType()+") "
                            +"from "+srcSecName+" on src cell "+synConn.sourceEndPoint.cellNumber
                            +" to "+tgtSecName+" on tgt cell "+synConn.targetEndPoint.cellNumber
                            +"\nFraction along src section: "
                            + fractSrcSection+", weight: " + weight
                            + "\nDelay due to AP prop along segs: " + apSegmentPropDelay
                            + ", delay due to AP jump pre -> post 3D location "+ apSpaceDelay
                            + "\nInternal synapse delay (from Synaptic Props): " + synInternalDelay
                            +", TOTAL delay: "+totalDelay);



                    
                    if (!isGapJunction)
                    {
                        
                        String objVarCmd = "objectvar " + objectVarArrayName+"["+allSynapses.size()+"]\n\n";
                        String objVarTempCmd = "objectvar " + objectVarArrayName+"_temp["+allSynapses.size()+"]\n\n";
                        
                        if (!synObjvarArraysCreated.contains(objVarCmd))
                        {
                            responseConn.append(objVarCmd);
                            synObjvarArraysCreated.add(objVarCmd);
                        }
                        String objectVarName = objectVarArrayName + "["+singleConnIndex+"]";
                        String objectVarTempName = null;
                        
                        if(simConfig.getMpiConf().isParallelNet())
                        {
                            if (!synObjvarArraysCreated.contains(objVarTempCmd))
                            {
                                responseConn.append(objVarTempCmd);
                                synObjvarArraysCreated.add(objVarTempCmd);
                            }
                            objectVarTempName = objectVarArrayName + "_temp["+singleConnIndex+"]";
                        }
                        
                        
                        if (!simConfig.getMpiConf().isParallelNet())
                        {
                            // put synaptic start point on source axon
                            responseConn.append("{"+tgtSecNameFull + " " + objectVarName
                                            + " = new " + synapseType + "(" + fractTgtSection + ")}\n");

                            responseConn.append("{"+srcSecNameFull + " "  + tgtCellName
                                            + ".synlist.append(new NetCon(&v("+ fractSrcSection + "), "
                                            + objectVarName+", "+threshold+", "+totalDelay+", "+weight + "))"+"}\n\n");

                            CellMechanism cm = project.cellMechanismInfo.getCellMechanism(synProps.getSynapseType());

                            if (cm instanceof AbstractedCellMechanism)
                            {
                                AbstractedCellMechanism acm = (AbstractedCellMechanism)cm;

                                try
                                {
                                    if (acm.getParameter("RequiresXYZ") == 1)
                                    {
                                        Point3f synRelToCell = CellTopologyHelper.convertSegmentDisplacement(targetCell,
                                            targetSegment.getSegmentId(),
                                            synConn.targetEndPoint.location.getFractAlong());

                                        Point3f posAbsSyn  = project.generatedCellPositions.getOneCellPosition(targetCellGroup,
                                            synConn.targetEndPoint.cellNumber);

                                        posAbsSyn.add(synRelToCell);

                                        addHocComment(responseConn, "Synapse location on cell: " + synRelToCell);
                                        addHocComment(responseConn, "Synapse absolute location: " + posAbsSyn);

                                        responseConn.append(objectVarName+".x = "+posAbsSyn.x+"\n");
                                        responseConn.append(objectVarName+".y = "+posAbsSyn.y+"\n");
                                        responseConn.append(objectVarName+".z = "+posAbsSyn.z+"\n\n");
                                    }
                                }
                                catch (CellMechanismException ex)
                                {
                                    logger.logComment("No xyz parameter: "+ex);
                                }
                            }
                            if (cm instanceof ChannelMLCellMechanism)
                            {
                                ChannelMLCellMechanism ccm = (ChannelMLCellMechanism)cm;
                                try
                                {
                                    String stdpEl = ccm.getXMLDoc().getValueByXPath(ChannelMLConstants.getSTDPSynapseXPath());
                                    if(stdpEl!=null)
                                    {
                                        addHocComment(responseConn, "This is an STDP based synaptic contact!");
                                        String postNetConnObj = GeneralUtils.replaceAllTokens(objectVarName, "[", "_");
                                        postNetConnObj = GeneralUtils.replaceAllTokens(postNetConnObj, "]", "_");
                                        postNetConnObj = "postNetCon_"+postNetConnObj;

                                        responseConn.append("\nobjectvar "+postNetConnObj+"\n");
                                        responseConn.append(tgtSecNameFull+" { "+postNetConnObj+" = new NetCon(&v(0.5), "
                                            + objectVarName+", "+threshold+", 0, -1)"+"}\n");
                                    }
                                }
                                catch (XMLMechanismException e)
                                {

                                }
                            }
                        }
                        else   // not if (!simConfig.getMpiConf().isParallel())
                        {
                            responseConn.append("localSynapseId = -2\n");
                            responseConn.append("globalPreSynId = "+globalPreSynId+" // provisional gid for NetCon\n");

                            /////String netConRef = "NetCon_"+globalPreSynId;

                            ////response.append("objectvar " + netConRef + "\n\n");
                            ////String ncTemp = netConRef + "_temp";
                            ////response.append("objectvar " + ncTemp+"\n\n");

                            String targetExists = "isCellOnNode(\""+ targetCellGroup + "\", "+ synConn.targetEndPoint.cellNumber + ")";
                            String sourceExists = "isCellOnNode(\""+ sourceCellGroup + "\", " + synConn.sourceEndPoint.cellNumber + ")";

                            
                            
                                    // Post synaptic setup
                            
                            
                            if (addComments) responseConn.append("print \"> Doing post syn setup for "+objectVarName+", "+srcCellName+" -> "+tgtCellName+"\"\n\n");

                            responseConn.append("if ("+targetExists+") {\n");
                            if (addComments) responseConn.append("    print \"Target IS on host: \", hostid\n\n");

                            responseConn.append("    "+tgtSecNameFull+" " + objectVarName
                                            + " = new " + synapseType + "(" + fractTgtSection + ")\n");


                            responseConn.append("    "+tgtCellName+".synlist.append( "+ objectVarName + " )\n");

                            responseConn.append("    localSynapseId = "+tgtCellName+".synlist.count()-1\n");

                            if (addComments) responseConn.append("    print \"Created: \", "+objectVarName+",\" on "+tgtCellName+" on host \", hostid\n\n");

                            responseConn.append("} else {\n");
                            if (addComments) responseConn.append("    print \"Target NOT on host: \", hostid\n\n");
                            responseConn.append("}\n");


                            
                            
                            
                                    // Pre synaptic setup
                            
                            if (addComments) responseConn.append("\nprint \"Doing pre syn setup for "+objectVarName+"\"\n\n");
                            
                            responseConn.append("if ("+sourceExists+") {\n");
                            
                            if (addComments) responseConn.append("    print \"Source IS on host: \", hostid\n\n");
                            
                            int gidOfSource = globalPreSynId;

                            if (!preSectionsVsGids.containsKey(srcSecNameFull))
                            {
                                
                                if (addComments) responseConn.append("    print \"No NetCon exists yet for section: "+srcSecNameFull+" on host \", hostid\n\n");
                                
                                responseConn.append("    {pnm.pc.set_gid2node(globalPreSynId, hostid)}\n");

                                responseConn.append("    "+srcSecNameFull+" "+objectVarName+" = new NetCon(&v("+synConn.sourceEndPoint.location.getFractAlong()+"), nil)\n");
                                
                                responseConn.append("    "+objectVarName+".delay = "+(synInternalDelay + apSegmentPropDelay + apSpaceDelay)+"\n");
                                responseConn.append("    "+objectVarName+".weight = "+weight+" // not really needed on the pre side\n");
                                responseConn.append("    "+objectVarName+".threshold = "+synProps.getThreshold()+"\n\n");

                                responseConn.append("    pnm.pc.cell(globalPreSynId, "+objectVarName+")\n");

                                responseConn.append("    allCurrentNetConns.append("+objectVarName+")\n");

                                if (addComments) responseConn.append("    print \"Created: \", "+objectVarName+",\" on "+srcSecNameFull+" on host \", hostid\n\n");

                                preSectionsVsGids.put(srcSecNameFull, globalPreSynId);
                            }
                            else
                            {
                                gidOfSource = preSectionsVsGids.get(srcSecNameFull);
                                if (addComments) responseConn.append("    print \"NetCon for "+srcSecNameFull+" on host \", hostid, \" was already created with gid "
                                        +gidOfSource+"\"\n\n");
                            }

                            responseConn.append("} else {\n");
                            if (addComments) responseConn.append("    print \"Source NOT on host: \", hostid\n\n");
                            responseConn.append("}\n");
                            

                            ////////////if (addComments) response.append("netConInfoParallel("+objectVarName+")\n\n");
                            
                            
                                    // Connecting post to pre
                            

                            if (addComments) responseConn.append("\nprint \"Doing pre to post attach for "+objectVarName+"\"\n\n");

                            responseConn.append("if ("+targetExists+") {\n");
                            if (addComments) responseConn.append("    print \"Target IS on host: \", hostid, \" using gid: \", globalPreSynId\n\n");

                            responseConn.append("    gidOfSource = "+gidOfSource+"\n\n");

                            responseConn.append("    "+objectVarTempName+" = pnm.pc.gid_connect(gidOfSource, a_" + targetCellGroup
                                            + "[" + synConn.targetEndPoint.cellNumber + "]"
                                            + ".synlist.object(localSynapseId))\n");

                            responseConn.append("    "+objectVarTempName+".delay = "+(synInternalDelay + apSegmentPropDelay + apSpaceDelay)+"\n");
                            responseConn.append("    "+objectVarTempName+".weight = "+weight+"\n");
                            responseConn.append("    "+objectVarTempName+".threshold = "+synProps.getThreshold()+"\n\n");

                            ////////////if (addComments) response.append("    netConInfoParallel("+objectVarTempName+")\n\n");
                            responseConn.append("} else {\n");
                            if (addComments) responseConn.append("    print \"Target NOT on host: \", hostid\n\n");
                            responseConn.append("}\n");

                            if (addComments) responseConn.append("print \"< Done setup for "+objectVarName+"\"\n\n");


                        }
                        globalPreSynId++;
                        
                    }    // end of   if (!isGapJunction)                  
                    else     
                    {
                        containsGapJunctions = true;
                        
                        String arrayA = objectVarArrayName + "_A";
                        String arrayB = objectVarArrayName + "_B";
                        
                        String objVarCmdA = "objectvar " + arrayA+"["+allSynapses.size()+"]\n\n";
                        String objVarCmdB = "objectvar " + arrayB+"["+allSynapses.size()+"]\n\n";
                        
                        if (!synObjvarArraysCreated.contains(objVarCmdA))
                        {
                            responseConn.append(objVarCmdA);
                            synObjvarArraysCreated.add(objVarCmdA);
                        }
                        if (!synObjvarArraysCreated.contains(objVarCmdB))
                        {
                            responseConn.append(objVarCmdB);
                            synObjvarArraysCreated.add(objVarCmdB);
                        }
                        String gjListenObjA = arrayA + "["+singleConnIndex+"]";
                        String gjListenObjB = arrayB + "["+singleConnIndex+"]";
                        
                        
                        String srcSecVarLoc = srcSecNameFull + ".v("+ fractSrcSection + ")";
                        String tgtSecVarLoc = tgtSecNameFull + ".v("+ fractTgtSection + ")";
                        int tgtGlobalId  = globalPreSynId;
                        int srcGlobalId  = globalPreSynId*2;
                        globalPreSynId++;
                        
                        if (!simConfig.getMpiConf().isParallelNet())
                        {
                            // Target cell mechanism
                            responseConn.append(tgtSecNameFull + " { " + gjListenObjA + " = new " + synapseType + "(" + fractTgtSection + ") }\n");

                            responseConn.append(gjListenObjA + ".weight = "+weight+"\n");

                            responseConn.append("setpointer "+ gjListenObjA + ".vgap, "  + srcSecVarLoc + "\n\n");


                            // Source cell mechanism
                            responseConn.append(srcSecNameFull + " { "  + gjListenObjB + " = new " + synapseType + "(" + fractSrcSection + ") }\n");

                            responseConn.append(gjListenObjB + ".weight = "+weight+"\n");

                            responseConn.append("setpointer "+ gjListenObjB + ".vgap, " + tgtSecVarLoc + "\n\n");
                        }
                        else
                        {
                            
                            String targetExists = "isCellOnNode(\""+ targetCellGroup + "\", "+ synConn.targetEndPoint.cellNumber + ")";
                            String sourceExists = "isCellOnNode(\""+ sourceCellGroup + "\", " + synConn.sourceEndPoint.cellNumber + ")";
                            
                            // Target cell mechanism
                            responseConn.append("if ("+targetExists+") {\n");
                            
                            responseConn.append("    "+tgtSecNameFull + " { " + gjListenObjA + " = new " + synapseType + "(" + fractTgtSection + ") }\n");
                            responseConn.append("    "+gjListenObjA + ".weight = "+weight+"\n");
                            responseConn.append("    "+"pnm.pc.target_var(&"+gjListenObjA + ".vgap, "+tgtGlobalId+")\n");
                             
                            responseConn.append("    "+"pnm.pc.source_var(&"+tgtSecVarLoc+ ", "+srcGlobalId+")\n");
                            
                            responseConn.append("}\n");


                            

                            // Source cell mechanism
                            responseConn.append("if ("+sourceExists+") {\n");
                            responseConn.append("    "+srcSecNameFull + " { "  + gjListenObjB + " = new " + synapseType + "(" + fractSrcSection + ") }\n");
                            responseConn.append("    "+gjListenObjB + ".weight = "+weight+"\n");
                            responseConn.append("    "+"pnm.pc.target_var(&"+gjListenObjB + ".vgap, "+srcGlobalId+")\n");
                             
                            responseConn.append("    "+"pnm.pc.source_var(&"+srcSecVarLoc+ ", "+tgtGlobalId+")\n");
                            
                            responseConn.append("}\n");

                        }
                        

                    }


                }
                
                hocWriter.write(responseConn.toString());
                hocWriter.flush();
            }
            
            
            //StringBuilder responseConn = new StringBuilder();
            
            if (simConfig.getMpiConf().isParallelNet() && addComments)
            {
                hocWriter.write("print \"++++++++++++\"\n");
                hocWriter.write("print \"Created netcons: \", allCurrentNetConns.count(), \" on host \", hostid\n");
                hocWriter.write("for c = 0,allCurrentNetConns.count()-1 {\n");
                hocWriter.write("   print \"Source of \", c,\": \", allCurrentNetConns.o(c).precell(), \", gid: \",allCurrentNetConns.o(c).srcgid()   \n");
                hocWriter.write("}\n");
                hocWriter.write("print \"++++++++++++\"\n");
            }
        }
        
        if (containsGapJunctions && simConfig.getMpiConf().isParallelNet())
        {
            hocWriter.write("\npnm.pc.setup_transfer()\n\n");
        }


        //GeneralUtils.timeCheck("Finsihed gen of syn conns, totNetConns: "+totNetConns+", response len: "+response.length()+ ", ratio: "+ (float)response.length()/totNetConns);
        GeneralUtils.timeCheck("Finsihed gen of syn conns");
        
        
        if (addComments && simConfig.getMpiConf().isParallelNet())
        {
            hocWriter.write("print \" -----------------------   Finished generation of net conns on host: \", hostid\n"); 
           // response.append("waittime = pnm.pc.barrier()\n"); 
           // response.append("print \"  Host: \", hostid, \" was waiting: \", waittime\n"); 
            /*response.append("netConInfoParallel(NetCon_10000000)\n"); 
            response.append("netConInfoParallel(NetCon_10000000_temp)\n"); 
            response.append("netConInfoParallel(NetCon_10000001)\n"); 
            response.append("netConInfoParallel(NetCon_10000001_temp)\n"); */
            hocWriter.write("print \""+preSectionsVsGids+"\"\n"); 
            hocWriter.write("print \"\"\n"); 
        }


        logger.logComment("Finished generation of the net conns");

    }

    public String generatePlots()
    {
        StringBuilder response = new StringBuilder();

        ArrayList<PlotSaveDetails> plots = project.generatedPlotSaves.getPlottedPlotSaves();

        addMajorHocComment(response, "Adding " + plots.size() + " plot(s)");

        for (PlotSaveDetails plot : plots)
        {
            ArrayList<Integer> cellNumsToPlot = plot.cellNumsToPlot;
            ArrayList<Integer> segIdsToPlot = plot.segIdsToPlot;

            String neuronVar = this.convertToNeuronVarName(plot.simPlot.getValuePlotted());

            float minVal = convertToNeuronValue(plot.simPlot.getMinValue(), plot.simPlot.getValuePlotted());
            float maxVal = convertToNeuronValue(plot.simPlot.getMaxValue(), plot.simPlot.getValuePlotted());

            Cell nextCell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(plot.simPlot.getCellGroup()));

            for (Integer cellNum : cellNumsToPlot)
            {
                for (Integer segId: segIdsToPlot)
                {
                    Segment seg = nextCell.getSegmentWithId(segId);

                    if (seg==null)
                    {
                        GuiUtils.showErrorMessage(logger,
                                              "Problem getting segment with id: "+segId+" for input: "+plot.simPlot.getPlotReference(), null, null);

                        return null;
                    }

                    float lenAlongSegment
                        = CellTopologyHelper.getFractionAlongSection(nextCell,
                                                                     seg,
                                                                     0.5f);

                    String title = "a_" + plot.simPlot.getCellGroup()
                        + "[" + cellNum + "]"
                        + "." + getHocSectionName(seg.getSection().getSectionName())
                        + "." + neuronVar;

                    String varRefIncFract = title + "(" + lenAlongSegment + ")";

                    if (plot.simPlot.isSynapticMechanism())
                    {
                        String netConn = SimPlot.getNetConnName(plot.simPlot.getValuePlotted());
                        
                        String synType = SimPlot.getSynapseType(plot.simPlot.getValuePlotted());

                        boolean isGapJunc = project.cellMechanismInfo.getCellMechanism(synType).isGapJunctionMechanism();

                        ArrayList<PostSynapticObject> synObjs = project.generatedNetworkConnections.getSynObjsPresent(netConn,
                                                                      synType,
                                                                      cellNum,
                                                                      segId);

                        logger.logComment("Syn objs for: "+netConn+", "+synType+", cellNum: "+cellNum
                                          +", segId: "+segId+": " + synObjs);

                        for (PostSynapticObject synObj: synObjs)
                        {
                            title = this.getSynObjName(synObj)+"."+neuronVar;
                            if(isGapJunc)
                            {
                                title = "elec"+title;
                                title = GeneralUtils.replaceAllTokens(title, "[", "_A[");
                                //title = GeneralUtils.replaceAllTokens(title, ".g", ".i");
                            }
                            
                            varRefIncFract = title;

                            response.append(generateSinglePlot(title,
                                                               plot.simPlot.getGraphWindow(),
                                                               minVal,
                                                               maxVal,
                                                               varRefIncFract,
                                                               getNextColour(plot.simPlot.getGraphWindow())));
                        }

                    }
                    else
                    {


                        response.append(generateSinglePlot(title,
                                                           plot.simPlot.getGraphWindow(),
                                                           minVal,
                                                           maxVal,
                                                           varRefIncFract,
                                                           getNextColour(plot.simPlot.getGraphWindow())));
                    }
                }
            }

        }

        

        return response.toString();
    }

    private String generateSinglePlot(String plotTitle,
                                      String graphWindow,
                                      float minVal,
                                      float maxVal,
                                      String varReference,
                                      String colour)
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response,
                          " This code pops up a plot of " + varReference +"\n");

        if (!graphsCreated.contains(graphWindow))
        {
            response.append("objref " + graphWindow + "\n");
            response.append(graphWindow + " = new Graph(0)\n");
            response.append("{"+graphWindow + ".size(0, tstop"
                            + "," + minVal
                            + "," + maxVal + ")}\n");

            response.append("{"+graphWindow + ".view(0, " + minVal + ", tstop" +
                            ", " + (maxVal - minVal) + ", 80, 330, 330, 250)}\n");

            graphsCreated.add(graphWindow);
        }

        response.append("{\n");

        response.append("    " + graphWindow + ".addexpr(\"" + plotTitle
                        + "\", \""+varReference+"\", " + colour
                        + ", 1, 0.8, 0.9, 2)\n");


        response.append("    " + "graphList[0].append(" + graphWindow + ")\n");
        response.append("}\n");

        return response.toString();

    }

    public String generateShapePlot()
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response, " This code pops up a Shape plot of the cells\n");

        response.append("objref plotShape\n");
        response.append("plotShape = new PlotShape()\n");

        //response.append("plotShape.show(0)\n");
        response.append("plotShape.exec_menu(\"Shape Plot\")\n\n");
        response.append("fast_flush_list.append(plotShape)\n\n");


        return response.toString();
    }

    public String getNextColour(String plotFrame)
    {
        if (!nextColour.containsKey(plotFrame))
        {
            nextColour.put(plotFrame, 1);
        }
        int colNum = nextColour.get(plotFrame);
        
        String colour = colNum + "";
        int newColour = colNum +1;
        if (newColour >= 10) newColour = 1;
        
        nextColour.put(plotFrame, newColour);
        
        return colour;
    }


    public boolean generateLinuxBasedScripts()
    {
        // If the code is remotely eecuted it assumes it will be Linux based scripts (for running on Linux or Macs)
        if (GeneralUtils.isWindowsBasedPlatform() && !simConfig.getMpiConf().isRemotelyExecuted()) return false;

        return true;
    }

    public String generateRunMechanism()
    {
        StringBuilder response = new StringBuilder();

        response.append(generateNeuronCodeBlock(NativeCodeLocation.AFTER_NET_CREATION));

        String dateCommand = "date +%x,%X:%N";


        String timeStepInfo = "dt: \",dt,\"ms,";
        if(project.neuronSettings.isVarTimeStep())
        {
            timeStepInfo = " variable time step,";
        }

        boolean announceDate = true;

        if (GeneralUtils.isWindowsBasedPlatform())
        {
            if ((new File("c:\\cygwin")).exists() ||
                (new File("c:\\Program Files\\cygwin")).exists() ||
                (new File("c:\\Program Files (x86)\\cygwin")).exists())
            {
                announceDate = true;
            }
            else
            {
                announceDate = false;
            }
        }

        //Temp for laptop
                announceDate = false;


        if (simConfig.getMpiConf().isParallelNet() && simConfig.getMpiConf().getMpiVersion().equals(MpiSettings.OPENMPI_V2))
        {
            //Unresolved error on OpenMPI on Matthau/Lemmon...
            announceDate = false;
        }

        String dateInfo = "";


        response.append("strdef date\n");
        if(announceDate)
        {
            response.append("// Note: if there is a problem with this line under Windows, it may mean that you will have to install Cygwin");
            response.append("// which includes the \"date\" unix command. Install under c:\\cygwin\n\n");
            response.append("{system(\"" + dateCommand + "\", date)}\n");
            dateInfo = " at time: \", date, \"";
        }
        else
        {
            response.append("// Note: not showing date/time of start/stop of simulation. This requires Cygwin to be installed");
            response.append("// which includes the \"date\" unix command. Install under c:\\cygwin\n\n");
        }
        
        String startUpInfo = "print \"Starting simulation of duration \",tstop,\"ms, "+timeStepInfo+" reference: \",simReference,\""  +
                    dateInfo+"\"\n\n";

        if (simConfig.getMpiConf().isParallelNet())
        {
            response.append("setuptime = stopsw()\n\n");
            response.append("print \"Setup time for simulation on host \",hostid,\": \",setuptime,\" seconds\"\n\n");
            
            response.append("{pnm.want_all_spikes()}\n");

            response.append("{stdinit()}\n");
            response.append("print \"Initialised on \", host, \", hostid: \", hostid\n");
            response.append("{currenttime = startsw()}\n");

            response.append(startUpInfo);

            response.append("{pnm.psolve(tstop)}\n");
            response.append("{realruntime = startsw() - currenttime}\n");

            response.append("print \"Finished simulation in \", realruntime ,\"seconds on host \", hostid\n\n");

            if(announceDate)
            {
                response.append("{system(\"" + dateCommand + "\", date)}\n");
                dateInfo = "print \"Current time: \", date\n\n";
            }

            if (addComments) 
            {
                response.append("for i=0, pnm.spikevec.size-1 {\n");
                response.append("    print \"Spike \",i, \" at time \", pnm.spikevec.x[i],\" in cell: \", pnm.idvec.x[i]\n");
                response.append("}\n");
            }

            return response.toString();
        }


        //if (this.windowsTargetEnv()) dateCommand = "c:/windows/time.exe /T";
        
        
        response.append("setuptime = stopsw()\n\n");
        response.append("print \"Setup time for simulation: \",setuptime,\" seconds\"\n\n");


        response.append(startUpInfo);

        response.append("{currenttime = startsw()}\n");

        if (!project.neuronSettings.isVarTimeStep())
        {
            addMajorHocComment(response, "Main run statement");
            response.append("{run()}\n\n");
        }
        else
        {
            addMajorHocComment(response, "Main run statement");
            addHocComment(response, "Setting basic variable time step active");

            response.append("{cvode.active(1)}\n");
            response.append("{cvode.atol("+project.neuronSettings.getVarTimeAbsTolerance()+")}\n");

            response.append("{run()}\n\n");

        }
        dateInfo = "";
        
        if(announceDate)
        {
            response.append("{system(\"" + dateCommand + "\", date)}\n");
            dateInfo = "print \"Current time: \", date\n\n";
        }

        response.append("{realruntime = startsw() - currenttime}\n");

        response.append("print \"Finished simulation in \", realruntime ,\"seconds\"\n\n");
        response.append(dateInfo);

        return response.toString();



    }





    public String generateGUIForRerunning()
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response, " This code pops up a simple Run Control\n");

        response.append("{\n");
        response.append("xpanel(\"RunControl\", 0)\n");
        response.append("v_init = " + project.simulationParameters.getInitVm() + "\n");
        //response.append("xvalue(\"Init\",\"v_init\", 1,\"stdinit()\", 1, 1 )\n");
        response.append("xbutton(\"Init & Run\",\"run()\")\n");
        response.append("xbutton(\"Stop\",\"stoprun=1\")\n");
        response.append("t = 0\n");
        response.append("xvalue(\"t\",\"t\", 2 )\n");
        response.append("tstop = " + getSimDuration() + "\n");
        response.append("xvalue(\"Tstop\",\"tstop\", 1,\"tstop_changed()\", 0, 1 )\n");
        response.append("dt = " + project.simulationParameters.getDt() + "\n");
        response.append(" xvalue(\"dt\",\"dt\", 1,\"setdt()\", 0, 1 )\n");
        response.append("xpanel(80,80)\n");
        response.append("}\n\n");

        return response.toString();
    }

    private String generateQuit()
    {
        StringBuilder response = new StringBuilder();

        addHocComment(response,
                          " The hoc will quit after finishing...\n");

        response.append("\nquit()\n");

        return response.toString();
    }
    
    public static boolean isRunModePythonBased(int runMode)
    {
        return (runMode == RUN_PYTHON_XML || runMode == RUN_PYTHON_HDF5) ;
    }


    public void addComment(StringBuilder responseBuffer, String comment)
    {
        if (!isRunModePythonBased(genRunMode))
            addHocComment(responseBuffer, comment);
        else
            PythonUtils.addComment(responseBuffer, comment, addComments);
    }

    public static void addHocComment(StringBuilder responseBuffer, String comment)
    {
        if (!addComments) return;

        //comment = GeneralUtils.replaceAllTokens(comment, "\n", "\n//  ");
        
        addHocComment(responseBuffer, comment, "", true);
    }
    
    public static void addHocComment(StringBuilder responseBuffer, String comment, boolean inclReturn)
    {
        if (!addComments) return;
        addHocComment(responseBuffer, comment, "", inclReturn);
    }


    public static synchronized void addHocComment(StringBuilder responseBuffer, String comment, String preSlashes, boolean inclReturn)
    {
        if (!addComments) return;
        String pre = preSlashes+ "//  ";
        
        
        if (!comment.toString().endsWith("\n"))
            comment = comment +"\n";

        comment = GeneralUtils.replaceAllTokens(comment.substring(0,comment.length()-1), "\n", "\n"+pre) + "\n";

        
        responseBuffer.append("\n" + pre + comment);
        if (inclReturn) responseBuffer.append("\n");
    }
    
    public void addMajorComment(StringBuilder responseBuffer, String comment)
    {
        if (!addComments)return;
        
        if (!isRunModePythonBased(genRunMode)) 
            addMajorHocComment(responseBuffer, comment);
        else
            PythonUtils.addMajorComment(responseBuffer, comment);
    }

    public static void addMajorHocComment(StringBuilder responseBuffer, String comment)
    {
        if (!addComments)return;
        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//////////////////////////////////////////////////////////////////////\n");
        responseBuffer.append("//   " + comment + "\n");
        responseBuffer.append("//////////////////////////////////////////////////////////////////////\n");
        responseBuffer.append("\n");
    }


    public static float convertToNeuronValue(Float val, String simIndepVarName)
    {
        if (simIndepVarName.equals(SimPlot.VOLTAGE))
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    UnitConverter.NEURON_UNITS);
        }
        if (simIndepVarName.indexOf(SimPlot.SPIKE)>=0)
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    UnitConverter.NEURON_UNITS);
        }
        else if (simIndepVarName.indexOf(SimPlot.COND_DENS) >= 0)
        {
            return (float) UnitConverter.getConductanceDensity(val,
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               UnitConverter.NEURON_UNITS);
        }
        else if (simIndepVarName.indexOf(SimPlot.CONCENTRATION)>=0)
        {
            /** @todo Check this... */
            return (float) UnitConverter.getConcentration(val,
                                                          UnitConverter.NEUROCONSTRUCT_UNITS,
                                                          UnitConverter.NEURON_UNITS);
        }
        else if (simIndepVarName.indexOf(SimPlot.CURR_DENS)>=0)
        {
            return (float) UnitConverter.getCurrentDensity(val,
                                                           UnitConverter.NEUROCONSTRUCT_UNITS,
                                                           UnitConverter.NEURON_UNITS);
        }
        else if (simIndepVarName.equals(SimPlot.REV_POT))
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    UnitConverter.NEURON_UNITS);
        }
        else if (simIndepVarName.indexOf(SimPlot.SYN_COND)>=0)
        {
            return (float) UnitConverter.getConductance(val,
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        UnitConverter.NEURON_UNITS);
        }
        return val;
    }



    public String convertToNeuronVarName(String simIndepVarName)
    {
        String neuronVar = null;
        String origIndepName = simIndepVarName;

        if (simIndepVarName.equals(SimPlot.VOLTAGE))
        {
            neuronVar = "v";
        }
        else if (simIndepVarName.indexOf(SimPlot.SPIKE)>=0)
        {
            // only used when plotting a spike, when saving, an aPCount is used.
            neuronVar = "v";
        }
        else if (simIndepVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
        {
            boolean isSyn = false;
            if (simIndepVarName.indexOf(SimPlot.SYNAPSES)>=0)
            {
                simIndepVarName = simIndepVarName.substring(SimPlot.SYNAPSES.length()+
                                                            SimPlot.PLOTTED_VALUE_SEPARATOR.length());
                isSyn = true;
            }

            String mechanismName = simIndepVarName.substring(0,
                                                      simIndepVarName.indexOf(SimPlot.
                                                                         PLOTTED_VALUE_SEPARATOR));


            String variable = simIndepVarName.substring(
                simIndepVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) + 1);

            logger.logComment("--------------   Original: "+origIndepName+", so looking to plot " 
                + variable +" on cell mechanism: " + mechanismName+", simIndepVarName: "+simIndepVarName);

            if (variable.startsWith(SimPlot.COND_DENS))
            {
                variable = "gion";

                neuronVar = variable + "_" + mechanismName;

            }
            else if (variable.indexOf(SimPlot.SYN_COND)>=0)
            {
                neuronVar = "g";

            }
            else if (variable.indexOf(SimPlot.SYN_CURR)>=0)
            {
                neuronVar = "i";

            }
            else if (isSyn)
            {
                neuronVar = simIndepVarName.substring(simIndepVarName.lastIndexOf(SimPlot.
                                                                         PLOTTED_VALUE_SEPARATOR)+1);
                
                logger.logComment("neuronVar: "+neuronVar);
            }
            else
            {
                CellMechanism cp = project.cellMechanismInfo.getCellMechanism(mechanismName);

                logger.logComment("Cell mech found: " + cp);

                if (cp == null)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Problem generating plot with Cell Mechanism: " +
                                              mechanismName, null, null);

                    return null;

                    //neuronVar = variable + "_" + mechanismName;
                }
                else
                {

                    if (cp instanceof ChannelMLCellMechanism)
                    {
                        try
                        {
                            if (variable.startsWith(SimPlot.CONCENTRATION))
                            {
                                logger.logComment("Looking to plot the concentration...");

                                String ion = variable.substring(variable.indexOf(SimPlot.
                                    PLOTTED_VALUE_SEPARATOR) + 1);

                                neuronVar = ion + "i"; // assume internal concentration
                            }
                            else if (variable.startsWith(SimPlot.CURR_DENS))
                            {
                                logger.logComment("Looking to plot the current density...");

                                String ion = variable.substring(variable.indexOf(SimPlot.
                                    PLOTTED_VALUE_SEPARATOR) + 1);

                                neuronVar = "i" + ion;
                            }
                            else if (variable.startsWith(SimPlot.REV_POT))
                            {
                                logger.logComment("Looking to plot the reversal potential...");

                                String ion = variable.substring(variable.indexOf(SimPlot.
                                    PLOTTED_VALUE_SEPARATOR) + 1);

                                neuronVar = "e" + ion;
                            }


                            else
                            {
                                logger.logComment(
                                    "Assuming using the native name of the variable");
                                neuronVar = variable + "_" + mechanismName;
                            }
                        }
                        catch (Exception ex)
                        {
                            GuiUtils.showErrorMessage(logger,
                                                      "Problem generating a plot with Cell Mechanism: " +
                                                      mechanismName, ex, null);
                            return null;

                        }

                    }
                    else
                    {
                        logger.logError("Unsupported type of Cell Mechanism");

                        neuronVar = variable + "_" + mechanismName;

                    }
                }
            }
        }
        else
        {
            // use the name itself...
            neuronVar = simIndepVarName;
        }


        return neuronVar;
    }


    private int suggestedRemoteRunTime = -1;

    public void setSuggestedRemoteRunTime(int t)
    {
        this.suggestedRemoteRunTime = t;
    }

    private String getArchSpecificDir()
    {
        String locationOfNeuron = GeneralProperties.getNeuronHomeDir();
        String dir = GeneralUtils.getArchSpecificDir();
        if (locationOfNeuron.indexOf("umac")>=0)
            dir = "umac";
        if (locationOfNeuron.indexOf("i386")>=0)
            dir = "i386";

        return dir;

    }

    public void runNeuronFile(File mainHocFile) throws NeuronException
    {
        logger.logComment("Trying to run the hoc file: "+ mainHocFile);

        if (!mainHocFile.exists())
        {
            throw new NeuronException("The NEURON file: "+ mainHocFile
                                      + " does not exist. Have you generated the NEURON code?");
        }

        ProcessFeedback pf = new ProcessFeedback()
        {
            public void comment(String comment)
            {
                logger.logComment("ProcessFeedback: "+comment);
            }
            public void error(String comment)
            {
                logger.logComment("ProcessFeedback: "+comment);
            }
        };

        logger.logComment("Getting rid of old simulation files...");

        File dirForDataFiles = mainHocFile.getParentFile();
        
        File[] filesInDir = dirForDataFiles.listFiles();

        logger.logComment("Files in dir: "+ dirForDataFiles.getAbsolutePath());
        for (int i = 0; i < filesInDir.length; i++)
        {
            logger.logComment("File "+i+": "+filesInDir[i]);
        }

        Runtime rt = Runtime.getRuntime();
        
        String fullCommand = "";

        if (genRunMode!=RUN_VIA_CONDOR)
        {
            try
            {
                String locationOfNeuron = GeneralProperties.getNeuronHomeDir();

                String neuronExecutable = null;

                if (!generateLinuxBasedScripts())
                {
                    logger.logComment("Assuming Windows environment...");
                    
                    neuronExecutable = locationOfNeuron + System.getProperty("file.separator")
                        + "bin" + System.getProperty("file.separator") + "neuron.exe";
                    
                    String filename = getHocFriendlyFilename(mainHocFile.getAbsolutePath());

                    if (filename.indexOf(" ")>=0)
                    {
                        GuiUtils.showErrorMessage(logger, "Error. The full name of the file to execute in NEURON: "+filename
                                                  +" contains a space. This will throw an error in NEURON." +
                                                  "\n Was the code created in a directory containing a space in its name?", null, null);
                    }

                    if (true || !project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                    {
                        fullCommand = GeneralProperties.getExecutableCommandLine() + " "
                            + neuronExecutable + " "+filename;
                    }
                    //todo: Needs further testing
                    else
                    {
                        fullCommand = GeneralUtils.replaceAllTokens(neuronExecutable, "neuron", "nrniv") + " "+filename;
                    }

                    File dirToRunIn = dirForDataFiles;

                    String scriptText = "cd "+dirToRunIn.getAbsolutePath()+"\n";
                  
                    scriptText = scriptText + fullCommand;
                    
                    File scriptFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()), "runsim.bat");
                    FileWriter fw = new FileWriter(scriptFile);
                    fw.write(scriptText);
                    fw.close();

                    logger.logComment("Going to execute command: " + fullCommand + " in dir: " + dirToRunIn);

                    rt.exec(fullCommand, null, dirToRunIn);

                    logger.logComment("Have executed command: " + fullCommand + " in dir: " +
                                      dirToRunIn);

                }
                else
                {
                    String[] commandToExe = null;
                    String[] envParams = null;
                    
                    /*TODO: Make this an input option!!!
                    if (genRunMode==RUN_PYTHON_HDF5)
                    {
                        envParams = new String[1];
                        envParams[0] = "LD_LIBRARY_PATH=/usr/local/hdf5/lib";
                    }*/
                    
                    if (dirForDataFiles.getAbsolutePath().indexOf(" ")>=0)
                    {
                        throw new NeuronException("NEURON files cannot be run in a directory like: "+ dirForDataFiles
                                + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run NEURON.\nArguments need to be given to this executable and spaces in filenames cause problems.\n"
                                +"Try saving the project in a directory without spaces.");
                    }

                    String title = "NEURON_simulation" + "__"+ project.simulationParameters.getReference();

                    File dirToRunInFile = dirForDataFiles;

                    String dirToRunInPath = dirToRunInFile.getAbsolutePath();
                    
                    String mainExecutable = "nrngui";
                    
                    if (simConfig.getMpiConf().isParallelOrRemote() )
                    {
                        mainExecutable = "nrniv";

                        neuronExecutable = locationOfNeuron
                            + System.getProperty("file.separator")
                            + "bin"
                            + System.getProperty("file.separator")
                            + mainExecutable;
                    }
                    else if (project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                    {
                        String dir = getArchSpecificDir();

                        neuronExecutable = dirToRunInFile.getAbsolutePath()+"/"+dir+"/special";

                        if(neuronExecutable.indexOf("generated")<0)
                        {
                            // Ensure the file is executable
                            rt.exec(new String[]{"chmod","u+x",neuronExecutable}).waitFor();

                        }

                    }
                    else
                    {

                        neuronExecutable = locationOfNeuron
                            + System.getProperty("file.separator")
                            + "bin"
                            + System.getProperty("file.separator")
                            + mainExecutable;
                    }




                    String basicCommLine = GeneralProperties.getExecutableCommandLine();


                    String executable = "";
                    String extraArgs = "";
                    String titleOpt = "";
                    String workdirOpt = "";
                    String postArgs = "";
                    
                    String mpiFlags = "";
                    
                    StringBuilder preCommand = new StringBuilder("");

                    /*TODO: Move to hpc/mpi package...*/

                    //System.out.println("Generating for: "+ simConfig.getMpiConf().getMpiVersion()+", "+simConfig.getMpiConf().getMpiVersion().equals(MpiSettings.OPENMPI_V2));

                    if (simConfig.getMpiConf().isParallelNet())
                    {
                        
                        StringBuilder hostList = new StringBuilder("-map ");
                        
                        String hostSeperator = ":";
                        String mainCmd = "mpirun";
                        
                        if (simConfig.getMpiConf().getMpiVersion().equals(MpiSettings.OPENMPI_V2))
                        {                            
                            hostList = new StringBuilder("-host ");
                            hostSeperator = ",";
                            mpiFlags = "-mpi ";
                            mainCmd = "mpiexec";
                        }
                        else if (simConfig.getMpiConf().getMpiVersion().equals(MpiSettings.MPICH_V2))
                        {
                            hostSeperator = ",";
                            mpiFlags = "-mpi ";
                            mainCmd = "mpiexec";
                        }

                        logger.logComment("MPI flags: "+ mpiFlags+", hostSeperator: "+ hostSeperator+", hostList: "+ hostList+", mainCmd: "+ mainCmd);

                        ArrayList<MpiHost> hosts = simConfig.getMpiConf().getHostList();
                        
                        StringBuilder machineFileString = new StringBuilder("# An automatically generated machine file for MPI configuration: "+ simConfig.getMpiConf()+"\n");
                        
                        for (int i = 0; i < hosts.size(); i++)
                        {
                            for (int j = 0; j < hosts.get(i).getNumProcessors(); j++)
                            {
                                if (!(i==0 && j==0)) hostList.append(hostSeperator);

                                hostList.append(hosts.get(i).getHostname());
                            }
                            String host = hosts.get(i).getHostname();
                            if (host.equals(MpiSettings.LOCALHOST))
                                host = GeneralUtils.getLocalHostname(); // try to get a better name...
                            
                            machineFileString.append(host+":"+hosts.get(i).getNumProcessors()+"\n");
                        }
                        
                        
                        if (simConfig.getMpiConf().getMpiVersion().equals(MpiSettings.MPICH_V2))
                        {
                            
                        
                            File machineFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()),
                                               MpiSettings.MACHINE_FILE);
                            FileWriter fw = new FileWriter(machineFile);
                            fw.write(machineFileString.toString());
                            fw.close();
                            
                            preCommand.append(mainCmd+" -machinefile "+ machineFile.getAbsolutePath()+" -np "+ simConfig.getMpiConf().getTotalNumProcessors() +" ");
                        }
                        else
                        {
                            
                            preCommand.append(mainCmd+" "+hostList+" ");

                        }
                        
                        

                    }

                    File scriptFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()),
                                               "runsim.sh");
                    
                    
                    StringBuilder scriptText = new StringBuilder();

                    
                    if ((GeneralUtils.isLinuxBasedPlatform() || simConfig.getMpiConf().isRemotelyExecuted())
                            && !GeneralUtils.isMacBasedPlatform())
                    {
                        logger.logComment("Is linux platform...");

                        if (basicCommLine!=null && basicCommLine.indexOf("konsole") >= 0)
                        {
                            logger.logComment("Assume we're using KDE");
                            titleOpt = "--title";
                            workdirOpt = "--workdir";
                               // + dirToRunIn.getAbsolutePath();

                            extraArgs = "-e /bin/bash ";
                            executable = basicCommLine.trim();
                        }
                        else if (basicCommLine!=null && basicCommLine.indexOf("gnome") >= 0)
                        {
                            logger.logComment("Assume we're using Gnome");
                            titleOpt = "--title";
                            workdirOpt = "--working-directory";

                            if (basicCommLine.trim().indexOf(" ") > 0) // case where basicCommLine is gnome-terminal -x
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
                        else if (GeneralUtils.isWindowsBasedPlatform())
                        {
                            // i.e. is remotely executed from Windows
                            title = "";
                            dirToRunInPath = "";
                            extraArgs = " /D "+scriptFile.getParent()+" bash ";

                            executable = GeneralProperties.getExecutableCommandLine();
                        }
                        else
                        {
                            logger.logComment("Unknown console command, going with the flow...");

                            executable = basicCommLine.trim();
                        }

                        commandToExe = new String[]{executable,
                                titleOpt, title,
                                workdirOpt, dirToRunInPath,
                                extraArgs,
                                scriptFile.getAbsolutePath()};
                    }
                    else if (GeneralUtils.isMacBasedPlatform())
                    {
                            logger.logComment("Assuming a Mac based machine it is...");

                            executable = basicCommLine.trim();

                            /** @todo update with real command line option for working dir... */
                            workdirOpt = " ";


                            postArgs = "";

                            //dirToRunInFile = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());
                            //dirToRunInPath = "";
                            title = "";


                            commandToExe = new String[]{executable,
                                    scriptFile.getAbsolutePath()};
                    }


                    if (project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                    {
                        commandToExe = new String[]{scriptFile.getAbsolutePath()};
                    }


                    if(!simConfig.getMpiConf().isRemotelyExecuted())
                    {

                        scriptText.append("cd '" + dirToRunInFile.getAbsolutePath() + "'\n");

                        if (genRunMode==RUN_PYTHON_HDF5 || project.neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC))
                        {

                            if (simConfig.getMpiConf().isParallelNet())
                            {
                                neuronExecutable = neuronExecutable+" -python ";
                            }
                            else
                            {

                                String dir = getArchSpecificDir();
                                
                                neuronExecutable = dir+"/special -python ";
                            }

                            scriptText.append(preCommand
                            + neuronExecutable
                            + " "
                            + mpiFlags
                            + runPythonFile.getName()
                            + postArgs);

                        }
                        else if (true || !isRunModePythonBased(genRunMode))
                        {
                            scriptText.append(preCommand
                            + neuronExecutable
                            + " "
                            + mpiFlags
                            + mainHocFile.getName()
                            + postArgs);

                            if (project.neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                            {
                                scriptText.append(" > /tmp/logNEURON_"+project.getProjectFileName()
                                        +"_"+project.simulationParameters.getReference());
                            }
                        }
                        else
                        {
                            scriptText.append("python "+ runPythonFile.getName());
                        }



                    }
                    else
                    {

                        int time;

                        if (suggestedRemoteRunTime<=0)
                        {
                            time = QueueInfo.getWallTimeSeconds(project, simConfig);
                        }
                        else
                        {
                            time = suggestedRemoteRunTime;
                        }

                        KnownSimulators sim = KnownSimulators.NEURON;

                        if (genRunMode==RUN_PYTHON_HDF5 || project.neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC))
                        {
                            sim = KnownSimulators.PY_NEURON;
                        }

                        scriptText.append(simConfig.getMpiConf().getPushScript(project.getProjectName(), 
                                                                               project.simulationParameters.getReference(),
                                                                               sim,
                                                                               dirToRunInFile));

                        File simResultsDir = new File(ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()),
                                project.simulationParameters.getReference());

                        if (simConfig.getMpiConf().getQueueInfo()!=null)
                        {
                            String submitJob = simConfig.getMpiConf().getQueueSubmitScript(project.getProjectName(), project.simulationParameters.getReference(), time, sim);

                            File submitJobFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()), QueueInfo.submitScript);

                            FileWriter fw = new FileWriter(submitJobFile);
                            //scriptFile.se
                            fw.write(submitJob);
                            fw.close();
                            
                            try
                            {
                                // This is to make sure the file is written
                                Thread.sleep(500);
                            }
                            catch (InterruptedException ex)
                            {
                            }

                            // bit of a hack...
                            rt.exec(new String[]{"chmod","u+x",submitJobFile.getAbsolutePath()}).waitFor();

                            if (project.neuronSettings.isCopySimFiles())
                            {
                                GeneralUtils.copyFileIntoDir(submitJobFile, simResultsDir);
                            }

                        }

                        File pullScriptFile = new File(simResultsDir, RemoteLogin.remotePullScriptName);

                        String pullScriptText = simConfig.getMpiConf().getPullScript(project.getProjectName(),
                                                                                     project.simulationParameters.getReference(),
                                                                                     ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()));

                        FileWriter fw = new FileWriter(pullScriptFile);
                        //scriptFile.se
                        fw.write(pullScriptText);
                        fw.close();

                        // bit of a hack...
                        rt.exec(new String[]{"chmod","u+x",pullScriptFile.getAbsolutePath()}).waitFor();


                    }


                    
                    FileWriter fw = new FileWriter(scriptFile);
                    //scriptFile.se
                    fw.write(scriptText.toString());
                    fw.close();

                    // bit of a hack...
                    rt.exec(new String[]{"chmod","u+x",scriptFile.getAbsolutePath()}).waitFor();
                    
                    fullCommand = "";
                    for (int i=0;i<commandToExe.length;i++)
                    {
                        fullCommand = fullCommand+commandToExe[i]+" ";
                    }

                    String summary = "command: <" + fullCommand+"> with params: [";

                    if(envParams!=null)
                    {
                        for(String p:envParams)
                            summary = summary + p;
                    }
                    else
                    {
                        envParams = new String[]{};
                        summary = summary+"-none-";
                    }
                    summary = summary+"]";


                    logger.logComment("Going to execute: "+summary);

                    if(envParams!=null && envParams.length>0)
                    {
                        logger.logComment("++ <" + commandToExe+"> + <" + envParams[0]+", ..>");
                        rt.exec(commandToExe, envParams);
                    }
                    else
                    {
                        //rt.exec(fullCommand, envParams);#
                        
                        logger.logComment("== <" + fullCommand+">");
                        
                        ProcessManager.runCommand(fullCommand, pf, 4);

                    }

                    logger.logComment("Have successfully executed " + summary);

                }

            }
            catch (Exception ex)
            {
                logger.logError("Error running command: " + fullCommand, ex);
                throw new NeuronException("Error executing the hoc file: " + mainHocFile+"\n"
                        + "Trying to execute command:\n"+fullCommand+"\n\n"
                        + "This may be resolvable by updating the Command line executable used to run external programs\n"
                        + "at Settings -> General Properties and Project Defaults. If you're running on a Gnome desktop\n"
                        + "based Linux install (e.g. Ubuntu), you might want to change \"konsole\" to \"gnome-terminal -x\"\n\n"+ex.getMessage(), ex);
            }
        }
        else
        {
            logger.logComment("Creating the extra files for running the code through Condor...");
            FileWriter condorBatchFileWriter = null;
            FileWriter condorSubmitFileWriter = null;
            File condorSubmitFile = null;
            File condorBatchFile = null;


            File dirForSimFiles = ProjectStructure.getDirForSimFiles(project.simulationParameters.getReference(), project);

            try
            {
                //String systemOS = System.getProperty("os.name");

                if (GeneralUtils.isWindowsBasedPlatform())
                {

                    condorBatchFile = new File(dirForSimFiles, project.simulationParameters.getReference() + ".bat");
                    condorBatchFileWriter = new FileWriter(condorBatchFile);

                    condorSubmitFile = new File(dirForSimFiles, project.simulationParameters.getReference() + ".sub");
                    condorSubmitFileWriter = new FileWriter(condorSubmitFile);

                    condorSubmitFileWriter.write("universe = vanilla\n");
                    //fw.write("environment = path=c:\WINDOWS\SYSTEM32;C:\nrn55\bin\n"):
                    condorSubmitFileWriter.write("executable = " + condorBatchFile.getName() + "\n");
                    condorSubmitFileWriter.write("arguments = " + mainHocFile.getName() + "\n");
                    condorSubmitFileWriter.write("initialdir = " + dirForSimFiles.getAbsolutePath() + "\n");


                    condorSubmitFileWriter.write("transfer_input_files = nrnmech.dll");// + mainHocFile.getName());

                    File[] allHocFiles
                        = mainHocFile.getParentFile().listFiles(new SimpleFileFilter(new String[]{".hoc"}, ""));

                    for (int i = 0; i < allHocFiles.length; i++)
                    {
                        condorSubmitFileWriter.write(", " + allHocFiles[i].getName());

                    }


                    for (int i = 0; i < cellTemplatesGenAndIncluded.size(); i++)
                    {
                        String nextHocFile = cellTemplatesGenAndIncluded.elementAt(i);
                        condorSubmitFileWriter.write(", " + (new File(nextHocFile)).getName());
                    }

                    //condorSubmitFileWriter.write(", nCtools.hoc");
                    condorSubmitFileWriter.write("\n");

                    condorSubmitFileWriter.write("should_transfer_files = YES\n");
                    condorSubmitFileWriter.write("when_to_transfer_output = ON_EXIT\n");
                    condorSubmitFileWriter.write("transfer_files = ALWAYS\n");
                    condorSubmitFileWriter.write("output = nrn.out\n");
                    condorSubmitFileWriter.write("error = nrn.err\n");
                    condorSubmitFileWriter.write("log = nrn.log\n");
                    condorSubmitFileWriter.write("queue\n");

                    condorSubmitFileWriter.flush();
                    condorSubmitFileWriter.close();

                    condorBatchFileWriter.write("C:\\WINDOWS\\SYSTEM32\\cmd /C "
                                                + GeneralProperties.getNeuronHomeDir()
                                                + System.getProperty("file.separator")
                                                + "bin"
                                                + System.getProperty("file.separator")
                                                + "nrniv.exe %1\n");

                    condorBatchFileWriter.flush();
                    condorBatchFileWriter.close();

                    logger.logComment("Assuming Windows environment...");
                    String executable = "condor_submit " + condorSubmitFile.getName();

                    File dirToRunIn = dirForSimFiles;

                    logger.logComment("Going to execute: " + executable + " in dir: " +
                                      dirToRunIn);


                    rt.exec(executable, null, dirToRunIn);
                    logger.logComment("Have successfully executed command: " + executable + " in dir: " +
                                      dirToRunIn);




                }
                else
                {

                    condorBatchFile = new File(dirForSimFiles, project.simulationParameters.getReference() + ".sh");
                    condorBatchFileWriter = new FileWriter(condorBatchFile);

                    condorSubmitFile = new File(dirForSimFiles, project.simulationParameters.getReference() + ".sub");
                    condorSubmitFileWriter = new FileWriter(condorSubmitFile);

                    condorSubmitFileWriter.write("universe = vanilla\n");
                    //fw.write("environment = path=c:\WINDOWS\SYSTEM32;C:\nrn55\bin\n"):
                    //condorSubmitFileWriter.write("executable = " + condorBatchFile.getName() + "\n");
                    condorSubmitFileWriter.write("executable = /bin/bash\n");
                    //condorSubmitFileWriter.write("arguments = " + mainHocFile.getName() + "\n");
                    condorSubmitFileWriter.write("arguments = " + condorBatchFile.getName() + "\n");
                    condorSubmitFileWriter.write("initialdir = " + dirForSimFiles.getAbsolutePath() + "\n");


                    //condorSubmitFileWriter.write("transfer_input_files = nrnmech.dll");// + mainHocFile.getName());
                    condorSubmitFileWriter.write("transfer_input_files = "+condorBatchFile.getName());

                    //StringBuilder file

                    File[] allHocFiles
                        = mainHocFile.getParentFile().listFiles(new SimpleFileFilter(new String[]{".hoc"}, ""));

                    for (int i = 0; i < allHocFiles.length; i++)
                    {
                        //System.out.println("Looking at: "+ allHocFiles[i].getAbsolutePath());
                        if (!allHocFiles[i].isDirectory())
                        {
                            condorSubmitFileWriter.write(", ");
                            condorSubmitFileWriter.write(allHocFiles[i].getName());
                        }
                    }

                    File libsDir = new File(dirForSimFiles, getArchSpecificDir());

                    // Messy roundabout way to do it...
                    File tempDir = new File(dirForSimFiles, "temp");
                    tempDir.mkdir();
                    File tempLibsDir = new File(tempDir, getArchSpecificDir());
                    tempLibsDir.mkdir();


                    GeneralUtils.copyDirIntoDir(libsDir, tempLibsDir, true, true);

                    String zippedLibsFilename = getArchSpecificDir() + ".zip";

                    File zipFile = new File(dirForSimFiles, zippedLibsFilename);

                    zipFile = ZipUtils.zipUp(tempDir, 
                            zipFile.getAbsolutePath(),
                            new ArrayList<String>(),
                            new ArrayList<String>());

                    //System.out.println("Zip file created with mod libs: "+ zipFile.getAbsolutePath());

                    condorSubmitFileWriter.write(", "+ zipFile.getName());


                    condorSubmitFileWriter.write(", nCtools.hoc");
                    condorSubmitFileWriter.write("\n");

                    condorSubmitFileWriter.write("should_transfer_files = YES\n");
                    condorSubmitFileWriter.write("when_to_transfer_output = ON_EXIT\n");
                    condorSubmitFileWriter.write("transfer_files = ALWAYS\n");
                    condorSubmitFileWriter.write("output = nrn.out\n");
                    condorSubmitFileWriter.write("error = nrn.err\n");
                    condorSubmitFileWriter.write("log = nrn.log\n");
                    condorSubmitFileWriter.write("queue\n");

                    condorSubmitFileWriter.flush();
                    condorSubmitFileWriter.close();

                    //condorBatchFileWriter.write("C:\\WINDOWS\\SYSTEM32\\cmd /C "
                    //                            + GeneralProperties.getNeuronHomeDir()
                    //                            + System.getProperty("file.separator")
                     //                           + "bin"
                    //                            + System.getProperty("file.separator")
                   //                             + "nrniv.exe %1\n");

                   //condorBatchFileWriter.write("mkdir i686\n");
                   //condorBatchFileWriter.write("mv \n");
                   condorBatchFileWriter.write("unzip "+zippedLibsFilename+"\n");
                   condorBatchFileWriter.write("chmod -R a+x "+getArchSpecificDir()+"\n");


                   // for testing...
                   condorBatchFileWriter.write("echo Current dir structure:\n");
                   condorBatchFileWriter.write("ls -altR\n");
                   //condorBatchFileWriter.write("sleep 4\n");
                   //condorBatchFileWriter.write("cp -Rv . /home/condor/temp/cop\n");


                   condorBatchFileWriter.write("echo\n");
                   condorBatchFileWriter.write("echo Starting NEURON...\n");

                   condorBatchFileWriter.write("/usr/local/nrn/"+getArchSpecificDir()+"/bin/nrngui "+mainHocFile.getName()+"\n");



                    condorBatchFileWriter.flush();
                    condorBatchFileWriter.close();

                    logger.logComment("Assuming *nix environment...");
                    String executable = "condor_submit " + condorSubmitFile.getName();

                    File dirToRunIn = dirForSimFiles;

                    logger.logComment("Going to execute: " + executable + " in dir: " +
                                      dirToRunIn);


                    rt.exec(executable, null, dirToRunIn);
                    logger.logComment("Have successfully executed command: " + executable + " in dir: " +
                                      dirToRunIn);


                }

            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error writing to file: " + condorSubmitFile , ex, null);
                try
                {
                    condorSubmitFileWriter.close();
                    condorBatchFileWriter.close();
                }
                catch (IOException ex1)
                {
                }
                catch (NullPointerException ex1)
                {
                }

                return;
            }

        }

    }


    public File getMainHocFile() throws NeuronException
    {
        if (!this.hocFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new NeuronException("Hoc file not yet generated");
        }

        return this.mainHocFile;

    }


    public static  void main(String[] args)
    {
        try
        {
            String str = "v < t";
            System.out.println(NeuronWriter.replaceInFunction(str, "v", "mmm"));
            System.out.println(NeuronWriter.replaceInFunction(str, "t", "ppp"));
            //System.exit(0);

            int runMode  = RUN_HOC;


            MainFrame frame = new MainFrame();

           // File pf = new File("/bernal/models/Parallel/Parallel.neuro.xml");
            File pf = new File("/home/padraig/nC_projects/Project_1ppp/Project_1ppp.neuro.xml");
            pf = new File("lems/nCproject/LemsTest/LemsTest.ncx");

            //File pf = new File("models/PVMExample/PVMExample.neuro.xml");

            frame.doLoadProject(pf.getAbsolutePath());

            System.out.println("doGenerate...");

            frame.projManager.doGenerate("AbstractCells", 1234);
            //frame.projManager.doGenerate("Test NeuroML2 ionChannel", 1234);

            System.out.println("Snoozing...");

            Thread.sleep(500);

            System.out.println("Coming out of sleep");

            frame.doCreateHoc(runMode);

            System.out.println("done create...");

            //System.exit(0);

            frame.projManager.getCurrentProject().neuronFileManager.runNeuronFile(
                frame.projManager.getCurrentProject().neuronFileManager.getMainHocFile());


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }

    public Vector<String> getModFilesToCompile()
    {
        Vector<String> allMods = new Vector<String>(this.stimModFilesRequired);
        
        allMods.addAll(cellMechFilesGenAndIncl);
        

        return allMods;
    }

}
