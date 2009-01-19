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

package ucl.physiol.neuroconstruct.neuroml;

import java.io.*;
import java.util.*;
import java.util.ArrayList;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.simulation.IClampSettings;
import ucl.physiol.neuroconstruct.simulation.RandomSpikeTrainSettings;
import ucl.physiol.neuroconstruct.simulation.StimulationSettings;
import ucl.physiol.neuroconstruct.utils.*;

import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

/**
 * NetworkML file Reader. Importer of NetworkML files to neuroConstruct format using SAX
 *
 * @author Padraig Gleeson
 *  
 */

public class NetworkMLReader extends XMLFilterImpl implements NetworkMLnCInfo
{
    private static ClassLogger logger = new ClassLogger("NetworkMLReader");


    //private String importationComment = "Importation comment: ";

    private Stack<String> elementStack = new Stack<String>();

    private int projUnitSystem = -1;
    private int inputUnitSystem = -1;

    private String currentPopulation = null;
    private int currentInstanceId = -1;
    private int currentNodeId = -1;


    private String currentProjection = null;

    private String currentSynType = null;

    private int currentSourceCellNumber = -1;
    private int currentSourceCellSegmentIndex = -1;
    private float currentSourceCellDisplacement = -1;

    private int currentTargetCellNumber = -1;
    private int currentTargetCellSegmentIndex = -1;
    private float currentTargetCellDisplacement = -1;
    
    private String currentPropertyName = null;
    
    private long foundRandomSeed = Long.MIN_VALUE;
    private String foundSimConfig = null;

    private ArrayList<ConnSpecificProps> projectConnProps = new ArrayList<ConnSpecificProps>();
    private ArrayList<ConnSpecificProps> globConnProps = new ArrayList<ConnSpecificProps>();
    private ArrayList<ConnSpecificProps> localConnProps = new ArrayList<ConnSpecificProps>();
    
    private float globAPDelay = 0;
    private float localAPDelay = 0;

    private String currentElecInput = null;
    private String currentInputType = null;
    private String currentCellGroup = null;
    private String currentInputName = null;
        
    //private String metadataPrefix = MetadataConstants.PREFIX + ":";

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    private GeneratedElecInputs elecInputs = null;    
    
    private Project project = null;

    public NetworkMLReader(Project project)
    {
        this.cellPos = project.generatedCellPositions;
        this.netConns = project.generatedNetworkConnections;
        this.elecInputs = project.generatedElecInputs;
        this.project = project;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String contents = new String(ch, start, length);


        if (contents.trim().length() > 0)
        {
            logger.logComment("Got a string: (" + contents + ") at: "+ elementStack);

            if (getCurrentElement().equals(NetworkMLConstants.CELLTYPE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.POPULATION_ELEMENT))
            {
                //currentCellType = contents;
                logger.logComment(">> currentCellType: "+contents+", currentPopulation: "+currentPopulation);
            }
            else if (getCurrentElement().equals(NetworkMLConstants.SOURCE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.PROJECTION_ELEMENT))
            {
                //currentSource = contents;
                logger.logComment("currentSource: "+contents);
            }
            else if (getCurrentElement().equals(NetworkMLConstants.TARGET_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.PROJECTION_ELEMENT))
            {
                //currentTarget = contents;
                logger.logComment("currentTarget: "+contents);
            }
            // Pre v1.7.1 style
            else if (getCurrentElement().equals(NetworkMLConstants.SYN_TYPE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.SYN_PROPS_ELEMENT))
            {
                this.currentSynType = contents;
                logger.logComment("currentSynType: " + currentSynType);
            }
            else if (getCurrentElement().equals(NetworkMLConstants.INPUT_ELEMENT)
                    && getAncestorElement(1).equals(NetworkMLConstants.INPUTS_ELEMENT))
            {
                this.currentElecInput = contents;
                logger.logComment("currentElecInput: " + currentElecInput);                
            } 
            else if (getCurrentElement().equals(MetadataConstants.PROP_TAG_ELEMENT)
                     && getAncestorElement(1).equals(MetadataConstants.PROP_ELEMENT))
            {
                this.currentPropertyName = contents;
            }
            else if (getCurrentElement().equals(MetadataConstants.PROP_VALUE_ELEMENT)
                     && getAncestorElement(1).equals(MetadataConstants.PROP_ELEMENT))
            {
                if (this.currentPropertyName.equals(NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED))
                {
                    this.foundRandomSeed = Long.parseLong(contents);
                }
                else if (this.currentPropertyName.equals(NetworkMLConstants.NC_SIM_CONFIG))
                {
                    this.foundSimConfig = contents;
                }
            }
        }
    }

