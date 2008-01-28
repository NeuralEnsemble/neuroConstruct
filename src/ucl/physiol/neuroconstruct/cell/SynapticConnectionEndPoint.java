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

 package ucl.physiol.neuroconstruct.cell;

 /**
  * The location of a (pre or post) synaptic end point, along with which
  * cell its on
  *
  * @author Padraig Gleeson
  *  
  */

public class SynapticConnectionEndPoint
{
    public SegmentLocation location;
    public int cellNumber;

    public SynapticConnectionEndPoint(SegmentLocation location,
                               int cellNumber)
    {
        this.location = location;
        this.cellNumber = cellNumber;
    }

    public String toString()
    {
        return "SynapticConnectionLocation [location: "+location+", cellNumber: "+cellNumber+"]";
    }

}
