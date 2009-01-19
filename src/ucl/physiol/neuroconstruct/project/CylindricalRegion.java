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

import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;
import ucl.physiol.neuroconstruct.utils.*;
import javax.media.j3d.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.cell.*;

import java.util.*;
import java.awt.*;

/**
 * Extension of Regions in 3D
 *
 * @author Padraig Gleeson
 *  
 */

public class CylindricalRegion extends Region
{
    static final long serialVersionUID = -646745486L;


    public CylindricalRegion()
    {
        this(0,0,0,0,100,0, 50);
        logger = new ClassLogger("CylindricalRegion");
    }

    public CylindricalRegion(float startx, float starty, float startz, float endx, float endy, float endz, float radius)
    {
        super.setDescription("Cylinder");

        parameterList = new InternalParameter[7];
        parameterList[0] = new InternalParameter("StartX",
                                                 "X value of start point",
                                                 startx);

        parameterList[1] = new InternalParameter("StartY",
                                                 "Y value of start point",
                                                 starty);

        parameterList[2] = new InternalParameter("StartZ",
                                                 "Z value of start point",
                                                 startz);

        parameterList[3] = new InternalParameter("EndX",
                                                 "X value of end point",
                                                 endx);

        parameterList[4] = new InternalParameter("EndY",
                                                 "Y value of end point",
                                                 endy);

        parameterList[5] = new InternalParameter("EndZ",
                                                 "Z value of end point",
                                                 endz);


        parameterList[6] = new InternalParameter("Radius",
                                                 "Radius of cylinder",
                                                 radius);

    }

