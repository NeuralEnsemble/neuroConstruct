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

package ucl.physiol.neuroconstruct.project.stimulation;

import ucl.physiol.neuroconstruct.utils.GeneralUtils;




/**
 * Helper class for info on variations in amplitude, etc. of a single electrical instance
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainInstanceProps extends InputInstanceProps
{
    float rate = Float.NaN;
    
    @Override
    public String details(boolean html)
    {
        return "rate: "+ GeneralUtils.getBold(rate, html);
    }
    
}
