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
 * Extension of CellChooser. Choosing this means a specified list of cell numbers
 * in the Cell Group will be chosen.
 *
 * @author Padraig Gleeson
 *  
 */

public class IndividualCells extends CellChooser
{
    private static ClassLogger logger = new ClassLogger("IndividualCells");

    //float myPercentage = 110;



    public static final String LIST_OF_CELLS = "CellList";
    public static final String LIST_OF_CELLS_DESC = "Comma separated string listing all cells which will be chosen. "
        +"If a number in the list is not in the generated cells, it will be ignored";

    int indexToReturnNext = 0;
    int[] allCellNumbersToReturn;

    public IndividualCells()
    {
        super("Cell Chooser which picks a specified comma separated list of cells from the selected Cell Group, e.g. 0 or 0, 2, 3");
        parameterList = new InternalStringFloatParameter[1];

        parameterList[0] = new InternalStringFloatParameter(LIST_OF_CELLS,
                                                            LIST_OF_CELLS_DESC,
                                                            "0");

    }
    
    public IndividualCells(String list)
    {
        super("Cell Chooser which picks a specified comma separated list of cells from the selected Cell Group, e.g. 0 or 0, 2, 3");
        parameterList = new InternalStringFloatParameter[1];

        parameterList[0] = new InternalStringFloatParameter(LIST_OF_CELLS,
                                                            LIST_OF_CELLS_DESC,
                                                            list);

    }



    protected void reinitialise()
    {
        indexToReturnNext = 0;

        // Note allCellNumbersToReturn filled in at setParameter stage
    }



    /**
     * Overridden, to check the string format...
     */
    public void setParameter(String parameterName,
                             String parameterStringValue) throws CellChooserException
    {
        //logger.logComment("Setting string parameter with the name: "+parameterName+" found in "+this.getClass().getName(), true);

        if (!parameterName.equals(LIST_OF_CELLS))
        {
            throw new CellChooserException("No parameter with the name: "+parameterName+" found in "+this.getClass().getName());
        }

        this.parameterList[0].setStringValue(parameterStringValue);

        generateCellNumbersToReturn();

/*
        String[] numbers = parameterStringValue.split(",");

        allCellNumbersToReturn  = new int[numbers.length];

        try
        {
            for (int i = 0; i < numbers.length; i++)
            {
                logger.logComment("Found a cell number to return: ("+ numbers[i] +")", true);
                int nextNum = Integer.parseInt(numbers[i].trim());
                allCellNumbersToReturn[i] = nextNum;
            }

        }
        catch (Exception ex)
        {
            throw new CellChooserException("Error parsing comma separated cell number list: " + parameterStringValue,ex);
        }*/

        return;

    }

    private void generateCellNumbersToReturn() throws CellChooserException
    {
        String[] numbers = this.getParameterStringValue(LIST_OF_CELLS).split(",");

        allCellNumbersToReturn  = new int[numbers.length];

        try
        {
            for (int i = 0; i < numbers.length; i++)
            {
                logger.logComment("Found a cell number to return: ("+ numbers[i] +")");
                int nextNum = Integer.parseInt(numbers[i].trim());
                allCellNumbersToReturn[i] = nextNum;
            }

        }
        catch (Exception ex)
        {
            throw new CellChooserException("Error parsing comma separated cell number list: " + getParameterStringValue(LIST_OF_CELLS),ex);
        }

    }

    public String toNiceString()
    {
        try
        {
            generateCellNumbersToReturn();
            String message = "Cell numbers: ";

            for (int i = 0; i < allCellNumbersToReturn.length; i++)
            {
                message+=allCellNumbersToReturn[i];
                if (i<allCellNumbersToReturn.length-1) message+= ", ";
            }
            return message;
        }
        catch (CellChooserException ex)
        {
            return ex.getMessage();
        }
    }

    public String toShortString()
    {
        try
        {
            generateCellNumbersToReturn();
            String message = "Cells: ";

            for (int i = 0; i < allCellNumbersToReturn.length; i++)
            {
                message+=allCellNumbersToReturn[i];
                if (i<allCellNumbersToReturn.length-1) message+= ", ";
            }
            return message;
        }
        catch (CellChooserException ex)
        {
            return ex.getMessage();
        }
    }


    public void setParameter(String parameterName,
                             float parameterValue) throws CellChooserException
    {
        ///logger.logComment("Setting float parameter with the name: "+parameterName+" found in "+this.getClass().getName(), true);

        throw new CellChooserException("No float storing parameter in " +
                                       this.getClass().getName());
    }



    protected int generateNextCellIndex() throws AllCellsChosenException, CellChooserException
    {
        if (allCellNumbersToReturn==null) generateCellNumbersToReturn();

        if (indexToReturnNext >= cellPositions.size())
        {
            logger.logComment("Reached same num as cell generated: " + cellPositions.size());
            throw new AllCellsChosenException();
        }

        if (indexToReturnNext >= allCellNumbersToReturn.length)
            throw new AllCellsChosenException();

        int toReturn = allCellNumbersToReturn[indexToReturnNext];

        indexToReturnNext++;

        return toReturn;

    }

    public static void main(String[] args)
    {
        ArrayList<PositionRecord> cellPositions = new ArrayList<PositionRecord>();
        cellPositions.add(new PositionRecord(0,0,0,0));
        cellPositions.add(new PositionRecord(1,110,0,0));
        cellPositions.add(new PositionRecord(2,220,0,0));
        cellPositions.add(new PositionRecord(6,660,0,0));
        cellPositions.add(new PositionRecord(7,770,0,0));

        IndividualCells cells = new IndividualCells();


        try
        {
            cells.setParameter(IndividualCells.LIST_OF_CELLS, "0,6,1");
            //cells.setParameter(IndividualCells.LIST_OF_CELLS, "1");
            cells.initialise(cellPositions);

            System.out.println("cells: " + cells);

            while(true)
            {
                logger.logComment("Next cell index found: "+cells.generateNextCellIndex());
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
