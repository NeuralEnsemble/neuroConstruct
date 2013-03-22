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
 * Settings specifically for single NetStim/randomspike like stimulation
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrain extends ElectricalInput
{
    public static final String TYPE =  "RandomSpikeTrain";


    //public float rate;
    /**
     * This is a NumberGenerator so that, say, if all cells in a cell group have this stim,
     * they can all have a fixed rate, or a random/gaussian set of rates, etc.
     */
    private NumberGenerator rate = null;
    //private float noise;

    
    private String synapseType = null;

    public RandomSpikeTrain()
    {
        this.setType(TYPE);
    }

    public RandomSpikeTrain(NumberGenerator rate,
                           String synapseType)
   {
        this.setType(TYPE);
        this.rate = rate;
        //this.noise = de;
        this.synapseType = synapseType;
    }

    /*
     * noise not currently supported
     */
    @Deprecated
    public RandomSpikeTrain(NumberGenerator rate,
                           float noise,
                           String synapseType)
   {
        this.setType(TYPE);
        this.rate = rate;
        //this.noise = noise;
        this.synapseType = synapseType;
    }
    
    public Object clone()
    {
        NumberGenerator rateClone = (NumberGenerator)rate.clone();
        
        RandomSpikeTrain rst = new RandomSpikeTrain(rateClone,new String(synapseType));
        
        return rst;
    }

    public NumberGenerator getRate()
    {
        return rate;
    }


    //public float getNoise()
    //{
    //    return noise;
    //}

    /**
     * This is left in to cope with old code where rate was always fixed
  
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        this.rate = new NumberGenerator(fixedRate);
    }   */

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("Spiking rate being set with NumberGenerator: "+rate.toString());
        this.rate = rate;
    }


    //public void setNoise(float noise)
    //{
    //    this.noise = noise;
    //}
    
    @Override
    public String toString()
    {
        return this.getType()+": [rate: "
            +(rate==null?"???":rate.toShortString())+", synaptic input: "+synapseType+"]";
    }
    
    
    public String toLinkedString()
    {
        return this.getType()+": [rate: "
            +rate.toShortString()+", synaptic input: "+ ClickProjectHelper.getCellMechLink(synapseType)+"]";
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
