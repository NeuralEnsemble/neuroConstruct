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
import java.io.Serializable;
import ucl.physiol.neuroconstruct.utils.*;
import javax.media.j3d.*;
import ucl.physiol.neuroconstruct.cell.*;

/**
 * Base class for Regions in 3D. Any new Region must implement all the abstract
 * methods here, most importantly, defining the internal variables which make the shape
 * and implementing a 3D view for addPrimitiveForRegion()
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class Region implements Serializable
{
    static ClassLogger logger = new ClassLogger("Region");

    String description = null;

    InternalParameter[] parameterList = null;

    public Region()
    {}

    public Region(String description)
    {
        this.description = description;
    }

    @Override
    public abstract boolean equals(Object obj);

    protected boolean parametersEqual(Region region)
    {
        if (parameterList.length != region.getParameterList().length)
            return false;

        for (int i = 0; i < parameterList.length; i++)
        {
            if (!parameterList[i].equals(region.getParameterList()[i]))
            {
                return false;
            }
        }
        return true;

    }


    /**
     * This function is needed for automatic storage of the Parameters by XMLEncoder.
     * If the internal functioning of the subclasses only use these params, they don't need to worry
     * about data saving.
     */
    public InternalParameter[] getParameterList()
    {
        return parameterList;
    };

    /**
    * Sub classes should know what the parameters mean, and so this function could need to be overwritten,
    * for better checking of values
    */
   public void setParameter(String parameterName,
                            float parameterValue)
   {
       for (int i = 0; i < parameterList.length; i++)
       {
           if (parameterList[i].parameterName.equals(parameterName))
           {
               parameterList[i].setValue(parameterValue);
               return;
           }
       }

   };


    public void setParameterList(InternalParameter[] parameterList)
    {
        this.parameterList = parameterList;
    }


    /**
     * Returns a short summary of the class's state, for GUIs etc. Included here
     * (even though it's in Object) to force the subclasses to implement it
     *
     * @return A string rep of internal state
     */
    @Override
    public abstract String toString();


    /**
     * These functions need to be implemented in the subclass for packing purposes
     * and for getting the general location of the region in a generic way
     */
    public abstract float getLowestXValue();

    public abstract float getLowestYValue();

    public abstract float getLowestZValue();

    public abstract float getHighestXValue();

    public abstract float getHighestYValue();

    public abstract float getHighestZValue();

    /**
     * Needs to be used when generating a copy. Ideally just using the default
     * constructor and copying the internal params
     * but that can't be done here since the class is abstract
     */
    @Override
    public abstract Object clone();


    /**
     * The function to check if the cell mentioned is within the region
     * @param point The point at which the cell is translated to.
     * @param cell The cell to be placed in the region
     * @param completelyInside If true, all soma segments of the cell must be inside region
     * if false, only the start point of the soma must be inside (centre for spherical somas)
     * @return true if the cell can be placed inside the region
     *
     */
    public abstract boolean isCellWithinRegion(Point3f point, Cell cell, boolean completelyInside);


    /**
     * Gets a new Region of the same type, with coords translated by the specified Vector
     */
    public abstract Region getTranslatedRegion(Vector3f trans);


    public abstract boolean isPointInRegion(Point3f point);


    public abstract double getVolume();

    /**
     * Generate a shape of the region in 3D and add it to the TransformGroup
     */
    public abstract Primitive addPrimitiveForRegion(TransformGroup tg, Appearance app);


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }

}
