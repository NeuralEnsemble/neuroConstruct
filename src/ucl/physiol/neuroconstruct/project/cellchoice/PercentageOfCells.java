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

import ucl.physiol.neuroconstruct.project.PositionRecord;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.project.InternalStringFloatParameter;


/**
 * Extension of CellChooser. Choosing this means a certain percentage of cells
 * in the Cell Group will be chosen at random.
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class PercentageOfCells extends CellChooser
{
    private static ClassLogger logger = new ClassLogger("PercentageOfCells");

    //float myPercentage = 110;



    public static final String PERCENTAGE_CELLS = "PercentageCells";
    public static final String PERCENTAGE_CELLS_DESC = "Percentage of cells which will be chosen at random";


    int targetNumberToChoose = -1;

    boolean[] cellsChosen;

    public PercentageOfCells()
    {
        super("Cell Chooser which picks a certain percentage of cells at random from the selected Cell Group");
        parameterList = new InternalStringFloatParameter[1];

        parameterList[0] = new InternalStringFloatParameter(PERCENTAGE_CELLS,
                                                            PERCENTAGE_CELLS_DESC,
                                                            50);

    }

    public String toNiceString()
    {
        return getParameterValue(PERCENTAGE_CELLS) + "% of cells at random";
    }

    protected void reinitialise()
    {
        cellsChosen = new boolean[cellPositions.size()];
        for (int i = 0; i < cellsChosen.length; i++)
        {
            cellsChosen[i] = false;
        }

        float myPercentage = (int)getParameterValue(PERCENTAGE_CELLS);

        if (myPercentage<0) myPercentage = 0;
        if (myPercentage>100) myPercentage = 100;

        float targetNumberFloat = (float)cellPositions.size()*myPercentage/100;

        targetNumberToChoose = Math.round(targetNumberFloat);

        logger.logComment(targetNumberToChoose +" is close to "+myPercentage
                          +"% of "+cellPositions.size() + ", it's really: "+ targetNumberFloat);
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

    protected int generateNextCellIndex() throws AllCellsChosenException
    {
        int numAlreadyChosen = getNumberCellsChosen();

        if (numAlreadyChosen >=  cellPositions.size() ||
            numAlreadyChosen >= targetNumberToChoose)
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
        ArrayList<PositionRecord> cellPositions = new ArrayList();
        cellPositions.add(new PositionRecord(0,0,0,0));
        cellPositions.add(new PositionRecord(1,110,0,0));
        cellPositions.add(new PositionRecord(2,220,0,0));
        cellPositions.add(new PositionRecord(6,660,0,0));
        cellPositions.add(new PositionRecord(7,770,0,0));
        cellPositions.add(new PositionRecord(4,770,0,0));
        cellPositions.add(new PositionRecord(9,770,0,0));
        cellPositions.add(new PositionRecord(12,770,0,0));

        PercentageOfCells percentageCells = new PercentageOfCells();

        try
        {
            percentageCells.initialise(cellPositions);

            ArrayList<Integer> ordered = percentageCells.getOrderedCellList();

            for (Integer next: ordered )
            {
                logger.logComment("next: " + next);
            }
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
