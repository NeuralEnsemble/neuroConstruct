/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.neuroml;

import java.io.*;
import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Main file for generating simulations based on NeuroML/Python
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuroMLPythonFileManager
{
    private static ClassLogger logger = new ClassLogger("NeuroMLPythonFileManager");

    Project project = null;

    /////File mainSimFile = null;

    int randomSeed = 0;

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    //ArrayList<String> cellTemplatesGenerated = new ArrayList<String>();

    ////private static boolean addComments = true;

    //ArrayList<String> graphsCreated = new ArrayList<String>();

    SimConfig simConfig = null;

    //////MorphCompartmentalisation morphComp = null;

    //////Hashtable<String, Cell> mappedCells = new Hashtable<String, Cell>();
    //////Hashtable<String, SegmentLocMapper> mappedSegments = new Hashtable<String, SegmentLocMapper>();

    private NeuroMLPythonFileManager()
    {
    }


    public NeuroMLPythonFileManager(Project project)
    {
        this.project = project;
    }


    public void reset()
    {
        ////////////cellTemplatesGenerated = new ArrayList<String>();
        ////////////graphsCreated = new ArrayList<String>();

        //////////addComments = project.genesisSettings.isGenerateComments();
    }




    public void generateTheFiles(SimConfig simConfig,
                                  MorphCompartmentalisation mc,
                                  int seed)
    {
        logger.logComment("Starting generation of the files...");

        //long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;

        this.removeAllPreviousGeneratedFiles();


        randomSeed = seed;
        

        File neuroMLDir = ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory());

        ArrayList<Cell> generatedCells = null;
        
        try
        {
            generatedCells = MorphMLConverter.saveAllCellsInNeuroML(project, 
                                                   mc, 
                                                   NeuroMLConstants.NEUROML_LEVEL_3, 
                                                   simConfig,
                                                   neuroMLDir);
        }
        catch (MorphologyException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving cells in NeuroML format" , ex1, null);
            return;
        }
        
        ArrayList<String> cellMechFilesHandled = new ArrayList<String>();
        
        for(Cell nextCell: generatedCells)
        {
            ArrayList<String> cellMechs = new ArrayList<String>();
            ArrayList<String> chanMechNames = nextCell.getAllChanMechNames(true);
            
            for(String cm: chanMechNames)
            {
                cellMechs.add(cm);
            }
            ArrayList<String> syns = nextCell.getAllAllowedSynapseTypes();
            
            cellMechs.addAll(syns);
            
            logger.logComment("cellMechs: "+cellMechs);
            
            for(String cellMech: cellMechs)
            {
                if (!cellMechFilesHandled.contains(cellMech))
                {
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cellMech);
                    
                    if (!(cm instanceof ChannelMLCellMechanism))
                    {
                        File warnFile = new File(neuroMLDir, cm.getInstanceName()+".warning");
                        try
                        {
                            FileWriter fw = new FileWriter(warnFile);
                            fw.write("Warning: cell mechanism "+cm.getInstanceName()+" is not implemented in ChannelML in the project: "+project.getProjectFileName()+", and so cannot be used in the Python/NeuroML test simulation.");
                            
                            fw.close();
                            
                        }
                        catch(IOException ioe)
                        {
                            GuiUtils.showErrorMessage(logger, "Problem writing to file: " +warnFile, ioe, null);
                        }
                    }
                    else
                    {
                        ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;
                        
                        File cmlFile = new File(neuroMLDir, cm.getInstanceName()+".channel.xml");
                        try
                        {
                            File copied = GeneralUtils.copyFileIntoDir(cmlCm.getChannelMLFile(project), neuroMLDir);
                            
                            copied.renameTo(cmlFile);
                            
                        }
                        catch(IOException ioe)
                        {
                            GuiUtils.showErrorMessage(logger, "Problem writing to file: " +cmlCm, ioe, null);
                        }
                    }
                    cellMechFilesHandled.add(cellMech);
                }
            }
        }
        

        File networkFile = new File(neuroMLDir, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML);
        
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
            GuiUtils.showErrorMessage(logger, "Problem saving network in NeuroML", ex1, null);
        }


        
        /*

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed

        addComments = project.genesisSettings.isGenerateComments();
        
      

        FileWriter fw = null;

        if (!(project.genesisSettings.getUnitSystemToUse()==UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS
              || project.genesisSettings.getUnitSystemToUse()==UnitConverter.GENESIS_SI_UNITS))
            throw new
                GenesisException("The units specified for generation of the GENESIS code are not recognised: "
                                 +project.genesisSettings.getUnitSystemToUse());


        try
        {
            generateCellMappings();

            File dirForGenesisFiles = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());


            mainGenesisFile = new File(dirForGenesisFiles, project.getProjectName() + ".g");

            logger.logComment("generating: "+ mainGenesisFile);
            fw = new FileWriter(mainGenesisFile);

            fw.write(getGenesisFileHeader());

            fw.write(generateWelcomeComments());

            fw.write(generateRandomise());

            fw.write(generateGlobals());

            fw.write(generateIncludes());

            fw.write(printEnv());

            fw.write(generateChanMechIncludes());

            fw.write(generateSynMechIncludes());

            fw.write(generateScriptBlock(ScriptLocation.BEFORE_CELL_CREATION));

            fw.write(generateCellGroups());

            fw.write(generateNetworkConnections());

            fw.write(generateStimulations());

            //fw.write(generateAfterCreationText());

            fw.write(generateNumIntegMethod());

            fw.write(generateRunSettings());

            fw.write(generatePlots());

            fw.write(generate3Dplot());


            fw.write(generateRunControls());

            //fw.write(generateCellParamControl());

            fw.write(generateGenesisSimulationRecording());


            fw.write(generateScriptBlock(ScriptLocation.AFTER_SIMULATION));

            fw.flush();
            fw.close();


            File utilsFile = new File(ProjectStructure.getGenesisUtilsFile());
            GeneralUtils.copyFileIntoDir(utilsFile, dirForGenesisFiles);

        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (IOException ex1)
            {
            }
            throw new GenesisException("Error creating file: " + mainGenesisFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage());

        }

        this.mainFileGenerated = true;

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("... Created Main GENESIS file: " + mainGenesisFile
                +" in "+genTime+" seconds. ");
        */

    }


    public int getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    public float getCurrentGenTime()
    {
        return this.genTime;
    }



    /**
     * Add the specified extra script block
   
    private String generateScriptBlock(ScriptLocation sl)
    {
        StringBuffer response = new StringBuffer();

        String text = project.genesisSettings.getNativeBlock(sl);

        text = ScriptLocation.parseForSimConfigSpecifics(text, simConfig.getName());

        logger.logComment("Cleaned up to: "+ text);


        if (text==null|| text.trim().length()==0)
        {
            return "";
        }
        else
        {
            addMajorComment(response, "GENESIS script to run at location: "+ sl.toString());
            response.append(text + "\n");
            addMajorComment(response, "End of GENESIS script to run at location: "+ sl.toString());

            return response.toString();
        }
    }  */



    /**
     * Generates the synaptic connections from the values in generatedNetworkConnections

    private String generateNetworkConnections()
    {
        StringBuffer response = new StringBuffer();
        response.append("\n");

        Iterator allNetConnNames = project.generatedNetworkConnections.getNamesNetConns();

        if (!allNetConnNames.hasNext())
        {
            logger.logComment("There are no synaptic connections");
            return "";
        }

        addMajorComment(response, "Adding Network Connections");

        // Adding specific network connections...
        while (allNetConnNames.hasNext())
        {
            String netConnName = (String) allNetConnNames.next();

            String sourceCellGroup = null;
            String targetCellGroup = null;

            Vector synPropList = null;

            if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
            {
                sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                synPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
            }
            else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
            {
                sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                synPropList = project.volBasedConnsInfo.getSynapseList(netConnName);
            }

            addComment(response, "Adding Network Connection: " + netConnName
                              + " from Cell Group: " + sourceCellGroup
                              + " to: " + targetCellGroup);

            ArrayList<SingleSynapticConnection> allSynapses = project.generatedNetworkConnections.getSynapticConnections(netConnName);


            String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);
            //Cell targetCell = mappedCells.get(targetCellType);

            String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
            Cell sourceCell = mappedCells.get(sourceCellType);

            // used for storing alternate connection locations
            // when ApPropSpeed on sections..
            Hashtable<Integer, SegmentLocation> substituteConnPoints = new Hashtable<Integer,SegmentLocation>();

            if (sourceCell.getApPropSpeedsVsGroups().size()>0) // are there any?
            {
                ArrayList<Section> allSecs = sourceCell.getAllSections();

                for (int j = 0; j < allSecs.size(); j++)
                {
                    Section nextSec = allSecs.get(j);

                    if (sourceCell.getApPropSpeedForSection(nextSec)!=null)
                    {
                        LinkedList<Segment> segs = sourceCell.getAllSegmentsInSection(nextSec);

                        SegmentLocation synconloc = CellTopologyHelper.getConnLocOnExpModParent(sourceCell, segs.getFirst());

                        for (int k = 0; k < segs.size(); k++)
                        {
                            int id = segs.get(k).getSegmentId();
                            substituteConnPoints.put(new Integer(id), synconloc);
                        }
                    }
                }
            }


            // go through all of the synapse types. There will be only one for a simple network connection,
            // but multiple for complex net conns...
            for (int synapseIndex = 0; synapseIndex < allSynapses.size(); synapseIndex++)
            {
                GeneratedNetworkConnections.SingleSynapticConnection syn = allSynapses.get(synapseIndex);

                for (int synPropIndex = 0; synPropIndex < synPropList.size(); synPropIndex++)
                {
                    SynapticProperties synProps = (SynapticProperties) synPropList.elementAt(synPropIndex);

                    logger.logComment("netConnName: "+netConnName
                                      + ", synapseIndex: " + synapseIndex
                                      + ", synProps: " + synProps);

                    //Segment targetSegment = targetCell.getSegmentWithId(syn.targetEndPoint.location.segmentId);

                    Segment targetSegment = this.getMappedSegment(targetCellType,
                                                                  syn.targetEndPoint.location.getSegmentId(),
                                                                  syn.targetEndPoint.location.getFractAlong());

                    logger.logComment("targetSegment: " + targetSegment);

                    Segment sourceSegment = null;

                    int origId = syn.sourceEndPoint.location.getSegmentId();

                    float apSegmentPropDelay = 0;

                    if (substituteConnPoints.size() == 0 || // there is no ApPropSpeed on cell
                        !substituteConnPoints.containsKey(new Integer(origId))) // none on this segment
                    {
                        logger.logComment("No ap speed on cell");
                        //sourceSegment = sourceCell.getSegmentWithId(origId);
                        sourceSegment = getMappedSegment(sourceCellType,origId,syn.sourceEndPoint.location.getFractAlong());

                    }
                    else
                    {
                        logger.logComment("Is ap speed on cell");

                        Segment realSource = sourceCell.getSegmentWithId(origId);

                        logger.logComment("realSource: " + realSource);

                        SegmentLocation subsSynConLoc = substituteConnPoints.get(new Integer(origId));

                        logger.logComment("subsSynConLoc: " + subsSynConLoc);

                        float fractionAlongSegment = subsSynConLoc.getFractAlong();

                        sourceSegment = getMappedSegment(sourceCellType,
                                                         subsSynConLoc.getSegmentId(),
                                                         fractionAlongSegment);


                        apSegmentPropDelay = CellTopologyHelper.getTimeToFirstExpModParent(sourceCell,
                            realSource,
                            syn.sourceEndPoint.location.getFractAlong());

                        addComment(response, "Instead of point " + syn.sourceEndPoint.location.getFractAlong() + " along seg: "
                                   + realSource.toShortString() + " connecting to point in middle of compartment: "
                                   + sourceSegment.toShortString() + "");

                    }

                    logger.logComment("source segmnt: " + sourceSegment);

                    addComment(response, "Source segment: " + sourceSegment.getSegmentName()
                               + ", target segment: " + targetSegment.getSegmentName());

                    String receivingElement = getCellElementName(targetCellGroup, syn.targetEndPoint.cellNumber)
                        + "/" + SimEnvHelper.getSimulatorFriendlyName(targetSegment.getSegmentName());

                    String triggeringElement = getCellElementName(sourceCellGroup, syn.sourceEndPoint.cellNumber)
                        + "/" + SimEnvHelper.getSimulatorFriendlyName(sourceSegment.getSegmentName());

                    //NumberGenerator delayGenerator = synProps.delayGenerator;

                    //NumberGenerator weightsGenerator = synProps.weightsGenerator;

                    float synInternalDelay = -1;
                    float weight = -1;

                    if (syn.props==null || syn.props.size()==0)
                    {
                        synInternalDelay = synProps.getDelayGenerator().getNominalNumber();
                        weight = synProps.getWeightsGenerator().getNominalNumber();
                    }
                    else
                    {
                        for (ConnSpecificProps prop:syn.props)
                        {
                            logger.logComment("....  Looking at : " + prop+", for: "+ synProps);
                            if (prop.synapseType.equals(synProps.getSynapseType()))
                            {
                                synInternalDelay = prop.internalDelay;
                                weight = prop.weight;
                            }
                        }
                    }


                    // put synaptic start point on source axon

                    String spikeElement = triggeringElement + "/spike";

                    response.append("if (!({exists " + spikeElement + "}))\n");
                    response.append("    create spikegen " + spikeElement + "\n");

                    response.append("    setfield " + spikeElement + "  thresh "
                                    + UnitConverter.getVoltage(synProps.getThreshold(),
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               project.genesisSettings.getUnitSystemToUse())

                                    + "  abs_refract "
                                    + UnitConverter.getTime(10,
                                                            UnitConverter.NEUROCONSTRUCT_UNITS,
                                                            project.genesisSettings.getUnitSystemToUse())
                                    + " output_amp 1\n");

                    response.append("    addmsg  " + triggeringElement
                                    + "  " + spikeElement + "  INPUT Vm\n");

                    response.append("end\n\n");

                    String newSynapseName = null;


                    newSynapseName = synProps.getSynapseType()
                        + "_" + netConnName;

                    response.append("makechannel_" + synProps.getSynapseType()
                                    + " " + receivingElement
                                    + " " + newSynapseName + "\n\n");

                    // link source and target...

                    response.append("addmsg " + spikeElement + " "
                                    + receivingElement + "/"
                                    + newSynapseName + " SPIKE\n");

                    response.append("int msgnum\n");
                    response.append("msgnum = {getfield " + receivingElement + "/" + newSynapseName
                                    + " nsynapses} - 1\n\n");



                    float apSpaceDelay = syn.apPropDelay;


                    addComment(response, "Weight of syn: " + weight);
                    addComment(response,
                               "Delay due to AP propagation along segments: " + apSegmentPropDelay
                               + ", delay due to AP jump pre -> post location "+ apSpaceDelay
                               + ", internal synapse delay (from Synaptic Props): " + synInternalDelay);

                    response.append("setfield " + receivingElement + "/" + newSynapseName
                                    + " synapse[{msgnum}].weight "
                                    + weight
                                    + " synapse[{msgnum}].delay "
                                    + UnitConverter.getTime(  (synInternalDelay + apSegmentPropDelay + apSpaceDelay),
                                                            UnitConverter.NEUROCONSTRUCT_UNITS,
                                                            project.genesisSettings.getUnitSystemToUse())
                                    + "\n\n");

                }

            }
        }
        return response.toString();
        }
    */


    private void removeAllPreviousGeneratedFiles()
    {
        /////////////cellTemplatesGenerated.clear();

        File nmlFileDir = ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(nmlFileDir, false, true, true);


    }


    public static String getFileHeader()
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

    /*
    private String generateIncludes()
    {
        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            dir = this.mainGenesisFile.getParentFile().getAbsolutePath()+ System.getProperty("file.separator");

            //if (!(new File(dir)))
            dir = GeneralUtils.convertToCygwinPath(dir);
        }
        StringBuffer response = new StringBuffer();
        addComment(response, "Including neuroConstruct utilities file");
        response.append("include "+ getFriendlyDirName(dir)+"nCtools \n\n");

        addComment(response, "Including external files");
        response.append("include compartments \n\n");


        addComment(response, "Creating element for channel prototypes");
        response.append("create neutral /library\n");
        response.append("disable /library\n");

        response.append("pushe /library\n");
        response.append("make_cylind_compartment\n");

        response.append("make_cylind_symcompartment\n");
        response.append("pope\n\n");

        return response.toString();
    }*/









