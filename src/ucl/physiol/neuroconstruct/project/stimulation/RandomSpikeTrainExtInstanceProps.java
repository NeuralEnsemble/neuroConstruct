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

import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;




/**
 * Helper class for info on variations in amplitude, etc. of a single electrical instance
 *
 * @author Matteo Farinella
 *  
 */


public class RandomSpikeTrainExtInstanceProps extends InputInstanceProps
{
    public float rate = Float.NaN;
    public String SynapseType = null;
    public float delay = Float.NaN;
    public float duration = Float.NaN;
    public boolean repeat = false;
    
    @Override
    public String details(boolean html)
    {
        return "rate: "+ GeneralUtils.getBold(rate, html)
              +", type: "+ GeneralUtils.getBold(SynapseType, html)
              +", delay: "+ GeneralUtils.getBold(delay, html)
              +", duration: "+ GeneralUtils.getBold(duration, html);
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
        return SynapseType;
    }

    public void setSynapseType(String synapseType)
    {
        this.SynapseType = synapseType;
    }
    
     public float getDelay()
    {
        return delay;
    }

    public void setDelay(float delay)
    {
        this.delay = delay;
    }
    
    public float getDuration()
    {
        return duration;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }
    
    public boolean getRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }
}
