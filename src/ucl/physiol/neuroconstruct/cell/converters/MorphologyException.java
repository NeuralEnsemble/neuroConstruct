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

package ucl.physiol.neuroconstruct.cell.converters;

/**
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 *
 */

public class MorphologyException extends Exception
{
    private MorphologyException()
    {
    }

    public MorphologyException(String message)
    {
        super(message);
    }

    public MorphologyException(String filename, String comment)
    {
        super("Problem with morphology file: "+ filename+", "+ comment);
    }


    public MorphologyException(String comment, Throwable t)
    {
        super(comment, t);
    }


    public MorphologyException(String filename, String comment, Throwable t)
    {
        super("Problem with morphology file: "+ filename+", "+ comment, t);
    }


}
