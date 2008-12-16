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

package ucl.physiol.neuroconstruct.project.stimulation;

import ucl.physiol.neuroconstruct.utils.GeneralUtils;




/**
 * Helper class for info on variations in amplitude, etc. of a single electrical instance
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainInstanceProps extends InputInstanceProps
{
    private float rate = Float.NaN;
    private String synapseType = null;
    
    @Override
    public String details(boolean html)
    {
        return "rate: "+ GeneralUtils.getBold(rate, html)
              +", type: "+ GeneralUtils.getBold(synapseType, html);
    }
    
    public float getRate()
    {
        return rate;
    }

    public void setRate(float rate)
    {
        this.rate = rate;
    }
    
    public String getSynapseType()
    {
        return synapseType;
    }

    public void setSynapseType(String synapseType)
    {
        this.synapseType = synapseType;
    }
}
