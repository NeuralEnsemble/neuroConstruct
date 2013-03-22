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
import ucl.physiol.neuroconstruct.cell.utils.*;

/**
 * Simple cell packing adapter. Places fixed number of cells in 1 dimension inside region
 *
 * @author Padraig Gleeson
 *  
 */


public class OneDimRegSpacingPackingAdapter extends CellPackingAdapter
{
    ClassLogger logger = new ClassLogger("OneDimRegSpacingPackingAdapter");
    
    
    //Point3d lastProposedPoint = null;
    int numProposedPoints = 0;


    public static final int EDGE_POLICY_PARAM = 0;
    public static final int DIMENSION_PARAM = 1;
    public static final int NUMBER_PARAM = 2;
    public static final int OTHER_OVERLAP_PARAM = 3;

    public static final int EDGE_POLICY_PARAM_EXTEND = 0;
    public static final int EDGE_POLICY_PARAM_NO_EXTEND = 1;

    public static final int DIMENSION_PARAM_X = 0;
    public static final int DIMENSION_PARAM_Y = 1;
    public static final int DIMENSION_PARAM_Z = 2;



    public static final String EDGE_POLICY_PARAM_NAME = "EdgePolicy";
    public static final String DIMENSION_PARAM_NAME = "Dimension";
    public static final String NUMBER_PARAM_NAME = "Cell Number";

    public OneDimRegSpacingPackingAdapter()
    {
        super("Places a fixed number of cells at regular spacing in 1 dimension starting at lowest (x,y,z) point of box around region. "+
              "NOTE: This packing pattern is better for more abstract (as opposed to physically realistic) networks");


        parameterList = new InternalParameter[4];

        parameterList[EDGE_POLICY_PARAM] = new InternalParameter(EDGE_POLICY_PARAM_NAME,
                                                                 "Value = 0: Soma centres within region, walls can extend beyond edge; Value = 1: Soma completely inside region",
                                                                 EDGE_POLICY_PARAM_NO_EXTEND);

        parameterList[DIMENSION_PARAM] = new InternalParameter(DIMENSION_PARAM_NAME,
                                                               "Value = 0: Cells placed in x dimension starting at lowest (x,y,z) point; Value = 1: Cells placed in y dimension; Value = 2: Cells placed in z dimension",
                                                               DIMENSION_PARAM_X);

        parameterList[NUMBER_PARAM] = new InternalParameter(NUMBER_PARAM_NAME,
                                                            "Number of cells to place. Note, depending on region size, cells may overlap",
                                                            3);


        parameterList[OTHER_OVERLAP_PARAM] = new InternalParameter(OTHER_OVERLAP_POLICY,
                                                                   "Value = 0: Segments with finite volume in this Cell Group will avoid existing cells from other groups; Value = 1: Existing cells will be ignored",
                                                                   0);


    }

            

    protected Point3f generateNextPosition() throws CellPackingException
    {
        Point3f proposedPoint = null;

        logger.logComment("----   Generating next position. positionsAlreadyTaken.size() = "+getNumPosAlreadyTaken());

        float minXLoc, minYLoc, minZLoc, maxXLoc, maxYLoc, maxZLoc;

        if (mustBeCompletelyInsideRegion())
        {
            // limit the extent for possible location of soma centre to box which
            // will preclude soma wall impacting region boundary.

            minXLoc = myRegion.getLowestXValue() - CellTopologyHelper.getMinXExtent(myCell, true, false);
            minYLoc = myRegion.getLowestYValue() - CellTopologyHelper.getMinYExtent(myCell, true, false);
            minZLoc = myRegion.getLowestZValue() - CellTopologyHelper.getMinZExtent(myCell, true, false);

            maxXLoc = myRegion.getHighestXValue() - CellTopologyHelper.getMaxXExtent(myCell, true, false);
            maxYLoc = myRegion.getHighestYValue() - CellTopologyHelper.getMaxYExtent(myCell, true, false);
            maxZLoc = myRegion.getHighestZValue() - CellTopologyHelper.getMaxZExtent(myCell, true, false);
        }
        else
        {
            // in this case the soma centre can be anywhere inside the region..

            minXLoc = myRegion.getLowestXValue();
            minYLoc = myRegion.getLowestYValue();
            minZLoc = myRegion.getLowestZValue();

            maxXLoc = myRegion.getHighestXValue();
            maxYLoc = myRegion.getHighestYValue();
            maxZLoc = myRegion.getHighestZValue();

        }
        
        
        Point3f startPoint = new Point3f(minXLoc,
                                         minYLoc,
                                         minZLoc);

        logger.logComment("minXLoc: "+ minXLoc+", maxXLoc: "+maxXLoc);

        if (maxXLoc-minXLoc<0||maxYLoc-minYLoc<0||maxZLoc-minZLoc<0)
        {
            throw new CellPackingException("Diameter of cell is smaller than region");
        }


        if (getNumPosAlreadyTaken() >= getNumberCells())
        {
            throw new CellPackingException("All cells successfully placed");
        }

        if (getNumberCells() == 0)
        {
            throw new CellPackingException("Cell number set to zero!!");
        }

        else if (getNumberCells() == 1)
        {
            if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_X)
            {
                proposedPoint = new Point3f( (minXLoc + maxXLoc) / 2,
                                    minYLoc,
                                    minZLoc);

            }

            else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Y)
            {
                proposedPoint = new Point3f(minXLoc,
                                    (minYLoc + maxYLoc) / 2,
                                    minZLoc);

            }

