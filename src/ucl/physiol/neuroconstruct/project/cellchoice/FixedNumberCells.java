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

package ucl.physiol.neuroconstruct.project.cellchoice;

import java.util.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Extension of CellChooser. Choosing this means a certain fixed number of cells
 * in the Cell Group will be chosen at random. If the number is greater than the number in the
 * Cell Group, only the number in it will be returned
 *
 * @author Padraig Gleeson
 *  
 */

public class FixedNumberCells extends CellChooser
{
    private static ClassLogger logger = new ClassLogger("FixedNumberCells");


    public static final String MAX_NUM_CELLS = "NumberCells";
    public static final String MAX_NUM_CELLS_DESC = "Max number of cells which will be chosen";

    boolean[] cellsChosen;

    public FixedNumberCells()
    {
        super("Cell Chooser which picks cells at random up to a fixed number in the selected Cell Group");
        parameterList = new InternalStringFloatParameter[1];

        parameterList[0] = new InternalStringFloatParameter(MAX_NUM_CELLS,
                                                            MAX_NUM_CELLS_DESC,
                                                            3);

    }

    protected void reinitialise()
    {
        cellsChosen = new boolean[cellPositions.size()];
        for (int i = 0; i < cellsChosen.length; i++)
        {
            cellsChosen[i] = false;
        }
    }

    private int getNumberCellsChosen()
    {
        int num = 0;
        for (int i = 0; i < cellsChosen.length; i++)
        {
            if (cellsChosen[i]) num++;
        }
        return num;

    }

    public String toNiceString()
    {
        return (int)getParameterValue(MAX_NUM_CELLS) + " random cells";
    }

    protected int generateNextCellIndex() throws AllCellsChosenException
    {
        int numAlreadyChosen = getNumberCellsChosen();

        int myMaxNumberToChoose = (int)getParameterValue(MAX_NUM_CELLS);

        if (numAlreadyChosen >=  cellPositions.size() ||
            numAlreadyChosen >= myMaxNumberToChoose)
            throw new AllCellsChosenException();

        int chosenCellIndex = -1;

        while(chosenCellIndex < 0)
        {
            int randomIndex = ProjectManager.getRandomGenerator().nextInt(cellPositions.size());
            if (!cellsChosen[randomIndex]) chosenCellIndex = randomIndex;
        }
        cellsChosen[chosenCellIndex] = true;
        PositionRecord nextPosRecord = this.cellPositions.get(chosenCellIndex);

        return nextPosRecord.cellNumber;

    }

    public static void main(String[] args)
    {
        ArrayList<PositionRecord> cellPositions = new ArrayList<PositionRecord>();
        cellPositions.add(new PositionRecord(0,0,0,0));
        cellPositions.add(new PositionRecord(1,110,0,0));
        cellPositions.add(new PositionRecord(2,220,0,0));
        cellPositions.add(new PositionRecord(6,660,0,0));
        cellPositions.add(new PositionRecord(7,770,0,0));

        FixedNumberCells fixedNumCells = new FixedNumberCells();

        try
        {
            fixedNumCells.initialise(cellPositions);

            while(true)
            {
                logger.logComment("Next cell index found: "+fixedNumCells.generateNextCellIndex());
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
