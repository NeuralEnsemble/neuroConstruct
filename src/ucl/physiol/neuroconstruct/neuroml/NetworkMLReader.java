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

package ucl.physiol.neuroconstruct.neuroml;

import java.io.*;
import java.util.*;
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
    private String currentCellType = null;
    private int currentInstanceId = -1;


    private String currentProjection = null;
    private String currentSource = null;
    private String currentTarget = null;


    private String currentSynType = null;

    private int currentSourceCellNumber = -1;
    private int currentSourceCellSegmentIndex = -1;
    private float currentSourceCellDisplacement = -1;

    private int currentTargetCellNumber = -1;
    private int currentTargetCellSegmentIndex = -1;
    private float currentTargetCellDisplacement = -1;

    private float currentAPSpeed = -1;


    private String currentPropertyName = null;

    private long foundRandomSeed = Long.MIN_VALUE;
    private String foundSimConfig = null;

    private ArrayList<ConnSpecificProps> props = null;

    //private String metadataPrefix = MetadataConstants.PREFIX + ":";

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;

    public NetworkMLReader(GeneratedCellPositions cellPos, GeneratedNetworkConnections netConns)
    {
        this.cellPos = cellPos;
        this.netConns = netConns;

    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String contents = new String(ch, start, length);


        if (contents.trim().length() > 0)
        {
            logger.logComment("Got a string: (" + contents + ") at: "+ elementStack);

            if (getCurrentElement().equals(NetworkMLConstants.CELLTYPE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.POPULATION_ELEMENT))
            {
                currentCellType = contents;
                logger.logComment(">> currentCellType: "+currentCellType+", currentPopulation: "+currentPopulation);
            }
            else if (getCurrentElement().equals(NetworkMLConstants.SOURCE_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.PROJECTION_ELEMENT))
            {
                currentSource = contents;
                logger.logComment("currentSource: "+currentSource);
            }
            else if (getCurrentElement().equals(NetworkMLConstants.TARGET_ELEMENT)
                && getAncestorElement(1).equals(NetworkMLConstants.PROJECTION_ELEMENT))
            {
                currentTarget = contents;
                logger.logComment("currentTarget: "+currentTarget);
            }
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




    public void startDocument()
    {
        logger.logComment("startDocument...");

        //cell = new Cell();
    }

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


    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes)
    throws SAXException
    {
        logger.logComment("\n          -----   Start element: namespaceURI: " + namespaceURI
                           + ", localName: " + localName
                           + ", qName: " + qName);


         int attrsLength = attributes.getLength();
         for (int i = 0; i < attrsLength; i++)
         {
             String name = attributes.getLocalName(i);
             String val = attributes.getValue(i);

             logger.logComment("Attr name: " + name+ ", val: " + val+ ", qname: "
                               + attributes.getQName(i)+ ", uri: " + attributes.getURI(i));

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

         }


         else if (getCurrentElement().equals(NetworkMLConstants.LOCATION_ELEMENT))
         {
             String x = attributes.getValue(NetworkMLConstants.LOC_X_ATTR);
             String y = attributes.getValue(NetworkMLConstants.LOC_Y_ATTR);
             String z = attributes.getValue(NetworkMLConstants.LOC_Z_ATTR);
             logger.logComment(">>  Found a location");

             //currentInstanceId = Integer.parseInt(id);
             this.cellPos.addPosition(currentPopulation,
                                      currentInstanceId,
                                      Float.parseFloat(x),
                                      Float.parseFloat(y),
                                      Float.parseFloat(z));

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
                 currentTargetCellDisplacement = Float.parseFloat(attributes.getValue(NetworkMLConstants.
                     FRACT_ALONG_ATTR));
             }

         }

         else if (getCurrentElement().equals(NetworkMLConstants.CONN_PROP_ELEMENT)
             && getAncestorElement(1).equals(NetworkMLConstants.CONNECTION_ELEMENT))
         {
             String synType = currentSynType;
             String inclSynType = attributes.getValue(NetworkMLConstants.SYN_TYPE_ELEMENT);

             if (inclSynType != null && inclSynType.length() > 0)
                 synType = inclSynType;

             ConnSpecificProps props = new ConnSpecificProps(synType);
             if (attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR) != null)
             {
                 props.internalDelay = Float.parseFloat(attributes.getValue(NetworkMLConstants.INTERNAL_DELAY_ATTR));
             }
             if (attributes.getValue(NetworkMLConstants.WEIGHT_ATTR) != null)
             {
                 props.weight = Float.parseFloat(attributes.getValue(NetworkMLConstants.WEIGHT_ATTR));
             }

             if (this.props == null)this.props = new ArrayList<ConnSpecificProps> ();
             this.props.add(props);

             if (attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR)!=null)
             {
                 this.currentAPSpeed = Float.parseFloat(attributes.getValue(NetworkMLConstants.PROP_DELAY_ATTR));
             }
         }




    }


    public void endElement(String namespaceURI, String localName, String qName)
    {

        logger.logComment("-----   End element: " + localName);

        if (getCurrentElement().equals(NetworkMLConstants.POPULATION_ELEMENT))
        {
            currentPopulation = null;
            this.currentCellType = null;
        }
        else if (getCurrentElement().equals(NetworkMLConstants.INSTANCE_ELEMENT))
        {
            this.currentInstanceId = -1;
        }
        else if (getCurrentElement().equals(NetworkMLConstants.CONNECTION_ELEMENT))
        {

            this.netConns.addSynapticConnection(this.currentProjection,
                    GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                currentSourceCellNumber,
                                                currentSourceCellSegmentIndex,
                                                currentSourceCellDisplacement,
                                                currentTargetCellNumber,
                                                currentTargetCellSegmentIndex,
                                                currentTargetCellDisplacement,
                                                currentAPSpeed,
                                                props);

            props = null;


        }




        stepDownElement();
    }



    public static void main(String args[])
    {

        try
        {
            Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),
                                                   null);

            File f = new File("projects/Parall/savedNetworks/Net_21-Dec-06_18-02-49.nml");

            logger.logComment("Loading netml cell from "+ f.getAbsolutePath());

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
            logger.logComment("Net conns: "+gnc.toString());




        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