    public double getVolume()
    {
        return Math.PI * (getRadius() * getRadius()) * getLength();
    }



    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof CylindricalRegion)
        {
            CylindricalRegion other = (CylindricalRegion) otherObj;

            if (parametersEqual(other))
            {
                return true;
            }
        }
        return false;
    }


    public Object clone()
    {
        CylindricalRegion region = new CylindricalRegion();

        for (int i = 0; i < parameterList.length; i++)
        {
            region.setParameter(new String(parameterList[i].parameterName), parameterList[i].getValue());
        }
        return region;
    }


    public boolean isPointInRegion(Point3f point)
    {
        return isSphereInRegion(point, 0);
    }


    public Region getTranslatedRegion(Vector3f trans)
    {
        CylindricalRegion newCyl = new CylindricalRegion(this.getStartPoint().x + trans.x,
                                                   this.getStartPoint().y + trans.y,
                                                   this.getStartPoint().z + trans.z,
                                                   this.getEndPoint().x + trans.x,
                                                   this.getEndPoint().y + trans.y,
                                                   this.getEndPoint().z + trans.z,
                                                   this.getRadius());


        return newCyl;
    }



    private boolean isSegmentInRegion(Point3f startPoint,
                                     float startRadius,
                                     Point3f endPoint,
                                     float endRadius)
    {
        if (startPoint.equals(endPoint)) return isSphereInRegion(startPoint, Math.max(startRadius, endRadius));

        /** @todo Need more accurate way to do this!!! */

        Point3f midPoint = new Point3f(0.5f * (endPoint.x + startPoint.x),
                                       0.5f * (endPoint.y + startPoint.y),
                                       0.5f * (endPoint.z + startPoint.z));

        float height = startPoint.distance(endPoint);

        float maxRadius =  Math.max(startRadius, endRadius);

        float newCylRad = (float)Math.sqrt((height*height/4) + (maxRadius*maxRadius));

        return isSphereInRegion(midPoint, newCylRad);

    }

    public boolean isSphereInRegion(Point3f point, float sphereRadius)
    {
        logger.logComment("Checking whether sphere at: "+ point + " with radius "+sphereRadius+" is in: "+ toString());

        if ((point.x-sphereRadius)<getLowestXValue() || point.x+sphereRadius>getHighestXValue()) return false;
        if (point.y-sphereRadius<getLowestYValue() || point.y+sphereRadius>getHighestYValue()) return false;
        if (point.z-sphereRadius<getLowestZValue() || point.z+sphereRadius>getHighestZValue()) return false;

        //if (1==1) return true;

        Vector3f posVecRelToStart = new Vector3f(point.x - parameterList[0].value,
                                              point.y - parameterList[1].value,
                                              point.z - parameterList[2].value);

        //System.out.println("pointRelToStart: "+ posVecRelToStart);

        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(getNorm(), yAxis);

        //System.out.println("angle4: "+ angle4);

        Transform3D trans = new Transform3D();
        trans.setRotation(angle4);

        trans.transform(posVecRelToStart);

        //System.out.println("pointRelToStart: "+ posVecRelToStart);

        double distFromAxis = Math.sqrt((posVecRelToStart.x*posVecRelToStart.x) + (posVecRelToStart.z*posVecRelToStart.z));

        if (distFromAxis+sphereRadius>getRadius()) return false;

        if (posVecRelToStart.y-sphereRadius<0 || posVecRelToStart.y+sphereRadius>getLength())  return false;

        return true;

    }




    public boolean isCellWithinRegion(Point3f point, Cell cell, boolean completelyInside)
    {
        logger.logComment("Checking point: "+ point + " in: "+ toString());

        Segment firstSeg = cell.getFirstSomaSegment();
        Point3f actualStartOfSoma = new Point3f(point.x + firstSeg.getSection().getStartPointPositionX(),
                                        point.y + firstSeg.getSection().getStartPointPositionY(),
                                        point.z + firstSeg.getSection().getStartPointPositionZ());


        if (completelyInside)
            logger.logComment("Cell needs to be completely inside");
        else
        {
            logger.logComment("Only cell centre (" + firstSeg.getStartPointPosition() + ") needs to be inside");
            return isPointInRegion(actualStartOfSoma);
        }

        Vector somaSegments = cell.getOnlySomaSegments();


        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment nextSeg = (Segment) somaSegments.elementAt(i);

            Point3f actualStartPoint = new Point3f(point.x + nextSeg.getStartPointPosition().x,
                                                   point.y + nextSeg.getStartPointPosition().y,
                                                   point.z + nextSeg.getStartPointPosition().z);

            Point3f actualEndPoint = new Point3f(point.x + nextSeg.getEndPointPositionX(),
                                                 point.y + nextSeg.getEndPointPositionY(),
                                                 point.z + nextSeg.getEndPointPositionZ());


            logger.logComment("actualEndPoint: " + actualEndPoint + ", radius: " + nextSeg.getRadius());

            boolean thisInside = isSegmentInRegion(actualStartPoint,
                                                   nextSeg.getSegmentStartRadius(),
                                                   actualEndPoint,
                                                   nextSeg.getRadius());

            if (!thisInside) return false;

        }

        return true;

    }




    public Primitive addPrimitiveForRegion(TransformGroup tg, Appearance app)
    {
        return Utils3D.addCylinder(getRadius(),
                                             getStartPoint(),
                                             getEndPoint(),
                                             30,
                                             30,
                                             tg,
                                             app);
    }

    public Vector3f getNorm()
    {
        Vector3f norm = new Vector3f(parameterList[3].value - parameterList[0].value,
                                     parameterList[4].value - parameterList[1].value,
                                     parameterList[5].value - parameterList[2].value);

        norm.normalize();

        return norm;

    }

    public float getRadius()
    {
        return parameterList[6].value;
    }

    public float getLength()
    {
        return getStartPoint().distance(getEndPoint());
    }



    public Point3f getStartPoint()
    {
        return new Point3f(parameterList[0].value, parameterList[1].value, parameterList[2].value);
    }

    public Point3f getEndPoint()
    {
        return new Point3f(parameterList[3].value,parameterList[4].value, parameterList[5].value);

    }



    public float getLowestXValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_X_AXIS));
        return Math.min(parameterList[0].value, parameterList[3].value) - (float)(getRadius() * Math.sin(angle));

    };

    public float getLowestYValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Y_AXIS));
        //System.out.println("angle: "+angle);
        return Math.min(parameterList[1].value, parameterList[4].value) - (float)(getRadius() * Math.sin(angle));
    };

    public float getLowestZValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Z_AXIS));
        return Math.min(parameterList[2].value, parameterList[5].value) - (float)(getRadius() * Math.sin(angle));
    };

    public float getHighestXValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_X_AXIS));
        return Math.max(parameterList[0].value, parameterList[3].value) + (float)(getRadius() * Math.sin(angle));

    }

    public float getHighestYValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Y_AXIS));
        return Math.max(parameterList[1].value, parameterList[4].value) + (float)(getRadius() * Math.sin(angle));

    };

    public float getHighestZValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Z_AXIS));
        return Math.max(parameterList[2].value, parameterList[5].value) + (float)(getRadius() * Math.sin(angle));

    };



    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.getDescription());

        sb.append(" from: " + getStartPoint()+" to "+getEndPoint()+" with radius: "
                  + getRadius());

        return sb.toString();
    }



    public static void main(String[] args)
    {
        CylindricalRegion c = new CylindricalRegion(0, 0, 0, 100, 0, 0, 50);

        System.out.println("Created: " + c);

        Point3f p = new Point3f(99, 0, 0);

        System.out.println("Extent: ("
                           + c.getLowestXValue()
                           + ", "
                           + c.getLowestYValue()
                           + ", "
                           + c.getLowestZValue()
                           + ") to ("
                           + c.getHighestXValue()
                           + ", "
                           + c.getHighestYValue()
                           + ", "
                           + c.getHighestZValue()
                           + ")");


        System.out.println("Volume: " + c.getVolume());

        Appearance app = Utils3D.getGeneralObjectAppearance(Color.red);


        //SimpleCell cell = new SimpleCell("fff");

        System.out.println("Is point: " + p + " in region: " + c.isPointInRegion(p));

        System.out.println("-----------------------");
        c.addPrimitiveForRegion(new TransformGroup(), app);

    }


}
