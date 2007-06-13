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
import ucl.physiol.neuroconstruct.simulation.*;

/**
 * Extension of AbstractTableModel for storing info on electrophysiological inputs
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

@SuppressWarnings("serial")

public class ElecInputInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("ElecInputInfo");

    public final static int COL_NUM_REFERENCE = 0;
    public final static int COL_NUM_CELL_GROUP = 1;
    public final static int COL_NUM_CELL_NUM = 2;
    public final static int COL_NUM_CELL_SEGMENT_ID = 3;
    public final static int COL_NUM_CELL_INFO = 4;

    final String[] columnNames = new String[5];

    Vector<StimulationSettings> allStims = new Vector<StimulationSettings>();


    public ElecInputInfo()
    {
        columnNames[COL_NUM_REFERENCE] = new String("Reference");
        columnNames[COL_NUM_CELL_GROUP] = new String("Cell Group");
        columnNames[COL_NUM_CELL_NUM] = new String("Cells to choose");
        columnNames[COL_NUM_CELL_SEGMENT_ID] = new String("Segment");
        columnNames[COL_NUM_CELL_INFO] = new String("Info");
    }


    public int getRowCount()
    {
        return allStims.size();
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
        StimulationSettings stim = (StimulationSettings)allStims.elementAt(rowIndex);

        if (stim==null) return null;

        switch (columnIndex)
        {
            case COL_NUM_REFERENCE:
                return stim.getReference();
            case COL_NUM_CELL_GROUP:
                return stim.getCellGroup();
            case COL_NUM_CELL_NUM:
                return stim.getCellChooser().toString();
            case COL_NUM_CELL_SEGMENT_ID:
                return new Integer(stim.getSegmentID());
            case COL_NUM_CELL_INFO:
                return stim.toString();


            default:
                return null;
        }
    }


    public void addStim(StimulationSettings stim)
    {
        allStims.add(stim);

        this.fireTableStructureChanged();
    }

    public StimulationSettings getStim(String stimRef)
    {
        for (int i = 0; i < allStims.size(); i++)
        {
            StimulationSettings nextStim = (StimulationSettings)allStims.elementAt(i);

            if (nextStim.getReference().equals(stimRef))
            {
                return nextStim;
            }
        }
        return null;
    }





    public StimulationSettings getStim(int index)
    {
        return (StimulationSettings)allStims.elementAt(index);
    }

    public void updateStim(StimulationSettings stim)
    {
        for (int i = 0; i < allStims.size(); i++)
        {
            StimulationSettings nextStim = (StimulationSettings)allStims.elementAt(i);

            if (stim.getReference().equals(nextStim.getReference()))
            {
                allStims.setElementAt(stim, i);
                this.fireTableStructureChanged();
                return;
            }
        }
        // if it's not found
        allStims.add(stim);
    }
/*
    public void deleteStim(StimulationSettings stim)
    {
        allStims.remove(stim);
        this.fireTableRowsDeleted(index, index);
        return;
    }
*/

    public void deleteStim(int index)
    {
        allStims.removeElementAt(index);
        this.fireTableRowsDeleted(index, index);
        return;
    }




    public Vector<StimulationSettings> getAllStims()
    {
        return this.allStims;
    }


    public Vector<String> getAllStimRefs()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < allStims.size(); i++)
        {
            StimulationSettings nextStim = (StimulationSettings)allStims.elementAt(i);
            allNames.add(nextStim.getReference());
        }
        return allNames;
    }








    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public void setAllStims(Vector<StimulationSettings> allStims)
    {
        this.allStims = allStims;
    }


}
