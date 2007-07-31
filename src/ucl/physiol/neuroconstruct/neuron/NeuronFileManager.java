/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.PlotSaveDetails;
import ucl.physiol.neuroconstruct.hpc.mpi.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;

/**
 * Main file for generating the script files for NEURON
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class NeuronFileManager
{
    private static ClassLogger logger = new ClassLogger("NeuronFileManager");

    /**
     * Various options for running the generated code
     */
    public static final int RUN_LOCALLY = 0;
    public static final int RUN_VIA_CONDOR = 1;
    public static final int RUN_PARALLEL = 2;


    /**
     * The random seed placed into the generated NEURON code
     */
    private long randomSeed = 0;

    private Project project = null;

    private File mainHocFile = null;

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

    private MultiRunManager multiRunManager = null;

    private int nextColour = 1;

    private Vector<String> graphsCreated = new Vector<String>();

    private static boolean addComments = true;

    private SimConfig simConfig = null;

    //ArrayList<String> multiRunLoops = new ArrayList<String>();

    private NeuronFileManager()
    {

    }

    public NeuronFileManager(Project project)
    {
        this.project = project;
        addComments = project.neuronSettings.isGenerateComments();

    }

    public static boolean addComments()
    {
        return addComments;
    }

    public void reset()
    {
        cellTemplatesGenAndIncluded = new Vector<String>();
        cellMechFilesGenAndIncl = new Vector<String>();
        this.stimModFilesRequired =  new Vector<String>();
        nextColour = 1; // reset it...
        graphsCreated = new Vector<String>();
        addComments = project.neuronSettings.isGenerateComments();

    }

    public void generateTheNeuronFiles(SimConfig simConfig,
                                       MultiRunManager multiRunManager,
                                       int runMode,
                                       long randomSeed) throws NeuronException, IOException
    {

        this.simConfig = simConfig;

        this.multiRunManager = multiRunManager;

        this.removeAllPreviousFiles();

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed
        ProjectManager.reinitialiseRandomGenerator();

        this.randomSeed = randomSeed;

        FileWriter fw = null;

        try
        {
            File dirForNeuronFiles = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());

            mainHocFile = new File(dirForNeuronFiles, project.getProjectName() + ".hoc");

            fw = new FileWriter(mainHocFile);

            fw.write(getHocFileHeader());

            if (runMode != RUN_VIA_CONDOR || GeneralUtils.isWindowsBasedPlatform())
                fw.write(generateGUIInclude());

            fw.write(generateWelcomeComments());

            fw.write(generateIncludes());

            fw.write(generateRandomise());

            fw.write(getHostname());

            if (runMode == RUN_PARALLEL) fw.write(initialiseParallel());

            fw.write(generateNeuronCodeBlock(NativeCodeLocation.BEFORE_CELL_CREATION));

            if (runMode == RUN_PARALLEL) fw.write(associateCellsWithNodes());

            fw.write(generateCellGroups(runMode));

            fw.write(generateInitialParameters(runMode));

            fw.write(generateNetworkConnections(runMode));

            fw.write(generateStimulations(runMode));

            fw.write(generateAccess(runMode));

            //fw.write(generateAfterCreationText());

            if (runMode != RUN_VIA_CONDOR && runMode != RUN_PARALLEL) // No gui if it's condor or parallel...
            {
                if (project.neuronSettings.isGraphicsMode())
                {
                    fw.write(generatePlots());

                    if (project.neuronSettings.isShowShapePlot())
                    {
                        fw.write(generateShapePlot());
                    }

                }

            }

            fw.write(generateInitHandlers());

            fw.write(generateRunSettings(runMode));
            fw.write(generateNeuronSimulationRecording(runMode));

            // Finishing up...

            if (runMode == RUN_LOCALLY)
            {
                fw.write(generateGUIForRerunning());
                fw.write(generateNeuronCodeBlock(NativeCodeLocation.AFTER_SIMULATION));
            }
            else if (runMode == RUN_PARALLEL)
            {
                fw.write(generateNeuronCodeBlock(NativeCodeLocation.AFTER_SIMULATION));
                fw.write(this.finishParallel());
            }
            else
            {
                fw.write(generateNeuronCodeBlock(NativeCodeLocation.AFTER_SIMULATION));
                fw.write(generateQuit());
            }

            fw.flush();
            fw.close();

            File utilsFile = new File(ProjectStructure.getNeuronUtilsFile());

            GeneralUtils.copyFileIntoDir(utilsFile, dirForNeuronFiles);

        }
        catch (IOException ex)
        {

            try
            {
                fw.close();
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
                "\nEnsure the NEURON files you are trying to generate are not currently being used");
        }
        logger.logComment("... Created Main hoc file: " + mainHocFile);
        //generatedRunMode = runMode;
        this.hocFileGenerated = true;
        return;

    }


    /** @todo Put option on NEURON frame for this... */

    private void removeAllPreviousFiles()
    {
        cellTemplatesGenAndIncluded.removeAllElements();
        cellMechFilesGenAndIncl.removeAllElements();

        File hocFileDir = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(hocFileDir, false, true);

    }

    public ArrayList<String> getGeneratedSimReferences()
    {
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
        StringBuffer response = new StringBuffer();
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
        StringBuffer response = new StringBuffer();
        response.append("load_file(\"nrngui.hoc\")" + "\n\n");
        return response.toString();
    }

    private String generateNeuronCodeBlock(NativeCodeLocation ncl)
    {
        StringBuffer response = new StringBuffer();

        String text = project.neuronSettings.getNativeBlock(ncl);


        text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());

        logger.logComment("Cleaned up to: "+ text);


        if (text == null || text.trim().length() == 0)
        {
            return "";
        }
        else
        {
            addComment(response, "Hoc commands to run at location: " + ncl.toString());
            response.append(text + "\n");
            addComment(response, "End of hoc commands to run at location: " + ncl.toString());

            return response.toString();
        }
    }

    private String generateInitHandlers()
    {
        StringBuffer response = new StringBuffer();

        NativeCodeLocation[] neuronFInitNcls = new NativeCodeLocation[]
            {
            NativeCodeLocation.BEFORE_INITIAL,
            NativeCodeLocation.AFTER_INITIAL,
            NativeCodeLocation.BEFORE_FINITIALIZE_RETURNS,
            NativeCodeLocation.START_FINITIALIZE};

        StringBuffer nativeBlocks = new StringBuffer();

        for (int i = 0; i < neuronFInitNcls.length; i++)
        {
            String text = project.neuronSettings.getNativeBlock(neuronFInitNcls[i]);

            text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());
            logger.logComment("Cleaned up to: "+ text);


            if (text != null && text.trim().length() > 0)
            {
                int ref = neuronFInitNcls[i].getPositionReference();
                String objName = "fih_" + ref;
                String procName = "callfi" + ref;
                addComment(nativeBlocks, "Hoc commands to run at location: " + neuronFInitNcls[i].toString());
                nativeBlocks.append("objref " + objName + "\n");
                nativeBlocks.append(objName + " = new FInitializeHandler(" + neuronFInitNcls[i].getPositionReference() +
                                ", \"" +
                                procName + "()\")" + "\n");
                nativeBlocks.append("proc " + procName + "() {" + "\n");
                nativeBlocks.append(text + "\n");
                nativeBlocks.append("}" + "\n");

                addComment(nativeBlocks, "End of hoc commands to run at location: " + neuronFInitNcls[i].toString());

            }

        }

        if (nativeBlocks.length()>0)
        {
            addMajorComment(response, "Adding blocks of native NEURON code");
            response.append(nativeBlocks.toString());
        }


        return response.toString();
    }


    private String generateInitialParameters(int runMode)
    {
        StringBuffer response = new StringBuffer();

        addMajorComment(response, "Setting initial parameters");

        response.append("strdef simConfig\n");
        response.append("simConfig = \""+this.simConfig.getName()+"\"\n");

        response.append("celsius = " + project.simulationParameters.getTemperature() + "\n\n");

        response.append("proc initialiseValues() {\n\n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

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

                addComment(response, "Setting initial vals in cell group: " + cellGroupName
                           + " which has " + cellGroupPositions.size() + " cells");

                for (int cellIndex = 0; cellIndex < cellGroupPositions.size(); cellIndex++)
                {
                    PositionRecord posRecord
                        = (PositionRecord) cellGroupPositions.get(cellIndex);

                    if (cell.getInitialPotential().getDistributionType() != NumberGenerator.FIXED_NUM)
                    {
                        double initVolt = UnitConverter.getVoltage(cell.getInitialPotential().getNextNumber(),
                                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                   UnitConverter.NEURON_UNITS);
                        addComment(response,
                                   "Giving cell " + posRecord.cellNumber + " an initial potential of: " + initVolt+" based on: "+ cell.getInitialPotential().toString());

                        if (runMode==RUN_PARALLEL) response.append("  if(pnm.gid_exists(getGid(\""+cellGroupName+"\", "
                                                                        + posRecord.cellNumber + "))) {\n");

                        response.append("    forsec " + nameOfArrayOfTheseCells + "[" + posRecord.cellNumber + "].all {\n");
                        response.append("        v = " + initVolt + "\n");
                        response.append("    }\n\n");
                        if (runMode==RUN_PARALLEL) response.append("  }\n\n");
                    }

                    //Point3f point = new Point3f(posRecord.x_pos, posRecord.y_pos, posRecord.z_pos);

                }

                if (cell.getInitialPotential().getDistributionType() == NumberGenerator.FIXED_NUM &&
                    cellGroupPositions.size() > 0)
                {

                    double initVolt = UnitConverter.getVoltage(cell.getInitialPotential().getNextNumber(),
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               UnitConverter.NEURON_UNITS);

                    addComment(response, "Giving all cells an initial potential of: " + initVolt);

                    response.append("    for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");
                    response.append("        ");

                        if (runMode==RUN_PARALLEL) response.append("if(pnm.gid_exists(getGid(\""+cellGroupName
                                                                        +"\", i))) ");

                        response.append("forsec " + nameOfArrayOfTheseCells + "[i].all "
                                    + " v = " + initVolt + "\n\n");
                    response.append("    }" + "\n\n");

                }

                response.append("\n");
            }
        }

        response.append("}\n\n");

        response.append("objref fih\n");
        response.append("fih = new FInitializeHandler(0, \"initialiseValues()\")\n\n\n");

        return response.toString();
    }

    private String generateAccess(int runMode)
    {
        StringBuffer response = new StringBuffer();
        response.append("\n");

        if (runMode == RUN_PARALLEL)
        {
            response.append("test_gid = 0\n");
            response.append("while (test_gid < ncell) {\n");
            response.append("    if (pnm.gid_exists(test_gid)) {\n");
            if (addComments)
                response.append("        //print \"Setting access on host \", host, \", host id \", hostid, \", to cell gid: \", test_gid, \":\"\n");
            response.append("        objectvar accessCell\n");
            response.append("        //print pnm.pc.gid2cell(test_gid).Soma\n");
            response.append("        test_gid = ncell\n");
            response.append("    } else {\n");
            response.append("        test_gid = test_gid + 1\n");
            response.append("    } \n");
            response.append("}\n");

            return response.toString();
        };


        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        if (cellGroupNames.size() == 0)
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
        StringBuffer response = new StringBuffer();

        addComment(response, "Initializes random-number generator");
        response.append("use_mcell_ran4(1)\n");
        response.append("mcell_ran4_init(" + this.randomSeed + ")\n");
        return response.toString();

    }

    private String getHostname()
    {
        StringBuffer response = new StringBuffer();

        if (this.savingHostname()) // temporarily disabled for win, will it ever be needed?
        {
            addComment(response, "Getting hostname");
    
            response.append("objref strFuncs\n");
            response.append("strFuncs = new StringFunctions()\n");
    
            response.append("strdef host\n");
    
            if (GeneralUtils.isWindowsBasedPlatform())
                response.append("system(\"C:/WINDOWS/SYSTEM32/hostname.exe\", host)\n");
            else
                response.append("system(\"hostname\", host)\n");
    
            response.append("strFuncs.left(host, strFuncs.len(host)-1)\n\n");
        }
        
        return response.toString();
    }



    private String initialiseParallel()
    {
        StringBuffer response = new StringBuffer();

        addMajorComment(response, "Initialising parallelization");


        response.append("ncell = " + project.generatedCellPositions.getNumberInAllCellGroups() + "\n\n");

        addComment(response, "Parallel NEURON setup");

        response.append("load_file(\"netparmpi.hoc\")\n");
        response.append("objref pnm\n");
        response.append("pnm = new ParallelNetManager(ncell)\n\n");

        response.append("hostid = pnm.pc.id\n\n");

        if (addComments) response.append("print \"Set up ParallelNetManager managing \",ncell,\" cells in total on: \", host, \" with id: \", hostid\n");


        //response.append("pnm.round_robin()\n");

        response.append("\n");

        return response.toString();

    }


    private String associateCellsWithNodes()
    {
        StringBuffer response = new StringBuffer();

        addMajorComment(response, "Associating cells with nodes");

        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        int totalProcs = GeneralProperties.getMpiSettings().getMpiConfigurations().get(1).getTotalNumProcessors();


        response.append("func getGid() {\n\n");

        int currentGid = 0;

        for (int cellGroupIndex = 0; cellGroupIndex < cellGroupNames.size(); cellGroupIndex++)
        {
            String cellGroupName = cellGroupNames.get(cellGroupIndex);
            response.append("    if (strcmp($s1,\""+cellGroupName+"\")==0) {\n");

            addComment(response, "There are " + project.generatedCellPositions.getNumberInCellGroup(cellGroupName)
                            + " cells in this Cell Group", "        ", false);

            response.append("        gid = "+currentGid+" + $2\n");
            currentGid+=project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
            response.append("    }\n\n");
        }


        response.append("    return gid\n");
        response.append("}\n\n");



        Random r = new Random();


        for (int cellGroupIndex = 0; cellGroupIndex < cellGroupNames.size(); cellGroupIndex++)
        {
            String cellGroupName = cellGroupNames.get(cellGroupIndex);
            int numHere = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);


            for (int cellNum = 0; cellNum < numHere; cellNum++)
            {
                int nodeID = r.nextInt(totalProcs);

                response.append("pnm.set_gid2node(getGid(\"" + cellGroupName + "\", " + cellNum + "), " + nodeID + ")\n");
                currentGid++;
            }
        }


        response.append("\n");

        return response.toString();

    }
 /*
    private String runworkerCutoff()
    {

        StringBuffer response = new StringBuffer();

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
        StringBuffer response = new StringBuffer();

        addComment(response, "Shutting down parallelisation");

        ///response.append("forall psection()\n");
        response.append("\n");
        response.append("\n");
        response.append("\n");


        response.append("pnm.pc.done\n");
       // response.append("quit()\n");



        return response.toString();

    }

    public long getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    private String generateWelcomeComments()
    {
        StringBuffer response = new StringBuffer();
        if (!project.neuronSettings.isGenerateComments()) return "";

        response.append("print \"\"\n");
        response.append("print \"*****************************************************\"\n");
        response.append("print \"\"\n");
        response.append("print \"    neuroConstruct generated NEURON simulation \"\n");
        response.append("print \"    for project: " + project.getProjectFile().getAbsolutePath() + " \"\n");

        response.append("print \"\"\n");



        String desc = new String();

        if (project.getProjectDescription() != null) desc = project.getProjectDescription();

        desc = GeneralUtils.replaceAllTokens(desc, "\n", " ");
        desc = GeneralUtils.replaceAllTokens(desc, "\"", "");

        response.append("print \"    Description: " + desc + "\"\n");

        response.append("print \" \"\n");
        response.append("print  \"*****************************************************\"\n\n");
        return response.toString();
    };

    private String generateIncludes()
    {
        StringBuffer response = new StringBuffer();

        response.append("objectvar allCells\n");
        response.append("allCells = new List()\n\n");

        addComment(response, "Including neuroConstruct utilities file ");
        response.append("load_file(\"nCtools.hoc\")\n");

        return response.toString();
    }

    private String generateRunSettings(int runMode)
    {
        StringBuffer response = new StringBuffer();
        addMajorComment(response, "Settings for running the demo");
        response.append("\n");
        response.append("tstop = " + getSimDuration() + "\n");

        /** @todo See why this is necessary (dt = 0.1 etc wasn't enough...) */
        response.append("dt = " + project.simulationParameters.getDt() + "\n");
        //response.append("steps_per_ms = " + Math.round(1d / (double) project.simulationParameters.getDt()) + "\n");
        response.append("steps_per_ms = " + 1d / (double) project.simulationParameters.getDt() + "\n");

        if (runMode == RUN_PARALLEL)
        {
            response.append("pnm.set_maxstep(5)\n\n");
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

    private String generateStimulations(int runMode) throws NeuronException
    {
        int totalStims = project.generatedElecInputs.getNumberSingleInputs();

        StringBuffer response = new StringBuffer(totalStims*800);  // initial cap

        ArrayList<String> allStims = this.simConfig.getInputs();

        addMajorComment(response, "Adding " + allStims.size() + " stimulation(s)");

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

                    float fractionAlongSegment = nextInput.getFractionAlong();

                    float fractionAlongSection
                        = CellTopologyHelper.getFractionAlongSection(stimCell,
                                                                     segToStim,
                                                                     fractionAlongSegment); // assume centre of segment...

                    if (nextInput.getElectricalInputType().equals(IClamp.TYPE))
                    {
                        String stimObjectName = "CurrentClampExt";
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

                        String stimName = getStimArrayName(allStims.get(k));

                        IClampSettings iClamp = (IClampSettings) project.elecInputInfo.getStim(allStims.get(k));

                        logger.logComment("Adding stim: " + nextInput);

                        if (j == 0) // define array...
                        {
                            String sizeName = getStimArraySizeName(allStims.get(k));
                            response.append(sizeName + " = " + allInputLocs.size() + "\n");
                            response.append("objectvar " + stimName + "[" + sizeName + "]\n\n");
                        }

                        String prefix = "";
                        String post = "";

                        if (runMode==RUN_PARALLEL)
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (pnm.gid_exists(getGid(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + "))) {\n");
                        }

                        addComment(response, "Note: the stimulation was specified as being at a point "
                                          + fractionAlongSegment + " along segment: " + segToStim.getSegmentName(),prefix, false);
                        addComment(response, "in section: " + getHocSectionName(segToStim.getSection().getSectionName()) +
                                          ". For NEURON, this translates to a point " + fractionAlongSection +
                                          " along section: " +
                                          getHocSectionName(segToStim.getSection().getSectionName()),prefix,true);

                        response.append(prefix+"a_" + nextInput.getCellGroup()
                                        + "[" + nextInput.getCellNumber() + "]"
                                        + "." + getHocSectionName(segToStim.getSection().getSectionName()) + " {\n");

                        response.append(prefix+"    "+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection +
                                        ")\n");

                        response.append(prefix+"    "+stimName + "[" + j + "].del = " + iClamp.getDelay().getStart() + "\n");
                        response.append(prefix+"    "+stimName + "[" + j + "].dur = " + iClamp.getDuration().getStart() + "\n");
                        response.append(prefix+"    "+stimName + "[" + j + "].amp = " + iClamp.getAmplitude().getStart() + "\n");

                        int repeat = iClamp.isRepeat() ? 1:0;

                        response.append(prefix+"    "+stimName + "[" + j + "].repeat = " + repeat + "\n");

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
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");
                         */



                        String prefix = "";
                        String post = "";

                        if (runMode==RUN_PARALLEL)
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (pnm.gid_exists(getGid(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + "))) {\n");
                        }

                        response.append(prefix+"access "
                                        + "a_" + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");

                        response.append(prefix+stimName + "[" + j + "] = new NetStim(" +
                                        fractionAlongSection + ")\n");

                        /** @todo This is wrong!!! */
                        float expectedRate = rndTrain.getRate().getNextNumber();

                        addComment(response,
                                          "NOTE: This is a very rough way to get an average rate of " + expectedRate +
                                          " kHz!!!", prefix, false);

                        float expectedNumber = getSimDuration()
                            * expectedRate
                            * increaseFactor; // no units...

                        double interval = UnitConverter.getTime(1f / expectedRate,
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

                        addComment(response, " Inserts synapse 0.5 of way down",prefix, true);

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

                        if (runMode==RUN_PARALLEL)
                        {
                            prefix = "    ";
                            post = "}" + "\n";
                            response.append("if (pnm.gid_exists(getGid(\""
                                            + nextInput.getCellGroup() + "\", "
                                            + nextInput.getCellNumber() + "))) {\n");
                        }

                        response.append(prefix+"access "
                                        + "a_" + nextInput.getCellGroup()
                                        + "["
                                        + nextInput.getCellNumber()
                                        + "]." + getHocSectionName(segToStim.getSection().getSectionName()) + " \n");

                        response.append(prefix+stimName + "[" + j + "] = new "+stimObjectName+"(" +
                                        fractionAlongSection + ")\n");

                        /** @todo This is wrong!!! */
                        float expectedRate = rndTrainExt.getRate().getNextNumber();

                        addComment(response,
                                          "NOTE: This is a very rough way to get an average rate of " + expectedRate +
                                          " kHz!!!", prefix, false);

                        float expectedNumber = getSimDuration()
                            * expectedRate
                            * increaseFactor; // no units...

                        double interval = UnitConverter.getTime(1f / expectedRate,
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                UnitConverter.NEURON_UNITS);

                        response.append(prefix+stimName + "[" + j + "].number = " + expectedNumber +
                                        "\n");
                        response.append(prefix+stimName + "[" + j + "].interval = " + interval + "\n");

                        response.append(prefix+stimName + "[" + j + "].noise = " + noise + " \n");
                        response.append(prefix+stimName + "[" + j + "].del = "+ rndTrainExt.getDelay() +" \n");
                        response.append(prefix+stimName + "[" + j + "].dur = "+ rndTrainExt.getDuration() +" \n");

                        int repeat = rndTrainExt.isRepeat() ? 1:0;

                        response.append(prefix+stimName + "[" + j + "].repeat = "+ repeat +" \n");

                        response.append(prefix+synapseName + "[" + j + "] = new " +
                                        rndTrainExt.getSynapseType() +
                                        "(" + fractionAlongSection +
                                        ") \n");

                        addComment(response, " Inserts synapse 0.5 of way down",prefix, true);

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
                addComment(response, "No electrical inputs generated for: " + allStims.get(k));
            }
        }

        /*
                response.append("\n");

                //Vector allStims = project.elecInputInfo.getAllStims();


                ArrayList<String> allStims = simConfig.getInputs();

                addMajorHocFileComment(response, "Adding " + allStims.size() + " stimulation(s)");

                for (int i = 0; i < allStims.size(); i++)
                {
                    StimulationSettings nextStim = project.elecInputInfo.getStim(allStims.get(i));

                    if (!project.cellGroupsInfo.getAllCellGroupNames().contains(nextStim.cellGroup))
                    {
         throw new NeuronException("The Cell Group specified for the Stimulation: "+ nextStim.getReference()+
                                                   " does not exist!");
                    }


                    String stimCellType = project.cellGroupsInfo.getCellType(nextStim.cellGroup);
                    Cell stimCell = project.cellManager.getCell(stimCellType);
                    //Section somaSection = (Section) stimCell.getAllSections().elementAt(0);

                    //Vector segments = stimCell.getAllSegments();
                    Segment segToStim = stimCell.getSegmentWithId(nextStim.segmentID);

         ArrayList cellGroupPositions =  project.generatedCellPositions.getPositionRecords(nextStim.cellGroup);

                    if (cellGroupPositions.size() == 0)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "The Cell Group which is to be stimulated: " + nextStim.cellGroup +
                                                  " has no cells!!\n"
         + "Ensure the Cell Type specified can be packed into the associated region.", null, null);
                        //return "";
                    }
                    else
                    {

                        float fractionAlongSegment = nextStim.getFractionAlong();

                        float fractionAlongSection
                            = CellTopologyHelper.getFractionAlongSection(stimCell,
                                                                         segToStim,
         fractionAlongSegment); // assume centre of segment...

                        addHocFileComment(response, "Note: the stimulation was specified as being at a point "
                                          + fractionAlongSegment + " along segment: " + segToStim.getSegmentName());
                        addHocFileComment(response, "in section: " + getHocSectionName(segToStim.getSection().getSectionName() +
                                          ". For NEURON, this translates to a point " + fractionAlongSection +
                                          " along section: " +
                                          getHocSectionName(segToStim.getSection().getSectionName());

                        int singleCellToStim = -1;
                        float percentage = -1;

                        try
                        {
                            if (nextStim.getCellNumberString().equals("*"))
                            {
                                percentage = 100;
                            }
                            else if (nextStim.getCellNumberString().endsWith("%"))
                            {
                                percentage
                                    = Float.parseFloat(nextStim.getCellNumberString().substring(0,
                                    nextStim.getCellNumberString().length() - 1));
                            }
                            else
                            {

                                singleCellToStim = Integer.parseInt(nextStim.getCellNumberString());

                            }
                        }
                        catch (NumberFormatException ex)
                        {
                            GuiUtils.showErrorMessage(logger,
                                                      "Unable to determine cell number to stimulate: " +
                                                      nextStim.getCellNumberString(),
                                                      ex, null);
                            return "";
                        }

                        int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(nextStim.cellGroup);

                        if (nextStim instanceof IClampSettings)
                        {

                            IClampSettings icStim = (IClampSettings) nextStim;

                            if (singleCellToStim >= 0)
                            {
                                String stimName = "stim_" + nextStim.getReference();

                                response.append("objectvar " + stimName + "\n\n");
                                response.append("a_" + icStim.cellGroup
                                                + "[" + singleCellToStim + "]"
                                                + "." + segToStim.getSection().getSectionName() + " {\n");

                                response.append(stimName + " = new IClamp(" + fractionAlongSection + ")\n");
                                response.append(stimName + ".del = " + icStim.getDelay() + "\n");
                                response.append(stimName + ".dur = " + icStim.getDuration() + "\n");
                                response.append(stimName + ".amp = " + icStim.getAmplitude() + "\n");
                                response.append("}" + "\n\n");
                            }
                            else
                            {

                                if (percentage > 0)
                                {
                                    if (percentage > 100) percentage = 100;
         int numToStimulate = (int) Math.floor( ( ( (float) numInCellGroup * percentage) / 100) +
                                                                          0.5);
                                    if (numToStimulate == 0) numToStimulate = 1;

         logger.logComment("Going to add stim to " + numToStimulate + " out of " + numInCellGroup +
                                                      " cells");

                                    String stimName = "stim_" + nextStim.getReference();

                                    response.append("objectvar " + stimName + "[" + numToStimulate + "]\n\n");

                                    Vector usedCellNums = new Vector();

                                    int numAlreadyUsed = 0;
                                    while (numAlreadyUsed < numToStimulate)
                                    {
                                        int nextCellNum = ProjectManager.getRandomGenerator().nextInt(numInCellGroup);
                                        if (!usedCellNums.contains(new Integer(nextCellNum)))
                                        {
                                            logger.logComment("Adding stim to cell " + nextCellNum);
                                            response.append("a_" + icStim.cellGroup
                                                            + "[" + nextCellNum + "]"
                                                            + "." + getHocSectionName(segToStim.getSection().getSectionName() + " {\n");
                                            response.append(stimName + "[" + numAlreadyUsed + "] = new IClamp(" +
                                                            fractionAlongSection +
                                                            ")\n");
         response.append(stimName + "[" + numAlreadyUsed + "].del = " + icStim.getDelay() + "\n");
         response.append(stimName + "[" + numAlreadyUsed + "].dur = " + icStim.getDuration() +
                                                            "\n");
         response.append(stimName + "[" + numAlreadyUsed + "].amp = " + icStim.getAmplitude() +
                                                            "\n");
                                            response.append("}" + "\n\n");

                                            usedCellNums.add(new Integer(nextCellNum));
                                            numAlreadyUsed++;
                                        }
                                    }
                                }

                            }
                        }
                        else if (nextStim instanceof RandomSpikeTrainSettings)
                        {
                            // to make the NetStim more randomish...
                            int increaseFactor = 100;
                            float noise = 1f;

                            RandomSpikeTrainSettings randStim = (RandomSpikeTrainSettings) nextStim;

                            if (singleCellToStim >= 0)
                            {
                                response.append("access a_" + randStim.cellGroup + "[" + singleCellToStim +
                                                "]." + getHocSectionName(segToStim.getSection().getSectionName() + " \n");

                                String stimName = "spikesource_" + nextStim.getReference();
                                String synapseName = "synapse_" + nextStim.getReference();
                                String connectionName = "connection_" + nextStim.getReference();

                                response.append("objref " + stimName + "\n");
                                response.append(stimName + " = new NetStim(" + fractionAlongSection + ")\n");

                                addHocFileComment(response,
         "NOTE: this is just a very rough way to get an average rate of randStim.rate!!!");

                                float expectedNumber = getSimDuration()
         * randStim.getRate()
         * increaseFactor; // no units...

                                double interval = UnitConverter.getTime(1f / randStim.getRate(),
                                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                        UnitConverter.NEURON_UNITS);

                                response.append(stimName + ".number = " + expectedNumber + "\n");
                                response.append(stimName + ".interval = " + interval + "\n");

                                response.append(stimName + ".noise = " + noise + " \n");
                                response.append(stimName + ".start = 0 \n");

                                response.append("objref " + synapseName + "\n");

                                response.append(synapseName + " = new " + randStim.getSynapseType() + "(" +
                                                fractionAlongSection +
                                                ") ");

                                addHocFileComment(response, "Inserts new synapse 0.5 of way down\n");

                                response.append("objref " + connectionName + "\n");
                                response.append("thresh = -20\n");
                                response.append("delay = 0\n");
                                response.append("weight = 1\n");
                                response.append(
                                    connectionName + " = new NetCon(" + stimName + ", " + synapseName +
                                    ", thresh, delay, weight)\n");
                                response.append("\n\n");
                            }
                            else
                            {
                                if (percentage > 0)
                                {
                                    if (percentage > 100) percentage = 100;
         int numToStimulate = (int) Math.floor( ( ( (float) numInCellGroup * percentage) / 100) +
                                                                          0.5);
                                    if (numToStimulate == 0) numToStimulate = 1;

         logger.logComment("Going to add stim to " + numToStimulate + " out of " + numInCellGroup +
                                                      " cells");

                                    String stimName = "spikesource_" + nextStim.getReference();
                                    String synapseName = "synapse_" + nextStim.getReference();
                                    String connectionName = "connection_" + nextStim.getReference();

                                    response.append("objref " + stimName + "[" + numToStimulate + "]\n\n");
                                    response.append("objref " + synapseName + "[" + numToStimulate + "]\n");
                                    response.append("objref " + connectionName + "[" + numToStimulate + "]\n");
                                    response.append("thresh = -20\n");
                                    response.append("delay = 0\n");
                                    response.append("weight = 1\n\n");

                                    Vector usedCellNums = new Vector();

                                    int numAlreadyUsed = 0;
                                    while (numAlreadyUsed < numToStimulate)
                                    {
                                        int nextCellNum = ProjectManager.getRandomGenerator().nextInt(numInCellGroup);
                                        if (!usedCellNums.contains(new Integer(nextCellNum)))
                                        {
                                            logger.logComment("Adding stim to cell " + nextCellNum);

                                            response.append("access a_"
                                                            + randStim.cellGroup
                                                            + "["
                                                            + nextCellNum
                                                            + "]." + getHocSectionName(segToStim.getSection().getSectionName() + " \n");

                                            response.append(stimName + "[" + numAlreadyUsed + "] = new NetStim(" +
                                                            fractionAlongSection + ")\n");

                                            addHocFileComment(response,
         "NOTE: this is just a very rough way to get an average rate of randStim.rate!!!");

                                            float expectedNumber = getSimDuration()
         * randStim.getRate()
         * increaseFactor; // no units...

                                            double interval = UnitConverter.getTime(1f / randStim.getRate(),
                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                UnitConverter.NEURON_UNITS);

         response.append(stimName + "[" + numAlreadyUsed + "].number = " + expectedNumber +
                                                            "\n");
         response.append(stimName + "[" + numAlreadyUsed + "].interval = " + interval + "\n");

         response.append(stimName + "[" + numAlreadyUsed + "].noise = " + noise + " \n");
                                            response.append(stimName + "[" + numAlreadyUsed + "].start = 0 \n");

                                            response.append(synapseName + "[" + numAlreadyUsed + "] = new " +
                                                            randStim.getSynapseType() +
                                                            "(" + fractionAlongSection +
                                                            ") ");

                                            addHocFileComment(response, " Inserts synapse 0.5 of way down\n");

                                            response.append(connectionName + "["
                                                            + numAlreadyUsed
                                                            + "] = new NetCon("
                                                            + stimName + "["
                                                            + numAlreadyUsed +
                                                            "], " + synapseName + "["
                                                            + numAlreadyUsed +
                                                            "], thresh, delay, weight)\n");

                                            response.append("\n\n");

                                            usedCellNums.add(new Integer(nextCellNum));
                                            numAlreadyUsed++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
         */
        return response.toString();
    }

    private String generateMultiRunPreScript()
    {
        StringBuffer response = new StringBuffer();

        response.append(multiRunManager.getMultiRunPreScript(SimEnvHelper.NEURON));

        return response.toString();
    }

    private String generateMultiRunPostScript()
    {
        StringBuffer response = new StringBuffer();

        response.append(multiRunManager.getMultiRunPostScript(SimEnvHelper.NEURON));
        /*
                for (String nextLoop: multiRunLoops)
                {
                    this.addHocFileComment( response,"End of loop for: "+nextLoop);
                    response.append("}\n\n");
                }*/

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

    public static String getHocFriendlyFilename(String filename)
    {
        logger.logComment("filename: " + filename);
        filename = GeneralUtils.replaceAllTokens(filename, "\\", "/");

        filename = GeneralUtils.replaceAllTokens(filename,
                                                 "Program Files",
                                                 "Progra~1");

        filename = GeneralUtils.replaceAllTokens(filename,
                                                 "Documents and Settings",
                                                 "Docume~1");

        if (GeneralUtils.isWindowsBasedPlatform())
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
    private String generateNeuronSimulationRecording(int runMode)
    {
        StringBuffer response = new StringBuffer();

        response.append("\n");

        int numStepsTotal = (int) Math.round(getSimDuration() / project.simulationParameters.getDt()) + 1;

        addMajorComment(response,
                        "This will run a full simulation of " + numStepsTotal +
                        " steps when the hoc file is executed");

        ArrayList<PlotSaveDetails> recordings = project.generatedPlotSaves.getSavedPlotSaves();

        addComment(response, "Recording " + recordings.size() + " variable(s)");

        boolean recordingSomething = !recordings.isEmpty();


        response.append("objref v_time\n");
        response.append("objref f_time\n");
        response.append("objref propsFile\n\n");

        if (recordingSomething)
        {
            String prefix = "";
            String post = "";

            if (runMode == RUN_PARALLEL)
            {
                prefix = "    ";
                post = "}" + "\n";

                response.append("if (hostid == 0) {\n");
            }

            response.append(prefix+"v_time = new Vector()\n");
            response.append(prefix+"v_time.record(&t)\n");
            response.append(prefix+"v_time.resize(" + numStepsTotal + ")\n");

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
                addComment(response, record.getDescription(true));

                for (Integer segId : record.segIdsToPlot)
                {
                    Segment segToRecord = cell.getSegmentWithId(segId);

                    float lenAlongSection
                        = CellTopologyHelper.getFractionAlongSection(cell,
                                                                     segToRecord,
                                                                     0.5f);

                    if (record.allCellsInGroup && !record.simPlot.isSynapticMechanism())
                    {
                        addComment(response,
                                   "Creating vector for segment: " + segToRecord.getSegmentName() + "(ID: " +
                                   segToRecord.getSegmentId() + ")");

                        String objName = this.getObjectName(record, -1, getHocSegmentName(segToRecord.getSegmentName()));

                        if (isSpikeRecording) objName = objName + "_spike";

                        String vectorObj = "v_" + objName;
                        String fileObj = "f_" + objName;
                        String apCountObj = "apc_" + objName;

                        response.append("objref " + vectorObj + "[" + numInCellGroup + "]\n");
                        if (isSpikeRecording) response.append("objref " + apCountObj + "[" + numInCellGroup + "]\n");

                        response.append("for i=0, " + (numInCellGroup - 1) + " {\n");

                        String prefix = "";
                        String post = "";

                        if (runMode==RUN_PARALLEL)
                        {
                            prefix = "    ";
                            post = "    }" + "\n";
                            response.append("    if (pnm.gid_exists(getGid(\""
                                            + cellGroupName + "\", i))) {\n");
                        }


                        response.append(prefix+"    " + vectorObj + "[i] = new Vector()\n");

                        if (!isSpikeRecording)
                        {
                            response.append(prefix+"    " + vectorObj + "[i].record(&a_" + cellGroupName + "[i]"
                                            + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + "." + whatToRecord + "(" +
                                            lenAlongSection +
                                            "))\n");
                            response.append(prefix+"    " + vectorObj + "[i].resize(" + numStepsTotal + ")\n");
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

                                    String vectorObj = "v_" + synObjName+"_"+neuronVar;
                                    String fileObj = "f_" + synObjName+"_"+neuronVar;

                                    response.append("objref " + vectorObj + "\n");

                                    String prefix = "";
                                    String post = "";

                                    if (runMode == RUN_PARALLEL)
                                    {
                                        prefix = "    ";
                                        post = "}" + "\n";
                                        response.append("if (pnm.gid_exists(getGid(\""
                                                        + cellGroupName + "\", " + cellNum + "))) {\n");
                                    }

                                    response.append(prefix + vectorObj + " = new Vector()\n");

                                    response.append(prefix + vectorObj + ".record(&"+var+")\n");

                                    response.append(prefix + vectorObj + ".resize(" + numStepsTotal + ")\n");

                                    response.append(post);
                                    response.append("objref " + fileObj + "\n");
                                    response.append("\n");
                                }
                            }
                            else
                            {

                                addComment(response,
                                           "Creating vector for segment: " + segToRecord.getSegmentName() +
                                           "(ID: " + segToRecord.getSegmentId() + ") in cell number: " + cellNum);

                                String objName = this.getObjectName(record, cellNum, getHocSegmentName(segToRecord.getSegmentName()));

                                if (isSpikeRecording) objName = objName + "_spike";

                                String vectorObj = "v_" + objName;
                                String fileObj = "f_" + objName;
                                String apCountObj = "apc_" + objName;

                                response.append("objref " + vectorObj + "\n");

                                String prefix = "";
                                String post = "";

                                if (runMode == RUN_PARALLEL)
                                {
                                    prefix = "    ";
                                    post = "}" + "\n";
                                    response.append("if (pnm.gid_exists(getGid(\""
                                                    + cellGroupName + "\", " + cellNum + "))) {\n");
                                }

                                if (isSpikeRecording) response.append(prefix + "objref " + apCountObj + "\n");

                                response.append(prefix + vectorObj + " = new Vector()\n");

                                if (!isSpikeRecording)
                                {
                                    response.append(prefix + vectorObj + ".record(&a_" + cellGroupName + "[" + cellNum +
                                                    "]"
                                                    + "." + getHocSectionName(segToRecord.getSection().getSectionName()) + "." +
                                                    whatToRecord + "(" + lenAlongSection + "))\n");

                                    response.append(prefix + vectorObj + ".resize(" + numStepsTotal + ")\n");
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

        String hocFriendlyDirName = getHocFriendlyFilename(dataFileDirName);

        response.append("\n\nstrdef simsDir\n");
        response.append("simsDir = \"" + hocFriendlyDirName + "\"\n\n");

        response.append("strdef simReference\n");
        response.append("simReference = \"" + project.simulationParameters.getReference() + "\"\n\n");

        response.append("strdef targetDir\n");
        response.append("sprint(targetDir, \"%s%s/\", simsDir, simReference)\n\n");

        response.append(generateMultiRunPreScript());

        response.append(generateRunMechanism(runMode));

        if (recordingSomething)
        {
            response.append("print \"Storing the data...\"\n\n");
            response.append("strdef timeFilename\n");

            String prefix = "";
            String post = "";

            if (runMode == RUN_PARALLEL)
            {
                prefix = "    ";
                post = "}" + "\n";
                response.append("if (hostid == 0) {\n");
            }


            response.append(prefix+"sprint(timeFilename, \"%s%s\", targetDir, \"" + SimulationData.TIME_DATA_FILE + "\")\n");
            response.append(prefix+"f_time.wopen(timeFilename)\n");
            response.append(prefix+"v_time.printf(f_time)\n");
            response.append(prefix+"f_time.close()\n");

            response.append(post);
            response.append("\n");


            for (PlotSaveDetails record : recordings)
            {
                String cellGroupName = record.simPlot.getCellGroup();

                int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
                String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
                Cell cell = project.cellManager.getCell(cellType);

                boolean isSpikeRecording = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;

                if (numInCellGroup > 0)
                {
                    addComment(response, record.getDescription(true));

                    for (Integer segId : record.segIdsToPlot)
                    {
                        Segment segToRecord = cell.getSegmentWithId(segId);

                        if (record.allCellsInGroup && !record.simPlot.isSynapticMechanism())
                        {

                            addComment(response, "Saving vector for segment: "
                                       + segToRecord.getSegmentName() + "(ID: " + segToRecord.getSegmentId() + ")");

                            String objName = this.getObjectName(record, -1, getHocSegmentName(segToRecord.getSegmentName()));

                            if (isSpikeRecording) objName = objName + "_spike";

                            String vectObj = "v_" + objName;
                            String fileObj = "f_" + objName;

                            response.append("for i=0, " + (numInCellGroup - 1) + " {\n");

                            prefix = "";
                            post = "";

                            if (runMode == RUN_PARALLEL)
                            {
                                prefix = "    ";
                                post = "    }" + "\n";
                                response.append("    if (pnm.gid_exists(getGid(\""
                                                + cellGroupName + "\", i))) {\n");
                            }


                            response.append(prefix+"    " + fileObj + "[i] = new File()\n");
                            response.append(prefix+"    strdef filename\n");

                            String fileName = SimPlot.getFilename(record, segToRecord, "%d");

                            response.append(prefix+"    sprint(filename, \"%s" + fileName + "\", targetDir, i)\n");
                            response.append(prefix+"    " + fileObj + "[i].wopen(filename)\n");
                            response.append(prefix+"    " + vectObj + "[i].printf(" + fileObj + "[i])\n");
                            response.append(prefix+"    " + fileObj + "[i].close()\n");
                            response.append(post);
                            response.append("}\n\n");
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

                                        String vectorObj = "v_" + synObjName + "_" + neuronVar;
                                        String fileObj = "f_" + synObjName + "_" + neuronVar;

                                        prefix = "";
                                        post = "";

                                        if (runMode == RUN_PARALLEL)
                                        {
                                            prefix = "    ";
                                            post = "}" + "\n";
                                            response.append("if (pnm.gid_exists(getGid(\""
                                                            + cellGroupName + "\", " + cellNum + "))) {\n");
                                        }

                                        response.append(prefix + fileObj + " = new File()\n");
                                        response.append(prefix + "strdef filename\n");

                                        String fileName = SimPlot.getFilename(record, postSynObj, "%d");

                                        response.append(prefix + "sprint(filename, \"%s" + fileName + "\", targetDir, " +
                                                        cellNum + ")\n");

                                        response.append(prefix + fileObj + ".wopen(filename)\n");
                                        response.append(prefix + vectorObj + ".printf(" + fileObj + ")\n");
                                        response.append(prefix + fileObj + ".close()\n\n");
                                        response.append(post);

                                    }
                                }
                                else
                                {

                                    addComment(response,
                                               "Saving vector for segment: " + segToRecord.getSegmentName() +
                                               "(ID: " + segToRecord.getSegmentId() + ") in cell number: " +
                                               cellNum);

                                    String objName = this.getObjectName(record, cellNum, getHocSegmentName(segToRecord.getSegmentName()));

                                    if (isSpikeRecording) objName = objName + "_spike";

                                    String vectObj = "v_" + objName;
                                    String fileObj = "f_" + objName;

                                    prefix = "";
                                    post = "";

                                    if (runMode == RUN_PARALLEL)
                                    {
                                        prefix = "    ";
                                        post = "}" + "\n";
                                        response.append("if (pnm.gid_exists(getGid(\""
                                                        + cellGroupName + "\", " + cellNum + "))) {\n");
                                    }

                                    response.append(prefix + fileObj + " = new File()\n");
                                    response.append(prefix + "strdef filename\n");

                                    String fileName = SimPlot.getFilename(record, segToRecord, "%d");

                                    response.append(prefix + "sprint(filename, \"%s" + fileName + "\", targetDir, " +
                                                    cellNum + ")\n");
                                    response.append(prefix + fileObj + ".wopen(filename)\n");
                                    response.append(prefix + vectObj + ".printf(" + fileObj + ")\n");
                                    response.append(prefix + fileObj + ".close()\n\n");
                                    response.append(post);
                                }
                            }
                        }
                    }
                }
            }
            response.append("\n");

            prefix = "";
            post = "";

            if (runMode == RUN_PARALLEL)
            {
                prefix = "    ";
                post = "}" + "\n";

                response.append("if (hostid == 0) {\n");
            }

            response.append(prefix+"propsFile = new File()\n");
            response.append(prefix+"strdef propsFilename\n");
            response.append(prefix+"sprint(propsFilename, \"%s" + SimulationsInfo.simulatorPropsFileName + "\", targetDir)\n");
            response.append(prefix+"propsFile.wopen(propsFilename)\n");
            response.append(prefix+
                "propsFile.printf(\"#This is a list of properties generated by NEURON during the simulation run\\n\")\n");



            if (this.savingHostname()) response.append(prefix+"propsFile.printf(\"Host=%s\\n\", host)\n");
            
            response.append(prefix+"propsFile.printf(\"RealSimulationTime=%g\\n\", realtime)\n");

            response.append(generateMultiRunPostScript());
            response.append(prefix+"propsFile.close()\n");
            response.append(post);
            response.append("\n");

            response.append(prefix+"print \"Data stored in directory: \", targetDir\n\n");
        }

        return response.toString();

    }
    
    private boolean savingHostname()
    {
        // There have been some problems getting C:/WINDOWS/SYSTEM32/hostname.exe to run on win
        // so temporarily disableing it. It's only needed for parallel running of sims, which is 
        // unlikely on win for the forseeable future.
        
        if (GeneralUtils.isWindowsBasedPlatform()) return false;
        
        return true;
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



    public String generateCellGroups(int runMode) throws NeuronException
    {
        StringBuffer response = new StringBuffer();
        response.append("\n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        if (cellGroupNames.size() == 0)
        {
            logger.logComment("There are no cell groups!!");

            addMajorComment(response, "There were no cell groups specified in the project...");
            return response.toString();
        }


        GeneralUtils.timeCheck("Starting gen of cell groups");


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

                ArrayList cellMechanisms = cell.getAllChannelMechanisms(true);

                //Vector allSyns = cell.getAllAllowedSynapseTypes();

                Iterator allNetConns = project.generatedNetworkConnections.getNamesNetConns();

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


                    }
                }

                logger.logComment("------------    All cell mechs: " + cellMechanisms);

                for (int i = 0; i < cellMechanisms.size(); i++)
                {

                    CellMechanism cellMechanism = null;

                    if (cellMechanisms.get(i) instanceof String)
                        cellMechanism = project.cellMechanismInfo.getCellMechanism( (String) cellMechanisms.get(i));

                    else if (cellMechanisms.get(i) instanceof ChannelMechanism)
                    {
                        logger.logComment("Is a ChannelMechanism...");
                        ChannelMechanism nextCellMech = (ChannelMechanism) cellMechanisms.get(i);
                        cellMechanism = project.cellMechanismInfo.getCellMechanism(nextCellMech.getName());
                    }

                    if (cellMechanism == null)
                    {
                        throw new NeuronException("Problem generating file for cell mech: "
                                                  + cellMechanisms.get(i)
                                                  +
                                                  "\nPlease ensure there is an implementation for that process in NEURON");
                        //return "";
                    }

                    logger.logComment("Looking at cell process: " + cellMechanism.getInstanceName());

                    if (!cellMechFilesGenAndIncl.contains(cellMechanism.getInstanceName()))
                    {

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
                                addComments);
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

                                response.append("load_file(\"" + cellMechanism.getInstanceName() + ".hoc\")\n");

                            }
                            success = cmlMechanism.createImplementationFile(SimEnvHelper.
                                NEURON,
                                UnitConverter.NEURON_UNITS,
                                newMechFile,
                                project,
                                cmlMechanism.getSimMapping(SimEnvHelper.NEURON).isRequiresCompilation(),
                                addComments);
                        }

                        if (!success)
                        {
                            throw new NeuronException("Problem generating file for cell process: "
                                                      + cellMechanisms.get(i)
                                                      +
                                                      "\nPlease ensure there is an implementation for that process in NEURON");

                        }

                        cellMechFilesGenAndIncl.add(cellMechanism.getInstanceName());
                    }

                }

                logger.logComment("------    needsGrowthFunctionality: " + needsGrowthFunctionality(cellGroupName));

                NeuronTemplateGenerator cellTemplateGen
                    = new NeuronTemplateGenerator(project,
                                                  cell,
                                                  dirForNeuronFiles,
                                                  needsGrowthFunctionality(cellGroupName));

                String filenameToBeGenerated = cellTemplateGen.getHocFilename();

                logger.logComment("Will need a cell template file called: " +
                                  filenameToBeGenerated);

                if (cellTemplatesGenAndIncluded.contains(filenameToBeGenerated))
                {
                    logger.logComment("It's already been generated!");
                }
                else
                {
                    logger.logComment("Generating it...");
                    try
                    {
                        cellTemplateGen.generateFile();

                        cellTemplatesGenAndIncluded.add(filenameToBeGenerated);
                    }
                    catch (NeuronException ex)
                    {
                        logger.logError("Problem generating one of the template files...", ex);
                        throw ex;
                    }
                }

                logger.logComment("Adding include for the file to the main hoc file...");

                StringBuffer fileNameBuffer = new StringBuffer(filenameToBeGenerated);

                for (int j = 0; j < fileNameBuffer.length(); j++)
                {
                    char c = fileNameBuffer.charAt(j);
                    if (c == '\\')
                        fileNameBuffer.replace(j, j + 1, "/");
                }
                //response.append("load_file(\"" + fileNameBuffer.toString() + "\")\n");
                response.append("load_file(\"" + cellTemplateGen.hocFile.getName() + "\")\n\n");
                // now we've got the includes

                String currentRegionName = project.cellGroupsInfo.getRegionName(cellGroupName);

                ArrayList cellGroupPositions = project.generatedCellPositions.getPositionRecords(cellGroupName);

                addComment(response, "Adding " + cellGroupPositions.size()
                                  + " cells of type " + cellTypeName
                                  + " in region " + currentRegionName);

                String nameOfNumberOfTheseCells = "n_" + cellGroupName;
                String nameOfArrayOfTheseCells = "a_" + cellGroupName;

                if (cellGroupPositions.size() > 0)
                {
                    response.append(nameOfNumberOfTheseCells + " = " + cellGroupPositions.size() + "\n\n");

                    response.append("objectvar " + nameOfArrayOfTheseCells + "[" + nameOfNumberOfTheseCells + "]" +
                                    "\n\n");

                    if (runMode != RUN_PARALLEL)
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

                        response.append("    addCell_" + cellGroupName + "(i)" + "\n\n");

                        response.append("}" + "\n\n");
                    }
                    else
                    {

                        response.append("for i = 0, " + nameOfNumberOfTheseCells + "-1 {" + "\n");

                        //response.append("addCell_" + cellGroupName + "(i)" + "\n\n");

                        response.append("    if(pnm.gid_exists(getGid(\""+cellGroupName+"\", i))) {\n");

                        response.append("        strdef reference\n");
                        response.append("        sprint(reference, \"" + cellGroupName + "_%d\", i)\n");
                        response.append("        strdef type\n");
                        response.append("        sprint(type, \"" + cellTypeName + "\")\n");
                        response.append("        strdef description\n");
                        response.append("        sprint(description, \"" + GeneralUtils.replaceAllTokens(cell.getCellDescription(), "\n", " ") + "\")\n");

                        //response.append("        strdef command\n");
                        //response.append("        sprint(command, \"new " + cellTypeName + "(reference, type, description)\")\n");

                        response.append(
                            "        print \"Going to create cell: \", reference, \" on host \", host, \", id: \", hostid\n");

                        //response.append("    pnm.create_cell(i, command)\n");
                        response.append( "        a_"+cellGroupName+"[i] = new "+cellTypeName+"(reference, type, description)\n");


                        response.append( "        pnm.register_cell(getGid(\""+cellGroupName+"\", i), a_"+cellGroupName+"[i])\n");

                        response.append("    }\n");

                        response.append("}" + "\n\n");
                    }
                }



                    Region regionInfo = project.regionsInfo.getRegionObject(currentRegionName);
                    CellPackingAdapter packer = project.cellGroupsInfo.getCellPackingAdapter(cellGroupName);

                    //     float yDisplacementOfThisRegion = project.regionsInfo.getYDisplacementOfRegion(currentRegionName);

                    addComment(response, "Placing these cells in a region described by: " + regionInfo);
                    addComment(response, "Packing has been generated by: " + packer.toString());

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
                        if (runMode == RUN_PARALLEL)
                            parallelCheck = "if (pnm.gid_exists(getGid(\""+cellGroupName+"\", "+posRecord.cellNumber+"))) ";

                        response.append(parallelCheck+nameOfArrayOfTheseCells + "[" + posRecord.cellNumber + "].position("
                                        + posRecord.x_pos + "," + posRecord.y_pos + "," + posRecord.z_pos + ")\n");

                    }


                logger.logComment("***  Finished looking at cell group number " + ii + ", called: " + cellGroupName);

            }

            response.append("\n");

        }

            GeneralUtils.timeCheck("Finished gen of cell groups");

        return response.toString();

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
            + "_" + synDetails.getSynapseIndex();

        return objectVarName;

    }

    private String generateNetworkConnections(int runMode)
    {
        int totNetConns = project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION);

        StringBuffer response = new StringBuffer(totNetConns*1000); // initial capacity...

        response.append("\n");

        addMajorComment(response, "Adding Network Connections");


        Iterator allNetConnNames = project.generatedNetworkConnections.getNamesNetConns();

        if (!allNetConnNames.hasNext())
        {
            logger.logComment("There are no synaptic connections");
            return "";
        }

            GeneralUtils.timeCheck("Starting gen of syn conns");


        // this section calculates the number of ADDITIONAL dendritic/axonal
        // sections on each cell.
        //logger.logComment("------------    Generating extra dends etc...");

        //Hashtable cellnameVsNumExtraDends = new Hashtable();
        //Hashtable cellnameVsNumExtraAxons = new Hashtable();

        while (allNetConnNames.hasNext())
        {
            String netConnName = (String) allNetConnNames.next();
            String sourceCellGroup = null;
            String targetCellGroup = null;

            if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
            {
                sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                //growMode = project.simpleNetworkConnectionsInfo.getGrowMode(netConnName);
            }
            else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
            {
                sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
            }
            ArrayList<SingleSynapticConnection> allSynapses = project.generatedNetworkConnections.getSynapticConnections(netConnName);

            /*
            for (int i = 0; i < allSynapses.size(); i++)
            {
                GeneratedNetworkConnections.SingleSynapticConnection syn = allSynapses.get(i);

                String targetCellName = "a_"
                    + targetCellGroup
                    + "["
                    + syn.targetEndPoint.cellNumber
                    + "]";

                String sourceCellName = "a_"
                    + sourceCellGroup
                    + "["
                    + syn.sourceEndPoint.cellNumber
                    + "]";

            }*/
        }


        // refresh iterator...
        allNetConnNames = project.generatedNetworkConnections.getNamesNetConns();

        // Adding specific network connections...
        while (allNetConnNames.hasNext())
        {
            String netConnName = (String) allNetConnNames.next();

            String sourceCellGroup = null;
            String targetCellGroup = null;
            //GrowMode growMode = null;
            //SynapticProperties synProps = null;
            Vector<SynapticProperties> synPropList = null;

            if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
            {
                sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                //growMode = project.simpleNetworkConnectionsInfo.getGrowMode(netConnName);
                //synPropList = new Vector();
                //synPropList.add(project.simpleNetworkConnectionsInfo.getSynapseProperties(netConnName));
                synPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
            }

            else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
            {
                sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                //growMode = project.arbourConnectionsInfo.getGrowMode(netConnName);
                synPropList = project.volBasedConnsInfo.getSynapseList(netConnName);
            }

            String targetCellName = project.cellGroupsInfo.getCellType(targetCellGroup);
            Cell targetCell = project.cellManager.getCell(targetCellName);

            String sourceCellName = project.cellGroupsInfo.getCellType(sourceCellGroup);
            Cell sourceCell = project.cellManager.getCell(sourceCellName);

            Hashtable<Integer, SegmentLocation> substituteConnPoints
                = new Hashtable<Integer, SegmentLocation> (); // used for storing alternate connection locations
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

            response.append("\n");
            addComment(response, "Adding Network Connection: "
                              + netConnName
                              + " from Cell Group: "
                              + sourceCellGroup
                              + " to: "
                              + targetCellGroup);

            ArrayList<SingleSynapticConnection> allSynapses = project.generatedNetworkConnections.getSynapticConnections(netConnName);


            for (int singleConnIndex = 0; singleConnIndex < allSynapses.size(); singleConnIndex++)
            {
                GeneratedNetworkConnections.SingleSynapticConnection synConn = allSynapses.get(singleConnIndex);

                for (int synPropIndex = 0; synPropIndex < synPropList.size(); synPropIndex++)
                {
                    SynapticProperties synProps = (SynapticProperties) synPropList.elementAt(synPropIndex);

                    PostSynapticObject synObj = new PostSynapticObject(netConnName,
                                                         synProps.getSynapseType(),
                                                         synConn.targetEndPoint.cellNumber,
                                                         synConn.targetEndPoint.location.getSegmentId(),
                                                         singleConnIndex);


                    String objectVarName = getSynObjName(synObj);


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

                    float lengthAlongTargetSection
                        = CellTopologyHelper.getFractionAlongSection(targetCell,
                                                                     targetSegment,
                                                                     synConn.targetEndPoint.location.getFractAlong());

                    Segment sourceSegment = null;
                    float fractionAlongSegment = -1;
                    int origId = synConn.sourceEndPoint.location.getSegmentId();

                    float apSegmentPropDelay = 0;

                    if (substituteConnPoints.size() == 0 || // there is no ApPropSpeed on cell
                        !substituteConnPoints.containsKey(new Integer(origId))) // none on this segment
                    {
                        sourceSegment = sourceCell.getSegmentWithId(origId);
                        fractionAlongSegment = synConn.sourceEndPoint.location.getFractAlong();
                    }
                    else
                    {
                        Segment realSource = sourceCell.getSegmentWithId(origId);

                        SegmentLocation subsSynConLoc = substituteConnPoints.get(new Integer(origId));

                        sourceSegment = sourceCell.getSegmentWithId(subsSynConLoc.getSegmentId());
                        fractionAlongSegment = subsSynConLoc.getFractAlong();

                        apSegmentPropDelay = CellTopologyHelper.getTimeToFirstExpModParent(sourceCell,
                                                                                    realSource,
                                                                                    synConn.sourceEndPoint.location.getFractAlong());

                        addComment(response,
                                   "Instead of point " + synConn.sourceEndPoint.location.getFractAlong() + " along seg: "
                                   + realSource.toShortString() + " connecting to point " +
                                   fractionAlongSegment + " along seg: "
                                   + sourceSegment.toShortString() + "");

                    }

                    logger.logComment("source segment: " + sourceSegment);

                    float fractAlongSourceSection
                        = CellTopologyHelper.getFractionAlongSection(sourceCell,
                                                                     sourceSegment,
                                                                     fractionAlongSegment);

                    float synInternalDelay = -1;
                    float weight = -1;
                    if (synConn.props==null || synConn.props.size()==0)
                    {
                        synInternalDelay = synProps.getDelayGenerator().getNominalNumber();
                        weight = synProps.getWeightsGenerator().getNominalNumber();
                    }
                    else
                    {
                        for (ConnSpecificProps prop:synConn.props)
                        {
                            if (prop.synapseType.equals(synProps.getSynapseType()))
                            {
                                synInternalDelay = prop.internalDelay;
                                weight = prop.weight;
                            }
                        }
                    }

                    float apSpaceDelay = synConn.apPropDelay;

                    addComment(response, "Connection from src cell "+synConn.sourceEndPoint.cellNumber
                               +" to tgt cell "+synConn.targetEndPoint.cellNumber+". Fract along source section: "
                               + fractAlongSourceSection+", weight of syn: " + weight , false);
                    addComment(response,
                               "Delay due to AP propagation along segments: " + apSegmentPropDelay
                               + ", delay due to AP jump pre -> post location "+ apSpaceDelay
                               + ", internal synapse delay (from Synaptic Props): " + synInternalDelay);


                    response.append("objectvar " + objectVarName + "\n\n");



                    if (runMode != RUN_PARALLEL)
                    {
                        // put synaptic start point on source axon
                        response.append("a_" + targetCellGroup
                                        + "[" + synConn.targetEndPoint.cellNumber + "]"
                                        + "."
                                        + getHocSectionName(targetSegment.getSection().getSectionName())
                                        + " "
                                        + objectVarName
                                        + " = new "
                                        + synapseType
                                        + "(" + lengthAlongTargetSection + ")\n");

                        response.append("a_" + sourceCellGroup
                                        + "[" + synConn.sourceEndPoint.cellNumber + "]"
                                        + "."
                                        + getHocSectionName(sourceSegment.getSection().getSectionName())
                                        + " "
                                        + "a_" + targetCellGroup
                                        + "[" + synConn.targetEndPoint.cellNumber + "]"
                                        + ".synlist.append(new NetCon(&v("
                                        + fractAlongSourceSection
                                        + "), "
                                        + objectVarName
                                        +
                                        ", "
                                        + threshold
                                        + ", "
                                        + (synInternalDelay + apSegmentPropDelay + apSpaceDelay)
                                        + ", "
                                        + weight
                                        + "))" /** @todo make this variable... */
                                        + "\n\n");

                        CellMechanism cm = project.cellMechanismInfo.getCellMechanism(synProps.getSynapseType());

                        if (cm instanceof AbstractedCellMechanism)
                        {
                            AbstractedCellMechanism acm = (AbstractedCellMechanism)cm;

                            try
                            {
                                if (acm.getParameter("RequiresXYZ") == 1)
                                {
                                    Point3f synRelToCell
                                        = CellTopologyHelper.convertSegmentDisplacement(targetCell,
                                        targetSegment.getSegmentId(),
                                        synConn.targetEndPoint.location.getFractAlong());

                                    Point3f posAbsSyn
                                        = project.generatedCellPositions.getOneCellPosition(targetCellGroup,
                                        synConn.targetEndPoint.cellNumber);

                                    posAbsSyn.add(synRelToCell);

                                    addComment(response, "Synapse location on cell: " + synRelToCell);
                                    addComment(response, "Synapse absolute location: " + posAbsSyn);

                                    response.append(objectVarName+".x = "+posAbsSyn.x+"\n");
                                    response.append(objectVarName+".y = "+posAbsSyn.y+"\n");
                                    response.append(objectVarName+".z = "+posAbsSyn.z+"\n\n");
                                }
                            }
                            catch (CellMechanismException ex)
                            {
                                logger.logComment("No xyz parameter: "+ex);
                            }
                        }
                    }
                    else
                    {
                        if (!sourceSegment.getSection().isSomaSection())
                        {
                            // will throw error in hoc...
                            response.append(" ... Warning, source of synapse is not soma, not supported yet!!");
                        }
                        response.append("synapse_id = -2\n");

                        response.append("if (pnm.gid_exists(getGid(\""
                                        + targetCellGroup + "\", "
                                        + synConn.targetEndPoint.cellNumber + "))) {\n");

                        // put synaptic start point on source axon
                        response.append("    a_" + targetCellGroup
                                        + "[" + synConn.targetEndPoint.cellNumber + "]"
                                        + "."
                                        + getHocSectionName(targetSegment.getSection().getSectionName())
                                        + " "
                                        + objectVarName
                                        + " = new "
                                        + synapseType
                                        + "(" + lengthAlongTargetSection + ")\n");


                        response.append("    a_" + targetCellGroup
                                        + "[" + synConn.targetEndPoint.cellNumber + "]"
                                        + ".synlist.append("
                                        + " "
                                        + objectVarName + ")\n");

                        response.append("    synapse_id = a_" + targetCellGroup
                                        + "[" + synConn.targetEndPoint.cellNumber + "]"
                                        + ".synlist.count()-1\n");

                        response.append("}\n\n");



                        response.append("pnm.nc_append("
                                        +"getGid(\""+ sourceCellGroup+"\", "+synConn.sourceEndPoint.cellNumber+ ")"
                                        +", getGid(\""+targetCellGroup+"\", "+synConn.targetEndPoint.cellNumber + ")"
                                        +", synapse_id, "
                                        + weight
                                        + ", "
                                        + (synInternalDelay + apSegmentPropDelay + apSpaceDelay)
                                        + ")"
                                        + "\n\n");

                        /** @todo threshold */
                        addComment(response, "What about threshold???");

                    }


                }
            }
        }


        //GeneralUtils.timeCheck("Finsihed gen of syn conns, totNetConns: "+totNetConns+", response len: "+response.length()+ ", ratio: "+ (float)response.length()/totNetConns);
        GeneralUtils.timeCheck("Finsihed gen of syn conns");


        return response.toString();
    }

    public String generatePlots()
    {
        StringBuffer response = new StringBuffer();

        ArrayList<PlotSaveDetails> plots = project.generatedPlotSaves.getPlottedPlotSaves();

        addMajorComment(response, "Adding " + plots.size() + " plot(s)");

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

                        ArrayList<PostSynapticObject> synObjs = project.generatedNetworkConnections.getSynObjsPresent(netConn,
                                                                      synType,
                                                                      cellNum,
                                                                      segId);

                        logger.logComment("Syn objs for: "+netConn+", "+synType+", cellNum: "+cellNum
                                          +", segId: "+segId+": " + synObjs);

                        for (PostSynapticObject synObj: synObjs)
                        {
                            title = this.getSynObjName(synObj)+"."+neuronVar;
                            varRefIncFract = title;

                            response.append(generateSinglePlot(title,
                                                               plot.simPlot.getGraphWindow(),
                                                               minVal,
                                                               maxVal,
                                                               varRefIncFract,
                                                           getNextColour()));
                        }

                    }
                    else
                    {


                        response.append(generateSinglePlot(title,
                                                           plot.simPlot.getGraphWindow(),
                                                           minVal,
                                                           maxVal,
                                                           varRefIncFract,
                                                           getNextColour()));
                    }
                }
            }

        }

        /*
                 ArrayList<String> allPlotNames = simConfig.getPlots();

                 addMajorHocFileComment(response, "Adding " + allPlotNames.size() + " plot(s)");

                 for (int j = 0; j < allPlotNames.size(); j++)
                 {
            SimPlot simPlot = project.simPlotInfo.getSimPlot(allPlotNames.get(j));

            String cellGroup = simPlot.getCellGroup();

            if (!project.cellGroupsInfo.getAllCellGroupNames().contains(cellGroup))
            {
                GuiUtils.showErrorMessage(logger, "The cell group "
                                          + cellGroup + " doesn't exist and so cannot be plotted", null, null);

            }
            else
            {
                Cell nextCell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroup));

                Vector segments = nextCell.getExplicitlyModelledSegments();

                String cellNumberString = simPlot.getCellNumber();

                Vector<Integer> cellNumsToPlot = new Vector();

                int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

                if (numInCellGroup == 0 && simPlot.toBePlotted())
                {
                    GuiUtils.showErrorMessage(logger, "There are no cells in group " + cellGroup
                                              + ", therefore no plot can be generated", null, null);
                }
                else
                {
                    float percentage = -1;

                    if (cellNumberString.endsWith("%"))
                    {
                        percentage
                            = Float.parseFloat(cellNumberString.substring(0,
                                                                          cellNumberString.length() - 1));
                    }
                    if (percentage > 100) percentage = 100;
                    if (percentage > 0)
                    {
                        int numToPlot = (int) Math.floor( ( ( (float) numInCellGroup * percentage) / 100) + 0.5);
                        if (numToPlot == 0) numToPlot = 1;

                        int numAlreadyUsed = 0;
                        while (numAlreadyUsed < numToPlot)
                        {
                            int nextCellNum = ProjectManager.getRandomGenerator().nextInt(numInCellGroup);
                            if (!cellNumsToPlot.contains(new Integer(nextCellNum)))
                            {
                                cellNumsToPlot.add(new Integer(nextCellNum));
                                numAlreadyUsed++;
                            }
                        }

                    }
                    else if (cellNumberString.equals("*"))
                    {
                        for (int i = 0; i < numInCellGroup; i++)
                        {
                            cellNumsToPlot.add(new Integer(i));
                        }
                    }
                    else
                    {
                        cellNumsToPlot.add(new Integer(cellNumberString));
                    }

                    for (int i = 0; i < cellNumsToPlot.size(); i++)
                    {
                        // NOTE: Index not Id!!
                        int startSegmentIndex;
                        int endSegmentIndex;

                        int nextCellNum = cellNumsToPlot.elementAt(i).intValue();

                        if (simPlot.getSegmentId().equals("*"))
                        {
                            startSegmentIndex = 0;
                            endSegmentIndex = nextCell.getExplicitlyModelledSegments().size() - 1;
                        }
                        else
                        {
                            startSegmentIndex = Integer.parseInt(simPlot.getSegmentId());
                            endSegmentIndex = Integer.parseInt(simPlot.getSegmentId());
                        }

                        for (int nextSegmentIndex = startSegmentIndex; nextSegmentIndex <= endSegmentIndex;
                             nextSegmentIndex++)
                        {
                            Segment seg = null;
                            if (startSegmentIndex == endSegmentIndex)
                                seg = nextCell.getSegmentWithId(nextSegmentIndex);
                            else
                                seg = (Segment) segments.elementAt(nextSegmentIndex);

                            if (seg == null)
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "A plot for segment ID " + nextSegmentIndex +
                                                          " in cell group " + cellGroup +
         " is requested, but the cell doesn't seen to have that segment", null, null);
                                return response.toString();
                            }

                            float lenAlongSegment
                                = CellTopologyHelper.getFractionAlongSection(nextCell,
                                seg,
                                0.5f);

                            String whatToPlot = simPlot.getValuePlotted();
                            float minVal = simPlot.getMinValue();
                            float maxVal = simPlot.getMaxValue();
                            float initialMin = minVal;
                            float initialMax = maxVal;

                             @todo Put in more of these...
                            if (whatToPlot.equals(SimPlot.PLOT_VOLTAGE))
                                whatToPlot = "v";

                            if (whatToPlot.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
                            {
                                String processName = whatToPlot.substring(0,
                                                                          whatToPlot.indexOf(SimPlot.
                                    PLOTTED_VALUE_SEPARATOR));

                                String variable = whatToPlot.substring(
                                    whatToPlot.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) + 1);

                                logger.logComment("--------------     Looking to plot " + variable +
                                                  " on cell process: " + processName);

                                if (variable.equals(SimPlot.PLOT_COND_DENS))
                                {
                                    variable = "gion";

                                    minVal
                                        = (float) UnitConverter.getConductanceDensity(minVal,
                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                        UnitConverter.NEURON_UNITS);
                                    maxVal
                                        = (float) UnitConverter.getConductanceDensity(maxVal,
                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                        UnitConverter.NEURON_UNITS);

                                    this.addHocFileComment(response, "Old max, min: ("
                                                           + initialMax + "," + initialMin + "), new values: ("
                                                           + maxVal + "," + minVal + ")");

                                    whatToPlot = variable + "_" + processName;

                                }
                                else
                                {
                                    CellMechanism cp = project.cellMechanismInfo.getCellMechanism(processName);

                                    logger.logComment("Cell process found: " + cp);

                                    if (cp == null)
                                    {
                                        GuiUtils.showErrorMessage(logger,
                                                                  "Problem generating plot " +
                                                                  simPlot.getPlotReference() +
                                                                  "with Cell Mechanism: " +
                                                                  processName, null, null);

                                        whatToPlot = variable + "_" + processName;
                                    }
                                    else
                                    {

                                        if (cp instanceof ChannelMLCellMechanism)
                                        {
                                            try
                                            {
                                                if (variable.startsWith(SimPlot.PLOT_CONCENTRATION))
                                                {
                                                    logger.logComment("Looking to plot the concentration...");

                                                    String ion = variable.substring(variable.indexOf(SimPlot.
                                                        PLOTTED_VALUE_SEPARATOR) + 1);

                                                    whatToPlot = ion + "i"; // assume internal concentration
                                                }
                                                else if (variable.startsWith(SimPlot.PLOT_CURRENT))
                                                {
                                                    logger.logComment("Looking to plot the current...");

                                                    String ion = variable.substring(variable.indexOf(SimPlot.
                                                        PLOTTED_VALUE_SEPARATOR) + 1);

                                                    whatToPlot = "i" + ion;

                                                    minVal
                                                        = (float) UnitConverter.getCurrentDensity(minVal,
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        UnitConverter.NEURON_UNITS);

                                                    maxVal
                                                        = (float) UnitConverter.getCurrentDensity(maxVal,
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        UnitConverter.NEURON_UNITS);
                                                }
                                                else
                                                {
                                                    logger.logComment(
                                                        "Assuming using the native name of the variable");
                                                    whatToPlot = variable + "_" + processName;
                                                }
                                            }
                                            catch (Exception ex)
                                            {
                                                GuiUtils.showErrorMessage(logger,
                                                                          "Problem generating plot " +
                                                                          simPlot.getPlotReference() +
                                                                          "with Cell Mechanism: " +
                                                                          processName, ex, null);

                                            }

                                        }
                                        else
                                        {
                                            logger.logError("Unsupported type of Cell Mechanism");

                                            whatToPlot = variable + "_" + processName;

                                        }
                                    }
                                }

                                logger.logComment("Plotting variable : " + variable
                                                  + " in process: " + processName + ", so whatToPlot: " +
                                                  whatToPlot);

                            }

                            response.append(generateSinglePlot(simPlot.getPlotReference(),
                                                               simPlot.getGraphWindow(),
                                                               cellGroup,
                                                               nextCellNum,
                                                               getHocSectionName(seg.getSection().getSectionName(),
                                                               lenAlongSegment,
                                                               minVal,
                                                               maxVal,
                                                               whatToPlot,
                                                               getNextColour()));
                        }
                    }
                }
            }

                 }
         */

        return response.toString();
    }

    private String generateSinglePlot(String plotTitle,
                                      String graphWindow,
                                      float minVal,
                                      float maxVal,
                                      String varReference,
                                      String colour)
    {
        StringBuffer response = new StringBuffer();

        addComment(response,
                          " This code pops up a plot of " + varReference +"\n");

        if (!graphsCreated.contains(graphWindow))
        {
            response.append("objref " + graphWindow + "\n");
            response.append(graphWindow + " = new Graph(0)\n");
            response.append(graphWindow + ".size(0," + getSimDuration()
                            + "," + minVal
                            + "," + maxVal + ")\n");

            response.append(graphWindow + ".view(0, " + minVal + ", " + getSimDuration() +
                            ", " + (maxVal - minVal) + ", 80, 330, 330, 250)\n");

            graphsCreated.add(graphWindow);
        }

        response.append("{\n");

        /*
        String varRef = "a_" + cellGroup
            + "[" + cellNumber + "]"
            + "." + sectionName
            + "." + whatToRec;

        String varRefIncFract = varRef+"(" + distAlong + ")";*/


        response.append("    " + graphWindow + ".addexpr(\"" + plotTitle
                        + "\", \""+varReference+"\", " + colour
                        + ", 1, 0.8, 0.9, 2)\n");

    /*
        response.append("    " + graphWindow + ".addexpr(\""+ whatToRecord
                        + "\", \"a_" + cellGroup
                        + "[" + cellNumber + "]"
                        + "." + sectionName
                        + "." + whatToRecord
                        + "(" + distAlong
                        + ")\", " + colour
                        + ", 1, 0.8, 0.9, 2)\n");*/


//response.append("somaVoltage.addexpr(\"soma\", \"v(.5)\", 1, 1, 0.8, 0.9, 2)\n");

        response.append("    " + "graphList[0].append(" + graphWindow + ")\n");
        response.append("}\n");

        return response.toString();

    }

    public String generateShapePlot()
    {
        StringBuffer response = new StringBuffer();

        addComment(response, " This code pops up a Shape plot of the cells\n");

        response.append("objref plotShape\n");
        response.append("plotShape = new PlotShape()\n");

        //response.append("plotShape.show(0)\n");
        response.append("plotShape.exec_menu(\"Shape Plot\")\n\n");
        response.append("fast_flush_list.append(plotShape)\n\n");


        return response.toString();
    }

    public String getNextColour()
    {
        String colour = nextColour + "";
        nextColour++;
        if (nextColour >= 10) nextColour = 1;
        return colour;
    }

    public String generateRunMechanism(int runMode)
    {
        StringBuffer response = new StringBuffer();

        String dateCommand = "date +%x,%X:%N";

        if (runMode == RUN_PARALLEL)
        {
            response.append("pnm.want_all_spikes()\n");

            response.append("stdinit()\n");
            response.append("print \"Initialised on \", host\n");
            response.append("realtime = startsw()\n");
            response.append("pnm.psolve("+simConfig.getSimDuration()+")\n");
            response.append("realtime = startsw() - realtime\n");

            response.append("for i=0, pnm.spikevec.size-1 {\n");
            response.append("    print \"Spike \",i, \": \", pnm.spikevec.x[i], pnm.idvec.x[i]\n");
            response.append("}\n");

            return response.toString();
        }


        //if (this.windowsTargetEnv()) dateCommand = "c:/windows/time.exe /T";
        
        boolean announceDate = !GeneralUtils.isWindowsBasedPlatform();
        String dateInfo = "";
        
        if(announceDate)
        {
    
            response.append("strdef date\n");
            response.append("system(\"" + dateCommand + "\", date)\n");
            dateInfo = " at time: \", date, \"";
        }

        response.append("print \"Starting simulation of duration "+simConfig.getSimDuration()+" ms, reference: " + project.simulationParameters.getReference() +
                dateInfo+"\"\n\n");

        response.append("startsw()\n\n");

        if (!project.neuronSettings.isVarTimeStep())
        {
            addMajorComment(response, "Main run statement");
            response.append("run()\n\n");
        }
        else
        {
            addMajorComment(response, "Main run statement");
            addComment(response, "Setting basic variable time step active");

            response.append("cvode.active(1)\n");
            response.append("run()\n\n");


        }
        dateInfo = "";
        
        if(announceDate)
        {
            response.append("system(\"" + dateCommand + "\", date)\n");
            dateInfo = "print \"Current time: \", date\n\n";
        }

        response.append("print \"Finished simulation in \", realtime ,\"seconds\"\n\n");
        response.append(dateInfo);

        return response.toString();

    }





    public String generateGUIForRerunning()
    {
        StringBuffer response = new StringBuffer();

        addComment(response, " This code pops up a simple Run Control\n");

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
        StringBuffer response = new StringBuffer();

        addComment(response,
                          " As it is intended to run the file under Condor, the hoc will quit after finishing\n");

        response.append("\nquit()\n");

        return response.toString();
    }



    public static void addComment(StringBuffer responseBuffer, String comment)
    {
        if (!addComments) return;
        addComment(responseBuffer, comment, "", true);
    }
    public static void addComment(StringBuffer responseBuffer, String comment, boolean inclReturn)
    {
        if (!addComments) return;
        addComment(responseBuffer, comment, "", inclReturn);
    }


    public static void addComment(StringBuffer responseBuffer, String comment, String preSlashes, boolean inclReturn)
    {
        if (!addComments) return;
        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append(preSlashes+"//  " + comment + "\n");
        if (inclReturn) responseBuffer.append("\n");
    }

    public static void addMajorComment(StringBuffer responseBuffer, String comment)
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
        else if (simIndepVarName.indexOf(SimPlot.CURRENT)>=0)
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
            if (simIndepVarName.indexOf(SimPlot.SYNAPSES)>=0)
            {
                simIndepVarName = simIndepVarName.substring(SimPlot.SYNAPSES.length()+
                                                            SimPlot.PLOTTED_VALUE_SEPARATOR.length());
            }

            String mechanismName = simIndepVarName.substring(0,
                                                      simIndepVarName.indexOf(SimPlot.
                                                                         PLOTTED_VALUE_SEPARATOR));


            String variable = simIndepVarName.substring(
                simIndepVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) + 1);

            logger.logComment("--------------     Looking to plot " + variable +
                              " on cell process: " + mechanismName);

            if (variable.startsWith(SimPlot.COND_DENS))
            {
                variable = "gion";

                neuronVar = variable + "_" + mechanismName;

            }

            else if (variable.indexOf(SimPlot.SYN_COND)>=0)
            {
                neuronVar = "g";

                //neuronVar = mechanismName+"[]."+variable;

            }

            else if (simIndepVarName.indexOf(SimPlot.SYNAPSES)>=0)
            {
                neuronVar = simIndepVarName.substring(simIndepVarName.lastIndexOf(SimPlot.
                                                                         PLOTTED_VALUE_SEPARATOR)+1);



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
                            else if (variable.startsWith(SimPlot.CURRENT))
                            {
                                logger.logComment("Looking to plot the current...");

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


    public void runNeuronFile(File mainHocFile, int runMode) throws NeuronException
    {
        logger.logComment("Trying to run the hoc file: "+ mainHocFile);

        if (!mainHocFile.exists())
        {
            throw new NeuronException("The NEURON file: "+ mainHocFile
                                      + " does not exist. Have you generated the NEURON code?");
        }


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

        if(runMode==RUN_LOCALLY || runMode==RUN_PARALLEL)
        {
            try
            {
                String locationOfNeuron = GeneralProperties.getNeuronHomeDir();

                String neuronExecutable = null;

                if (GeneralUtils.isWindowsBasedPlatform())
                {
                    
                    logger.logComment("Assuming Windows environment...");
                    neuronExecutable = locationOfNeuron
                        + System.getProperty("file.separator")
                        + "bin"
                        + System.getProperty("file.separator")
                        + "neuron.exe";
                    
                    String filename = getHocFriendlyFilename(mainHocFile.getAbsolutePath());

                    if (filename.indexOf(" ")>=0)
                    {
                        GuiUtils.showErrorMessage(logger, "Error. The full name of the file to execute in NEURON: "+filename
                                                  +" contains a space. This will throw an error in NEURON.\n Was the code created in a directory containing a space in its name?", null, null);

                    }

                    fullCommand = GeneralProperties.getExecutableCommandLine() + " "
                    	+ neuronExecutable + " "+filename;
                  


                    File dirToRunIn = dirForDataFiles;


                    String scriptText = "cd "+dirToRunIn.getAbsolutePath()+"\n";
                  
                    scriptText = scriptText + fullCommand;
                    
                    File scriptFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()), "runsim.bat");
                    FileWriter fw = new FileWriter(scriptFile);
                    fw.write(scriptText);
                    fw.close();

                    logger.logComment("Going to execute command: " + fullCommand + " in dir: " +
                                      dirToRunIn);


                    rt.exec(fullCommand, null, dirToRunIn);
                    

                    logger.logComment("Have executed command: " + fullCommand + " in dir: " +
                                      dirToRunIn);

                }
                else
                {
                    String[] commandToExe = null;
                    
                    if (dirForDataFiles.getAbsolutePath().indexOf(" ")>=0)
                    {
                        throw new NeuronException("NEURON files cannot be run in a directory like: "+ dirForDataFiles
                                + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run NEURON.\nArguments need to be given to this executable and spaces in filenames cause problems.\n"
                                +"Try saving the project in a directory without spaces.");
                    }
                    
                    
                    String mainExecutable = "nrngui";
                    if (runMode==RUN_PARALLEL) mainExecutable = "nrniv";

                    neuronExecutable = locationOfNeuron
                        + System.getProperty("file.separator")
                        + "bin"
                        + System.getProperty("file.separator")
                        + mainExecutable;

                    String title = "NEURON_simulation" + "__"+ project.simulationParameters.getReference();

                    File dirToRunIn = dirForDataFiles;


                    String basicCommLine = GeneralProperties.getExecutableCommandLine();

                    String executable = "";
                    String extraArgs = "";
                    String titleOpt = "";
                    String workdirOpt = "";
                    String postArgs = "";

                    StringBuffer preCommand = new StringBuffer("");

                    if (runMode==RUN_PARALLEL)
                    {
                        ArrayList<MpiConfiguration> configs = GeneralProperties.getMpiSettings().getMpiConfigurations();

                        preCommand.append("mpirun -map ");

                        ArrayList<MpiHost> hosts = configs.get(MpiSettings.favouredConfig).getHostList();

                        for (int i = 0; i < hosts.size(); i++)
                        {
                            for (int j = 0; j < hosts.get(i).getNumProcessors(); j++)
                            {
                                if (!(i==0 && j==0)) preCommand.append(":");
                                preCommand.append(hosts.get(i).getHostname());
                            }
                        }
                        preCommand.append("  ");

                    }

                    if (GeneralUtils.isLinuxBasedPlatform())
                    {
                        logger.logComment("Is linux platform...");

                        if (basicCommLine.indexOf("konsole") >= 0)
                        {
                            logger.logComment("Assume we're using KDE");
                            titleOpt = "-T";
                            workdirOpt = "--workdir";
                               // + dirToRunIn.getAbsolutePath();
                            
                            extraArgs = "-e";
                            executable = basicCommLine.trim();
                        }
                        else if (basicCommLine.indexOf("gnome") >= 0)
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
                                extraArgs = "-x";
                            }

                        }
                        else
                        {
                            logger.logComment("Unknown console command, going with the flow...");

                            executable = basicCommLine.trim();
                        }
                    }
                    else if (GeneralUtils.isMacBasedPlatform())
                    {
                            logger.logComment("Assuming a Mac based machine...");

                            executable = basicCommLine.trim();

                            /** @todo update with real command line option for working dir... */
                            workdirOpt = " ";
                                //+ dirToRunIn.getAbsolutePath();


                            postArgs = "";

                            dirToRunIn = ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory());
                    }

                    String scriptText = "cd '" + dirToRunIn.getAbsolutePath() + "'\n" 
                        + preCommand
                        + neuronExecutable
                        + " "
                        + mainHocFile.getName()
                        + postArgs;

                    File scriptFile = new File(ProjectStructure.getNeuronCodeDir(project.getProjectMainDirectory()),
                                               "runsim.sh");
                    
                    FileWriter fw = new FileWriter(scriptFile);
                    //scriptFile.se
                    fw.write(scriptText);
                    fw.close();

                    // bit of a hack...
                    rt.exec(new String[]{"chmod","u+x",scriptFile.getAbsolutePath()});
                    
                    try
                    {
                        // This is to make sure the file permission is updated..
                        Thread.sleep(600);
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }

                    commandToExe = new String[]{executable, 
                            titleOpt, title,
                            workdirOpt, dirToRunIn.getAbsolutePath(),
                            extraArgs,
                            scriptFile.getAbsolutePath()};

                    fullCommand = "";
                    for (int i=0;i<commandToExe.length;i++)
                    {
                        fullCommand = fullCommand+" "+ commandToExe[i];
                    }

                    logger.logComment("Going to execute command: " + fullCommand);

                    rt.exec(commandToExe, null);

                    logger.logComment("Have successfully executed command: " + fullCommand);

                }

            }
            catch (Exception ex)
            {
                logger.logError("Error running the command: " + fullCommand, ex);
                throw new NeuronException("Error executing the hoc file: " + mainHocFile+"\n"+ex.getMessage(), ex);
            }
        }
        else if (runMode==RUN_VIA_CONDOR)
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
                        String nextHocFile = (String) cellTemplatesGenAndIncluded.elementAt(i);
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

                    //StringBuffer file

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


    /**
     * @return i686 for most, x86_64 if "64" present in system properties os.arch, e.g. amd64. Will need updating as Neuron tested on more platforms...
     *
     */
    public static String getArchSpecificDir()
    {
        if (System.getProperty("os.arch").indexOf("64")>=0)
        {
            return "x86_64";
        }
        else
        {
            return "i686";
        }
    }


    public static  void main(String[] args)
    {
        try
        {

            int runMode  = NeuronFileManager.RUN_PARALLEL;


            MainFrame frame = new MainFrame();

            File pf = new File("/bernal/projects/Parallel/Parallel.neuro.xml");

            //File pf = new File("models/PVMExample/PVMExample.neuro.xml");

            frame.doLoadProject(pf.getAbsolutePath());

            System.out.println("doGenerate...");
            frame.projManager.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 1234);

            System.out.println("Snoozing...");

            Thread.sleep(500);

            System.out.println("Coming out of sleep");

            frame.doCreateHoc(runMode);

            System.out.println("done create");


            System.exit(0);

            frame.projManager.getCurrentProject().neuronFileManager.runNeuronFile(
                frame.projManager.getCurrentProject().neuronFileManager.getMainHocFile(),
                runMode);

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
