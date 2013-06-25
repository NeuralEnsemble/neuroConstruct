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

package ucl.physiol.neuroconstruct.project;


import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.cell.*;
import java.io.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import javax.vecmath.*;
import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.gui.ValidityStatus;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;

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

    private Hashtable<String, ArrayList<SingleSynapticConnection>> mySynapticConnectionVectors
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
        logger.logComment("From src " + sourceCellNumber +"("+sourceCellSegmentIndex+"("+sourceCellDisplacement+")) to "
                +targetCellNumber+"("+targetCellSgmentIndex+"("+targetCellDisplacement+")), props: "+props);
        //logger.logComment("Current num syn conns: "+ getNumberSynapticConnections());
    }
    
    /*
     * @returns A matrix of size (pre syn cell number) x (post syn cell number) with entry (i,j)
     * giving the number of connections between pre cell i and post cell j
     * 
     */
    public int[][] getConnectionMatrix(String netConnectionName, Project project)
    {
        logger.logComment("Synaptic conn matrix sought for: #" + this.hashCode() + " out of my " +
                          getNumberSynapticConnections(ANY_NETWORK_CONNECTION)
                          + " net conns from " + mySynapticConnectionVectors.keySet());
        

        if (!mySynapticConnectionVectors.containsKey(netConnectionName))
        {
            logger.logComment("No SingleSynapticConnections yet...");
            return null;
        }
        
        ArrayList<SingleSynapticConnection> synapticConnectionVector
            = mySynapticConnectionVectors.get(netConnectionName);
        
        String src = null;
        String tgt = null;
        
        if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnectionName))
        {
            src = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnectionName);
            tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnectionName);

        }
        else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnectionName))
        {
            src = project.volBasedConnsInfo.getSourceCellGroup(netConnectionName);
            tgt = project.volBasedConnsInfo.getTargetCellGroup(netConnectionName);

        }
        
        
        int[][] mx = new int[project.generatedCellPositions.getNumberInCellGroup(src)][project.generatedCellPositions.getNumberInCellGroup(tgt)];
       
        
        
        for(SingleSynapticConnection conn: synapticConnectionVector)
        {
            mx[conn.sourceEndPoint.cellNumber][conn.targetEndPoint.cellNumber]++;
        }

        logger.logComment(synapticConnectionVector.size()+ " SingleSynapticConnections so far...");
        
        return mx;
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

    public int getNumAllSynConns()
    {
        return getNumberSynapticConnections(ANY_NETWORK_CONNECTION);
    }

    public int getNumNonEmptyNetConns()
    {
        int totalCount = 0;

        Enumeration keys = mySynapticConnectionVectors.keys();

        while(keys.hasMoreElements())
        {
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get((String)keys.nextElement());
            if (synConns.size()>0)
                totalCount++;
        }
        return totalCount;
    }


    public ArrayList<String> getNamesNonEmptyNetConns()
    {
        ArrayList<String> ncs = new ArrayList<String>();

        Enumeration<String> keys = mySynapticConnectionVectors.keys();

        while(keys.hasMoreElements())
        {
            String nc = keys.nextElement();
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get(nc);
            if (synConns.size()>0)
                ncs.add(nc);
        }
        return ncs;
    }


    public int getNumberSynapticConnections(int connType)
    {
        //logger.logComment("getNumberSynapticConnections sought for : "+this.hashCode());
        int totalCount = 0;

        Enumeration keys = mySynapticConnectionVectors.keys();

        while(keys.hasMoreElements())
        {
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get((String)keys.nextElement());
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
                    if (!allIndices.contains(conn.targetEndPoint.cellNumber))
                        allIndices.add(conn.targetEndPoint.cellNumber);
                }
                else
                {
                    allIndices.add(conn.targetEndPoint.cellNumber);
                }
            }
        }
        return allIndices;
    }
    
    public boolean areConnected(String netConnectionName, 
                               int sourceCellIndex, 
                               int targetCellIndex)
    {
        for (SingleSynapticConnection synConn: getSynapticConnections(netConnectionName))
        {
            if (synConn.sourceEndPoint.cellNumber==sourceCellIndex &&
                synConn.targetEndPoint.cellNumber==targetCellIndex)
                return true;
        }
        return false;
        
    }
    
    
