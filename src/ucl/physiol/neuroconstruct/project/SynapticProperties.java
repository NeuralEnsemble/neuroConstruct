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

package ucl.physiol.neuroconstruct.project;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;

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
        try {
            // fixed at 5ms
            weightsGenerator = new WeightGenerator("1", false); // fixed at 1 each
        } catch (EquationException ex) 
        {
            // equation 1 should not throw an error
        }
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
