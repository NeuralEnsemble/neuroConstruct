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

import java.util.*;


import java.awt.*;
import javax.swing.table.*;

import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Extension of AbstractTableModel used for storing the Cell Group info
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class CellGroupsInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("CellGroupsInfo");

    public final static int COL_NUM_CELLGROUPNAME = 0;
    public final static int COL_NUM_CELLTYPE = 1;
    public final static int COL_NUM_REGIONNAME = 2;
    public final static int COL_NUM_COLOUR = 3;
    public final static int COL_NUM_PACKING_ADAPTER = 4;
    public final static int COL_NUM_PRIORITY = 5;

    String[] columnNames = new String[6];


    Vector<String> vectorCellGroupNames = new Vector<String>();
    Vector<String> vectorCellTypes = new Vector<String>();
    Vector<String> vectorRegionNames = new Vector<String>();
    Vector<Color> vectorColours = new Vector<Color>();
    Vector<CellPackingAdapter> vectorPackingAdapter = new Vector<CellPackingAdapter>();
    Vector<Integer> vectorPriority = new Vector<Integer>();


    public CellGroupsInfo()
    {
        columnNames[COL_NUM_CELLGROUPNAME] = new String("Cell Group Name");
        columnNames[COL_NUM_CELLTYPE] = new String("Cell Type");
        columnNames[COL_NUM_REGIONNAME] = new String("Region Name");
        columnNames[COL_NUM_COLOUR] = new String("Colour");
        columnNames[COL_NUM_PACKING_ADAPTER] = new String("Cell Packing Adapter");
        columnNames[COL_NUM_PRIORITY] = new String("Priority");
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return vectorCellGroupNames.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public CellPackingAdapter getCellPackingAdapter(String cellGroupName)
    {
        int indexCellGroup = vectorCellGroupNames.indexOf(cellGroupName);

        if (indexCellGroup < 0)
            return null;

        CellPackingAdapter adapter = (CellPackingAdapter) vectorPackingAdapter.elementAt(indexCellGroup);
        return adapter;
    }


    public Color getColourOfCellGroup(String cellGroupName)
    {
        int indexCellGroup = vectorCellGroupNames.indexOf(cellGroupName);

        if (indexCellGroup<0) return null;

        Color groupColour = (Color)vectorColours.elementAt(indexCellGroup);
        return groupColour;
    }

    public String getRegionName(String cellGroupName)
    {
        int indexCellGroup = vectorCellGroupNames.indexOf(cellGroupName);

        if (indexCellGroup<0) return null;

        String regionName = (String)vectorRegionNames.elementAt(indexCellGroup);


        return regionName;
    }



    public Object getValueAt(int row, int col)
    {
        switch (col)
        {
            case COL_NUM_CELLGROUPNAME:
                return (String)this.vectorCellGroupNames.elementAt(row);

            case COL_NUM_CELLTYPE:
                return (String)this.vectorCellTypes.elementAt(row);

            case COL_NUM_REGIONNAME:
                return (String)this.vectorRegionNames.elementAt(row);

            case COL_NUM_COLOUR:
                return (Color)this.vectorColours.elementAt(row);

            case COL_NUM_PACKING_ADAPTER:
                return (CellPackingAdapter)this.vectorPackingAdapter.elementAt(row);

            case COL_NUM_PRIORITY:
                if (vectorPriority.size()<vectorCellGroupNames.size())
                {
                    vectorPriority = new Vector<Integer>();

                    for (int i = 0; i < vectorCellGroupNames.size(); i++)
                    {
                        vectorPriority.add(10  - i);
                    }
                }
                return vectorPriority.elementAt(row);

            default:
                return null;
        }
    }

    public ArrayList<String> getAllCellGroupNames()
    {
        return new ArrayList<String>(vectorCellGroupNames);
    }

    public String[] getAllCellGroupNamesArray()
    {
        String[] names = new String[vectorCellGroupNames.size()];
        vectorCellGroupNames.copyInto(names);
        return names;
    }


    public int getNumberCellGroups()
    {
        return vectorCellGroupNames.size();
    }


    public boolean isCellEditable(int row, int col)
    {
        if (col == COL_NUM_COLOUR || col == COL_NUM_PACKING_ADAPTER/* || col == COL_NUM_ENABLED*/) return true;
        else return false;
    }

    public void setValueAt(Object value, int row, int col)
    {
        logger.logComment("Setting row: "+row+", col: "+col + ", to: "+ value);
        switch (col)
        {
            case COL_NUM_CELLGROUPNAME:
                this.vectorCellGroupNames.setElementAt((String)value , row);
                break;
            case COL_NUM_CELLTYPE:
                this.vectorCellTypes.setElementAt((String)value , row);
                break;
            case COL_NUM_REGIONNAME:
                this.vectorRegionNames.setElementAt((String)value , row);
                break;
            case COL_NUM_COLOUR:
                this.vectorColours.setElementAt((Color)value , row);
                break;
            case COL_NUM_PACKING_ADAPTER:
                vectorPackingAdapter.setElementAt((CellPackingAdapter)value , row);
                break;

            case COL_NUM_PRIORITY:
                vectorPriority.setElementAt((Integer)value , row);
                break;
        }
        fireTableCellUpdated(row, col);
    }



    public void deleteCellGroup(String cellGroupName)
    {
        deleteCellGroup(vectorCellGroupNames.indexOf(cellGroupName));
    }

    public void deleteCellGroup(int index)
    {

        logger.logComment("Contents before: " + printSimpleContents());
        vectorCellGroupNames.removeElementAt(index);
        vectorCellTypes.removeElementAt(index);
        vectorColours.removeElementAt(index);
        vectorPackingAdapter.removeElementAt(index);
        vectorRegionNames.removeElementAt(index);
        vectorPriority.removeElementAt(index);

        this.fireTableRowsDeleted(index, index);

        logger.logComment("Contents after: " + printSimpleContents());

    }

    private String printSimpleContents()
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < vectorCellGroupNames.size(); i++)
        {
            sb.append("Cell Group "
                      +i
                      +": ("+vectorCellGroupNames.elementAt(i)
                      +", type: "
                      +vectorCellTypes.elementAt(i)+"), ");
        }
        return sb.toString();
    }

    public void addRow(String cellGroupName,
                       String cellType,
                       String regionName,
                       Color colour,
                       CellPackingAdapter adapter,
                       int priority)
        throws NamingException
    {
        if (vectorCellGroupNames.contains(cellGroupName))
            throw new NamingException("The Cell Group name: "+cellGroupName+" has already been used");

        int cellGroupCount = this.vectorCellGroupNames.size();

        this.vectorCellGroupNames.add(cellGroupCount, cellGroupName);
        this.vectorCellTypes.add(cellGroupCount, cellType);
        this.vectorRegionNames.add(cellGroupCount, regionName);
        this.vectorColours.add(cellGroupCount, colour);
        this.vectorPackingAdapter.add(cellGroupCount, adapter);
        this.vectorPriority.add(cellGroupCount, new Integer(priority));

        this.fireTableRowsInserted(cellGroupCount, cellGroupCount);
    }

    public String getCellType(String cellGroupName)
    {
        logger.logComment("Request for Cell Type in group: "+ cellGroupName);
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        if (index < 0) return null;
        String cellType = (String)vectorCellTypes.elementAt(index);
        return cellType;
    }

    public int getPriority(String cellGroupName)
    {
        logger.logComment("Request for Cell Type in group: "+ cellGroupName);
        //System.out.println("vectorPriority: "+vectorPriority);
        int index = this.vectorCellGroupNames.indexOf(cellGroupName);
        if (index < 0) return -1;
        Integer priority = (Integer)this.getValueAt(index, COL_NUM_PRIORITY);
        return priority;
    }





    public boolean isValidCellGroup(String cellGroupName)
    {
        if (vectorCellGroupNames.contains(cellGroupName))
        {
            return true;
        }
        else return false;
    }

    public String getCellGroupNameAt(int index)
    {
        String name = (String) vectorCellGroupNames.elementAt(index);
        return name;
    }

    public void setColourOfCellGroup(String cellGroupName, Color c)
    {
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(c, index, COL_NUM_COLOUR);
    }


    public void setCellGroupPriority(String cellGroupName, int priority)
    {
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(new Integer(priority), index, COL_NUM_PRIORITY);
    }


    public void setRegion(String cellGroupName, String regionName)
    {
        logger.logComment("Setting region: "+ regionName);
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(regionName, index, COL_NUM_REGIONNAME);
    }



    public void setPriority(String cellGroupName, int priority)
    {
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(new Integer(priority), index, COL_NUM_PRIORITY);
    }


    public void setCellType(String cellGroupName, String cellType)
    {
        logger.logComment("Setting cellType: "+ cellType);
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(cellType, index, COL_NUM_CELLTYPE);
    }


    public void setAdapter(String cellGroupName, CellPackingAdapter adapter)
    {
        int index = vectorCellGroupNames.indexOf(cellGroupName);
        this.setValueAt(adapter, index, COL_NUM_PACKING_ADAPTER);
    }



    /**
     * Gets the cellgroups names which are in regionName
     */
    public Vector<String> getCellGroupsInRegion(String regionName)
    {
        Vector<String> cellGroups = new Vector<String>();

        for (int i = 0; i < vectorRegionNames.size(); i++)
        {
            if ( ( (String) vectorRegionNames.elementAt(i)).equals(regionName))
            {
                String cellGroupName = (String) vectorCellGroupNames.elementAt(i);
                if (!cellGroups.contains(cellGroupName))
                    cellGroups.add(cellGroupName);
            }
        }

        return cellGroups;
    }


    /**
     * Gets the cellgroups names use cell type specified
     */
    public Vector<String> getCellGroupsUsingCellType(String cellType)
    {
        Vector<String> cellGroups = new Vector<String>();

        for (int i = 0; i < vectorCellTypes.size(); i++)
        {
            if ( ( (String) vectorCellTypes.elementAt(i)).equals(cellType))
            {
                String cellGroupName = vectorCellGroupNames.elementAt(i);
                if (!cellGroups.contains(cellGroupName))
                    cellGroups.add(cellGroupName);
            }
        }

        return cellGroups;
    }



    /**
     * Added to allow storing of data by XMLEncoder
     */
    public Hashtable<String, Vector> getInternalData()
    {
        Hashtable<String, Vector> allInfo = new Hashtable<String, Vector>();
        allInfo.put(columnNames[COL_NUM_CELLGROUPNAME], vectorCellGroupNames);
        allInfo.put(columnNames[COL_NUM_CELLTYPE], vectorCellTypes);
        allInfo.put(columnNames[COL_NUM_REGIONNAME], vectorRegionNames);
        allInfo.put(columnNames[COL_NUM_COLOUR], vectorColours);
        allInfo.put(columnNames[COL_NUM_PACKING_ADAPTER], vectorPackingAdapter);
        allInfo.put(columnNames[COL_NUM_PRIORITY], vectorPriority);
        return allInfo;
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public void setInternalData(Hashtable<String, Vector> allInfo)
    {
        Vector<String> vectorCellGroupNamesTemp = (Vector<String>)allInfo.get(columnNames[COL_NUM_CELLGROUPNAME]);
        if (vectorCellGroupNamesTemp != null) vectorCellGroupNames = vectorCellGroupNamesTemp;

        Vector vectorCellTypesTemp = (Vector) allInfo.get(columnNames[COL_NUM_CELLTYPE]);
        if (vectorCellTypesTemp != null) vectorCellTypes = vectorCellTypesTemp;

        Vector vectorRegionNamesTemp = (Vector) allInfo.get(columnNames[COL_NUM_REGIONNAME]);
        if (vectorRegionNamesTemp != null) vectorRegionNames = vectorRegionNamesTemp;

        Vector vectorColoursTemp = (Vector) allInfo.get(columnNames[COL_NUM_COLOUR]);
        if (vectorColoursTemp != null) vectorColours = vectorColoursTemp;

        Vector vectorPackingAdapterTemp = (Vector) allInfo.get(columnNames[COL_NUM_PACKING_ADAPTER]);
        if (vectorPackingAdapterTemp != null) vectorPackingAdapter = vectorPackingAdapterTemp;

        Vector vectorPriorityTemp = (Vector) allInfo.get(columnNames[COL_NUM_PRIORITY]);
        if (vectorPriorityTemp != null) vectorPriority = vectorPriorityTemp;


    }



}
