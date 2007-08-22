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
  * Location of a synaptic endpoint on an axonal section
  *
  * @author Padraig Gleeson
  *  
  *
  */


@SuppressWarnings("serial")

public class PreSynapticTerminalLocation extends SegmentLocation
{
    public PreSynapticTerminalLocation(int sectionIndex,
                                       float fractionAlong)

    {
        super(sectionIndex, fractionAlong);
    }

}
