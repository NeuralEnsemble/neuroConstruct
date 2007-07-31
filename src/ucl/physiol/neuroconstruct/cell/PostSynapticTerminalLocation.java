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
  *
  * Location of a synaptic endpoint on a dendritic section
  *
  * @author Padraig Gleeson
  * @version 1.0.4
  *
  */


public class PostSynapticTerminalLocation extends SegmentLocation
{

    public PostSynapticTerminalLocation(int segmentId,
                                        float fractionAlong)
    {
        super(segmentId, fractionAlong);
    }
}
