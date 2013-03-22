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

import java.awt.Color;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.converters.MorphMLReader;
import ucl.physiol.neuroconstruct.genesis.GenesisSettings;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.SimulatorMapping;
import ucl.physiol.neuroconstruct.neuron.NeuronSettings;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.AllCells;
import ucl.physiol.neuroconstruct.project.cellchoice.CellChooser;
import ucl.physiol.neuroconstruct.project.packing.CellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.SinglePositionedCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.project.stimulation.IClampInstanceProps;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainInstanceProps;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;

import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLAttribute;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLDocument;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLElement;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLNamespace;

/**
 * NetworkML file Reader. Importer of NetworkML files to neuroConstruct format using SAX
 *
 * @author Padraig Gleeson
 *  
 * 
 * Changes made to extend the importer to Level 3 NeuroML files (cells and channels have to be extracted):
 * - if the startElement find a CELL element turn on the flag insideCell.
 * - if the flag insideCell is true the reader start to copy the file line by line in a new file containing a Level 3 Cell Description.
 * - once the CELL is finished the new cell type is loaded in the project.
 * 
 * The same procedure is applied to the CHANNEL type...
 * NB: the channels can't be renamed by the user because the loaded cells will use the old names.
 * 
 * @author  Matteo Farinella
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
    private String currentInputCellGroup = null;
    private String currentInputName = null;

    private ElectricalInput currentElectricalInput = null;

    private SingleElectricalInput currentSingleInput = null;

    private HashMap<String, SimpleXMLElement> ionElements =  new HashMap<String, SimpleXMLElement>();
    
    private boolean level3 = false;
    
    private StringBuffer cellInfoBuffer = new StringBuffer(); // contains the cell element of a Level 3 Network that once stored in a file can be parse with morphMLReader

    private boolean insideCell = false;
    private String cellPrefix = "";
    private String cellName = "Cell";
    private Boolean addCell = true;
    private Boolean replaceAll = false;
    private Boolean renameAll = false;                            
    
    private String chanBuffer = ""; // contains the channelML substring of a Level 3 Network that once stored in a file can be parse with channelMLReader
    private boolean insideChannel = false;
    private String chanPrefix = "";
    private String chanName = "Channel";
    private Boolean replace = false;
    
    private Hashtable<String, String> renamedCells = new Hashtable<String, String>();
    private RectangularBox region = new RectangularBox(0, 0, 0, 100, 100, 100);
    private String source = "";
    private String target = "";
    private boolean addNet = false;
    
    private String synInput = "";
    private boolean addStim = false;
    
    private SimConfig simConfigToUse = new SimConfig();
    private Integer priority = 0;
    private String groupCellType = "";
    
    private Boolean annotations = false; //if true networkMLreader will assume that all the necessary NC informations are stored in annotations of the level3 file
                                                        //if false it will try to add some buffer NC objects just to enable the project generation
    private Boolean firstOccurence = true;
    private Boolean insideAnnotation = false;
    private Boolean addAnnotations = true;
    private String annotationString = "";

    //private String metadataPrefix = MetadataConstants.PREFIX + ":";

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    private GeneratedElecInputs elecInputs = null;    
    
    private Project project = null;
    
    public boolean testMode = false;

    public NetworkMLReader(Project project)
    {
        this.cellPos = project.generatedCellPositions;
        this.netConns = project.generatedNetworkConnections;
        this.elecInputs = project.generatedElecInputs;
        this.project = project;
        this.testMode = false;
    }
    
    public void setTestMode(boolean test)
    {
        this.testMode = test;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String contents = new String(ch, start, length);
        
        if (contents.trim().length() > 0)
        {
            logger.logComment("Got a string: (" + contents + ") at: "+ elementStack);
            
         if (insideCell)
         {
              contents = contents.replace("<", "&lt;");
              contents = contents.replace(">", "&gt;");
              contents = contents.replace("\"", "&quot;");
              contents = contents.replace("&", "&amp;");
              cellInfoBuffer.append(contents);
         }
         else
         {
                
         if (insideChannel)
         {
              contents = contents.replace("<", "&lt;");
              contents = contents.replace(">", "&gt;");
              contents = contents.replace("\"", "&quot;");
              contents = contents.replace("&", "&amp;");
              chanBuffer = chanBuffer + contents;            
         }
         else
         {
         
        if (insideAnnotation && addAnnotations)
        {
            contents = contents.replace("<", "&lt;");
            contents = contents.replace(">", "&gt;");
            contents = contents.replace("\"", "&quot;");
            contents = contents.replace("&", "&amp;");
            annotationString = annotationString + contents;
        }
        else
        {
             
            if (getCurrentElement().equals(NetworkMLConstants.CELLTYPE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.POPULATION_ELEMENT))
            {
                //currentCellType = contents;
                contents = renamedCells.get(contents);
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
                setProperty(currentPropertyName, contents);

                /*
                if (this.currentPropertyName.equals(NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED))
                {
                    this.foundRandomSeed = Long.parseLong(contents);
                }
                else if (this.currentPropertyName.equals(NetworkMLConstants.NC_SIM_CONFIG))
                {
                    this.foundSimConfig = contents;
                    Integer nameN = -1;
                    String simName = this.foundSimConfig;
                    SimConfig existingSC = project.simConfigInfo.getSimConfig(foundSimConfig);

                    if (foundSimConfig.equals(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME) && existingSC==null)
                    {
                        existingSC = new SimConfig(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, SimConfigInfo.DEFAULT_SIM_CONFIG_DESC);
                        project.simConfigInfo.add(existingSC);
                    }

                    if (existingSC!=null )
                    {
                        simConfigToUse = existingSC;
                    }
                    else
                    {
                        while (project.simConfigInfo.getAllSimConfigNames().contains(simName))
                        {
                            nameN++;
                            simName = this.foundSimConfig.concat("_imported"+nameN);
                        }
                        
                        simConfigToUse = new SimConfig(simName, "");
                        project.simConfigInfo.add(simConfigToUse);
                    }
                    logger.logComment(">>>Existing simulation configuration: "+ (existingSC==null?existingSC:existingSC.toLongString()));
                    logger.logComment(">>>Using simulation configuration: "+ simConfigToUse);

                }
                else if (this.currentPropertyName.equals(NetworkMLConstants.NC_SIM_DURATION))
                {
                     project.simulationParameters.setDuration(Float.valueOf(contents));
                     simConfigToUse.setSimDuration(Float.valueOf(contents));
                     logger.logComment(">>>Found a simulation duration...");
                 }
                else if (this.currentPropertyName.equals(NetworkMLConstants.NC_SIM_TIME_STEP)) 
                {
                     project.simulationParameters.setDt(Float.valueOf(contents));
                     //importedSimConfig.setSimDt(Float.valueOf(contents));
                     logger.logComment(">>>Found a simulation time step duration...");
                }
                else if (this.currentPropertyName.equals(NetworkMLConstants.NC_TEMPERATURE)) 
                 {
                     project.simulationParameters.setTemperature(Float.valueOf(contents));
                     logger.logComment(">>>Found a simulation temperature...");
                 }     */
            }
         }//else annotation
         }//else channel
         }//else cell
        }
    }


    private void setProperty(String name, String value)
    {
        logger.logComment("Setting property  "+name+": "+value);
        if (name == null)
        {
            logger.logError("Setting null property:  "+name+": "+value);
        }
        else if(name.equals(NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED))
        {
            this.foundRandomSeed = Long.parseLong(value);
        }
        else if (name.equals(NetworkMLConstants.NC_SIM_CONFIG))
        {
            this.foundSimConfig = value;
            Integer nameN = -1;
            String simName = this.foundSimConfig;
            SimConfig existingSC = project.simConfigInfo.getSimConfig(foundSimConfig);

            if (foundSimConfig.equals(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME) && existingSC==null)
            {
                existingSC = new SimConfig(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, SimConfigInfo.DEFAULT_SIM_CONFIG_DESC);
                project.simConfigInfo.add(existingSC);
            }

            if (existingSC!=null /*&& existingSC.getCellGroups().size()==0*/)
            {
                simConfigToUse = existingSC;
            }
            else
            {
                while (project.simConfigInfo.getAllSimConfigNames().contains(simName))
                {
                    nameN++;
                    simName = this.foundSimConfig.concat("_imported"+nameN);
                }

                simConfigToUse = new SimConfig(simName, "");
                project.simConfigInfo.add(simConfigToUse);
            }
            logger.logComment(">>>Existing simulation configuration: "+ (existingSC==null?existingSC:existingSC.toLongString()));
            logger.logComment(">>>Using simulation configuration: "+ simConfigToUse);

        }
        else if (name.equals(NetworkMLConstants.NC_SIM_DURATION))
        {
             project.simulationParameters.setDuration(Float.valueOf(value));
             simConfigToUse.setSimDuration(Float.valueOf(value));
             logger.logComment(">>>Found a simulation duration...");
         }
        else if (name.equals(NetworkMLConstants.NC_SIM_TIME_STEP))
        {
             project.simulationParameters.setDt(Float.valueOf(value));
             //importedSimConfig.setSimDt(Float.valueOf(contents));
             logger.logComment(">>>Found a simulation time step duration...");
        }
        else if (name.equals(NetworkMLConstants.NC_TEMPERATURE))
         {
             project.simulationParameters.setTemperature(Float.valueOf(value));
             logger.logComment(">>>Found a simulation temperature...");
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
         String renamedVal = renamedCells.get(val);

         logger.logComment("Attr:  " + name+ " = " + val+ " ("+renamedVal+")         (qname: "
                           + attributes.getQName(i)+ ", uri: " + attributes.getURI(i)+")");
        }

         setCurrentElement(localName);
         
         logger.logComment("current element:"+ getCurrentElement());
         
         if (!insideCell && getCurrentElement().equals(MorphMLConstants.CELL_ELEMENT))
         {
             level3 = true;
             
             logger.logComment(">>  Found a cell type: going to write a separate NeuroML file");
             cellName = attributes.getValue(attributes.getLocalName(0));
             insideCell = true;

             logger.logComment("simConfigToUse: "+simConfigToUse);
             if (simConfigToUse.getName()==null || simConfigToUse.getName().length()==0)
             {
                 if (project.simConfigInfo.getDefaultSimConfig().getCellGroups().isEmpty())
                 {
                     simConfigToUse = project.simConfigInfo.getDefaultSimConfig();
                 }
             }

         }
                      
         logger.logComment("inside a cell:" +insideCell);
        
         
         if (insideCell)             
         {
             
             //dealing with prefix

             if (elementStack.contains("segments") || elementStack.contains("cables"))
             {
                 cellPrefix = MorphMLConstants.PREFIX+":";

                 if (getCurrentElement().equals("group"))
                     cellPrefix = MetadataConstants.PREFIX+":";
             }
             else if (elementStack.contains("biophysics"))
             {
                 cellPrefix = BiophysicsConstants.PREFIX + ":";
             }
             else if (elementStack.contains("connectivity"))
             {
                 cellPrefix = NetworkMLConstants.PREFIX + ":";
             }
             else if (getCurrentElement().equals("notes"))
             {
                 cellPrefix = MetadataConstants.PREFIX+":";
             }

             if (getCurrentElement().equals("cell") || getCurrentElement().equals("biophysics") || getCurrentElement().equals("connectivity"))
                 cellPrefix = "";
                                   
         //string elongation
             if (getCurrentElement().equals("biophysics"))
             {
                 cellInfoBuffer.append("<!--Adding the biophysical parameters-->\n");
             }
             if (cellInfoBuffer.length()>1 && cellInfoBuffer.charAt(cellInfoBuffer.length()-1) == '>')
             {
                 cellInfoBuffer.append("\n");
             }
             cellInfoBuffer.append("<"+cellPrefix+getCurrentElement());

             for (int i = 0; i < attrsLength; i++)
             {
                 String name = attributes.getLocalName(i);
                 String val = attributes.getValue(i);

                 cellInfoBuffer.append(" "+name+"=\""+val+"\"");
             }
             cellInfoBuffer.append(">");

         }

         logger.logComment("cellPrefix:" +cellPrefix);

         if (getCurrentElement().equals(ChannelMLConstants.ION_ELEMENT))
         {
            SimpleXMLElement ionEl = new SimpleXMLElement(ChannelMLConstants.ION_ELEMENT);
            String ionName = attributes.getValue(ChannelMLConstants.LEGACY_ION_NAME_ATTR);

            ionEl.addAttribute(ChannelMLConstants.LEGACY_ION_NAME_ATTR, ionName);
            if (attributes.getValue(ChannelMLConstants.ION_CHARGE_ATTR)!=null)
            {
                ionEl.addAttribute(ChannelMLConstants.ION_CHARGE_ATTR, attributes.getValue(ChannelMLConstants.ION_CHARGE_ATTR));
            }
            if (attributes.getValue(ChannelMLConstants.ION_ROLE_ATTR)!=null)
            {
                ionEl.addAttribute(ChannelMLConstants.ION_ROLE_ATTR, attributes.getValue(ChannelMLConstants.ION_ROLE_ATTR));
            }
            ionElements.put(ionName, ionEl);

             logger.logComment(">>  Current ions: "+ ionElements);
         }
         
         if (!insideChannel && 
             ((getCurrentElement().equals(ChannelMLConstants.CHAN_TYPE_ELEMENT)) ||
             (getCurrentElement().equals(ChannelMLConstants.SYN_TYPE_ELEMENT))||
             (getCurrentElement().equals(ChannelMLConstants.ION_CONC_ELEMENT))))
         {
             logger.logComment(">>  Found a channel mechanism: going to write a separate ChannelML file");
             chanName = attributes.getValue(attributes.getLocalName(0));
             insideChannel = true;
         }
         
         logger.logComment("inside a channel:" +insideChannel);
             
         if (insideChannel)
         {
         //dealing with prefix
             Vector<String> meta = new Vector<String>();
             meta.add("comment");
             meta.add("issue");
             meta.add("contributor");
             meta.add("notes");
             meta.add("authorList");
             meta.add("modelAuthor");
             meta.add("modelTranslator");
             meta.add("publication");
             meta.add("neuronDBref");
             meta.add("modelDBref");
             meta.add("modelName");

             if (getCurrentElement().equals("comment") && getAncestorElement(1).equals("impl_prefs"))
                 chanPrefix = "";
             else if (meta.contains(getCurrentElement()) || meta.contains(getAncestorElement(1)))
                 chanPrefix = MetadataConstants.PREFIX+":";
             else
                chanPrefix = "";

         //string elongation
             if (chanBuffer.endsWith(">"))
                 chanBuffer = chanBuffer + "\n";
             chanBuffer = chanBuffer + ("<"+chanPrefix+getCurrentElement());
             for (int i = 0; i < attributes.getLength(); i++) {
                 String name = attributes.getLocalName(i);
                 String val = attributes.getValue(i);

                 val = GeneralUtils.replaceAllTokens(val, "<", "&lt;");

                 chanBuffer = chanBuffer + (" "+name+"=\""+val+"\"");
             }
            chanBuffer = chanBuffer +">";

         }
         
         if (!insideAnnotation && getCurrentElement().equals(MetadataConstants.ANNOTATION_ELEMENT))
         {             
             annotations = true;
             level3 = true;         
             
              if (!testMode && annotations && firstOccurence)
              {
                  firstOccurence = false;
                  logger.logComment("The file contains neuroConstruct annotations that will overwrite existing information on Regions, cell Groups, etc. Asking the user...");
                  Object[] options2 = {"Import", "Ignore"};
                  Object choice = "";

                  JOptionPane option2 = new JOptionPane(
                          "The file contains neuroConstruct annotations that will overwrite existing information on Regions, Cell Groups, Networks and input/output configurations.\n" +
                          "What do you want to do with them?",
                          JOptionPane.DEFAULT_OPTION,
                          JOptionPane.WARNING_MESSAGE,
                          null,
                          options2,
                          options2[0]);

                  JDialog dialog2 = option2.createDialog(null, "Import annotations?");
                  dialog2.setVisible(true);
                  choice = option2.getValue();
                  if (choice.equals("Ignore"))
                  {
                      logger.logComment("User chose to ignore annotations");
                      addAnnotations = false;
                  }
             }
             logger.logComment(">>  Found an annotation element: going to extract relevant informations for NeuroConstruct");
             insideAnnotation = true;    
             annotationString = "";
         }         
         
         if (insideAnnotation && !getCurrentElement().equals(MetadataConstants.ANNOTATION_ELEMENT) && addAnnotations)
         {
              logger.logComment("inside an annotation:" +insideAnnotation);
              annotationString = annotationString + "\n<" + getCurrentElement();
               for (int i = 0; i < attributes.getLength(); i++)
               {
                 String name = attributes.getLocalName(i);
                 String val = attributes.getValue(i);
                 
                 annotationString = annotationString + (" "+name+"=\""+val+"\"");
               }
               annotationString = annotationString + ">";
         }

         if(getCurrentElement().equals(MetadataConstants.PROP_ELEMENT)
             && getAncestorElement(1).equals(MetadataConstants.PROPS_ELEMENT))
         {
             String name = attributes.getValue(MetadataConstants.PROP_TAG_ATTR);
             String value = attributes.getValue(MetadataConstants.PROP_VALUE_ATTR);

             setProperty(name, value);
         }
         else if(getCurrentElement().equals(NetworkMLConstants.POPULATION_ELEMENT))
         {
             String name = attributes.getValue(NetworkMLConstants.POP_NAME_ATTR);
             
             groupCellType = attributes.getValue(NetworkMLConstants.CELLTYPE_ATTR);
             
             logger.logComment(">>  Found a population of name: "+ name);
             currentPopulation = name;             
         }
         
         else if (getCurrentElement().equals(NetworkMLConstants.INSTANCES_ELEMENT))
         {
            Integer popNumber = Integer.valueOf(attributes.getValue(NetworkMLConstants.INSTANCES_SIZE_ATTR));
             
            if (!project.cellGroupsInfo.getAllCellGroupNames().contains(currentPopulation) && (level3))
            {
                logger.logComment("groupCellType: "+groupCellType+", renamedCells: "+renamedCells);
                if (renamedCells.containsKey(groupCellType))
                {
                     groupCellType = renamedCells.get(groupCellType);
                }
                
                if (!annotations)
                {
                                   
                    logger.logComment("Going to add a group "+currentPopulation+" for the new cell type "+groupCellType);
                    try
                    {
                        Random rand= new  Random();
                        Color col = new Color(rand.nextInt(256),
                                              rand.nextInt(256),
                                              rand.nextInt(256));

                        project.regionsInfo.addRow(currentPopulation+"_region", region, col);

                        CellPackingAdapter cp = new RandomCellPackingAdapter();
                        try
                        {
                            cp.setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, popNumber);
                            cp.setParameter(RandomCellPackingAdapter.EDGE_POLICY, 0);
                            cp.setParameter(RandomCellPackingAdapter.SELF_OVERLAP_POLICY, 1);
                            cp.setParameter(RandomCellPackingAdapter.OTHER_OVERLAP_POLICY, 1);


                            if (popNumber==1)
                            {
                                cp =  new SinglePositionedCellPackingAdapter(0,0,0);
                            }

                        }
                        catch (CellPackingException ex)
                        {
                            logger.logError("Error: "+ex.getMessage(), ex);
                        }
                        project.cellGroupsInfo.addRow(currentPopulation, groupCellType, currentPopulation+"_region", col, cp, priority++);
                        simConfigToUse.addCellGroup(currentPopulation);

                        logger.logComment("simConfigToUse "+simConfigToUse.toLongString());

                        project.markProjectAsEdited();
                    }
                    catch (NamingException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem creating a new cell group...", ex, null);

                    }
                }
            }
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

             if (project.cellGroupsInfo.getCellPackingAdapter(currentPopulation) instanceof SinglePositionedCellPackingAdapter)
             {
                 SinglePositionedCellPackingAdapter pa = (SinglePositionedCellPackingAdapter)project.cellGroupsInfo.getCellPackingAdapter(currentPopulation);
                 try
                 {
                    pa.setPosition(posRec.x_pos, posRec.y_pos, posRec.z_pos);
                 }
                 catch(CellPackingException e)
                 {
                     logger.logError("Problem setting position", e);
                 }
             }
             

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

             logger.logComment(">>  Found a network connection of name: "+ currentProjection);
             
              Vector<SynapticProperties> sps = null;
              if (level3)
               {
                  simConfigToUse.addNetConn(currentProjection);
                  if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(currentProjection)) {
                      sps = project.morphNetworkConnectionsInfo.getSynapseList(currentProjection);
                  } else if (project.volBasedConnsInfo.isValidVolBasedConn(currentProjection)) {
                      sps = project.volBasedConnsInfo.getSynapseList(currentProjection);
                  } else if (!annotations){
                         addNet = true;
                         source = attributes.getValue(NetworkMLConstants.SOURCE_ELEMENT);
                         target = attributes.getValue(NetworkMLConstants.TARGET_ELEMENT);
                  }
              }
              else
              {               
                  if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(currentProjection)) {
                      sps = project.morphNetworkConnectionsInfo.getSynapseList(currentProjection);
                  } else if (project.volBasedConnsInfo.isValidVolBasedConn(currentProjection)) {
                      sps = project.volBasedConnsInfo.getSynapseList(currentProjection);
                  } else {
                      throw new SAXException("Error when parsing NetworkML file. Network Connection: " + currentProjection + " not found in project: " + project.getProjectName() + ".\n" +
                              "Add a network connection with this name to the project to allow this NetworkML file to be used with this project. ");
                  }
                  for (SynapticProperties sp : sps) {
                      ConnSpecificProps csp = new ConnSpecificProps(sp.getSynapseType());
                      if (sp.getDelayGenerator().isTypeFixedNum()) {
                      csp.internalDelay = sp.getDelayGenerator().getNextNumber();
                      } else {
                          csp.internalDelay = Float.NaN;
                      }
                      if (sp.getWeightsGenerator().isTypeFixedNum()) {
                      csp.weight = sp.getWeightsGenerator().getNextNumber();
                      } else {
                          csp.weight = Float.NaN;
                  }
                      projectConnProps.add(csp);
             }
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
             if (!annotations && level3 && addNet)
             { 
                 String synType = attributes.getValue(NetworkMLConstants.SYN_TYPE_ATTR);
                 SynapticProperties synProp = new SynapticProperties(synType);
                 Vector<SynapticProperties> synList = new Vector<SynapticProperties>();
                 synList.add(synProp);
                 SearchPattern sp = SearchPattern.getRandomSearchPattern();
                 MaxMinLength mml = new MaxMinLength(100, 0, "r", 100);
                 ConnectivityConditions connConds = new ConnectivityConditions();
                 float jumpSpeed = Float.MAX_VALUE;

                 logger.logComment("Going to add a volume based network connection "+currentProjection+" from group "+source+" to group "+target);

                 try {
                     project.morphNetworkConnectionsInfo.addRow(currentProjection, source, target, synList, sp, mml, connConds, jumpSpeed);
                 } catch (NamingException ex) {
                     logger.logComment("Problem creating volume based network connection...");
                 }
             }
               
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
             
             if (level3 && !project.elecInputInfo.getAllStimRefs().contains(currentInputName))
             {                 
                     addStim = true;
                     logger.logComment("Input "+currentInputName+" doesn't exist in the current project. Going to add it...");

             }
             
             //currentElecInput = inputName;
             
