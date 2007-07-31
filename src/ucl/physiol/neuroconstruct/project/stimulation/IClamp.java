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

package ucl.physiol.neuroconstruct.project.stimulation;

import ucl.physiol.neuroconstruct.utils.*;


/**
 * Settings specifically for single IClamp stimulation
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class IClamp extends ElectricalInput
{
    public static final String TYPE =  "IClamp";

    private SequenceGenerator delay = new SequenceGenerator(20);
    private SequenceGenerator duration = new SequenceGenerator(80);
    private SequenceGenerator amplitude = new SequenceGenerator(0.1f);

    private boolean repeat = false;


    public IClamp()
    {
        this.setType(TYPE);
    }

    public IClamp(float delay,
                  float duration,
                  float amplitude,
                  boolean repeat)
    {
        this.setType(TYPE);

        this.delay = new SequenceGenerator(delay);
        this.duration = new SequenceGenerator(duration);
        this.amplitude = new SequenceGenerator(amplitude);
        this.repeat = repeat;
    }

    public IClamp(SequenceGenerator delay,
                  SequenceGenerator duration,
                  SequenceGenerator amplitude,
                  boolean repeat)
    {
        this.setType(TYPE);
        this.delay = delay;
        this.duration = duration;
        this.amplitude = amplitude;
        this.repeat = repeat;
    }



    public SequenceGenerator getAmplitude()
    {
        return amplitude;
    }
    public SequenceGenerator getDuration()
    {
        return duration;
    }
    public SequenceGenerator getDelay()
    {
        return delay;
    }


    public boolean isRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }


    public void setAmplitude(float amplitude)
    {
        this.amplitude = new SequenceGenerator(amplitude);
    }

    public void setAmplitude(SequenceGenerator amplitude)
    {
        this.amplitude = amplitude;
    }



    public void setDuration(float duration)
    {
        this.duration = new SequenceGenerator(duration);
    }

    public void setDuration(SequenceGenerator duration)
    {
        this.duration = duration;
    }


    public void setDelay(float delay)
    {
        this.delay = new SequenceGenerator(delay);
    }



    public void setDelay(SequenceGenerator delay)
    {
        this.delay = delay;
    }




    public String toString()
    {
        return this.getType()+": [del: "+ delay
            +", dur: "+ duration
            + ", amp: "+ amplitude + ", repeats: "+repeat+"]";
    }
}
