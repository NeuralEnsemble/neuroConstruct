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

package ucl.physiol.neuroconstruct.gui.view2d;

import java.awt.*;
import java.util.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Frame for showing 2D view of synchrony of a cell rel to a cell group
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class ViewSynchro2D extends View2DPlane
{
    ClassLogger logger = new ClassLogger("ViewSynchro2D");

    Color minValColor = Color.blue;
    Color maxValColor = Color.green;

    float minValue = -100;
    float maxValue = 100;



    public ViewSynchro2D(String cellGroup,
                  ArrayList<PositionRecord> positions,
                  ViewingDirection viewingDir,
                  boolean standAlone)
    {
        super(cellGroup,  positions, viewingDir, standAlone);

        super.setTolerance(0);

        this.setTitle("Synchrony of cell in Cell Group: "+cellGroup);
    }



    public void updateValue(float value, String cellGroup, int cellNumber, boolean refresh)
    {
        double fractionAlong = (value - minValue) / (maxValue - minValue);

        fractionAlong = fractionAlong < 0 ? 0 : fractionAlong;
        fractionAlong = fractionAlong > 1 ? 1 : fractionAlong;

        if (this.cellGroup.equals(cellGroup))
        {
            Color thisColour = GeneralUtils.getFractionalColour(minValColor,
                                                                maxValColor,
                                                                fractionAlong);

            logger.logComment("update value: " + value + ", fractionAlong: " + fractionAlong + ", " + cellGroup + ", " +
                              cellNumber + ", thisColour: " + thisColour);

            viewCanvas.updateColour(thisColour, cellGroup, cellNumber, refresh);
        }
    }


    public void updateColour(Color color, String cellGroup, int cellNumber, boolean refresh)
    {

        logger.logComment("update color: " + color + ", " + cellGroup + ", " + cellNumber);

        if (this.cellGroup.equals(cellGroup))
        {
            viewCanvas.updateColour(color, cellGroup, cellNumber, refresh);
        }
    }



    public static void main(String[] args)
    {
        ArrayList<PositionRecord> positions = new ArrayList<PositionRecord>();


        positions.add(new PositionRecord(0,    0.5f, 0.5f, 1));
        positions.add(new PositionRecord(1,    1, 0.5f, 6));
        positions.add(new PositionRecord(2,    00.5f, 1, 12));
        positions.add(new PositionRecord(3,    10,2, 18));

        ViewSynchro2D view2d = new ViewSynchro2D("Test group", positions, ViewCanvas.X_Y_NEGZ_DIR, true);
        view2d.pack();
        view2d.validate();

        GuiUtils.centreWindow(view2d);

        view2d.setVisible(true);

        view2d.updateValue(0, "Test group", 0, true);
        view2d.updateValue(-50f, "Test group", 1, true);
        view2d.updateValue(99f, "Test group", 2, true);
        view2d.updateValue(1, "Test group", 3, true);
        //view2d.updateVoltage(-90, "Test group", 1, true);
    }

}
