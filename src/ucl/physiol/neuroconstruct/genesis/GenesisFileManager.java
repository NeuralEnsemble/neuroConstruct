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

package ucl.physiol.neuroconstruct.genesis;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;


/**
 * Main file for generating the GENESIS files
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class GenesisFileManager
{
    private static ClassLogger logger = new ClassLogger("GenesisFileManager");

    Project project = null;

    File mainGenesisFile = null;

    int randomSeed = 0;

    boolean mainFileGenerated = false;

    ArrayList<String> cellTemplatesGenAndIncl = new ArrayList<String>();

    boolean newRecordingToBeMade = false;

    int nextColour = 1;

    private static boolean addComments = true;

    MultiRunManager multiRunManager = null;

    public static final String HSOLVE_ELEMENT_NAME = "solve";
    public static final String CELL_ELEMENT_ROOT = "/cells";
    public static final String FILE_ELEMENT_ROOT = "/fileout";
    public static final String PLOT_ELEMENT_ROOT = "/plots";
    public static final String STIM_ELEMENT_ROOT = "/stim";
    public static final String PULSE_ELEMENT_ROOT = STIM_ELEMENT_ROOT + "/pulse";
    public static final String RNDSPIKE_ELEMENT_ROOT = STIM_ELEMENT_ROOT + "/rndspike";

    public static final String SCRIPT_OUT_ELEMENT_ROOT = "/script_outs";

    public static final String CONTROLS_ELEMENT_ROOT = "/controls";

    ArrayList<String> graphsCreated = new ArrayList<String>();

    SimConfig simConfig = null;

    MorphCompartmentalisation morphComp = null;

    Hashtable<String, Cell> mappedCells = new Hashtable<String, Cell>();
    Hashtable<String, SegmentLocMapper> mappedSegments = new Hashtable<String, SegmentLocMapper>();

    private GenesisFileManager()
    {
    }


    public GenesisFileManager(Project project)
    {
        this.project = project;
    }


    public void reset()
    {
        cellTemplatesGenAndIncl = new ArrayList<String>();
        graphsCreated = new ArrayList<String>();
        nextColour = 1; // reset it...

        addComments = project.genesisSettings.isGenerateComments();
    }




    public void generateTheGenesisFiles(SimConfig simConfig,
                                       MultiRunManager multiRunManager,
                                        MorphCompartmentalisation mc,
                                        int seed) throws GenesisException
    {
        this.simConfig = simConfig;

        this.multiRunManager = multiRunManager;

        this.removeAllPreviousGenesisFiles();

        morphComp = mc;

        randomSeed = seed;

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed

        addComments = project.genesisSettings.isGenerateComments();

        FileWriter fw = null;
        nextColour = 1; // reset it...

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


                        //System.out.println("Encoding, "+mainGenesisFile+": "+ fw.getEncoding());

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
        logger.logComment("... Created Main GENESIS file: " + mainGenesisFile);
        this.mainFileGenerated = true;

    }


    public int getCurrentRandomSeed()
    {
        return this.randomSeed;
    }


    private String getCellElementName(String cellGroupName, int cellNumber)
    {
        return getCellGroupElementName(cellGroupName)+"/"
            +SimEnvHelper.getSimulatorFriendlyName(cellGroupName) + "_" + cellNumber;
    }

    private String getCompElementName(Segment seg, String cellGroupName, int cellNumber)
    {
        return getCellElementName(cellGroupName, cellNumber)+"/"
            +SimEnvHelper.getSimulatorFriendlyName(seg.getSegmentName());
    }





    private String getCellGroupElementName(String cellGroupName)
    {
        return CELL_ELEMENT_ROOT+"/"
            +SimEnvHelper.getSimulatorFriendlyName(cellGroupName);
    }



    private String getRootCellFileElementName()
     {
         return FILE_ELEMENT_ROOT+ CELL_ELEMENT_ROOT;
     }



    private String getCellGroupFileElementName(String cellGroupName)
    {
        return getRootCellFileElementName()+"/"
            +SimEnvHelper.getSimulatorFriendlyName(cellGroupName);
    }


    /**
     * changes /cells/granule/granule_0/ to cells_granule_granule_0
     */
    public static String getSingleWordElementName(String oldName)
    {
        String newName = GeneralUtils.replaceAllTokens(oldName, "/", "_");
        if (newName.startsWith("_")) newName = newName.substring(1);

        newName = GeneralUtils.replaceAllTokens(newName, "-", "min");

        return newName;
    }


    /**
     * Note: no need to return fract along mapped seg for GENESIS seg/compartment...
     */
    public Segment getMappedSegment(String mappedCellType, int originalSegId, float fractAlong)
    {
        SegmentLocation oldSl = new SegmentLocation(originalSegId, fractAlong);
        logger.logComment("Request to map: " + oldSl + " on "+mappedCellType);
        Cell mappedCell = this.mappedCells.get(mappedCellType);
        SegmentLocMapper slm = mappedSegments.get(mappedCellType);
        SegmentLocation segLoc = slm.mapSegmentLocation(oldSl);

        logger.logComment(CellTopologyHelper.printDetails(mappedCell, project));
        logger.logComment("SegmentLocMapper: " + slm);

        Segment seg =mappedCell.getSegmentWithId(segLoc.getSegmentId());
        logger.logComment("oldSl: "+oldSl+" new segLoc: "+segLoc+", seg: "+seg);
        return seg;
    }


    private String generateStimulations() throws GenesisException
    {
        StringBuffer response = new StringBuffer();

        ArrayList<String> allStims = simConfig.getInputs();

        addMajorComment(response, "Adding "+allStims.size()+" stimulation(s)");


        response.append("create neutral " + STIM_ELEMENT_ROOT + "\n");
        response.append("create neutral " + PULSE_ELEMENT_ROOT + "\n");
        response.append("create neutral " + RNDSPIKE_ELEMENT_ROOT + "\n");



        for (int k = 0; k < allStims.size(); k++)
        {
            logger.logComment("++++++++++++     Checking for stim ref: " + allStims.get(k));

            ArrayList<SingleElectricalInput> allInputLocs =
                project.generatedElecInputs.getInputLocations(allStims.get(k));

            for (int j = 0; j < allInputLocs.size(); j++)
            {
                SingleElectricalInput input = allInputLocs.get(j);

                String cellGroup = input.getCellGroup();
                int cellNum = input.getCellNumber();

                if (!project.cellGroupsInfo.getAllCellGroupNames().contains(cellGroup))
                {
                    throw new GenesisException("The Cell Group specified for the Stimulation: " + allStims.get(k) +
                                               " does not exist!");
                }

                String stimCellType = project.cellGroupsInfo.getCellType(cellGroup);

                Segment segToStim = this.getMappedSegment(stimCellType, input.getSegmentId(), 0.5f);

                if (input.getElectricalInputType().equals(IClamp.TYPE))
                {
                    String stimElement = PULSE_ELEMENT_ROOT + "/"
                        + getSingleWordElementName("stim_" + allStims.get(k) + "_" +
                                                   getTopElementName(getCellElementName(cellGroup,
                        cellNum)));

                    response.append("create pulsegen " + stimElement + "\n");

                    IClampSettings iClamp = (IClampSettings) project.elecInputInfo.getStim(allStims.get(k));

                    double current = UnitConverter.getCurrent(iClamp.getAmplitude().getStart(),
                                                              UnitConverter.NEUROCONSTRUCT_UNITS,
                                                              project.genesisSettings.getUnitSystemToUse());

                    addComment(response, "Adding a current pulse of amplitude: " + current + " "
                                      +
                                      UnitConverter.currentUnits[project.genesisSettings.getUnitSystemToUse()].getSafeSymbol()
                                      + ", "+input+" \n");

                    addComment(response, "Pulses are shifted one dt step, so that pulse will begin at delay1, as in NEURON\n");

                    float recurDelay = Integer.MAX_VALUE; // A long time before any recurrance

                    if (iClamp.isRepeat())  recurDelay = 0;

                    response.append("setfield ^ level1 "
                                    + current
                                    + " width1 "
                                    + convertNeuroConstructTime(iClamp.getDuration().getStart())
                                    + " delay1 "
                                    + convertNeuroConstructTime(iClamp.getDelay().getStart() - project.simulationParameters.getDt())
                                    + " delay2 "
                                    + convertNeuroConstructTime(recurDelay)
                                    + "  \n");

                    String cellElement = getCellElementName(cellGroup,cellNum);

                    String realTargetElement = cellElement + "/"
                        + SimEnvHelper.getSimulatorFriendlyName(segToStim.getSegmentName());

                    response.append("addmsg " + stimElement + " " + realTargetElement
                                    + " INJECT output\n\n");

                }
                else if (input.getElectricalInputType().equals(RandomSpikeTrain.TYPE))
                {
                    addComment(response, "Adding a random spike to: "
                                      + getCellElementName(cellGroup,cellNum)
                                      + "\n");

                    String stimulatedElement = getCellElementName(cellGroup,cellNum)
                        + "/"
                        + SimEnvHelper.getSimulatorFriendlyName(segToStim.getSegmentName());

                    String stimulationElement = RNDSPIKE_ELEMENT_ROOT + "/"
                        + getSingleWordElementName("rndspike_" + allStims.get(k) + "_" +
                                                   getTopElementName(getCellElementName(cellGroup,cellNum)));

                    response.append("create randomspike " + stimulationElement + "\n");

                    RandomSpikeTrainSettings rndTrain =
                        (RandomSpikeTrainSettings)project.elecInputInfo.getStim(allStims.get(k));

                    response.append("setfield " + stimulationElement
                                    + " min_amp 1.0 "
                                    + "max_amp 1.0 "
                                    + " rate " +
                                    UnitConverter.getRate(rndTrain.getRate().getNextNumber(),
                                                          UnitConverter.NEUROCONSTRUCT_UNITS,
                                                          project.genesisSettings.getUnitSystemToUse())
                                    + " reset 1 "
                                    + " abs_refract "
                                    + UnitConverter.getTime(5f,
                                                            UnitConverter.NEUROCONSTRUCT_UNITS,
                                                            project.genesisSettings.getUnitSystemToUse())
                                    + " reset_value 0\n");

                    response.append("makechannel_" + rndTrain.getSynapseType()
                                    + " " + stimulatedElement
                                    + " SynChannel\n\n");

                    // link source and target...

                    response.append("addmsg " + stimulationElement + " "
                                    + stimulatedElement + "/SynChannel"
                                    + " SPIKE\n");

                }
            }
        }

        return response.toString();
    }



    /**
     * Add the specified extra script block
     */
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
    }



    /**
     * Generates the synaptic connections from the values in generatedNetworkConnections
     */
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

                    /** @todo Change handling of abs_refract */
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



    private void removeAllPreviousGenesisFiles()
    {
        cellTemplatesGenAndIncl.clear();

        File genesisFileDir = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(genesisFileDir, false, true);


    }


    public static String getGenesisFileHeader()
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
    }

    private String generateGlobals()
    {
        StringBuffer response = new StringBuffer();
        addComment(response, "This temperature is needed if any of the channels are temp dependent (Q10 dependence) \n");
        response.append("float celsius = " + project.simulationParameters.getTemperature() + "\n\n");

        response.append("str units = \"" + UnitConverter.getUnitSystemDescription(project.genesisSettings.getUnitSystemToUse()) + "\"\n\n");


        return response.toString();
    }

    private String printEnv()
    {
        StringBuffer response = new StringBuffer();

        if(addComments)
        {
            response.append("env // prints details on some global variables\n\n\n");
        }
        return response.toString();
    }




    private String generateChanMechIncludes() throws GenesisException
    {
        StringBuffer response = new StringBuffer();
        addComment(response, "Including channel mechanisms \n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> includedChanMechNames = new ArrayList<String>();

        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            dir = this.mainGenesisFile.getParentFile().getAbsolutePath() + System.getProperty("file.separator");

            dir = GeneralUtils.convertToCygwinPath(dir);
        }


        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            if(project.generatedCellPositions.getNumberInCellGroup(cellGroupName)>0)
            {

                String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);
                //Cell cell = project.cellManager.getCell(cellTypeName);
                Cell cell = this.mappedCells.get(cellTypeName);

                ArrayList<ChannelMechanism> chanMechNames = cell.getAllChannelMechanisms(false);

                //boolean foundFirstPassi

                for (int j = 0; j < chanMechNames.size(); j++)
                {
                    ChannelMechanism nextChanMech = chanMechNames.get(j);
                    logger.logComment("Cell in group " + cellGroupName + " needs channel mech: " + nextChanMech);

                    if (!includedChanMechNames.contains(nextChanMech.getName()) &&
                        project.generatedCellPositions.getNumberInCellGroup(cellGroupName) > 0)
                    {
                        CellMechanism cellProcess = project.cellMechanismInfo.getCellMechanism(nextChanMech.getName());

                        if (cellProcess == null)
                        {
                            throw new GenesisException("Problem including cell process: " + nextChanMech);

                        }

                        if ( (cellProcess.getMechanismType().equals(DistMembraneMechanism.CHANNEL_MECHANISM) ||
                              cellProcess.getMechanismType().equals(DistMembraneMechanism.ION_CONCENTRATION)) &&
                            ! (cellProcess instanceof PassiveMembraneMechanism))
                        {
                            File newProcessFile = new File(ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory()),
                                                           cellProcess.getInstanceName() + ".g");

                            boolean success = false;

                            if (cellProcess instanceof AbstractedCellMechanism)
                            {
                                success = ( (AbstractedCellMechanism) cellProcess).createImplementationFile(SimEnvHelper.GENESIS,
                                    project.genesisSettings.getUnitSystemToUse(),
                                    newProcessFile,
                                    project,
                                    false,
                                    addComments);
                            }
                            else if (cellProcess instanceof ChannelMLCellMechanism)
                            {
                                success = ( (ChannelMLCellMechanism) cellProcess).createImplementationFile(SimEnvHelper.
                                    GENESIS,
                                    project.genesisSettings.getUnitSystemToUse(),
                                    newProcessFile,
                                    project,
                                    false,
                                    addComments);
                            }

                            if (!success)
                            {
                                throw new GenesisException("Problem generating file for cell process: "
                                                           + nextChanMech
                                                           +
                                                           "\nPlease ensure there is an implementation for that process in GENESIS");

                            }

                            response.append("include " + getFriendlyDirName(dir) + cellProcess.getInstanceName() + "\n");
                            response.append("make_" + cellProcess.getInstanceName() + "\n\n");

                            includedChanMechNames.add(nextChanMech.getName());
                        }
                    }
                }
            }
            else
            {
                logger.logComment("No cells in group, ignoring the channels...");
            }

        }
        return response.toString();
    }


    private String generateSynMechIncludes() throws GenesisException
    {
        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            dir = this.mainGenesisFile.getParentFile().getAbsolutePath() + System.getProperty("file.separator");

            dir = GeneralUtils.convertToCygwinPath(dir);
        }

        StringBuffer response = new StringBuffer();
        addComment(response, "Including synaptic mech \n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> includedSynapses = new ArrayList<String>();

        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = (String) cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            //String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);
            //Cell cell = project.cellManager.getCell(cellTypeName);

            Vector<String> synNames = new Vector<String>();

            Iterator allNetConns = project.generatedNetworkConnections.getNamesNetConns();

            while (allNetConns.hasNext())
            {
                String netConnName = (String) allNetConns.next();

                logger.logComment("Checking net conn: "+ netConnName);

                if (project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION) > 0)
                {
                    if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                    {
                        if (project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName) != null
                            && cellGroupName.equals(project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName)))
                        {
                            Vector synapses = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                            for (int i = 0; i < synapses.size(); i++)
                            {
                                SynapticProperties next = (SynapticProperties) synapses.elementAt(i);
                                synNames.add(next.getSynapseType());
                            }
                        }
                    }
                    else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
                    {
                        if (project.volBasedConnsInfo.getTargetCellGroup(netConnName) != null
                            && cellGroupName.equals(project.volBasedConnsInfo.getTargetCellGroup(netConnName)))
                        {
                            Vector synapses = project.volBasedConnsInfo.getSynapseList(netConnName);
                            for (int i = 0; i < synapses.size(); i++)
                            {
                                SynapticProperties next = (SynapticProperties) synapses.elementAt(i);
                                synNames.add(next.getSynapseType());
                            }
                        }
                    }


                }
            }


            for (int j = 0; j < synNames.size(); j++)
            {
                String nextSynMechName = (String)synNames.elementAt(j);
                logger.logComment("Cell in group "+cellGroupName+" has synapse: "+ nextSynMechName);

                if (!includedSynapses.contains(nextSynMechName))
                {
                    logger.logComment("Adding it");
                    CellMechanism cellMech
                        = project.cellMechanismInfo.getCellMechanism(nextSynMechName);

                    if (cellMech==null)
                    {
                        throw new GenesisException("Problem including cell mech: " + nextSynMechName  +
                                                   "\nPlease ensure there is an implementation for that process in GENESIS");

                    }

                    File newMechFile = new File(ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory()),
                                                   cellMech.getInstanceName() + ".g");

                    boolean success = false;
                    if (cellMech instanceof AbstractedCellMechanism)
                    {

                          success  = ( (AbstractedCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                                                                                              project.genesisSettings.getUnitSystemToUse(),
                                                                                              newMechFile,
                                                                                              project,
                                                                                              false,
                                                                                              addComments);
                    }
                    else if(cellMech instanceof ChannelMLCellMechanism)
                    {
                        success
                            = ( (ChannelMLCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                                                                                              project.genesisSettings.getUnitSystemToUse(),
                                                                                              newMechFile,
                                                                                              project,
                                                                                              false,
                                                                                              addComments);


                    }

                    if (!success)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem generating file for cell mech: "
                                                  + nextSynMechName
                                                  +
                            "\nPlease ensure there is an implementation for that mech in GENESIS", null, null);

                        return "";
                    }

                    response.append("include "+ getFriendlyDirName(dir) + cellMech.getInstanceName() + "\n");

                    includedSynapses.add(nextSynMechName);

                }
                else
                {
                    logger.logComment("Ignoring, due to already being included");
                }
            }



        }

        Vector allStims = project.elecInputInfo.getAllStims();

        for (int i = 0; i < allStims.size(); i++)
        {
            StimulationSettings nextStim = (StimulationSettings) allStims.elementAt(i);
            if (nextStim instanceof RandomSpikeTrainSettings)
            {
                RandomSpikeTrainSettings randStim = (RandomSpikeTrainSettings)nextStim;

                if (!includedSynapses.contains(randStim.getSynapseType()))
                {
                    logger.logComment("Adding it");
                    CellMechanism cellMech
                        = project.cellMechanismInfo.getCellMechanism(randStim.getSynapseType());

                    if (cellMech==null)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem including cell mech: " + randStim.getSynapseType(), null, null);

                        return "";
                    }

                    File newMechFile = new File(ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory()),
                                                   cellMech.getInstanceName() + ".g");

                    boolean success = false;
                    if (cellMech instanceof AbstractedCellMechanism)
                    {
                       success = ( (AbstractedCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                            project.genesisSettings.getUnitSystemToUse(),
                            newMechFile,
                            project,
                            false,
                            addComments);
                    }
                    else if (cellMech instanceof ChannelMLCellMechanism)
                    {
                       success = ( (ChannelMLCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                            project.genesisSettings.getUnitSystemToUse(),
                            newMechFile,
                            project,
                            false,
                            addComments);
                    }



                    if (!success)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem generating file for cell mech: "
                                                  + randStim.getSynapseType()
                                                  +
                            "\nPlease ensure there is an implementation for that mech in GENESIS", null, null);

                        return "";
                    }

                    response.append("include " + getFriendlyDirName(dir) + cellMech.getInstanceName() + "\n");

                    includedSynapses.add(randStim.getSynapseType());

                }

            }
        }
        return response.toString();
    }



    private String generateRunSettings()
    {
        StringBuffer response = new StringBuffer();
        addMajorComment(response, "Settings for running the demo");
        response.append("\n");
        //response.append("tstop = "+project.simulationParameters.duration+"\n");

        response.append("setclock 0 "
                        + convertNeuroConstructTime(project.simulationParameters.getDt()));
        if (project.genesisSettings.isGenerateComments())
            response.append(" // " + getTimeUnitString());

        response.append("\n");

        return response.toString();
    }

    private String generateNumIntegMethod()
    {
        StringBuffer response = new StringBuffer();

        response.append(project.genesisSettings.numMethod.getScript());

        return response.toString();

    }

    private String generateRandomise()
    {
        StringBuffer response = new StringBuffer();

        addComment(response, "Initializes random-number generator");
        response.append("randseed "+this.randomSeed+"\n");
        return response.toString();

    }



    private String generateWelcomeComments()
    {
        StringBuffer response = new StringBuffer();
        if (!project.genesisSettings.isGenerateComments()) return "";

        response.append("echo \"\"\n");
        response.append("echo \"*****************************************************\"\n");
        response.append("echo \"\"\n");
        response.append("echo \"    neuroConstruct generated GENESIS simulation\"\n");
        response.append("echo \"    for project: "+ project.getProjectFile().getAbsolutePath() +"\"\n");
        response.append("echo \"\"\n");



        StringBuffer desc = new StringBuffer();
        if (project.getProjectDescription() != null) desc.append(project.getProjectDescription());
        for (int i = 0; i < desc.length(); i++)
        {
            if (desc.charAt(i) == '\n')
            {
                desc.replace(i, i + 1, " ");
            }
        }
        response.append("echo \"    Description: " + desc.toString() + "\"\n");

        response.append("echo \" \"\n");
        response.append("echo  \"*****************************************************\"\n\n\n");

        return response.toString();
    };


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

