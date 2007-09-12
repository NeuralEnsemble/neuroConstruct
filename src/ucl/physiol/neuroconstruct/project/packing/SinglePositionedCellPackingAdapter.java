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

package ucl.physiol.neuroconstruct.project.packing;

import javax.vecmath.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Very simple cell packing adapter. Just places cells in cubic 3D grid
 *
 * @author Padraig Gleeson
 *  
 */


public class SinglePositionedCellPackingAdapter extends CellPackingAdapter
{
    ClassLogger logger = new ClassLogger("SinglePositionedCellPackingAdapter");


    public SinglePositionedCellPackingAdapter()
    {
        super("CellPacker for placing a single cell at a specified position");

        parameterList = new InternalParameter[4];

        parameterList[0] = new InternalParameter("X_position",
                                              "Displacement in X direction from corner of region",
                                              0);
        parameterList[1] = new InternalParameter("Y_position",
                                              "Displacement in Y direction from corner of region",
                                              0);
        parameterList[2] = new InternalParameter("Z_position",
                                              "Displacement in Z direction from corner of region",
                                              0);
        parameterList[3] = new InternalParameter("Relative",
                                              "Value = 1: Above coords are relative to lowest (x,y,z) point in this region, Value = 0, Coordinates are absolute",
                                              1);




    }



    protected Point3f generateNextPosition() throws CellPackingException
    {
        Point3f proposedPoint = null;

        logger.logComment("Generating next position...");

        if (getNumPosAlreadyTaken()>0)
        {
            throw new CellPackingException("Already a cell positioned...");
        }
        else
        {
            if (parameterList[3].value==0)
            {
                proposedPoint = new Point3f(parameterList[0].value,
                                          parameterList[1].value,
                                          parameterList[2].value);

            }
            else
            {
                proposedPoint = new Point3f(parameterList[0].value + myRegion.getLowestXValue(),
                                          parameterList[1].value+ myRegion.getLowestYValue(),
                                          parameterList[2].value + myRegion.getLowestZValue());

            }
        }


        return proposedPoint;
    }


    public boolean avoidOtherCellGroups()
    {
        return false;
    }




    public void setParameter(String parameterName,
                             float parameterValue)  throws CellPackingException
    {
        if (parameterName.equals("X_position")) parameterList[0].value = parameterValue;
        else if (parameterName.equals("Y_position")) parameterList[1].value = parameterValue;
        else if (parameterName.equals("Z_position")) parameterList[2].value = parameterValue;
        else if (parameterName.equals("Relative"))
        {
            if (parameterValue == 0f || parameterValue == 1f)
            {
                parameterList[3].value = parameterValue;

            }
            else throw new CellPackingException("Invalid Parameter value");
        }

        else throw new CellPackingException("Not a valid Parameter name");

    };


    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String nameOfClass = this.getClass().getName();

        sb.append(nameOfClass.substring(nameOfClass.lastIndexOf(".")+1)+ "[");

       sb.append("Point: ("
                 + parameterList[0].value
                 + ", "
                 + parameterList[1].value
                 + ", "
                 + parameterList[2].value
                 + ")]");


        return (sb.toString());
    }


    public static void main(String args[])
    {
        SimpleCell pCell = new SimpleCell("Test cell");

        SimpleRegularCellPackingAdapter ada = new SimpleRegularCellPackingAdapter();

        ada.addRegionAndCellInfo(new RectangularBox(0,0,0, 90,90,90), pCell);

        System.out.println("Parameter: " + ada.getParameterList()[0]);
        System.out.println("Description: " + ada.getDescription());


        try
        {
            while(true)
            {
                System.out.println("Point: " + ada.getNextPosition());
            }
        }
        catch (CellPackingException ex)
        {
            ex.printStackTrace();
        }

        System.out.println("Number of positions: "+ada.getCurrentNumberPositions());

    }

}
