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

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;


/**
 * Settings specifically for extended NetStim/randomspike like stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainExtSettings extends StimulationSettings
{
    public RandomSpikeTrainExt randomSpikeTrainExt = null;


    public RandomSpikeTrainExtSettings()
    {
        randomSpikeTrainExt = new RandomSpikeTrainExt();
    }

    public RandomSpikeTrainExtSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           NumberGenerator rate,
                           float noise,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrainExt = new RandomSpikeTrainExt(rate, noise, synapseType);
    }
    
    public RandomSpikeTrainExtSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           SegmentLocationChooser segs,
                           NumberGenerator rate,
                           float noise,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segs);
        randomSpikeTrainExt = new RandomSpikeTrainExt(rate, noise, synapseType);
    }
    
    
    
    public Object clone()
    {
        RandomSpikeTrainExt rstOrig = (RandomSpikeTrainExt)this.getElectricalInput();
        
        RandomSpikeTrainExt rstClone = (RandomSpikeTrainExt)rstOrig.clone();
        
        RandomSpikeTrainExtSettings rsts = new RandomSpikeTrainExtSettings(this.reference,
                                 this.cellGroup,
                                 (CellChooser)this.cellChooser.clone(),
                                 (SegmentLocationChooser)this.segmentChooser.clone(),
                                 rstClone.getRate(),
                                 rstClone.getNoise(),
                                 rstClone.getSynapseType());
        
        return rsts;
                                 
    }



    public ElectricalInput getElectricalInput()
    {
        return randomSpikeTrainExt;
    };


    public boolean isRepeat()
    {
        return randomSpikeTrainExt.isRepeat();
    }

    public void setRepeat(boolean repeat)
    {
        randomSpikeTrainExt.setRepeat(repeat);
    }



    public float getDelay()
    {
        return randomSpikeTrainExt.getDelay();
    }

    public float getDuration()
    {
        return randomSpikeTrainExt.getDuration();
    }


    public void setDelay(float delay)
    {
        randomSpikeTrainExt.setDelay(delay);
    }

    public void setDuration(float duration)
    {
        randomSpikeTrainExt.setDuration(duration);
    }



    public NumberGenerator getRate()
    {
        return randomSpikeTrainExt.getRate();
    }
    public float getNoise()
    {
        return randomSpikeTrainExt.getNoise();
    }
    /**
     * This is to cope with the old code, where rate was always fixed
     */
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        NumberGenerator rate = new NumberGenerator();
        rate.initialiseAsFixedFloatGenerator(fixedRate);

        randomSpikeTrainExt.setRate(rate);
    }

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("rate: " + rate);
        randomSpikeTrainExt.setRate(rate);
    }


    public void setNoise(float noise)
    {
        randomSpikeTrainExt.setNoise(noise);
    }


    public String toString()
    {
        return randomSpikeTrainExt.toString();
    }
    public String getSynapseType()
    {
        return randomSpikeTrainExt.getSynapseType();
    }
    public void setSynapseType(String synapseType)
    {
        randomSpikeTrainExt.setSynapseType(synapseType);
    }

    public static void main(String[] args)
    {

    }

}
