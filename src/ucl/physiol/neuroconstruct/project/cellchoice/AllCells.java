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

package ucl.physiol.neuroconstruct.project.cellchoice;

import ucl.physiol.neuroconstruct.project.PositionRecord;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.project.InternalStringFloatParameter;


/**
 * Extension of CellChooser. Choosing this means all cells in the Cell Group
 * will be chosen, e.g. for stimulation
 *
 * @author Padraig Gleeson
 *  
 */

public class AllCells extends CellChooser
{

    int chosenCellIndex = 0;

    public AllCells()
    {
        super("Cell Chooser which picks all cells in the Cell Group");
        
        logger = new ClassLogger("AllCells");
        parameterList = new InternalStringFloatParameter[0];
    }

    protected void reinitialise()
    {
        chosenCellIndex = 0;
    }

    protected int generateNextCellIndex() throws AllCellsChosenException
    {
        if (chosenCellIndex >=  cellPositions.size())
            throw new AllCellsChosenException();

        PositionRecord nextPosRecord = this.cellPositions.get(chosenCellIndex);

        chosenCellIndex++;

        return nextPosRecord.cellNumber;

    }

    public String toNiceString()
    {
        return "All cells";
    }
    
    public String toShortString()
    {
        return "All cells";
    }


    @Override
    public String toString()
    {
        return "All cells";
    }

    public static void main(String[] args)
    {
        ArrayList<PositionRecord> cellPositions = new ArrayList<PositionRecord>();
        cellPositions.add(new PositionRecord(0,0,0,0));
        cellPositions.add(new PositionRecord(1,110,0,0));
        cellPositions.add(new PositionRecord(2,220,0,0));
        cellPositions.add(new PositionRecord(6,660,0,0));
        cellPositions.add(new PositionRecord(7,770,0,0));

        AllCells allcells = new AllCells();

        try
        {
            allcells.initialise(cellPositions);

            while(true)
            {
                logger.logComment("Next cell index found: "+allcells.generateNextCellIndex());
            }
        }
        catch (AllCellsChosenException ex)
        {
            logger.logComment("Found all: "+ex.getMessage());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
