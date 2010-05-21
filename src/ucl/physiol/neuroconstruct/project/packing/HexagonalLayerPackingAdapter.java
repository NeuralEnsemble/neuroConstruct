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
//import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Creates a single layer of cell bodies in hexagonal formation (e.g. as found
 * with Golgi cells in the cerebellum)
 *
 * @author Padraig Gleeson
 *  
 */

public class HexagonalLayerPackingAdapter extends CellPackingAdapter
{
    ClassLogger logger = new ClassLogger("HexagonalLayerPackingAdapter");

    // this will be like positionsAlreadyTaken, but will include ALL the points
    // attempted, including ones removed with cancelPosition(Point3d point)
    Vector<Point3f> positionsAlreadyAttempted = new Vector<Point3f>();

    int zLayerNumber = 0;


    public HexagonalLayerPackingAdapter()
    {
        super("CellPacker for cells placed in a single layer in hexagonal formation. Cells will be packed on lowest y plane of Region. Note: only works with Regions with \"flat\" plane on lowest y face");

        logger.logComment("New HexagonalLayerPackingAdapter created...");
        parameterList = new InternalParameter[2];
        parameterList[0] = new InternalParameter("EdgePolicy",
            "Value = 0: Soma centre of first cell placed at lowest (x,y,z) point in region; Value = 1: Soma placed distance CentreSpacing/2 in x,y,z dims away from lowest point in region",
            1);
        parameterList[1] = new InternalParameter("CentreSpacing",
            "Cells will be packed so that their centres are this dist apart; Value must be > 0",
            10);

    }



    @Override
    public void reset()
    {
        super.reset();
        logger.logComment("------------------  Adapter being reset...");
        this.positionsAlreadyAttempted.removeAllElements();
        // reset...
        zLayerNumber = 0;
    }


    protected Point3f generateNextPosition() throws CellPackingException
    {
        Point3f proposedPoint = null;

        logger.logComment("Generating next position...");

        float yLocation;

        float minXLoc;
        float minZLoc;

        float maxXLoc;
        float maxYLoc;
        float maxZLoc;

        if (mustBeCompletelyInsideRegion())
        {
            // limit the extent for possible location of soma centre to box which
            // will preclude soma wall impacting region boundary.

            minXLoc = myRegion.getLowestXValue() + getCentreSpacing()/2;
            yLocation = myRegion.getLowestYValue() + getCentreSpacing()/2;
            minZLoc = myRegion.getLowestZValue() + getCentreSpacing()/2;

            maxXLoc = myRegion.getHighestXValue() - getCentreSpacing()/2;
            maxYLoc = myRegion.getHighestYValue() - getCentreSpacing()/2;
            maxZLoc = myRegion.getHighestZValue() - getCentreSpacing()/2;
        }
        else
        {
            // in this case the soma centre can be anywhere inside the region..

            minXLoc = myRegion.getLowestXValue();
            yLocation = myRegion.getLowestYValue();
            minZLoc = myRegion.getLowestZValue();

            maxXLoc = myRegion.getHighestXValue();
            maxYLoc = myRegion.getHighestYValue();
            maxZLoc = myRegion.getHighestZValue();

        }
        if (maxXLoc<=minXLoc||maxYLoc<=yLocation||maxZLoc<=minZLoc)
        {
            throw new CellPackingException("Diameter of cell is smaller than region");
        }

        if (getNumPosAlreadyTaken()==0 && positionsAlreadyAttempted.size()==0)
        {
            logger.logComment("This is first point...");
            proposedPoint = new Point3f(minXLoc,
                                        yLocation,
                                        minZLoc);

        }
        else
        {
            Point3f lastPositionedPoint = positionsAlreadyAttempted.lastElement();
            logger.logComment("Last point attempted at: " + lastPositionedPoint);

            float proposedNewXPos = lastPositionedPoint.x + getCentreSpacing();

            if (proposedNewXPos <= (maxXLoc))
            {
                logger.logComment("Positioning this cell with just an x displacement from the last...");
                proposedPoint = new Point3f(proposedNewXPos,
                                            lastPositionedPoint.y,
                                            lastPositionedPoint.z);
            }
            else
            {
                logger.logComment(">>>>>>>>>>>>>>>>>>>>>>>>>>   Need to place the cell in another dimension...");
                zLayerNumber++;

                // do the math...
                float increaseInZdir = getCentreSpacing() * ((float)Math.sqrt(3d)/2f);
                float increaseInXdir = getCentreSpacing() / 2f;

                float proposedNewZPos = lastPositionedPoint.z + increaseInZdir;

                if (proposedNewZPos <= (maxZLoc))
                {


                    //float division = (lastPositionedPoint.z - lowestZLocation) / increaseInZdir;
                    //int currentLayerNumber = (int) Math.ceil(division);

                    logger.logComment("Looks like we're in layer number: " + zLayerNumber);

                    if ( (zLayerNumber / 2) * 2 == zLayerNumber) // i.e. even layer...
                    {
                        proposedNewXPos = minXLoc;
                    }
                    else
                    {
                        proposedNewXPos = minXLoc + increaseInXdir;
                    }

                    proposedPoint = new Point3f(proposedNewXPos,
                                                yLocation,
                                                proposedNewZPos);

                }
                else
                {
                    logger.logComment("All dimensions tried...");
                    throw new CellPackingException("All dimensions tried...");
                }

            }
        }


        if (proposedPoint!=null  && myRegion.isCellWithinRegion(proposedPoint, myCell, mustBeCompletelyInsideRegion()))
        {
            logger.logComment("Adding point "+proposedPoint
                              +"to list of attempts, which already has "
                              + positionsAlreadyAttempted.size()
                              + " entries, compared to "
                              + getNumPosAlreadyTaken()
                              + " successful ones so far...");
            
            positionsAlreadyAttempted.add(proposedPoint);

            return proposedPoint;
        }
        else
        {
            logger.logComment("Found a valid position in XYZ space, but the cell's not inside the region...");
            // go again until all positions used up..
            positionsAlreadyAttempted.add(proposedPoint);
            return generateNextPosition();
        }

    }