/*             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             if (ss==null) 
             {
                 GuiUtils.showWarningMessage(logger, "Error, stimulation reference "+inputName+" not found in project", null);
                 currentInputName = null;
             }
*/             
         }
         
               
         // If pulse input get pulse input attributes
         else if (getCurrentElement().equals(NetworkMLConstants.PULSEINPUT_ELEMENT))
         {
             currentInputType = IClamp.TYPE;
             
             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             
             if (!level3 && !(ss instanceof IClampSettings))
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


                 if (level3 && ss == null)
                 {
                     currentElectricalInput = new IClamp(currentPulseDelay, currentPulseDur, currentPulseAmp, false);
                 }
                 else
                 {
                     try
                     {
                         float currDelay = currentIClampSettings.getDel().getNominalNumber();
                         if (currDelay != currentPulseDelay)
                         {
                            GuiUtils.showWarningMessage(logger, "Error, delay in NetworkML ("+currentPulseDelay+") for IClamp: "+currentInputName+" is different from that currently in the project: "+ ss+".\n" +
                                    "Project settings will be used!", null);
                         }

                         float currDur = currentIClampSettings.getDur().getNominalNumber();
                         if (currDur != currentPulseDur)
                         {
                           GuiUtils.showWarningMessage(logger, "Error, duration in NetworkML ("+currentPulseDur+") for IClamp: "+currentInputName+" is different from that currently in the project: "+ss+".\n" +
                                    "Project settings will be used!", null);
                         }

                         //.getAmplitude().reset();
                         float currAmp = currentIClampSettings.getAmp().getNominalNumber();

                         if (currAmp != currentPulseAmp)
                         {
                           GuiUtils.showWarningMessage(logger, "Error, amplitude in NetworkML ("+currentPulseAmp+") for IClamp: "+currentInputName+" is different from that currently in the project: "+ ss+".\n" +
                                    "Project settings will be used!", null);
                         }
                    }
                    catch (Exception ex)
                    {
                        logger.logError("Legacy error getting iclamp params!!");
                    }
                 }
             }
         }             
             
         // If random input get random input attributes
         else if (getCurrentElement().equals(NetworkMLConstants.RANDOMSTIM_ELEMENT))
         {
             currentInputType = RandomSpikeTrain.TYPE;
             synInput = attributes.getValue(NetworkMLConstants.RND_STIM_MECH_ATTR);         
             StimulationSettings ss = project.elecInputInfo.getStim(currentInputName);
             

             if (!(ss instanceof RandomSpikeTrainSettings || ss instanceof RandomSpikeTrainExtSettings || ss instanceof RandomSpikeTrainVariableSettings) && !level3)
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

                 if (level3 && ss == null)
                 {
                     currentElectricalInput = new RandomSpikeTrain(new NumberGenerator(currentRate), currentSynapseType);
                 }
                 else
                 {
                     if (ss instanceof RandomSpikeTrainSettings)
                     {
                         RandomSpikeTrainSettings currentRandomSpikeTrainSettings = (RandomSpikeTrainSettings)ss;

                         if (currentRandomSpikeTrainSettings.getRate().getFixedNum() != currentRate)
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported rate ("+currentRate+") for RandomSpikeTrain "+currentInputName+" is different from that currently in the project", null);
                         }

                         if (!currentRandomSpikeTrainSettings.getSynapseType().equals(currentSynapseType))
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported synapse type ("+currentSynapseType+") for RandomSpikeTrain "+currentInputName+" is different from that currently in the project", null);
                         }
                     }
                     else if (ss instanceof RandomSpikeTrainExtSettings)
                     {
                         RandomSpikeTrainExtSettings currentRandomSpikeTrainSettings = (RandomSpikeTrainExtSettings)ss;

                         if (currentRandomSpikeTrainSettings.getRate().getFixedNum() != currentRate)
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported rate ("+currentRate+") for RandomSpikeTrainExtSettings "+currentInputName+" is different from that currently in the project", null);
                         }

                         if (!currentRandomSpikeTrainSettings.getSynapseType().equals(currentSynapseType))
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported synapse type ("+currentSynapseType+") for RandomSpikeTrainExtSettings "+currentInputName+" is different from that currently in the project", null);
                         }
                     }
                     else if (ss instanceof RandomSpikeTrainVariableSettings)
                     {
                         RandomSpikeTrainVariableSettings currentRandomSpikeTrainSettings = (RandomSpikeTrainVariableSettings)ss;

                         if (currentRandomSpikeTrainSettings.getRate().equals(""+currentRate))
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported rate ("+currentRate+") for RandomSpikeTrainVariableSettings "+currentInputName+" is different from that currently in the project", null);
                         }

                         if (!currentRandomSpikeTrainSettings.getSynapseType().equals(currentSynapseType))
                         {
                           GuiUtils.showWarningMessage(logger, "Error, imported synapse type ("+currentSynapseType+") for RandomSpikeTrainVariableSettings "+currentInputName+" is different from that currently in the project", null);
                         }
                     }
                 }
             }
         } 
         
         
         else if (getCurrentElement().equals(NetworkMLConstants.INPUT_TARGET_ELEMENT))
         {
             currentInputCellGroup = attributes.getValue(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR);
             if (currentInputCellGroup==null)
             {
                 currentInputCellGroup = attributes.getValue(NetworkMLConstants.INPUT_TARGET_CELLGROUP_OLD_ATTR); // check old name
             }
             if (currentInputCellGroup==null)
             {

                 GuiUtils.showErrorMessage(logger, "Problem finding population/cell group attribute in "+elementStack, null, null);
             }
             
             if (!project.cellGroupsInfo.isValidCellGroup(currentInputCellGroup))
             {
                 GuiUtils.showWarningMessage(logger, "Error, target cell group: "+currentInputCellGroup+" not found in project. Current Cell Groups: "
                         + project.cellGroupsInfo.getAllCellGroupNames(), null);
             }
             
             else if (!annotations && level3 && addStim)
             {
                 StimulationSettings stim = null;
                 CellChooser cellChoose = new AllCells();
                 

                 ArrayList<Integer> segs = new ArrayList<Integer>();
                 segs.add(0);

                 SegmentLocationChooser segChoose = new IndividualSegments(segs);
                 

                if (currentInputType.equals(IClamp.TYPE))
                {
                    IClamp iClamp = (IClamp)currentElectricalInput;
                    stim = new IClampSettings(currentInputName, currentInputCellGroup, cellChoose, segChoose, iClamp.getDel(), iClamp.getDur(), iClamp.getAmp(), false);
                }
                if (currentInputType.equals(RandomSpikeTrain.TYPE))
                {
                    RandomSpikeTrain rst = (RandomSpikeTrain)currentElectricalInput;
                    stim = new RandomSpikeTrainSettings(currentInputName, currentInputCellGroup, cellChoose, segChoose,  rst.getRate(), rst.getSynapseType());
                }
                project.elecInputInfo.addStim(stim);
                simConfigToUse.addInput(stim.getReference());
                logger.logComment(currentInputType+" electrical input "+currentInputName+" on the cell group "+ currentInputCellGroup+" added to the project.");
             }
         }
               
         // if site element get cellID segmentID and fraction along
         else if (getCurrentElement().equals(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT))
         {
             // get cell ID target cell convert to int so it can be added to SingleElectricalInput in GeneratedInputs
             Integer currentCellID = (Integer.parseInt(attributes.getValue(NetworkMLConstants.INPUT_SITE_CELLID_ATTR)));
             // get segment ID of target cell and convert to int so it can be added to SingleElectricalInput in GeneratedInputs
             Integer currentSegID = 0;
             
             if (attributes.getValue(NetworkMLConstants.INPUT_SITE_SEGID_ATTR)!=null)
             {
                currentSegID = Integer.parseInt(attributes.getValue(NetworkMLConstants.INPUT_SITE_SEGID_ATTR));
             }
             // get fraction along segment of target segment and convert to float so it can be added to SingleElectricalInput in GeneratedInputs
             Float currentFrac = 0.5f;
             
             if (attributes.getValue(NetworkMLConstants.INPUT_SITE_FRAC_ATTR)!=null)
             {
                currentFrac = Float.parseFloat(attributes.getValue(NetworkMLConstants.INPUT_SITE_FRAC_ATTR));
             }

             currentSingleInput = new SingleElectricalInput(currentInputType, currentInputCellGroup, currentCellID, currentSegID, currentFrac, null);
             logger.logComment("New instance: "+ currentSingleInput);

             elecInputs.addSingleInput(currentInputName, currentSingleInput);
          }
         else if (getCurrentElement().equals(NetworkMLConstants.PULSEINPUT_INSTANCE_ELEMENT) && getAncestorElement(1).equals(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT))
         {

             Float currentPulseDelay =
                     (float)UnitConverter.getTime(Float.parseFloat(attributes.getValue(NetworkMLConstants.INPUT_DELAY_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             Float currentPulseDur =
                     (float)UnitConverter.getTime(Float.parseFloat (attributes.getValue(NetworkMLConstants.INPUT_DUR_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
             Float currentPulseAmp =
                     (float)UnitConverter.getCurrent(Double.parseDouble(attributes.getValue(NetworkMLConstants.INPUT_AMP_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);

             IClampInstanceProps iip = new IClampInstanceProps();

             iip.setDelay(currentPulseDelay);
             iip.setDuration(currentPulseDur);
             iip.setAmplitude(currentPulseAmp);

             currentSingleInput.setInstanceProps(iip);

             //currentSingleInput.

             logger.logComment("New instance props: "+ getCurrentElement()+" ("+currentPulseDelay+", "+currentPulseDur+", "+currentPulseAmp+")");
          }
         else if (getCurrentElement().equals(NetworkMLConstants.RANDOMSTIM_INSTANCE_ELEMENT) && getAncestorElement(1).equals(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT))
         {

             Float currentRate =
                     (float)UnitConverter.getRate(Float.parseFloat(attributes.getValue(NetworkMLConstants.RND_STIM_FREQ_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);

             RandomSpikeTrainInstanceProps rp = new RandomSpikeTrainInstanceProps();

             rp.setRate(currentRate);

             currentSingleInput.setInstanceProps(rp);

             //currentSingleInput.

             logger.logComment("New instance props: "+ getCurrentElement()+" ("+rp.details(false)+")");
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

    private boolean acceptCellTypeIncludes = false;
    private boolean acceptMakePlots = false;

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {

        logger.logComment("-----   End element: " + localName);

        if (insideCell)
        {
            //dealing with prefix
            if (elementStack.contains("segments") || elementStack.contains("cables"))
            {
                cellPrefix = MorphMLConstants.PREFIX + ":";
                if (getCurrentElement().equals("group"))
                {
                    cellPrefix = MetadataConstants.PREFIX + ":";
                }
            }
            if (elementStack.contains("biophysics"))
            {
                cellPrefix = BiophysicsConstants.PREFIX + ":";
            }
            if (elementStack.contains("connectivity"))
            {
                cellPrefix = NetworkMLConstants.PREFIX + ":";
            }
            if (getCurrentElement().equals("notes"))
            {
                cellPrefix = MetadataConstants.PREFIX + ":";
            }
            if (getCurrentElement().equals("cell") || getCurrentElement().equals("biophysics") || getCurrentElement().equals("connectivity"))
            {
                cellPrefix = "";
            }

            cellInfoBuffer.append("</" + cellPrefix + getCurrentElement() + ">\n");
            if (getCurrentElement().equals(MorphMLConstants.CELL_ELEMENT))
            {
                logger.logComment("FINISHED CELL STRING for " + cellName + ":\n" + cellInfoBuffer.toString());
                insideCell = false;
                SimpleXMLDocument doc = new SimpleXMLDocument();
                SimpleXMLElement rootElement = null;

                //make some checks before write to the file
                addCell = false;
                Object choice = "";

                if (!testMode && !replaceAll && !renameAll && !acceptCellTypeIncludes)
                {
                    logger.logComment("Asking the user if the cell type " + cellName + " has to be added...");
                    Object[] options =
                    {
                        "Yes", "Yes to all", "No"
                    };

                    JOptionPane option = new JOptionPane(
                        "Found a new Cell Type in NeuroML file: " + cellName + "\nWould you like to add this to the current project?",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);

                    JDialog dialog = option.createDialog(null, "Reading the Level 3 file...");

                    dialog.setVisible(true);

                    choice = option.getValue();
                    logger.logComment("User has chosen: " + choice);
                    if (choice.equals("Yes to all"))
                    {
                        acceptCellTypeIncludes = true;
                    }
                }

                  
                  
                if (choice.equals("Yes") || testMode || replaceAll || renameAll || acceptCellTypeIncludes)
                {
                    logger.logComment("User accepted the cell type. Checking if it is already defined...");
                    addCell = true;
                    ArrayList<String> existingCells = project.cellManager.getAllCellTypeNames();
                    if (existingCells.contains(cellName))
                    {
                        Object choice2 = "";
                        if (!testMode && !replaceAll && !renameAll)
                        {
                            logger.logComment("The cell type " + cellName + " already exists in the current project. Asking the user...");
                            Object[] options2 =
                            {
                                "Replace", "Replace all", "Rename", "Rename all"
                            };

                            JOptionPane option2 = new JOptionPane(
                                "The cell type: " + cellName + " is already defined in the current project. \n You can overwrite the old definition or rename the new cell type",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                options2,
                                options2[0]);

                            JDialog dialog2 = option2.createDialog(null, "Warning");
                            dialog2.setVisible(true);
                            choice2 = option2.getValue();
                        }

                        if (choice2.equals("Replace all"))
                        {
                            replaceAll = true;
                        }
                        if (choice2.equals("Rename all"))
                        {
                            renameAll = true;
                        }
                        if (choice2.equals("Replace") || replaceAll || testMode)
                        {
                            renamedCells.put(cellName, cellName);//to avoid a check each time that we have to use the cellName
                            logger.logComment("User decided to replace the old type");
                            project.cellManager.deleteCellType(project.cellManager.getCell(cellName));
                        }
                        if (choice2.equals("Rename") || renameAll)
                        {
                            int i = 0;
                            String newCellName = cellName + "_imported";
                            if (!renameAll)
                            {
                                newCellName = JOptionPane.showInputDialog("Please enter a new name for the cell type", newCellName);
                            }
                            while (existingCells.contains(newCellName))
                            {
                                i++;
                                newCellName = cellName + "_imported" + String.valueOf(i);
                                if (!renameAll)
                                {
                                    newCellName = JOptionPane.showInputDialog("The chosen name is already taken. Please enter a new name for the cell type", newCellName);
                                }
                            }
                            renamedCells.put(cellName, newCellName);
                            logger.logComment("cell " + cellName + " renamed " + renamedCells.get(cellName));
                            String cellString = cellInfoBuffer.toString();
                            cellInfoBuffer = new StringBuffer(cellString.replaceAll(cellName, renamedCells.get(cellName)));
                        }

                    }
                    else
                    {

                        logger.logComment("The cell type " + cellName + " doesn't already exist");

                    }
                }
                                  else if (choice.equals("No"))
                {
                    logger.logComment("User refused the cell type " + cellName);
                    addCell = false;
                }


                if (addCell)
                {

                    //initializing the root element
                    rootElement = new SimpleXMLElement(NeuroMLConstants.ROOT_ELEMENT);
                    rootElement.addNamespace(new SimpleXMLNamespace("", NeuroMLConstants.NAMESPACE_URI));
                    rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX, MetadataConstants.NAMESPACE_URI));
                    rootElement.addNamespace(new SimpleXMLNamespace(MorphMLConstants.PREFIX, MorphMLConstants.NAMESPACE_URI));
                    rootElement.addNamespace(new SimpleXMLNamespace(BiophysicsConstants.PREFIX, BiophysicsConstants.NAMESPACE_URI));
                    rootElement.addNamespace(new SimpleXMLNamespace(NetworkMLConstants.PREFIX, NetworkMLConstants.NAMESPACE_URI));
                    rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, MetadataConstants.LENGTH_UNITS_MICROMETER));
                    doc.addRootElement(rootElement);
                    rootElement.addContent("\n\n");
                    StringBuilder notes = new StringBuilder("\nNeuroML (level 3) description of a cell " + renamedCells.get(cellName) + " generated with project: "
                                                          + project.getProjectName() + " saved with neuroConstruct v"
                                                          + GeneralProperties.getVersionNumber() + " on: " + GeneralUtils.getCurrentTimeAsNiceString() + ", "
                                                          + GeneralUtils.getCurrentDateAsNiceString() + "\n\n");
                    rootElement.addChildElement(new SimpleXMLElement(MetadataConstants.PREFIX + ":" + MetadataConstants.NOTES_ELEMENT, "\n" + notes.toString()));
                    SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" + MorphMLConstants.PROPS_ELEMENT);
                    rootElement.addContent("\n\n");
                    rootElement.addChildElement(props);
                    rootElement.addContent("\n\n");
                    rootElement.addContent("<cells>");

                    //add the generated cellBuffer
                    rootElement.addContent(cellInfoBuffer.toString());
                    rootElement.addContent("</cells>");
                    doc.addRootElement(rootElement);

                    //reset the string for the next cell
                    cellInfoBuffer = new StringBuffer();


                    //write the NeuroML file
                    File neuroMLDir = ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory());
                    File morphMLFile = new File(neuroMLDir,
                                                renamedCells.get(cellName)
                                                + ProjectStructure.getMorphMLFileExtension());
                    String stringForm = doc.getXMLString("", false);
                    FileWriter fw;
                    try
                    {
                        fw = new FileWriter(morphMLFile);
                        fw.write(stringForm);
                        fw.close();
                    }
                    catch (IOException ex)
                    {
                        logger.logComment("Problem writing to file: " + morphMLFile);
                    }

//              pass the file to a MorphMLReader and generate the cell type in the project                                    
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    spf.setNamespaceAware(true);
                    XMLReader xmlReader;
                    MorphMLReader mmlBuilder = new MorphMLReader();
                    try
                    {
                        xmlReader = spf.newSAXParser().getXMLReader();
                        xmlReader.setContentHandler(mmlBuilder);
                        FileInputStream instream = new FileInputStream(morphMLFile);
                        InputSource is = new InputSource(instream);
                        xmlReader.parse(is);
                        Cell builtCell = mmlBuilder.getBuiltCell();

                        if (builtCell.getInstanceName().indexOf(" ")>=0)
                        {
                            String oldName = builtCell.getInstanceName();
                            String newName = GeneralUtils.replaceAllTokens(oldName, " ", "_");

                            GuiUtils.showInfoMessage(logger, "Renaming cell ("+oldName+") to ("+newName+")", "Renaming", null);

                            builtCell.setInstanceName(newName);
                        }

                        project.cellManager.addCellType(builtCell);
                        project.markProjectAsEdited();

                        logger.logComment(builtCell.getInstanceName() + " added to the project");
                    }
                    catch (Exception ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem loading MorphML file from: "+ morphMLFile.getAbsolutePath(), ex, null);
                    }

                }//if add the cell

            }//if the cell is finished

        }//if cell


        if (insideChannel)
        {
            //dealing with prefix
            ArrayList<String> meta = new ArrayList<String>();
            meta.add("comment");
            meta.add("issue");
            meta.add("contributor");
            meta.add("notes");
            meta.add("modelAuthor");
            meta.add("authorList");
            meta.add("modelTranslator");
            meta.add("publication");
            meta.add("neuronDBref");
            meta.add("modelDBref");
            meta.add("modelName");

            if (getCurrentElement().equals("comment") && getAncestorElement(1).equals("impl_prefs"))
            {
                chanPrefix = "";
            }
            else if (meta.contains(getCurrentElement()) || meta.contains(getAncestorElement(1)))
            {
                chanPrefix = MetadataConstants.PREFIX + ":";
            }
            else
            {
                chanPrefix = "";
            }

            chanBuffer = chanBuffer + ("</" + chanPrefix + getCurrentElement() + ">\n");

            /*if (getCurrentElement().equals(ChannelMLConstants.ION_ELEMENT))
            {
                SimpleXMLElement ionEl = new SimpleXMLElement(ChannelMLConstants.ION_ELEMENT);
                ionEl.addAttribute(ChannelMLConstants.LEGACY_ION_NAME_ATTR, );
            }
            else */if (getCurrentElement().equals(ChannelMLConstants.CHAN_TYPE_ELEMENT)
                     || getCurrentElement().equals(ChannelMLConstants.SYN_TYPE_ELEMENT)
                     || getCurrentElement().equals(ChannelMLConstants.ION_CONC_ELEMENT))
            {

                logger.logComment("FINISHED CHANNEL STRING for " + chanName + ":\n" + chanBuffer);
                insideChannel = false;
                SimpleXMLDocument doc = new SimpleXMLDocument();
                SimpleXMLElement rootElement = null;


                //make some checks befor write to the file
                ArrayList<String> existingChannels = project.cellMechanismInfo.getAllCellMechanismNames();
                if (existingChannels.contains(chanName))
                {
                    Object choice = "";
                    if (!replace && !testMode)
                    {
                        logger.logComment("Some mechanisms in the Level3 file have the same name of the existing mechanisms\n Warning the user...");
                        Object[] options =
                        {
                            "Yes", "No"
                        };
                        JOptionPane option = new JOptionPane(
                            "Some cell mechanisms in the Level3 file you are importing will overwrite the existing mechanisms\n"
                            + "Overwrite?\n"
                            + "\nNOTE: This will remove files in the project/cellMechanisms directory!",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[0]);

                        JDialog dialog = option.createDialog(null, "WARNING");
                        dialog.setVisible(true);
                        choice = option.getValue();
                        logger.logComment("User has chosen: " + choice);
                    }

                    if (choice.equals("No"))
                    {
                        logger.logComment("User aborted the program");
                        Runtime.getRuntime().halt(0);
                    }
                    else if (choice.equals("Yes") || testMode)
                    {
                        replace = true;
                        project.cellMechanismInfo.deleteCellMechanism(project.cellMechanismInfo.getCellMechanism(chanName));
                    }
                }

                //initializing the root element
                rootElement = new SimpleXMLElement(ChannelMLConstants.ROOT_ELEMENT);
                rootElement.addNamespace(new SimpleXMLNamespace("", ChannelMLConstants.NAMESPACE_URI));
                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX, NeuroMLConstants.XSI_URI));
                rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX, MetadataConstants.NAMESPACE_URI));
                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC, ChannelMLConstants.NAMESPACE_URI + " http://www.neuroml.org/NeuroMLValidator/NeuroMLFiles/Schemata/v1.8.1/Level2/ChannelML_v1.8.1.xsd"));
                rootElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.UNIT_SCHEME, ChannelMLConstants.PHYSIOLOGICAL_UNITS));
                doc.addRootElement(rootElement);
                rootElement.addContent("\n\n");
                StringBuilder notes = new StringBuilder("\nChannelML description of a channel " + chanName + " generated with project: "
                                                      + project.getProjectName() + " saved with neuroConstruct v"
                                                      + GeneralProperties.getVersionNumber() + " on: " + GeneralUtils.getCurrentTimeAsNiceString() + ", "
                                                      + GeneralUtils.getCurrentDateAsNiceString() + "\n\n");
                rootElement.addChildElement(new SimpleXMLElement(MetadataConstants.PREFIX + ":" + MetadataConstants.NOTES_ELEMENT, "\n" + notes.toString()));
                //                  SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" +  MorphMLConstants.PROPS_ELEMENT);
                rootElement.addContent("\n\n");

                if(getCurrentElement().equals(ChannelMLConstants.ION_CONC_ELEMENT))
                {
                    // cheeky way of checking ion name
                    String att = "ion_species name=\"";
                    int startName = chanBuffer.indexOf(att)+att.length();
                    int endName = chanBuffer.indexOf("\"", startName);
                    String ionName = chanBuffer.substring(startName, endName);
                    SimpleXMLElement ionInfo = ionElements.get(ionName);
                    rootElement.addChildElement(ionInfo);
                }
                rootElement.addContent("\n\n");

                //add the generated cellBuffer
                rootElement.addContent(chanBuffer);
                doc.addRootElement(rootElement);

                //reset the string for the next cell
                chanBuffer = "";

                //write the NeuroML file

                File CMLDir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory());
                File newDir = new File(CMLDir, chanName);
                newDir.mkdirs();

                File chanMLFile = new File(newDir,
                                           chanName
                                           + ".xml");
                String stringForm = doc.getXMLString("", false);
                FileWriter fw;
                try
                {
                    fw = new FileWriter(chanMLFile);
                    fw.write(stringForm);
                    fw.close();
                }
                catch (IOException ex)
                {
                    logger.logComment("Problem writing to file: " + chanMLFile);
                }

                File xslDir = GeneralProperties.getChannelMLSchemataDir();
                ChannelMLCellMechanism cmlMech = new ChannelMLCellMechanism();

                cmlMech.setXMLFile(chanMLFile.getName());

                cmlMech.setInstanceName(chanName);
                if (getCurrentElement().equals(ChannelMLConstants.CHAN_TYPE_ELEMENT))
                {
                    cmlMech.setMechanismType(ChannelMLConstants.CHAN_TYPE_ELEMENT);
                }
                if (getCurrentElement().equals(ChannelMLConstants.SYN_TYPE_ELEMENT))
                {
                    cmlMech.setMechanismType(ChannelMLConstants.SYN_TYPE_ELEMENT);
                }
                if (getCurrentElement().equals(ChannelMLConstants.ION_CONC_ELEMENT))
                {
                    cmlMech.setMechanismType(ChannelMLConstants.ION_CONC_ELEMENT);
                }
                cmlMech.setDescription(doc.getValueByXPath(ChannelMLConstants.NOTES_ELEMENT));
                cmlMech.setMechanismModel("ChannelML mechanism imported from a network");

                project.cellMechanismInfo.addCellMechanism(cmlMech);


                File newXslNeuron = new File(xslDir, "ChannelML_v" + GeneralProperties.getNeuroMLVersionNumber() + "_NEURONmod.xsl");
                File newXslGenesis = new File(xslDir, "ChannelML_v" + GeneralProperties.getNeuroMLVersionNumber() + "_GENESIStab.xsl");
                File newXslPSICS = new File(xslDir, "ChannelML_v" + GeneralProperties.getNeuroMLVersionNumber() + "_PSICS.xsl");

                File[] newXsl =
                {
                    newXslNeuron, newXslGenesis, newXslPSICS
                };
                String[] simEnv =
                {
                    "NEURON", "GENESIS", "PSICS"
                };

                for (int i = 0; i < newXsl.length; i++)
                {
                    try
                    {
                        GeneralUtils.copyFileIntoDir(newXsl[i], newDir);
                        SimulatorMapping map = new SimulatorMapping(newXsl[i].getName(), simEnv[i], true);

                        cmlMech.addSimMapping(map);

                        try
                        {
                            cmlMech.reset(project, false);
                            logger.logComment("New cml mech: " + cmlMech.getInstanceName());
                        }
                        catch (Exception ex)
                        {
                            GuiUtils.showErrorMessage(logger, "Problem updating mechanism to support mapping to simulator: " + simEnv[i], ex, null);
                        }
                        project.markProjectAsEdited();

                    }
                    catch (IOException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem adding the mapping for " + chanName, ex, null);
                    }
                }



            }//if the channel is finished
        }//if channel


        
        if (insideAnnotation && addAnnotations)
        {     
             if (!getCurrentElement().equals(MetadataConstants.ANNOTATION_ELEMENT))
             {
                 annotationString = annotationString + "</" + getCurrentElement() + ">\n";
             }
             else
             {
                insideAnnotation = false;
                logger.logComment("FINISHED ANNOTATION STRING: \n"+annotationString);
                XMLDecoder xmlDecoder = null;
                ByteArrayInputStream baos = null;               
                try
                { 
                    baos = new ByteArrayInputStream(annotationString.getBytes());
                    baos.close();
                } catch (IOException ex) {
                    logger.logError("Problem with annotation ByteArrayInputStream", ex);
                }
                
                xmlDecoder = new XMLDecoder(baos);
                Object nextReadObject = null;
                nextReadObject = xmlDecoder.readObject();
                
                while (  nextReadObject != null)
                {

                     /* --  Reading Basic Info -- */
                    if  (nextReadObject instanceof BasicProjectInfo)
                    {
                        logger.logComment("Found BasicProjectInfo object in level3 file annotation...");
                        BasicProjectInfo bpi = (BasicProjectInfo) nextReadObject;

                        project.setProjectDescription(bpi.getProjectDescription());
                        project.setProjectFileVersion(bpi.getProjectFileVersion());
                    }
                     /* --  Reading Regions Info -- */
                    if  (nextReadObject instanceof RegionsInfo)
                    {
                        logger.logComment("Found RegionsInfo object in level3 file annotation...");
                        project.regionsInfo = (RegionsInfo) nextReadObject;
                        project.regionsInfo.addTableModelListener(project);
                    }

                    /* --  Reading Cell Group Info -- */
                    if (nextReadObject instanceof CellGroupsInfo)
                    {
                        logger.logComment("Found CellGroupsInfo object in  level3  file annotation...");
                         project.cellGroupsInfo = (CellGroupsInfo) nextReadObject;
                         project.cellGroupsInfo.addTableModelListener(project);
                    }

                    /* --  Reading ElecInputInfo --*/
                    if (nextReadObject instanceof ElecInputInfo)
                    {
                        logger.logComment("Found ElecInputInfo object in  level3  file annotation...");
                         project.elecInputInfo = (ElecInputInfo) nextReadObject;
                         project.elecInputInfo.addTableModelListener(project);
                    }

                    /* --  Reading Simple Net Conn Info -- */
                    if (nextReadObject instanceof SimpleNetworkConnectionsInfo)
                    {
                        logger.logComment("Found SimpleNetworkConnectionsInfo object in  level3  file annotation...");
                         project.morphNetworkConnectionsInfo = (SimpleNetworkConnectionsInfo) nextReadObject;
                         project.morphNetworkConnectionsInfo.addTableModelListener(project);
                    }

                    /* --  Reading Simulation Info --*/
                    if (nextReadObject instanceof SimulationParameters)
                    {
                        logger.logComment("Found SimulationParameters object in  level3  file annotation...");
                         project.simulationParameters = (SimulationParameters) nextReadObject;
                    }

                    /* --  Reading Sim Plot Info -- */
                    if (nextReadObject instanceof SimPlotInfo)
                    {
                        logger.logComment("Found SimPlotInfo object in  level3  file annotation...");
                         project.simPlotInfo = (SimPlotInfo) nextReadObject;
                         project.simPlotInfo.addTableModelListener(project);
                    }

                    /* --  Reading SimConfigInfo --*/
                    if (nextReadObject instanceof SimConfigInfo)
                    {
                        logger.logComment("Found SimConfigInfo object in  level3  file annotation...");
                         project.simConfigInfo = (SimConfigInfo) nextReadObject;
                    }     
                    
                    /* --  Reading NeuronSettings --*/
                    if (nextReadObject instanceof NeuronSettings)
                    {
                        logger.logComment("Found SimConfigInfo object in  level3  file annotation...");
                         project.neuronSettings = (NeuronSettings) nextReadObject;
                    }   
                    
                    /* --  Reading GenesisSettings --*/
                    if (nextReadObject instanceof GenesisSettings)
                    {
                        logger.logComment("Found GenesisSettings object in  level3  file annotation...");
                         project.genesisSettings = (GenesisSettings) nextReadObject;
                    }    
                    
                    try
                    {
                        nextReadObject = xmlDecoder.readObject();
                    }
                    catch (Exception ex)
                    {
                        nextReadObject = null;
                        logger.logComment("No more objects to read...");
                    }

                }
                xmlDecoder.close();
                baos.reset();
                    
            }//if the annotation is finished
        }//if annotation
        
        
        if (getCurrentElement().equals(NetworkMLConstants.POPULATION_ELEMENT))
        {
            Vector<SimPlot> simPlots = project.simPlotInfo.getAllSimPlots();

            boolean hasPlot = false;
            for (SimPlot sp: simPlots)
            {
                if (sp.getCellGroup().equals(currentPopulation))
                    hasPlot = true;
            }
            if (!hasPlot)
            {
                boolean addPlot = acceptMakePlots || testMode;

                if (!addPlot)
                {
                    Object[] options = {"Yes", "Yes to all", "No"};

                    JOptionPane option = new JOptionPane(
                        "Cell Group: "+currentPopulation+" from NeuroML file has no entry for plotting/saving. " +
                        "Would you like to add an entry to plot & save all membrane potentials at somas (segment id =0)?",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);

                    JDialog dialog = option.createDialog(null, "Add plot/save entry?");

                    dialog.setVisible(true);

                    if (option.getValue().equals("Yes to all"))
                    {
                        acceptMakePlots = true;
                    }
                    addPlot = !option.getValue().equals("No");
                }

                if (addPlot)
                {
                    SimPlot sp = new SimPlot(currentPopulation+"_v",
                                             currentPopulation+"_g",
                                             currentPopulation,
                                             "*",
                                             "0",
                                             SimPlot.VOLTAGE,
                                             -90,
                                             50,
                                             SimPlot.PLOT_AND_SAVE);

                    project.simPlotInfo.addSimPlot(sp);
                    simConfigToUse.addPlot(sp.getPlotReference());
                }
            }

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
        
        else if (!testMode && level3 && getCurrentElement().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            File savedNetsDir = ProjectStructure.getSavedNetworksDir(project.getProjectMainDirectory());

            String timeInfo = GeneralUtils.getCurrentDateAsNiceString() +"_"+GeneralUtils.getCurrentTimeAsNiceString();
            timeInfo = GeneralUtils.replaceAllTokens(timeInfo, ":", "-");

            String fullName = "Net_" +timeInfo+ ProjectStructure.getNeuroMLFileExtension();

            File networkFile = new File(savedNetsDir, fullName);

            if (networkFile.exists())
            {

            }
            try
            {
                File fileSaved = ProjectManager.saveNetworkStructureXML(project, networkFile, false, false, simConfigToUse.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            }
            catch (NeuroMLException ex)
            {
                logger.logError("Error: " + ex.getMessage(), ex);
            }


            logger.logComment("Reached the end of the file. Warning the user about the project state...");
            Object[] options = {"OK"};
            String message = "All of the elements in the file have been correctly imported.\n" +
                "NOTE: the network structure present in the NeuroML file has been loaded into memory (Latest Generated Positions)\n" +
                "and a copy of the network structure has been stored " +
                "in the savedNetworks folder of the current project.";
            
            if (!addAnnotations)
            {
                message = message+"\nYou can run a simulation from the Export tab but no variables will be plotted/saved, as this information was not present in the NeuroML file." +
                                                "\n\nNOTE: some default regions, Cell Groups and inputs have been created but these may not be equal to the ones used to create the imported file\n";
            }
                
            JOptionPane fileEnd = new JOptionPane(
                                          message,
                                          JOptionPane.DEFAULT_OPTION,
                                          JOptionPane.INFORMATION_MESSAGE,
                                          null,
                                          options,
                                          options[0]);

                                  JDialog dialog = fileEnd.createDialog(null, "Imported NeuroML");
                                  dialog.setVisible(true);
        }

        stepDownElement();
    }



    public static void main(String args[])
    {

        try
        {
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
//            Project testProj = Project.loadProject(new File("examples/Ex4-NEURONGENESIS/Ex4-NEURONGENESIS.neuro.xml"),null);
            //Project testProj = Project.loadProject(new File("nCexamples/Ex5_Networks/Ex5_Networks.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml"),null);

//            File f = new File("examples/Ex4-NEURONGENESIS/savedNetworks/nnn.nml");
            //File f = new File("nCexamples/Ex5_Networks/generatedNeuroML/Generated.net.xml");
            File f = new File("testProjects/TestNetworkML/savedNetworks/test.xml");

            logger.logComment("Loading netml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);
            
            ProjectManager pm = new ProjectManager(null, null);
   
            pm.setCurrentProject(testProj);
            
//            pm.doLoadNetworkMLAndGenerate(f);
            
            pm.doLoadNetworkML(f, false);
            
            
            while (pm.isGenerating())
            {
                Thread.sleep(2);
                System.out.println("Waiting...");
            }
            
            
            System.out.println(testProj.generatedElecInputs.details());
            

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
