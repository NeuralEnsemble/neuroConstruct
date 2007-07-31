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

 package ucl.physiol.neuroconstruct.cell;

/**
 * The location along a specified segment, e.g. a (pre or post) synaptic end point
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class SegmentLocation
{
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
