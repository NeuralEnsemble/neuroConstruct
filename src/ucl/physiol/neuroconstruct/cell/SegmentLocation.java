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
