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
 * @version 1.0.4
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
        TransformGroup tg = null;
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

