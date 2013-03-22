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

public class ConicalRegion extends Region
{
    static final long serialVersionUID = -45357457456L;


    public ConicalRegion()
    {
        this(0,0,0,0,100,0, 50);
        logger = new ClassLogger("SphericalRegion");
    }

    public ConicalRegion(float startx, float starty, float startz, float endx, float endy, float endz, float baseRadius)
    {
        super.setDescription("Cone");

        parameterList = new InternalParameter[7];
        parameterList[0] = new InternalParameter("BaseX",
                                                 "X value of center of base",
                                                 startx);

        parameterList[1] = new InternalParameter("BaseY",
                                                 "Y value of center of base",
                                                 starty);

        parameterList[2] = new InternalParameter("BaseZ",
                                                 "Z value of center of base",
                                                 startz);

        parameterList[3] = new InternalParameter("ApexX",
                                                 "X value of apex",
                                                 endx);

        parameterList[4] = new InternalParameter("ApexY",
                                                 "Y value of apex",
                                                 endy);

        parameterList[5] = new InternalParameter("ApexZ",
                                                 "Z value of apex",
                                                 endz);


        parameterList[6] = new InternalParameter("BaseRadius",
                                                 "Radius of base of cone",
                                                 baseRadius);

    }

    public double getVolume()
    {
        return (1/3f) * Math.PI * (getBaseRadius() * getBaseRadius()) * getHeight();
    }



    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof ConicalRegion)
        {
            ConicalRegion other = (ConicalRegion) otherObj;

            if (parametersEqual(other))
            {
                return true;
            }
        }
        return false;
    }


    public Object clone()
    {
        ConicalRegion region = new ConicalRegion();

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



    public boolean isSphereInRegion(Point3f point, float sphereRadius)
    {
        logger.logComment("Checking whether sphere at: "+ point + " with radius "+sphereRadius+" is in: "+ toString());

        if ((point.x-sphereRadius)<getLowestXValue() || point.x+sphereRadius>getHighestXValue()) return false;
        if (point.y-sphereRadius<getLowestYValue() || point.y+sphereRadius>getHighestYValue()) return false;
        if (point.z-sphereRadius<getLowestZValue() || point.z+sphereRadius>getHighestZValue()) return false;



        Vector3f posVecRelToStart = new Vector3f(point.x - parameterList[0].value,
                                              point.y - parameterList[1].value,
                                              point.z - parameterList[2].value);

        logger.logComment("pointRelToStart: "+ posVecRelToStart);

        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(getNorm(), yAxis);

        //System.out.println("angle4: "+ angle4);

        Transform3D trans = new Transform3D();
        trans.setRotation(angle4);

        trans.transform(posVecRelToStart);

        logger.logComment("pointRelToStart: "+ posVecRelToStart);

        double distFromAxis = Math.sqrt((posVecRelToStart.x*posVecRelToStart.x) + (posVecRelToStart.z*posVecRelToStart.z));

        logger.logComment("distFromAxis: "+distFromAxis);


        if (posVecRelToStart.y-sphereRadius<0 || posVecRelToStart.y+sphereRadius>getHeight())
        {
            logger.logComment("point above or below cylinder...");
            return false;
        }

        float distToCylSurface = getBaseRadius()
            - (posVecRelToStart.y * getBaseRadius() / getHeight()) ;

        logger.logComment("distToCylSurface at "+posVecRelToStart.y+": "+distToCylSurface);

        float distFromYaxisAllowed = distToCylSurface
            - (float)(sphereRadius/Math.cos(Math.asin(getBaseRadius() / getHeight())));

        logger.logComment("distFromYaxisAllowed at "+posVecRelToStart.y+": "+distFromYaxisAllowed);

        if (distFromAxis>distFromYaxisAllowed) return false;


        return true;

    }

    public Region getTranslatedRegion(Vector3f trans)
    {
        ConicalRegion newCyl = new ConicalRegion(this.getBaseCenterPoint().x + trans.x,
                                                   this.getBaseCenterPoint().y + trans.y,
                                                   this.getBaseCenterPoint().z + trans.z,
                                                   this.getApexPoint().x + trans.x,
                                                   this.getApexPoint().y + trans.y,
                                                   this.getApexPoint().z + trans.z,
                                                   this.getBaseRadius());


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

        return Utils3D.addCone(getBaseRadius(),
                                             getBaseCenterPoint(),
                                             getApexPoint(),
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

    public float getBaseRadius()
    {
        return parameterList[6].value;
    }

    public float getHeight()
    {
        return getBaseCenterPoint().distance(getApexPoint());
    }



    public Point3f getBaseCenterPoint()
    {
        return new Point3f(parameterList[0].value, parameterList[1].value, parameterList[2].value);
    }

    public Point3f getApexPoint()
    {
        return new Point3f(parameterList[3].value,parameterList[4].value, parameterList[5].value);

    }



    public float getLowestXValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_X_AXIS));

        return Math.min(parameterList[0].value - (float)(getBaseRadius() * Math.sin(angle)), parameterList[3].value);

    };

    public float getLowestYValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Y_AXIS));
        //System.out.println("angle: "+angle);
        return Math.min(parameterList[1].value - (float)(getBaseRadius() * Math.sin(angle)), parameterList[4].value);
    };

    public float getLowestZValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Z_AXIS));
        return Math.min(parameterList[2].value - (float)(getBaseRadius() * Math.sin(angle)), parameterList[5].value);
    };

    public float getHighestXValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_X_AXIS));
        return Math.max(parameterList[0].value + (float)(getBaseRadius() * Math.sin(angle)), parameterList[3].value);

    }

    public float getHighestYValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Y_AXIS));
        return Math.max(parameterList[1].value + (float)(getBaseRadius() * Math.sin(angle)), parameterList[4].value);

    };

    public float getHighestZValue()
    {
        double angle = Math.acos( getNorm().dot(Utils3D.POS_Z_AXIS));
        return Math.max(parameterList[2].value + (float)(getBaseRadius() * Math.sin(angle)), parameterList[5].value);

    };



    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.getDescription());

        sb.append(" with base of radius: "+ this.getBaseRadius() +" centered at "  + this.getBaseCenterPoint()
                  +" to apex at "+this.getApexPoint());

        return sb.toString();
    }



    public static void main(String[] args)
    {
        ConicalRegion c = new ConicalRegion(0, 0, 0, 0, 0, 100, 50);

        System.out.println("Created: " + c);

        Point3f p = new Point3f(0, 0, 90);

        System.out.println("Volume: " + c.getVolume());

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

        Appearance app = Utils3D.getGeneralObjectAppearance(Color.red);


        //SimpleCell cell = new SimpleCell("fff");

        float rad = 4;

        System.out.println("Is point: " + p + ", radius: "+rad+" in region: " + c.isSphereInRegion(p, rad));

        System.out.println("-----------------------");
        c.addPrimitiveForRegion(new TransformGroup(), app);

    }


}
