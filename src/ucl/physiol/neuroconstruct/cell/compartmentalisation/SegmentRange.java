
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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;


/**
 * Helper class for compartmentalisations. Defines a segment Id, it's length, and a range
 * defined by start and end fractions (0 to 1). This forms the basis of the SegmentLocMapper
 * where a SegmentRange of one cell (e.g seg of len 10, 0 to 1) is mapped to a recompartmentalised cell
 * e.g. seg of len 20, 0 to 0.5
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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

