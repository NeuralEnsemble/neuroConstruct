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

package ucl.physiol.neuroconstruct.project.packing;

import java.util.*;
import javax.vecmath.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Base class for all Cell Packing adapters.
 * <b>Extend this class for customised Cell packing...</b>
 * The main function which needs to be overridden is generateNextPosition()
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class CellPackingAdapter
{
    private ClassLogger logger = new ClassLogger("CellPackingAdapter");

    protected Region myRegion = null;

    protected Cell myCell = null;

    protected String description = null;
    private Vector<Point3f> positionsAlreadyTaken = new Vector<Point3f>();

    protected InternalParameter[] parameterList = null;


    public final static String OTHER_OVERLAP_POLICY = "Existing Group Overlap";

    /**
     * Create a new CellPackingAdapter
     *
     * @param description A short description of how the cells will be arranged
     */

    public CellPackingAdapter(String description)
    {
        this.description = description;
        //logger.setThisClassSilent(true);
    }

    protected int getNumPosAlreadyTaken()
    {
        return positionsAlreadyTaken.size();
    }

    protected Point3f getLastPosTaken()
    {
        return positionsAlreadyTaken.lastElement();
    }




    /**
     * Add Info for region, and cell info
     *
     * @param region Extended region object where the cells are placed
     * @param cell The cell to be placed in the region (required for soma length)
     */
    public void addRegionAndCellInfo(Region region,
                                     Cell cell)
    {
        logger.logComment("addRegionAndCellInfo called...");
        myRegion = region;
        myCell = cell;
    }



    /**
     * Resets to zero internally computed positions
     */
    public void reset()
    {
        this.positionsAlreadyTaken.removeAllElements();
    }

    public int getCurrentNumberPositions()
    {
        int num = positionsAlreadyTaken.size();
        logger.logComment("There are "+num+" cells in this: "+ this.toString());
        return num;
    }

    public String getDescription()
    {
        return description;
    };

    /**
     * The method to call on the adapter for the next position
     * @throws CellPackingException if no more cells can be fit in, or simply
     * the maximum number of cells is reached
     * @return Point3f of position
     */
    public Point3f getNextPosition() throws CellPackingException
    {
        logger.logComment("       +++++       getNextPosition called with cell: "+ myCell.getInstanceName()+" and adapter: "+ toString());
        Point3f newPoint = generateNextPosition();

        this.positionsAlreadyTaken.add(newPoint);

        return newPoint;
    }

    /**
     * After this class has suggested a position, it may later be rejected,
     * i.e. due to a collision with another cell in another region. The
     * suggested position can be cancelled here.
     *
     * @param point Point3f of position
     */
    public void cancelPosition(Point3f point)
    {
        logger.logComment("Position: "+ point +" being cancelled...");
        boolean success = positionsAlreadyTaken.remove(point);
        if (success) logger.logComment("Successfully removed");
        else logger.logComment("Point never present...");
     }


    public boolean doesCellCollideWithExistingCells(Point3f suggestedLocation,
                                                    Cell newCell)
    {
        logger.logComment("Packer of cells: "+myCell.getInstanceName()+" checking if cell: "+newCell.getInstanceName()+" at: "+ suggestedLocation + " collides with one of my "+positionsAlreadyTaken.size()+ " cells");

        Iterator iter = positionsAlreadyTaken.iterator();

        while (iter.hasNext())
        {
            Point3f locationMyCell = (Point3f) iter.next();

            logger.logComment("Checking one of my cells at: " + Utils3D.getShortStringDesc(locationMyCell));

            Vector<Segment> volumeSegmentsMyCell = new Vector<Segment>();
            Vector<Segment> mySegments = myCell.getAllSegments();

            for (int i = 0; i < mySegments.size(); i++)
            {
                Segment segment = mySegments.elementAt(i);
                if (segment.isFiniteVolume())
                    volumeSegmentsMyCell.add(segment);
            }

            Vector<Segment> volumeSegmentsNewCell = new Vector<Segment>();
            Vector<Segment> newSegments = newCell.getAllSegments();

            for (int i = 0; i < newSegments.size(); i++)
            {
                Segment segment = newSegments.elementAt(i);
                if (segment.isFiniteVolume())
                    volumeSegmentsNewCell.add(segment);
            }

            for (int newSecs = 0; newSecs < volumeSegmentsNewCell.size(); newSecs++)
            {
                Segment segmentNewCell = volumeSegmentsNewCell.elementAt(newSecs);


                for (int mySecs = 0; mySecs < volumeSegmentsMyCell.size(); mySecs++)
                {
                    Segment segmentMyCell = volumeSegmentsMyCell.elementAt(mySecs);

                    /** @todo Redo this for all eventualities... */

                    Point3f realStartPosNew = new Point3f(segmentNewCell.getStartPointPosition());
                    realStartPosNew.add(suggestedLocation);
                    Point3f realEndPosNew = new Point3f(segmentNewCell.getEndPointPosition());
                    realEndPosNew.add(suggestedLocation);

                    Point3f realStartPosMine = new Point3f(segmentMyCell.getStartPointPosition());
                    realStartPosMine.add(locationMyCell);
                    Point3f realEndPosMine = new Point3f(segmentMyCell.getEndPointPosition());
                    realEndPosMine.add(locationMyCell);

                    logger.logComment("realStartPosNew: "+ realStartPosNew + ", sec: "+segmentNewCell);
                    logger.logComment("realStartPosMine: "+ realStartPosMine+ ", sec: "+segmentMyCell);

                    boolean collides = false;

                    // CASE: both segments are spherical...

                    if (segmentMyCell.getSegmentShape()==Segment.SPHERICAL_SHAPE &&
                        segmentNewCell.getSegmentShape()==Segment.SPHERICAL_SHAPE)
                    {

                        if (realStartPosMine.distance(realStartPosNew)
                            < segmentMyCell.getRadius() + segmentNewCell.getRadius())
                        {
                            logger.logComment("2 Spherical collision");
                            collides = true;
                        }
                    }

                    // CASE: Only my segment is spherical
                    else if (segmentMyCell.getSegmentShape()==Segment.SPHERICAL_SHAPE)
                    {
                        if (Utils3D.checkIntersectCylinderSphere(realStartPosMine,
                                                             segmentMyCell.getRadius(),
                                                             realStartPosNew,
                                                             realEndPosNew,
                                                             segmentNewCell.getRadius()))
                        {
                            logger.logComment("Cylindrical collision");
                            collides = true;
                        }
                    }

                    // CASE: Only new segment is spherical
                    else if (segmentMyCell.getSegmentShape()==Segment.SPHERICAL_SHAPE)
                    {
                        if (Utils3D.checkIntersectCylinderSphere(realStartPosNew,
                                                             segmentNewCell.getRadius(),
                                                             realStartPosMine,
                                                             realEndPosMine,
                                                             segmentMyCell.getRadius()))
                        {
                            logger.logComment("Cylindrical collision");
                            collides = true;
                        }
                    }

                    // CASE: Both are cylindrical
                    else
                    {
                        // Check whether the spheres at the new segment start point
                        // and end points collide with the cylinder of my segment.
                        // Obviously not a perfect criterion, but will sufice in most cases.

                        if (Utils3D.checkIntersectCylinderSphere(realStartPosNew,
                                                                 segmentNewCell.getSegmentStartRadius(),
                                                                 realStartPosMine,
                                                                 realEndPosMine,
                                                                 segmentMyCell.getRadius()))
                          {
                              logger.logComment("Start point new collision");
                              collides = true;
                          }
                          if (Utils3D.checkIntersectCylinderSphere(realEndPosNew,
                                                                   segmentNewCell.getRadius(),
                                                                   realStartPosMine,
                                                                   realEndPosMine,
                                                                   segmentMyCell.getRadius()))
                          {
                              logger.logComment("End point new collision");
                              collides = true;
                          }
                    }

                    if (collides)
                    {
                        logger.logComment("Collides...");
                        return true;
                    }

                }
            }

        }
        return false;
    }


    public InternalParameter[] getParameterList()
    {
        return parameterList;
    };

    /**
     * Internal function to generate the next position
     * @return The position as Point3d object
     * @throws CellPackingException if a position cannoth be generated, i.e. region full
     */
    protected abstract Point3f generateNextPosition() throws CellPackingException;



    public abstract void setParameter(String parameterName,
                                      float parameterValue) throws CellPackingException;

    /**
     * Returns a short summary of the class's state, for GUIs etc. Included here
     * (even though it's in Object) to force the subclasses to implement it
     *
     * @return A string rep of internal state
     */
    @Override
    public abstract String toString();

    /**
     * For a more plain english description of the settings
     */
    public abstract String toNiceString();
    //public  String toNiceString(){return null;};


    public boolean avoidOtherCellGroups()
    {
        return true;
    }



    public void setParameterList(InternalParameter[] parameterList)
    {
        this.parameterList = parameterList;
    }

/*
    /**
     * Sets the internal paramters using the info in the XML string
     *
     * @param xmlElement the Element containing the stored parameters
     * @throws CellPackingException if there's a parsing problem
     */
    /*
    public void setInternalInfo (Element xmlElement) throws CellPackingException
    {
        for (int i = 0; i < parameterList.length; i++)
        {
            PackingParameter param = parameterList[i];
            Attribute attrib =  xmlElement.getAttribute(param.parameterName);

            try
            {
                logger.logComment("Trying to set "+param.parameterName+" to "+attrib.getFloatValue());
                setParameter(param.parameterName, attrib.getFloatValue());
            }
            catch (DataConversionException ex)
            {
                throw new CellPackingException("Problem reading parameter value: "
                                               +param.parameterName
                                               + " from element: "
                                               + xmlElement,
                                               ex);
            }
            catch (CellPackingException ex)
            {
                throw new CellPackingException("Problem setting parameter: "
                                               +param.parameterName
                                               + " from element: "
                                               + xmlElement,
                                               ex);
            }
        }
    }
*/


}
