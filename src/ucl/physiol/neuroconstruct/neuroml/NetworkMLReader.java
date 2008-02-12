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
import java.util.ArrayList;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * NetworkML file Reader. Importer of NetworkML files to neuroConstruct format using SAX
 *
 * @author Padraig Gleeson
 *  
 */

public class NetworkMLReader extends XMLFilterImpl
{
    private static ClassLogger logger = new ClassLogger("NetworkMLReader");


    //private String importationComment = "Importation comment: ";

    private Stack<String> elementStack = new Stack<String>();

    private String unitsUsed = null;

    private String currentPopulation = null;
    //private String currentCellType = null;
    private int currentInstanceId = -1;
    private int currentNodeId = -1;


    private String currentProjection = null;
    //private String currentSource = null;
    //private String currentTarget = null;


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

    private ArrayList<ConnSpecificProps> globConnProps = new ArrayList<ConnSpecificProps>();
    private ArrayList<ConnSpecificProps> localConnProps = new ArrayList<ConnSpecificProps>();
    
    private float globAPDelay = 0;
    private float localAPDelay = 0;

    //private String metadataPrefix = MetadataConstants.PREFIX + ":";

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;

    public NetworkMLReader(GeneratedCellPositions cellPos, GeneratedNetworkConnections netConns)
    {
        this.cellPos = cellPos;
        this.netConns = netConns;

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
                 posRec.nodeId = currentNodeId;
             }
             
             this.cellPos.addPosition(currentPopulation, posRec);
             

         }
         else if (getCurrentElement().equals(NetworkMLConstants.PROJECTIONS_ELEMENT))
         {
             this.unitsUsed = attributes.getValue(NetworkMLConstants.UNITS_ATTR);
             logger.logComment("unitsUsed: "+unitsUsed);
         }
         else if (getCurrentElement().equals(NetworkMLConstants.PROJECTION_ELEMENT))
         {
             this.currentProjection = attributes.getValue(NetworkMLConstants.PROJ_NAME_ATTR);
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
                 connProps.internalDelay = Float.parseFloat(attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR)!=null)
             {
                 if (connProps==null) 
                     connProps = new ConnSpecificProps(currentSynType);
                 
                 connProps.internalDelay = connProps.internalDelay + Float.parseFloat(attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR)!=null)
             {
                 if (connProps==null) 
                     connProps = new ConnSpecificProps(currentSynType);
                 
                 connProps.internalDelay = connProps.internalDelay + Float.parseFloat(attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)!=null)
             {                 
                 this.globAPDelay = Float.parseFloat(attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR));
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
                 localProps.internalDelay = Float.parseFloat(attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.internalDelay = localProps.internalDelay + Float.parseFloat(attributes.getValue(NetworkMLConstants.PRE_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR) != null)
             {
                 if (localProps==null) localProps = getGlobalSynProps(synType);
                 localProps.internalDelay = localProps.internalDelay + Float.parseFloat(attributes.getValue(NetworkMLConstants.POST_DELAY_ATTR));
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
                 this.localAPDelay = Float.parseFloat(attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR));
             }
         }

    }
    
    ConnSpecificProps getGlobalSynProps(String synType)
    {
        for(ConnSpecificProps props: globConnProps)
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
            
            this.netConns.addSynapticConnection(this.currentProjection,
                    GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                currentSourceCellNumber,
                                                currentSourceCellSegmentIndex,
                                                currentSourceCellDisplacement,
                                                currentTargetCellNumber,
                                                currentTargetCellSegmentIndex,
                                                currentTargetCellDisplacement,
                                                propDelay,
                                                connProps);

            this.localConnProps = new ArrayList<ConnSpecificProps>();
            localAPDelay = 0;


        }
        




        stepDownElement();
    }



    public static void main(String args[])
    {

        try
        {
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("examples\\Ex4-NEURONGENESIS\\Ex4-NEURONGENESIS.neuro.xml"),null);

            File f = new File("examples\\Ex4-NEURONGENESIS\\savedNetworks\\nnn.nml");

            logger.logComment("Loading netml cell from "+ f.getAbsolutePath(), true);

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
            logger.logComment("Net conns: "+gnc.toNiceString());




        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
