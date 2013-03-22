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

import java.util.ArrayList;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
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
public class IClampSettings extends StimulationSettings {

    private IClamp iclamp = null;

    public IClampSettings() {
        iclamp = new IClamp();
    }

    public IClampSettings(String reference,
            String cellGroup,
            CellChooser cellChooser,
            int segmentID,
            float delay,
            float duration,
            float amplitude,
            boolean repeat) {
        super(reference, cellGroup, cellChooser, segmentID);

        iclamp = new IClamp(delay, duration, amplitude, repeat);
    }

    public IClampSettings(String reference,
            String cellGroup,
            CellChooser cellChooser,
            int segmentID,
            NumberGenerator delay,
            NumberGenerator duration,
            NumberGenerator amplitude,
            boolean repeat) {
        super(reference, cellGroup, cellChooser, segmentID);

        iclamp = new IClamp(delay, duration, amplitude, repeat);
    }

    public IClampSettings(String reference,
            String cellGroup,
            CellChooser cellChooser,
            SegmentLocationChooser segs,
            NumberGenerator delay,
            NumberGenerator duration,
            NumberGenerator amplitude,
            boolean repeat) {
        super(reference, cellGroup, cellChooser, segs);

        iclamp = new IClamp(delay, duration, amplitude, repeat);
    }

    public Object clone() {
        IClamp iclampOrig = (IClamp) this.getElectricalInput();
        IClamp iclampClone = (IClamp) iclampOrig.clone();
        IClampSettings ics = new IClampSettings();
        if (this.getSegChooser() != null) {
            ics = new IClampSettings(this.getReference(),
                    this.getCellGroup(),
                    (CellChooser) this.getCellChooser().clone(),
                    (SegmentLocationChooser) this.getSegChooser().clone(),
                    iclampClone.getDel(),
                    iclampClone.getDur(),
                    iclampClone.getAmp(),
                    iclampClone.isRepeat());
        }else{
            ics = new IClampSettings(this.getReference(),
                    this.getCellGroup(),
                    (CellChooser) this.getCellChooser().clone(),
                    0,
                    iclampClone.getDel(),
                    iclampClone.getDur(),
                    iclampClone.getAmp(),
                    iclampClone.isRepeat());
        }
        return ics;

    }

    public ElectricalInput getElectricalInput() {
        return iclamp;
    }

    ;


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
    public void setAmp(NumberGenerator amplitude) {
        iclamp.setAmp(amplitude);
    }

    public void setDur(NumberGenerator dur) {
        iclamp.setDur(dur);
    }

    public void setDel(NumberGenerator del) {
        iclamp.setDel(del);
    }

    public NumberGenerator getDur() {
        return iclamp.getDur();
    }

    public NumberGenerator getDel() {
        return iclamp.getDel();
    }

    public NumberGenerator getAmp() {
        return iclamp.getAmp();
    }

    public boolean isRepeat() {
        return iclamp.isRepeat();
    }

    public void setRepeat(boolean repeat) {
        iclamp.setRepeat(repeat);
    }

    /// Needed for legacy projects...
    @Deprecated
    public SequenceGenerator getDuration() {
        return iclamp.getDuration();
    }

    @Deprecated
    public SequenceGenerator getDelay() {
        return iclamp.getDelay();
    }

    @Deprecated
    public SequenceGenerator getAmplitude() {
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
    public String toString() {
        return iclamp.toString();
    }

    public static void main(String[] args) {
        CellChooser cc = new IndividualCells("1, 4, 7");
        ArrayList<Integer> segs = new ArrayList<Integer>();
        segs.add(0);
        segs.add(4);

        SegmentLocationChooser slc = new IndividualSegments(segs);

        IClampSettings ic = new IClampSettings("IC1", "CG1", cc, slc,
                new NumberGenerator(20),
                new NumberGenerator(20),
                new NumberGenerator(2),
                false);

        System.out.println("IClampSettings: " + ic.toLongString());
    }
}
