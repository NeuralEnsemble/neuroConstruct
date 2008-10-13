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
 * Will be thrown by SegmentChooser subclass when all cells are chosen
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class AllSegmentsChosenException extends Exception
{
    public AllSegmentsChosenException()
    {
        super("All Segments which can be chosen have been");
    }
}
