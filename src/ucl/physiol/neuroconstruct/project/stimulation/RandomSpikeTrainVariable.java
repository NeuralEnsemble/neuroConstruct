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
 * Settings specifically for NetStim/randomspike like stimulation, extended to 
 * allow variable rate
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainVariable extends ElectricalInput
{
    public static final String TYPE =  "RandomSpikeTrainVariable";


    private String rate = null; // Expression for rate(t)
    
    private String synapseType = null;
    private NumberGenerator delay = new NumberGenerator(0);
    private NumberGenerator duration = new NumberGenerator(100);


    public RandomSpikeTrainVariable()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrainVariable(String rate,
                               String synapseType,
                               NumberGenerator delay,
                               NumberGenerator duration)
   {
        this.setType(TYPE);
        this.rate = rate;
        this.synapseType = synapseType;
        this.delay = delay;
        this.duration = duration;
    }
    
    
    public Object clone()
    {
        String rateClone = new String(rate);
        NumberGenerator delayClone = (NumberGenerator)delay.clone();
        NumberGenerator durClone = (NumberGenerator)duration.clone();
        
        RandomSpikeTrainVariable rste = new RandomSpikeTrainVariable(rateClone, new String(synapseType), delayClone, durClone);
        
        return rste;
    }

    public String getRate()
    {
        return rate;
    }


    public NumberGenerator getDelay()
    {
        return delay;
    }

    public NumberGenerator getDuration()
    {
        return duration;
    }



    public void setRate(String rate)
    {

        this.rate = rate;
    }

    
    public void setDelay(NumberGenerator delay)
    {
        this.delay = delay;
    }

    public void setDuration(NumberGenerator duration)
    {
        this.duration = duration;
    }


    public String toString()
    {
        return this.getType()+": [rate(t) = "
            +rate+", syn: "+synapseType+", del: "+delay.toShortString()+", dur: "+duration.toShortString()+"]";
    }
    
    public String toLinkedString()
    {
        return this.getType()+": [rate(t) = "
            +rate+", syn: "+ ClickProjectHelper.getCellMechLink(synapseType)+", del: "+delay.toShortString()+", dur: "+duration.toShortString()+"]";
    }

    public String getDescription()
    {
        return this.getType()+" with rate(t) = "
            +rate+" "+" and syn input type: "+synapseType+", delay: "+delay.toShortString()+", duration: "+duration.toShortString()+"";
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
