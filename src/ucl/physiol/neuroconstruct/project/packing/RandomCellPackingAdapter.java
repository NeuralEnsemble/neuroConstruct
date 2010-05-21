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

import java.beans.*;
import java.io.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Cell packing adapter which places a number of cells in random locations in
 * the specified region
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomCellPackingAdapter extends CellPackingAdapter
{
    ClassLogger logger = new ClassLogger("RandomCellPackingAdapter");

    final int maximumNumberAllowed = 1;
    final int maxNumberOfTries = 70;

    public final static String EDGE_POLICY = "EdgePolicy";
    public final static String SELF_OVERLAP_POLICY = "Self Overlap Policy";
    public final static String CELL_NUMBER_POLICY = "CellNumber";

    public RandomCellPackingAdapter()
    {
        super("CellPacker for cells placed completely randomly in this region");

        //logger.setThisClassSilent(true);

        parameterList = new InternalParameter[4];
        parameterList[0] = new InternalParameter(EDGE_POLICY,
                                              "Value = 0: Soma centres within region, walls can extend beyond edge; Value = 1: Soma completely inside region",
                                              1);
        parameterList[1] = new InternalParameter(SELF_OVERLAP_POLICY,
                                              "Value = 0: Segments with finite volume in this Cell Group cannot overlap; Value = 1: All Segments in this group can overlap"
                                              , 1);
        parameterList[2] = new InternalParameter(CELL_NUMBER_POLICY,
                                              "Number of cells in group. Positive integer > 1.",
                                              maximumNumberAllowed);
        parameterList[3] = new InternalParameter(OTHER_OVERLAP_POLICY,
                                              "Value = 0: Segments with finite volume in this Cell Group will avoid existing cells from other groups; Value = 1: Existing cells will be ignored",
                                              1);

    }


    protected Point3f generateNextPosition() throws CellPackingException
    {

        logger.logComment("---      Being asked to generate a new position");

        if (getNumPosAlreadyTaken()>=getMaxNumberCells())
            throw new CellPackingException("Maximum number in region reached");

        Point3f proposedPoint = null;
        int numberOfTries = 0;

        logger.logComment("Generating next position...");

        float minXLoc = myRegion.getLowestXValue();
        float minYLoc = myRegion.getLowestYValue();
        float minZLoc = myRegion.getLowestZValue();

        float maxXLoc = myRegion.getHighestXValue();
        float maxYLoc = myRegion.getHighestYValue();
        float maxZLoc = myRegion.getHighestZValue();

        if (maxXLoc-minXLoc<0||maxYLoc-minYLoc<0||maxZLoc-minZLoc<0)
        {
            throw new CellPackingException("Diameter of cell is smaller than region");
        }


        while (proposedPoint==null && numberOfTries<maxNumberOfTries)
        {
            float newXLoc = minXLoc
                + (ProjectManager.getRandomGenerator().nextFloat() * (maxXLoc-minXLoc));
            float newYLoc = minYLoc
                + (ProjectManager.getRandomGenerator().nextFloat() * (maxYLoc-minYLoc));
            float newZLoc = minZLoc
                + (ProjectManager.getRandomGenerator().nextFloat() * (maxZLoc-minZLoc));

            proposedPoint = new Point3f(newXLoc, newYLoc, newZLoc);

            logger.logComment("Generated a new proposed point: "+ Utils3D.getShortStringDesc(proposedPoint));

            boolean satisfiesOverlapPolicy = true;

            if(overlappingAllowed())
            {
                satisfiesOverlapPolicy = true;
            }
            else
            {
                satisfiesOverlapPolicy
                    = !(doesCellCollideWithExistingCells(proposedPoint, myCell));
            }

            if (!(satisfiesOverlapPolicy))
                proposedPoint = null;

            if (proposedPoint != null && !(myRegion.isCellWithinRegion(proposedPoint, myCell, mustBeCompletelyInsideRegion())))
                proposedPoint = null;


            numberOfTries++;

        }
        if (proposedPoint!=null) return proposedPoint;
        else throw new CellPackingException("Maximum number of new positions attempted without successfully placing cell.");

    }

    private boolean mustBeCompletelyInsideRegion()
    {
        if (parameterList[0].value == 1) return true;
        else return false;
    }

    private boolean overlappingAllowed()
    {
        if (parameterList[1].value == 1) return true;
        else return false;
    }

    public int getMaxNumberCells()
    {
        return (int)parameterList[2].value;
    }
    
    public void setMaxNumberCells(int num)
    {
        parameterList[2].value = num;
    }

    @Override
    public boolean avoidOtherCellGroups()
    {
        return parameterList[3].value==0;
    }



    public void setParameter(String parameterName,
                             float parameterValue)  throws CellPackingException
    {
        if (parameterName.equals(EDGE_POLICY))
        {
            if (parameterValue == 0||parameterValue == 1)
                parameterList[0].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals(SELF_OVERLAP_POLICY))
        {
            if (parameterValue == 0 || parameterValue == 1)
                parameterList[1].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }
        else if (parameterName.equals(OTHER_OVERLAP_POLICY))
        {
            if (parameterValue == 0 || parameterValue == 1)
                parameterList[3].value = parameterValue;
            else
                throw new CellPackingException("Invalid Parameter value");
        }


        else if (parameterName.equals(CELL_NUMBER_POLICY))
        {
            int num = (int)parameterValue;
            if ((double)num!=parameterValue || num < 1)
                throw new CellPackingException("Invalid Parameter value");

            else parameterList[2].value = num;
        }

        else throw new CellPackingException("Not a valid Parameter name");

    };


    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("Random: ");

        sb.append("num: "+ getMaxNumberCells()+", ");

        sb.append("edge: ");
        if (mustBeCompletelyInsideRegion()) sb.append("1, ");
        else sb.append("0, ");

        sb.append("overlap: ");
        if (overlappingAllowed()) sb.append("1, ");
        else sb.append("0, ");

        sb.append("other overlap"+": "+(int)parameterList[3].value+"");




        return (sb.toString());
    }

    public String toNiceString()
    {
        StringBuffer sb = new StringBuffer();
        //String nameOfClass = this.getClass().getName();

        sb.append("Cells randomly placed in 3D, max cell number: ");

        sb.append(getMaxNumberCells());


        sb.append(" (must be completely inside region: " +mustBeCompletelyInsideRegion());


        sb.append(", can overlap with cells in this group: "+overlappingAllowed()+"");
        sb.append(", other groups: "+!avoidOtherCellGroups()+")");





        return (sb.toString());
    }


    public static void main(String args[])
    {

        SimpleCell pCell = new SimpleCell("Simple test cell");

        RandomCellPackingAdapter rand = new RandomCellPackingAdapter();

        rand.addRegionAndCellInfo(new RectangularBox(1,1,1, 1,1,1), pCell);

        try
        {
            rand.setParameter(CELL_NUMBER_POLICY, 66f);
        }
        catch (CellPackingException ex1)
        {
            ex1.printStackTrace();
        }

        System.out.println("Parameter: " + rand.getParameterList()[2].parameterName);
        System.out.println("Value: " + rand.getParameterList()[2].value);
        System.out.println("Description: " + rand.getDescription());
        System.out.println("String of Class: " + rand);


        XMLEncoder e = null;
        try
        {
            e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("c:\\temp\\rand.xml")));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        e.writeObject(rand);

        System.out.println("Saved: " + rand);
        e.close();

    }

}
