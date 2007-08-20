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

package ucl.physiol.neuroconstruct.project;

import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;

/**
 * deprecated. Any extra functionality incorporated into SimpleNetworkConnectionsInfo
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class ComplexConnectionsInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("ComplexConnectionsInfo");

    public final static int COL_NUM_COMP_CONN_NAME = 0;
    public final static int COL_NUM_SOURCE = 1;
    public final static int COL_NUM_TARGET = 2;
    public final static int COL_NUM_SYNAPSE_LIST = 3;
    public final static int COL_NUM_SYN_TARGET = 4;
    public final static int COL_NUM_SEARCH_PATTERN = 5;
    public final static int COL_NUM_GROW_MODE = 6;
    public final static int COL_NUM_MAX_MIN = 7;
    public final static int COL_NUM_CONN_CONDS = 8;

    final String[] columnNames = new String[9];

    Vector<String> vectorNames = new Vector<String>();
    Vector<String> vectorSource = new Vector<String>();
    Vector<String> vectorTarget = new Vector<String>();
    Vector vectorSynapseList = new Vector();
    Vector vectorSynTargetOption = new Vector();
    Vector vectorSearchPattern = new Vector();
    Vector vectorGrowMode = new Vector();
    Vector vectorMaxMin = new Vector();

    Vector vectorConnConds = new Vector();

    public final static int SYN_TARGET_UNIQUE = 0;
    public final static int SYN_TARGET_REUSE = 1;






    public ComplexConnectionsInfo()
    {
        logger.logComment("New ComplexConnectionsInfo created");
        columnNames[COL_NUM_COMP_CONN_NAME] = new String("Name");


        columnNames[COL_NUM_SOURCE] = new String("Source");
        columnNames[COL_NUM_TARGET] = new String("Target");
        columnNames[COL_NUM_SYNAPSE_LIST] = new String("Synapse List");
        columnNames[COL_NUM_SYN_TARGET] = new String("Syn Target Option");
        columnNames[COL_NUM_SEARCH_PATTERN] = new String("Search Pattern");
        columnNames[COL_NUM_CONN_CONDS] = new String("Connectivity Conditions");
        columnNames[COL_NUM_GROW_MODE] = new String("Grow mode");
        columnNames[COL_NUM_MAX_MIN] = new String("Max/min");

    }

    public int getRowCount()
    {
        return vectorNames.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch (columnIndex)
        {
            case COL_NUM_COMP_CONN_NAME:
                return vectorNames.elementAt(rowIndex);
            case COL_NUM_SYNAPSE_LIST:
                return vectorSynapseList.elementAt(rowIndex);
            case COL_NUM_SYN_TARGET:
                return vectorSynTargetOption.elementAt(rowIndex);
            case COL_NUM_SOURCE:
                return vectorSource.elementAt(rowIndex);
            case COL_NUM_TARGET:
                return vectorTarget.elementAt(rowIndex);
            case COL_NUM_SEARCH_PATTERN:
                return vectorSearchPattern.elementAt(rowIndex);
            case COL_NUM_CONN_CONDS:
                return vectorConnConds.elementAt(rowIndex);
            case COL_NUM_GROW_MODE:
                return vectorGrowMode.elementAt(rowIndex);
            case COL_NUM_MAX_MIN:
                return vectorMaxMin.elementAt(rowIndex);

            default:
                return null;
        }
    }

    public void setValueAt(Object value, int row, int col)
    {
        switch (col)
        {
            case COL_NUM_COMP_CONN_NAME:
                this.vectorNames.setElementAt((String)value , row);
                break;
            case COL_NUM_SYNAPSE_LIST:
                this.vectorSynapseList.setElementAt(value , row);
                break;
            case COL_NUM_SYN_TARGET:
                this.vectorSynTargetOption.setElementAt(value , row);
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
            case COL_NUM_GROW_MODE:
                this.vectorGrowMode.setElementAt((GrowMode)value , row);
                break;

            case COL_NUM_MAX_MIN:
                this.vectorMaxMin.setElementAt((MaxMinLength)value , row);
                break;


        }
        fireTableCellUpdated(row, col);
    }


    public void addRow(String name,
                       String source,
                       String target,
                       Vector synProps,
                       int synTargetOption,
                       SearchPattern searchPattern,
                       GrowMode growMode,
                       MaxMinLength maxMin,
                       ConnectivityConditions connConds)  throws NamingException
    {
        int countSoFar = vectorNames.size();

        vectorNames.add(countSoFar, name);
        vectorSynapseList.add(countSoFar, synProps);
        vectorSynTargetOption.add(countSoFar, new Integer(synTargetOption));
        vectorSource.add(countSoFar, source);
        vectorTarget.add(countSoFar, target);
        vectorSearchPattern.add(countSoFar, searchPattern);
        vectorConnConds.add(countSoFar, connConds);
        vectorGrowMode.add(countSoFar, growMode);
        vectorMaxMin.add(countSoFar, maxMin);

        this.fireTableRowsInserted(countSoFar, countSoFar);
    }


    public boolean deleteNetConn(int index)
    {
        if (index<0 || index>=vectorNames.size()) return false;
        vectorNames.removeElementAt(index);
        vectorSynapseList.removeElementAt(index);
        vectorSynTargetOption.removeElementAt(index);
        vectorSource.removeElementAt(index);
        vectorTarget.removeElementAt(index);
        vectorSearchPattern.removeElementAt(index);
        vectorGrowMode.removeElementAt(index);
        vectorMaxMin.removeElementAt(index);
        vectorConnConds.removeElementAt(index);


        this.fireTableRowsDeleted(index, index);
        return true;
    }


    public boolean deleteComplexConn(String complexConnName)
    {
        return deleteNetConn(vectorNames.indexOf(complexConnName));
    }



    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    public int getNumComplexConns()
    {
        return vectorNames.size();
    }

    public Vector getAllComplexConnNames()
    {
        return new Vector(vectorNames);
    }

    public Vector getSynapseList(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        Vector synPropList = (Vector) vectorSynapseList.elementAt(index);
        return synPropList;
    }
    public void setSynapseProperties(String complexConnName, Vector synPropList)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(synPropList, index, this.COL_NUM_SYNAPSE_LIST);
    }


    public String getSourceCellGroup(String complexConnName)
    {
        if (!vectorNames.contains(complexConnName)) return null;
        int index = vectorNames.indexOf(complexConnName);
        String source = (String) vectorSource.elementAt(index);
        return source;
    }

    public void setSourceCellGroup(String complexConnName, String source)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(source, index, this.COL_NUM_SOURCE);
    }


    public String getTargetCellGroup(String complexConnName)
    {
        if (!vectorNames.contains(complexConnName)) return null;
        int index = vectorNames.indexOf(complexConnName);
        String target = (String) vectorTarget.elementAt(index);
        return target;
    }


    public void setTargetCellGroup(String complexConnName, String target)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(target, index, this.COL_NUM_TARGET);
    }


    public SearchPattern getSearchPattern(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return (SearchPattern) vectorSearchPattern.elementAt(index);
    }


    public void setSearchPattern(String complexConnName, SearchPattern sp)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(sp, index, this.COL_NUM_SEARCH_PATTERN);
    }


    public GrowMode getGrowMode(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return (GrowMode)vectorGrowMode.elementAt(index);
    }


    public void setGrowMode(String complexConnName, GrowMode gm)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(gm, index, this.COL_NUM_GROW_MODE);
    }


    public int getSynTargetOption(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return ((Integer)vectorSynTargetOption.elementAt(index)).intValue();
    }


    public void setSynTargetOption(String complexConnName, int i)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(new Integer(i), index, this.COL_NUM_SYN_TARGET);
    }


    public MaxMinLength getMaxMinLength(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return (MaxMinLength)vectorMaxMin.elementAt(index);
    }


    public void setMaxMinLength(String complexConnName, MaxMinLength mm)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(mm, index, this.COL_NUM_MAX_MIN);
    }



    public ConnectivityConditions getConnectivityConditions(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return (ConnectivityConditions)vectorConnConds.elementAt(index);
    }


    public void setConnectivityConditions(String complexConnName, ConnectivityConditions cc)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(cc, index, this.COL_NUM_CONN_CONDS);
    }



    public String getComplexConnNameAt(int index)
    {
        String name = (String) vectorNames.elementAt(index);
        return name;
    }

    /**
     * Gets the complexConns names where cellGroupName is source or target.
     * Used when deleting a cell group...
     */
    public Vector getComplexConnsUsingCellGroup(String cellGroupName)
    {
        Vector complexConns = new Vector();

        for (int i = 0; i < vectorSource.size(); i++)
        {
            if (((String)vectorSource.elementAt(i)).equals(cellGroupName))
            {
                String complexConnName = (String)vectorNames.elementAt(i);
                if (!complexConns.contains(complexConnName))
                    complexConns.add(complexConnName);
            }
        }

        for (int i = 0; i < vectorTarget.size(); i++)
        {
            if (((String)vectorTarget.elementAt(i)).equals(cellGroupName))
            {
                String complexConnName = (String)vectorNames.elementAt(i);
                if (!complexConns.contains(complexConnName))
                    complexConns.add(complexConnName);
            }
        }

        return complexConns;
    }





    public boolean isValidComplexConn(String complexConnName)
    {
        return vectorNames.contains(complexConnName);
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public Hashtable getInternalData()
    {
        Hashtable allInfo = new Hashtable();
        allInfo.put(columnNames[COL_NUM_COMP_CONN_NAME], vectorNames);
        allInfo.put(columnNames[COL_NUM_SOURCE], vectorSource);
        allInfo.put(columnNames[COL_NUM_TARGET], vectorTarget);
        allInfo.put(columnNames[COL_NUM_SYNAPSE_LIST], vectorSynapseList);
        allInfo.put(columnNames[COL_NUM_SYN_TARGET], vectorSynTargetOption);
        allInfo.put(columnNames[COL_NUM_SEARCH_PATTERN], vectorSearchPattern);
        allInfo.put(columnNames[COL_NUM_GROW_MODE], vectorGrowMode);
        allInfo.put(columnNames[COL_NUM_MAX_MIN], vectorMaxMin);
        allInfo.put(columnNames[COL_NUM_CONN_CONDS], vectorConnConds);
        return allInfo;
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public void setInternalData(Hashtable allInfo)
    {
        Vector vectorNamesTemp = (Vector)allInfo.get(columnNames[COL_NUM_COMP_CONN_NAME]);
        if (vectorNamesTemp!=null) vectorNames = vectorNamesTemp;

        Vector vectorSourceTemp = (Vector)allInfo.get(columnNames[COL_NUM_SOURCE]);
        if (vectorSourceTemp!=null) vectorSource = vectorSourceTemp;

        Vector vectorTargetTemp = (Vector)allInfo.get(columnNames[COL_NUM_TARGET]);
        if (vectorTargetTemp!=null) vectorTarget = vectorTargetTemp;

        Vector vectorSynapsePropertiesTemp = (Vector)allInfo.get(columnNames[COL_NUM_SYNAPSE_LIST]);
        if (vectorSynapsePropertiesTemp!=null) vectorSynapseList = vectorSynapsePropertiesTemp;

        Vector vectorSynTargetOptionTemp = (Vector)allInfo.get(columnNames[COL_NUM_SYN_TARGET]);
        if (vectorSynTargetOptionTemp!=null) vectorSynapseList = vectorSynTargetOptionTemp;


        Vector vectorSearchPatternTemp = (Vector)allInfo.get(columnNames[COL_NUM_SEARCH_PATTERN]);
        if (vectorSearchPatternTemp!=null) vectorSearchPattern = vectorSearchPatternTemp;

        Vector vectorGrowModeTemp = (Vector)allInfo.get(columnNames[COL_NUM_GROW_MODE]);
        if (vectorGrowModeTemp!=null) vectorGrowMode = vectorGrowModeTemp;

        Vector vectorMaxMinTemp = (Vector)allInfo.get(columnNames[COL_NUM_MAX_MIN]);
        if (vectorMaxMinTemp!=null) vectorMaxMin = vectorMaxMinTemp;

        Vector vectorConnCondsTemp = (Vector)allInfo.get(columnNames[COL_NUM_CONN_CONDS]);
        if (vectorConnCondsTemp!=null) vectorConnConds = vectorConnCondsTemp;

    }



}
