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

package ucl.physiol.neuroconstruct.j3D;

import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.picking.behaviors.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Extension of PickMouseBehavior to allow sections to be picked in the 3D Panels
 *
 * @author Padraig Gleeson
 *  
 */


public class SectionPicker extends PickMouseBehavior
{

    ClassLogger logger = new ClassLogger("SectionPicker");

    Primitive lastPickedPrim = null;
    Appearance lastPickedPrimApp = null;

    Base3DPanel base3DPanel = null;

    public SectionPicker(BranchGroup root, Canvas3D canvas, Bounds bounds, Base3DPanel base3DPanel)
    {
        super(canvas, root, bounds);
        logger.logComment("New SectionPicker created...");
        this.setSchedulingBounds(bounds);
        this.base3DPanel = base3DPanel;

            // pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);

        pickCanvas.setTolerance(1);

    }

    public void updateScene(int xpos, int ypos)
    {
        logger.logComment("Point picked: (" + xpos + "," + ypos + ")");
        //TransformGroup tg = null;
        pickCanvas.setShapeLocation(xpos, ypos);
        PickResult pr = pickCanvas.pickClosest();

        Primitive selectedPrim = null;
        if ( pr != null)
        {

            selectedPrim = (Primitive) pr.getNode(PickResult.PRIMITIVE);

            logger.logComment("Selected primitive: "+ selectedPrim);

            if (selectedPrim==null)
            {
                Shape3D shape3D = (Shape3D) pr.getNode(PickResult.SHAPE3D);
                logger.logComment("Shape3d: " + shape3D);
                //logger.logComment("getGeometryArray: " + pr.getGeometryArray());
                //logger.logComment("getIntersection: " + pr.getIntersection(0));
               // logger.logComment("getClosestVertexIndex: " + pr.getIntersection(0).getClosestVertexIndex());
            }
            else
            {
                base3DPanel.markPrimitiveAsSelected(selectedPrim);
            }

            //base3DPanel.markPrimitiveAsSelected(selectedPrim);
        }
    }
}

