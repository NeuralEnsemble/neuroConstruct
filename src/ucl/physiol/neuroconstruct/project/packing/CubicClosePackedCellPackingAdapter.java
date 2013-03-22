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
import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Places cells in Cubic Close Packed formation. *
 * For more on Cubic Close Packing see:
 * <p>http://mathworld.wolfram.com/CubicClosePacking.html</p>
 * <p>http://www.ill.fr/dif/3D-crystals/packing.html</p>
 * <p>http://www.tg.rim.or.jp/~kanai/chemistry/cry3d-e.htm</p>
 *
 * @author Padraig Gleeson
 *  
 */

public class CubicClosePackedCellPackingAdapter extends CellPackingAdapter
{

    ClassLogger logger = new ClassLogger("CubicClosePackedCellPackingAdapter");

    // this will be like positionsAlreadyTaken, but will include ALL the points
    // attempted, including ones removed with cancelPosition(Point3d point)
    Vector<Point3f> positionsAlreadyAttempted = new Vector<Point3f>();

    int currentYLayerNumber = 0;


    public CubicClosePackedCellPackingAdapter()
    {
        super("Cell Packer for region with cells arranged in Cubic Close Packed (CCP) pattern");

        logger.logComment("CubicClosePackedCellPackingAdapter created...");

        parameterList = new InternalParameter[2];
        parameterList[0] = new InternalParameter("EdgePolicy",
            "Value = 0: Soma centres within region, sphere with EffectiveRadius can extend beyond edge; Value = 1: Sphere of EffectiveRadius completely inside region",
            1);
        parameterList[1] = new InternalParameter("EffectiveRadius",
            "Cells will be packed as if there is a sphere of this radius surrounding them; Value must be > 0",
            30);

        //logger.setThisClassSilent(true);

    }

    @Override
    public void reset()
    {
        super.reset();
        logger.logComment("------------------  Adapter being reset...");
        currentYLayerNumber = 0;
        this.positionsAlreadyAttempted.removeAllElements();
    }



