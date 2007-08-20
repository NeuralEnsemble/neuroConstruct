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

package ucl.physiol.neuroconstruct.project.cellchoice;


/**
 * Can be thrown by CellChooser subclass
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */


public class CellChooserException extends Exception
{
    public CellChooserException()
    {
        super();
    }

    public CellChooserException(String message)
    {
        super(message);
    }

    public CellChooserException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CellChooserException(Throwable cause)
    {
        super(cause);
    }


}
