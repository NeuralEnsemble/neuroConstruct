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

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Parameters for a stimulation to add to the simulation
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public abstract class StimulationSettings
{
    private static ClassLogger logger = new ClassLogger("StimulationSettings");

    protected String cellGroup;

    protected String reference;


    protected CellChooser cellChooser = new AllCells(); // a default...


    protected int segmentID = 0;
    protected float fractionAlong = 0.5f;


    public StimulationSettings()
    {
    }

    public StimulationSettings(String reference,
                               String cellGroup,
                               CellChooser cellChooser,
                               int segmentID)
    {
        this.reference = reference;
        this.cellGroup = cellGroup;
        this.cellChooser = cellChooser;
        this.segmentID = segmentID;
    }

    public abstract String toString();

    public abstract ElectricalInput getElectricalInput();

    public String getCellGroup()
    {
        return cellGroup;
    }



    public CellChooser getCellChooser()
    {
        //System.out.println("Cell chooser requested: "+ cellChooser + " in " + this.toString());
        return this.cellChooser;
    }


    public void setCellChooser(CellChooser cellChooser)
    {
        //System.out.println("Setting cell chooser: "+ cellChooser);
        this.cellChooser = cellChooser;
    }


    /**
     * Kept from an older version where a string was used to encode which cells to stimulate
     */
    public void setCellNumberString(String cellNumberString)
    {
        //System.out.println("Setting cellNumberString: "+ cellNumberString);
        if (cellNumberString.equals("*"))
        {
            this.cellChooser = new AllCells();

        }
        else if(cellNumberString.endsWith("%"))
        {
            String percent = cellNumberString.substring(0,cellNumberString.length()-1);
            try
            {
                Float percentage = Float.parseFloat(percent);
                this.cellChooser = new PercentageOfCells();
                cellChooser.setParameter(PercentageOfCells.PERCENTAGE_CELLS, percentage);


            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Error calculating the percentage of cells to stimulate from string: "
                                          +cellNumberString, ex, null);
            }
        }
        else
        {
            try
            {
                this.cellChooser = new IndividualCells();
                cellChooser.setParameter(IndividualCells.LIST_OF_CELLS, cellNumberString);


            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Error calculating the cell to stimulate from: "
                                          +cellNumberString, ex, null);
            }
        }

            //System.out.println("New input cells: "+ cellChooser.toString());

    }





    public void setCellGroup(String cellGroup)
    {
        this.cellGroup = cellGroup;
    }

    public int getSegmentID()
    {
        return segmentID;
    }
    public void setSegmentID(int segmentID)
    {
        this.segmentID = segmentID;
    }
    public String getReference()
    {
        return reference;
    }
    public void setReference(String reference)
    {
        this.reference = reference;
    }
    public float getFractionAlong()
    {
        return fractionAlong;
    }
    public void setFractionAlong(float fractionAlong)
    {
        this.fractionAlong = fractionAlong;
    }

}
