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

package ucl.physiol.neuroconstruct.project;

import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;
import ucl.physiol.neuroconstruct.utils.*;
import javax.media.j3d.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import java.util.*;

/**
 * Extension of Regions in 3D
 *
 * @author Padraig Gleeson
 *  
 */

public class SphericalRegion extends Region
{
    ClassLogger logger = new ClassLogger("SphericalRegion");

    public SphericalRegion(float x, float y, float z, float radius)
    {
        super.setDescription("Sphere");

        parameterList = new InternalParameter[4];
        parameterList[0] = new InternalParameter("X",
                                                 "X value of centre",
                                                 x);

        parameterList[1] = new InternalParameter("Y",
                                                 "Y value of centre",
                                                 y);

        parameterList[2] = new InternalParameter("Z",
                                                 "Z value of centre",
                                                 z);

        parameterList[3] = new InternalParameter("Radius",
                                                 "Radius of sphere",
                                                 radius);
    }

    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof SphericalRegion)
        {
            SphericalRegion other = (SphericalRegion) otherObj;

            if (parametersEqual(other))
            {
                return true;
            }
        }
        return false;
    }

    public double getVolume()
    {
        return 4 * Math.PI * (getRadius()*getRadius()*getRadius()) / 3;
    }

    public SphericalRegion()
    {
        this(0,0,0,100);
    }

    public Object clone()
    {
        SphericalRegion region = new SphericalRegion();
        for (int i = 0; i < parameterList.length; i++)
        {
            region.setParameter(new String(parameterList[i].parameterName), parameterList[i].getValue());
        }
        return region;
    }

    public boolean isPointInRegion(Point3f point)
    {
        logger.logComment("Checking whether point: " + point + " is in: " + toString());

        if (point.distance(getCentre()) > parameterList[3].value)
            return false;

        return true;
    }


    public Region getTranslatedRegion(Vector3f trans)
    {
        SphericalRegion newSphere = new SphericalRegion(this.getCentre().x + trans.x,
                                                        this.getCentre().y + trans.y,
                                                        this.getCentre().z + trans.z,
                                                        this.getRadius());

        return newSphere;
    }


    public boolean isCellWithinRegion(Point3f point, Cell cell, boolean completelyInside)
    {
        logger.logComment("Checking point: " + point + " in: " + toString());

        Segment firstSeg = cell.getFirstSomaSegment();

        if (completelyInside)
            logger.logComment("Cell needs to be completely inside");
        else
            logger.logComment("Only cell centre (" + firstSeg.getStartPointPosition() + ") needs to be inside");

        //int factor = 0;
        //if (completelyInside) factor = 1;

        Point3f actualStartOfSoma = new Point3f(point.x + firstSeg.getSection().getStartPointPositionX(),
                                                point.y + firstSeg.getSection().getStartPointPositionY(),
                                                point.z + firstSeg.getSection().getStartPointPositionZ());

        logger.logComment("actualStartOfSoma: " + actualStartOfSoma + ", radius: " +
                          firstSeg.getSection().getStartRadius());

        float effectiveRadius = parameterList[3].value;
        if (completelyInside) effectiveRadius = parameterList[3].value - firstSeg.getSection().getStartRadius();

        if (actualStartOfSoma.distance(getCentre()) > effectiveRadius)
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

                effectiveRadius = parameterList[3].value - nextSeg.getRadius();

                logger.logComment("actualEndPoint: " + actualEndPoint + ", radius: " + nextSeg.getRadius());

                if (actualEndPoint.distance(getCentre()) > effectiveRadius)
                    return false;
            }
        }
        return true;

    }

    public Primitive addPrimitiveForRegion(TransformGroup tg, Appearance app)
    {
        return Utils3D.addSphereAtLocation(getRadius(),
                                           new Vector3f(getCentre()),
                                           tg,
                                           app);
    }

    public float getLowestXValue()
    {
        return parameterList[0].value - parameterList[3].value;
    };

    public float getLowestYValue()
    {
        return parameterList[1].value - parameterList[3].value;
    };

    public float getLowestZValue()
    {
        return parameterList[2].value - parameterList[3].value;
    };

    public float getHighestXValue()
    {
        return parameterList[0].value + parameterList[3].value;
    }

    public float getHighestYValue()
    {
        return parameterList[1].value + parameterList[3].value;
    };

    public float getHighestZValue()
    {
        return parameterList[2].value + parameterList[3].value;
    };

    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.getDescription());

        sb.append(" at: " + getCentre() + " with radius: "
                  + getRadius());

        return sb.toString();
    }

    public float getRadius()
    {
        return parameterList[3].value;
    }


    public Point3f getCentre()
    {
        return new Point3f(parameterList[0].value,
                           parameterList[1].value,
                           parameterList[2].value);
    }

    public static void main(String[] args)
    {
        SphericalRegion s = new SphericalRegion();

        System.out.println("Created: "+ s);


        System.out.println("Volume: " + s.getVolume());

        Point3f p = new Point3f(100,0,0);

        SimpleCell cell = new SimpleCell("fff");

        System.out.println("Is point: "+ p+ " in region: "+ s.isCellWithinRegion(p, cell, true));


    }


}
