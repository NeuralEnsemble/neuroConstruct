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
 * Exception when trying to create new cells
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")
public class CellCreationException extends Exception
{

    public CellCreationException()
    {
    }

    public CellCreationException(String message)
    {
        super(message);
    }

    public CellCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CellCreationException(Throwable cause)
    {
        super(cause);
    }
}