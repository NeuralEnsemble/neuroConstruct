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
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator;


/**
 * Settings specifically for IClamp stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 *  
 */

public class IClampSettings extends StimulationSettings
{

    private IClamp iclamp = null;


    public IClampSettings()
    {
        iclamp = new IClamp();
    }

    public IClampSettings(String reference,
                          String cellGroup,
                          CellChooser cellChooser,
                          int segmentID,
                          float delay,
                          float duration,
                          float amplitude,
                          boolean repeat)
    {
        super(reference, cellGroup, cellChooser, segmentID);

        iclamp = new IClamp(delay,duration,amplitude, repeat);
    }
    

    public IClampSettings(String reference,
                          String cellGroup,
                          CellChooser cellChooser,
                          int segmentID,
                          NumberGenerator delay,
                          NumberGenerator duration,
                          NumberGenerator amplitude,
                          boolean repeat)
    {
        super(reference, cellGroup, cellChooser, segmentID);

        iclamp = new IClamp(delay,duration,amplitude, repeat);
    }
    
    
    
    public Object clone()
    {
        IClamp iclampOrig = (IClamp)this.getElectricalInput();
        
        IClamp iclampClone = (IClamp)iclampOrig.clone();
        
        IClampSettings ics = new IClampSettings(this.reference,
                                 this.cellGroup,
                                 (CellChooser)this.cellChooser.clone(),
                                 this.segmentID,
                                 iclampClone.getDel(),
                                 iclampClone.getDur(),
                                 iclampClone.getAmp(),
                                 iclampClone.isRepeat());
        
        return ics;
                                 
    }

    public ElectricalInput getElectricalInput()
    {
        return iclamp;
    };


    /*
    public void setAmplitude(SequenceGenerator amplitude)
    {
        iclamp.setAmplitude(amplitude);
    }

    public void setDuration(SequenceGenerator dur)
    {
        iclamp.setDuration(dur);
    }

    public void setDelay(SequenceGenerator del)
    {
        iclamp.setDelay(del);
    }*/
    
    public void setAmp(NumberGenerator amplitude)
    {
        iclamp.setAmp(amplitude);
    }

    public void setDur(NumberGenerator dur)
    {
        iclamp.setDur(dur);
    }

    public void setDel(NumberGenerator del)
    {
        iclamp.setDel(del);
    }

    
    
    public NumberGenerator getDur()
    {
        return iclamp.getDur();
    }
    public NumberGenerator getDel()
    {
        return iclamp.getDel();
    }
    public NumberGenerator getAmp()
    {
        return iclamp.getAmp();
    }
    
    
    

    public boolean isRepeat()
    {
        return iclamp.isRepeat();
    }

    public void setRepeat(boolean repeat)
    {
        iclamp.setRepeat(repeat);
    }



    /// Needed for legacy projects...
    public SequenceGenerator getDuration()
    {
        return iclamp.getDuration();
    }
    public SequenceGenerator getDelay()
    {
        return iclamp.getDelay();
    }
    public SequenceGenerator getAmplitude()
    {
        return iclamp.getAmplitude();
    }
    
    
    /*
    public void setAmplitude(float amplitude)
    {
        iclamp.setAmplitude(amplitude);
    }
    public void setDuration(float duration)
    {
        iclamp.setDuration(duration);
    }
    public void setDelay(float delay)
    {
        iclamp.setDelay(delay);
    }*/






    public String toString()
    {
        return iclamp.toString();
    }

    public static void main(String[] args)
    {
        IClampSettings ic = new IClampSettings();
        System.out.println("IClampSettings: "+ ic);
    }
}
