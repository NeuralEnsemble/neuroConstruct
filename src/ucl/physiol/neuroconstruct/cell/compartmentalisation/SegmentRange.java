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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;


/**
 * Helper class for compartmentalisations. Defines a segment Id, it's length, and a range
 * defined by start and end fractions (0 to 1). This forms the basis of the SegmentLocMapper
 * where a SegmentRange of one cell (e.g seg of len 10, 0 to 1) is mapped to a recompartmentalised cell
 * e.g. seg of len 20, 0 to 0.5
 *
 * @author Padraig Gleeson
 *  
 * @see     SegmentLocMapper
 *
 */

public class SegmentRange
{
    private int segmentId = -1;
    private float totalSegmentLength = -1;
    private float startFract = 0;
    private float endFract = 1;

    public SegmentRange(int segmentId, float totalSegmentLength, float startFract, float endFract)
    {
        this.segmentId = segmentId;
        this.totalSegmentLength = totalSegmentLength;
        if (startFract<0) startFract = 0;
        this.startFract = startFract;
        if (endFract>1) endFract = 1;
        this.endFract = endFract;
    }

    public int getSegmentId()
    {
        return this.segmentId;
    }

    public float getTotalSegmentLength()
    {
        return this.totalSegmentLength;
    }
    public float getStartFract()
    {
        return this.startFract;
    }
    public float getEndFract()
    {
        return this.endFract;
    }


    /**
     * The length of the segment between the start and end of the range
     */
    public float getRangeLength()
    {
        return totalSegmentLength * (endFract-startFract);
    }

    public String toString()
    {
        return "SegmentRange [id: "+segmentId+", tot len: "+ totalSegmentLength+", ("+startFract+"->"+endFract+"), range len: "+getRangeLength()+"]";
    }


}

