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

package ucl.physiol.neuroconstruct.gui.plotter;

/**
 * Exception thrown when a value is not present
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class ValueNotPresentException extends Exception
{

    public ValueNotPresentException()
    {
    }

    public ValueNotPresentException(String message)
    {
        super(message);
    }

    public ValueNotPresentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ValueNotPresentException(Throwable cause)
    {
        super(cause);
    }
}