    public String getSimConfig()
    {
        return this.foundSimConfig;
    }

    public long getRandomSeed()
    {
        return this.foundRandomSeed;
    }




    @Override
    public void startDocument()
    {
        logger.logComment("startDocument...");

        //cell = new Cell();
    }

    @Override
    public void endDocument()
    {

    }

    public String getCurrentElement()
    {
        return elementStack.peek();
    }

    public String getParentElement()
    {
        return getAncestorElement(1);
    }

    /**
     * Taking the child parent thing to it's logical extension...
     * parent element is 1 generation back, parent's parent is 2 back, etc.
     */
    public String getAncestorElement(int generationsBack)
    {
        if (elementStack.size()<generationsBack+1) return null;
        return elementStack.elementAt(elementStack.size()-(generationsBack+1));
    }





    public void setCurrentElement(String newElement)
    {
        this.elementStack.push(newElement);

         logger.logComment("Elements: "+ elementStack);

     }

     public void stepDownElement()
     {
         elementStack.pop();
     }


    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes)
    throws SAXException
    {
        logger.logComment("\n\n          -----   Start element: " + qName + "        (namespaceURI: " + namespaceURI
                           + ", localName: " + localName + ")");


        int attrsLength = attributes.getLength();
        for (int i = 0; i < attrsLength; i++)
        {
         String name = attributes.getLocalName(i);
         String val = attributes.getValue(i);

         logger.logComment("Attr:  " + name+ " = " + val+ "         (qname: "
                           + attributes.getQName(i)+ ", uri: " + attributes.getURI(i)+")");
        }

         setCurrentElement(localName);

         if (getCurrentElement().equals(NetworkMLConstants.POPULATION_ELEMENT)
             && getAncestorElement(1).equals(NetworkMLConstants.POPULATIONS_ELEMENT))
         {
             String name = attributes.getValue(NetworkMLConstants.POP_NAME_ATTR);

             logger.logComment(">>  Found a population of name: "+ name);

             currentPopulation = name;
         }

         else if (getCurrentElement().equals(NetworkMLConstants.INSTANCE_ELEMENT))
         {
             String id = attributes.getValue(NetworkMLConstants.INSTANCE_ID_ATTR);
             logger.logComment(">>  Found a pop id: "+ id);

             currentInstanceId = Integer.parseInt(id);
             
             String nodeId = attributes.getValue(NetworkMLConstants.NODE_ID_ATTR);
             if (nodeId!=null)
             {
                 currentNodeId = Integer.parseInt(nodeId);
             }
         }


         else if (getCurrentElement().equals(NetworkMLConstants.LOCATION_ELEMENT))
         {
             String x = attributes.getValue(NetworkMLConstants.LOC_X_ATTR);
             String y = attributes.getValue(NetworkMLConstants.LOC_Y_ATTR);
             String z = attributes.getValue(NetworkMLConstants.LOC_Z_ATTR);
             logger.logComment(">>  Found a location");


             PositionRecord posRec = new PositionRecord(currentInstanceId,
                                             Float.parseFloat(x),
                                             Float.parseFloat(y),
                                             Float.parseFloat(z));

             if (currentNodeId>=0)
             {
                 posRec.setNodeId(currentNodeId);
             }
             
             this.cellPos.addPosition(currentPopulation, posRec);
             

         }
         else if (getCurrentElement().equals(NetworkMLConstants.PROJECTIONS_ELEMENT))
         {
             String unitsUsed = attributes.getValue(NetworkMLConstants.UNITS_ATTR);
             logger.logComment("unitsUsed: "+unitsUsed);
             projUnitSystem = UnitConverter.getUnitSystemIndex(unitsUsed);
             
         }
         else if (getCurrentElement().equals(NetworkMLConstants.INPUTS_ELEMENT))
         {
             String unitsUsed = attributes.getValue(NetworkMLConstants.UNITS_ATTR);
             logger.logComment("unitsUsed: "+unitsUsed);
             inputUnitSystem = UnitConverter.getUnitSystemIndex(unitsUsed);
             
         }
         else if (getCurrentElement().equals(NetworkMLConstants.PROJECTION_ELEMENT))
         {
             this.currentProjection = attributes.getValue(NetworkMLConstants.PROJ_NAME_ATTR);
             
             //if ()
             
             Vector<SynapticProperties> sps = null;
             
             if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(currentProjection))
                sps = project.morphNetworkConnectionsInfo.getSynapseList(currentProjection);
             else if (project.volBasedConnsInfo.isValidVolBasedConn(currentProjection))
                sps = project.volBasedConnsInfo.getSynapseList(currentProjection);
             else
                 throw new SAXException("Error when parsing NetworkML file. Network Connection: "+ currentProjection
                         +" not found in project: "+ project.getProjectName()+".\n" +
                         "Add a network connection with this name to the project to allow this NetworkML file to be used with this project. ");
             
             for(SynapticProperties sp:sps)
             {
                 ConnSpecificProps csp = new ConnSpecificProps(sp.getSynapseType());
                 if (sp.getDelayGenerator().isTypeFixedNum())
                     csp.internalDelay = sp.getDelayGenerator().getNextNumber();   
                 else
                     csp.internalDelay = Float.NaN;
                 
                 if (sp.getWeightsGenerator().isTypeFixedNum())
                     csp.weight = sp.getWeightsGenerator().getNextNumber();   
                 else
                     csp.weight = Float.NaN;
                 
                 projectConnProps.add(csp);
             }
             
         }
         else if (getCurrentElement().equals(NetworkMLConstants.PRE_CONN_ELEMENT)
             && getAncestorElement(1).equals(NetworkMLConstants.CONNECTION_ELEMENT))
         {
             this.currentSourceCellNumber = Integer.parseInt(attributes.getValue(NetworkMLConstants.CELL_ID_ATTR));

             this.currentSourceCellSegmentIndex = 0;
             if (attributes.getValue(NetworkMLConstants.SEGMENT_ID_ATTR)!=null)
             {
                 currentSourceCellSegmentIndex = Integer.parseInt(attributes.getValue(NetworkMLConstants.
                     SEGMENT_ID_ATTR));
             }

             currentSourceCellDisplacement = 0.5f;
             if (attributes.getValue(NetworkMLConstants.FRACT_ALONG_ATTR)!=null)
             {
                 currentSourceCellDisplacement = Float.parseFloat(attributes.getValue(NetworkMLConstants.FRACT_ALONG_ATTR));
             }
         }
         else if (getCurrentElement().equals(NetworkMLConstants.CONNECTION_ELEMENT))
         {
             int connId = Integer.parseInt(attributes.getValue(NetworkMLConstants.CONNECTION_ID_ATTR));
             
             logger.logComment("Looking at conn: "+ connId);
             
             if (attributes.getValue(NetworkMLConstants.PRE_CELL_ID_ATTR)!=null)
             {
                 currentSourceCellNumber = Integer.parseInt(attributes.getValue(NetworkMLConstants.PRE_CELL_ID_ATTR));
             }
             currentSourceCellSegmentIndex =  0;
             if (attributes.getValue(NetworkMLConstants.PRE_SEGMENT_ID_ATTR)!=null)
             {
                 currentSourceCellSegmentIndex = Integer.parseInt(attributes.getValue(NetworkMLConstants.PRE_SEGMENT_ID_ATTR));
             }
             currentSourceCellDisplacement = 0.5f;
             if (attributes.getValue(NetworkMLConstants.PRE_FRACT_ALONG_ATTR)!=null)
             {
                 currentSourceCellDisplacement = Float.parseFloat(attributes.getValue(NetworkMLConstants.PRE_FRACT_ALONG_ATTR));
             }
             
             if (attributes.getValue(NetworkMLConstants.POST_CELL_ID_ATTR)!=null)
             {
                 currentTargetCellNumber = Integer.parseInt(attributes.getValue(NetworkMLConstants.POST_CELL_ID_ATTR));
             }
             currentTargetCellSegmentIndex =  0;
             if (attributes.getValue(NetworkMLConstants.POST_SEGMENT_ID_ATTR)!=null)
             {
                 currentTargetCellSegmentIndex = Integer.parseInt(attributes.getValue(NetworkMLConstants.POST_SEGMENT_ID_ATTR));
             }
             currentTargetCellDisplacement = 0.5f;
             if (attributes.getValue(NetworkMLConstants.POST_FRACT_ALONG_ATTR)!=null)
             {
                 currentTargetCellDisplacement = Float.parseFloat(attributes.getValue(NetworkMLConstants.POST_FRACT_ALONG_ATTR));
             }
         }
         
         
         // Post v1.7.1 style
         else if (getCurrentElement().equals(NetworkMLConstants.SYN_PROPS_ELEMENT)
             && getAncestorElement(1).equals(NetworkMLConstants.PROJECTION_ELEMENT))
         {
             ConnSpecificProps connProps = null;
             if (attributes.getValue(NetworkMLConstants.SYN_TYPE_ATTR)!=null)
             {
                 this.currentSynType = attributes.getValue(NetworkMLConstants.SYN_TYPE_ATTR);
                 logger.logComment("currentSynType: " + currentSynType);
             }
             if (attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR)!=null)
             {
                 connProps = new ConnSpecificProps(currentSynType);
                 connProps.internalDelay = (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR)!=null)
             {
                 if (connProps==null) 
                     connProps = new ConnSpecificProps(currentSynType);
                 
                 connProps.internalDelay = connProps.internalDelay + 
                         (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR)!=null)
             {
                 if (connProps==null) 
                     connProps = new ConnSpecificProps(currentSynType);
                 
                 connProps.internalDelay = connProps.internalDelay + 
                         (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)!=null)
             {                 
                 this.globAPDelay = (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.WEIGHT_ATTR)!=null)
             {
                 if (connProps==null) 
                     connProps = new ConnSpecificProps(currentSynType);
                 
                 connProps.weight = Float.parseFloat(attributes.getValue(NetworkMLConstants.WEIGHT_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.THRESHOLD_ATTR)!=null)
             {
                 logger.logComment("Note: connection specific thresholds not implemented!!");
             }
             
             if (connProps!=null) this.globConnProps.add(connProps);
         }

         else if (getCurrentElement().equals(NetworkMLConstants.POST_CONN_ELEMENT)
             && getAncestorElement(1).equals(NetworkMLConstants.CONNECTION_ELEMENT))
         {
             currentTargetCellNumber = Integer.parseInt(attributes.getValue(NetworkMLConstants.CELL_ID_ATTR));

             currentTargetCellSegmentIndex =  0;
             if (attributes.getValue(NetworkMLConstants.SEGMENT_ID_ATTR)!=null)
             {
                 currentTargetCellSegmentIndex = Integer.parseInt(attributes.getValue(NetworkMLConstants.
                     SEGMENT_ID_ATTR));
             }

             currentTargetCellDisplacement = 0.5f;
             if (attributes.getValue(NetworkMLConstants.FRACT_ALONG_ATTR)!=null)
             {
                 currentTargetCellDisplacement = Float.parseFloat(attributes.getValue(NetworkMLConstants.FRACT_ALONG_ATTR));
             }

         }

         else if (getCurrentElement().equals(NetworkMLConstants.CONN_PROP_ELEMENT)
                  && getAncestorElement(1).equals(NetworkMLConstants.CONNECTION_ELEMENT))
         {
             String synType = currentSynType;
             String inclSynType = attributes.getValue(NetworkMLConstants.SYN_TYPE_ELEMENT);

             if (inclSynType != null && inclSynType.length() > 0)
                 synType = inclSynType;

             ConnSpecificProps localProps = null;
             
             
             if (attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.internalDelay = (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.internalDelay = localProps.internalDelay + 
                         (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.internalDelay = localProps.internalDelay + 
                         (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
             if (attributes.getValue(NetworkMLConstants.WEIGHT_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.weight = Float.parseFloat(attributes.getValue(NetworkMLConstants.WEIGHT_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.THRESHOLD_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 logger.logComment("Note: conn specific threshold not implemented!!");
             }

             if (this.localConnProps == null) this.localConnProps = new ArrayList<ConnSpecificProps> ();
             if (localProps!=null)  localConnProps.add(localProps);

             if (attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)!=null)
             {
                 this.localAPDelay = (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             }
         }
         else if (getCurrentElement().equals(NetworkMLConstants.INPUT_ELEMENT)
                 && getAncestorElement(1).equals(NetworkMLConstants.INPUTS_ELEMENT))
         {    
         
             String inputName = attributes.getValue(NetworkMLConstants.INPUT_NAME_ATTR);

             logger.logComment(">>  Found a input of name: "+ inputName);

             currentInputName = inputName;
             //currentElecInput = inputName;
             
/*             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             if (ss==null) 
             {
                 GuiUtils.showWarningMessage(logger, "Error, stimulation reference "+inputName+" not found in project", null);
                 currentInputName = null;
             }
*/             
         }
         
         else if (getCurrentElement().equals(NetworkMLConstants.INPUT_TARGET_ELEMENT))
         {
             currentCellGroup = attributes.getValue(NetworkMLConstants.INPUT_TARGET_CELLGROUP_ATTR);
                        
             if (!project.cellGroupsInfo.isValidCellGroup(currentCellGroup))
             {
                 GuiUtils.showWarningMessage(logger, "Error, target cell group "+currentCellGroup+" not found in project", null);
                 currentCellGroup = null;
             }
         }
         // If pulse input get pulse input attributes
         else if (getCurrentElement().equals(NetworkMLConstants.PULSEINPUT_ELEMENT))
         {
             currentInputType = IClamp.TYPE;
             
             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             
             if (!(ss instanceof IClampSettings))
             {
                  GuiUtils.showWarningMessage(logger, "Error, IClamp "+currentInputName+" not found in project", null);
                  currentInputName = null;                
             }
             else
             {
                 
                 Float currentPulseDelay = 
                         (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.INPUT_DELAY_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 Float currentPulseDur = 
                         (float)UnitConverter.getTime(Float.parseFloat (attributes.getValue(NetworkMLConstants.INPUT_DUR_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 Float currentPulseAmp = 
                         (float)UnitConverter.getCurrent(Float.parseFloat(attributes.getValue(NetworkMLConstants.INPUT_AMP_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 
                 IClampSettings currentIClampSettings = (IClampSettings)ss;
                 
                 try
                 {
//////////                     currentIClampSettings.getDelay().reset();
//////////                     float currDelay = currentIClampSettings.getDelay().getNumber();
//////////                     if (currDelay != currentPulseDelay)
//////////                     {
//////////                       GuiUtils.showWarningMessage(logger, "Error, imported delay ("+currentPulseDelay+") for IClamp "+currentInputName+" is different from that currently in the project: "+ ss, null);                
//////////                     }                 
//////////
//////////                     currentIClampSettings.getDuration().reset();
//////////                     float currDur = currentIClampSettings.getDuration().getNumber();
//////////                     if (currDur != currentPulseDur)
//////////                     {
//////////                       GuiUtils.showWarningMessage(logger, "Error, imported duration ("+currentPulseDur+") for IClamp "+currentInputName+" is different from that currently in the project: "+currDur, null);                
//////////                     }
//////////
//////////                     currentIClampSettings.getAmplitude().reset();
//////////                     float currAmp = currentIClampSettings.getAmplitude().getNumber();
//////////
//////////                     if (currAmp != currentPulseAmp)
//////////                     {
//////////                       GuiUtils.showWarningMessage(logger, "Error, the imported amplitude ("+currentPulseAmp+") for IClamp "+currentInputName+" is different from that currently in the project: "+ currAmp, null);                
//////////                     }
                } 
                catch (Exception ex)
                {
                    logger.logError("Legacy error getting iclamp params!!");
                }
             }
         }             
             
         // If random input get random input attributes
         else if (getCurrentElement().equals(NetworkMLConstants.RANDOMSTIM_ELEMENT))
         {
             currentInputType = RandomSpikeTrain.TYPE;
             
             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             
             if (!(ss instanceof RandomSpikeTrainSettings))
             {
                  GuiUtils.showWarningMessage(logger, "Error, RandomSpikeTrain "+currentInputName+" not found in project", null);
                  currentInputName = null;                
             }
             else
             {
                 // get stim frequency for random input and convert to float so it can be added to RandomSpikeStrain
                 float currentRate = (float)UnitConverter.getRate(Float.parseFloat(attributes.getValue(NetworkMLConstants.RND_STIM_FREQ_ATTR)), 
                         inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 
                 // noise is not currently in v1.7 so it is set to a standard value of 10
                 // get current noise from inported file
                 //Float currentNoise = Float.parseFloat(attributes.getValue(NetworkMLConstants.RND_STIM_NOISE_ATTR));
                 //Float currentNoise = new Float(10.0);

                 // get stim mechanism for random input and convert to float so it can be added to RandomSpikeStrain
                 String currentSynapseType = attributes.getValue(NetworkMLConstants.RND_STIM_MECH_ATTR);
                 
                 RandomSpikeTrainSettings currentRandomSpikeTrainSettings = (RandomSpikeTrainSettings)ss;
                 
                 if (currentRandomSpikeTrainSettings.getRate().getFixedNum() != currentRate)
                 //if (currentRandomSpikeTrainSettings.getRate() != currentRate)
                 {
                   GuiUtils.showWarningMessage(logger, "Error, imported rate ("+currentRate+") for RandomSpikeTrain "+currentInputName+" is different from that currently in the project", null);                
                 }                 
                                  
/*                 if (currentRandomSpikeTrainSettings.getNoise() != currentNoise)
                 {
                   GuiUtils.showWarningMessage(logger, "Error, imported noise ("+currentNoise+") for RandomSpikeTrain "+currentInputName+" is different from that currently in the project", null);                
                 }
*/                 
                 if (!currentRandomSpikeTrainSettings.getSynapseType().equals(currentSynapseType))
                 {
                   GuiUtils.showWarningMessage(logger, "Error, imported synapse type ("+currentSynapseType+") for RandomSpikeTrain "+currentInputName+" is different from that currently in the project", null);                
                 }
             }
         }             
             
         // if site element get cellID segmentID and fraction along
         else if (getCurrentElement().equals(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT))
         {
             // get cell ID target cell convert to int so it can be added to SingleElectricalInput in GeneratedInputs
             Integer currentCellID = (Integer.parseInt(attributes.getValue(NetworkMLConstants.INPUT_SITE_CELLID_ATTR)));
             // get segment ID of target cell and convert to int so it can be added to SingleElectricalInput in GeneratedInputs
             Integer currentSegID = (Integer.parseInt(attributes.getValue(NetworkMLConstants.INPUT_SITE_SEGID_ATTR)));
             // get fraction along segment of target segment and convert to float so it can be added to SingleElectricalInput in GeneratedInputs
             Float currentFrac = (Float.parseFloat(attributes.getValue(NetworkMLConstants.INPUT_SITE_FRAC_ATTR)));
             
             SingleElectricalInput currentInput = new SingleElectricalInput(currentInputType, currentCellGroup, currentCellID, currentSegID, currentFrac, null);
             elecInputs.addSingleInput(currentInputName, currentInput);
          }         
    }
    
    private ConnSpecificProps getGlobalSynProps(String synType)
    {
        for(ConnSpecificProps props: globConnProps)
        {
            if (props.synapseType.equals(synType))
                return new ConnSpecificProps(props);
        }
        return new ConnSpecificProps(synType);
    }
    private ConnSpecificProps getProjSynProps(String synType)
    {
        for(ConnSpecificProps props: projectConnProps)
        {
            if (props.synapseType.equals(synType))
                return new ConnSpecificProps(props);
        }
        return new ConnSpecificProps(synType);
    }
    

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
    {

        logger.logComment("-----   End element: " + localName);

        if (getCurrentElement().equals(NetworkMLConstants.POPULATION_ELEMENT))
        {
            currentPopulation = null;
            //this.currentCellType = null;
        }
        else if (getCurrentElement().equals(NetworkMLConstants.INSTANCE_ELEMENT))
        {
            this.currentInstanceId = -1;
            this.currentNodeId = -1;
        }
        else if (getCurrentElement().equals(NetworkMLConstants.PROJECTION_ELEMENT))
        {
            globAPDelay = 0;
            globConnProps = new ArrayList<ConnSpecificProps>();
        }
        else if (getCurrentElement().equals(NetworkMLConstants.CONNECTION_ELEMENT))
        {

            float propDelay = globAPDelay;
            
            if (localAPDelay>0) propDelay = this.localAPDelay;
            
            
            
            ArrayList<ConnSpecificProps> connProps = new ArrayList<ConnSpecificProps>();
            connProps.addAll(globConnProps);
            
            for(ConnSpecificProps props: localConnProps)
            {
                for(ConnSpecificProps globProps: globConnProps)
                {
                    if (globProps.synapseType.equals(props.synapseType))
                    {
                        connProps.remove(globProps);
                    }
                }
                connProps.add(props);
            }
            
            ArrayList<ConnSpecificProps> connPropsToUse = new ArrayList<ConnSpecificProps>();
            for(ConnSpecificProps cp: connProps)
            {
                if (!getProjSynProps(cp.synapseType).equals(cp))
                    connPropsToUse.add(cp);
            }
            
            this.netConns.addSynapticConnection(this.currentProjection,
                    GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                currentSourceCellNumber,
                                                currentSourceCellSegmentIndex,
                                                currentSourceCellDisplacement,
                                                currentTargetCellNumber,
                                                currentTargetCellSegmentIndex,
                                                currentTargetCellDisplacement,
                                                propDelay,
                                                connPropsToUse);

            this.localConnProps = new ArrayList<ConnSpecificProps>();
            localAPDelay = 0;
        }
/*        else if (getCurrentElement().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            //elecInputs.addSingleInput(name, );
            currentElecInput = null;
            //this.currentCellType = null;
        }
*/

        



        stepDownElement();
    }



    public static void main(String args[])
    {

        try
        {
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("examples/Ex4-NEURONGENESIS/Ex4-NEURONGENESIS.neuro.xml"),null);

            File f = new File("examples/Ex4-NEURONGENESIS/savedNetworks/nnn.nml");

            logger.logComment("Loading netml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);
            
            ProjectManager pm = new ProjectManager(null, null);
            
            pm.setCurrentProject(testProj);
            
            pm.doLoadNetworkMLAndGenerate(f);
            
            
            while (pm.isGenerating())
            {
                Thread.sleep(2);
                System.out.println("Waiting...");
            }
            
            
            System.out.println(testProj.generatedElecInputs.toString());
            

            /*
            GeneratedCellPositions gcp = new GeneratedCellPositions(testProj);
            GeneratedNetworkConnections gnc = new GeneratedNetworkConnections(testProj);

            FileInputStream instream = null;
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            NetworkMLReader nmlBuilder = new NetworkMLReader(gcp, gnc);
            xmlReader.setContentHandler(nmlBuilder);

            instream = new FileInputStream(f);

            is = new InputSource(instream);

            xmlReader.parse(is);

            logger.logComment("Contents: "+gcp.toString());
            logger.logComment("Net conns: "+gnc.toNiceString());*/




        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
