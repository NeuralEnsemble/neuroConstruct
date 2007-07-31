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

import ucl.physiol.neuroconstruct.utils.NumberGenerator;


/**
 * Settings specifically for single NetStim/randomspike like stimulation
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class RandomSpikeTrain extends ElectricalInput
{
    public static final String TYPE =  "RandomSpikeTrain";


    //public float rate;
    /**
     * This is a NumberGenerator so that, say, if all cells in a cell group have this stim,
     * they can all have a fixed rate, or a random/gaussian set of rates, etc.
     */
    public NumberGenerator rate = null;


    public float noise;
    public String synapseType = null;

    public RandomSpikeTrain()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrain(NumberGenerator rate,
                           float noise,
                           String synapseType)
   {
        this.setType(TYPE);
        this.rate = rate;
        this.noise = noise;
        this.synapseType = synapseType;
    }

    public NumberGenerator getRate()
    {
        return rate;
    }


    public float getNoise()
    {
        return noise;
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

    public String toString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", synaptic input: "+synapseType+"]";
    }

    public String getDescription()
    {
        return this.getType()+" with a rate of "
            +rate.toShortString()+" "+" and synaptic input type: "+synapseType+"";
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
