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
 *  it under the terms of the GNU General Public License as published byi
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

package ucl.physiol.neuroconstruct.genesis;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.regex.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.DataSet;
import ucl.physiol.neuroconstruct.gui.MainApplication;
import ucl.physiol.neuroconstruct.gui.plotter.PlotManager;
import ucl.physiol.neuroconstruct.gui.plotter.PlotterFrame;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings.KnownSimulators;
import ucl.physiol.neuroconstruct.hpc.mpi.QueueInfo;
import ucl.physiol.neuroconstruct.hpc.mpi.RemoteLogin;
import ucl.physiol.neuroconstruct.hpc.utils.ProcessFeedback;
import ucl.physiol.neuroconstruct.hpc.utils.ProcessManager;
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
 *  
 */

public class GenesisFileManager
{
    private static final ClassLogger logger = new ClassLogger("GenesisFileManager");

    Project project = null;

    File mainGenesisFile = null;

    int randomSeed = 0;

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    ArrayList<String> cellTemplatesGenAndIncl = new ArrayList<String>();

    boolean newRecordingToBeMade = false;

    private Hashtable<String, Integer> nextColour = new Hashtable<String, Integer>();

    private static boolean addComments = true;

    MultiRunManager multiRunManager = null;

    public static final String GEN_CORE_VARIABLE = "genesisCore";
    public static final String GEN_CORE_GENESIS = "GENESIS2";
    public static final String GEN_CORE_MOOSE = "MOOSE";
    public static final String GEN_CORE_NEUROSPACES = "Neurospaces";

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
    
        
    private boolean quitAfterRun = false;
        
    static
    {
        logger.setThisClassVerbose(false);
    }


    private GenesisFileManager()
    {
    }


    public GenesisFileManager(Project project)
    {
        this.project = project;
    }
    
    
    
    public boolean mooseCompatMode()
    {
        return project.genesisSettings.isMooseCompatMode();
    }


    public void reset()
    {
        cellTemplatesGenAndIncl = new ArrayList<String>();
        graphsCreated = new ArrayList<String>();
        nextColour = new Hashtable<String, Integer>(); // reset it...

        addComments = project.genesisSettings.isGenerateComments();
    }



    // using GenesisCompartmentalisation by default...
    public void generateTheGenesisFiles(SimConfig simConfig,
                                        int seed) throws GenesisException, IOException
    {
        generateTheGenesisFiles(simConfig, null, new GenesisCompartmentalisation(), seed);
    }
    public void generateTheGenesisFiles(SimConfig simConfig,
                                        MorphCompartmentalisation mc,
                                        int seed) throws GenesisException, IOException
    {
        generateTheGenesisFiles(simConfig, null, mc, seed);
    }

