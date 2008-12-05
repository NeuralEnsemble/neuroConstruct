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

package ucl.physiol.neuroconstruct.psics;


/**
 * General exception when dealing with/generating PSICS files
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class PsicsException extends Exception
{

    public PsicsException()
    {
    }

    public PsicsException(String message)
    {
        super(message);
    }

    public PsicsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PsicsException(Throwable cause)
    {
        super(cause);
    }
}
