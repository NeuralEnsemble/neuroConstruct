/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
    public String synapseType = null;
    public NumberGenerator delay = new NumberGenerator(0);
    public float duration = 100;
    public boolean repeat = false;


    public RandomSpikeTrainExt()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrainExt(NumberGenerator rate,
                               String synapseType,
                               NumberGenerator delay,
                               float duration,
                               boolean repeat)
   {
        this.setType(TYPE);
        this.rate = rate;
        this.synapseType = synapseType;
        this.delay = delay;
        this.duration = duration;
        this.repeat = repeat;
    }
    
    
    public Object clone()
    {
        NumberGenerator rateClone = (NumberGenerator)rate.clone();
        NumberGenerator delayClone = (NumberGenerator)delay.clone();
        
        RandomSpikeTrainExt rste = new RandomSpikeTrainExt(rateClone, new String(synapseType), delayClone, this.duration, this.repeat);
        
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

    public NumberGenerator getDelay()
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

    
    public void setDelay(float fixedDelay)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        this.delay = new NumberGenerator();
        delay.initialiseAsFixedFloatGenerator(fixedDelay);
    }
    public void setDelay(NumberGenerator delay)
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
            +rate.toShortString()+", syn: "+synapseType+", del: "+delay.toShortString()+", dur: "+duration+", repeats: "+repeat+"]";
    }
    
    public String toLinkedString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", syn: "+ ClickProjectHelper.getCellMechLink(synapseType)+", del: "+delay.toShortString()+", dur: "+duration+", repeats: "+repeat+"]";
    }

    public String getDescription()
    {
        return this.getType()+" with a rate of "
            +rate.toShortString()+" "+" and syn input type: "+synapseType+", delay: "+delay.toShortString()+", duration: "+duration+"";
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
