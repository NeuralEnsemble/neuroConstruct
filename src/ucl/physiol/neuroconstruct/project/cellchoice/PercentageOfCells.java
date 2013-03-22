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
import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.project.InternalStringFloatParameter;


/**
 * Extension of CellChooser. Choosing this means a certain percentage of cells
 * in the Cell Group will be chosen at random.
 *
 * @author Padraig Gleeson
 *  
 */

public class PercentageOfCells extends CellChooser
{


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
    
    public PercentageOfCells(float percentage)
    {
        super("Cell Chooser which picks a certain percentage of cells at random from the selected Cell Group");
        parameterList = new InternalStringFloatParameter[1];
        
        if (percentage>100) 
            percentage = 100;
        if (percentage<0) 
            percentage = 0;

        parameterList[0] = new InternalStringFloatParameter(PERCENTAGE_CELLS,
                                                            PERCENTAGE_CELLS_DESC,
                                                            percentage);
    }

    public String toNiceString()
    {
        return getParameterValue(PERCENTAGE_CELLS) + "% of cells at random";
    }
    public String toShortString()
    {
        return "Rand "+getParameterValue(PERCENTAGE_CELLS) + "% of cells";
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
        ArrayList<PositionRecord> cellPositions = new ArrayList<PositionRecord>();
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
                 System.out.println("next: " + next);
            }
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
