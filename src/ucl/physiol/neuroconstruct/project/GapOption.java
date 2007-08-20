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

package ucl.physiol.neuroconstruct.project;


/**
 * Options for what to do with gap between pre & post synaptic connection point
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public abstract class GapOption
{


    public GapOption()
    {
    }

    public abstract String toString();


    public class InstantaneousJump extends GapOption
    {
        public String toString()
        {
            return "No delay";
        };
    }

    public class DelayedJump extends GapOption
    {
        float signalPropSpeed = 1000;
        public String toString()
        {
            return "Delay of "+ signalPropSpeed +" ms/um??";
        };
    }

}