/*
    private String generateCellParamControl()
    {
        StringBuffer response = new StringBuffer();

        response.append("\n\n");

        addComment(response, "This code generates a number of controls for altering cell parameters\n");

        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        response.append("if (!{exists "+CONTROLS_ELEMENT_ROOT+"})\n"+
                "    create neutral "+CONTROLS_ELEMENT_ROOT+"\n"+
                "end\n");


        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);

            String paramControl = CONTROLS_ELEMENT_ROOT + "/params"+cellGroupName;

            response.append("create xform " + paramControl + " [700, 20, 250, 200] -title \"Parameters for cell "+cellTypeName+" in group "+cellGroupName+"\"\n");

            response.append("xshow " + paramControl + "\n\n");

            Cell cell = this.mappedCells.get(cellTypeName);

            ArrayList<ChannelMechanism> chanMechs = cell.getAllChannelMechanisms(false);

            String firstPassiveCellProc = null;

            for (int i = 0; i < chanMechs.size(); i++)
            {
                ChannelMechanism cm = chanMechs.get(i);

                CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(cm.getName());

                boolean isCMLPassive = false;
                if (cellMech instanceof ChannelMLCellMechanism)
                {
                    try
                    {
                        isCMLPassive = ( (ChannelMLCellMechanism) cellMech).isPassiveNonSpecificCond();
                    }
                    catch (CMLMechNotInitException e)
                    {
                        ChannelMLCellMechanism cp = (ChannelMLCellMechanism) cellMech;
                        try
                        {
                            cp.initialise(project, false);
                            isCMLPassive = cp.isPassiveNonSpecificCond();
                        }
                        catch (CMLMechNotInitException cmle)
                        {
                            // nothing more to try...
                        }
                        catch (ChannelMLException cmle2)
                        {
                            // nothing more to do...
                        }


                    }
                }

                if ((firstPassiveCellProc==null) &&
                    (cellMech instanceof PassiveMembraneMechanism || isCMLPassive))
                {
                    logger.logComment("Ignoring cell proc which will be used for Rm, etc.");
                    firstPassiveCellProc = cellMech.getInstanceName();
                }
                else
                {


                    String funcName = "toggle_" + cm.getName() + "_" + cellGroupName;
                    String xtogName = paramControl + "/Chan" + cm.getName();
                    String valName = "prevVal_" + cm.getName() + "_" + cellGroupName;

                    response.append("create xtoggle " + xtogName +
                                    " -onlabel \"" + cm.getName() + " density: " + UnitConverter.getConductanceDensity(
                                                     cm.getDensity(),
                                                     UnitConverter.NEUROCONSTRUCT_UNITS,
                                                     project.genesisSettings.getUnitSystemToUse()) + "\"" +
                                    " -offlabel \"" + cm.getName() + " density: 0\"" +
                                    " -state 1 " +
                                    " -script \"" + funcName + "\"\n\n");

                    response.append("float " + valName + "\n");

                    String allComps = "/cells/" + cellGroupName + "/#/#[][TYPE=compartment],/cells/" + cellGroupName +
                        "/#/#[][TYPE=symcompartment]";

                    response.append("function " + funcName + "\n" +
                                    "   echo Chan mech " + cm.getName() + " altered\n" +
                                    "   echo Toggle state: {getfield " + xtogName + " state}\n" +
                                    "   \n" +
                                    "   if ({getfield " + xtogName + " state}==0)\n" +
                                    "       " + valName + " = {getfield /cells/" + cellGroupName + "/" + cellGroupName +
                                    "_0/" + SimEnvHelper.getSimulatorFriendlyName(cell.getFirstSomaSegment().getSegmentName()) + "/" + cm.getName() +
                                    " Gbar}\n" +
                                    "       str tempCellName\n" +
                                    //"       foreach tempCellName ({el /cells/"+cellGroupName+"/#/#})\n"+
                                    "       foreach tempCellName ({el " + allComps + "})\n" +
                                    "           echo Setting chan dens of " + cm.getName() +
                                    " in {tempCellName} to 0 \n" +
                                    "           setfield {tempCellName}/" + cm.getName() + " Gbar 0 \n" +
                                    "       end\n" +
                                    "   else\n" +
                                    "       echo Turning channel back on with dens: {" + valName + "}\n" +
                                    "       foreach tempCellName ({el " + allComps + "})\n" +
                                    "           setfield {tempCellName}/" + cm.getName() + " Gbar {" + valName + "} \n" +
                                    "       end\n" +

                                    "   end\n" +
                                    "   \n" +
                                    "   \n" +
                                    "   \n" +
                                    "   \n" +
                                    "end\n\n");
                }
            }



        }

        return response.toString();
    }

*/


    public static float convertToGenesisValue(Float val, String simIndepVarName, int units)
    {
        if (simIndepVarName.equals(SimPlot.VOLTAGE))
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    units);

        }
        if (simIndepVarName.indexOf(SimPlot.SPIKE)>=0)
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    units);

        }

        else if (simIndepVarName.indexOf(SimPlot.COND_DENS) >= 0)
        {
            return (float) UnitConverter.getConductanceDensity(val,
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               units);
        }


        else if (simIndepVarName.indexOf(SimPlot.CURRENT)>=0)
        {
            return (float) UnitConverter.getCurrentDensity(val,
                                                           UnitConverter.NEUROCONSTRUCT_UNITS,
                                                           units);
        }

        else if (simIndepVarName.indexOf(SimPlot.REV_POT)>=0)
        {
            return (float) UnitConverter.getVoltage(val,
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    units);
        }
        else if (simIndepVarName.indexOf(SimPlot.CONCENTRATION)>=0)
        {
            return (float) UnitConverter.getConcentration(val,
                                                          UnitConverter.NEUROCONSTRUCT_UNITS,
                                                          units);


        }


        return val;
    }


    /**
     * To handle plotting/saving a variable
     */
    public class VariableHelper
    {
        private String compParentElement = null;
        private String compTopElementName = null;
        private String extraLines = null;
        private String variableName = null;

        private VariableHelper(){};

        public VariableHelper(PlotSaveDetails record, int cellNum, Segment seg) throws GenesisException
        {
            String compElement = getCompElementName(seg, record.simPlot.getCellGroup(), cellNum);
            compParentElement = getParentElement(compElement);
            compTopElementName = getTopElementName(compElement);

            String fullVarName = record.simPlot.getValuePlotted();

            if (fullVarName.equals(SimPlot.VOLTAGE))
            {
                variableName = "Vm";
            }
            if (fullVarName.indexOf(SimPlot.SPIKE) >= 0)
            {
                variableName = "Vm";
            }
            else if (fullVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
            {
                String extraElementPart
                    = fullVarName.substring(0,
                                           fullVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR));

                String mechanismName
                    = fullVarName.substring(0,
                                           fullVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR));

                String simIndepVarPart = fullVarName.substring(fullVarName.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) + 1);

                if (!extraElementPart.startsWith("/"))
                {
                    extraElementPart = "/" + extraElementPart;
                }

                String element = compElement + extraElementPart;

                compParentElement = getParentElement(element);
                compTopElementName = getTopElementName(element);

                if (simIndepVarPart.startsWith(SimPlot.COND_DENS))
                {
                   // variableName = "Gk";

                    variableName = "RESULT";

                    String convName = "CondDensConv_" + record.simPlot.getPlotReference() + "_"
                        + getSingleWordElementName(compElement);

                    String scriptOutElement = SCRIPT_OUT_ELEMENT_ROOT + "/" + convName;

                    compParentElement = getParentElement(scriptOutElement);
                    compTopElementName = getTopElementName(scriptOutElement);

                    double surfArea;
                    if (seg.getSegmentShape() == Segment.SPHERICAL_SHAPE)
                    {
                        surfArea = 4f * Math.PI * seg.getSegmentStartRadius() *
                            seg.getSegmentStartRadius();
                    }
                    else
                    {
                        SimpleCompartment comp = new SimpleCompartment(seg.getSegmentStartRadius(),
                                                                       seg.getRadius(),
                                                                       seg.getSegmentLength());

                        surfArea = comp.getCurvedSurfaceArea();
                    }
                    double unit = UnitConverter.getLength(1,
                                                          UnitConverter.NEUROCONSTRUCT_UNITS,
                                                          project.genesisSettings.getUnitSystemToUse());

                    surfArea = surfArea * unit * unit;

                    String cellNameRef = record.simPlot.getCellGroup() + "_" + cellNum;

                    extraLines = new String("\nif (!{exists " + SCRIPT_OUT_ELEMENT_ROOT + "})\n" +
                                            "    create neutral " + SCRIPT_OUT_ELEMENT_ROOT + "\n" +
                                            "end\n" +
                                            "\n" +
                                            "if (!{exists " + scriptOutElement + "})\n" +
                                            "    create script_out " + scriptOutElement + "\n" +
                                            "    addfield " + scriptOutElement + " RESULT\n" +
                                            "    setfield " + scriptOutElement + " command \"update_" + convName + "\"\n" +
                                            "\n" +
                                            "    float surf_area_" + cellNameRef + " = " + surfArea + "\n" +
                                            "\n" +

                                            "    function update_" + convName + "\n" +
                                            "        setfield " + scriptOutElement + " RESULT { {getfield " + element +
                                            " Gk} / surf_area_" + cellNameRef + "}\n" +
                                            "    end\n" +
                                            "\n" +
                                            "\n" +
                                            "end\n");

                }
                else
                {
                    logger.logComment("--------------     Looking to plot " + simIndepVarPart +
                                      " on cell mech: " + mechanismName);

                    CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(mechanismName);

                    logger.logComment("Cell mech found: " + cellMech);

                    if (cellMech == null)
                    {
                         throw new GenesisException("Problem generating plot/save for Cell Mechanism: " +
                                                  mechanismName);

                    }
                    else
                    {
                        if (simIndepVarPart.startsWith(SimPlot.CONCENTRATION))
                        {
                            logger.logComment("Looking to plot the concentration...");

                            String ion = simIndepVarPart.substring(simIndepVarPart.indexOf(SimPlot.
                                                                             PLOTTED_VALUE_SEPARATOR) + 1);

                            if (!ion.equals("ca"))
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "Note, Ca is only ion supported under GENESIS at the moment. Will have problems plotting "
                                                          + fullVarName, null, null);

                            }

                            // Will work for Ca at least...
                            variableName = ion.substring(0, 1).toUpperCase() + ion.substring(1);

                        }
                        else if (simIndepVarPart.indexOf(SimPlot.CURRENT)>=0)
                        {

                            //String ion = simIndepVarPart.substring(simIndepVarPart.indexOf(SimPlot.
                            //                                                 PLOTTED_VALUE_SEPARATOR) + 1);

                            //System.out.println("Looking to plot the current due to ion: " + ion + "...");

                            // Note: currents in normal channels aren't split between ions...

                            variableName = "RESULT";

                            String convName = "CurrConv_" + record.simPlot.getPlotReference() + "_"
                                + getSingleWordElementName(compElement);

                            String scriptOutElement = SCRIPT_OUT_ELEMENT_ROOT + "/" + convName;

                            compParentElement = getParentElement(scriptOutElement);
                            compTopElementName = getTopElementName(scriptOutElement);

                            double surfArea;
                            if (seg.getSegmentShape() == Segment.SPHERICAL_SHAPE)
                            {
                                surfArea = 4f * Math.PI * seg.getSegmentStartRadius() *
                                    seg.getSegmentStartRadius();
                            }
                            else
                            {
                                SimpleCompartment comp = new SimpleCompartment(seg.getSegmentStartRadius(),
                                                                               seg.getRadius(),
                                                                               seg.getSegmentLength());

                                surfArea = comp.getCurvedSurfaceArea();
                            }
                            double unit = UnitConverter.getLength(1,
                                                                  UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                  project.genesisSettings.getUnitSystemToUse());

                            surfArea = surfArea * unit * unit;

                            String cellNameRef = getSingleWordElementName(record.simPlot.getCellGroup() +"_"+cellNum); // cleans up -1 etc

                            extraLines = new String("\nif (!{exists " + SCRIPT_OUT_ELEMENT_ROOT + "})\n" +
                                "    create neutral " + SCRIPT_OUT_ELEMENT_ROOT + "\n" +
                                "end\n" +
                                "\n" +
                                "if (!{exists " + scriptOutElement + "})\n" +
                                "    create script_out " + scriptOutElement + "\n" +
                                "    addfield " + scriptOutElement + " RESULT\n" +
                                "    setfield " + scriptOutElement + " command \"update_" + convName + "\"\n" +
                                "\n" +
                                "    float surf_area_" + cellNameRef + " = " + surfArea + "\n" +
                                "\n" +

                                "    function update_" + convName + "\n" +
                                "        setfield " + scriptOutElement + " RESULT {-1 * {getfield " + element +
                                " Ik} / surf_area_" + cellNameRef + "}\n" +
                                "    end\n" +
                                "\n" +
                                "\n" +
                                "end\n");

                        }

                        else
                        {
                            if (cellMech instanceof ChannelMLCellMechanism)
                            {
                                try
                                {
                                    boolean foundGateVariable = false;
                                    int maxGates = 3; // for now...

                                    for (int gateIndex = 1; gateIndex <= maxGates; gateIndex++)
                                    {
                                        String xpath = ChannelMLConstants.getGateXPath(
                                            gateIndex);

                                        logger.logComment("Checking xpath: " + xpath);

                                        SimpleXMLEntity[] gate = ( (ChannelMLCellMechanism) cellMech).
                                            getXMLDoc().
                                            getXMLEntities(xpath);

                                        if (gate != null && gate.length > 0)
                                        {

                                            logger.logComment("Looking at: " + gate[0]);

                                            SimpleXMLEntity gateState = ( (SimpleXMLElement)
                                                                         gate[0]).getXMLEntities(ChannelMLConstants.
                                                STATE_ELEMENT)[0];

                                            logger.logComment("State: " +
                                                              gateState.getXMLString("", false));

                                            String stateName = ( (SimpleXMLElement) gateState).
                                                getAttributeValue(ChannelMLConstants.
                                                                  STATE_NAME_ELEMENT);

                                            if (stateName.equals(simIndepVarPart))
                                            {
                                                foundGateVariable = true;
                                                if (gateIndex == 1) variableName = "X"; // GENESIS convention...
                                                else if (gateIndex == 2) variableName = "Y";
                                                else if (gateIndex == 3) variableName = "Z";
                                            }
                                        }
                                    }
                                    if (!foundGateVariable && simIndepVarPart.startsWith(SimPlot.REV_POT))
                                    {
                                        variableName = "Ek"; // have to assume only one ion...
                                        foundGateVariable = true;
                                    }


                                    if (!foundGateVariable)
                                    {
                                        variableName = simIndepVarPart;
                                    }
                                }
                                catch (ChannelMLException ex)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "Problem generating plot/save " +
                                                              record.simPlot.getPlotReference() +
                                                              "with Cell Mechanism: " + mechanismName, ex, null);

                                }

                            }
                            else
                            {
                                logger.logError("Un supported type of Cell Mechanism");
                                variableName = simIndepVarPart;

                            }
                        }
                    }
                }

                logger.logComment("Plotting field : " + variableName
                                  + " in element: " + element);

            }
            logger.logComment("Created var helper for " +variableName+", on "+compTopElementName+" which is on "
                + compParentElement +", with extra script : ("+extraLines+")");


        }

        public String getVariableName()
        {
            return variableName;
        }

        public String getCompParentElementName()
        {
            return compParentElement;
        }

        public String getCompTopElementName()
        {
            return compTopElementName;
        }

        public String getCompFullElementName()
        {
            //System.out.println("getCompFullElementName: "+compParentElement +"/"+compTopElementName);
            return compParentElement +"/"+compTopElementName;
        }


        public String getExtraLines()
        {
            return extraLines==null ? "" : extraLines;
        }




    }



    public String generatePlots() throws GenesisException
    {
        StringBuffer response = new StringBuffer();

        ArrayList<PlotSaveDetails> plots = project.generatedPlotSaves.getPlottedPlotSaves();

        if (project.genesisSettings.isGraphicsMode())
        {

            addMajorComment(response, "Adding " + plots.size() + " plot(s)");

            for (PlotSaveDetails plot : plots)
            {
                if (plot.simPlot.isSynapticMechanism())
                {
                    String error = "Note, synaptic mechanism variable plotting/saving not supported yet in GENESIS, so not plotting: "+plot.simPlot;
                    logger.logError(error);

                    addComment(response, error);

                }
                else
                {
                    ArrayList<Integer> cellNumsToPlot = plot.cellNumsToPlot;
                    ArrayList<Integer> segIdsToPlot = plot.segIdsToPlot;

                    String plotFrameName = PLOT_ELEMENT_ROOT + "/" + plot.simPlot.getGraphWindow();

                    float minVal = convertToGenesisValue(plot.simPlot.getMinValue(),
                                                              plot.simPlot.getValuePlotted(),
                                                              project.genesisSettings.getUnitSystemToUse());

                    float maxVal = convertToGenesisValue(plot.simPlot.getMaxValue(),
                                                              plot.simPlot.getValuePlotted(),
                                                              project.genesisSettings.getUnitSystemToUse());

                    //Cell nextCell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(plot.simPlot.getCellGroup()));
                    String cellType = project.cellGroupsInfo.getCellType(plot.simPlot.getCellGroup());
                    //Cell nextCell = this.mappedCells.get(cellType);

                    for (Integer cellNum : cellNumsToPlot)
                    {
                        for (Integer segId : segIdsToPlot)
                        {
                            //Segment segInOrigCell = project.cellManager.getCell(cellType).getSegmentWithId(segId);

                            Segment segInMappedCell = this.getMappedSegment(cellType, segId, 0.5f);

                            //String compElement = getCellElementName(plot.simPlot.getCellGroup(), cellNum)
                            //    + "/" + SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName());

                            VariableHelper var = new VariableHelper(plot, cellNum, segInMappedCell);

                            String plotted = plot.simPlot.getValuePlotted();

                            if (!plotted.equals(var.getVariableName())) plotted = plotted + " (" + var.getVariableName() +
                                ")";

                            String dataSetName = var.getCompParentElementName() + "_" + var.getCompTopElementName() +
                                ":" + var.getVariableName();
                            //String dataSetName = var.getCompTopElementName() + ":" +var.getVariableName();

                            if (dataSetName.indexOf("Conv") >= 0) // i.e. maybe a converted amt, e.g. current
                            {
                                dataSetName = plot.simPlot.getValuePlotted();
                            }

                            if (dataSetName.length() > 28) // long names give errors...
                                dataSetName = "..." + dataSetName.substring(dataSetName.length() - 25);

                            response.append(createSinglePlot(plotFrameName,
                                                             dataSetName,
                                                             "Values of " + plotted + " in " +
                                                             getCellElementName(plot.simPlot.getCellGroup(),
                                cellNum),
                                                             var.getCompParentElementName(),
                                                             var.getCompTopElementName(),
                                                             var.getVariableName(),
                                                             maxVal,
                                                             minVal,
                                                             getNextColour(),
                                                             var.getExtraLines()));

                        }
                    }
                }

            }
        }
        else
        {
            addMajorComment(response, "Adding no plots, due to No Graphics Mode");

        }
        return response.toString();
    }


    /**
     * Generate a 3D xcell plot of the generated cells. Needs lots of work...
     */
    private String generate3Dplot()
    {
        StringBuffer response = new StringBuffer();

        if (project.genesisSettings.isShowShapePlot() && project.genesisSettings.isGraphicsMode())
        {
            /*
             response.append("create xform /cellform [50,50,800,400]\n");
             response.append("create xdraw /cellform/draw [0,0,100%,100%]\n");
             response.append("setfield /cellform/draw xmin -0.003 xmax 0.001 ymin -5e-5 ymax 5e-5 zmin -1e-3 zmax 1e-3 transform z\n");
             response.append("xshow /cellform\n");
             response.append("create xcell /cellform/draw/cell\n");
             response.append("setfield /cellform/draw/cell colmin -0.1 colmax 0.1 path "
                            + CELL_ELEMENT_ROOT+"/##[TYPE=symcompartment] field Vm script \"echo <w> <v>\"\n");
             */

            /** @todo Make these dependent on the size of the generated cell groups */
            response.append("create xform /form [0,0,400,400] -nolabel\n");
            response.append("create xdraw /form/draw [0,0,100%,100%] -wx " + quickConvertCm(2e-3) + " -wy " +
                            quickConvertCm(2e-3) + " -transform ortho3d -bg white\n");
            response.append("setfield /form/draw xmin " + quickConvertCm( -3e-4) + " xmax " + quickConvertCm(3e-4) +
                            " ymin " +
                            quickConvertCm( -3e-4) + " ymax " + quickConvertCm(3e-4) + " "
                            + "vx " + quickConvertCm(0) + " vy " + quickConvertCm(0) + " vz " + quickConvertCm( -2e-3) +
                            "\n");

            response.append(
                "create xcell /form/draw/cell -path \"/cells/##[][TYPE=compartment],/cells/##[][TYPE=symcompartment]\" "
                + " -colfield Vm -colmin -0.07 -colmax 0.03 -diarange -5\n");

            // response.append("useclock /form/draw/cell 1\n");
            //     response.append("script \"echo widget clicked on = <w> value = <v>\"\n");

            response.append("xcolorscale hot\n");
            response.append("xshow /form\n");
        }
        return response.toString();

    }

    private String generateRunControls()
    {
        StringBuffer response = new StringBuffer();

        response.append("\n\n");

        addMajorComment(response, "Creating a simple Run Control");

        response.append("if (!{exists "+CONTROLS_ELEMENT_ROOT+"})\n"+
                        "    create neutral "+CONTROLS_ELEMENT_ROOT+"\n"+
                        "end\n");

        String runControl = CONTROLS_ELEMENT_ROOT + "/runControl";
        response.append("create xform "+runControl + " [700, 20, 200, 120] -title \"Run Controls\"\n");

        response.append("xshow " + runControl+"\n\n");

        response.append("create xbutton " + runControl+"/RESET -script reset\n");
        response.append("create xbutton " + runControl+"/RUN -script \"step "
                        + ( (int) (getSimDuration() /
                                   project.simulationParameters.getDt())) + "\"\n"); // +1 to include 0 and last timestep
       response.append("create xbutton " + runControl+"/QUIT -script quit\n\n");


        return response.toString();
    }


    /**
     * Quick way to convert length to cm if the units are Physiological
     */
    private double quickConvertCm(double siValue)
    {
        return UnitConverter.getLength(siValue, UnitConverter.GENESIS_SI_UNITS, project.genesisSettings.getUnitSystemToUse());

    }


    private String createSinglePlot(String plotFrameName,
                                    String dataSetName,
                                    String plotFrameTitle,
                                    String cellElement,
                                    String internalElement,
                                    String fieldToPlot,
                                    float max,
                                    float min,
                                    String colour,
                                    String extraLinesB4Plot)
    {
        logger.logComment("+++++++++++++++++++++++++++++++    Creating plot for: "
                          + cellElement + "/" + internalElement
                          + ", field: " + fieldToPlot);

        StringBuffer response = new StringBuffer();


        if (graphsCreated.size()==0)
        {
            response.append("create neutral "+PLOT_ELEMENT_ROOT+"\n\n");
        }


        if (!graphsCreated.contains(plotFrameName))
        {

            response.append("\ncreate xform "
                            + plotFrameName
                            + " [500,100,400,400]  -title \"" + plotFrameTitle + "\"\n");

            response.append("xshow " + plotFrameName + "\n");

            response.append("create xgraph "
                            + plotFrameName + "/graph"
                            + " -xmin 0 -xmax "
                            + convertNeuroConstructTime(getSimDuration())
                            + " -ymin " + min
                            + " -ymax " + max + "\n");

            graphsCreated.add(plotFrameName);
        }

        String realFieldName = fieldToPlot;
        String realMsgElement = cellElement + "/" + internalElement;

        if (project.genesisSettings.getNumMethod().isHsolve() &&
            project.genesisSettings.getNumMethod().getChanMode() >=2)
        {
            String hsolveElement = cellElement  + "/" + HSOLVE_ELEMENT_NAME;

            realFieldName = "{findsolvefield "
                + hsolveElement
                + " " + cellElement + "/" + internalElement
                + " " + fieldToPlot + "}";

            realMsgElement = hsolveElement;
        }

        if (extraLinesB4Plot!=null && extraLinesB4Plot.trim().length()>0)
        {
            addComment(response, "Some extra lines...");
            response.append(extraLinesB4Plot+"\n");
        }



        response.append("addmsg "
                        + realMsgElement
                        + " "
                        + plotFrameName
                        + "/graph"
                        + " PLOT "
                        +realFieldName
                        +" *"+dataSetName
                        + " *" + colour + "\n");



        return response.toString();

    }

    /**
     * Gets the top element name, i.e returns MossyFibers_0 for /cells/MossyFibers/MossyFibers_0
     */
    private String getTopElementName(String fullElementPath)
    {
        return fullElementPath.substring(fullElementPath.lastIndexOf("/")+1);
    }

    /**
     * Gets /cells/MossyFibers for /cells/MossyFibers/MossyFibers_0
     */

    private String getParentElement(String fullElementPath)
    {
        if (fullElementPath.indexOf("/")<0)
        {
            return fullElementPath+"/..";
        }
        String fullParentPath = fullElementPath.substring(0,fullElementPath.lastIndexOf("/"));

        return fullParentPath;
    }


    private double convertNeuroConstructTime(double time)
    {
        return UnitConverter.getTime(time,
                                     UnitConverter.NEUROCONSTRUCT_UNITS,
                                     project.genesisSettings.getUnitSystemToUse());

    }

    private String getTimeUnitString()
    {
        return UnitConverter.timeUnits[project.genesisSettings.getUnitSystemToUse()].toString();

    }


    /**
     * Adds a line commented out by //, and an empty line after the comment
     */
    public static void addComment(StringBuffer responseBuffer, String comment)
    {
        logger.logComment("Adding GENESIS comment: "+ comment);
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//   " + comment + "\n");
        responseBuffer.append("\n");
    }

    /**
     * Adds a line commented out by //, but with no empty line after the comment
     */
    public static void addQuickGenesisComment(StringBuffer responseBuffer, String comment)
    {
        logger.logComment("Adding GENESIS comment: "+ comment);
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
    }


    private void generateCellMappings()
    {
        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);

            Cell origCell = project.cellManager.getCell(cellTypeName);

            Cell mappedCell = this.morphComp.getCompartmentalisation(origCell);

            mappedCells.put(cellTypeName, mappedCell);
            mappedSegments.put(cellTypeName, morphComp.getSegmentMapper());
        }
    }

    private String generateCellGroups() throws GenesisException
    {

        StringBuffer response = new StringBuffer();
        response.append("\n");

        //Vector cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        //System.out.println(simConfig.toLongString());

        if (cellGroupNames.size() == 0)
        {
            logger.logComment("There are no cell groups!!");

            addMajorComment(response, "There were no cell groups specified in the project, might be a pretty boring simulation...");
            return response.toString();
        }
        response.append("\ncreate neutral "+CELL_ELEMENT_ROOT+"\n\n");



        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);

            addMajorComment(response, "Cell group "
                                       + ii
                                       + ": "
                                       + cellGroupName
                                       + " has cells of type: "
                                       + cellTypeName);

            response.append("\ncreate neutral " + getCellGroupElementName(cellGroupName) + "\n");


            Cell mappedCell = this.mappedCells.get(cellTypeName);

            File dirForGenFiles = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());;

            logger.logComment("Dir: " + dirForGenFiles);

            GenesisMorphologyGenerator cellMorphGen
                = new GenesisMorphologyGenerator(mappedCell,
                                                 project,
                                                 dirForGenFiles);

            String filenameToBeGenerated = cellMorphGen.getFilename();

            logger.logComment("Will need a cell template file called: " +
                              filenameToBeGenerated);

            if (cellTemplatesGenAndIncl.contains(filenameToBeGenerated))
            {
                logger.logComment("It's already been generated!");
            }
            else
            {
                logger.logComment("Generating it...");
                try
                {
                    cellMorphGen.generateFile();

                    cellTemplatesGenAndIncl.add(filenameToBeGenerated);
                }
                catch (GenesisException ex)
                {
                    logger.logError("Problem generating one of the template files...", ex);
                    throw ex;
                }
            }

            ArrayList cellGroupPositions =  project.generatedCellPositions.getPositionRecords(cellGroupName);

            String currentRegionName = project.cellGroupsInfo.getRegionName(cellGroupName);


            addComment(response, "Adding cells of type " + cellTypeName + " in region " + currentRegionName);

            Region regionInfo = project.regionsInfo.getRegionObject(currentRegionName);
            CellPackingAdapter packer = project.cellGroupsInfo.getCellPackingAdapter(cellGroupName);

            addComment(response, "Placing these cells in a region described by: "+ regionInfo);
            addComment(response, "Packing has been generated by: "+ packer.toString());

            response.append("\nstr compName\n");

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                filenameToBeGenerated = GeneralUtils.convertToCygwinPath(filenameToBeGenerated);
            }



            for (int cellNumber = 0; cellNumber < cellGroupPositions.size(); cellNumber++)
            {
                PositionRecord posRecord
                    = (PositionRecord) cellGroupPositions.get(cellNumber);

                logger.logComment("Moving cell number: " + cellNumber + " into place");

                if (cellNumber != posRecord.cellNumber)
                {
                    // not really a problem, but best to highlight it...

                    logger.logComment("-------------------------                Position number " + cellNumber
                                      + " doesn't match cell number: " + posRecord);
                    // continue...
                }
                String firstElementName = getCellElementName(cellGroupName, 0);

                String newElementName = getCellElementName(cellGroupName, cellNumber);

                if (cellNumber==0)
                {
                    response.append("\nreadcell "
                                    + filenameToBeGenerated
                                    + " "
                                    + newElementName
                                    + "\n");
                }
                else
                {
                    response.append("\n\ncopy "
                                    + firstElementName
                                    + " "
                                    + newElementName
                                    + "\n");

                }


                response.append("position "+ newElementName +" "
                   +UnitConverter.getLength(posRecord.x_pos,
                                            UnitConverter.NEUROCONSTRUCT_UNITS,
                                            project.genesisSettings.getUnitSystemToUse())
                   +" "+UnitConverter.getLength(posRecord.y_pos,
                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                project.genesisSettings.getUnitSystemToUse())
                   +" "+UnitConverter.getLength(posRecord.z_pos,
                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                project.genesisSettings.getUnitSystemToUse())
                   +"\n\n");

                   if (mappedCell.getInitialPotential().getDistributionType()!=NumberGenerator.FIXED_NUM)
                   {
                       response.append("setfield " + newElementName + "/# initVm " +
                                       UnitConverter.getVoltage(mappedCell.getInitialPotential().getNextNumber(),
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                project.genesisSettings.getUnitSystemToUse()) + "\n");

                   }



                //Point3f point = new Point3f(posRecord.x_pos, posRecord.y_pos, posRecord.z_pos);

                //PositionedCell cellPosn = new PositionedCell(point, cellGroupName, cellTypeName);

            }

            logger.logComment("\n              ++++++++    Calculating ion exchange, conc dep, etc for: "+mappedCell.getInstanceName()+"...");

            ArrayList<ChannelMechanism> chanMechs = mappedCell.getAllChannelMechanisms(false);
            logger.logComment("Chan mechs: "+ chanMechs);

            Hashtable<String, ArrayList<String>> ionCurrentSources = new Hashtable<String,ArrayList<String>>();
            Hashtable<String, ArrayList<String>> ionRateDependence = new Hashtable<String,ArrayList<String>>();
            Hashtable<String, ArrayList<String>> ionConcentration = new Hashtable<String,ArrayList<String>>();

            for (int j = 0; j < chanMechs.size(); j++)
            {
                logger.logComment(j+"   -    Looking at Chan mech...: "+chanMechs.get(j));

                String nextChanMech = chanMechs.get(j).getName();
                CellMechanism cellProc = project.cellMechanismInfo.getCellMechanism(nextChanMech);


                if (cellProc instanceof ChannelMLCellMechanism)
                {
                    ChannelMLCellMechanism cmlp = (ChannelMLCellMechanism)cellProc;

                    String xpath = ChannelMLConstants.getIonsXPath();
                    logger.logComment("Checking xpath: " + xpath);

                    try
                    {
                        SimpleXMLEntity[] ions = cmlp.getXMLDoc().getXMLEntities(xpath);

                        if (ions != null)
                        {
                            for (int k = 0; k < ions.length; k++)
                            {
                                SimpleXMLElement ionElement = (SimpleXMLElement)ions[k];

                                logger.logComment("Got entity: " + ionElement.getXMLString("", false));
                                String name = ionElement.getAttributeValue(ChannelMLConstants.ION_NAME_ATTR);
                                String role = ionElement.getAttributeValue(ChannelMLConstants.ION_ROLE_ATTR);
                                logger.logComment("Ion name: "+ name + ", role: "+ role);

                                if (role!=null && (role.equals(ChannelMLConstants.ION_ROLE_MODULATING) ||
                                    role.equals(ChannelMLConstants.ION_ROLE_MODULATING_v1_2)))
                                {
                                    ArrayList<String> cellProcsDepOnIonConc = ionRateDependence.get(name);
                                    
                                    if (cellProcsDepOnIonConc==null)
                                    {
                                        cellProcsDepOnIonConc = new ArrayList<String> ();
                                        ionRateDependence.put(name, cellProcsDepOnIonConc);
                                    }
                                    cellProcsDepOnIonConc.add(cellProc.getInstanceName());

                                }
                                else if (role!=null && (role.equals(ChannelMLConstants.ION_ROLE_SIGNALLING) ||
                                    role.equals(ChannelMLConstants.ION_ROLE_SIGNALLING_v1_2)))
                                {
                                    ArrayList<String> cellProcsInfluencingConc = ionConcentration.get(name);
                                    if (cellProcsInfluencingConc==null)
                                    {
                                        cellProcsInfluencingConc = new ArrayList<String> ();
                                        ionConcentration.put(name, cellProcsInfluencingConc);
                                    }
                                    cellProcsInfluencingConc.add(cellProc.getInstanceName());

                                }
                                else
                                {
                                    logger.logComment("Ignoring Transmitted, wait to see if they're explicitly mentioned in a channel...");
                                }

                            }
                        }
                        String ohmicXpath = ChannelMLConstants.getOhmicXPath();
                        logger.logComment("Checking xpath: " + xpath);


                        SimpleXMLEntity[] ohmicElement = cmlp.getXMLDoc().getXMLEntities(ohmicXpath);

                        if (ohmicElement!=null && ohmicElement.length>0)
                        {

                            String ion_name = ((SimpleXMLElement)ohmicElement[0]).getAttributeValue(ChannelMLConstants.OHMIC_ION_ATTR);
                            logger.logComment("Found transmitted ion: "+ion_name);

                            ArrayList<String> cellProcsTransmittingIon = ionCurrentSources.get(ion_name);
                            if (cellProcsTransmittingIon == null)
                            {
                                cellProcsTransmittingIon = new ArrayList<String> ();
                                ionCurrentSources.put(ion_name, cellProcsTransmittingIon);
                            }
                            cellProcsTransmittingIon.add(cellProc.getInstanceName());

                        }
                        else
                        {
                            logger.logComment("No ohmic relation, so assuming no transmitted ion...");
                        }


                        logger.logComment("ionConcentration: "+ ionConcentration);
                        logger.logComment("ionRateDependence: "+ ionRateDependence);
                        logger.logComment("ionCurrentSources: "+ ionCurrentSources);

                    }
                    catch(ChannelMLException ex)
                    {
                        logger.logError( "Problem extracting ion info from Cell Process: "+ cmlp+", file: "+cmlp.getChannelMLFile(), ex);

                    }
                }
                else
                {
                    logger.logComment("Note: "+ nextChanMech +", cell proc: "+ cellProc +" will not be used when linking ion sources and sinks...");
                }
            }

            Enumeration<String> ionsAffectingRates = ionRateDependence.keys();

            boolean nameDefined = false;

            while (ionsAffectingRates.hasMoreElements())
            {
                String ion = ionsAffectingRates.nextElement();
                ArrayList<String> cellProcsAffected = ionRateDependence.get(ion);

                if (!nameDefined)
                {
                    response.append("str tempCompName\n\n");
                    nameDefined = true;
                }

                addComment(response,
                                       "The concentration of: " + ion + " has an effect on rate of " + cellProcsAffected);

                response.append("foreach tempCompName ({el "+getCellGroupElementName(cellGroupName)+"/#/#})\n");

                ArrayList<String> cellProcsConcs = ionConcentration.get(ion);

                String concVariable = "C"; // futureproofing. I beleve this is used in concenpool as Ca is used in Ca_concen
                if (ion.equals("ca")) concVariable = "Ca";

                for (int concIndex = 0; concIndex < cellProcsConcs.size(); concIndex++)
                {
                    String ionConcCellProc = cellProcsConcs.get(concIndex);
                    response.append("    if ({exists  {tempCompName}/" + ionConcCellProc + "})\n");

                    for (int rateDepIndex = 0; rateDepIndex < cellProcsAffected.size(); rateDepIndex++)
                    {
                        response.append("        if ({exists  {tempCompName}/" + cellProcsAffected.get(rateDepIndex) + "})\n");
                        response.append("            addmsg {tempCompName}/" + ionConcCellProc + " {tempCompName}/" +
                                        cellProcsAffected.get(rateDepIndex) + " CONCEN " + concVariable + "\n");
                        response.append("        end\n");

                    }

                    response.append("    end\n");
                }

                response.append("end\n\n");
            }


            Enumeration<String> currentsAffectingConcs = ionCurrentSources.keys();

            while (currentsAffectingConcs.hasMoreElements())
            {
                String ion = currentsAffectingConcs.nextElement();
                ArrayList<String> cellProcsTransmittingIon = ionCurrentSources.get(ion);

                logger.logComment("Ion "+ion+" is transmitted by "+ cellProcsTransmittingIon);

                ArrayList<String> cellProcsConcs = ionConcentration.get(ion);

                logger.logComment("Ion "+ion+" has conc cell processes: "+cellProcsConcs);

                if (cellProcsConcs!=null)
                {
                    if (!nameDefined)
                    {
                        response.append("str tempCompName\n\n");
                        nameDefined = true;
                    }
                    addComment(response, "Ion "+ion+" is transmitted by "+ cellProcsTransmittingIon
                                    +" affecting conc cell processes: "+cellProcsConcs);


                    response.append("foreach tempCompName ({el "+getCellGroupElementName(cellGroupName)+"/#/#})\n");

                    // Note currently this only works for Ca_Concen...
                    String concCurrentName = "I_"+ ion.substring(0,1).toUpperCase();
                    if (ion.length()>1) concCurrentName = concCurrentName + ion.substring(1);


                    for (int concIndex = 0; concIndex < cellProcsConcs.size(); concIndex++)
                    {
                        String ionConcCellProc = cellProcsConcs.get(concIndex);

                        response.append("    if ({exists  {tempCompName}/" + ionConcCellProc + "})\n");

                        for (int transIndex = 0; transIndex < cellProcsTransmittingIon.size(); transIndex++)
                        {
                            response.append("        if ({exists  {tempCompName}/" + cellProcsTransmittingIon.get(transIndex) + "})\n");

                            response.append("            addmsg {tempCompName}/"+cellProcsTransmittingIon.get(transIndex)+" {tempCompName}/"+ionConcCellProc+" "+concCurrentName+" Ik\n");
                            response.append("        end\n");
                        }
                        response.append("    end\n");

                    }

                    response.append("end\n\n");
                }
                else
                {
                    logger.logComment("No cell proc for that ion.");
                }
            }





            response.append("\n");

            logger.logComment("***  Finished looking at cell group number " + ii + ", called: " + cellGroupName);
        }

        return response.toString();

    }



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



