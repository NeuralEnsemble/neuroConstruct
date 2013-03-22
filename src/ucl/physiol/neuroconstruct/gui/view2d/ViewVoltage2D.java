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

import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Frame for showing 2D view of Cell Group(s) during simulation rerun
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class ViewVoltage2D extends View2DPlane implements VoltageEventListener
{
    ClassLogger logger = new ClassLogger("ViewVoltage2D");


    public ViewVoltage2D(String cellGroup,
                  ArrayList<PositionRecord> positions,
                  ViewingDirection viewingDir,
                  boolean standAlone)
    {
        super(cellGroup,  positions, viewingDir, standAlone);
    }




    public void updateVoltage(float voltage, String cellGroup, int cellNumber, boolean refresh)
    {
        //System.out.println("updateVoltage: "+voltage+", "+cellGroup+", "+cellNumber+", "+segmentId);
        logger.logComment("updateVoltage: "+voltage+", "+cellGroup+", "+cellNumber);
        if (this.cellGroup.equals(cellGroup))
        {
            Color c = SimulationRerunFrame.getLinearColor(voltage);
            viewCanvas.updateColour(c, cellGroup, cellNumber, refresh);
        }
    }




    public static void main(String[] args)
    {
        ArrayList<PositionRecord> positions = new ArrayList<PositionRecord>();


        positions.add(new PositionRecord(0,    0, 0, 0));
        positions.add(new PositionRecord(1,    1, 0, 6));
        positions.add(new PositionRecord(2,    0, 1, 12));
        positions.add(new PositionRecord(3,    10,2, 18));

        ViewVoltage2D view2d = new ViewVoltage2D("Test group", positions, ViewCanvas.X_Y_NEGZ_DIR, true);
        view2d.pack();
        view2d.validate();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = view2d.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        view2d.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);


        view2d.setVisible(true);

        view2d.updateVoltage(44, "Test group", 0, true);
        view2d.updateVoltage(-90, "Test group", 1, true);
    }

}
