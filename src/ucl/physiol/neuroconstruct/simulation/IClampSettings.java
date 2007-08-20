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
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator;


/**
 * Settings specifically for IClamp stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 * @version 1.0.6
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

    public ElectricalInput getElectricalInput()
    {
        return iclamp;
    };

    public SequenceGenerator getAmplitude()
    {
        return iclamp.getAmplitude();
    }

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
    }


    public boolean isRepeat()
    {
        return iclamp.isRepeat();
    }

    public void setRepeat(boolean repeat)
    {
        iclamp.setRepeat(repeat);
    }




    public SequenceGenerator getDuration()
    {
        return iclamp.getDuration();
    }
    public void setAmplitude(float amplitude)
    {
        iclamp.setAmplitude(amplitude);
    }
    public void setDuration(float duration)
    {
        iclamp.setDuration(duration);
    }


    public SequenceGenerator getDelay()
    {
        return iclamp.getDelay();
    }
    public void setDelay(float delay)
    {
        iclamp.setDelay(delay);
    }




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
