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
 * Class for extra info on single connection
 *
 * @author Padraig Gleeson
 *  
 */

public class ConnSpecificProps
{
    /** @todo Put in get set etc */
    public String synapseType = null;
    public float weight = 1;
    public float internalDelay = 0;
    //public float threshold = 0;

    private ConnSpecificProps()
    {

    }
    public ConnSpecificProps(String synapseType)
    {
        this.synapseType = synapseType;
    }

    public String toString()
    {
        return "ConnSpecificProps [synapseType: "+synapseType
            +", internalDelay: "+internalDelay+", weight: "+weight+"]";
    }

    public String toNiceString()
    {
        return "Props for "+synapseType
            +": internal delay: "+internalDelay+" ms, weight: "+weight+"";
    }



}



