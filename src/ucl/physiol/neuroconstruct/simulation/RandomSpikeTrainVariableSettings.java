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


public class RandomSpikeTrainVariableSettings extends StimulationSettings
{
    private RandomSpikeTrainVariable randomSpikeTrainVariable = null;


    public RandomSpikeTrainVariableSettings()
    {
        randomSpikeTrainVariable = new RandomSpikeTrainVariable();
    }

    public RandomSpikeTrainVariableSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           String rate,
                           String synapseType,
                           NumberGenerator delay,
                           NumberGenerator duration)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrainVariable = new RandomSpikeTrainVariable(rate, synapseType, delay, duration);
    }
    
    public RandomSpikeTrainVariableSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           SegmentLocationChooser segs,
                           String rate,
                           String synapseType,
                           NumberGenerator delay,
                           NumberGenerator duration)
    {
        super(reference, cellGroup, cellChooser, segs);
        randomSpikeTrainVariable = new RandomSpikeTrainVariable(rate, synapseType, delay, duration);
    }
    
    
    
    public Object clone()
    {
        RandomSpikeTrainVariable rstOrig = (RandomSpikeTrainVariable)this.getElectricalInput();
        
        RandomSpikeTrainVariable rstClone = (RandomSpikeTrainVariable)rstOrig.clone();
        
        RandomSpikeTrainVariableSettings rsts = new RandomSpikeTrainVariableSettings(this.getReference(),
                                 this.getCellGroup(),
                                 (CellChooser)this.getCellChooser().clone(),
                                 (SegmentLocationChooser)this.getSegChooser().clone(),
                                 rstClone.getRate(),
                                 rstClone.getSynapseType(),
                                 rstClone.getDelay(),
                                 rstClone.getDuration());
        
        return rsts;
                                 
    }



    public ElectricalInput getElectricalInput()
    {
        return randomSpikeTrainVariable;
    };




    public NumberGenerator getDelay()
    {
        return randomSpikeTrainVariable.getDelay();
    }

    public NumberGenerator getDuration()
    {
        return randomSpikeTrainVariable.getDuration();
    }


    
    public void setDelay(NumberGenerator delay)
    {
        randomSpikeTrainVariable.setDelay(delay);
    }

    public void setDuration(NumberGenerator duration)
    {
        randomSpikeTrainVariable.setDuration(duration);
    }



    public String getRate()
    {
        return randomSpikeTrainVariable.getRate();
    }
    

    public void setRate(String rate)
    {
        randomSpikeTrainVariable.setRate(rate);
    }




    public String toString()
    {
        return randomSpikeTrainVariable.toString();
    }
    public String getSynapseType()
    {
        return randomSpikeTrainVariable.getSynapseType();
    }
    public void setSynapseType(String synapseType)
    {
        randomSpikeTrainVariable.setSynapseType(synapseType);
    }


}
