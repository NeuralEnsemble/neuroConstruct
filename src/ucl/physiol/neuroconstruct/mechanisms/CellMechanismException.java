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

package ucl.physiol.neuroconstruct.mechanisms;

/**
 * Class for specifying a Cell Mechanism related Exception
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class CellMechanismException extends Exception
{
    public CellMechanismException(String e)
    {
        super(e);
    }
    public CellMechanismException(String e, Throwable t)
    {
        super(e, t);
    }


}
