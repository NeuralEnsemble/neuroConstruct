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

package ucl.physiol.neuroconstruct.project;


import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.cell.*;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import javax.vecmath.*;

/**
 * Storage for network connection info generated when "Generate cell positions
 * and network connections" button pressed. Note: this contains connections for both
 * Morph based and volume based connections
 *
 * @author Padraig Gleeson
 *  
 */

public class GeneratedNetworkConnections
{
    ClassLogger logger = new ClassLogger("GeneratedNetworkConnections");

    Hashtable<String, ArrayList<SingleSynapticConnection>> mySynapticConnectionVectors
        = new Hashtable<String, ArrayList<SingleSynapticConnection>>();

    public static final int MORPH_NETWORK_CONNECTION = 0;
    public static final int COMPLEX_NETWORK_CONNECTION = 1;

    public static final int VOL_NETWORK_CONNECTION = 2;
    public static final int ANY_NETWORK_CONNECTION = -1;

    private Project project = null;


    public GeneratedNetworkConnections(Project project)
    {
        this.project = project;


    }

    public void reset()
    {

        logger.logComment("--------------------------------    Resetting: "+this.hashCode());
        this.mySynapticConnectionVectors.clear();
    }


    /**
     * todo Add more funcs for python
     */
    public void addSynapticConnection(String netConnectionName,
                                      int sourceCellNumber,
                                      int targetCellNumber)
    {
        addSynapticConnection(netConnectionName,
                              MORPH_NETWORK_CONNECTION,
                              sourceCellNumber,
                              0,
                              0.5f,
                              targetCellNumber,
                              0,
                              0.5f,
                              0,
                              null);

    }



    public void addSynapticConnection(String netConnectionName,
                                      int connectionType,
                                      int sourceCellNumber,
                                      int sourceCellSegmentIndex,
                                      float sourceCellDisplacement,
                                      int targetCellNumber,
                                      int targetCellSgmentIndex,
                                      float targetCellDisplacement,
                                      float apPropDelay,
                                      ArrayList<ConnSpecificProps> props)
    {
        if (!mySynapticConnectionVectors.containsKey(netConnectionName))
        {
            ArrayList<SingleSynapticConnection> newSynapticConnections = new ArrayList<SingleSynapticConnection>();
            mySynapticConnectionVectors.put(netConnectionName,
                                            newSynapticConnections);
        }
        ArrayList<SingleSynapticConnection> cellGroupSyns = mySynapticConnectionVectors.get(netConnectionName);

        SingleSynapticConnection conn
            = new SingleSynapticConnection(connectionType,
                                           sourceCellNumber,
                                           sourceCellSegmentIndex,
                                           sourceCellDisplacement,
                                           targetCellNumber,
                                           targetCellSgmentIndex,
                                           targetCellDisplacement,
                                           apPropDelay,
                                           props);


        cellGroupSyns.add(conn);
        logger.logComment("Adding new net conn: "+netConnectionName+", type: "+connectionType);
        logger.logComment("From src " + sourceCellNumber +" to "+targetCellNumber+", props: "+props);
        //logger.logComment("Current num syn conns: "+ getNumberSynapticConnections());
    }


    public ArrayList<SingleSynapticConnection> getSynapticConnections(String netConnectionName)
    {
        logger.logComment("SynapticConnections sought for: #" + this.hashCode() + " out of my " +
                          getNumberSynapticConnections(ANY_NETWORK_CONNECTION)
                          + " net conns from " + mySynapticConnectionVectors.keySet());

        //System.out.println("Details: " + this.toString());

        if (!mySynapticConnectionVectors.containsKey(netConnectionName))
        {
            logger.logComment("No SingleSynapticConnections yet...");
            return new ArrayList<SingleSynapticConnection>();
        }

        ArrayList<SingleSynapticConnection> synapticConnectionVector
            = mySynapticConnectionVectors.get(netConnectionName);

        logger.logComment(synapticConnectionVector.size()+ " SingleSynapticConnections so far...");

        return synapticConnectionVector;
    }