            else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Z)
            {

                proposedPoint = new Point3f(minXLoc,
                                            minYLoc,
                                            (minZLoc + maxZLoc) / 2);
            }

        }
        else
        {
            
            //Point3f lastPositionedPoint = getLastPosTaken();

            if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_X)
            {
                float distanceApart = (maxXLoc - minXLoc) / (getNumberCells()-1);
                
                proposedPoint = new Point3f(startPoint);
                proposedPoint.add(new Point3f(distanceApart*numProposedPoints, 0, 0));
                
                logger.logComment("Placing one of "+getNumberCells()+" cells in x dim..."+distanceApart
                    +" apart, new: "+proposedPoint+", already proposed: "+numProposedPoints);
                
                if(proposedPoint.x > maxXLoc)
                    throw new CellPackingException("Reached end of x dimension");
            }

            else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Y)
            {
                logger.logComment("Placing cell in y dim...");
                float distanceApart = (maxYLoc - minYLoc) / (getNumberCells()-1);
                proposedPoint = new Point3f(startPoint);
                proposedPoint.add(new Point3f(0, distanceApart*numProposedPoints, 0));
                
                if(proposedPoint.y > maxYLoc)
                    throw new CellPackingException("Reached end of y dimension");
            }

            else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Z)
            {
                logger.logComment("Placing cell in z dim...");
                float distanceApart = (maxZLoc - minZLoc) / (getNumberCells()-1);
                proposedPoint = new Point3f(startPoint);
                proposedPoint.add(new Point3f(0, 0, distanceApart*numProposedPoints));
                if(proposedPoint.z > maxZLoc)
                    throw new CellPackingException("Reached end of z dimension");
            }

            

            if (proposedPoint == null)
                
            throw new CellPackingException("Cannot successfully place cell.");
        }
        //lastProposedPoint = new Point3d(proposedPoint);
        numProposedPoints++;
        
        return new Point3f(proposedPoint);

    }

    private boolean mustBeCompletelyInsideRegion()
    {
        if (parameterList[EDGE_POLICY_PARAM].value == EDGE_POLICY_PARAM_NO_EXTEND)
            return true;
        else
            return false;
    }
    
    public int getNumberCells()
    {
        return (int)parameterList[NUMBER_PARAM].value;
    }


    public void setParameter(String parameterName,
                             float parameterValue)  throws CellPackingException
    {
        if (parameterName.equals(EDGE_POLICY_PARAM_NAME))
        {
            if (parameterValue == EDGE_POLICY_PARAM_EXTEND || parameterValue == EDGE_POLICY_PARAM_NO_EXTEND)
                parameterList[EDGE_POLICY_PARAM].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals(DIMENSION_PARAM_NAME))
        {
            if (parameterValue == DIMENSION_PARAM_X ||
                parameterValue == DIMENSION_PARAM_Y ||
                parameterValue == DIMENSION_PARAM_Z)
            {
                parameterList[DIMENSION_PARAM].value = parameterValue;
            }
            else
                throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals(NUMBER_PARAM_NAME))
        {
            if (parameterValue == (int)parameterValue)
                parameterList[NUMBER_PARAM].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }

        else if (parameterName.equals(OTHER_OVERLAP_POLICY))
        {
            if (parameterValue == 0 || parameterValue == 1)
                parameterList[OTHER_OVERLAP_PARAM].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }



        else throw new CellPackingException("Not a valid Parameter name");

    };

    @Override
    public void reset()
    {
        super.reset();
        
        numProposedPoints = 0;
        logger.logComment("------------------  Adapter being reset...");
    }



    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("One dim: ");

        sb.append("num: ");
        sb.append(getNumberCells()+ ", ");

        sb.append("dim: ");
        if (parameterList[DIMENSION_PARAM].value == 0) sb.append("x, ");
        else if (parameterList[DIMENSION_PARAM].value == 1) sb.append("y, ");
        else if (parameterList[DIMENSION_PARAM].value == 2) sb.append("z, ");


        sb.append("edge: ");
        if (mustBeCompletelyInsideRegion()) sb.append("1, ");
        else sb.append("0, ");


        sb.append("other overlap"+": "+(int)parameterList[OTHER_OVERLAP_PARAM].value+"");



        return (sb.toString());
    }


    @Override
    public String toNiceString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("Cells arranged in 1D line in ");

        if (parameterList[DIMENSION_PARAM].value == 0) sb.append("X");
        else if (parameterList[DIMENSION_PARAM].value == 1) sb.append("Y");
        else if (parameterList[DIMENSION_PARAM].value == 2) sb.append("Z");


        sb.append(" dimension, cell number: ");

        sb.append(getNumberCells()+ ", ");

        sb.append(" (must be completely inside region: " +mustBeCompletelyInsideRegion());


        sb.append(", can overlap with cells in other group: "+!avoidOtherCellGroups()+")");



        return (sb.toString());
    }


    @Override
    public boolean avoidOtherCellGroups()
    {
        return parameterList[OTHER_OVERLAP_PARAM].value==0;
    }

    public static void main(String args[])
    {
        SimpleCell pCell = new SimpleCell("Test cell");

        OneDimRegSpacingPackingAdapter ada = new OneDimRegSpacingPackingAdapter();
        try
        {
            ada.setParameter(OneDimRegSpacingPackingAdapter.NUMBER_PARAM_NAME, 1);

            ada.addRegionAndCellInfo(new RectangularBox(0, 0, 0, 90, 90, 90), pCell);

            System.out.println("Parameter: " + ada.getParameterList()[0]);
            System.out.println("Description: " + ada.getDescription());
            System.out.println("toString: " + ada.toString());

            while (true)
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
