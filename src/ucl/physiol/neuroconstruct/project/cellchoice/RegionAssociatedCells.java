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

import java.util.*;

import ucl.physiol.neuroconstruct.project.*;
//import ucl.physiol.neuroconstruct.utils.*;


/**
 * Extension of CellChooser. Choosing this means all cells inside (or outside)
 * a specific region will be chosen
 *
 * @author Padraig Gleeson
 *  
 */

public class RegionAssociatedCells extends CellChooser
{
    //private static ClassLogger logger = new ClassLogger("RegionAssociatedCells");

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
                                                            "SampleRegion");

        parameterList[1] = new InternalStringFloatParameter(INSIDE_OR_OUT,
                                                            INSIDE_OR_OUT_DESC,
                                                            0);

        parameterList[1].setAcceptableParameterValues(new Object[]{new Float(0),new Float(1)});

    }
    public RegionAssociatedCells(String region)
    {
        super("Cell Chooser which picks all cells inside (or outside) a specific 3D region");

        parameterList = new InternalStringFloatParameter[2];

        parameterList[0] = new InternalStringFloatParameter(REGION_NAME,
                                                            REGION_NAME_DESC,
                                                            region);

        parameterList[1] = new InternalStringFloatParameter(INSIDE_OR_OUT,
                                                            INSIDE_OR_OUT_DESC,
                                                            0);

        parameterList[1].setAcceptableParameterValues(new Object[]{new Float(0),new Float(1)});

    }

    public String toNiceString()
    {
        if (parameterList[1].getValue() == 0)
        {
            return "Cells inside region: " + getParameterStringValue(REGION_NAME);
        }
        else if (parameterList[1].getValue() == 1)
        {
            return "Cells outside region: " + getParameterStringValue(REGION_NAME);
        }
        else
            return "Error with Cell chooser";

    }
    public String toShortString()
    {
        if (parameterList[1].getValue() == 0)
        {
            return "Cells in: " + getParameterStringValue(REGION_NAME);
        }
        else if (parameterList[1].getValue() == 1)
        {
            return "Cells not in: " + getParameterStringValue(REGION_NAME);
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


    protected int generateNextCellIndex() throws AllCellsChosenException
    {

        while(true)
        {

            if (nextIndexToCheck >=  cellPositions.size())
                throw new AllCellsChosenException();

            Region region = project.regionsInfo.getRegionObject(this.getParameterStringValue(REGION_NAME));

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

}