    public Iterator<String> getNamesNetConnsIter()
    {
        return mySynapticConnectionVectors.keySet().iterator();
    }


    public int getNumberSynapticConnections(int connType)
    {
        //logger.logComment("getNumberSynapticConnections sought for : "+this.hashCode());
        int totalCount = 0;

        Enumeration keys = mySynapticConnectionVectors.keys();

        while(keys.hasMoreElements())
        {
            ArrayList<SingleSynapticConnection> synConns = (ArrayList<SingleSynapticConnection>)mySynapticConnectionVectors.get((String)keys.nextElement());
            if (connType == ANY_NETWORK_CONNECTION ||
                (connType == MORPH_NETWORK_CONNECTION && (synConns.get(0).connectionType == connType)) ||
                 (connType == VOL_NETWORK_CONNECTION && (synConns.get(0).connectionType == connType)))
            {
                totalCount = totalCount + synConns.size();
            }
        }

        return totalCount;
    }


    /**
     * Gets the indices of all target cells already connected to this source cell
     * @param netConnectionName name of the net conn in question
     * @param sourceCellIndex index of the cell which starts the connection
     * @param uniqueValues if true only gives single instance of target cell index,
     * even if there are two connections between the source cell and target
     */
    public ArrayList<Integer> getTargetCellIndices(String netConnectionName, int sourceCellIndex, boolean uniqueValues)
    {
        ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnectionName);