    public void generateTheGenesisFiles(SimConfig simConfig,
                                       MultiRunManager multiRunManager,
                                        MorphCompartmentalisation mc,
                                        int seed) throws GenesisException, IOException
    {
 
        this.removeAllPreviousGenesisFiles();

        File dirForGeneratedFiles = getGeneratedCodeDir();

        logger.logComment("Starting generation of the files into: "+dirForGeneratedFiles+", exists: "+dirForGeneratedFiles.exists());


        File utilsFile = ProjectStructure.getGenesisUtilsFile();
        GeneralUtils.copyFileIntoDir(utilsFile, dirForGeneratedFiles);

        long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;

        this.multiRunManager = multiRunManager;

        morphComp = mc;

        randomSeed = seed;

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed

        addComments = project.genesisSettings.isGenerateComments();

        FileWriter fw = null;
        nextColour = new Hashtable<String, Integer>(); // reset it...

        if (!(project.genesisSettings.getUnitSystemToUse()==UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS
              || project.genesisSettings.getUnitSystemToUse()==UnitConverter.GENESIS_SI_UNITS))
            throw new
                GenesisException("The units specified for generation of the GENESIS code are not recognised: "
                                 +project.genesisSettings.getUnitSystemToUse());


        try
        {
            generateCellMappings();

            mainGenesisFile = new File(dirForGeneratedFiles, project.getProjectName() + ".g");

            logger.logComment("generating: "+ mainGenesisFile);
            fw = new FileWriter(mainGenesisFile);

            fw.write(getGenesisFileHeader());

            fw.write(generateWelcomeComments());

            fw.write(generateRandomise());

            fw.write(generateGlobals());

            fw.write(generateIncludes());

            if (!mooseCompatMode()) fw.write(printEnv());

            fw.write(generateChanMechIncludes());

            fw.write(generateSynMechIncludes());

            fw.write(generateScriptBlock(ScriptLocation.BEFORE_CELL_CREATION));

            GeneralUtils.timeCheck("B4 generateCellGroups");
            fw.write(generateCellGroups());

            fw.write(generateScriptBlock(ScriptLocation.AFTER_CELL_CREATION));
            

            GeneralUtils.timeCheck("B4 generateNetworkConnections");
            fw.write(generateNetworkConnections());

            fw.write(generateStimulations());

            //fw.write(generateAfterCreationText());

            if (!mooseCompatMode()) fw.write(generateNumIntegMethod());

            fw.write(generateRunSettings());

            if (!mooseCompatMode()) fw.write(generatePlots());

            
            if (!mooseCompatMode()) fw.write(generate3Dplot());


            if (!mooseCompatMode()) fw.write(generateRunControls());

            //fw.write(generateCellParamControl());
            

            fw.write(generateGenesisSimulationRecording());


            fw.write(generateScriptBlock(ScriptLocation.AFTER_SIMULATION));

            if(mooseCompatMode()) fw.write("\n/*\n");
            if(mooseCompatMode()) fw.write("\n*/\n");
            
            
            if (quitAfterRun || 
                project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.NO_CONSOLE) ||
                simConfig.getMpiConf().isRemotelyExecuted())
            {
                fw.write(generateQuit());
            }
            
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
                throw new GenesisException("Error creating file: " + mainGenesisFile.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the GENESIS files you are trying to generate are not currently being used", ex1);
            }
            throw new GenesisException("Error creating file: " + mainGenesisFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the GENESIS files you are trying to generate are not currently being used", ex);

        }

        this.mainFileGenerated = true;

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("... Created Main GENESIS file: " + mainGenesisFile
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
    
    public void setQuitAfterRun(boolean quit)
    {
        this.quitAfterRun = quit;
    }
    
    
    private String generateQuit()
    {
        StringBuilder response = new StringBuilder();

        addComment(response,
                          " The script will quit after finishing...\n");
        /*
        response.append("\ncreate asc_file /finished\n");
        response.append("setfield /finished filename {strcat {targetDir} {\"finished\"}}\n");

        response.append("call /finished OUT_OPEN\n");
        response.append("call /finished FLUSH\n\n");*/

        response.append("\nexit\n");

        return response.toString();
    }
    


    private String getCellElementName(String cellGroupName, int cellNumber)
    {
        return getCellGroupElementName(cellGroupName)+"/"
            +SimEnvHelper.getSimulatorFriendlyName(cellGroupName) + "_" + cellNumber;
    }

    private String getCompElementName(Segment seg, String cellGroupName, int cellNumber)
    {
        if (cellNumber == -1)
            cellNumber = 0;
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

        //logger.logComment(CellTopologyHelper.printDetails(mappedCell, project));
        logger.logComment("SegmentLocMapper: " + slm);

        Segment seg =mappedCell.getSegmentWithId(segLoc.getSegmentId());
        logger.logComment("oldSl: "+oldSl+" new segLoc: "+segLoc+", seg: "+seg);
        return seg;
    }


    private String generateStimulations() throws GenesisException
    {
        StringBuilder response = new StringBuilder();

        ArrayList<String> allStims = project.generatedElecInputs.getNonEmptyInputRefs();

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

                Segment segToStim = this.getMappedSegment(stimCellType, input.getSegmentId(), input.getFractionAlong());

                if (input.getElectricalInputType().equals(IClamp.TYPE))
                {
                    String stimElement = PULSE_ELEMENT_ROOT + "/"
                        + getSingleWordElementName("stim_" + allStims.get(k) + "_" +
                                                   getTopElementName(getCellElementName(cellGroup,
                        cellNum)));

                    response.append("create pulsegen " + stimElement + "\n\n");
                    
                    IClampSettings iClamp = (IClampSettings) project.elecInputInfo.getStim(allStims.get(k));
                    
                    float del = -1, dur = -1, amp = -1;
                    
                    if (input.getInstanceProps()!=null)
                    {
                        IClampInstanceProps icip = (IClampInstanceProps)input.getInstanceProps();
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


                    double current = UnitConverter.getCurrent(amp,
                                                              UnitConverter.NEUROCONSTRUCT_UNITS,
                                                              project.genesisSettings.getUnitSystemToUse());

                    addComment(response, "Adding a current pulse of amplitude: " + current + " "
                                      +
                                      UnitConverter.currentUnits[project.genesisSettings.getUnitSystemToUse()].getSafeSymbol()
                                      + ", "+input+"");

                    float delToUse = del;

                    if(delToUse>0)
                    {
                        addComment(response, "Pulses are shifted one dt step, so that pulse will begin at delay1, as in NEURON");
                        delToUse = del - project.simulationParameters.getDt();
                    }

                    float recurDelay = 10000000; // A long time before any recurrance

                    if (iClamp.isRepeat())  recurDelay = 0;

                    response.append("setfield ^ level1 "
                                    + current
                                    + " width1 "
                                    + convertNeuroConstructTime(dur)
                                    + " delay1 "
                                    + convertNeuroConstructTime(delToUse)
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

                    float absRefractVal = 0f;

                    response.append("setfield " + stimulationElement
                                    + " min_amp 1.0 "
                                    + "max_amp 1.0 "
                                    + " rate " +
                                    (float)UnitConverter.getRate(rndTrain.getRate().getNextNumber(),
                                                          UnitConverter.NEUROCONSTRUCT_UNITS,
                                                          project.genesisSettings.getUnitSystemToUse())
                                    + " reset 1 "
                                    + " abs_refract "
                                    + (float)UnitConverter.getTime(absRefractVal,
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
        StringBuilder response = new StringBuilder();

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
        StringBuilder response = new StringBuilder();
        response.append("\n");

        Iterator allNetConnNames = project.generatedNetworkConnections.getNamesNetConnsIter();

        if (!allNetConnNames.hasNext())
        {
            logger.logComment("There are no synaptic connections");
            return "";
        }

        addMajorComment(response, "Adding Network Connections");

        boolean addedSpikegenCompatFunc = false;

        // Adding specific network connections...
        while (allNetConnNames.hasNext())
        {
            String netConnName = (String) allNetConnNames.next();

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
            
            boolean isGapJunction = false;
            
            if (synPropList.size()==1)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(synPropList.get(0).getSynapseType());
                if (cm.getMechanismType().equals(CellMechanism.GAP_JUNCTION))
                    isGapJunction = true;
            }


            // go through all of the synapse types. There will be only one for a simple network connection,
            // but multiple for complex net conns...
            for (int synapseIndex = 0; synapseIndex < allSynapses.size(); synapseIndex++)
            {
                GeneratedNetworkConnections.SingleSynapticConnection syn = allSynapses.get(synapseIndex);

                for (int synPropIndex = 0; synPropIndex < synPropList.size(); synPropIndex++)
                {
                    SynapticProperties synProps = synPropList.elementAt(synPropIndex);

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

                    if (substituteConnPoints.isEmpty() || // there is no ApPropSpeed on cell
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

                    if (syn.props==null || syn.props.isEmpty())
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

                    if(!isGapJunction)
                    {
                        // put synaptic start point on source axon

                        String spikeElement = triggeringElement + "/spike";
                        
                        float absRefractVal = project.genesisSettings.getAbsRefractSpikegen();

                        if (!mooseCompatMode() && !addedSpikegenCompatFunc && absRefractVal<0)
                        {
                            addComment(response, "Note this function is to help make the spikegen behave like a NetCon in NEURON\n" +
                                    "i.e. the spikegen will pass a spike event on crossing threshold, not pass any event while the presyn location stays above threshold,\n" +
                                    "and resume listening for passing threshold once it has gone below again. There will be no extra refractory period.");

                            response.append("function __doSpikingCheck__(action)\n\n");
                            response.append("    float input = {getmsg . -incoming -slot 0 0}\n\n");
                            response.append("    //echo \"Spiking state: \"{getfield spiking}\", input: \"{input}\n");
                            response.append("    \n");
                            response.append("    if ({getfield spiking} == 0)\n");
                            response.append("        call . PROCESS -parent  // Carry out all normal actions\n");
                            response.append("        if (input >= {getfield thresh})\n");
                            response.append("           setfield spiking 1\n");
                            response.append("        end\n");
                            response.append("    else\n");
                            response.append("        if (input < {getfield thresh})\n");
                            response.append("           setfield spiking 0\n");
                            response.append("        end\n");
                            response.append("    end\n");
                            response.append("end\n\n");
                                    

                            addedSpikegenCompatFunc=true;

                        }

                        response.append("if (!({exists " + spikeElement + "}))\n");
                        response.append("    create spikegen " + spikeElement + "\n\n");


                        if (absRefractVal<0)
                        {
                            if (mooseCompatMode())
                            {
                                addComment(response, "Note that in MOOSE (as of SVN rev 1388) if a negative abs_refract is set, the spikegen behaves like a NetCon in NEURON\n" +
                                        "i.e. the spikegen will pass a spike event on crossing threshold, not pass any event while the presyn location stays above threshold,\n" +
                                        "and resume listening for passing threshold once it has gone below again. There will be no extra refractory period.");
                            }
                        }
                    
                        response.append("    setfield " + spikeElement + "  thresh "
                                        + (float)UnitConverter.getVoltage(synProps.getThreshold(),
                                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                   project.genesisSettings.getUnitSystemToUse())

                                        + "  abs_refract "
                                        + (float)UnitConverter.getTime(absRefractVal,
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                project.genesisSettings.getUnitSystemToUse())
                                        + " output_amp 1\n");

                        response.append("    addmsg  " + triggeringElement
                                        + "  " + spikeElement + "  INPUT Vm\n");


                        if (!mooseCompatMode() && absRefractVal<0)
                        {
                            response.append("    addfield " + spikeElement + " spiking\n");
                            response.append("    setfield " + spikeElement + " spiking 0\n");


                            response.append("    addaction " + spikeElement + " PROCESS __doSpikingCheck__\n\n");
                        }

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
                        if (!mooseCompatMode())
                        {
                            response.append("msgnum = {getfield " + receivingElement + "/" + newSynapseName
                                        + " nsynapses} - 1\n\n");
                        }
                        else
                        {
                            response.append("msgnum = {getfield " + receivingElement + "/" + newSynapseName
                                        + " numSynapses} -1 \n\n");
                            response.append("echo \"msgnum is: \" {msgnum}\n\n");
                        }



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
                                        + (float)UnitConverter.getTime(  (synInternalDelay + apSegmentPropDelay + apSpaceDelay),
                                                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                project.genesisSettings.getUnitSystemToUse())
                                        + "\n\n");
                    }
                    else  /// isGapJunction
                    {
                        
                        response.append("connectGapJunction_" + synProps.getSynapseType()
                                        + " " + receivingElement
                                        + " " + triggeringElement + " "+weight+"\n\n");
                    }

                }

            }
        }
        return response.toString();
        }

    private File getGeneratedCodeDir()
    {
        File genFileDir = null;

        if (!mooseCompatMode())
            return ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());
        else
            return ProjectStructure.getMooseCodeDir(project.getProjectMainDirectory());

    }

    private void removeAllPreviousGenesisFiles()
    {
        cellTemplatesGenAndIncl.clear();

        File genFileDir = getGeneratedCodeDir();

        GeneralUtils.removeAllFiles(genFileDir, false, true, true);


    }


    public static String getGenesisFileHeader()
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

    private String generateIncludes()
    {
        StringBuilder response = new StringBuilder();

        boolean copyToSimDataDir = project.genesisSettings.isCopySimFiles();
        
        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            if (!copyToSimDataDir)
            {
                dir = this.mainGenesisFile.getParentFile().getAbsolutePath()+ System.getProperty("file.separator");
            }
            else
            {
                dir = getDirectoryForSimulationFiles()+ System.getProperty("file.separator");
            }

            //if (!(new File(dir)))
            dir = GeneralUtils.convertToCygwinPath(dir);
        }
        
        //if (!mooseCompatMode())
        {
            addComment(response, "Including neuroConstruct utilities file");
            response.append("include "+ getFriendlyDirName(dir)+"nCtools \n\n");
        }

        addComment(response, "Including external files");

        if (!mooseCompatMode()) 
        {
            response.append("include compartments \n\n");
        }


        addComment(response, "Creating element for channel prototypes");
        
        response.append("if (!{exists /library})\n");
        response.append("    create neutral /library\n");
        response.append("end\n\n");

        if (!mooseCompatMode())
        {

            response.append("disable /library\n");

            response.append("pushe /library\n");
            response.append("make_cylind_compartment\n");

            response.append("make_cylind_symcompartment\n");
            response.append("pope\n\n");
        }
        

        return response.toString();
    }

    private String generateGlobals()
    {
        StringBuilder response = new StringBuilder();
        addComment(response, "This temperature is needed if any of the channels are temp dependent (Q10 dependence) \n");
        response.append("float celsius = " + project.simulationParameters.getTemperature() + "\n\n");

        response.append("str units = \"" + UnitConverter.getUnitSystemDescription(project.genesisSettings.getUnitSystemToUse()) + "\"\n\n");


       

        String core = mooseCompatMode()?GEN_CORE_MOOSE:GEN_CORE_GENESIS;

        response.append("str "+GEN_CORE_VARIABLE+" = \"" + core + "\"\n\n");


        return response.toString();
    }

    private String printEnv()
    {
        StringBuilder response = new StringBuilder();

        if(addComments && !mooseCompatMode())
        {
            response.append("env // prints details on some global variables\n\n\n");
        }
        return response.toString();
    }




    private String generateChanMechIncludes() throws GenesisException
    {
        StringBuilder response = new StringBuilder();
        addComment(response, "Including channel mechanisms \n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> includedChanMechNames = new ArrayList<String>();

        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            if (!project.genesisSettings.isCopySimFiles())
            {
                dir = this.mainGenesisFile.getParentFile().getAbsolutePath()+ System.getProperty("file.separator");
            }
            else
            {
                dir = getDirectoryForSimulationFiles()+ System.getProperty("file.separator");
            }

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
                Cell cell = this.mappedCells.get(cellTypeName);

                ArrayList<ChannelMechanism> chanMechsFixed = cell.getAllUniformChanMechs(false);
                ArrayList<String> chanMechsAll = cell.getAllChanMechNames(false);

                //boolean foundFirstPassi

                for (int j = 0; j < chanMechsAll.size(); j++)
                {
                    //ChannelMechanism nextChanMech = chanMechNames.get(j);
                    String nextChanMechName = chanMechsAll.get(j);
                    
                    logger.logComment("Cell in group " + cellGroupName + " needs channel mech: " + nextChanMechName);
                    
                    //String chanMechNameToUse = nextChanMech.getName();
                    
                    if (!includedChanMechNames.contains(nextChanMechName) &&
                        project.generatedCellPositions.getNumberInCellGroup(cellGroupName) > 0)
                    {
                        CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMechName);

                        if (cellMech == null)
                        {
                            throw new GenesisException("Problem including cell mech: " + nextChanMechName);

                        }

                        if ( (cellMech.getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM) ||
                              cellMech.getMechanismType().equals(CellMechanism.ION_CONCENTRATION)) &&
                            ! (cellMech instanceof PassiveMembraneMechanism))
                        {
                            File newMechFile = new File(getGeneratedCodeDir(),
                                                           cellMech.getInstanceName() + ".g");

                            boolean success = false;

                            if (cellMech instanceof AbstractedCellMechanism)
                            {
                                success = ( (AbstractedCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                                    project.genesisSettings.getUnitSystemToUse(),
                                    newMechFile,
                                    project,
                                    false,
                                    addComments,
                                    false,
                                    false);
                            }
                            else if (cellMech instanceof ChannelMLCellMechanism)
                            {
                                success = ( (ChannelMLCellMechanism) cellMech).createImplementationFile(SimEnvHelper.
                                    GENESIS,
                                    project.genesisSettings.getUnitSystemToUse(),
                                    newMechFile,
                                    project,
                                    false,
                                    addComments,
                                    false,
                                    false);
                            }

                            if (!success)
                            {
                                throw new GenesisException("Problem generating file for cell mech: "
                                                           + nextChanMechName
                                                           +
                                                           "\nPlease ensure there is an implementation for that mechanism in GENESIS");

                            }

                            response.append("include " + getFriendlyDirName(dir) + cellMech.getInstanceName() + "\n");
                            response.append("make_" + cellMech.getInstanceName() + "\n\n");

                            includedChanMechNames.add(nextChanMechName);
                        }
                    }
                    /*
                    if (nextChanMech.getExtraParameters().size()>0)
                    {
                        String uniq = nextChanMech.getUniqueName();
                        
                        addComment(response, "Adding unique channel: "+ uniq + " for: "+ nextChanMech.toString());
                        
                        String oldChan = "/library/"+nextChanMech.getName();
                        String newChan = "/library/"+ uniq;
                        
                        response.append("copy "+oldChan +" "+ newChan + "\n");
                        
                        String initCall = "";
                        boolean onlyPassive = false;
                        
                        for(MechParameter mp: nextChanMech.getExtraParameters())
                        {
                            if (!mp.getName().equals(ChannelMLConstants.ION_CONC_FIXED_POOL_PHI_ELEMENT))
                            {
                                GenesisFileManager.addQuickComment(response, "Mechanism "+nextChanMech.getName()+" has parameter "+mp.getName()+" = "+mp.getValue());
                                
                                String paramName = mp.getName();
                                float paramVal = mp.getValue();
                                
                                if (mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT) || 
                                    mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                                {

                                    paramVal = (float)UnitConverter.getVoltage(mp.getValue(),
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        project.genesisSettings.getUnitSystemToUse()); 
                                    paramName = "Ek";
                                    
                                    if (nextChanMech.getExtraParameters().size()==1)
                                        onlyPassive = true;
                                }

                                response.append("setfield "+newChan+" "+paramName+" "+paramVal+"\n");
                                initCall = "init_"+nextChanMech.getName()+" "+newChan+"\n\n";
                            }
                            else
                            {
                                GenesisFileManager.addQuickComment(response, "Ignoring parameter "+mp.getName()+" on mechanism "+nextChanMech.getName()+" which has val  = "+mp.getValue()+"");
                            }
                        }
                        
                        if (!onlyPassive && initCall.length()>0)
                        {
                            GenesisFileManager.addQuickComment(response, "Reinitialising tables in channel with new params");
                            response.append(initCall);
                        }
                        else
                        {
                            response.append("\n");
                        }
                        
                        
                    }*/
                }
                
                ArrayList<String> copyCommands = new ArrayList<String>();
                
                for(int j = 0; j < chanMechsFixed.size(); j++)
                {
                    ChannelMechanism nextChanMech = chanMechsFixed.get(j);
                    
                    if (nextChanMech.getExtraParameters().size()>0)
                    {
                        String uniq = nextChanMech.getUniqueName();
                        
                        addComment(response, "Adding unique channel: "+ uniq + " for: "+ nextChanMech.toString());
                        
                        String oldChan = "/library/"+nextChanMech.getName();
                        String newChan = "/library/"+ uniq;
                        
                        String copy = "copy "+oldChan +" "+ newChan + "\n";
                        
                        if (!copyCommands.contains(copy))
                        {
                            response.append(copy);
                            copyCommands.add(copy);
                        }
                        else
                            addComment(response, "Previously copied...");

                        
                        String initCall = "";
                        boolean onlyPassive = false;
                        
                        for(MechParameter mp: nextChanMech.getExtraParameters())
                        {
                            if (!mp.getName().equals(ChannelMLConstants.ION_CONC_FIXED_POOL_PHI_ELEMENT))
                            {
                                GenesisFileManager.addQuickComment(response, "Mechanism "+nextChanMech.getName()+" has parameter "+mp.getName()+" = "+mp.getValue());
                                
                                String paramName = mp.getName();
                                float paramVal = mp.getValue();
                                
                                if (mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT) || 
                                    mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                                {

                                    paramVal = (float)UnitConverter.getVoltage(mp.getValue(),
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        project.genesisSettings.getUnitSystemToUse()); 
                                    paramName = "Ek";
                                    
                                    if (nextChanMech.getExtraParameters().size()==1)
                                        onlyPassive = true;
                                }
                                else if (mp.getName().equals("tau"))
                                {
                                    paramVal = (float)UnitConverter.getTime(mp.getValue(),
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        project.genesisSettings.getUnitSystemToUse());
                                }
                                else if (mp.getName().equals("beta"))
                                {
                                    paramVal = (float)UnitConverter.getRate(mp.getValue(),
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        project.genesisSettings.getUnitSystemToUse());
                                }

                                response.append("setfield "+newChan+" "+paramName+" "+paramVal+" \n");
                                initCall = "init_"+nextChanMech.getName()+" "+newChan+"\n\n";
                            }
                            else
                            {
                                GenesisFileManager.addQuickComment(response, "Ignoring parameter "+mp.getName()+" on mechanism "+nextChanMech.getName()+" which has val  = "+mp.getValue()+"");
                            }
                        }
                        
                        if (!onlyPassive && initCall.length()>0)
                        {
                            GenesisFileManager.addQuickComment(response, "Reinitialising tables in channel with new params");
                            response.append(initCall);
                        }
                        else
                        {
                            response.append("\n");
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
    
    
    private boolean warnedMooseRandStim = false; // temp warning flag


    private String generateSynMechIncludes() throws GenesisException
    {
        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            if (!project.genesisSettings.isCopySimFiles())
            {
                dir = this.mainGenesisFile.getParentFile().getAbsolutePath()+ System.getProperty("file.separator");
            }
            else
            {
                dir = getDirectoryForSimulationFiles()+ System.getProperty("file.separator");
            }
            
            dir = GeneralUtils.convertToCygwinPath(dir);
        }

        StringBuilder response = new StringBuilder();
        addComment(response, "Including synaptic mech \n");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> includedSynapses = new ArrayList<String>();

        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            //String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);
            //Cell cell = project.cellManager.getCell(cellTypeName);

            Vector<String> synNames = new Vector<String>();

            Iterator allNetConns = project.generatedNetworkConnections.getNamesNetConnsIter();

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
                    else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
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
                String nextSynMechName = synNames.elementAt(j);
                logger.logComment("Cell in group "+cellGroupName+" has synapse: "+ nextSynMechName);

                if (!includedSynapses.contains(nextSynMechName))
                {
                    logger.logComment("Adding it");
                    CellMechanism cellMech
                        = project.cellMechanismInfo.getCellMechanism(nextSynMechName);

                    if (cellMech==null)
                    {
                        throw new GenesisException("Problem including cell mech: " + nextSynMechName  +
                                                   "\nPlease ensure there is an implementation for that cell mechanism in GENESIS");

                    }

                    File newMechFile = new File(getGeneratedCodeDir(),
                                                   cellMech.getInstanceName() + ".g");

                    boolean success = false;
                    if (cellMech instanceof AbstractedCellMechanism)
                    {

                          success  = ( (AbstractedCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                                                                                              project.genesisSettings.getUnitSystemToUse(),
                                                                                              newMechFile,
                                                                                              project,
                                                                                              false,
                                                                                              addComments,
                                                                                              false,
                                                                                              false);
                    }
                    else if(cellMech instanceof ChannelMLCellMechanism)
                    {
                        success
                            = ( (ChannelMLCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                                                                                              project.genesisSettings.getUnitSystemToUse(),
                                                                                              newMechFile,
                                                                                              project,
                                                                                              false,
                                                                                              addComments,
                                                                                              false,
                                                                                              false);


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
            
            if (nextStim instanceof RandomSpikeTrainSettings &&
                project.generatedElecInputs.getNumberSingleInputs(nextStim.getReference())>0)
            {
                RandomSpikeTrainSettings randStim = (RandomSpikeTrainSettings)nextStim;

                if (mooseCompatMode()&& !warnedMooseRandStim)
                {
                    if (MainApplication.isGUIBasedStartupMode())
                    {
                        GuiUtils.showWarningMessage(logger, "MOOSE support for random synaptic input still under development! Use with caution!", null);
                    }

                    warnedMooseRandStim = true;
                }

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

                    File newMechFile = new File(getGeneratedCodeDir(),
                                                   cellMech.getInstanceName() + ".g");

                    boolean success = false;
                    if (cellMech instanceof AbstractedCellMechanism)
                    {
                       success = ( (AbstractedCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                            project.genesisSettings.getUnitSystemToUse(),
                            newMechFile,
                            project,
                            false,
                            addComments,
                            false,
                            false);
                    }
                    else if (cellMech instanceof ChannelMLCellMechanism)
                    {
                       success = ( (ChannelMLCellMechanism) cellMech).createImplementationFile(SimEnvHelper.GENESIS,
                            project.genesisSettings.getUnitSystemToUse(),
                            newMechFile,
                            project,
                            false,
                            addComments,
                            false,
                            false);
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
        StringBuilder response = new StringBuilder();
        addMajorComment(response, "Settings for running the demo");
        response.append("\n");
        //response.append("tstop = "+project.simulationParameters.duration+"\n");

        response.append("float dt = "
                        + (float)convertNeuroConstructTime(project.simulationParameters.getDt())+"\n");
        response.append("float duration = "
                        + (float)convertNeuroConstructTime(simConfig.getSimDuration())+"\n");

        if (!mooseCompatMode())
            response.append("int steps =  {round {{duration}/{dt}}}\n\n");
        else
            response.append("int steps =  {{duration}/{dt}}\n\n");
        
        
        response.append("setclock 0 {dt}");
        if (project.genesisSettings.isGenerateComments())
            response.append(" // " + getTimeUnitString());
        response.append("\n");

        if (mooseCompatMode())
        {
            response.append("setclock 1 {dt}");

            if (project.genesisSettings.isGenerateComments())
                response.append(" // " + getTimeUnitString());

            response.append("\n");

            response.append("setclock 2 {dt}");

            if (project.genesisSettings.isGenerateComments())
                response.append(" // " + getTimeUnitString());

            response.append("\n");
        }

        return response.toString();
    }

    private String generateNumIntegMethod()
    {
        StringBuilder response = new StringBuilder();

        response.append(project.genesisSettings.numMethod.getScript());

        return response.toString();

    }

    private String generateRandomise()
    {
        StringBuilder response = new StringBuilder();

        addComment(response, "Initializes random-number generator");
        response.append("randseed "+this.randomSeed+"\n");
        return response.toString();

    }



    private String generateWelcomeComments()
    {
        StringBuilder response = new StringBuilder();
        if (!project.genesisSettings.isGenerateComments()) return "";

        response.append("echo \"\"\n");
        response.append("echo \"*****************************************************\"\n");
        response.append("echo \"\"\n");
        response.append("echo \"    neuroConstruct generated GENESIS simulation\"\n");
        response.append("echo \"    for project: "+ project.getProjectFile().getAbsolutePath() +"\"\n");
        response.append("echo \"\"\n");



        String desc = project.getProjectDescription();
        String pre = "echo \"    ";
        desc = GeneralUtils.replaceAllTokens(desc, "\n", "\"\n"+pre);
        
        response.append(pre+"Description: " + desc + "\"\n\n");
        response.append(pre+"Simulation configuration: " + simConfig.getName() + "\"\n");
        response.append(pre+"Simulation reference: " + project.simulationParameters.getReference() + "\"\n");

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
        return dirForDataFiles.getAbsoluteFile();
    }


/*
    private float getSimDuration()
    {
        if (simConfig.getSimDuration()==0) // shouldn't be...
        {
            return project.simulationParameters.getDuration();
        }
        else
            return simConfig.getSimDuration();
    }


    private String generateCellParamControl()
    {
        StringBuilder response = new StringBuilder();

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

        else if (simIndepVarName.indexOf(SimPlot.SYNAPSES) >= 0 &&
                 simIndepVarName.indexOf(SimPlot.SYN_COND) >= 0)
        {
            return (float) UnitConverter.getConductance(val,
                                                       UnitConverter.NEUROCONSTRUCT_UNITS,
                                                       units);
        }

        else if (simIndepVarName.indexOf(SimPlot.COND_DENS) >= 0)
        {
            return (float) UnitConverter.getConductanceDensity(val,
                                                               UnitConverter.NEUROCONSTRUCT_UNITS,
                                                               units);
        }


        else if (simIndepVarName.indexOf(SimPlot.CURR_DENS)>=0)
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

            logger.logComment("Creating VariableHelper for: " + record.getDescription(false, false)+", cell: "+cellNum);


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


                logger.logComment("extraElementPart: "+extraElementPart+", mechanismName: "+mechanismName
                        +", simIndepVarPart: "+simIndepVarPart+", element: "+ element
                        +", compParentElement; "+compParentElement+", compTopElementName: "+ compTopElementName);

                if (mechanismName.equals(SimPlot.SYNAPSES))
                {
                    String netConn = simIndepVarPart.substring(0, simIndepVarPart.indexOf(':'));
                    String synMech = simIndepVarPart.substring(netConn.length()+1, simIndepVarPart.lastIndexOf(':'));
                    compTopElementName = synMech+"_"+netConn;
                    if(simIndepVarPart.endsWith(SimPlot.SYN_COND))
                    {
                        variableName = "Gk";
                    }
                    else if(simIndepVarPart.endsWith(SimPlot.SYN_CURR))
                    {
                        variableName = "Ik";
                    }
                    else
                    {
                        variableName = simIndepVarPart.substring(simIndepVarPart.lastIndexOf(':')+1);
                    }
                }
                else if (simIndepVarPart.startsWith(SimPlot.COND_DENS))
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


                    String cellNameRef = getSingleWordElementName(record.simPlot.getCellGroup() +"_"+cellNum); // cleans up -1 etc

                    extraLines = "\nif (!{exists " + SCRIPT_OUT_ELEMENT_ROOT + "})\n" +
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
                                            "end\n" +
                                            "update_" + convName + "\n\n";

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
                        else if (simIndepVarPart.indexOf(SimPlot.CURR_DENS)>=0)
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
                            double unit = UnitConverter.getLength(1, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                  project.genesisSettings.getUnitSystemToUse());

                            surfArea = surfArea * unit * unit;

                            String cellNameRef = getSingleWordElementName(record.simPlot.getCellGroup() +"_"+cellNum); // cleans up -1 etc

                            extraLines = "\nif (!{exists " + SCRIPT_OUT_ELEMENT_ROOT + "})\n" +
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
                                "end\n";

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
                                        String oldGateXpath = ChannelMLConstants.getPreV1_7_3GateXPath(gateIndex);

                                        logger.logComment("Checking xpath: " + oldGateXpath);

                                        SimpleXMLEntity[] oldGate = ( (ChannelMLCellMechanism) cellMech).getXMLDoc().getXMLEntities(oldGateXpath);

                                        if (oldGate != null && oldGate.length > 0)
                                        {
                                            logger.logComment("Looking at: " + oldGate[0]);

                                            SimpleXMLEntity gateState = ( (SimpleXMLElement)
                                                                         oldGate[0]).getXMLEntities(ChannelMLConstants.
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
                                        
                                        
                                        String newGateXpath = ChannelMLConstants.getIndexedGateXPath(gateIndex);
                                        
                                        SimpleXMLEntity[] newGate = ( (ChannelMLCellMechanism) cellMech).getXMLDoc().getXMLEntities(newGateXpath);
                                        
                                        if (newGate != null && newGate.length > 0)
                                        {
                                            
                                            String name = ( (SimpleXMLElement)newGate[0]).getAttributeValue(ChannelMLConstants.GATE_NAME_ATTR);
                                            
                                            logger.logComment("Looking at: " + newGate[0]+", name = "+name);
                                            
                                            if (name.equals(simIndepVarPart))
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
                                catch (XMLMechanismException ex)
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
        StringBuilder response = new StringBuilder();

        ArrayList<PlotSaveDetails> plots = project.generatedPlotSaves.getPlottedPlotSaves();

        if (project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.ALL_SHOW) &&
            !simConfig.getMpiConf().isRemotelyExecuted())
        {
            addMajorComment(response, "Adding " + plots.size() + " plot(s)");

            for (PlotSaveDetails plot : plots)
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

                String cellType = project.cellGroupsInfo.getCellType(plot.simPlot.getCellGroup());

                for (Integer cellNum : cellNumsToPlot)
                {
                    for (Integer segId : segIdsToPlot)
                    {
                        Segment segInMappedCell = this.getMappedSegment(cellType, segId, 0.5f);


                        String plotted = plot.simPlot.getValuePlotted();

                        VariableHelper var = new VariableHelper(plot, cellNum, segInMappedCell);

                       // if (plot.simPlot.isSynapticMechanism())
                      //  {
                    //        addComment(response, "Plotting synaptic value(s): "+plotted+" in: "+ plot.simPlot.toString());
                    //    }
                    //    else
                   //     {

                            if (!plotted.equals(var.getVariableName())) plotted = plotted + " (" + var.getVariableName() +
                                ")";

                            String dataSetName = var.getCompParentElementName() + "_" + var.getCompTopElementName() +
                                ":" + var.getVariableName();

                            if (dataSetName.indexOf("Conv") >= 0) // i.e. maybe a converted amt, e.g. current
                            {
                                dataSetName = plot.simPlot.getValuePlotted();
                            }

                            if (dataSetName.length() > 28) // long names give errors...
                                dataSetName = "..." + dataSetName.substring(dataSetName.length() - 25);

                            response.append(createSinglePlot(plotFrameName,
                                                             dataSetName,
                                                             "Values of " + plotted + " in " +
                                                             getCellElementName(plot.simPlot.getCellGroup(), cellNum)+": "+project.simulationParameters.getReference(),
                                                             var.getCompParentElementName(),
                                                             var.getCompTopElementName(),
                                                             var.getVariableName(),
                                                             maxVal,
                                                             minVal,
                                                             getNextColour(plotFrameName),
                                                             var.getExtraLines()));
                  //  }

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
        StringBuilder response = new StringBuilder();

        if (project.genesisSettings.isShowShapePlot() && 
            project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.ALL_SHOW))
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
        StringBuilder response = new StringBuilder();

        if (project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.ALL_SHOW))
        {

            response.append("\n\n");

            addMajorComment(response, "Creating a simple Run Control");

            response.append("if (!{exists "+CONTROLS_ELEMENT_ROOT+"})\n"+
                            "    create neutral "+CONTROLS_ELEMENT_ROOT+"\n"+
                            "end\n");

            String runControl = CONTROLS_ELEMENT_ROOT + "/runControl";
            response.append("create xform "+runControl + " [700, 20, 200, 140] -title \"Run Controls: "+project.simulationParameters.getReference()+"\"\n");

            response.append("xshow " + runControl+"\n\n");

            response.append("create xbutton " + runControl+"/RESET -script reset\n");

            response.append("str rerun\n");
            response.append("rerun = { strcat \"step \" {steps} }\n");
            response.append("create xbutton " + runControl+"/RUN -script {rerun}\n"); // +1 to include 0 and last timestep
            response.append("create xbutton " + runControl+"/STOP -script stop\n\n");
            response.append("create xbutton " + runControl+"/QUIT -script quit\n\n");

        }


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

        StringBuilder response = new StringBuilder();


        if (graphsCreated.isEmpty())
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
                            + " -xmin 0 -xmax {duration}"
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
    public static void addComment(StringBuilder responseBuffer, String comment)
    {
        logger.logComment("Adding GENESIS comment: "+ comment);
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        String pre = "//   ";
        String safeComment = GeneralUtils.replaceAllTokens(comment, "\n", "\n"+pre);
        responseBuffer.append(pre + safeComment + "\n");
        responseBuffer.append("\n");
    }

    /**
     * Adds a line commented out by //, but with no empty line after the comment
     */
    public static void addQuickComment(StringBuilder responseBuffer, String comment)
    {
        logger.logComment("Adding GENESIS comment: "+ comment);
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//   " + comment + "\n");

    }



    public static void addMajorComment(StringBuilder responseBuffer, String comment)
    {
        if (!addComments) return;

        if (!responseBuffer.toString().endsWith("\n")) responseBuffer.append("\n");
        responseBuffer.append("//////////////////////////////////////////////////////////////////////\n");
        String pre = "//   ";
        String safeComment = GeneralUtils.replaceAllTokens(comment, "\n", "\n"+pre);
        responseBuffer.append(pre + safeComment + "\n");
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

        StringBuilder response = new StringBuilder();
        response.append("\n");

        //Vector cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        ArrayList<String> cellGroupNames = simConfig.getCellGroups();

        logger.logComment("Looking at " + cellGroupNames.size() + " cell groups");

        //System.out.println(simConfig.toLongString());

        if (cellGroupNames.isEmpty())
        {
            logger.logComment("There are no cell groups!!");

            addMajorComment(response, "There were no cell groups specified in the project, might be a pretty boring simulation...");
            return response.toString();
        }
        response.append("\ncreate neutral "+CELL_ELEMENT_ROOT+"\n\n");


        //ArrayList<String> tempChans = new ArrayList<String>();
        
        // List of cells checked for bug in hsolve preventing use of hsolve and 
        // symmetric compartments when a compartment has 3 or more child compartments
        ArrayList<String> cellsCheckedHsolveBug = new ArrayList<String>();


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

            if (project.genesisSettings.numMethod.isHsolve() && 
                project.genesisSettings.isSymmetricCompartments() &&
                !mooseCompatMode() &&
                !cellsCheckedHsolveBug.contains(mappedCell.getInstanceName()))
            {
                int numComps = mappedCell.getAllSegments().size();
                Hashtable<Integer, Integer> numChildren = new Hashtable<Integer, Integer>();
                for(Segment seg: mappedCell.getAllSegments())
                {
                    if (seg.getParentSegment()!=null)
                    {
                        if (numChildren.get(seg.getParentSegment().getSegmentId())==null)
                        {
                            numChildren.put(seg.getParentSegment().getSegmentId(), 0);
                        }
                        numChildren.put(seg.getParentSegment().getSegmentId(), numChildren.get(seg.getParentSegment().getSegmentId())+1);
                    }
                }
                logger.logComment("numChildren: "+ numChildren);


                boolean problem = false;
                for(Integer children: numChildren.values())
                {
                    if (children>=3) problem = true;
                }

                if (problem)
                {
                    GuiUtils.showWarningMessage(logger, "Warning. There is a known bug in GENESIS 2 which occurs when using hsolve with symmetric compartments. Cells which have compartments with\n" +
                            "3 or more children will not behave properly. This is the case for cell: "+mappedCell.getInstanceName()+" in this simulation.", null);
                }

                cellsCheckedHsolveBug.add(mappedCell.getInstanceName());
            }
            
            logger.logComment("Got the mapped cell");


            GenesisMorphologyGenerator cellMorphGen
                = new GenesisMorphologyGenerator(mappedCell,
                                                 project,
                                                 getGeneratedCodeDir());
            

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
                    logger.logComment("Generated file: "+ cellMorphGen.getFilename());

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


            if (!project.genesisSettings.isCopySimFiles())
            {
                filenameToBeGenerated = GeneralUtils.convertToCygwinPath(filenameToBeGenerated);
            }
            else
            {
                File copiedFile = new File(getDirectoryForSimulationFiles(), (new File(filenameToBeGenerated)).getName());
                filenameToBeGenerated = GeneralUtils.convertToCygwinPath(copiedFile.getAbsolutePath());
            }

            if (simConfig.getMpiConf().isRemotelyExecuted())
            {
                filenameToBeGenerated = cellMorphGen.getFile().getName();
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

                    if (mooseCompatMode())
                    {
                        boolean methodChanged = false;
                        
                        ArrayList<String> chansAndSyns = new ArrayList<String>();
                        
                        for (ChannelMechanism cm: mappedCell.getChanMechsVsGroups().keySet())
                        {
                            chansAndSyns.add(cm.getName());
                        }
                        chansAndSyns.addAll(mappedCell.getAllAllowedSynapseTypes());

                        for (String cs: chansAndSyns)
                        {
                            if (!methodChanged)
                            {

                                File channelScript = new File(mainGenesisFile.getParentFile(), cs+".g");
                                logger.logComment("Checking "+channelScript+" for tab2dchannel or Mg_block..");

                                String contents = GeneralUtils.readShortFile(channelScript);

                                if (contents.indexOf("tab2Dchannel")>=0 )
                                {
                                    response.append("//***********************************************************************************************\n");
                                    response.append("//********** Cell: "+newElementName+" will use tab2Dchannel, so using method ee on it ***********\n");
                                    response.append("setfield "+newElementName+" method ee\n");
                                    response.append("//***********************************************************************************************\n\n");
                                    methodChanged = true;
                                }
                                if (contents.indexOf("Mg_block")>=0 )
                                {
                                    response.append("//***********************************************************************************************\n");
                                    response.append("//********** Cell: "+newElementName+" will use Mg_block, so using method ee on it ***********\n");
                                    response.append("setfield "+newElementName+" method ee\n");
                                    response.append("//***********************************************************************************************\n\n");
                                    methodChanged = true;
                                }
                            }
                        }
                    }
                    response.append("addfield "
                                    + newElementName
                                    + " celltype\n");
                    response.append("setfield "
                                    + newElementName
                                    + " celltype " + mappedCell.getInstanceName() + "\n\n");

                    
                    if (CellTopologyHelper.hasExtraCellMechParams(mappedCell))
                    {
                        GenesisFileManager.addComment(response, "Some of the channel mechanisms in this cell have some of their internal params changed after initialisation");
                        
                        response.append("str tempChanName\n\n");
                        
                        ArrayList<String> passChanNames =  CellTopologyHelper.getPassiveChannels(mappedCell, project);
                        
                        Hashtable<ChannelMechanism, Vector<String>> chanMechVsGroups = mappedCell.getChanMechsVsGroups();
                        
                        Enumeration<ChannelMechanism> keys =  chanMechVsGroups.keys();
                        
                        Hashtable<String, ArrayList<Segment>> segsVsGroups = new Hashtable<String, ArrayList<Segment>>();  // caching of seg lists per group
                        
                        while(keys.hasMoreElements())
                        {
                            ChannelMechanism cm = keys.nextElement();
                            
                            for(String group: chanMechVsGroups.get(cm))
                            {
                                if (!segsVsGroups.containsKey(group))
                                {
                                    ArrayList<Segment> segsTemp = mappedCell.getSegmentsInGroup(group);
                                    segsVsGroups.put(group, segsTemp);
                                }
                                
                                ArrayList<Segment> segs = segsVsGroups.get(group); // retrieve from cache
                                
                                ArrayList<String> moveCommands = new ArrayList<String>();
                                
                                for(MechParameter mp: cm.getExtraParameters())
                                {
                                    if (!mp.getName().equals(ChannelMLConstants.ION_CONC_FIXED_POOL_PHI_ELEMENT))
                                    {
                                        GenesisFileManager.addQuickComment(response, "Mechanism "+cm.getName()+" has parameter "+mp.getName()+" = "+mp.getValue()+" on group: "+group);

                                        if (group.equals(Section.ALL))
                                        {
                                            String chanElement = newElementName+"/#/"+cm.getName();
                                            String paramName = mp.getName();
                                            float paramVal = mp.getValue();
                                            String initCmd = "    init_"+cm.getName()+" {tempChanName}\n";

                                            if ((paramName.equals(BiophysicsConstants.PARAMETER_REV_POT) || 
                                                paramName.equals(BiophysicsConstants.PARAMETER_REV_POT_2)) &&
                                                cm.getName().equals(passChanNames.get(0)))
                                            {
                                                paramVal = (float)UnitConverter.getVoltage(mp.getValue(),
                                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                    project.genesisSettings.getUnitSystemToUse()); 


                                                GenesisFileManager.addQuickComment(response, "That is the passive channel reversal potential");
                                                chanElement = newElementName+"/#";
                                                paramName = "Em";
                                                initCmd="";
                                           
                                                response.append("foreach tempChanName ({el "+chanElement+"})\n"+
                                                                "    //echo Resetting param "+paramName+" to "+paramVal+" on {tempChanName} \n"+
                                                                "    setfield {tempChanName} "+paramName+" "+paramVal+"\n"+
                                                                initCmd+
                                                                "end\n\n");
                                            }
                                            else
                                            {
                                                String uniq = cm.getUniqueName();
                                                
                                                String oldChanElement = newElementName+"/#/"+uniq;
                                                //String newChanElement = newElementName+"/#/"+uniq;
                                                
                                                String command = "foreach tempChanName ({el "+oldChanElement+"})\n"
                                                        +"    move {tempChanName} {getpath {tempChanName} -head}"+cm.getName()+"\n"
                                                        +"end\n\n";
                                                
                                                if (!moveCommands.contains(command))
                                                {
                                                    if (addComments)
                                                        moveCommands.add("//   " +"Channel was set to "+uniq+" in *.p file, and correct params set there. Moving back to original name.\n");

                                                    //if (!tempChans.contains(uniq))
                                                    //    tempChans.add(uniq);
                                                    
                                                    moveCommands.add(command);
                                                    
                                                    //GenesisFileManager.addQuickComment(moveCommand, "Channel was set to "+uniq+" in *.p file, and correct params set there. Moving back to original name.");

                                                    //moveCommand.append("foreach tempChanName ({el "+oldChanElement+"})\n");
                                                    //moveCommand.append("    move {tempChanName} {getpath {tempChanName} -head}"+cm.getName()+"\n");
                                                    //moveCommand.append("end\n\n");
                                                }
                                                
                                            }
                                                    

                                        }
                                        else
                                        {
                                            for(Segment seg: segs)
                                            {
                                                String chanElement = newElementName+"/"+SimEnvHelper.getSimulatorFriendlyName(seg.getSegmentName())+"/"+cm.getName();
                                                String paramName = mp.getName();
                                                float paramVal = mp.getValue();
                                                String initCmd = "init_"+cm.getName()+" \""+chanElement+"\"\n";

                                                if ((paramName.equals(BiophysicsConstants.PARAMETER_REV_POT) || 
                                                    paramName.equals(BiophysicsConstants.PARAMETER_REV_POT_2))&&
                                                    cm.getName().equals(passChanNames.get(0)))
                                                {
                                                    paramVal = (float)UnitConverter.getVoltage(mp.getValue(),
                                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                        project.genesisSettings.getUnitSystemToUse());

                                                    
                                                    GenesisFileManager.addQuickComment(response, "That is the passive channel reversal potential");
                                                    chanElement = newElementName+"/"+SimEnvHelper.getSimulatorFriendlyName(seg.getSegmentName());
                                                    paramName = "Em";
                                                    initCmd="";
                                                   
                                                    response.append("setfield "+chanElement+" "+paramName+" "+paramVal+"\n"+
                                                            initCmd);
                                                }
                                                else
                                                {
                                                    String uniq = cm.getUniqueName();
                                                    //String oldChanElement = newElementName+"/"+SimEnvHelper.getSimulatorFriendlyName(seg.getSegmentName())+"/"+uniq;
                                                    String oldChanElement = newElementName+"/#/"+uniq;

                                                    /////////////String command = "move "+oldChanElement+" "+chanElement+"\n\n";

                                                    String command = "foreach tempChanName ({el "+oldChanElement+"})\n"
                                                        +"    move {tempChanName} {getpath {tempChanName} -head}"+cm.getName()+"\n"
                                                        +"end\n\n";
                                                    
                                                    //if (moveCommand.length()==0)
                                                    //{
                                                    if (!moveCommands.contains(command))
                                                    {
                                                        if (addComments)
                                                            moveCommands.add("//   " +"Channel was set to "+uniq+" in *.p file, and correct params set there. Moving back to original name.\n");

                                                        //if (!tempChans.contains(uniq))
                                                        //    tempChans.add(uniq);

                                                        moveCommands.add(command);
                                                    }
                                                        //GenesisFileManager.addQuickComment(moveCommand, "Channel was set to "+uniq+" in *.p file, and correct params set there. Moving back to original name.");

                                                        //moveCommand.append("move "+oldChanElement+" "+chanElement+"\n\n");
                                                    //}
                                                }

                                            }
                                        }
                                    }
                                    else
                                    {
                                        GenesisFileManager.addQuickComment(response, "Ignoring mechanism "+cm.getName()+" which has parameter "+mp.getName()+" = "+mp.getValue()+". This will be set in the *.p file... ");
                                    }
                                }
                                for(String command: moveCommands)
                                {
                                    response.append(command);
                                }
                                
                            }
                        }
                        response.append("\n");
                    }
                                    
                }
                else
                {
                    response.append("\n\ncopy "
                                    + firstElementName
                                    + " "
                                    + newElementName
                                    + "\n");

                }
                
                
                logger.logComment("Done cell number: " + cellNumber + "..");

                if (!mooseCompatMode())
                {
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
                }

               if (posRecord.hasUniqueInitV())
               {
                   response.append("setfield " + newElementName + "/# initVm " +
                                   UnitConverter.getVoltage(posRecord.getInitV(),
                                                            UnitConverter.NEUROCONSTRUCT_UNITS,
                                                            project.genesisSettings.getUnitSystemToUse()) + "\n");

               }



                //Point3f point = new Point3f(posRecord.x_pos, posRecord.y_pos, posRecord.z_pos);

                //PositionedCell cellPosn = new PositionedCell(point, cellGroupName, cellTypeName);

            }

            logger.logComment("\n              ++++++++    Calculating ion exchange, conc dep, etc for: "+mappedCell.getInstanceName()+"...");

            ArrayList<String> chanMechNames = mappedCell.getAllChanMechNames(true);
            logger.logComment("Chan mechs: "+ chanMechNames);

            Hashtable<String, ArrayList<String>> ionCurrentSources = new Hashtable<String,ArrayList<String>>();
            Hashtable<String, ArrayList<String>> ionRateDependence = new Hashtable<String,ArrayList<String>>();
            Hashtable<String, ArrayList<String>> ionConcentration = new Hashtable<String,ArrayList<String>>();
            Hashtable<String, ArrayList<String>> ionCurrFixedRevPot = new Hashtable<String,ArrayList<String>>();

            for (int j = 0; j < chanMechNames.size(); j++)
            {
                logger.logComment(j+"   -    Looking at Chan mech...: "+chanMechNames.get(j));

                String nextChanMech = chanMechNames.get(j);
                CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMech);


                if (cellMech instanceof ChannelMLCellMechanism)
                {
                    ChannelMLCellMechanism cmlp = (ChannelMLCellMechanism)cellMech;
                    try
                    {
                        // Post v1.7.3 form of cml
                        String xpath = ChannelMLConstants.getCurrVoltRelXPath();
                        SimpleXMLEntity[] currVoltRels = cmlp.getXMLDoc().getXMLEntities(xpath);

                        for(SimpleXMLEntity sxe: currVoltRels)
                        {
                            String ionName = ((SimpleXMLElement)sxe).getAttributeValue(ChannelMLConstants.NEW_ION_NAME_ATTR);
                            if (ionName!=null)
                            {
                                logger.logComment("Found transmitted ion: "+ionName);

                                ArrayList<String> cellProcsTransmittingIon = ionCurrentSources.get(ionName);
                                if (cellProcsTransmittingIon == null)
                                {
                                    cellProcsTransmittingIon = new ArrayList<String> ();
                                    ionCurrentSources.put(ionName, cellProcsTransmittingIon);
                                }
                                if (!cellProcsTransmittingIon.contains(cellMech.getInstanceName()))
                                    cellProcsTransmittingIon.add(cellMech.getInstanceName());
                            
                                String fixedRevPot = ((SimpleXMLElement)sxe).getAttributeValue(ChannelMLConstants.FIXED_ION_REV_POT_ATTR);
                                if (fixedRevPot!=null)
                                {
                                    ArrayList<String> cellMechsFixedRevPot = ionCurrFixedRevPot.get(ionName);
                                    
                                    if (cellMechsFixedRevPot==null)
                                    {
                                        cellMechsFixedRevPot = new ArrayList<String> ();
                                        ionCurrFixedRevPot.put(ionName, cellMechsFixedRevPot);
                                    }
                                    cellMechsFixedRevPot.add(cellMech.getInstanceName());
                                }
                            }                            
                        }
                        
                        
                        
                        xpath = ChannelMLConstants.getCurrVoltRelXPath()+"/"+ChannelMLConstants.CONC_FACTOR_ELEMENT;
                        SimpleXMLEntity[] concFactors = cmlp.getXMLDoc().getXMLEntities(xpath);
                        
                        for(SimpleXMLEntity sxe: concFactors)
                        {
                            String ionName = ((SimpleXMLElement)sxe).getAttributeValue(ChannelMLConstants.CONC_DEP_ION_ATTR);
                            
                            ArrayList<String> cellProcsDepOnIonConc = ionRateDependence.get(ionName);

                            if (cellProcsDepOnIonConc==null)
                            {
                                cellProcsDepOnIonConc = new ArrayList<String> ();
                                ionRateDependence.put(ionName, cellProcsDepOnIonConc);
                            }
                            cellProcsDepOnIonConc.add(cellMech.getInstanceName());
                        }
                        
                        xpath = ChannelMLConstants.getCurrVoltRelXPath()+"/"+ChannelMLConstants.CONC_DEP_ELEMENT;
                        SimpleXMLEntity[] concDeps = cmlp.getXMLDoc().getXMLEntities(xpath);
                        
                        for(SimpleXMLEntity sxe: concDeps)
                        {
                            String ionName = ((SimpleXMLElement)sxe).getAttributeValue(ChannelMLConstants.CONC_DEP_ION_ATTR);
                            
                            ArrayList<String> cellProcsDepOnIonConc = ionRateDependence.get(ionName);

                            if (cellProcsDepOnIonConc==null)
                            {
                                cellProcsDepOnIonConc = new ArrayList<String> ();
                                ionRateDependence.put(ionName, cellProcsDepOnIonConc);
                            }
                            cellProcsDepOnIonConc.add(cellMech.getInstanceName());
                        }

                        // Pre v1.7.3 form of cml
                        xpath = ChannelMLConstants.getPreV1_7_3IonsXPath();
                        logger.logComment("Checking xpath: " + xpath);

                    
                        SimpleXMLEntity[] ions = cmlp.getXMLDoc().getXMLEntities(xpath);

                        if (ions != null)
                        {
                            for (int k = 0; k < ions.length; k++)
                            {
                                SimpleXMLElement ionElement = (SimpleXMLElement)ions[k];

                                logger.logComment("Got entity: " + ionElement.getXMLString("", false));
                                String name = ionElement.getAttributeValue(ChannelMLConstants.LEGACY_ION_NAME_ATTR);
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
                                    cellProcsDepOnIonConc.add(cellMech.getInstanceName());

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
                                    if (!cellProcsInfluencingConc.contains(cellMech.getInstanceName()))
                                        cellProcsInfluencingConc.add(cellMech.getInstanceName());

                                }
                                else if (role!=null && (role.equals(ChannelMLConstants.ION_ROLE_PERMEATED_FIXED_REV_POT)))
                                {
                                    ArrayList<String> cellMechsFixedRevPot = ionCurrFixedRevPot.get(name);
                                    
                                    if (cellMechsFixedRevPot==null)
                                    {
                                        cellMechsFixedRevPot = new ArrayList<String> ();
                                        ionCurrFixedRevPot.put(name, cellMechsFixedRevPot);
                                    }
                                    cellMechsFixedRevPot.add(cellMech.getInstanceName());

                                }
                                else
                                {
                                    logger.logComment("Wait to see if others are explicitly mentioned in a channel...");
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
                            if (!cellProcsTransmittingIon.contains(cellMech.getInstanceName()))
                                cellProcsTransmittingIon.add(cellMech.getInstanceName());
                        }
                        else
                        {
                            logger.logComment("No ohmic relation, so assuming no transmitted ion...");
                        }
                    }
                    catch(XMLMechanismException ex)
                    {
                        logger.logError( "Problem extracting ion info from Cell Mechanism: "+ cmlp+", file: "+cmlp.getXMLFile(), ex);

                    }
                }
                else
                {
                    logger.logComment("Note: "+ nextChanMech +", cell proc: "+ cellMech +" will not be used when linking ion sources and sinks...");
                }
            }
            
            boolean localVerbose = false;
            logger.logComment("ionConcentration: "+ ionConcentration, localVerbose);
            logger.logComment("ionRateDependence: "+ ionRateDependence, localVerbose);
            logger.logComment("ionCurrentSources: "+ ionCurrentSources, localVerbose);
            logger.logComment("ionCurrFixedRevPot: "+ ionCurrFixedRevPot, localVerbose);

            Enumeration<String> ionsAffectingRates = ionRateDependence.keys();

            boolean nameDefined = false;

            while (ionsAffectingRates.hasMoreElements())
            {
                String ion = ionsAffectingRates.nextElement();
                ArrayList<String> cellProcsAffected = ionRateDependence.get(ion);

                if (!nameDefined)
                {
                    response.append("str tempCompName\n\n");
                    response.append("str tempCellName\n\n");
                    response.append("str tempChanName\n\n");
                    nameDefined = true;
                }

                addComment(response,"The concentration of: " + ion + " has an effect on rate of " 
                        + cellProcsAffected);

                response.append("foreach tempCompName ({el "+getCellGroupElementName(cellGroupName)+"/#/#})\n");

                ArrayList<String> cellProcsConcs = ionConcentration.get(ion);

                String concVariable = "C"; // futureproofing. I beleve this is used in concenpool as Ca is used in Ca_concen
                if (ion.equals("ca")) concVariable = "Ca";

                if (cellProcsConcs!=null)
                {
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

                logger.logComment("Ion "+ion+" has conc cell mechs: "+cellProcsConcs);

                if (cellProcsConcs!=null)
                {
                    if (!nameDefined)
                    {
                        response.append("str tempCompName\n\n");
                        nameDefined = true;
                    }
                    addComment(response, "Ion "+ion+" is transmitted by "+ cellProcsTransmittingIon
                                    +" affecting conc cell mechs: "+cellProcsConcs);


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

            //System.out.println(".... "+mappedCell.getIonPropertiesVsGroups());

            Enumeration<IonProperties> ips = mappedCell.getIonPropertiesVsGroups().keys();

            while (ips.hasMoreElements())
            {
                IonProperties ip = ips.nextElement();
                Vector<String> groups = mappedCell.getIonPropertiesVsGroups().get(ip);
                logger.logComment("Checking "+ip+", present on "+groups+" and cell: "+mappedCell, localVerbose);

                if (ip.revPotSetByConcs())
                {

                    ArrayList<String> mechsPassingIon = ionCurrentSources.get(ip.getName());

                    if (mechsPassingIon!=null)
                    {
                        for (String mech: mechsPassingIon)
                        {
                            if (ionCurrFixedRevPot.get(ip.getName())!=null && ionCurrFixedRevPot.get(ip.getName()).contains(mech))
                            {
                                addComment(response, "    "+ip+" is present on "+groups+" and flows through "+mech+" but that channel has a fixed rev pot (see the ChannelML file!)\n");

                            }
                            else
                            {
                                addComment(response, "    "+ip+" is present on "+groups+" and reversal potential of this through "+mech+" is calculated from internal/external membrane potential\n");

                                response.append("foreach tempCellName ({el "+getCellGroupElementName(cellGroupName)+"/#})\n");

                                for(String group: groups)
                                {
                                    ArrayList<String> compsToDo = new ArrayList<String>();

                                    if (group.equals(Section.ALL))
                                    {
                                        compsToDo.add("#");
                                    }
                                    else
                                    {
                                        for (Segment seg : mappedCell.getSegmentsInGroup(group))
                                        {
                                            compsToDo.add(seg.getSegmentName());
                                        }

                                    }

                                    for (String comp: compsToDo)
                                    {

                                        response.append("    foreach tempChanName ({el  {tempCellName}/"+comp+"/"+mech+"})\n");

                                        response.append("        echo \"Adding nernst object to: \" {tempChanName}\n");

                                        response.append("        if (!{exists {tempChanName}/Ca_nernst})\n");

                                        response.append("            create nernst {tempChanName}/Ca_nernst\n");

                                        response.append("            float CCaO = "+UnitConverter.getConcentration(ip.getExternalConcentration(),
                                                                                         UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                                         project.genesisSettings.getUnitSystemToUse())+"\n");

                                        response.append("            float CCaI = "+UnitConverter.getConcentration(ip.getInternalConcentration(),
                                                                                         UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                                         project.genesisSettings.getUnitSystemToUse())+"\n");


                                        response.append("            float scale = "+( project.genesisSettings.isSIUnits() ? 1 : 1e3f )+"\n");

                                        int valence = 2; //TODO: get valence from ChannelML!!!


                                        response.append("            setfield {tempChanName}/Ca_nernst Cin {CCaI} Cout {CCaO} valency {"+valence+"} scale {scale} T {celsius}\n");


                                        response.append("            addmsg {tempChanName}/../"+ionConcentration.get(ip.getName()).get(0)+"  {tempChanName}/Ca_nernst CIN Ca\n");

                                        response.append("            addmsg {tempChanName}/Ca_nernst  {tempChanName}/../"+mech+" EK E\n");

                                        response.append("        end\n");

                                        response.append("    end\n\n");
                                    }


                                }

                                response.append("end\n\n");
                            }

                        }
                    }

                }
                else
                {
                    addComment(response, "    "+ip+" is present on "+groups+"");
                    
                    if (!nameDefined)
                    {
                        response.append("str tempCompName\n\n");
                        response.append("str tempCellName\n\n");
                        response.append("str tempChanName\n\n");
                        nameDefined = true;
                    }

                    response.append("foreach tempCellName ({el "+getCellGroupElementName(cellGroupName)+"/#})\n");


                    ArrayList<String> mechsPassingIon = ionCurrentSources.get(ip.getName());

                    for (String mech: mechsPassingIon)
                    {
                        addComment(response, "    "+ip.getName()+" is present on "+groups+" and reversal potential of this through "+mech+" is: "+ip.getReversalPotential()+" mV\n");


                        for(String group: groups)
                        {
                            ArrayList<String> compsToDo = new ArrayList<String>();

                            if (group.equals(Section.ALL))
                            {
                                compsToDo.add("#");
                            }
                            else
                            {
                                for (Segment seg : mappedCell.getSegmentsInGroup(group))
                                {
                                    compsToDo.add(seg.getSegmentName());
                                }

                            }

                            for (String comp: compsToDo)
                            {

                                response.append("    foreach tempChanName ({el  {tempCellName}/"+comp+"/"+mech+"})\n");


                                response.append("        setfield {tempChanName} Ek "+UnitConverter.getVoltage(ip.getReversalPotential(),
                                                                                     UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                                     project.genesisSettings.getUnitSystemToUse())+"\n");



                                response.append("    end\n\n");
                            }


                        }
                    }

                    response.append("end\n\n");
                }
            }




            response.append("\n");

            logger.logComment("***  Finished looking at cell group number " + ii + ", called: " + cellGroupName);
        }

        return response.toString();

    }


    private int suggestedRemoteRunTime = -1;

    public void setSuggestedRemoteRunTime(int t)
    {
        this.suggestedRemoteRunTime = t;
    }

    @SuppressWarnings("SleepWhileHoldingLock")
    public void runGenesisFile() throws GenesisException
    {
        logger.logComment("Trying to run the mainGenesisFile...");

        boolean copyToSimDataDir = project.genesisSettings.isCopySimFiles();

        ProcessFeedback pf = new ProcessFeedback()
        {

            public void comment(String comment)
            {
                logger.logComment("ProcessFeedback: ");
            }

            public void error(String comment)
            {
                logger.logComment("ProcessFeedback: ");
            }
        };

        nextColour = new Hashtable<String, Integer>(); // reset it...
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

        File dirForSimDataFiles = getDirectoryForSimulationFiles();
        File dirToRunFrom = null;
        File generatedCodeDir = getGeneratedCodeDir();
        
        if (copyToSimDataDir)
        {
            dirToRunFrom = dirForSimDataFiles;
            
            try
            {
                GeneralUtils.copyDirIntoDir(generatedCodeDir, dirForSimDataFiles, false, true);
            }
            catch (Exception e)
            {
                throw new GenesisException("Problem copying the GENESIS files from "+generatedCodeDir+" to "+ dirForSimDataFiles, e);
                
            }
        }
        else
        {
            dirToRunFrom = generatedCodeDir;
        }
        

        File positionsFile = new File(dirForSimDataFiles, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(dirForSimDataFiles, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(dirForSimDataFiles, SimulationData.ELEC_INPUT_DATA_FILE);
        

        
        if (dirToRunFrom.getAbsolutePath().indexOf(" ")>=0)
        {
            throw new GenesisException("GENESIS files cannot be run in a directory like: "+ dirToRunFrom
                    + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run GENESIS.\n" +
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
            String units = UnitConverter.getUnitSystemDescription(project.genesisSettings.getUnitSystemToUse());
            if (!mooseCompatMode())
            {
                SimulationsInfo.recordSimulationSummary(project, simConfig, dirForSimDataFiles, "GENESIS", morphComp, units);
            }
            else
            {
                SimulationsInfo.recordSimulationSummary(project, simConfig, dirForSimDataFiles, "MOOSE", morphComp, units);
            }
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "
                                      + dirForSimDataFiles+"\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
        }
        
        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;

        try
        {
            String genesisExecutable = null;

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                genesisExecutable = ProjectStructure.getGenesisWinScript().getAbsolutePath();
                
                File fullFileToRun = new File(dirToRunFrom, mainGenesisFile.getName());

                String args = GeneralUtils.convertToCygwinPath(fullFileToRun.getAbsolutePath());

                String title = "GENESIS_simulation" + "___" + project.simulationParameters.getReference();

                commandToExecute = "cmd /K start \""+title+"\"  " +  genesisExecutable + " "+args;

                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have executed command: " + commandToExecute);

            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                genesisExecutable = "/usr/local/genesis-2.3/genesis/genesis";  // default location for latest Genesis 2
                if (!(new File(genesisExecutable)).exists())
                {
                    genesisExecutable = "genesis"; // Hope it's on the path
                }

                String title = "GENESIS_simulation" + "___" + project.simulationParameters.getReference();

                
                if (mooseCompatMode())
                {
                    genesisExecutable = System.getProperty("user.home")+"/moose/moose";
                    if (!(new File(genesisExecutable)).exists())
                    {
                        genesisExecutable = "moose"; // Hope it's on the path
                    }
                    title = "MOOSE_simulation" + "___" + project.simulationParameters.getReference();

                }

                //File dirToRunIn = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());


                String basicCommLine = GeneralProperties.getExecutableCommandLine();

                String executable = "";
                String extraArgs = "";
                String titleOption = "";
                String workdirOption = "";

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


                StringBuilder scriptText = new StringBuilder();



                File scriptFile = new File(getGeneratedCodeDir(), "runsim.sh");


                if (!simConfig.getMpiConf().isRemotelyExecuted())
                {

                    scriptText.append("#!/bin/bash\n");

                    scriptText.append("cd " + dirToRunFrom.getAbsolutePath() + "\n" + genesisExecutable
                        + " "
                        + mainGenesisFile.getName());


                    if (project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.NO_CONSOLE))
                    {
                        if (!mooseCompatMode())
                        {
                            scriptText.append(" > /tmp/logGENESIS_"+project.getProjectFileName()
                                            +"_"+project.simulationParameters.getReference());
                        }
                        else
                        {
                            scriptText.append(" > /tmp/logMOOSE_"+project.getProjectFileName()
                                            +"_"+project.simulationParameters.getReference());
                        }
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
                    
                    KnownSimulators sim = mooseCompatMode() ? KnownSimulators.MOOSE : KnownSimulators.GENESIS;

                    scriptText.append(simConfig.getMpiConf().getPushScript(project.getProjectName(),
                                      project.simulationParameters.getReference(),
                                      sim,
                                      dirToRunFrom));

                    File simResultsDir = new File(ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()),
                            project.simulationParameters.getReference());

                    if (simConfig.getMpiConf().getQueueInfo()!=null)
                    {
                        String submitJob = simConfig.getMpiConf().getQueueSubmitScript(project.getProjectName(), project.simulationParameters.getReference(), time, sim);

                        File submitJobFile = new File(dirForSimDataFiles, QueueInfo.submitScript);

                        FileWriter fw = new FileWriter(submitJobFile);
                        //scriptFile.se
                        fw.write(submitJob);
                        fw.close();

                        // bit of a hack...
                        rt.exec(new String[]{"chmod","u+x",submitJobFile.getAbsolutePath()});

                        logger.logComment("-------   Written file: "+ submitJobFile.getAbsolutePath());
                    }



                    File pullScriptFile = new File(simResultsDir, RemoteLogin.remotePullScriptName);

                    String pullScriptText = simConfig.getMpiConf().getPullScript(project.getProjectName(),
                                                                                 project.simulationParameters.getReference(),
                                                                                 ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()));


                    FileWriter fw = new FileWriter(pullScriptFile);
                    //scriptFile.se
                    fw.write(pullScriptText);
                    fw.close();

                    logger.logComment("-------   Written file: "+ pullScriptFile.getAbsolutePath());

                    // bit of a hack...
                    rt.exec(new String[]{"chmod","u+x",pullScriptFile.getAbsolutePath()});

                }


                FileWriter fw = new FileWriter(scriptFile);

                fw.write(scriptText.toString());
                fw.close();

                // bit of a hack...
                rt.exec("chmod u+x " + scriptFile.getAbsolutePath());
                try
                {
                    // This is to make sure the file permission is updated..
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }

                if (project.genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.NO_CONSOLE))
                {
                    commandToExecute = scriptFile.getAbsolutePath();
                }
                else
                {
                    commandToExecute = executable
                        + " "
                        + titleOption
                        + " "
                        + workdirOption
                        + " "
                        + extraArgs
                        + " " +
                        scriptFile.getAbsolutePath();
                }



                logger.logComment("Going to execute command: " + commandToExecute);

                //rt.exec(commandToExecute);
                if (true || !simConfig.getMpiConf().isRemotelyExecuted())
                {
                    ProcessManager.runCommand(commandToExecute, pf, 4);
                }

                logger.logComment("Have successfully executed command: " + commandToExecute);
            }




        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new GenesisException("Error executing the GENESIS file: " + mainGenesisFile+
                "\nExecute command: "+commandToExecute+"\n\n"
                        + "This may be resolvable by updating the Command line executable used to run external programs\n"
                        + "at Settings -> General Properties and Project Defaults. If you're running on a Gnome desktop\n"
                        + "based Linux install (e.g. Ubuntu), you might want to change \"konsole\" to \"gnome-terminal -x\"\n\n"
                +"\n"+ex.getLocalizedMessage(), ex);
        }


        if (project.genesisSettings.getReloadSimAfterSecs()>0 &&
            MainApplication.isGUIBasedStartupMode())
        {
            logger.logComment("Going to reload data from: "+getDirectoryForSimulationFiles());
            SimulationData sd;
            try
            {
                File timesFile = new File (getDirectoryForSimulationFiles(), SimulationData.getStandardTimesFilename());
                int maxTries = (int)Math.ceil(project.genesisSettings.getReloadSimAfterSecs());
                int tries = 0;
                while(tries<maxTries)
                {
                    tries++;
                    if (timesFile.exists())
                    {
                        Thread.sleep(1000);
                        sd = new SimulationData(getDirectoryForSimulationFiles());
                        sd.initialise();

                        logger.logComment("Loading: " + sd.getAllLoadedDataStores());
                        tries = maxTries;
                        for(DataStore ds:sd.getAllLoadedDataStores())
                        {
                            DataSet dataSet = sd.getDataSet(ds.getCellSegRef(), ds.getVariable(), true);

                            String plotFrameRef = "Plot of "+ds.getVariable()+" from cell(s) in: "+ds.getCellGroupName()+", sim ref: "+project.simulationParameters.getReference();

                            PlotterFrame frame = PlotManager.getPlotterFrame(plotFrameRef);

                            frame.addDataSet(dataSet);

                            frame.setVisible(true);
                        }
                    }
                    else
                    {
                        Thread.sleep(1000);
                        logger.logComment("Sim not complete");
                    }
                }
            }
            catch (Exception ex)
            {
                logger.logError("Error reloading data from: "+getDirectoryForSimulationFiles(), ex);
            }
        }

    }

    public String getNextColour(String plotFrame)
    {
        if (!nextColour.containsKey(plotFrame))
        {
            nextColour.put(plotFrame, 1);
        }
        int colNum = nextColour.get(plotFrame);
        
        String colour = ColourUtils.getColourName(colNum).toLowerCase();
        int newColour = colNum +1;
        if (newColour >= 10) newColour = 1;

        nextColour.put(plotFrame, newColour);
        
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
        if (fileDirName.length()==0)
            return "";
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
        StringBuilder response = new StringBuilder();

        //String whatToSave = "Vm";

        response.append("\n");

        if (addComments) response.append("echo Checking and resetting...\n\n");

        if (!mooseCompatMode()) 
        {
            response.append("maxwarnings 400\n\n");
        }


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
        if (!mooseCompatMode())
        {
            response.append("reset\n");
        }


        File dirForDataFiles = getDirectoryForSimulationFiles();
        //String dataFileDirName = dirForDataFiles.getAbsolutePath() + System.getProperty("file.separator");

        //String friendlyDataDirName = getFriendlyDirName(dataFileDirName);


        File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

        response.append("str simsDir\n");
        response.append("simsDir = \"" + dirForSims.getAbsolutePath() + "/\"\n\n");

        response.append("str simReference\n");
        response.append("simReference = \"" + project.simulationParameters.getReference() + "\"\n\n");

        response.append("str targetDir\n");

        if (!simConfig.getMpiConf().isRemotelyExecuted())
        {
            response.append("targetDir =  {strcat {simsDir} {simReference}}\n");
            response.append("targetDir =  {strcat {targetDir} {\"/\"}}\n\n");
        }
        else
        {
            response.append("targetDir =  {\"./\"}\n\n");
        }
        
        
        String timeFileElement = FILE_ELEMENT_ROOT + "/timefile";
            
        boolean useTablesToSave = mooseCompatMode(); // to test pre asc_file impl in moose

        StringBuilder postRunLines = new StringBuilder();
            
        if (newRecordingToBeMade)
        {

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
            
            

            response.append("create neutral " + FILE_ELEMENT_ROOT + "\n");
            
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
                else if (mooseCompatMode() && record.simPlot.getValuePlotted().contains(SimPlot.COND_DENS))
                {
                    String error = "Note, conductance density saving not supported yet in MOOSE, so not saving: "+record.simPlot;
                    logger.logError(error);

                    addComment(response, error);
                }
                else
                {

                    addComment(response, record.getDescription(true, false));
                    String cellGroupName = record.simPlot.getCellGroup();
                    int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

                    String cellType = project.cellGroupsInfo.getCellType(cellGroupName);

                    boolean isSpikeRecording = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;

                    if (numInCellGroup > 0)
                    {
                        String cellGroupFileEl = getCellGroupFileElementName(cellGroupName);

                        response.append("if (!{exists " + cellGroupFileEl + "})\n"
                                        + "    create neutral " + cellGroupFileEl + "\n" +
                                        "end\n\n");

                        if (record.allCellsInGroup)
                        {
                            response.append("foreach cellName ({el " + getCellGroupElementName(cellGroupName) + "/#})\n");

                            if (useTablesToSave)
                            {

                                postRunLines.append("// "+ record.getDescription(true, false)+"\n");
                                postRunLines.append("foreach cellName ({el " + getCellGroupElementName(cellGroupName) + "/#})\n");
                                postRunLines.append("    ce {cellName}\n\n");
                            }

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

                                if (useTablesToSave)
                                {
                                    postRunLines.append("    compName = {strcat {cellName} /" +
                                                    SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName()) +
                                                    "}\n");
                                }

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
                                    String fileNameDef = "    str fileNameStr\n"
                                            +"    fileNameStr = {strcat {getpath {cellName} -tail} {\"" + segFileNamePart + varFileNamePart +
                                                    "." + extension + "\"} }\n";

                                    response.append(fileNameDef);

                                    if (useTablesToSave)
                                    {
                                        postRunLines.append(fileNameDef);
                                    }

                                    if (!useTablesToSave)
                                    {
                                        response.append("    create asc_file " + fileElement + "\n");
                                        response.append("    setfield " + fileElement +
                                                        "    flush 1    leave_open 1    append 1 notime 1\n");


                                        response.append("    setfield " + fileElement + " filename {strcat {targetDir} {fileNameStr}}\n");
                                    }
                                    else
                                    {
                                        response.append("    echo \"Going to record element at \" {compName}\n");
                                        response.append("    create table " + fileElement + "\n");
                                        response.append("    setfield " + fileElement + " step_mode 3\n");
                                        response.append("    call " + fileElement + " TABCREATE {{steps}+1} -1000 1000\n");

                                        //postRunLines.append("compName = "+compElement+"\n");
                                        postRunLines.append("tab2file {strcat {targetDir} {fileNameStr}} " + fileElement + " table -overwrite\n");

                                    }

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
                                if (!realElementToRecord.endsWith(var.getCompTopElementName()))
                                {
                                    realElementToRecord = realElementToRecord+"/"+var.getCompTopElementName();
                                }

                                String realVariableToSave = var.getVariableName();

                                response.append("    " + var.getExtraLines()+"\n");

                                if (project.genesisSettings.getNumMethod().isHsolve() &&
                                    project.genesisSettings.getNumMethod().getChanMode() >= 2)
                                {
                                    String hsolveElement = "{getpath {compName} -head}" + HSOLVE_ELEMENT_NAME;

                                    realVariableToSave = "{findsolvefield " + hsolveElement + " " + "{compName}"
                                        + " " + var.getVariableName() + "}";

                                    realElementToRecord = hsolveElement;
                                }


                                if (!isSpikeRecording)
                                {
                                    if (!useTablesToSave)
                                    {
                                        response.append("    addmsg " + realElementToRecord + " " + fileElement + " SAVE " +
                                                        realVariableToSave + "  //  .. \n");
                                        response.append("    call " + fileElement + " OUT_OPEN\n");
                                        response.append("    call " + fileElement + " OUT_WRITE {getfield "
                                                        + realElementToRecord + " " + realVariableToSave + "}\n\n");
                                    }
                                    else
                                    {

                                        response.append("    addmsg  " + realElementToRecord + " " + fileElement + " INPUT " + realVariableToSave + "\n");
                                    }

                                }
                                else
                                {
                                    response.append("    addmsg " + realElementToRecord + " " + fileElement + " INPUT " +
                                                    realVariableToSave + "\n");
                                    response.append("    call " + fileElement + " OPEN\n");
                                }

                            }
                            response.append("end\n\n");


                            if (useTablesToSave)
                            {
                                postRunLines.append("end\n\n");
                            }
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

                                        String fileNameDef = "str fileNameStr\n"
                                            +"fileNameStr = {strcat {getpath {cellName} -tail} {\"" + segFileNamePart + varFileNamePart +
                                                    "." + SimPlot.CONTINUOUS_DATA_EXT + "\"} }\n";


                                        response.append(fileNameDef);

                                        if (useTablesToSave)
                                        {
                                            postRunLines.append(fileNameDef);
                                        }

                                        if (!useTablesToSave)
                                        {
                                            response.append("create asc_file " + fileElement + "\n");

                                            response.append("setfield " + fileElement +
                                                            "    flush 1    leave_open 1    append 1 notime 1\n");

                                            response.append("setfield " + fileElement + " filename { strcat  {targetDir} {fileNameStr}}\n");
                                        }

                                        else
                                        {
                                            response.append("echo \"Going to record element at \" {compName}\n");
                                            response.append("create table " + fileElement + "\n");
                                            response.append("setfield " + fileElement + " step_mode 3\n");
                                            response.append("call " + fileElement + " TABCREATE {{steps}+1} -1000 1000\n");

                                            postRunLines.append("compName = {strcat {cellName} /" +
                                                    SimEnvHelper.getSimulatorFriendlyName(segInMappedCell.getSegmentName()) +
                                                    "}\n");
                                            postRunLines.append("tab2file {strcat {targetDir} {fileNameStr}} " + fileElement + " table -overwrite\n");

                                        }

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
                                        if (!useTablesToSave)
                                        {
                                            response.append("addmsg " + realElementToRecord + " " + fileElement + " SAVE " +
                                                            realVariableToSave + "\n");

                                            response.append("call " + fileElement + " OUT_OPEN\n");
                                            response.append("call " + fileElement + " OUT_WRITE {getfield "
                                                            + realElementToRecord + " " + realVariableToSave + "}\n\n");
                                        }
                                        else
                                        {

                                            response.append("    addmsg  " + realElementToRecord + " " + fileElement + " INPUT " + realVariableToSave + "\n");
                                        }


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

        if (!GeneralUtils.isWindowsBasedPlatform() )  // Functions need further testing on windows
        {
            response.append("str startTimeFile\n");
            response.append("str stopTimeFile\n");

            response.append("startTimeFile = {strcat {targetDir} {\"starttime\"}}\n");

            response.append("stopTimeFile = {strcat {targetDir} {\"stoptime\"}}\n");
            response.append("sh {strcat {\"date +%s.%N > \"} {startTimeFile}}\n\n");
        }
        
        String dateInfo = "";

        if (false&&!mooseCompatMode())
        {
            dateInfo = " at: {getdate}";
        }
        else
        {
            dateInfo = "\ndate +%F__%T__%N";
        }

        
        response.append("echo Starting sim: "+project.simulationParameters.getReference()+" on {"+
                GEN_CORE_VARIABLE+"} with dur: {duration}"+
                " dt: {dt} and steps: {steps} ("+project.genesisSettings.getNumMethod()+")"+dateInfo+"\n");

        response.append("step {steps}\n\n"); // +1 to include 0 and last timestep


        response.append("echo Finished simulation reference: "+project.simulationParameters.getReference()+dateInfo+"\n");

        if (addComments) response.append("echo Data stored in directory: {targetDir}\n\n");
        
        if (!mooseCompatMode())
        {
            addComment(response, "This will ensure the data files don't get written to again..");

            response.append("\nstr fileElement\n");
            response.append("foreach fileElement ({el "+FILE_ELEMENT_ROOT+CELL_ELEMENT_ROOT+"/##[][TYPE=asc_file]})\n");
            //response.append("echo Written from element {fileElement} to file {getfield {fileElement} filename}\n\n");
            //response.append("    deletemsg {fileElement} 0 -incoming\n");
            response.append("end\n");

            response.append("foreach fileElement ({el "+FILE_ELEMENT_ROOT+CELL_ELEMENT_ROOT+"/##[][TYPE=event_tofile]})\n");
            response.append("    echo Closing {fileElement}\n\n");
            response.append("    call {fileElement} CLOSE\n");
            response.append("end\n\n");
        }

        addComment(response, "Saving file containing time details");

        response.append("float i, timeAtStep\n");



        if (!useTablesToSave)
        {
            response.append("create asc_file " + timeFileElement + "\n");
            response.append("setfield " + timeFileElement + "    flush 1    leave_open 1    append 1  notime 1\n");

            response.append("setfield " + timeFileElement + " filename {strcat {targetDir} {\"" + SimulationData.getStandardTimesFilename() +  "\"}}\n");
            response.append("call " + timeFileElement + " OUT_OPEN\n");
        }
        else
        {
            response.append("create table " + timeFileElement + "\n");
            response.append("call " + timeFileElement + " TABCREATE {steps} 0 {duration}\n");
        }

        response.append("for (i = 0; i <= {steps}; i = i + 1"  + ")\n");

        response.append("    timeAtStep = {dt} * i\n");
        if (!useTablesToSave)
        {
            response.append("    call " + timeFileElement + " OUT_WRITE {timeAtStep} \n");
        }
        else
        {
            response.append("setfield " + timeFileElement + " table->table[{i}] {timeAtStep}\n");
        }

        response.append("end\n\n");

        
        if (useTablesToSave)
        {
            response.append("tab2file {strcat {targetDir} {\"" + SimulationData.getStandardTimesFilename()
                    + "\"}} "+timeFileElement+" table -nentries {{steps}+1} -overwrite\n\n");
        }
        else
        {
            response.append("call "+timeFileElement+" FLUSH\n\n");
        }

        response.append(postRunLines.toString()+"\n");
        
        if (!GeneralUtils.isWindowsBasedPlatform())  //  Functions need further testing on windows
        {
            response.append("sh {strcat {\"date +%s.%N > \"} {stopTimeFile}}\n\n"); // if you know a better way to get output of a system command from a sh call, let me know...

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
        }



        /// ********************
        if (multiRunManager!=null)
            response.append(multiRunManager.getMultiRunPostScript(SimEnvHelper.GENESIS));
        /// ********************



        return response.toString();

    }


    @SuppressWarnings("SleepWhileHoldingLock")
    public static void main(String[] args)
    {

        try
        {
            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File("../nC_projects/TestSyns/TestSyns.ncx"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            System.out.println("Going to generate project: "+ p.getProjectFullFileName());

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);

            GenesisFileManager gen = new GenesisFileManager(p);

            while (pm.isGenerating())
            {
                System.out.println("Generating...");
                Thread.sleep(600);
            }



            OriginalCompartmentalisation oc = new OriginalCompartmentalisation();



            gen.generateTheGenesisFiles(p.simConfigInfo.getDefaultSimConfig(), null, oc, 12345);
            //gen.runGenesisFile();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
