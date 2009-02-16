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
 * Settings specifically for single IClampVariable stimulation
 *
 * @author Padraig Gleeson
 *  
 */

public class IClampVariable extends ElectricalInput
{
    private static ClassLogger logger = new ClassLogger("IClampVariable");
    
    
    public static final String TYPE =  "IClampVariable";
    
    private NumberGenerator delay = new NumberGenerator(20f);
    private NumberGenerator duration = new NumberGenerator(60);
    private String amplitude = new String();
    

    public IClampVariable()
    {
        this.setType(TYPE);
    } 

    public IClampVariable(float delay,
                  float duration,
                  String amplitude,
                  boolean repeat)
    {
        this.setType(TYPE);
        
        this.delay = new NumberGenerator(delay);
        this.duration = new NumberGenerator(duration);
        this.amplitude = amplitude;
        
    }

    
    public IClampVariable(NumberGenerator delay,
                  NumberGenerator duration,
                  String amplitude)
    {
        this.setType(TYPE);
        this.delay = delay;
        this.duration = duration;
        this.amplitude = amplitude;
    }
    
   
    public Object clone()
    {
        IClampVariable ic = new IClampVariable((NumberGenerator)this.delay.clone(),
                               (NumberGenerator)this.duration.clone(),
                               new String(this.amplitude));
        return null;
    };




    
    
    public String getAmp()
    {
        return amplitude;
    }


    public NumberGenerator getDur()
    {
        return duration;
    }
    public NumberGenerator getDel()
    {
        return delay;
    }

    public void setAmp(String amplitude)
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
    

    
    public String toLinkedString()
    {
        return toString();
    }


    @Override
    public String toString()
    {
        return this.getType()+": [del: "+ getDel().toShortString()
            +", dur: "+ getDur().toShortString()
            + ", amp(t) = "+ getAmp() + "]";
    }
}
