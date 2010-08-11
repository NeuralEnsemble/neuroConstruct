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

    public final static String X_POS = "X_position";
    public final static String Y_POS = "Y_position";
    public final static String Z_POS = "Z_position";
    public final static String RELATIVE = "Relative";


    public SinglePositionedCellPackingAdapter()
    {
        super("CellPacker for placing a single cell at a specified position");

        parameterList = new InternalParameter[4];

        parameterList[0] = new InternalParameter(X_POS,
                                              "Displacement in X direction from corner of region",
                                              0);
        parameterList[1] = new InternalParameter(Y_POS,
                                              "Displacement in Y direction from corner of region",
                                              0);
        parameterList[2] = new InternalParameter(Z_POS,
                                              "Displacement in Z direction from corner of region",
                                              0);
        parameterList[3] = new InternalParameter(RELATIVE,
                                              "Value = 1: Above coords are relative to lowest (x,y,z) point in this region, Value = 0, Coordinates are absolute",
                                              1);

    }

    public SinglePositionedCellPackingAdapter(float x, float y, float z)  throws CellPackingException
    {
        this();
        this.setParameter(X_POS, x);
        this.setParameter(Y_POS, y);
        this.setParameter(Z_POS, z);
        this.setParameter(RELATIVE, 0);
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


    @Override
    public boolean avoidOtherCellGroups()
    {
        return false;
    }


    public boolean isRelativeToRegion()
    {
        return this.parameterList[3].value==1;
    }


    public void setParameter(String parameterName,
                             float parameterValue)  throws CellPackingException
    {
        if (parameterName.equals(X_POS)) parameterList[0].value = parameterValue;
        else if (parameterName.equals(Y_POS)) parameterList[1].value = parameterValue;
        else if (parameterName.equals(Z_POS)) parameterList[2].value = parameterValue;
        else if (parameterName.equals(RELATIVE))
        {
            if (parameterValue == 0f || parameterValue == 1f)
            {
                parameterList[3].value = parameterValue;

            }
            else throw new CellPackingException("Invalid Parameter value");
        }

        else throw new CellPackingException("Not a valid Parameter name");

    };

    public void setPosition(float x, float y, float z)  throws CellPackingException
    {
        setParameter(X_POS, x);
        setParameter(Y_POS, y);
        setParameter(Z_POS, z);
    }


    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("Single cell: ");

        sb.append("("
                 + parameterList[0].value
                 + ", "
                 + parameterList[1].value
                 + ", "
                 + parameterList[2].value
                 + ")");

        if (isRelativeToRegion())
        {
            sb.append(" relative to region");
        }
        else
        {
            sb.append(" (absolute position)");
        }


        return (sb.toString());
    }

    @Override
    public String toNiceString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("A single cell placed at: ");

       sb.append("("
                 + parameterList[0].value
                 + ", "
                 + parameterList[1].value
                 + ", "
                 + parameterList[2].value
                 + ")");

        if (isRelativeToRegion())
        {
            sb.append(" relative to Region");
        }
        else
        {
            sb.append(" (absolute position)");
        }


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
