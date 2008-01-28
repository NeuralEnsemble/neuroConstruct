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

package ucl.physiol.neuroconstruct.mechanisms;


/**
 * Class for specifying when a ChannelML Mechanism is not initialised
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class CMLMechNotInitException extends ChannelMLException
{

    public CMLMechNotInitException()
    {
        super("ChannelML Cell Mechanism has not been initialised with project information");
    }

}
