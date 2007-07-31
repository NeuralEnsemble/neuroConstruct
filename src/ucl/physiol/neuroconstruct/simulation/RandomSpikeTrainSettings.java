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

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.project.cellchoice.*;


/**
 * Settings specifically for NetStim/randomspike like stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class RandomSpikeTrainSettings extends StimulationSettings
{
    public RandomSpikeTrain randomSpikeTrain = null;


    public RandomSpikeTrainSettings()
    {
        randomSpikeTrain = new RandomSpikeTrain();
    }

    public RandomSpikeTrainSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           NumberGenerator rate,
                           float noise,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrain = new RandomSpikeTrain(rate, noise, synapseType);
    }



    public ElectricalInput getElectricalInput()
    {
        return randomSpikeTrain;
    };


    public NumberGenerator getRate()
    {
        return randomSpikeTrain.getRate();
    }
    public float getNoise()
    {
        return randomSpikeTrain.getNoise();
    }






    /**
     * This is to cope with the old code, where rate was always fixed
     */
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        NumberGenerator rate = new NumberGenerator();
        rate.initialiseAsFixedFloatGenerator(fixedRate);

        randomSpikeTrain.setRate(rate);
    }

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("rate: " + rate);
        randomSpikeTrain.setRate(rate);
    }


    public void setNoise(float noise)
    {
        randomSpikeTrain.setNoise(noise);
    }


    public String toString()
    {
        return randomSpikeTrain.toString();
    }
    public String getSynapseType()
    {
        return randomSpikeTrain.getSynapseType();
    }
    public void setSynapseType(String synapseType)
    {
        randomSpikeTrain.setSynapseType(synapseType);
    }

    public static void main(String[] args)
    {

    }

}
