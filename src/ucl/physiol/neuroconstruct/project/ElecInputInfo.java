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
import ucl.physiol.neuroconstruct.simulation.*;

/**
 * Extension of AbstractTableModel for storing info on electrophysiological inputs
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class ElecInputInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("ElecInputInfo");

    public final static int COL_NUM_REFERENCE = 0;
    public final static int COL_NUM_CELL_GROUP = 1;
    public final static int COL_NUM_CELL_NUM = 2;
    //public final static int COL_NUM_CELL_SEGMENT_ID = 3;
    public final static int COL_NUM_CELL_SEGMENT_INFO = 3;
    public final static int COL_NUM_CELL_INFO = 4;

    final String[] columnNames = new String[5];

    Vector<StimulationSettings> allStims = new Vector<StimulationSettings>();


    public ElecInputInfo()
    {
        columnNames[COL_NUM_REFERENCE] = new String("Reference");
        columnNames[COL_NUM_CELL_GROUP] = new String("Cell Group");
        columnNames[COL_NUM_CELL_NUM] = new String("Cells to choose");
        //columnNames[COL_NUM_CELL_SEGMENT_ID] = new String("Segment");
        columnNames[COL_NUM_CELL_SEGMENT_INFO] = new String("Segment Info");
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
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        StimulationSettings stim = allStims.elementAt(rowIndex);

        if (stim==null) return null;

        switch (columnIndex)
        {
            case COL_NUM_REFERENCE:
                return stim.getReference();
            case COL_NUM_CELL_GROUP:
                return stim.getCellGroup();
            case COL_NUM_CELL_NUM:
                return stim.getCellChooser().toString();
            case COL_NUM_CELL_SEGMENT_INFO:
                return stim.getSegChooser();
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
            StimulationSettings nextStim = allStims.elementAt(i);

            if (nextStim.getReference().equals(stimRef))
            {
                return nextStim;
            }
        }
        return null;
    }





    public StimulationSettings getStim(int index)
    {
        return allStims.elementAt(index);
    }

    public void updateStim(StimulationSettings stim)
    {
        for (int i = 0; i < allStims.size(); i++)
        {
            StimulationSettings nextStim = allStims.elementAt(i);

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

    public void deleteAllStims()
    {
        allStims = new Vector<StimulationSettings>();
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
            StimulationSettings nextStim = allStims.elementAt(i);
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
