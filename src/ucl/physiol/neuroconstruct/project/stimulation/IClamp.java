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

import ucl.physiol.neuroconstruct.utils.*;


/**
 * Settings specifically for single IClamp stimulation
 *
 * @author Padraig Gleeson
 *  
 */

public class IClamp extends ElectricalInput
{
    private static ClassLogger logger = new ClassLogger("IClamp");
    
    private boolean warningShown = false;
    
    public static final String TYPE =  "IClamp";
    
    private NumberGenerator delay = new NumberGenerator(20f);
    private NumberGenerator duration = new NumberGenerator(60);
    private NumberGenerator amplitude = new NumberGenerator(0.1f);
    
    private SequenceGenerator dummySG = new SequenceGenerator(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    private SequenceGenerator oldDelay = (SequenceGenerator)dummySG.clone();
    private SequenceGenerator oldDuration = (SequenceGenerator)dummySG.clone();
    private SequenceGenerator oldAmplitude = (SequenceGenerator)dummySG.clone();

    private boolean repeat = false;


    public IClamp()
    {
        this.setType(TYPE);
    } 

    public IClamp(float delay,
                  float duration,
                  float amplitude,
                  boolean repeat)
    {
        this.setType(TYPE);
//
//        this.delay = new SequenceGenerator(delay);
//        this.duration = new SequenceGenerator(duration);
//        this.amplitude = new SequenceGenerator(amplitude);
        
        this.delay = new NumberGenerator(delay);
        this.duration = new NumberGenerator(duration);
        this.amplitude = new NumberGenerator(amplitude);
//        this.duration = new SequenceGenerator(duration);
//        this.amplitude = new SequenceGenerator(amplitude);
        
        this.repeat = repeat;
    }

    
    public IClamp(NumberGenerator delay,
                  NumberGenerator duration,
                  NumberGenerator amplitude,
                  boolean repeat)
    {
        this.setType(TYPE);
        this.delay = delay;
        this.duration = duration;
        this.amplitude = amplitude;
        this.repeat = repeat;
    }
    
   
    public Object clone()
    {
        IClamp ic = new IClamp((NumberGenerator)this.delay.clone(),
                               (NumberGenerator)this.duration.clone(),
                               (NumberGenerator)this.amplitude.clone(),
                                this.repeat);
        return ic;
    };



    // Functions for legacy code
    public SequenceGenerator getAmplitude()
    {
        return oldAmplitude;
    }
    public SequenceGenerator getDuration()
    {
        //showWarning();
        return oldDuration;
    }
    public SequenceGenerator getDelay()
    {
        //showWarning();
        return oldDelay;
    }


    public boolean isRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }

    /*
    public void setAmp(float amplitude)
    {
        this.amplitude = new NumberGenerator(amplitude);
    }
    
    public void setDur(float duration)
    {
        this.duration = new NumberGenerator(duration);
    }
    
    public void setDel(float delay)
    {
        this.delay = new NumberGenerator(delay);
    }*/
    
    
    
    public NumberGenerator getAmp()
    {
        if (!oldAmplitude.equals(dummySG))
        {
            amplitude = new NumberGenerator(oldAmplitude.getStart());
            if (oldAmplitude.getNumInSequence()>1) 
                showWarning();
            oldAmplitude = (SequenceGenerator)dummySG.clone();
        }
        return amplitude;
    }
    public NumberGenerator getDur()
    {
        if (!oldDuration.equals(dummySG))
        {
            duration = new NumberGenerator(oldDuration.getStart());
            if (oldDuration.getNumInSequence()>1) 
                showWarning();
            oldDuration = (SequenceGenerator)dummySG.clone();
        }
        return duration;
    }
    public NumberGenerator getDel()
    {
        if (!oldDelay.equals(dummySG))
        {
            delay = new NumberGenerator(oldDelay.getStart());
            if (oldDelay.getNumInSequence()>1) 
                showWarning();
            oldDelay = (SequenceGenerator)dummySG.clone();
        }
        return delay;
    }

    public void setAmp(NumberGenerator amplitude)
    {
        this.amplitude = amplitude;
    }
    
    public void setDur(NumberGenerator duration)
    {
        this.duration = duration;
    }
    
    public void setDel(NumberGenerator delay)
    {
        this.delay = delay;
    }
    
    
    public void showWarning()
    {
        if (!warningShown)
        {
            GuiUtils.showErrorMessage(logger, 
                "Note: the generation of multiple simulations by specifying sequences of values for current clamp amplitude, etc.\n" +
                "has been removed in this version of neuroConstruct. It is replaced with the option to set a range of values for current\n" +
                "amplitudes, etc. allowing random input currents/rates to be applied to members of a Cell Group during a simulation.\n\n" +
                "The currently preferred way to generate multiple simulations is with the new Python based interface. Please get in touch\n" +
                "(p.gleeson@ucl.ac.uk) for a beta copy. ", null, null);
            
            warningShown = true;
        }
    }



    /*   No longer needed
    public void setDuration(SequenceGenerator duration)
    {
        showWarning();
    }
    public void setAmplitude(SequenceGenerator amplitude)
    {
        showWarning();
    }
    public void setDelay(SequenceGenerator delay)
    {
        showWarning();
    }*/

    
    
    public String toLinkedString()
    {
        return toString();
    }


    @Override
    public String toString()
    {
        return this.getType()+": [del: "+ getDel().toShortString()
            +", dur: "+ getDur().toShortString()
            + ", amp: "+ getAmp().toShortString() + ", repeats: "+repeat+"]";
    }
}