    protected Point3f generateNextPosition() throws CellPackingException
    {
        // See note above on Cubic Close Packing...

        Point3f proposedPoint = null;

        logger.logComment("                      .............    Generating next CCP position...");


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

            minXLoc = myRegion.getLowestXValue() + getEffectiveRadius();
            minYLoc = myRegion.getLowestYValue() + getEffectiveRadius();
            minZLoc = myRegion.getLowestZValue() + getEffectiveRadius();

            maxXLoc = myRegion.getHighestXValue() - getEffectiveRadius();
            maxYLoc = myRegion.getHighestYValue() - getEffectiveRadius();
            maxZLoc = myRegion.getHighestZValue() - getEffectiveRadius();
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

        if (maxXLoc<minXLoc||maxYLoc<minYLoc||maxZLoc<minZLoc)
        {
            throw new CellPackingException("Diameter of cell is greater than region");
        }

        if (getNumPosAlreadyTaken()==0  && positionsAlreadyAttempted.size()==0)
        {
            logger.logComment("This is first point...");
            proposedPoint = new Point3f(minXLoc,
                                        minYLoc,
                                        minZLoc);

        }
        else
        {
            Point3f lastPositionedPoint = positionsAlreadyAttempted.lastElement();
            logger.logComment("Last point attempted at: "+ lastPositionedPoint);

            // Separation of planes is sqrt(2) times radius. This can be seen from
            // considering 4 spheres in a plane and a fifth on top. The centres form a
            // pyramid with a square base. All sides are 2r. The height of the pyramid
            // is the separation of our planes

            float separationOfLayers = (float)(getEffectiveRadius()*Math.sqrt(2));

      ///      float division = (lastPositionedPoint.y - lowestYLocation)/separationOfLayers;
      //      int currentLayerNumber = (int)Math.ceil(division);

            logger.logComment("We're in layer number: "+ currentYLayerNumber);

            float proposedNewXPos = lastPositionedPoint.x + (getEffectiveRadius() * 2f);


            logger.logComment("Checking if x val of "+proposedNewXPos+" is allowed...");

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

                // going in z direction here, simply due to cell group regions being built up in the y direction
                // i.e. filling the regions from the bottom layer up...

                float proposedNewZPos = lastPositionedPoint.z + (getEffectiveRadius()*2f);

                if ((currentYLayerNumber/2)*2 == currentYLayerNumber) // i.e. even layer...
                {
                    proposedNewXPos = minXLoc; // for CCP
                }
                else
                {
                    proposedNewXPos = minXLoc + getEffectiveRadius(); // CCP, xpos moved to middle of 2 spheres
                }
                logger.logComment("Checking if z val of "+proposedNewZPos+" is allowed...");

                if (proposedNewZPos <= (maxZLoc))
                {
                    logger.logComment("Positioning this cell with a z displacement from the last...");

                    proposedPoint = new Point3f(proposedNewXPos,
                                                lastPositionedPoint.y,
                                                proposedNewZPos);
                }
                else
                {
                    logger.logComment(">>>>>>>>>>>>>>>>>>>>>>>>>>    Need to place the cell in another dimension...");

                    ///int newLayerNumber = currentLayerNumber +1;
                    currentYLayerNumber++;

                    logger.logComment("Entering layer number: "+ currentYLayerNumber);

                    float proposedNewYPos;

                    proposedNewYPos = lastPositionedPoint.y + separationOfLayers;

                    logger.logComment("Checking if y val of "+proposedNewYPos+" is allowed...");

                    if (proposedNewYPos <= (maxYLoc))
                    {
                        float newXPosition;
                        float newZPosition;

                        if ((currentYLayerNumber/2)*2 == currentYLayerNumber) // i.e. even layer...
                        {
                            logger.logComment("xxxxxxxxxxxxxxxEven numbered layer: ("+currentYLayerNumber+")");
                            newXPosition = minXLoc; // for CCP...
                            newZPosition = minZLoc; // for CCP...
                        }
                        else
                        {
                            logger.logComment("xxxxxxxxxxxxxxOdd numbered layer: ("+currentYLayerNumber+")");
                            newXPosition = minXLoc + getEffectiveRadius(); // for CCP...
                            newZPosition = minZLoc + getEffectiveRadius(); // for CCP...
                        }

                        logger.logComment("Positioning this cell with a y displacement from the last of: "+ (proposedNewYPos-lastPositionedPoint.y));
                        proposedPoint = new Point3f(newXPosition,
                                                    proposedNewYPos,
                                                    newZPosition);

                    }
                    else
                    {
                        logger.logComment("All dimensions tried...");
                        throw new CellPackingException("All dimensions tried...");
                    }

                }
            }

        }


        if (proposedPoint!=null && myRegion.isCellWithinRegion(proposedPoint, myCell, mustBeCompletelyInsideRegion()))
        {
            logger.logComment("Adding point "+proposedPoint
                              +"to list of attempts, which already has "
                              + positionsAlreadyAttempted.size()
                              + "entries, compared to "
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


    private float getEffectiveRadius()
    {
        return parameterList[1].value;
    }



    public void setParameter(String parameterName, float parameterValue)
        throws CellPackingException
    {
        if (parameterName.equals("EdgePolicy"))
        {
            if (parameterValue == 0||parameterValue == 1)
                parameterList[0].value = parameterValue;
            else throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals("EffectiveRadius"))
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

        sb.append("EffectiveRadius: "+ getEffectiveRadius() + "]");

        return (sb.toString());

    }

    @Override
    public String toNiceString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("Cells arranged with Cubic Close Packing");

        sb.append(" (must be completely inside region: " +mustBeCompletelyInsideRegion());

        sb.append(", effective radius: "+ getEffectiveRadius() + "um)");

        return (sb.toString());

    }


    public static void main(String args[])
    {

        SimpleCell pCell = new SimpleCell("Test cell");
        CubicClosePackedCellPackingAdapter ada = new CubicClosePackedCellPackingAdapter();
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

        System.out.println(ada.toNiceString());

    }

}
