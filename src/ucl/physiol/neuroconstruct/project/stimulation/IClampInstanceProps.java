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


public class IClampInstanceProps extends InputInstanceProps
{
    private float delay = Float.NaN;
    private float duration = Float.NaN;
    private float amplitude = Float.NaN;
    
    
    @Override
    public String details(boolean html)
    {
        return "delay: "+ GeneralUtils.getBold(delay, html)
            +", duration: "+ GeneralUtils.getBold(duration, html)
            + ", amplitude: "+ GeneralUtils.getBold(amplitude, html);
    }


    public float getAmplitude()
    {
        return amplitude;
    }

    public void setAmplitude(float amplitude)
    {
        this.amplitude = amplitude;
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
    
}