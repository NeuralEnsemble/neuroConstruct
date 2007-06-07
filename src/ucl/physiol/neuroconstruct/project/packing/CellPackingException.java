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

package ucl.physiol.neuroconstruct.project.packing;

/**
 * General Exception when trying to pack the cells
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class CellPackingException extends Exception
{

    public CellPackingException()
    {
    }

    public CellPackingException(String message)
    {
        super(message);
    }

    public CellPackingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CellPackingException(Throwable cause)
    {
        super(cause);
    }
}