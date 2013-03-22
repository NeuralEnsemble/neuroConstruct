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

package ucl.physiol.neuroconstruct.project;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;
import java.awt.Color;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.utils.*;

/**
 * Extension of Regions in 3D
 *
 * @author Padraig Gleeson
 *  
 */

public class RectangularBox extends Region
{
    
    static final long serialVersionUID = -2656949393L;
    
    public static final String X_PARAM = "X";
    public static final String Y_PARAM = "Y";
    public static final String Z_PARAM = "Z";
    public static final String WIDTH_PARAM = "Width";
    public static final String HEIGHT_PARAM = "Height";
    public static final String DEPTH_PARAM = "Depth";

    public RectangularBox()
    {
        super.setDescription("Rectangular Box");
        logger = new ClassLogger("RectangularBox");

        parameterList = new InternalParameter[6];

        parameterList[0] = new InternalParameter(X_PARAM,
                                                 "X value of bottom corner",
                                                 0);

        parameterList[1] = new InternalParameter(Y_PARAM,
                                                 "Y value of bottom corner",
                                                 0);

        parameterList[2] = new InternalParameter(Z_PARAM,
                                                 "Z value of bottom corner",
                                                 0);

        parameterList[3] = new InternalParameter(WIDTH_PARAM,
                                                 "Dimension of rectangular box in X dir",
                                                 100);

        parameterList[4] = new InternalParameter(HEIGHT_PARAM,
                                                 "Dimension of rectangular box in Y dir",
                                                 30);


        parameterList[5] = new InternalParameter(DEPTH_PARAM,
                                                 "Dimension of rectangular box in Z dir",
                                                 100);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof RectangularBox)
        {
            RectangularBox other = (RectangularBox) otherObj;

            if (parametersEqual(other))
            {
                return true;
            }
        }
        return false;
    }


    public RectangularBox(float x, float y, float z,
                          float width, float height, float depth)
    {
        this();
        this.setParameter(RectangularBox.X_PARAM, x);
        this.setParameter(RectangularBox.Y_PARAM, y);
        this.setParameter(RectangularBox.Z_PARAM, z);
        this.setParameter(RectangularBox.WIDTH_PARAM, width);
        this.setParameter(RectangularBox.HEIGHT_PARAM, height);
        this.setParameter(RectangularBox.DEPTH_PARAM, depth);
    }


    public double getVolume()
    {
        return (this.getHighestXValue() - this.getLowestXValue())*
            (this.getHighestYValue() - this.getLowestYValue())*
            (this.getHighestZValue() - this.getLowestZValue());

    };

    public Region getTranslatedRegion(Vector3f trans)
    {
        RectangularBox newBox = new RectangularBox(this.getLowestXValue() + trans.x,
                                                   this.getLowestYValue() + trans.y,
                                                   this.getLowestZValue() + trans.z,
                                                   Math.abs(this.parameterList[3].value),
                                                   Math.abs(this.parameterList[4].value),
                                                   Math.abs(this.parameterList[5].value));


        return newBox;
    }

    public static SphericalRegion getEnclosingSphere(RectangularBox box)
    {
        Point3f centre = new Point3f( (box.getHighestXValue() + box.getLowestXValue()) / 2,
                                     (box.getHighestYValue() + box.getLowestYValue()) / 2,
                                     (box.getHighestZValue() + box.getLowestZValue()) / 2);

        float radius = centre.distance(new Point3f(box.getLowestXValue(), box.getLowestYValue(), box.getLowestZValue()));

        SphericalRegion newSphere = new SphericalRegion(centre.x,
                                                        centre.y,
                                                        centre.z,
                                                        radius);

        return newSphere;
    }





    public Object clone()
    {
        RectangularBox region = new RectangularBox();
        for (int i = 0; i < parameterList.length; i++)
        {
            region.setParameter(new String(parameterList[i].parameterName), parameterList[i].getValue());
        }
        return region;

    }


    public boolean isCellWithinRegion(Point3f point, Cell cell, boolean completelyInside)
    {
        logger.logComment("Checking point: "+ point + " in: "+ toString());

        float minXLoc, minYLoc, minZLoc;
        float maxXLoc, maxYLoc, maxZLoc;

        Segment firstSeg = cell.getFirstSomaSegment();

        if (completelyInside)
            logger.logComment("Cell needs to be completely inside");
        else
            logger.logComment("Only cell centre ("+firstSeg.getStartPointPosition()+") needs to be inside");

        int factor = 0;
        if (completelyInside) factor = 1;

        minXLoc = parameterList[0].value + factor * firstSeg.getSection().getStartRadius();
        minYLoc = parameterList[1].value + factor * firstSeg.getSection().getStartRadius();
        minZLoc = parameterList[2].value + factor * firstSeg.getSection().getStartRadius();
        maxXLoc = parameterList[0].value + parameterList[3].value - factor * firstSeg.getSection().getStartRadius();
        maxYLoc = parameterList[1].value + parameterList[4].value - factor * firstSeg.getSection().getStartRadius();
        maxZLoc = parameterList[2].value + parameterList[5].value - factor * firstSeg.getSection().getStartRadius();

        Point3f actualStartOfSoma = new Point3f(point.x + firstSeg.getSection().getStartPointPositionX(),
                                                point.y + firstSeg.getSection().getStartPointPositionY(),
                                                point.z + firstSeg.getSection().getStartPointPositionZ());

        logger.logComment("actualStartOfSoma: "+ actualStartOfSoma+ ", radius: "+firstSeg.getSection().getStartRadius());

        if (actualStartOfSoma.x < minXLoc ||
            actualStartOfSoma.x > maxXLoc ||
            actualStartOfSoma.y < minYLoc ||
            actualStartOfSoma.y > maxYLoc ||
            actualStartOfSoma.z < minZLoc ||
            actualStartOfSoma.z > maxZLoc)
            return false;

        if (completelyInside)
        {
            Vector somaSegments = cell.getOnlySomaSegments();

            for (int i = 0; i < somaSegments.size(); i++)
            {
                Segment nextSeg = (Segment) somaSegments.elementAt(i);

                Point3f actualEndPoint = new Point3f(point.x + nextSeg.getEndPointPositionX(),
                                                     point.y + nextSeg.getEndPointPositionY(),
                                                     point.z + nextSeg.getEndPointPositionZ());

                logger.logComment("actualEndPoint: "+ actualEndPoint + ", radius: "+nextSeg.getRadius());

                if (actualEndPoint.x < parameterList[0].value + nextSeg.getRadius() ||
                    actualEndPoint.x > parameterList[0].value + parameterList[3].value - nextSeg.getRadius() ||
                    actualEndPoint.y < parameterList[1].value + nextSeg.getRadius() ||
                    actualEndPoint.y > parameterList[1].value + parameterList[4].value - nextSeg.getRadius() ||
                    actualEndPoint.z < parameterList[2].value + nextSeg.getRadius() ||
                    actualEndPoint.z > parameterList[2].value + parameterList[5].value - nextSeg.getRadius())
                    return false;
            }
        }
        return true;
    }





    public boolean isPointInRegion(Point3f point)
    {
        logger.logComment("Checking whether point: "+ point + " is in: "+ toString());


        if (point.x < parameterList[0].value ||
            point.x > parameterList[0].value + parameterList[3].value ||
            point.y < parameterList[1].value ||
            point.y > parameterList[1].value + parameterList[4].value ||
            point.z < parameterList[2].value ||
            point.z > parameterList[2].value + parameterList[5].value)
            return false;

        return true;
    }

    public Primitive addPrimitiveForRegion(TransformGroup tg, Appearance app)
    {
        /** @todo Need some way to display flat boxes, e.g. put line around edges...

        if (this.getLowestXValue()==this.getHighestXValue() ||
            this.getLowestYValue()==this.getHighestYValue() ||
            this.getLowestZValue()==this.getHighestZValue())
        {


            return Utils3D.addBoxAtLocation(parameterList[3].value,
                                        parameterList[4].value,
                                        parameterList[5].value,
                                        new Vector3f(parameterList[0].value,
                                                     parameterList[1].value,
                                                     parameterList[2].value),
                                        tg,
                                        Utils3D.getGeneralObjectAppearance(Color.red));

        }*/

        
        return Utils3D.addBoxAtLocation(parameterList[3].value,
                                        parameterList[4].value,
                                        parameterList[5].value,
                                        new Vector3f(parameterList[0].value,
                                                     parameterList[1].value,
                                                     parameterList[2].value),
                                        tg,
                                        app);
    }
    
    
    public void addLinesAroundRegion(TransformGroup tg)
    {
        Color3f x = new Color3f(Color.green.brighter().brighter());
        Color3f y = new Color3f(Color.yellow.brighter().brighter());
        Color3f z = new Color3f(Color.red.brighter().brighter());
        
        LineArray wireframe = new LineArray(12*2,
                                               GeometryArray.COORDINATES
                                               | GeometryArray.COLOR_3);
        
        wireframe.setCapability(LineArray.ALLOW_COLOR_WRITE);
        wireframe.setCapability(LineArray.ALLOW_COORDINATE_READ);
        wireframe.setCapability(LineArray.ALLOW_COUNT_READ);
        ArrayList<Point3f> vx = getVertices();

        // Add lines parallel to x
        wireframe.setCoordinate(0, vx.get(0));
        wireframe.setCoordinate(1, vx.get(2));
        wireframe.setCoordinate(2, vx.get(1));
        wireframe.setCoordinate(3, vx.get(3));
        
        wireframe.setCoordinate(4, vx.get(4));
        wireframe.setCoordinate(5, vx.get(6));
        wireframe.setCoordinate(6, vx.get(5));
        wireframe.setCoordinate(7, vx.get(7));
        
        for(int i=0;i<8;i++)
            wireframe.setColor(i, x);
        
        
        // Add lines parallel to y
        wireframe.setCoordinate(8, vx.get(0));
        wireframe.setCoordinate(9, vx.get(4));
        wireframe.setCoordinate(10, vx.get(1));
        wireframe.setCoordinate(11, vx.get(5));
        
        wireframe.setCoordinate(12, vx.get(2));
        wireframe.setCoordinate(13, vx.get(6));
        wireframe.setCoordinate(14, vx.get(3));
        wireframe.setCoordinate(15, vx.get(7));
        
        
        for(int i=8;i<16;i++)
            wireframe.setColor(i, y);
        
        // Add lines parallel to z
        wireframe.setCoordinate(16, vx.get(0));
        wireframe.setCoordinate(17, vx.get(1));
        wireframe.setCoordinate(18, vx.get(2));
        wireframe.setCoordinate(19, vx.get(3));
        wireframe.setCoordinate(20, vx.get(4));
        wireframe.setCoordinate(21, vx.get(5));
        wireframe.setCoordinate(22, vx.get(6));
        wireframe.setCoordinate(23, vx.get(7));
        
        
        for(int i=16;i<24;i++)
            wireframe.setColor(i, z);
        
        Shape3D shape = new Shape3D(wireframe);
        tg.addChild(shape);
        
       
        

    }
    
    public ArrayList<Point3f> getVertices()
    {
        ArrayList<Point3f> v = new ArrayList<Point3f>();
        v.add(new Point3f(getLowestXValue(), getLowestYValue(), getLowestZValue()));
        v.add(new Point3f(getLowestXValue(), getLowestYValue(), getHighestZValue()));
        v.add(new Point3f(getHighestXValue(), getLowestYValue(), getLowestZValue()));
        v.add(new Point3f(getHighestXValue(), getLowestYValue(), getHighestZValue()));
        
        v.add(new Point3f(getLowestXValue(), getHighestYValue(), getLowestZValue()));
        v.add(new Point3f(getLowestXValue(), getHighestYValue(), getHighestZValue()));
        v.add(new Point3f(getHighestXValue(), getHighestYValue(), getLowestZValue()));
        v.add(new Point3f(getHighestXValue(), getHighestYValue(), getHighestZValue()));
        
        return v;
        
    }


    public float getLowestXValue()
    {
        // just in case the length is negative...
        return Math.min(parameterList[0].value, parameterList[0].value + parameterList[3].value);
    };

    public float getLowestYValue()
    {
        // just in case the length is negative...
        return Math.min(parameterList[1].value, parameterList[1].value + parameterList[4].value);
    };

    public float getLowestZValue()
    {
        // just in case the length is negative...
        return Math.min(parameterList[2].value, parameterList[2].value + parameterList[5].value);
    };

    
    public float getXExtent()
    {
        return getHighestXValue()-getLowestXValue();
    }
    public float getYExtent()
    {
        return getHighestYValue()-getLowestYValue();
    }
    public float getZExtent()
    {
        return getHighestZValue()-getLowestZValue();
    }

    public float getHighestXValue()
    {
        // just in case the length is negative...
        return Math.max(parameterList[0].value, parameterList[0].value + parameterList[3].value);
    }

    public float getHighestYValue()
    {
        // just in case the length is negative...
        return Math.max(parameterList[1].value, parameterList[1].value + parameterList[4].value);
    };

    public float getHighestZValue()
    {
        // just in case the length is negative...
        return Math.max(parameterList[2].value, parameterList[2].value + parameterList[5].value);
    };




    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.getDescription());

        sb.append(" from point: ("
                  + getLowestXValue()
                  + ", "
                  + getLowestYValue()
                  + ", "
                  + getLowestZValue()
                  + ") to ("
                  + getHighestXValue()
                  + ", "
                  + getHighestYValue()
                  + ", "
                  + getHighestZValue()
                  + ")");

        return sb.toString();
    }

    public static void main(String[] args)
    {
        RectangularBox rb = new RectangularBox();

        System.out.println("Created: "+ rb);

        Point3f p = new Point3f(95,18f,98);

        System.out.println("Moved: "+ rb.getTranslatedRegion(new Vector3f(p)));
        System.out.println("Sphere around: "+ RectangularBox.getEnclosingSphere(rb));

        SimpleCell cell = new SimpleCell("fff");
        Vector3f origLoc = new Vector3f(cell.getFirstSomaSegment().getStartPointPosition());
        origLoc.scale(-1);
        CellTopologyHelper.translateAllPositions(cell, origLoc);

        System.out.println("Cell: "+CellTopologyHelper.printShortDetails(cell));

        System.out.println("Is point: "+ p+ " in region: "+ rb.isCellWithinRegion(p, cell, true));

    }


}
