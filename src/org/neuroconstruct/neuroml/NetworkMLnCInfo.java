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

package ucl.physiol.neuroconstruct.neuroml;

/**
 * Interface to gst neuroConstruct specific info from a parsed NetworkML file
 *
 * @author Padraig Gleeson
 *  
 */


/**
 *
 * @author padraig
 */
public interface NetworkMLnCInfo 
{
    String getSimConfig();
            
    long getRandomSeed();
    
    
}
