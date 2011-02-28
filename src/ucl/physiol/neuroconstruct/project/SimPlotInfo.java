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

import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;

/**
 * Extension of AbstractTableModel for storing info on what plots to show during a simulation
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimPlotInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("SimPlotInfo");

    public final static int COL_NUM_REFERENCE = 0;
    public final static int COL_NUM_CELL_GROUP = 1;
    public final static int COL_NUM_CELL_NUM = 2;
    public final static int COL_NUM_CELL_SEGMENT_ID = 3;
    public final static int COL_NUM_VALUE = 4;
    public final static int COL_NUM_MIN = 5;
    public final static int COL_NUM_MAX = 6;
    public final static int COL_NUM_GRAPH_WINDOW = 7;
    public final static int COL_NUM_PLOT_OR_SAVE = 8;

    final String[] columnNames = new String[9];

    private Vector<SimPlot> allPlots = new Vector<SimPlot>();


    public SimPlotInfo()
    {
        columnNames[COL_NUM_REFERENCE] = new String("Plot reference");
        columnNames[COL_NUM_CELL_GROUP] = new String("Cell Group");
        columnNames[COL_NUM_CELL_NUM] = new String("Cell Number");
        columnNames[COL_NUM_CELL_SEGMENT_ID] = new String("Segment");
        columnNames[COL_NUM_VALUE] = new String("Value plotted");
        columnNames[COL_NUM_MIN] = new String("Minimum");
        columnNames[COL_NUM_MAX] = new String("Maximum");
        columnNames[COL_NUM_GRAPH_WINDOW] = new String("Plot Frame");
        columnNames[COL_NUM_PLOT_OR_SAVE] = new String("Plot and/or Save");
    }


    public int getRowCount()
    {
        return allPlots.size();
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
        SimPlot simPlot = allPlots.get(rowIndex);

        if (simPlot==null) return null;

        switch (columnIndex)
        {
            case COL_NUM_REFERENCE:
                return simPlot.getPlotReference();
            case COL_NUM_CELL_GROUP:
                return simPlot.getCellGroup();
            case COL_NUM_CELL_NUM:
                return simPlot.getCellNumber();
            case COL_NUM_CELL_SEGMENT_ID:
                return simPlot.getSegmentId();
            case COL_NUM_VALUE:
                return simPlot.getValuePlotted();
            case COL_NUM_MAX:
                return new Float(simPlot.getMaxValue());
            case COL_NUM_MIN:
                return new Float(simPlot.getMinValue());
            case COL_NUM_GRAPH_WINDOW:
                return simPlot.getGraphWindow();
            case COL_NUM_PLOT_OR_SAVE:
                return simPlot.getPlotAndOrSave();



            default:
                return null;
        }
    }


    public void addSimPlot(SimPlot simPlot)
    {
        allPlots.add(simPlot);

        this.fireTableStructureChanged();
    }




    public SimPlot getSimPlot(int index)
    {
        return allPlots.elementAt(index);
    }

    public void updateSimPlot(SimPlot simPlot)
    {
        for (int i = 0; i < allPlots.size(); i++)
        {
            SimPlot nextSimPlot =  allPlots.elementAt(i);
            if (simPlot.getPlotReference().equals(nextSimPlot.getPlotReference()))
            {
                allPlots.setElementAt(simPlot, i);
                this.fireTableStructureChanged();
                return;
            }
        }
        // if it's not found
        allPlots.add(simPlot);
    }

    public SimPlot getSimPlot(String simPlotRef)
    {
        for (int i = 0; i < allPlots.size(); i++)
        {
            SimPlot nextSimPlot =  allPlots.elementAt(i);
            if (nextSimPlot.getPlotReference().equals(simPlotRef))
            {
                return nextSimPlot;
            }
        }
        return null;
    }



    public void deleteSimPlot(SimPlot simPlot)
    {
        allPlots.remove(simPlot);
        this.fireTableStructureChanged();
        return;
    }


    public void deleteSimPlot(int index)
    {
        allPlots.removeElementAt(index);
        this.fireTableStructureChanged();
        return;
    }

    public void deleteAllSimPlots()
    {
        allPlots = new Vector<SimPlot>();
    }



    public Vector<String> getAllSimPlotRefs()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < allPlots.size(); i++)
        {
            SimPlot next = allPlots.elementAt(i);
            allNames.add(next.getPlotReference());
        }
        return allNames;
    }


    public Vector getAllGraphWindows()
    {
        Vector<String> allWins = new Vector<String>();
        for (int i = 0; i < allPlots.size(); i++)
        {
            SimPlot next = allPlots.elementAt(i);
            if (!allWins.contains(next.getGraphWindow()))
                allWins.add(next.getGraphWindow());
        }
        return allWins;
    }



    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public Vector<SimPlot> getAllSimPlots()
    {
        return allPlots;
    }




    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public void setAllSimPlots(Vector<SimPlot> allPlots)
    {
        this.allPlots = allPlots;
    }


}
