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

package ucl.physiol.neuroconstruct.genesis;

/**
 * General exception when dealing with/generating GENESIS files
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class GenesisException extends Exception
{

    public GenesisException()
    {
    }

    public GenesisException(String message)
    {
        super(message);
    }

    public GenesisException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GenesisException(Throwable cause)
    {
        super(cause);
    }
}
