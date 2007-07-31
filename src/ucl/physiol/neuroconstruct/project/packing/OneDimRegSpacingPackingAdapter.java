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
import ucl.physiol.neuroconstruct.cell.utils.*;

/**
 * Simple cell packing adapter. Places fixed number of cells in 1 dimension inside region
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class OneDimRegSpacingPackingAdapter extends CellPackingAdapter
{
    ClassLogger logger = new ClassLogger("OneDimRegSpacingPackingAdapter");


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

        float minXLoc;
        float minYLoc;
        float minZLoc;

        float maxXLoc;
        float maxYLoc;
        float maxZLoc;

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

        logger.logComment("minXLoc: "+ minXLoc+", maxXLoc: "+maxXLoc);

        if (maxXLoc-minXLoc<0||maxYLoc-minYLoc<0||maxZLoc-minZLoc<0)
        {
            throw new CellPackingException("Diameter of cell is smaller than region");
        }


        if (getNumPosAlreadyTaken() >= parameterList[NUMBER_PARAM].value)
        {
            throw new CellPackingException("All cells successfully placed");
        }

        if (parameterList[NUMBER_PARAM].value == 0)
        {
            throw new CellPackingException("Cell number set to zero!!");
        }

        else if (parameterList[NUMBER_PARAM].value == 1)
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

            if (getNumPosAlreadyTaken() == 0)
            {
                logger.logComment("This is first point...");
                proposedPoint = new Point3f(minXLoc,
                                            minYLoc,
                                            minZLoc);

            }
            else
            {
                Point3f lastPositionedPoint = (Point3f) getLastPosTaken();
                logger.logComment("Last point attempted at: " + lastPositionedPoint);

                if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_X)
                {
                    logger.logComment("Placing cell in x dim...");
                    float distanceApart = (maxXLoc - minXLoc) / (parameterList[NUMBER_PARAM].value-1);
                    proposedPoint = new Point3f(lastPositionedPoint);
                    proposedPoint.add(new Point3f(distanceApart, 0, 0));
                }

                else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Y)
                {
                    logger.logComment("Placing cell in y dim...");
                    float distanceApart = (maxYLoc - minYLoc) / (parameterList[NUMBER_PARAM].value-1);
                    proposedPoint = new Point3f(lastPositionedPoint);
                    proposedPoint.add(new Point3f(0, distanceApart, 0));
                }

                else if (parameterList[DIMENSION_PARAM].value == DIMENSION_PARAM_Z)
                {
                    logger.logComment("Placing cell in z dim...");
                    float distanceApart = (maxZLoc - minZLoc) / (parameterList[NUMBER_PARAM].value-1);
                    proposedPoint = new Point3f(lastPositionedPoint);
                    proposedPoint.add(new Point3f(0, 0, distanceApart));
                }

            }

            if (proposedPoint == null)
            throw new CellPackingException("Cannot successfully place cell.");
        }
        return proposedPoint;

    }

    private boolean mustBeCompletelyInsideRegion()
    {
        if (parameterList[EDGE_POLICY_PARAM].value == EDGE_POLICY_PARAM_NO_EXTEND)
            return true;
        else
            return false;
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

    public void reset()
    {
        super.reset();
        logger.logComment("------------------  Adapter being reset...");
    }



    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String nameOfClass = this.getClass().getName();

        sb.append(nameOfClass.substring(nameOfClass.lastIndexOf(".")+1)+ "[");

        sb.append("CellNumber: ");
        sb.append(parameterList[NUMBER_PARAM].value+ ", ");

        sb.append("Dimension: ");
        if (parameterList[DIMENSION_PARAM].value == 0) sb.append("x, ");
        else if (parameterList[DIMENSION_PARAM].value == 1) sb.append("y, ");
        else if (parameterList[DIMENSION_PARAM].value == 2) sb.append("z, ");


        sb.append("EdgePolicy: ");
        if (mustBeCompletelyInsideRegion()) sb.append("1, ");
        else sb.append("0, ");


        sb.append(OTHER_OVERLAP_POLICY+": "+parameterList[OTHER_OVERLAP_PARAM].value+"]");



        return (sb.toString());
    }


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

            ada.addRegionAndCellInfo(new RectangularBox(0, 0, 0, 10, 90, 90), pCell);

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