//    // Matteo needs to check for connection in both directions for the "no recurrent" option...
//    public boolean areConnectedInAnyDirection(String netConnectionNameA, 
//                               int cellIndexA,
//                               String netConnectionNameB,
//                               int cellIndexB)
//    {
//                
//        boolean connected = false;
//        
//        int i=0;
//        ArrayList<SingleSynapticConnection> connFromA = getConnsFromSource(netConnectionNameA, cellIndexA);
//        while ((connected)&&(i<connFromA.size()))
//        {            
//            if (connFromA.get(i).targetEndPoint.cellNumber == cellIndexB)
//                connected = true;
//            
//            i++;            
//        }
//        
//        i=0;
//        ArrayList<SingleSynapticConnection> connFromB = getConnsFromSource(netConnectionNameB, cellIndexB);
//        while ((connected)&&(i<connFromB.size())) //if "connected" is already true this loop is skipped (optimization)
//        {            
//            if (connFromA.get(i).targetEndPoint.cellNumber == cellIndexA)
//                connected = true;
//            
//            i++;            
//        }
//        
//        return connected;
//          
//    }

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



    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("GeneratedNetworkConnections with " 
                + getNumberSynapticConnections(ANY_NETWORK_CONNECTION) +
                  " positions in total\n");

        Enumeration keys = mySynapticConnectionVectors.keys();

        while (keys.hasMoreElements())
        {
            String netConnName = (String) keys.nextElement();
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get(netConnName);
            sb.append(netConnName+" has "+synConns.size()
                      + " entries. First: "+synConns.get(0)+"\n");
        }
        return sb.toString();
    }

    /*
     * Useful for Python interface
     */
    public String details()
    {
        return details(false);
    }

    public String details(boolean html)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Network has " 
                + GeneralUtils.getTabbedString(getNumberSynapticConnections(ANY_NETWORK_CONNECTION)+"", "b", html) +
                  " connections in total"+GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));

        Enumeration keys = mySynapticConnectionVectors.keys();

        while (keys.hasMoreElements())
        {
            String netConnName = (String) keys.nextElement();
            
            ArrayList<SingleSynapticConnection> synConns = mySynapticConnectionVectors.get(netConnName);
            String src, tgt;

            if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
            {
                src = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
            }
            else
            {
                src = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                tgt = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
            }

            
            sb.append("Network Connection: "+ GeneralUtils.getBold(netConnName, html) + " "
                    + "("+ GeneralUtils.getBold(src, html) + " -> "+ GeneralUtils.getBold(tgt, html) + ") "
                    + "has "+
                    GeneralUtils.getBold(synConns.size()+"", html) +" individual synaptic connections"+GeneralUtils.getEndLine(html));
            
            for (int i = 0; i < synConns.size(); i++)
            {
                sb.append("Connection "+i+": "+ synConns.get(i).details(html));
            }
            sb.append(GeneralUtils.getEndLine(html));
        }
            
        sb.append(GeneralUtils.getEndLine(html));
            
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
        StringBuilder generationReport = new StringBuilder();
        ArrayList<String> netConns = new ArrayList<String>();

        if (connType == MORPH_NETWORK_CONNECTION || connType == ANY_NETWORK_CONNECTION)
            netConns.addAll(project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames());

        if (connType == VOL_NETWORK_CONNECTION || connType == ANY_NETWORK_CONNECTION)
            netConns.addAll(project.volBasedConnsInfo.getAllAAConnNames());


        if (netConns.isEmpty())
        {
            generationReport.append("No Network Connections generated.<br><br>");

        }
        for (String netConnName: netConns)
        {
            StringBuilder synNames = new StringBuilder("[");

            Vector<SynapticProperties> syns = null;
            String src = null;
            String tgt = null;

            ArrayList<SingleSynapticConnection> conns 
                = project.generatedNetworkConnections.getSynapticConnections(netConnName);

            if (! (conns.isEmpty()))
            {
                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                {
                    syns = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                    src = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                    tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                }
                else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                {
                    syns = project.volBasedConnsInfo.getSynapseList(netConnName);
                    src = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                    tgt = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                }
                
                

                int srcCellNum = project.generatedCellPositions.getNumberInCellGroup(src);
                int tgtCellNum = project.generatedCellPositions.getNumberInCellGroup(tgt);

                logger.logComment("srcCellNum: "+srcCellNum+", tgtCellNum: "+ tgtCellNum);

                int[] numInEachSrcCell = new int[srcCellNum];
                int[] numInEachTgtCell = new int[tgtCellNum];

                int numZeroWeight = 0;
                int numNegWeight = 0;

                for (SingleSynapticConnection conn : conns)
                {
                    //logger.logComment(conn.toNiceString());
                    numInEachSrcCell[conn.sourceEndPoint.cellNumber]++;
                    numInEachTgtCell[conn.targetEndPoint.cellNumber]++;

                    if (conn.props!=null && conn.props.size()>0)
                    {
                        for(ConnSpecificProps csp: conn.props)
                        {
                            if (csp.weight==0) numZeroWeight++;
                            if (csp.weight<0) numNegWeight++;

                        }
                    }
                }

                String weightReport = "";



                if(numZeroWeight>0)
                {
                    weightReport = weightReport +" <font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\">("+numZeroWeight+" with zero weight)</font>";
                }
                if(numNegWeight>0)
                {
                    weightReport = weightReport +" <font color=\"red\">("+numNegWeight+" with negative weight)</font>";
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
                            all.addAll(Arrays.asList(doubExpSyns));
                            all.addAll(Arrays.asList(blockingSyns));
                            
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
                                
                                
                                synReport.append("Syn "+ClickProjectHelper.getCellMechLink(synType)+" av max cond: <b>"+avMaxCond+" "+symbol+"</b> ("+tgtAvg+" x "+weight+" (weight) x "+maxCond+" "
                                        +symbol+")<br>");
                            }

                        }
                        catch(XMLMechanismException e)
                        {
                            logger.logError("Problem getting info out of "+cmlMech);
                        }
                        
                    }
                    
                    synNames.append(ClickProjectHelper.getCellMechLink(synType));
                    if (k < syns.size() - 1) synNames.append(", ");
                    else synNames.append("]");
                    
                }
                
                
                

                generationReport.append("<b>" + ClickProjectHelper.getNetConnLink(netConnName) + "</b> (<font color=\"green\">"
                                        + ClickProjectHelper.getCellGroupLink(src)
                                        + "</font> -> <font color=\"red\">"
                                        + ClickProjectHelper.getCellGroupLink(tgt) +
                                        "</font>, " + synNames.toString() + ")<br>");

                generationReport.append("No. of conns: <b>"
                                        +
                                        project.generatedNetworkConnections.getSynapticConnections(netConnName).size()
                                        + weightReport+ "</b> (<font color=\"green\">" + srcAvg + srcStdString + " each</font> -> " +
                                        "<font color=\"red\">" + tgtAvg + tgtStdString + " each</font>)<br>");
                
                NumberGenerator nb = null;
                String startCellGroup = null;
                float numStartConnGen = -1;
                
                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                {
                    nb = project.morphNetworkConnectionsInfo.getConnectivityConditions(netConnName).getNumConnsInitiatingCellGroup();
                    startCellGroup = project.morphNetworkConnectionsInfo.getGenerationStartCellGroup(netConnName);
                    if (project.morphNetworkConnectionsInfo.getConnectivityConditions(netConnName).getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET)
                        numStartConnGen = srcAvg;
                    else
                        numStartConnGen = tgtAvg;
                        
                }
                else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                {
                    nb = project.volBasedConnsInfo.getConnectivityConditions(netConnName).getNumConnsInitiatingCellGroup();
                    startCellGroup = project.volBasedConnsInfo.getGenerationStartCellGroup(netConnName);
                    if (project.volBasedConnsInfo.getConnectivityConditions(netConnName).getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET)
                        numStartConnGen = srcAvg;
                    else
                        numStartConnGen = tgtAvg;
                }
                
                String warn = null;
                
                if (nb.isTypeFixedNum() )
                {
                    if(numStartConnGen<nb.getFixedNum())
                    {
                        warn = "Warning: this network connection specifies "+nb.getFixedNum()+" as the number of connections on each cell in: "+startCellGroup+", but the average " +
                                "number of connections per cell is "+ numStartConnGen;
                    }
                }
                else if(nb.isTypeRandomNum())
                {
                    if(numStartConnGen<nb.getMin())
                    {
                        warn = "Warning: this network connection specifies "+nb.toShortString()+" as the number of connections on each cell in: "+startCellGroup+", but the average " +
                                "number of connections per cell is "+ numStartConnGen;
                    }
                }
                else if(nb.isTypeGaussianNum())
                {
                    if(numStartConnGen<nb.getMin() || numStartConnGen<(nb.getMean()-(nb.getStdDev()/5f)))
                    {
                        warn = "Warning: this network connection specifies "+nb.toShortString()+" as the number of connections on each cell in: "+startCellGroup+", but the average " +
                                "number of connections per cell is "+ numStartConnGen;
                    }
                    
                }
                
                        
                
                generationReport.append(synReport);
                
                if (warn!=null)
                {
                    generationReport.append(GeneralUtils.getBoldColouredString(warn+"<br>", ValidityStatus.VALIDATION_COLOUR_WARN, true));
                
                }

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


    
    public SimpleXMLEntity getNetworkMLElement(int unitSystem,
                                                boolean extraComments) throws NeuroMLException
    {
        return getNetworkMLElements(unitSystem,
                                   extraComments,
                                   NeuroMLVersion.NEUROML_VERSION_1).get(0);
    }
    
    
    public ArrayList<SimpleXMLEntity> getNetworkMLElements(int unitSystem,
                                                boolean extraComments,
                                                NeuroMLVersion version) throws NeuroMLException
    {
        ArrayList<SimpleXMLEntity> entities = new ArrayList<SimpleXMLEntity>();
        
        int numConns = this.getNumberSynapticConnections(ANY_NETWORK_CONNECTION);
        
        logger.logComment("Going to save file in NeuroML format: " + numConns +
                          " connections in total");
        if (numConns==0)
        {
            entities.add(new SimpleXMLComment("There are no synaptic connections present in the network"));
            return entities;
        }
       
        SimpleXMLElement projectionsElement = null;


        boolean nml2 = version.isVersion2();
        boolean nml2beta = version.isVersion2betaOrLater();
        boolean wd = false;
        //boolean nml2alpha = version.isVersion2alpha();
                

        String metadataPrefix = MetadataConstants.PREFIX + ":";
        if (nml2) metadataPrefix = "";

        try
        {

            projectionsElement = new SimpleXMLElement(NetworkMLConstants.PROJECTIONS_ELEMENT);
            if (!nml2) entities.add(projectionsElement);

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

                else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                {
                    logger.logComment("isValidAAConn..");
                    sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                    targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                    globalSynPropList = project.volBasedConnsInfo.getSynapseList(netConnName);
                }
                
                String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
                String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);



                SimpleXMLElement projectionElement = new SimpleXMLElement(NetworkMLConstants.PROJECTION_ELEMENT);

                if (!nml2) 
                {
                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROJ_NAME_ATTR, netConnName));

                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SOURCE_ATTR, sourceCellGroup));
                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.TARGET_ATTR, targetCellGroup));
                }
                else
                {
                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_PROJ_ID, netConnName));

                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_PROJ_PRE, sourceCellGroup));
                    projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_PROJ_POST, targetCellGroup));
                }

                if (!nml2)
                {
                    for (SynapticProperties synProps:  globalSynPropList)
                    {
                        SimpleXMLElement synPropsElement = new SimpleXMLElement(NetworkMLConstants.SYN_PROPS_ELEMENT);

                        synPropsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SYN_TYPE_ATTR, synProps.getSynapseType()));

                        //synPropsElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.SYN_TYPE_ELEMENT, synProps.getSynapseType()));

                        //SimpleXMLElement defValsElement = new SimpleXMLElement(NetworkMLConstants.DEFAULT_VAL_ELEMENT);
                        //synPropsElement.addChildElement(defValsElement);

                        synPropsElement.addContent("\n        ");

                        synPropsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INTERNAL_DELAY_ATTR, 
                                (float)UnitConverter.getTime(synProps.getDelayGenerator().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem) +""));
                        synPropsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.WEIGHT_ATTR, synProps.getWeightsGenerator().getNominalNumber()+""));
                        synPropsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.THRESHOLD_ATTR, 
                                (float)UnitConverter.getVoltage(synProps.getThreshold(), UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem)+""));

                        projectionElement.addChildElement(synPropsElement);
                    }
                }
                else
                {
                        projectionElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_PROJ_SYNAPSE, globalSynPropList.firstElement().getSynapseType()));
                }
                
                SimpleXMLElement connsElement = new SimpleXMLElement(NetworkMLConstants.CONNECTIONS_ELEMENT);
                
                if (!nml2)
                    projectionElement.addChildElement(connsElement);

                connsElement.addAttribute(NetworkMLConstants.CONNECTIONS_SIZE_ATTR, ""+synConns.size());

                int id = 0;

                for(SingleSynapticConnection synConn: synConns)
                {
                    if (nml2 && !nml2beta)
                    {
                        for (SynapticProperties synProps:  globalSynPropList)
                        {
                            SimpleXMLElement connElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_EXP_CONN_ELEMENT);
                            entities.add(connElement);

                            connElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_CONN_FROM_ATTR,
                                    sourceCellGroup+"["+synConn.sourceEndPoint.cellNumber+"]");
                            connElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_CONN_TO_ATTR,
                                    targetCellGroup+"["+synConn.targetEndPoint.cellNumber+"]");
                            connElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_CONN_SYN_ATTR,
                                    synProps.getSynapseType());

                            /*if (synConn.props!=null && synConn.props.size()>0)
                            {
                                for (ConnSpecificProps prop:synConn.props)
                                {

                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_EXP_CONN_DELAY_ATTR,
                                            (float)UnitConverter.getTime((synConn.apPropDelay+prop.internalDelay), UnitConverter.NEUROCONSTRUCT_UNITS,UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + "ms"));


                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_EXP_CONN_WEIGHT_ATTR, prop.weight+""));
                                }
                            }*/
                        }
                    }
                    else
                    {
                        String connElName = NetworkMLConstants.CONNECTION_ELEMENT;
                        if (nml2beta && wd)
                            connElName = NetworkMLConstants.NEUROML2_CONNECTION_WD_ELEMENT;
                        
                        SimpleXMLElement connElement = new SimpleXMLElement(connElName);

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

                        //boolean prev171format = false; // Not fully supported for all changes...
                        if (false)//prev171format)
                        {
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
                        }
                        else
                        {
                            if (!nml2) 
                            {
                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PRE_CELL_ID_ATTR, synConn.sourceEndPoint.cellNumber+""));

                                if (synConn.sourceEndPoint.location.getSegmentId()!=0)
                                {
                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PRE_SEGMENT_ID_ATTR, synConn.sourceEndPoint.location.getSegmentId()+""));
                                }

                                if (synConn.sourceEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN)
                                {
                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PRE_FRACT_ALONG_ATTR,
                                                                                    synConn.sourceEndPoint.location.getFractAlong()+""));
                                }

                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.POST_CELL_ID_ATTR, synConn.targetEndPoint.cellNumber+""));

                                if (synConn.targetEndPoint.location.getSegmentId()!=0)
                                {
                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.POST_SEGMENT_ID_ATTR, synConn.targetEndPoint.location.getSegmentId()+""));
                                }

                                if (synConn.targetEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN)
                                {
                                    connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.POST_FRACT_ALONG_ATTR,
                                                                                    synConn.targetEndPoint.location.getFractAlong() + ""));
                                }
                            }
                            else
                            {
                                
                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_PRE_CELL_ID, "../"+sourceCellGroup+"/"+ synConn.sourceEndPoint.cellNumber+"/"+sourceCellType));
                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_POST_CELL_ID, "../"+targetCellGroup+"/"+ synConn.targetEndPoint.cellNumber+"/"+targetCellType));
                                
                            }
                        }

                        //UnitConverter.getTime(XXX, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem)
                        float totalDelayMs = 0;
                        float weight = 1;

                        if (synConn.props!=null && synConn.props.size()>0)
                        {
                            for (ConnSpecificProps prop:synConn.props)
                            {
                                SimpleXMLElement propElement = new SimpleXMLElement(NetworkMLConstants.CONN_PROP_ELEMENT);
                                if (!nml2)
                                {
                                    connElement.addContent("\n                ");
                                    connElement.addChildElement(propElement);

                                    if (globalSynPropList.size()>1)
                                    {
                                        /** @todo Clean up... */
                                        propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.SYN_TYPE_ATTR, prop.synapseType));

                                    }
                                    propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROP_DELAY_ATTR,
                                            (float)UnitConverter.getTime(synConn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem) + ""));

                                    //System.out.println("..."+prop.internalDelay);
                                    //System.out.println("..."+(float)UnitConverter.getTime(prop.internalDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem));

                                    propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INTERNAL_DELAY_ATTR,
                                            (float)UnitConverter.getTime(prop.internalDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem)+""));

                                    propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.WEIGHT_ATTR, prop.weight+""));
                                } 
                                else
                                {
                                    weight = prop.weight;
                                    
                                    totalDelayMs = totalDelayMs + (float)UnitConverter.getTime(synConn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem);
                                    totalDelayMs = totalDelayMs + (float)UnitConverter.getTime(prop.internalDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem);
                                }
                            }
                        }
                        else
                        {
                            if (synConn.apPropDelay>0)
                            {
                                SimpleXMLElement propElement = new SimpleXMLElement(NetworkMLConstants.CONN_PROP_ELEMENT);
                                if (!nml2)
                                {
                                    connElement.addContent("\n                ");
                                    connElement.addChildElement(propElement);

                                    propElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.PROP_DELAY_ATTR,
                                        (float)UnitConverter.getTime(synConn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem) + ""));
                                }
                                else
                                {
                                    totalDelayMs += (float)UnitConverter.getTime(synConn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS,unitSystem);
                                }
                            }
                        }

                        if (!nml2) 
                        {
                            connElement.addContent("\n            ");
                            connsElement.addChildElement(connElement);
                        }
                        else
                        {
                            if (wd) 
                            {
                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_DELAY_ATTR, totalDelayMs + "ms"));
                                connElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NEUROML2_WEIGHT_ATTR, weight+""));
                            }
                                    
                            projectionElement.addContent("\n            ");
                            projectionElement.addChildElement(connElement);
                        }
                    }
                }

                if (!nml2) 
                {
                    projectionsElement.addChildElement(projectionElement);
                } 
                else if (nml2beta) 
                {
                    projectionElement.addContent("\n        ");
                    entities.add(projectionElement);
                }
            }
            logger.logComment("Finished saving data to projs element");

        }

        catch (Exception ex)
        {
            throw new NeuroMLException("Problem creating prjections element file", ex);
        }

        return entities;

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

        @Override
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
        
        
        public String toShortString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Conn:"
                      + sourceEndPoint.cellNumber + " ->"
                      + targetEndPoint.cellNumber);

        
            return sb.toString();
        }
        
        public String details(boolean html)
        {
            StringBuilder sb = new StringBuilder();

            String indent = "    ";
            if (html) indent = "&nbsp;&nbsp;&nbsp;&nbsp;";
        
            sb.append("Cell: "
                      + GeneralUtils.getBold( sourceEndPoint.cellNumber, html) + ", seg: "
                      + GeneralUtils.getBold( sourceEndPoint.location.getSegmentId(), html) + ", fract: "
                      + GeneralUtils.getBold( sourceEndPoint.location.getFractAlong(), html) + " -> Cell: "
                      + GeneralUtils.getBold( targetEndPoint.cellNumber, html) + ", seg: "
                      + GeneralUtils.getBold( targetEndPoint.location.getSegmentId(), html) + ", fract: "
                      + GeneralUtils.getBold( targetEndPoint.location.getFractAlong(), html) + ", prop delay: "
                      + GeneralUtils.getBold( apPropDelay, html) + ", conn type: "
                      + GeneralUtils.getBold( connectionType, html)+GeneralUtils.getEndLine(html));

            if (props!=null)
            {
                 for (ConnSpecificProps prop:props)
                 {
                     sb.append(indent+prop.details(html)+GeneralUtils.getEndLine(html));
                 }
            }
            return sb.toString();
        }
        
        
        public String toNiceString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Single Syn Conn: (cell: "
                      + sourceEndPoint.cellNumber + ", seg: "
                      + sourceEndPoint.location.getSegmentId() + ", fract: "
                      + sourceEndPoint.location.getFractAlong() + ") -> (cell: "
                      + targetEndPoint.cellNumber + ", seg: "
                      + targetEndPoint.location.getSegmentId() + ", fract: "
                      + targetEndPoint.location.getFractAlong() + ") prop delay: "
                      + apPropDelay + ", conn type: "
                      + connectionType);

            if (props!=null)
            {
             for (ConnSpecificProps prop:props)
             {
                 sb.append(", "+ prop.toNiceString());
             }
            }
            return sb.toString();
        }
    }




    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("osb/showcase/neuroConstructShowcase/Ex5_Networks/Ex5_Networks.ncx"),
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
            GeneratedCellPositions gcp = testProj.generatedCellPositions;
            GeneratedNetworkConnections gnc = testProj.generatedNetworkConnections;


            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(testProj);

            String simConfName = SimConfigInfo.DEFAULT_SIM_CONFIG_NAME;

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);

            while(pm.isGenerating())
            {
                Thread.sleep(200);
            }
            System.out.println("Num cells generated: "+ testProj.generatedCellPositions.getAllPositionRecords().size());

            /*

            gcp.addPosition("LowerCellGroup", 0, 0, 1, 2);
            gcp.addPosition("LowerCellGroup", 1, 0, 1, 2);
            gcp.addPosition("LowerCellGroup", 2, 0, 1, 2);
            gcp.addPosition("LowerCellGroup", 3, 0, 1, 2);
            
            gcp.addPosition("UpperCellGroup", 0, 0, 1, 2);
            gcp.addPosition("UpperCellGroup", 1, 0, 1, 2);
            gcp.addPosition("UpperCellGroup", 2, 0, 1, 2);

            //System.out.println("Internal info: \n" + gnc.toString()); ;


            ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps>();
            ConnSpecificProps cp2 = new ConnSpecificProps("hhh");
            cp2.internalDelay = 22;
            props.add(cp2);
            ConnSpecificProps cp = new ConnSpecificProps("ggg");
            cp.internalDelay = 9;
            props.add(cp);
            System.out.println("props: " + props);

            String nc1 = "Random";
            String nc2 = "NetConn_SampleCellGroup_CellGroup_2_1";
            

            gnc.addSynapticConnection(nc1,
                                      GeneratedNetworkConnections.VOL_NETWORK_CONNECTION,
                                      1,2,0.5f,2,4,0.7f,
                                      999,
                                      null);
            
            
            gnc.addSynapticConnection(nc1,
                                      GeneratedNetworkConnections.VOL_NETWORK_CONNECTION,
                                      1,2,0.7f,2,4,0.7f,
                                      999,
                                      null);
            
            
            gnc.addSynapticConnection(nc1, 2,1);
            gnc.addSynapticConnection(nc1, 1,1);
            gnc.addSynapticConnection(nc1, 0,0);
            gnc.addSynapticConnection(nc1, 0,1);
            
            
            gnc.addSynapticConnection(nc2,
                                      GeneratedNetworkConnections.VOL_NETWORK_CONNECTION,
                                      1,2,0.6f,2,4,0.7f,
                                      999,
                                      null);
            gnc.addSynapticConnection(nc2,
                                      GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                      1,2,0.9f,2,4,0.7f,
                                      999,
                                      props);

            System.out.println("Internal info: \n" + gnc.toString()); 
            String home = System.getProperty("user.home");
            
            int[][] mx = gnc.getConnectionMatrix(nc1, testProj);
            
            for(int i =0;i<mx.length;i++)
            {
                for(int j =0;j<mx[i].length;j++)
                    System.out.println("x(i,j) = x("+i+","+j+") = "+mx[i][j]);
            }
            */
            
            
            System.out.println("Ready...");

            ArrayList<SimpleXMLEntity> networkMLElements = gnc.getNetworkMLElements(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, false, NeuroMLVersion.NEUROML_VERSION_2_BETA);

            for (SimpleXMLEntity sxe: networkMLElements) {
                System.out.println("--- conns: "+sxe.getXMLString("", false));
            }







        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}

