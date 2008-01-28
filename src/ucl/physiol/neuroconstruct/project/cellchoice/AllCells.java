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
    private static ClassLogger logger = new ClassLogger("AllCells");

    int chosenCellIndex = 0;

    public AllCells()
    {
        super("Cell Chooser which picks all cells in the Cell Group");
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