    private boolean mustBeCompletelyInsideRegion()
    {
        if (parameterList[0].value == 1)
            return true;
        else
            return false;
    }


    private float getCentreSpacing()
    {
        return parameterList[1].value;
    }


    public void setParameter(String parameterName,
                             float parameterValue)  throws CellPackingException
    {
        if (parameterName.equals("EdgePolicy"))
        {
            if (parameterValue == 0 || parameterValue == 1)
                parameterList[0].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals("CentreSpacing"))
        {
            if (parameterValue >0) parameterList[1].value = parameterValue;
            else throw new CellPackingException("Invalid Parameter value");
        }
        else throw new CellPackingException("Not a valid Parameter name");

    };



    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String nameOfClass = this.getClass().getName();

        sb.append(nameOfClass.substring(nameOfClass.lastIndexOf(".")+1)+ "[");

        sb.append("EdgePolicy: ");
        if (mustBeCompletelyInsideRegion()) sb.append("1; ");
        else sb.append("0; ");

        sb.append("CentreSpacing: "+ getCentreSpacing() + "]");

        return (sb.toString());
    }

    @Override
    public String toNiceString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Cells arranged in hexagonal layer in XZ plane");


        sb.append(" (must be completely inside region: " +mustBeCompletelyInsideRegion());

        sb.append(", centre spacing: "+ getCentreSpacing() + ")");

        return (sb.toString());
    }


    public static void main(String args[])
    {

        SimpleCell pCell = new SimpleCell("Test cell");
        HexagonalLayerPackingAdapter ada = new HexagonalLayerPackingAdapter();
        ada.addRegionAndCellInfo(new RectangularBox(0,0,0,30,30,30), pCell);

        System.out.println("Parameter: " + ada.getParameterList()[0]);
        System.out.println("Description: " + ada.getDescription());




        try
        {
            ada.setParameter("EdgePolicy",0f);
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