/*
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







    private double convertNeuroConstructTime(double time)
    {
        return UnitConverter.getTime(time,
                                     UnitConverter.NEUROCONSTRUCT_UNITS,
                                     project.genesisSettings.getUnitSystemToUse());

    }*/


    /**
     * Adds a line commented out by //, and an empty line after the comment
    
    public static void addComment(StringBuffer responseBuffer, String comment)
    {
        logger.logComment("Adding comment: "+ comment);
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//   " + comment + "\n");
        responseBuffer.append("\n");
    } */

    /**
     * Adds a line commented out by //, but with no empty line after the comment
     
    public static void addQuickComment(StringBuffer responseBuffer, String comment)
    {
        logger.logComment("Adding comment: "+ comment);
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//   " + comment + "\n");

    }



    public static void addMajorComment(StringBuffer responseBuffer, String comment)
    {
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//////////////////////////////////////////////////////////////////////\n");
        responseBuffer.append("//   " + comment + "\n");
        responseBuffer.append("//////////////////////////////////////////////////////////////////////\n");
        responseBuffer.append("\n");
    }*/



/*

    public void runGenesisFile() throws GenesisException
    {
        logger.logComment("Trying to run the mainGenesisFile...");

        nextColour = 1; // reset it...
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new GenesisException("GENESIS file not yet generated");
        }

        if (newRecordingToBeMade)
        {
            logger.logComment("Getting rid of old files...");
            File dataFileDir = getDirectoryForSimulationFiles();

            if (dataFileDir.exists())
            {
                File[] files = dataFileDir.listFiles();
                for (int i = 0; i < files.length; i++)
                {
                    if (!files[i].getName().endsWith(".m"))
                    {
                        files[i].delete();
                    }
                }
                logger.logComment("Directory " + dataFileDir + " being cleansed");
            }
            else
            {
                logger.logError("Directory " + dataFileDir + " doesn't exist...");
                return;
            }
        }

        File dirForDataFiles = getDirectoryForSimulationFiles();

        File positionsFile = new File(dirForDataFiles, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(dirForDataFiles, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(dirForDataFiles, SimulationData.ELEC_INPUT_DATA_FILE);
        

        
        if (dirForDataFiles.getAbsolutePath().indexOf(" ")>=0)
        {
            throw new GenesisException("GENESIS files cannot be run in a directory like: "+ dirForDataFiles
                    + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run GENESIS.\nArguments need to be given to this executable and spaces in filenames cause problems.\n"
                    +"Try saving the project in a directory without spaces.");
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
            SimulationsInfo.recordSimulationSummary(project, simConfig, dirForDataFiles, "GENESIS", morphComp);
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "
                                      + dirForDataFiles+"\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
        }


        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;

        try
        {
            String genesisExecutable = null;

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                genesisExecutable = "templates\\genesisUtils\\startxwin2.bat";

                String args = GeneralUtils.convertToCygwinPath(mainGenesisFile.getAbsolutePath()+"");

                //args = this.getFriendlyDirName(args);

                String title = "GENESIS_simulation" + "___" + project.simulationParameters.getReference();

                commandToExecute = "cmd /K start \""+title+"\"  " +  genesisExecutable + " "+args;

                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have executed command: " + commandToExecute);


            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                genesisExecutable = "genesis";

                String title = "GENESIS_simulation" + "___" + project.simulationParameters.getReference();



                File dirToRunIn = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());

                String basicCommLine = GeneralProperties.getExecutableCommandLine();

                String executable = "";
                String extraArgs = "";
                String titleOption = "";
                String workdirOption = "";

                if (basicCommLine.indexOf("konsole")>=0)
                {
                    logger.logComment("Assume we're using KDE");
                    titleOption = " -T="+title;
                    workdirOption = " --workdir="+ dirToRunIn.getAbsolutePath();
                    extraArgs = "-e ";
                    executable = basicCommLine.trim();
                }
                else if (basicCommLine.indexOf("gnome")>=0)
                {
                    logger.logComment("Assume we're using Gnome");
                    titleOption = " --title="+title;
                    workdirOption = " --working-directory="+ dirToRunIn.getAbsolutePath();

                    if (basicCommLine.trim().indexOf(" ")>0) // case where basicCommLine is gnome-terminal -x
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

                String scriptText = "cd " + dirToRunIn.getAbsolutePath() + "\n" + genesisExecutable
                    + " "
                    + mainGenesisFile.getName();

                File scriptFile = new File(ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory()),
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
            throw new GenesisException("Error executing the GENESIS file: " + mainGenesisFile, ex);
        }
    }

*/
/*
    public String getMainSimFileName() throws GenesisException
    {
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new GenesisException("GENESIS file not yet generated");
        }

        return this.mainGenesisFile.getAbsolutePath();

    }*/

    public static void main(String[] args)
    {

        try
        {


            
            System.exit(2);




            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File("examples/Ex-Simple/Ex-Simple.neuro.xml"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);
            
            //NeuroMLPythonFileManager npfm = new NeuroMLPythonFileManager(p);



            //OriginalCompartmentalisation oc = new OriginalCompartmentalisation();




            //npfm.generateTheFiles(p.simConfigInfo.getDefaultSimConfig(), null, oc, 12345);
            
            //gen.runGenesisFile();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
