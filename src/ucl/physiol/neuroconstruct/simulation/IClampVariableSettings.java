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
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator;


/**
 * Settings specifically for IClampVariable stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 *  
 */

public class IClampVariableSettings extends StimulationSettings
{

    private IClampVariable iclampVariable = null;


    public IClampVariableSettings()
    {
        iclampVariable = new IClampVariable();
    }

    

    public IClampVariableSettings(String reference,
                          String cellGroup,
                          CellChooser cellChooser,
                          int segmentID,
                          NumberGenerator delay,
                          NumberGenerator duration,
                          String amplitude)
    {
        super(reference, cellGroup, cellChooser, segmentID);

        iclampVariable = new IClampVariable(delay,duration,amplitude);
    }

    public IClampVariableSettings(String reference,
                          String cellGroup,
                          CellChooser cellChooser,
                          SegmentLocationChooser segs,
                          NumberGenerator delay,
                          NumberGenerator duration,
                          String amplitude)
    {
        super(reference, cellGroup, cellChooser, segs);

        iclampVariable = new IClampVariable(delay,duration,amplitude);
    }
    
    
    
    public Object clone()
    {
        IClampVariable iclampOrig = (IClampVariable)this.getElectricalInput();
        
        IClampVariable iclampClone = (IClampVariable)iclampOrig.clone();
        
        IClampVariableSettings ics = new IClampVariableSettings(this.getReference(),
                                 this.getCellGroup(),
                                 (CellChooser)this.getCellChooser().clone(),
                                 (SegmentLocationChooser)this.getSegChooser().clone(),
                                 iclampClone.getDel(),
                                 iclampClone.getDur(),
                                 iclampClone.getAmp());
        
        return ics;
                                 
    }

    public ElectricalInput getElectricalInput()
    {
        return iclampVariable;
    };


    
    public void setAmp(String amplitude)
    {
        iclampVariable.setAmp(amplitude);
    }

    public void setDur(NumberGenerator dur)
    {
        iclampVariable.setDur(dur);
    }

    public void setDel(NumberGenerator del)
    {
        iclampVariable.setDel(del);
    }

    
    
    public NumberGenerator getDur()
    {
        return iclampVariable.getDur();
    }
    public NumberGenerator getDel()
    {
        return iclampVariable.getDel();
    }
    public String getAmp()
    {
        return iclampVariable.getAmp();
    }
    
    
    




    public String toString()
    {
        return iclampVariable.toString();
    }

    public static void main(String[] args)
    {
        IClampVariableSettings ic = new IClampVariableSettings();
        System.out.println("IClampVariableSettings: "+ ic);
    }
}
