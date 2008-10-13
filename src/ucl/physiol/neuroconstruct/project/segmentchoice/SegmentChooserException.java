/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project.segmentchoice;



/**
 * Can be thrown by SegmentChooser subclass
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SegmentChooserException extends Exception
{
    public SegmentChooserException()
    {
        super();
    }

    public SegmentChooserException(String message)
    {
        super(message);
    }

    public SegmentChooserException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SegmentChooserException(Throwable cause)
    {
        super(cause);
    }


}
