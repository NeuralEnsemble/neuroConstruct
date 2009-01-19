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

import ucl.physiol.neuroconstruct.utils.GeneralUtils;




/**
 * Helper class for info on variations in amplitude, etc. of a single electrical instance
 *
 * @author Padraig Gleeson
 *  
 */


public class IClampInstanceProps extends InputInstanceProps
{
    private float delay = Float.NaN;
    private float duration = Float.NaN;
    private float amplitude = Float.NaN;
    
    
    @Override
    public String details(boolean html)
    {
        return "delay: "+ GeneralUtils.getBold(delay, html)
            +", duration: "+ GeneralUtils.getBold(duration, html)
            + ", amplitude: "+ GeneralUtils.getBold(amplitude, html);
    }


    public float getAmplitude()
    {
        return amplitude;
    }

    public void setAmplitude(float amplitude)
    {
        this.amplitude = amplitude;
    }

    public float getDelay()
    {
        return delay;
    }

    public void setDelay(float delay)
    {
        this.delay = delay;
    }

    public float getDuration()
    {
        return duration;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }
    
}
