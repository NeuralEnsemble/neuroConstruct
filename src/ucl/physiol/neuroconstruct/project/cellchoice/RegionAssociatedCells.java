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

package ucl.physiol.neuroconstruct.project.cellchoice;

import java.util.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Extension of CellChooser. Choosing this means all cells inside (or outside)
 * a specific region will be chosen
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class RegionAssociatedCells extends CellChooser
{
    private static ClassLogger logger = new ClassLogger("RegionAssociatedCells");


    public static final String REGION_NAME = "RegionName";
    public static final String REGION_NAME_DESC = "Which of the 3D Regions is used";

    public static final String INSIDE_OR_OUT = "InsideOutside";
    public static final String INSIDE_OR_OUT_DESC = "If set to 0, all cells inside region will be chosen. "+
        "Set to 1 indicates all cells in that Cell Group outside the region will be chosen";


    int nextIndexToCheck = 0;


    private Project project = null;


    public RegionAssociatedCells()
    {
        super("Cell Chooser which picks all cells inside (or outside) a specific 3D region");

        parameterList = new InternalStringFloatParameter[2];

        parameterList[0] = new InternalStringFloatParameter(REGION_NAME,
                                                            REGION_NAME_DESC,
                                                            "Region_1");

        parameterList[1] = new InternalStringFloatParameter(INSIDE_OR_OUT,
                                                            INSIDE_OR_OUT_DESC,
                                                            0);

        parameterList[1].setAcceptableParameterValues(new Object[]{new Float(0),new Float(1)});

    }

    public String toNiceString()
    {
        if (parameterList[1].getValue() == 0)
        {
            return "Cells inside region: " + getParameterStringValue(this.REGION_NAME);
        }
        else if (parameterList[1].getValue() == 1)
        {
            return "Cells outside region: " + getParameterStringValue(this.REGION_NAME);
        }
        else
            return "Error with Cell chooser";

    }

    /**
     * As the regions info will be needed...
     */
    public void setProject(Project project)
    {
        this.project = project;
    }

    public void setRegionNames(Vector<String> regionNames)
    {
        Object[] regionNameObjs = new Object[regionNames.size()];

        regionNames.copyInto(regionNameObjs);

        parameterList[0].setAcceptableParameterValues(regionNameObjs);
    }

    protected void reinitialise()
    {
        nextIndexToCheck = 0;
    }

    /**
     * Overridden, to check the string...

    public void setParameter(String parameterName,
                             float parameterValue) throws CellChooserException
    {
        if (!parameterName.equals(this.INSIDE_OR_OUT))
        {
            throw new CellChooserException("No parameter with the name: " + parameterName + " found in " +
                                           this.getClass().getName());
        }

        Object[] acceptableValues


    }
*/

    protected int generateNextCellIndex() throws AllCellsChosenException
    {

        while(true)
        {

            if (nextIndexToCheck >=  cellPositions.size())
                throw new AllCellsChosenException();

            Region region = project.regionsInfo.getRegionObject(this.getParameterStringValue(this.REGION_NAME));

            PositionRecord nextPosRecord = this.cellPositions.get(nextIndexToCheck);

            if (parameterList[1].getValue()==0)
            {
                if (region.isPointInRegion(nextPosRecord.getPoint()))
                {
                    nextIndexToCheck++;
                    return nextPosRecord.cellNumber;
                }
            }
            else if (parameterList[1].getValue()==1)
            {
                if (!region.isPointInRegion(nextPosRecord.getPoint()))
                {
                    nextIndexToCheck++;
                    return nextPosRecord.cellNumber;
                }
            }

            nextIndexToCheck++;
        }


    }

    public static void main(String[] args)
    {


    }
}
