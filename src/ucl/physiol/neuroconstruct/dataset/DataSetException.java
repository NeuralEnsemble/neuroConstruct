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

package ucl.physiol.neuroconstruct.dataset;

/**
 * Exception thrown when there is a problem with a Data Set
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class DataSetException extends Exception
{

    public DataSetException()
    {
    }

    public DataSetException(String message)
    {
        super(message);
    }

    public DataSetException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataSetException(Throwable cause)
    {
        super(cause);
    }
}