/*
                commandToExecute = executable
                    + " "
                    + titleOpt
                    + title
                    + workdirOpt
                    + dirToRunIn.getAbsolutePath()
                    + " "
                    + extraArgs
                    + " "
                    + genesisExecutable
                    + " "
                    + mainGenesisFile.getName();*/



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

    public String getNextColour()
    {
        String colour = null;

        colour = ColourUtils.getColourName(nextColour).toLowerCase();

        nextColour++;
        if (nextColour >= 10) nextColour = 1;
        return colour;
    }



    public String getMainGenesisFileName() throws GenesisException
    {
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new GenesisException("GENESIS file not yet generated");
        }

        return this.mainGenesisFile.getAbsolutePath();

    }

    public static String getFriendlyDirName(String fileDirName)
    {

        Pattern p = Pattern.compile("\\\\"); // looking for one \ (there's \\ needed for java, and twice this for perl)
        Matcher m = p.matcher(fileDirName);
        String friendlyDirName = m.replaceAll("\\/"); // replacing with \\ (see above)

        if (friendlyDirName.indexOf("Program Files")>=0)
        {
            friendlyDirName = GeneralUtils.replaceAllTokens(friendlyDirName, "Program Files", "Progra~1");
        }
        if (friendlyDirName.indexOf("Documents and Settings")>=0)
        {
            friendlyDirName = GeneralUtils.replaceAllTokens(friendlyDirName, "Documents and Settings", "Docume~1");
        }
                logger.logComment("a friendlyDirName: " + friendlyDirName);

        if (GeneralUtils.isWindowsBasedPlatform())
        {
            boolean canFix = true;


            // Can catch spaces if a dir is called c:\Padraig Gleeson and change it to c:\Padrai~1
            while (friendlyDirName.indexOf(" ") > 0 && canFix)
            {
                int indexOfSpace = friendlyDirName.indexOf(" ");

                int prevSlash = friendlyDirName.substring(0, indexOfSpace).lastIndexOf("/");
                int nextSlash = friendlyDirName.indexOf("/", indexOfSpace);

                String spacedWord = friendlyDirName.substring(prevSlash + 1, nextSlash);

                logger.logComment("spacedWord: " + spacedWord);

                if (spacedWord.indexOf(" ") < 6) canFix = false;
                else
                {
                    String shortened = spacedWord.substring(0, 6) + "~1";
                    friendlyDirName = GeneralUtils.replaceAllTokens(friendlyDirName, spacedWord, shortened);
                    logger.logComment("filename now: " + friendlyDirName);
                }
            }
        }
                logger.logComment("c friendlyDirName now: " + friendlyDirName);


        return friendlyDirName;

    }


    /**
     * Generates the code for saving the simulation values to asc_file elements, and so to
     * text files, for later playback in neuroConstruct
     */
    private String generateGenesisSimulationRecording() throws GenesisException
    {
        StringBuffer response = new StringBuffer();

        //String whatToSave = "Vm";

        response.append("\n");

        if (addComments) response.append("echo Checking and resetting...\n\n");

        response.append("maxwarnings 400\n\n");


        ArrayList<PlotSaveDetails> recordings = project.generatedPlotSaves.getSavedPlotSaves();

        if (recordings.size()>0)
        {
            addMajorComment(response, "Recording " + recordings.size() + " variable(s)");
            newRecordingToBeMade = true;
        }



        /// ********************
        if (multiRunManager!=null)
            response.append(multiRunManager.getMultiRunPreScript(SimEnvHelper.GENESIS));
        /// ********************

       /////////// response.append("check\n");
        response.append("reset\n");


        File dirForDataFiles = getDirectoryForSimulationFiles();
        String dataFileDirName = dirForDataFiles.getAbsolutePath() + System.getProperty("file.separator");

        String friendlyDataDirName = getFriendlyDirName(dataFileDirName);


        if (newRecordingToBeMade)
        {
            File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

            response.append("str simsDir\n");
            response.append("simsDir = \"" + dirForSims.getAbsolutePath() + "/\"\n\n");

            response.append("str simReference\n");
            response.append("simReference = \"" + project.simulationParameters.getReference() + "\"\n\n");

            response.append("str targetDir\n");
            response.append("targetDir =  {strcat {simsDir} {simReference}}\n");
            response.append("targetDir =  {strcat {targetDir} {\"/\"}}\n\n");

            if (addComments)
            {
                response.append("echo\n");
                response.append("echo\n");
                response.append("echo     Preparing recording of cell parameters\n");
                response.append("echo\n");
                response.append("echo\n\n");
            }


            logger.logComment("Data files will be put in dir: " + dirForDataFiles);



            logger.logComment("Creating the simulation code");

            /** @todo There is certainly an easier way to do this... */
            String timeFileElement = FILE_ELEMENT_ROOT + "/timefile";


            addComment(response, "Saving file containing time details");

            response.append("create neutral " + FILE_ELEMENT_ROOT + "\n");
            response.append("create asc_file " + timeFileElement + "\n");
            response.append("setfield " + timeFileElement + "    flush 1    leave_open 1    append 1  notime 1\n");
            //response.append("str targetDir\n");
            
            response.append("setfield " + timeFileElement + " filename {strcat {targetDir} {\"" + SimulationData.TIME_DATA_FILE +  "\"}}\n");
            response.append("call " + timeFileElement + " OUT_OPEN\n");
            //response.append("call " + timeFileElement +
            //                " OUT_WRITE \"//This is a file containing the time values of the simulation\"\n");
            response.append("float i, timeAtStep\n");

            int steps = ( (int) (getSimDuration() /
                                 project.simulationParameters.getDt()));
            response.append("for (i = 0; i <= "
                            + steps
                            + "; i = i + 1"
                            + ")\n");

            response.append("timeAtStep = " + convertNeuroConstructTime(project.simulationParameters.getDt()) + " * i\n");
            response.append("call " + timeFileElement + " OUT_WRITE {timeAtStep} \n");

            response.append("end\n\n\n");

            response.append("str cellName\n");
            response.append("str compName\n");

            response.append("create neutral " + getRootCellFileElementName() + "\n");
            if (addComments) response.append("echo Created: " + getRootCellFileElementName() + "\n\n\n");

            for (PlotSaveDetails record: recordings)
            {
                if (record.simPlot.isSynapticMechanism())
                {
                    String error = "Note, synaptic mechanism variable plotting/saving not supported yet in GENESIS, so not saving: "+record.simPlot;
                    logger.logError(error);

                    addComment(response, error);
                }
                else
                {

                    addComment(response, record.getDescription(true));
                    String cellGroupName = record.simPlot.getCellGroup();
                    int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

                    String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
                    //Cell cell = project.cellManager.getCell(cellType);
                    //Cell cell = this.mappedCells.get(cellType);

                    boolean isSpikeRecording = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;

                    if (numInCellGroup > 0)
                    {
                        String cellGroupFileEl = getCellGroupFileElementName(cellGroupName);

                        response.append("if (!{exists " + cellGroupFileEl + "})\n"
                                        + "    create neutral " + cellGroupFileEl + "\n" +
                                        "end\n\n");

                        if (record.allCellsInGroup)
                        {
                            response.append("foreach cellName ({el " + getCellGroupElementName(cellGroupName) +
                                            "/#})\n");

                            String fileDir = FILE_ELEMENT_ROOT + "{cellName}";

                            response.append("    if (!{exists " + fileDir + "})\n"
                                            + "        create neutral " + fileDir + "\n" + "    end\n\n");

                            response.append("    ce {cellName}\n\n");

                            for (Integer segId : record.segIdsToPlot)
                            {
                                Segment segInOrigCell = project.cellManager.getCell(cellType).getSegmentWithId(segId);

                                Segment segInMappedCell = this.getMappedSegment(cellType, segId, 0.5f);

                                addComment(response,
                                                "Recording at segInOrigCell: " +
                                                SimEnvHelper.getSimulatorFriendlyName(segInOrigCell.getSegmentName()) +
                                                " (Id: "
                                                + segInOrigCell.getSegmentId() + "), segInMappedCell: " +
                                                segInMappedCell);

                                response.append("    compName = {strcat {cellName} /" +
                                                SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName()) +
                                                "}\n");

                                String compElement = "{cellName}/" +
                                    SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName());

                                VariableHelper var = new VariableHelper(record, -1, segInMappedCell);

                                String fileElement = FILE_ELEMENT_ROOT + "{compName}" + record.simPlot.getSafeVarName();
                                if (segInOrigCell.getSegmentId() != segInMappedCell.getSegmentId())
                                {
                                    fileElement = FILE_ELEMENT_ROOT + "{compName}_" + segInMappedCell.getSegmentId() +
                                        "_" + record.simPlot.getSafeVarName();
                                }

                                // Note: putting it in a file referring to the ORIGINAL segment
                                String segFileNamePart = "." + segInOrigCell.getSegmentId();

                                String extension = "dat";
                                if (isSpikeRecording) extension = "spike";

                                if (record.segIdsToPlot.size() == 1 && record.segIdsToPlot.get(0) == 0)
                                {
                                    segFileNamePart = ""; // used to represent whole cell
                                }

                                String varFileNamePart = "";
                                if (!record.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                                {
                                    varFileNamePart = "." + record.simPlot.getSafeVarName();
                                }

                                if (!isSpikeRecording)
                                {
                                    response.append("    create asc_file " + fileElement + "\n");
                                    response.append("    setfield " + fileElement +
                                                    "    flush 1    leave_open 1    append 1 notime 1\n");

                                    response.append("    str fileNameStr\n");
                                    response.append("    fileNameStr = {strcat {getpath {cellName} -tail} {\"" + segFileNamePart + varFileNamePart +
                                                    "." + extension + "\"} }\n");

                                    response.append("    setfield " + fileElement + " filename {strcat {targetDir} {fileNameStr}}\n");

                                }
                                else
                                {
                                    /** @todo Get better solution for this. */
                                    addComment(response,
                                        "Note: currently event_tofile is used for spike saving. This saves all times that the voltage is above threshold.");
                                    response.append("    create event_tofile " + fileElement + "\n");
                                    response.append("    echo Created:  " + fileElement + "\n");

                                    //response.append("    setfield " + fileElement + "    open 1\n");

                                    if (record.simPlot.getValuePlotted().indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) > 0)
                                    {
                                        String thresholdString = record.simPlot.getValuePlotted().substring(record.
                                            simPlot.
                                            getValuePlotted().indexOf(SimPlot.
                                                                      PLOTTED_VALUE_SEPARATOR) + 1);

                                        try
                                        {
                                            float threshold = Float.parseFloat(thresholdString);
                                            threshold = convertToGenesisValue(threshold,
                                                record.simPlot.getValuePlotted(),
                                                project.genesisSettings.getUnitSystemToUse());

                                            response.append("    setfield " + fileElement + " threshold  " + threshold +
                                                            "\n");
                                        }
                                        catch (NumberFormatException ex)
                                        {
                                            addComment(response,
                                                            "Error:  failuer to set threshold taken from: " +
                                                            record.simPlot.getValuePlotted());
                                        }
                                    }
                                    else
                                    {
                                        float threshold = convertToGenesisValue(SimPlot.DEFAULT_THRESHOLD,
                                            record.simPlot.getValuePlotted(),
                                            project.genesisSettings.getUnitSystemToUse());

                                        response.append("    setfield " + fileElement + " threshold  " + threshold +
                                                        "\n");

                                    }

                                    response.append("    setfield " + fileElement + " fname { strcat {strcat {targetDir} "  +
                                                    "{getpath {cellName} -tail}} {\"" + segFileNamePart + varFileNamePart +
                                                    "." + extension + "\"}}\n");

                                }

                                String realElementToRecord = compElement; //var.getCompFullElementName();

                                String realVariableToSave = var.getVariableName();

                                response.append("    " + var.getExtraLines());

                                if (project.genesisSettings.getNumMethod().isHsolve() &&
                                    project.genesisSettings.getNumMethod().getChanMode() >= 2)
                                {
                                    String hsolveElement = "{getpath {compName} -head}/" + var.getCompTopElementName() +
                                        HSOLVE_ELEMENT_NAME;

                                    realVariableToSave = "{findsolvefield " + hsolveElement + " " + "{compName}"
                                        + " " + var.getVariableName() + "}";

                                    realElementToRecord = hsolveElement;
                                }
                                if (!isSpikeRecording)
                                {
                                    response.append("    addmsg " + realElementToRecord + " " + fileElement + " SAVE " +
                                                    realVariableToSave + "\n");
                                    response.append("    call " + fileElement + " OUT_OPEN\n");
                                    response.append("    call " + fileElement + " OUT_WRITE {getfield "
                                                    + realElementToRecord + " " + realVariableToSave + "}\n\n");

                                }
                                else
                                {
                                    response.append("    addmsg " + realElementToRecord + " " + fileElement + " INPUT " +
                                                    realVariableToSave + "\n");
                                    response.append("    call " + fileElement + " OPEN\n");
                                }

                            }
                            response.append("end\n\n");
                        }
                        else
                        {
                            for (Integer cellNum : record.cellNumsToPlot)
                            {
                                String cellEl = getCellElementName(cellGroupName, cellNum);

                                addComment(response, "Recording cell: " + cellEl);

                                response.append("cellName = \"" + cellEl + "\"\n");

                                String fileDir = FILE_ELEMENT_ROOT + "{cellName}";

                                response.append("    if (!{exists " + fileDir + "})\n"
                                                + "        create neutral " + fileDir + "\n" + "    end\n\n");

                                response.append("ce {cellName}\n\n");

                                for (Integer segId : record.segIdsToPlot)
                                {
                                    //Segment segToRecord = cell.getSegmentWithId(segId);

                                    Segment segInOrigCell = project.cellManager.getCell(cellType).getSegmentWithId(
                                        segId);

                                    Segment segInMappedCell = this.getMappedSegment(cellType, segId, 0.5f);

                                    addComment(response,
                                                    "Recording at segInOrigCell: " +
                                                    SimEnvHelper.getSimulatorFriendlyName(segInOrigCell.getSegmentName()) +
                                                    " (Id: "
                                                    + segInOrigCell.getSegmentId() + "), segInMappedCell: " +
                                                    segInMappedCell);

                                    response.append("compName = {strcat {cellName} /" +
                                                    SimEnvHelper.
                                                    getSimulatorFriendlyName(segInMappedCell.getSegmentName()) +
                                                    "}\n");

                                    String fileElement = FILE_ELEMENT_ROOT + "{compName}" +
                                        record.simPlot.getSafeVarName();
                                    if (segInOrigCell.getSegmentId() != segInMappedCell.getSegmentId())
                                    {
                                        fileElement = FILE_ELEMENT_ROOT + "{compName}_" + segInMappedCell.getSegmentId() +
                                            "_" +
                                            record.simPlot.getSafeVarName();
                                    }

                                    //String compElement = "{cellName}/" +
                                    //    SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName());

                                    VariableHelper var = new VariableHelper(record, cellNum, segInMappedCell);

                                    // Note: putting it in a file referring to the ORIGINAL segment
                                    String segFileNamePart = "." + segInOrigCell.getSegmentId();

                                    String extension = "dat";
                                    if (isSpikeRecording) extension = "spike";

                                    if (record.segIdsToPlot.size() == 1 && record.segIdsToPlot.get(0) == 0)
                                    {
                                        segFileNamePart = ""; // used to represent whole cell
                                    }

                                    String varFileNamePart = "";
                                    if (!record.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                                    {
                                        varFileNamePart = "." + record.simPlot.getSafeVarName();
                                    }

                                    if (!isSpikeRecording)
                                    {
                                        response.append("create asc_file " + fileElement + "\n");
                                        response.append("setfield " + fileElement +
                                                        "    flush 1    leave_open 1    append 1 notime 1\n");

                                        response.append("setfield " + fileElement + " filename { strcat {strcat {targetDir} "  +
                                                        "{getpath {cellName} -tail} } {\"" + segFileNamePart +
                                                        varFileNamePart + "." + SimPlot.CONTINUOUS_DATA_EXT + "\"}}\n");

                                    }
                                    else
                                    {
                                        /** @todo Get better solution for this. */
                                        addComment(response,
                                                        "Note: currently event_tofile is used for spike saving. This saves all times that the voltage is above threshold.");
                                        response.append("    create event_tofile " + fileElement + "\n");
                                        response.append("    echo Created:  " + fileElement + "\n");

                                        if (record.simPlot.getValuePlotted().indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR) >
                                            0)
                                        {
                                            String thresholdString = record.simPlot.getValuePlotted().substring(record.
                                                simPlot.
                                                getValuePlotted().indexOf(SimPlot.
                                                                          PLOTTED_VALUE_SEPARATOR) + 1);

                                            float threshold = 0.0F;
                                            try
                                            {
                                                threshold = Float.parseFloat(thresholdString);
                                                threshold = convertToGenesisValue(threshold,
                                                    record.simPlot.getValuePlotted(),
                                                    project.genesisSettings.getUnitSystemToUse());

                                                response.append("    setfield " + fileElement + " threshold  " +
                                                                threshold + "\n");
                                            }
                                            catch (NumberFormatException ex)
                                            {
                                                addComment(response,
                                                                "Error:  failuer to set threshold taken from: " +
                                                                record.simPlot.getValuePlotted());
                                            }

                                        }
                                        else
                                        {
                                            float threshold = convertToGenesisValue(SimPlot.DEFAULT_THRESHOLD,
                                                record.simPlot.getValuePlotted(),
                                                project.genesisSettings.getUnitSystemToUse());

                                            response.append("    setfield " + fileElement + " threshold  " + threshold +
                                                            "\n");

                                        }

                                        response.append("    setfield " + fileElement + " fname { strcat {strcat {targetDir} " +
                                                        "{getpath {cellName} -tail} } {\"" + segFileNamePart +
                                                        varFileNamePart + "." + extension + "\"}}\n");

                                    }

                                    String realElementToRecord = var.getCompFullElementName();

                                    String realVariableToSave = var.getVariableName();

                                    response.append("    " + var.getExtraLines());

                                    if (project.genesisSettings.getNumMethod().isHsolve() &&
                                        project.genesisSettings.getNumMethod().getChanMode() >= 2)
                                    {
                                        String hsolveElement = "{getpath {compName} -head}/" +
                                            var.getCompTopElementName() + HSOLVE_ELEMENT_NAME;

                                        realVariableToSave = "{findsolvefield " + hsolveElement + " " + "{compName}"
                                            + " " + var.getVariableName() + "}";

                                        realElementToRecord = hsolveElement;

                                    }

                                    if (!isSpikeRecording)
                                    {

                                        response.append("addmsg " + realElementToRecord + " " + fileElement + " SAVE " +
                                                        realVariableToSave + "\n");

                                        response.append("call " + fileElement + " OUT_OPEN\n");
                                        response.append("call " + fileElement + " OUT_WRITE {getfield "
                                                        + realElementToRecord + " " + realVariableToSave + "}\n\n");

                                    }
                                    else
                                    {
                                        response.append("    addmsg " + realElementToRecord + " " + fileElement +
                                                        " INPUT " +
                                                        realVariableToSave + "\n");
                                        response.append("    call " + fileElement + " OPEN\n");
                                    }

                                }
                            }
                        }
                    }
                }
                }
            }

        addMajorComment(response, "This will run a full simulation when the file is executed");

        /////////  response.append("check\n");


        response.append(generateScriptBlock(ScriptLocation.BEFORE_FINAL_RESET));
        response.append("reset\n");
        response.append(generateScriptBlock(ScriptLocation.AFTER_FINAL_RESET));

        //String startTimeFile = friendlyDirName+"starttime";
        //String stopTimeFile = friendlyDirName+"stoptime";

        response.append("str startTimeFile\n");
        response.append("str stopTimeFile\n");


        response.append("startTimeFile = {strcat {targetDir} {\"starttime\"}}\n");

        response.append("stopTimeFile = {strcat {targetDir} {\"stoptime\"}}\n");
        

        response.append("echo \"Starting simulation reference: "+project.simulationParameters.getReference()+" at: \" {getdate}\n");
        response.append("sh {strcat {\"date +%s.%N > \"} {startTimeFile}}\n\n");

        response.append("step "+((int)Math.round(getSimDuration()/project.simulationParameters.getDt()))+"\n\n"); // +1 to include 0 and last timestep

        response.append("echo \"Finished simulation reference: "+project.simulationParameters.getReference()+" at: \" {getdate}\n");

        response.append("sh {strcat {\"date +%s.%N > \"} {stopTimeFile}}\n\n"); // if you know a better way to get output of a system command from a sh call, let me know...

        if (addComments) response.append("echo Data stored in directory: {targetDir}\n\n");


        response.append("openfile {startTimeFile} r\n");
        response.append("openfile {stopTimeFile} r\n");


        response.append("float starttime = {readfile {startTimeFile}}  \n");
        response.append("float stoptime =  {readfile {stopTimeFile}}  \n");
        response.append("float runTime = {stoptime - starttime}  \n");

        response.append("echo Simulation took : {runTime} seconds  \n");


        response.append("closefile {startTimeFile} \n");
        response.append("closefile {stopTimeFile} \n\n\n");
        

        response.append("str hostnameFile\n");
        response.append("hostnameFile = {strcat {targetDir} {\"hostname\"}}\n");

        response.append("sh {strcat {\"hostname > \"} {hostnameFile}}\n");
        response.append("openfile {hostnameFile} r\n");
        response.append("str hostnamestr = {readfile {hostnameFile}}\n");
        response.append("closefile {hostnameFile}\n\n");


        response.append("str simPropsFile\n");
        response.append("simPropsFile = {strcat {targetDir} {\""+SimulationsInfo.simulatorPropsFileName+"\"}}\n");
        
        response.append("openfile {simPropsFile} w\n");

        response.append("writefile {simPropsFile} \"RealSimulationTime=\"{runTime}\n");
        response.append("writefile {simPropsFile} \"Host=\"{hostnamestr}\n");
        response.append("closefile {simPropsFile} \n");






        addComment(response, "This will ensure the data files don't get written to again..");

        response.append("str fileElement\n");
        response.append("foreach fileElement ({el "+FILE_ELEMENT_ROOT+CELL_ELEMENT_ROOT+"/##[][TYPE=asc_file]})\n");
        //response.append("echo Written from element {fileElement} to file {getfield {fileElement} filename}\n\n");
        //response.append("    deletemsg {fileElement} 0 -incoming\n");
        response.append("end\n");

        response.append("foreach fileElement ({el "+FILE_ELEMENT_ROOT+CELL_ELEMENT_ROOT+"/##[][TYPE=event_tofile]})\n");
        response.append("    echo Closing {fileElement}\n\n");
        response.append("    call {fileElement} CLOSE\n");
        response.append("end\n");


        /// ********************
        if (multiRunManager!=null)
            response.append(multiRunManager.getMultiRunPostScript(SimEnvHelper.GENESIS));
        /// ********************



        return response.toString();

    }


    public static void main(String[] args)
    {

        try
        {


            getFriendlyDirName("\\doccccccc and se\\paaaaaat gell\\1234");
            System.exit(2);




            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File("examples/Ex-Simple/Ex-Simple.neuro.xml"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);
            GenesisFileManager gen = new GenesisFileManager(p);



            OriginalCompartmentalisation oc = new OriginalCompartmentalisation();


            MultiRunManager multiRunManager = new MultiRunManager(pm.getCurrentProject(),
                                                  p.simConfigInfo.getDefaultSimConfig(),
                                                  p.simulationParameters.getReference());


            gen.generateTheGenesisFiles(p.simConfigInfo.getDefaultSimConfig(), multiRunManager, oc, 12345);
            //gen.runGenesisFile();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
