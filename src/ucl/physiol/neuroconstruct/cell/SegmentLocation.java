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

 package ucl.physiol.neuroconstruct.cell;

 import java.io.*;

/**
 * The location along a specified segment, e.g. a (pre or post) synaptic end point
 *
 * @author Padraig Gleeson
 *  
 */

public class SegmentLocation implements Serializable
{
    static final long serialVersionUID = 376736284031155L;
    
    public static float DEFAULT_FRACT_CONN = 0.5f;
    
    private int segmentId;

    /**
     * Between 0 and 1
     */
    private float fractAlong;

    private SegmentLocation()
    {
    }

    public SegmentLocation(int segmentId,
                                      float fractionAlong)
    {
        this.segmentId = segmentId;
        this.fractAlong = fractionAlong;
    }

    /**
     * Between 0 and 1
     */
    public float getFractAlong()
    {
        return fractAlong;
    }

    public int getSegmentId()
    {
        return segmentId;
    }

    @Override
    public String toString()
    {
        String fullClassName = this.getClass().getName();

        String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1);

        return className
               + " [segmentId: "
               + segmentId
               + ", fractAlong: "
               + fractAlong
               + "]";

    }

}
