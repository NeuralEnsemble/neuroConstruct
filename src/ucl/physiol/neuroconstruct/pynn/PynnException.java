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

package ucl.physiol.neuroconstruct.pynn;


/**
 * General exception when dealing with/generating PyNN files
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class PynnException extends Exception
{

    public PynnException()
    {
    }

    public PynnException(String message)
    {
        super(message);
    }

    public PynnException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PynnException(Throwable cause)
    {
        super(cause);
    }
}