        /** @todo Do this quicker with collections... */
        ArrayList<Integer> allIndices = new ArrayList<Integer>();
        for (int i = 0; i < synConns.size(); i++)
        {
            SingleSynapticConnection conn = synConns.get(i);

            if (conn.sourceEndPoint.cellNumber==sourceCellIndex)
            {
                if (uniqueValues)
                {
                    if (!allIndices.contains(new Integer(conn.targetEndPoint.cellNumber)))
                        allIndices.add(new Integer(conn.targetEndPoint.cellNumber));
                }
                else
                {
                    allIndices.add(new Integer(conn.targetEndPoint.cellNumber));
                }
            }
        }
        return allIndices;
    }

    public ArrayList<SingleSynapticConnection> getConnsFromSource(String netConnectionName,
                                                                  int sourceCellIndex)
    {
        ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnectionName);

        /** @todo Do this quicker with collections... */
        ArrayList<SingleSynapticConnection> allConns = new ArrayList<SingleSynapticConnection> ();
        for (int i = 0; i < synConns.size(); i++)
        {
            SingleSynapticConnection conn = synConns.get(i);

            if (conn.sourceEndPoint.cellNumber == sourceCellIndex)
            {
                    allConns.add(conn);

            }
        }
        return allConns;
    }


    public ArrayList<SingleSynapticConnection> getConnsToTarget(String netConnectionName,
                                                                  int targetCellIndex)
    {
        ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnectionName);

        /** @todo Do this quicker with collections... */
        ArrayList<SingleSynapticConnection> allConns = new ArrayList<SingleSynapticConnection> ();
        for (int i = 0; i < synConns.size(); i++)
        {
            SingleSynapticConnection conn = synConns.get(i);

            if (conn.targetEndPoint.cellNumber == targetCellIndex)
            {
                    allConns.add(conn);

            }
        }
        return allConns;
    }




    /**
     * Gets the indices of all source cells already connected to this target cell
     * @param netConnectionName name of the net conn in question
     * @param targetCellIndex index of the cell which terminates the connection
     * @param uniqueValues if true only gives single instance of target cell index,
     * even if there are two connections between the source cell and target
     */
    public ArrayList<Integer> getSourceCellIndices(String netConnectionName, int targetCellIndex, boolean uniqueValues)
    {
        ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnectionName);

        /** @todo Do this quicker with collections... */
        ArrayList<Integer> allIndices = new ArrayList<Integer>();
        for (int i = 0; i < synConns.size(); i++)
        {
            SingleSynapticConnection conn = synConns.get(i);

            if (conn.targetEndPoint.cellNumber==targetCellIndex)
            {
                if (uniqueValues)
                {
                    if (!allIndices.contains(new Integer(conn.sourceEndPoint.cellNumber)))
                        allIndices.add(new Integer(conn.sourceEndPoint.cellNumber));
                }
                else
                {
                    allIndices.add(new Integer(conn.sourceEndPoint.cellNumber));
                }

            }
        }
        return allIndices;
    }



    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("GeneratedNetworkConnections with " 
                + getNumberSynapticConnections(ANY_NETWORK_CONNECTION) +
                  " positions in total\n");

        Enumeration keys = mySynapticConnectionVectors.keys();

        while (keys.hasMoreElements())
        {
            String netConnName = (String) keys.nextElement();
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get(netConnName);
            for (int i = 0; i < synConns.size(); i++)
            {
                sb.append(synConns.get(i)+"\n");
            }
        }
        return sb.toString();
    }

    public void saveToFile(File netConnFile) throws java.io.IOException
    {
        logger.logComment("Saving "
                          + getNumberSynapticConnections(ANY_NETWORK_CONNECTION)
                          + " connection records to file: "
                          + netConnFile.getAbsolutePath()+ ": "+ this.hashCode());

        FileWriter fw = new FileWriter(netConnFile);

        Enumeration keys = mySynapticConnectionVectors.keys();

        while (keys.hasMoreElements())
        {
            String netConnName = (String) keys.nextElement();
            ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnName);

            fw.write(netConnName + ":\n");

            for (int i = 0; i < synConns.size(); i++)
            {
                SingleSynapticConnection synConn = synConns.get(i);
                fw.write(synConn + "\n");

            }

        }
        logger.logComment("Finished saving data to file: " + netConnFile.getAbsolutePath());
        fw.flush();
        fw.close();
    }

    public String getHtmlReport(int connType, SimConfig simConfig)
    {
        StringBuffer generationReport = new StringBuffer();
        ArrayList<String> netConns = new ArrayList<String>();

        if (connType == MORPH_NETWORK_CONNECTION || connType == ANY_NETWORK_CONNECTION)
            netConns.addAll(project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames());

        if (connType == VOL_NETWORK_CONNECTION || connType == ANY_NETWORK_CONNECTION)
            netConns.addAll(project.volBasedConnsInfo.getAllAAConnNames());


        if (netConns.size() == 0)
        {
            generationReport.append("No Network Connections generated.<br><br>");

        }
        for (String netConnName: netConns)
        {
            StringBuffer synNames = new StringBuffer("[");

            Vector<SynapticProperties> syns = null;
            String src = null;
            String tgt = null;

            ArrayList<SingleSynapticConnection> conns 
                = project.generatedNetworkConnections.getSynapticConnections(netConnName);

            if (! (conns.size() == 0))
            {
                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                {
                    syns = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                    src = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                    tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                }
                else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
                {
                    syns = project.volBasedConnsInfo.getSynapseList(netConnName);
                    src = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                    tgt = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                }
                
                

                int srcCellNum = project.generatedCellPositions.getNumberInCellGroup(src);
                int tgtCellNum = project.generatedCellPositions.getNumberInCellGroup(tgt);

                int[] numInEachSrcCell = new int[srcCellNum];
                int[] numInEachTgtCell = new int[tgtCellNum];

                for (SingleSynapticConnection conn : conns)
                {
                    numInEachSrcCell[conn.sourceEndPoint.cellNumber]++;
                    numInEachTgtCell[conn.targetEndPoint.cellNumber]++;
                }

                float srcAvg = 0;
                float srcStd = 0;
                float tgtAvg = 0;
                float tgtStd = 0;
                int srcMin = Integer.MAX_VALUE;
                int srcMax = 0;
                int tgtMin = Integer.MAX_VALUE;
                int tgtMax = 0;

                for (int i = 0; i < srcCellNum; i++)
                {
                    srcAvg += numInEachSrcCell[i];
                    if (numInEachSrcCell[i] < srcMin) srcMin = numInEachSrcCell[i];
                    if (numInEachSrcCell[i] > srcMax) srcMax = numInEachSrcCell[i];
                }
                srcAvg = srcAvg / (float) srcCellNum;

                for (int i = 0; i < srcCellNum; i++)
                {
                    srcStd += (numInEachSrcCell[i] - srcAvg) * (numInEachSrcCell[i] - srcAvg);
                }
                srcStd = (float) Math.sqrt(srcStd / (float) srcCellNum);

                for (int i = 0; i < tgtCellNum; i++)
                {
                    tgtAvg += numInEachTgtCell[i];
                    if (numInEachTgtCell[i] < tgtMin) tgtMin = numInEachTgtCell[i];
                    if (numInEachTgtCell[i] > tgtMax) tgtMax = numInEachTgtCell[i];

                }
                tgtAvg = tgtAvg / (float) tgtCellNum;

                for (int i = 0; i < numInEachTgtCell.length; i++)
                {
                    tgtStd += (numInEachTgtCell[i] - tgtAvg) * (numInEachTgtCell[i] - tgtAvg);
                }
                tgtStd = (float) Math.sqrt(tgtStd / (float) tgtCellNum);

                String srcStdString = "";
                if (srcStd > 0)
                {
                    srcStdString = " +/- " + srcStd + " (" + srcMin + " to " + srcMax + ")";
                }
                String tgtStdString = "";
                if (tgtStd > 0)
                {
                    tgtStdString = " +/- " + tgtStd + " (" + tgtMin + " to " + tgtMax + ")";
                }
                
                
                StringBuffer synReport = new StringBuffer();

                for (int k = 0; k < syns.size(); k++)
                {
                    String synType = syns.get(k).getSynapseType();
                    
                    float weight = syns.get(k).getWeightsGenerator().getNominalNumber();
                    
                    CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(synType);
                    
                    if (cellMech instanceof AbstractedCellMechanism)
                    {
                        //todo: add info on file based syns...
                    }
                    else if(cellMech instanceof ChannelMLCellMechanism)
                    {
                        ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)cellMech;
                        try
                        {
                            String units = cmlMech.getXMLDoc().getValueByXPath(ChannelMLConstants.getUnitsXPath());

                            SimpleXMLEntity[] synTypes = cmlMech.getXMLDoc().getXMLEntities(ChannelMLConstants.getSynapseTypeXPath());

                            SimpleXMLElement firstSyn = (SimpleXMLElement)synTypes[0];

                            SimpleXMLEntity[] doubExpSyns = firstSyn.getXMLEntities(ChannelMLConstants.DOUB_EXP_SYN_ELEMENT);
                            SimpleXMLEntity[] blockingSyns = firstSyn.getXMLEntities(ChannelMLConstants.BLOCKING_SYN_ELEMENT);

                            ArrayList<SimpleXMLEntity> all = new ArrayList<SimpleXMLEntity>();
                            for (SimpleXMLEntity s: doubExpSyns) all.add(s);
                            for (SimpleXMLEntity s: blockingSyns) all.add(s);
                            
                            //all.addAll(doubExpSyns);
                            
                            for(SimpleXMLEntity next: all)
                            {
                                SimpleXMLElement des = (SimpleXMLElement)next;
                                float maxCond = Float.parseFloat(des.getAttributeValue(ChannelMLConstants.DES_MAX_COND_ATTR));
                                
                                maxCond = (float)UnitConverter.getConductance(maxCond, 
                                        UnitConverter.getUnitSystemIndex(units), 
                                        UnitConverter.NEUROCONSTRUCT_UNITS);
                                
                                float avMaxCond = tgtAvg * weight * maxCond;
                                String symbol = UnitConverter.conductanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
                                
                                
                                synReport.append("Syn "+synType+" av max cond: <b>"+avMaxCond+" "+symbol+"</b> ("+tgtAvg+" x "+weight+" (weight) x "+maxCond+" "
                                        +symbol+")<br>");
                            }

                        }
                        catch(ChannelMLException e)
                        {
                            logger.logError("Problem getting info out of "+cmlMech);
                        }
                        
                    }
                    
                    synNames.append(synType);
                    if (k < syns.size() - 1) synNames.append(", ");
                    else synNames.append("]");
                    
                }
                
                

                generationReport.append("<b>" + netConnName + "</b> (<font color=\"green\">"
                                        + src
                                        + "</font> -> <font color=\"red\">"
                                        + tgt +
                                        "</font>, " + synNames.toString() + ")<br>");

                generationReport.append("No. of conns: <b>"
                                        +
                                        project.generatedNetworkConnections.getSynapticConnections(netConnName).
                                        size()
                                        + "</b> (<font color=\"green\">" + srcAvg + srcStdString + " each</font>, " +
                                        "<font color=\"red\">" + tgtAvg + tgtStdString + " each</font>)<br>");
                
                generationReport.append(synReport);

                generationReport.append("<br>");
            }
            else
            {
                if (simConfig!=null && simConfig.getNetConns().contains(netConnName)) // No point printing info if it's not in sim config
                {
                    generationReport.append("No connections generated for Network Connection: " + netConnName +
                                            "<br><br>");
                }
            }
        }
        return generationReport.toString();

    }





    public SimpleXMLElement getNetworkMLElement(int unitSystem,
                                                boolean extraComments) throws NeuroMLException
    {
        SimpleXMLElement projectionsElement = null;

        String metadataPrefix = MetadataConstants.PREFIX + ":";
        try
        {
            logger.logComment("Going to save file in NeuroML format: " + this.getNumberSynapticConnections(ANY_NETWORK_CONNECTION) +
                              " connections in total");

            projectionsElement = new SimpleXMLElement(NetworkMLConstants.PROJECTIONS_ELEMENT);

            if (unitSystem == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                projectionsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, "Physiological Units"));
            else if (unitSystem == UnitConverter.GENESIS_SI_UNITS)
                projectionsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, "SI Units"));

            Enumeration keys = mySynapticConnectionVectors.keys();

            while (keys.hasMoreElements())
            {
                String netConnName = (String) keys.nextElement();
                ArrayList<SingleSynapticConnection> synConns = getSynapticConnections(netConnName);

                String sourceCellGroup = null;
                String targetCellGroup = null;

                Vector<SynapticProperties>  globalSynPropList = null;

                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                {
                    logger.logComment("isValidSimpleNetConn..");
                    sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                    targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                    globalSynPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                }

                else if (project.volBasedConnsInfo.isValidAAConn(netConnName))
                {
                    logger.logComment("isValidAAConn..");
                    sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                    targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                    globalSynPropList = project.volBasedConnsInfo.getSynapseList(netConnName);
                }



                SimpleXMLElement projectionElement = new SimpleXMLElement(NetworkMLConstants.PROJECTION_ELEMENT);

                projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROJ_NAME_ATTR, netConnName));

                projectionElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.SOURCE_ELEMENT, sourceCellGroup));
                projectionElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.TARGET_ELEMENT, targetCellGroup));

                for (SynapticProperties synProps:  globalSynPropList)
                {
                    SimpleXMLElement synPropsElement = new SimpleXMLElement(NetworkMLConstants.SYN_PROPS_ELEMENT);
                    synPropsElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.SYN_TYPE_ELEMENT, synProps.getSynapseType()));


                    SimpleXMLElement defValsElement = new SimpleXMLElement(NetworkMLConstants.DEFAULT_VAL_ELEMENT);
                    synPropsElement.addChildElement(defValsElement);

                    synPropsElement.addContent("\n        ");

                    defValsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INTERNAL_DELAY_ATTR, synProps.getDelayGenerator().getNominalNumber()+""));
                    defValsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.WEIGHT_ATTR, synProps.getWeightsGenerator().getNominalNumber()+""));
                    defValsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.THRESHOLD_ATTR, synProps.getThreshold()+""));

                    projectionElement.addChildElement(synPropsElement);
                }
                SimpleXMLElement connsElement = new SimpleXMLElement(NetworkMLConstants.CONNECTIONS_ELEMENT);
                projectionElement.addChildElement(connsElement);

                int id = 0;

                for(SingleSynapticConnection synConn: synConns)
                {
                    //System.out.println("synConn: "+synConn);
                    
                    SimpleXMLElement connElement = new SimpleXMLElement(NetworkMLConstants.CONNECTION_ELEMENT);

                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.CONNECTION_ID_ATTR, id+""));

                    id++;

                    if (extraComments)
                    {
                        Point3f sourceSynLoc = CellTopologyHelper.getAbsolutePosSegLoc(project,
                                                                                       sourceCellGroup,
                                                                                       synConn.sourceEndPoint.cellNumber,
                                                                                       synConn.sourceEndPoint.location);

                        Point3f tgtSynLoc = CellTopologyHelper.getAbsolutePosSegLoc(project,
                                                                                    targetCellGroup,
                                                                                    synConn.targetEndPoint.cellNumber,
                                                                                    synConn.targetEndPoint.location);

                        float dist = tgtSynLoc.distance(sourceSynLoc);

                        String info = "\nSource synaptic location: "+ sourceSynLoc+"\n"+
                            "Target synaptic location: "+ tgtSynLoc+"\n"+
                            "Separation: "+ dist;


                        SimpleXMLElement noteElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.NOTES_ELEMENT);
                        noteElement.addContent(info);
                        connElement.addChildElement(noteElement);

                    }


                    SimpleXMLElement preElement = new SimpleXMLElement(NetworkMLConstants.PRE_CONN_ELEMENT);
                    preElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.CELL_ID_ATTR, synConn.sourceEndPoint.cellNumber+""));
                    preElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SEGMENT_ID_ATTR, synConn.sourceEndPoint.location.getSegmentId()+""));
           
                    if (synConn.sourceEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN)
                    {
                        preElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.FRACT_ALONG_ATTR, 
                                                                        synConn.sourceEndPoint.location.getFractAlong()+""));
                    }
                    
                    SimpleXMLElement postElement = new SimpleXMLElement(NetworkMLConstants.POST_CONN_ELEMENT);
                    postElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.CELL_ID_ATTR, synConn.targetEndPoint.cellNumber+""));
                    postElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SEGMENT_ID_ATTR, synConn.targetEndPoint.location.getSegmentId()+""));

                    if (synConn.targetEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN)
                    {
                        postElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.FRACT_ALONG_ATTR,
                                                                        synConn.targetEndPoint.location.getFractAlong() + ""));
                    }

                    connElement.addContent("\n                ");
                    connElement.addChildElement(preElement);
                    connElement.addContent("\n                ");
                    connElement.addChildElement(postElement);


                    if (synConn.props!=null && synConn.props.size()>0)
                    {
                        for (ConnSpecificProps prop:synConn.props)
                        {
                            SimpleXMLElement propElement = new SimpleXMLElement(NetworkMLConstants.CONN_PROP_ELEMENT);
                            connElement.addContent("\n                ");
                            connElement.addChildElement(propElement);

                            if (globalSynPropList.size()>1)
                            {
                                /** @todo Clean up... */
                                propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SYN_TYPE_ELEMENT, prop.synapseType));

                            }
                            propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROP_DELAY_ATTR, synConn.apPropDelay + ""));

                            propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INTERNAL_DELAY_ATTR, prop.internalDelay+""));
                            propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.WEIGHT_ATTR, prop.weight+""));
                        }
                    }
                    else
                    {
                        if (synConn.apPropDelay>0)
                        {
                            SimpleXMLElement propElement = new SimpleXMLElement(NetworkMLConstants.CONN_PROP_ELEMENT);
                            connElement.addContent("\n                ");
                            connElement.addChildElement(propElement);

                            propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROP_DELAY_ATTR,
                                synConn.apPropDelay + ""));
                        }
                    }

                    connElement.addContent("\n            ");

                    connsElement.addChildElement(connElement);
                }

                projectionsElement.addChildElement(projectionElement);
            }
            logger.logComment("Finished saving data to projs element");

        }

        catch (Exception ex)
        {
            throw new NeuroMLException("Problem creating prjections element file", ex);
        }
        return projectionsElement;

    }



    /**
     * Gets all PostSynapticObjects for the specified synapse on the segment
     *
     * @todo probably not the most efficient implementation of this...
     *
     * @param netConnName String The NetConn
     * @param synapseType String Which of possible multiple synapse types to pick
     * @param cellNumber number of cell in cell group
     * @param segmentId int Segment id. If -1 then any segment on the cell
     * @return ArrayList List of PostSynapticObjects
     */
    public ArrayList<PostSynapticObject> getSynObjsPresent(String netConnName, String synapseType, int cellNumber, int segmentId)
    {
        ArrayList<PostSynapticObject> objNames = new ArrayList<PostSynapticObject>();

        ArrayList<SingleSynapticConnection> allConns = project.generatedNetworkConnections.getSynapticConnections(netConnName);

        for (int singleConnIndex=0; singleConnIndex<allConns.size(); singleConnIndex++)
        {
            SingleSynapticConnection synConn = allConns.get(singleConnIndex);

            if (synConn.targetEndPoint.cellNumber == cellNumber)
            {
                if (segmentId == -1 || synConn.targetEndPoint.location.getSegmentId() == segmentId)
                {
                    PostSynapticObject object = new PostSynapticObject(netConnName, synapseType, cellNumber, segmentId, singleConnIndex);
                    objNames.add(object);
                }
            }
        }

        return objNames;

    }









    public void loadFromFile(File netConnFile) throws java.io.IOException
    {
        logger.logComment("Loading net connections from file: "
                          + netConnFile.getAbsolutePath());

        this.reset();
        Reader in = new FileReader(netConnFile);
        LineNumberReader reader = new LineNumberReader(in);
        String nextLine = null;

        String  currentNetConnName = null;

        while ( (nextLine = reader.readLine()) != null)
        {
            if (nextLine.endsWith(":"))
            {
                currentNetConnName = nextLine.substring(0, nextLine.length()-1);
                logger.logComment("Current net conn: "+ currentNetConnName);
            }
            else
            {
                String[] brokenUp = nextLine.split(":");

                int sourceCellNumber = Integer.parseInt(brokenUp[1]);
                int sourceCellSegmentIndex = Integer.parseInt(brokenUp[2]);
                float sourceCellDistanceAlong = Float.parseFloat(brokenUp[3]);
                int targetCellNumber = Integer.parseInt(brokenUp[4]);
                int targetCellSegmentIndex = Integer.parseInt(brokenUp[5]);
                float targetCellDistanceAlong = Float.parseFloat(brokenUp[6]);

                int connectionType = MORPH_NETWORK_CONNECTION;
                // ensuring backwards compatability...
                if (brokenUp.length >7)
                connectionType = Integer.parseInt(brokenUp[7]);



                addSynapticConnection(currentNetConnName,
                                      connectionType,
                                      sourceCellNumber,
                                      sourceCellSegmentIndex,
                                      sourceCellDistanceAlong,
                                      targetCellNumber,
                                      targetCellSegmentIndex,
                                      targetCellDistanceAlong,
                                      0,
                                      null);

            }
        }
        in.close();

        logger.logComment("Finished loading info. Internal state: "+ this.toString());

    }




    public class SingleSynapticConnection
    {
        public int connectionType;

        public SynapticConnectionEndPoint sourceEndPoint;
        public SynapticConnectionEndPoint targetEndPoint;

        /**
         * Note: the delay associated with the Action Potential propagating between rpre & post
         * connection points could in theory be calculated each time from cell positions and morphology,
         * but it's more efficient to cache it here when the connections are generated
         */
        public float apPropDelay = 0;

        public ArrayList<ConnSpecificProps> props = null;

        public SingleSynapticConnection(int connectionType,
                                        int sourceCellNumber,
                                        int sourceCellSegmentIndex,
                                        float sourceCellDistanceAlong,
                                        int targetCellNumber,
                                        int targetCellSgmentIndex,
                                        float targetCellDistanceAlong,
                                        float apPropDelay,
                                        ArrayList<ConnSpecificProps> props)
        {
            this.connectionType = connectionType;

            this.props = props;

            this.apPropDelay = apPropDelay;

            this.sourceEndPoint
                = new SynapticConnectionEndPoint(
                        new PreSynapticTerminalLocation(sourceCellSegmentIndex,
                                                        sourceCellDistanceAlong),
                        sourceCellNumber);

            this.targetEndPoint
                = new SynapticConnectionEndPoint(
                        new PostSynapticTerminalLocation(targetCellSgmentIndex,
                                                        targetCellDistanceAlong),
                        targetCellNumber);
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SingleSynapticConnection:"
                      + sourceEndPoint.cellNumber + ":"
                      + sourceEndPoint.location.getSegmentId() + ":"
                      + sourceEndPoint.location.getFractAlong() + ":"
                      + targetEndPoint.cellNumber + ":"
                      + targetEndPoint.location.getSegmentId() + ":"
                      + targetEndPoint.location.getFractAlong() + ":"
                      + connectionType);

            if (props!=null)
            {
             for (ConnSpecificProps prop:props)
             {
                 sb.append(": "+ prop);
             }
            }
            return sb.toString();
        }
    }




    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("/bernal/projects/Delays/Delays.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });

            GeneratedNetworkConnections gnc = new GeneratedNetworkConnections(testProj);

            //System.out.println("Internal info: \n" + gnc.toString()); ;


            ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps>();
            ConnSpecificProps cp2 = new ConnSpecificProps("hhh");
            cp2.internalDelay = 22;
            props.add(cp2);
            ConnSpecificProps cp = new ConnSpecificProps("ggg");
            cp.internalDelay = 9;
            props.add(cp);
            System.out.println("props: " + props);



            gnc.addSynapticConnection("NetConn_SampleCellGroup_CellGroup_2",
                                      GeneratedNetworkConnections.COMPLEX_NETWORK_CONNECTION,
                                      1,2,0.5f,2,4,0.7f,
                                      999,
                                      null);
            gnc.addSynapticConnection("NetConn_SampleCellGroup_CellGroup_2",
                                      GeneratedNetworkConnections.COMPLEX_NETWORK_CONNECTION,
                                      1,2,0.7f,2,4,0.7f,
                                      999,
                                      null);
            gnc.addSynapticConnection("NetConn_SampleCellGroup_CellGroup_2_1",
                                      GeneratedNetworkConnections.COMPLEX_NETWORK_CONNECTION,
                                      1,2,0.6f,2,4,0.7f,
                                      999,
                                      null);
            gnc.addSynapticConnection("NetConn_SampleCellGroup_CellGroup_2_1",
                                      GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                      1,2,0.9f,2,4,0.7f,
                                      999,
                                      props);

            System.out.println("Internal info: \n" + gnc.toString()); ;
            String home = System.getProperty("user.home");

            SimpleXMLElement projs = gnc.getNetworkMLElement(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, true);

            System.out.println("projs: "+projs.getXMLString("", false));

if (true) return;
            File f = new File(home + System.getProperty("file.separator") + "tempp.txt");
            if (!f.exists()) System.out.println("File doesn't exist yet...");
            else System.out.println("File exists...");
            gnc.saveToFile(f);

            GeneratedNetworkConnections cpr2 = new GeneratedNetworkConnections(testProj);

            cpr2.loadFromFile(f);
            System.out.println("New internal info: \n" + cpr2.toString()); ;






        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}


