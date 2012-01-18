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
    private RandomSpikeTrainExt randomSpikeTrainExt = null;


    public RandomSpikeTrainExtSettings()
    {
        randomSpikeTrainExt = new RandomSpikeTrainExt();
    }

    public RandomSpikeTrainExtSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           NumberGenerator rate,
                           String synapseType,
                           NumberGenerator delay,
                           NumberGenerator duration,
                           boolean repeat)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrainExt = new RandomSpikeTrainExt(rate, synapseType, delay, duration, repeat);
    }
    
    public RandomSpikeTrainExtSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           SegmentLocationChooser segs,
                           NumberGenerator rate,
                           String synapseType,
                           NumberGenerator delay,
                           NumberGenerator duration,
                           boolean repeat)
    {
        super(reference, cellGroup, cellChooser, segs);
        randomSpikeTrainExt = new RandomSpikeTrainExt(rate, synapseType, delay, duration, repeat);
    }
    
    
    
    public Object clone()
    {
        RandomSpikeTrainExt rstOrig = (RandomSpikeTrainExt)this.getElectricalInput();
        
        RandomSpikeTrainExt rstClone = (RandomSpikeTrainExt)rstOrig.clone();
        
        RandomSpikeTrainExtSettings rsts = new RandomSpikeTrainExtSettings(this.getReference(),
                                 this.getCellGroup(),
                                 (CellChooser)this.getCellChooser().clone(),
                                 (SegmentLocationChooser)this.getSegChooser().clone(),
                                 rstClone.getRate(),
                                 rstClone.getSynapseType(),
                                 rstClone.getDelay(),
                                 rstClone.getDuration(),
                                 rstClone.isRepeat());
        
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



    public NumberGenerator getDelay()
    {
        return randomSpikeTrainExt.getDelay();
    }

    public NumberGenerator getDuration()
    {
        return randomSpikeTrainExt.getDuration();
    }


    public void setDelay(float fixedDelay)
    {
        NumberGenerator delay = new NumberGenerator(fixedDelay);

        randomSpikeTrainExt.setDelay(delay);
    }
    
    public void setDelay(NumberGenerator delay)
    {
        randomSpikeTrainExt.setDelay(delay);
    }

    public void setDuration(float duration)
    {
        randomSpikeTrainExt.setDuration(duration);
    }


    public void setDuration(NumberGenerator duration)
    {
        randomSpikeTrainExt.setDuration(duration);
    }



    public NumberGenerator getRate()
    {
        return randomSpikeTrainExt.getRate();
    }
    
    /**
     * This is to cope with the old code, where rate was always fixed
     */
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        NumberGenerator rate = new NumberGenerator(fixedRate);

        randomSpikeTrainExt.setRate(rate);
    }

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("rate: " + rate);
        randomSpikeTrainExt.setRate(rate);
    }


    public void setNoise(float noise)
    {
        // Note: noise removed from randomSpikeTrainExt
        //randomSpikeTrainExt.setNoise(noise);
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
