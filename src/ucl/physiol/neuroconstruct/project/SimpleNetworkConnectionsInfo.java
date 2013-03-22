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

import java.util.*;

import javax.swing.table.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Implementation of AbstractTableModel used for storing the Network Connections
 * Info
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimpleNetworkConnectionsInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("NetworkConnectionsInfo");

    public final static int COL_NUM_NETCONN_NAME = 0;
    public final static int COL_NUM_SOURCE = 1;
    public final static int COL_NUM_TARGET = 2;
    public final static int COL_NUM_SYNAPSE_LIST = 3;
    public final static int COL_NUM_SEARCH_PATTERN = 4;
    public final static int COL_NUM_MAX_MIN = 5;
    public final static int COL_NUM_CONN_CONDS = 6;


    public final static int COL_NUM_AP_SPEED = 7;


    //public final static int COL_NUM_SYNAPSE_PROPS = 3;

    //public final static int COL_NUM_GAP_OPTION = 7;


    final String[] columnNames = new String[8];

    Vector<String> vectorNames = new Vector<String>();
    Vector vectorSynapseList = new Vector();  // type not specified, as used to be Vector<SynapticProperties>, now Vector<Vector<SynapticProperties>>
    Vector<String> vectorSource = new Vector<String>();
    Vector<String> vectorTarget = new Vector<String>();
    Vector<SearchPattern> vectorSearchPattern = new Vector<SearchPattern>();
    Vector<MaxMinLength> vectorMaxMin = new Vector<MaxMinLength>();
    Vector<ConnectivityConditions> vectorConnConds = new Vector<ConnectivityConditions>();
    Vector<Float> vectorAPSpeed = new Vector<Float>();




    public SimpleNetworkConnectionsInfo()
    {
        logger.logComment("New NetworkConnectionsInfo created");
        columnNames[COL_NUM_NETCONN_NAME] = new String("Name");
        columnNames[COL_NUM_SYNAPSE_LIST] = new String("Synapse Type");
        columnNames[COL_NUM_SOURCE] = new String("Source");
        columnNames[COL_NUM_TARGET] = new String("Target");
        columnNames[COL_NUM_SEARCH_PATTERN] = new String("Search Pattern");
        columnNames[COL_NUM_CONN_CONDS] = new String("Connectivity Conditions");
        columnNames[COL_NUM_MAX_MIN] = new String("Max/min");
        columnNames[COL_NUM_AP_SPEED] = new String("AP speed");
       // columnNames[COL_NUM_GAP_OPTION] = new String("Gap Option");

    }

    public int getRowCount()
    {
        return vectorNames.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch (columnIndex)
        {
            case COL_NUM_NETCONN_NAME:
                return vectorNames.elementAt(rowIndex);
            case COL_NUM_SYNAPSE_LIST:
            {
                //System.out.println("vectorSynapseList: "+vectorSynapseList);
                if (vectorSynapseList.elementAt(rowIndex) instanceof SynapticProperties)
                {
                    //System.out.println("Converting from old Vector<SynapticProperties> to Vector<Vector<SynapticProperties>>");
                    Vector<SynapticProperties> vec = new Vector<SynapticProperties>();
                    vec.add((SynapticProperties)vectorSynapseList.elementAt(rowIndex));
                    vectorSynapseList.setElementAt(vec, rowIndex);
                }
                return vectorSynapseList.elementAt(rowIndex);
            }
            case COL_NUM_SOURCE:
                return vectorSource.elementAt(rowIndex);
            case COL_NUM_TARGET:
                return vectorTarget.elementAt(rowIndex);
            case COL_NUM_SEARCH_PATTERN:
                return vectorSearchPattern.elementAt(rowIndex);
            case COL_NUM_CONN_CONDS:
                return vectorConnConds.elementAt(rowIndex);
            case COL_NUM_MAX_MIN:
                return vectorMaxMin.elementAt(rowIndex);
            case COL_NUM_AP_SPEED:
            {
                if (vectorAPSpeed.size()==0)
                {
                    vectorAPSpeed.setSize(vectorNames.size());
                    for (int i = 0; i < vectorNames.size(); i++)
                    {
                        vectorAPSpeed.setElementAt(Float.MAX_VALUE, i);
                    }
                }
                return vectorAPSpeed.elementAt(rowIndex);
            }

           // case COL_NUM_GAP_OPTION:
            //    return vectorGapOption.elementAt(rowIndex);

            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        logger.logComment("Setting row: "+row+", col: "+col + " to: "+ value) ;
        switch (col)
        {
            case COL_NUM_NETCONN_NAME:
                this.vectorNames.setElementAt((String)value , row);
                break;
            case COL_NUM_SYNAPSE_LIST:
                this.vectorSynapseList.setElementAt(value , row);
                break;
            case COL_NUM_SOURCE:
                this.vectorSource.setElementAt((String)value , row);
                break;
            case COL_NUM_TARGET:
                this.vectorTarget.setElementAt((String)value , row);
                break;
            case COL_NUM_SEARCH_PATTERN:
                this.vectorSearchPattern.setElementAt((SearchPattern)value , row);
                break;
            case COL_NUM_CONN_CONDS:
                this.vectorConnConds.setElementAt((ConnectivityConditions)value , row);
                break;
          //  case COL_NUM_GROW_MODE:
          //      this.vectorGrowMode.setElementAt((GrowMode)value , row);
         //       break;

            case COL_NUM_MAX_MIN:
                this.vectorMaxMin.setElementAt((MaxMinLength)value , row);
                break;

            case COL_NUM_AP_SPEED:
                this.vectorAPSpeed.setElementAt((Float)value , row);
                break;

        }
        fireTableCellUpdated(row, col);
    }


    public void addNetConn(String name,
                       String source,
                       String target,
                       Vector<SynapticProperties> synList,
                       SearchPattern searchPattern,
                       MaxMinLength maxMin,
                       ConnectivityConditions connConds,
                       float jumpSpeed)  throws NamingException
    {
        addRow(name,
               source,
               target,
               synList,
               searchPattern,
               maxMin,
               connConds,
               jumpSpeed);
    }

    public void addRow(String name,
                       String source,
                       String target,
                       Vector<SynapticProperties> synList,
                       SearchPattern searchPattern,
                       MaxMinLength maxMin,
                       ConnectivityConditions connConds,
                       float jumpSpeed)  throws NamingException
    {
        int countSoFar = vectorNames.size();

        vectorNames.add(countSoFar, name);
        vectorSynapseList.add(countSoFar, synList);
        vectorSource.add(countSoFar, source);
        vectorTarget.add(countSoFar, target);
        vectorSearchPattern.add(countSoFar, searchPattern);
        vectorConnConds.add(countSoFar, connConds);
        //vectorGrowMode.add(countSoFar, growMode);
        vectorMaxMin.add(countSoFar, maxMin);
        this.vectorAPSpeed.add(jumpSpeed);



        this.fireTableRowsInserted(countSoFar, countSoFar);
    }




    public boolean deleteNetConn(int index)
    {
        if (index<0 || index>=vectorNames.size()) return false;

        vectorNames.removeElementAt(index);
        vectorSynapseList.removeElementAt(index);
        vectorSource.removeElementAt(index);
        vectorTarget.removeElementAt(index);
        vectorSearchPattern.removeElementAt(index);
        //vectorGrowMode.removeElementAt(index);
        vectorMaxMin.removeElementAt(index);
        vectorConnConds.removeElementAt(index);
        this.vectorAPSpeed.removeElementAt(index);


        this.fireTableRowsDeleted(index, index);
        return true;
    }

    public void deleteAllNetConns()
    {
        vectorNames = new Vector<String>();
        vectorSynapseList = new Vector();  // type not specified, as used to be Vector<SynapticProperties>, now Vector<Vector<SynapticProperties>>
        vectorSource = new Vector<String>();
        vectorTarget = new Vector<String>();
        vectorSearchPattern = new Vector<SearchPattern>();
        vectorMaxMin = new Vector<MaxMinLength>();
        vectorConnConds = new Vector<ConnectivityConditions>();
        vectorAPSpeed = new Vector<Float>();
    }


    public boolean deleteNetConn(String netConnName)
    {
        return deleteNetConn(vectorNames.indexOf(netConnName));
    }



    @Override
    public boolean isCellEditable(int row, int col)
    {
        // Changed due to using NewNetworkConnDialog for editing...
/*        if (col == COL_NUM_AVG_NUM_SOURCE
            || col == COL_NUM_SEARCH_PATTERN
            || col == COL_NUM_PRE_SYN_DISTRIBUTION
            || col == COL_NUM_GROW_MODE
            || col == COL_NUM_SYNAPSE_PROPS)
            return true;
        else
 */           return false;
    }

    public int getNumSimpleNetConns()
    {
        return vectorNames.size();
    }

    public Vector<String> getAllSimpleNetConnNames()
    {
        return new Vector<String>(vectorNames);
    }


    public String getSummary(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return "No Net Conn of name: "+netConnName;
        StringBuffer sb = new StringBuffer(""+netConnName+": " );
        sb.append(getSourceCellGroup(netConnName) +" -> "+getTargetCellGroup(netConnName)+" (");
        
        for(SynapticProperties sp:getSynapseList(netConnName))
        {
                    if (!sb.toString().endsWith("(")) sb.append(", "+ sp.toString());
                    else sb.append(sp.toString());
        }
        sb.append("), "+getSearchPattern(netConnName)+", "+ getConnectivityConditions(netConnName));
        sb.append(", "+getMaxMinLength(netConnName)+", APSpeed: "+getAPSpeed(netConnName));
        return sb.toString();
    }
    

    public Vector<SynapticProperties> getSynapseList(String connName)
    {
        int index = vectorNames.indexOf(connName);
        Vector<SynapticProperties> synPropList = (Vector<SynapticProperties>) getValueAt(index, COL_NUM_SYNAPSE_LIST);
        return synPropList;
    }


    public void setSynapseList(String connName, Vector<SynapticProperties> synPropList)
    {
        int index = vectorNames.indexOf(connName);
        this.setValueAt(synPropList, index, COL_NUM_SYNAPSE_LIST);
    }



    public String getSourceCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        String source = (String) vectorSource.elementAt(index);
        return source;
    }

    public void setSourceCellGroup(String netConnName, String source)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(source, index, COL_NUM_SOURCE);
    }


    public String getTargetCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        String target = (String) vectorTarget.elementAt(index);
        return target;
    }
    
    public String getGenerationStartCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        ConnectivityConditions cc = (ConnectivityConditions)vectorConnConds.elementAt(index);
        if (cc.getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET) return getSourceCellGroup(netConnName);
        else  return getTargetCellGroup(netConnName);
    }
    public String getGenerationEndCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        ConnectivityConditions cc = (ConnectivityConditions)vectorConnConds.elementAt(index);
        if (cc.getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET) return getTargetCellGroup(netConnName);
        else  return getSourceCellGroup(netConnName);
    }


    public void setTargetCellGroup(String netConnName, String target)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(target, index, COL_NUM_TARGET);
    }


    public SearchPattern getSearchPattern(String netConnName)
    {
        int index = vectorNames.indexOf(netConnName);
        return (SearchPattern) vectorSearchPattern.elementAt(index);
    }


    public void setSearchPattern(String netConnName, SearchPattern sp)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(sp, index, COL_NUM_SEARCH_PATTERN);
    }


    public GrowMode getGrowMode(String netConnName)
    {
        return GrowMode.getGrowModeJump();
        //int index = vectorNames.indexOf(netConnName);
        //return (GrowMode)vectorGrowMode.elementAt(index);
    }


    public void setGrowMode(String netConnName, GrowMode gm)
    {
        logger.logComment("Ignoring setGrowMode()...");
        //int index = vectorNames.indexOf(netConnName);
        //this.setValueAt(gm, index, this.COL_NUM_GROW_MODE);
    }


    public MaxMinLength getMaxMinLength(String netConnName)
    {
        int index = vectorNames.indexOf(netConnName);
        return (MaxMinLength)vectorMaxMin.elementAt(index);
    }


    public void setMaxMinLength(String netConnName, MaxMinLength mm)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(mm, index, COL_NUM_MAX_MIN);
    }




    public float getAPSpeed(String netConnName)
    {
        int index = this.vectorNames.indexOf(netConnName);
        return (Float)this.getValueAt(index, COL_NUM_AP_SPEED);
    }


    public void setAPSpeed(String netConnName, float aps)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(new Float(aps), index, COL_NUM_AP_SPEED);
    }




    public ConnectivityConditions getConnectivityConditions(String netConnName)
    {
        int index = vectorNames.indexOf(netConnName);
        return (ConnectivityConditions)vectorConnConds.elementAt(index);
    }


    public void setConnectivityConditions(String netConnName, ConnectivityConditions cc)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(cc, index, COL_NUM_CONN_CONDS);
    }



    public String getNetConnNameAt(int index)
    {
        String name = (String) vectorNames.elementAt(index);
        return name;
    }

    /**
     * Gets the netconn names where cellGroupName is source or target.
     * Used when deleting a cell group...
     */
    public Vector<String> getNetConnsUsingCellGroup(String cellGroupName)
    {
        Vector<String> netConns = new Vector<String>();

        for (int i = 0; i < vectorSource.size(); i++)
        {
            if ((vectorSource.elementAt(i)).equals(cellGroupName))
            {
                String netConnName = vectorNames.get(i);
                if (!netConns.contains(netConnName))
                    netConns.add(netConnName);
            }
        }

        for (int i = 0; i < vectorTarget.size(); i++)
        {
            if ((vectorTarget.elementAt(i)).equals(cellGroupName))
            {
                String netConnName = vectorNames.get(i);
                if (!netConns.contains(netConnName))
                    netConns.add(netConnName);
            }
        }
        return netConns;
    }

    public boolean isValidSimpleNetConn(String netConnName)
    {
        return vectorNames.contains(netConnName);
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public Hashtable getInternalData()
    {
        //System.out.println("getInternalData...");
        Hashtable<String, Vector> allInfo = new Hashtable<String, Vector>();
        allInfo.put(columnNames[COL_NUM_NETCONN_NAME], vectorNames);
        allInfo.put(columnNames[COL_NUM_SOURCE], vectorSource);
        allInfo.put(columnNames[COL_NUM_TARGET], vectorTarget);
        allInfo.put(columnNames[COL_NUM_SYNAPSE_LIST], vectorSynapseList);
        allInfo.put(columnNames[COL_NUM_SEARCH_PATTERN], vectorSearchPattern);
        //allInfo.put(columnNames[COL_NUM_GROW_MODE], vectorGrowMode);
        allInfo.put(columnNames[COL_NUM_MAX_MIN], vectorMaxMin);
        allInfo.put(columnNames[COL_NUM_CONN_CONDS], vectorConnConds);
        allInfo.put(columnNames[COL_NUM_AP_SPEED], vectorAPSpeed);
        return allInfo;
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public void setInternalData(Hashtable allInfo)
    {
        //System.out.println("setInternalData...");
        Vector vectorNamesTemp = (Vector)allInfo.get(columnNames[COL_NUM_NETCONN_NAME]);
        if (vectorNamesTemp!=null) vectorNames = vectorNamesTemp;

        Vector vectorSourceTemp = (Vector)allInfo.get(columnNames[COL_NUM_SOURCE]);
        if (vectorSourceTemp!=null) vectorSource = vectorSourceTemp;

        Vector vectorTargetTemp = (Vector)allInfo.get(columnNames[COL_NUM_TARGET]);
        if (vectorTargetTemp!=null) vectorTarget = vectorTargetTemp;

        Vector vectorSynapsePropertiesTemp = (Vector)allInfo.get(columnNames[COL_NUM_SYNAPSE_LIST]);
        if (vectorSynapsePropertiesTemp!=null) vectorSynapseList = vectorSynapsePropertiesTemp;

        Vector vectorSearchPatternTemp = (Vector)allInfo.get(columnNames[COL_NUM_SEARCH_PATTERN]);
        if (vectorSearchPatternTemp!=null) vectorSearchPattern = vectorSearchPatternTemp;

        //Vector vectorGrowModeTemp = (Vector)allInfo.get(columnNames[COL_NUM_GROW_MODE]);
        //if (vectorGrowModeTemp!=null) vectorGrowMode = vectorGrowModeTemp;

        Vector vectorMaxMinTemp = (Vector)allInfo.get(columnNames[COL_NUM_MAX_MIN]);
        if (vectorMaxMinTemp!=null) vectorMaxMin = vectorMaxMinTemp;

        Vector vectorConnCondsTemp = (Vector)allInfo.get(columnNames[COL_NUM_CONN_CONDS]);
        if (vectorConnCondsTemp!=null) vectorConnConds = vectorConnCondsTemp;

        Vector vectorAPSpeedTemp = (Vector)allInfo.get(columnNames[COL_NUM_AP_SPEED]);
        if (vectorAPSpeedTemp!=null) vectorAPSpeed = vectorAPSpeedTemp;



    }



}
