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

package ucl.physiol.neuroconstruct.project;

/**
 * Exception thrown when someone tries to name a Cell Group with a name
 * already taken
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

@SuppressWarnings("serial")
public class NamingException extends Exception
{

    public NamingException()
    {
    }

    public NamingException(String message)
    {
        super(message);
    }

    public NamingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NamingException(Throwable cause)
    {
        super(cause);
    }
}