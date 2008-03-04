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

import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;


/**
 * Settings specifically for NetStim/randomspike like stimulation, extended to allow specification
 * of duration etc.
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainExt extends ElectricalInput
{
    public static final String TYPE =  "RandomSpikeTrainExt";


    //public float rate;
    /**
     * This is a NumberGenerator so that, say, if all cells in a cell group have this stim,
     * they can all have a fixed rate, or a random/gaussian set of rates, etc.
     */
    public NumberGenerator rate = null;
    public float noise;
    public String synapseType = null;


    private float delay = 0;
    private float duration = 1000000;


    private boolean repeat = false;


    public RandomSpikeTrainExt()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrainExt(NumberGenerator rate,
                               float noise,
                               String synapseType)
   {
        this.setType(TYPE);
        this.rate = rate;
        this.noise = noise;
        this.synapseType = synapseType;
    }
    
    
    public Object clone()
    {
        NumberGenerator rateClone = (NumberGenerator)rate.clone();
        
        RandomSpikeTrainExt rste = new RandomSpikeTrainExt(rateClone,this.noise, new String(synapseType));
        
        return rste;
    }

    public NumberGenerator getRate()
    {
        return rate;
    }

    public boolean isRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }

    public float getNoise()
    {
        return noise;
    }

    public float getDelay()
    {
        return delay;
    }

    public float getDuration()
    {
        return duration;
    }


    /**
     * This is left in to cope with old code where rate was always fixed
     */
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        this.rate = new NumberGenerator();
        rate.initialiseAsFixedFloatGenerator(fixedRate);
    }

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("Spiking rate being set with NumberGenerator: "+rate.toString());
        this.rate = rate;
    }


    public void setNoise(float noise)
    {
        this.noise = noise;
    }
    public void setDelay(float delay)
    {
        this.delay = delay;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }


    public String toString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", syn: "+synapseType+", del: "+delay+", dur: "+duration+", repeats: "+repeat+"]";
    }
    
    public String toLinkedString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", syn: "+ ClickProjectHelper.getCellMechLink(synapseType)+", del: "+delay+", dur: "+duration+", repeats: "+repeat+"]";
    }

    public String getDescription()
    {
        return this.getType()+" with a rate of "
            +rate.toShortString()+" "+" and syn input type: "+synapseType+", delay: "+delay+", duration: "+duration+"";
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
