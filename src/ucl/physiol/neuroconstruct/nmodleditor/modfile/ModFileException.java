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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class ModFileException extends Exception
{

    public ModFileException()
    {
    }

    public ModFileException(String message)
    {
        super(message);
    }

    public ModFileException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModFileException(Throwable cause)
    {
        super(cause);
    }
}