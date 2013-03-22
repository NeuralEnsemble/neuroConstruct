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
    private NumberGenerator rate = null;
    private String synapseType = null;
    private NumberGenerator delay = new NumberGenerator(0);
    private NumberGenerator duration = new NumberGenerator(100);
    private boolean repeat = false;


    public RandomSpikeTrainExt()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrainExt(NumberGenerator rate,
                               String synapseType,
                               NumberGenerator delay,
                               NumberGenerator duration,
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
        NumberGenerator durationClone = (NumberGenerator)duration.clone();
        
        RandomSpikeTrainExt rste = new RandomSpikeTrainExt(rateClone, new String(synapseType), delayClone, durationClone, this.repeat);
        
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

    public NumberGenerator getDuration()
    {
        return duration;
    }


    /**
     * This is left in to cope with old code where rate was always fixed
     */
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        this.rate = new NumberGenerator(fixedRate);
    }

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("Spiking rate being set with NumberGenerator: "+rate.toString());
        this.rate = rate;
    }

    
    public void setDelay(float fixedDelay)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        this.delay = new NumberGenerator(fixedDelay);
    }
    public void setDelay(NumberGenerator delay)
    {
        this.delay = delay;
    }

    public void setDuration(float duration)
    {
        this.duration = new NumberGenerator(duration);
    }
    public void setDuration(NumberGenerator duration)
    {
        this.duration = duration;
    }


    public String toString()
    {
        return this.getType()+": [rate: "
            +(rate==null?"???":rate.toShortString())+", syn: "+synapseType
            +", del: "+(delay==null?"???":delay.toShortString())+", dur: "+(duration==null?"???":duration.toShortString())+", repeats: "+repeat+"]";
    }
    
    public String toLinkedString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", syn: "+ ClickProjectHelper.getCellMechLink(synapseType)+", del: "+delay.toShortString()+", dur: "+duration.toShortString()+", repeats: "+repeat+"]";
    }

    public String getDescription()
    {
        return this.getType()+" with a rate of "
            +rate.toShortString()+" "+" and syn input type: "+synapseType+", delay: "+delay.toShortString()+", duration: "+duration.toShortString()+"";
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
