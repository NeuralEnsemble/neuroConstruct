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

package ucl.physiol.neuroconstruct.project;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog for entry of synaptic connection properties: type, delay threshold, etc.
 *
 * @author Padraig Gleeson
 *  
 */


public class SynapticProperties
{
    private String synapseType = null;

    private NumberGenerator delayGenerator = null;
    private WeightGenerator weightsGenerator = null;
    private double threshold;


    public SynapticProperties(){};


    /**
     * Created a new SynapticProperties with some default values for the
     * delay, threshold and weight
     */
    public SynapticProperties(String synapseType)
    {
        this.synapseType = synapseType;
        delayGenerator = new NumberGenerator(5f);  // fixed at 5ms
    
        weightsGenerator = new WeightGenerator();
        weightsGenerator.initialiseAsFixedFloatGenerator(1);

        threshold = -20f; // -20 mV
    }

    /**
     * Return a simple string representation...
     */
    @Override
    public String toString()
    {
        return synapseType
            + " [thresh: "+threshold
            + ", delay: "+delayGenerator.toShortString()
            + ", weight: "+weightsGenerator.toShortString()
            +"]";
    }


    public String toNiceString()
    {
        return "Syn type: " + synapseType
            + ", threshold: "+threshold
            + ", internal delay: "+delayGenerator.toShortString()
            + ", weight: "+weightsGenerator.toShortString()
            +"";
    }



    // Functions needed for XMLEncoder...

    public double getThreshold()
    {
        return threshold;
    }
    public WeightGenerator getWeightsGenerator()
    {
        return weightsGenerator;
    }
    public String getSynapseType()
    {
        return synapseType;
    }
    public NumberGenerator getDelayGenerator()
    {
        return delayGenerator;
    }
    public void setDelayGenerator(NumberGenerator delayGenerator)
    {
        this.delayGenerator = delayGenerator;
    }

    public void setFixedDelay(float delay)
    {
        this.delayGenerator = new NumberGenerator(delay);
    }

    public void setSynapseType(String synapseType)
    {
        this.synapseType = synapseType;
    }
    public void setThreshold(double threshold)
    {
        this.threshold = threshold;
    }
    public void setWeightsGenerator(WeightGenerator weightsGenerator)
    {
        this.weightsGenerator = weightsGenerator;
    }

    public void setWeightsGenerator(NumberGenerator numGenerator) 
    {
        this.weightsGenerator = WeightGenerator.initialiseFromNumGenerator(numGenerator);
    }



}
