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

package ucl.physiol.neuroconstruct.neuroml;

/**
 * NeuroML related exception
 * @author Padraig Gleeson
 * @version 1.0.3
 *
 */

public class NeuroMLException extends Exception
{
    private NeuroMLException()
    {
    }

    public NeuroMLException(String message)
    {
        super(message);
    }

    public NeuroMLException(String filename, String comment)
    {
        super("NeuroML related problem: "+ filename+", "+ comment);
    }


    public NeuroMLException(String comment, Throwable t)
    {
        super(comment, t);
    }



}
