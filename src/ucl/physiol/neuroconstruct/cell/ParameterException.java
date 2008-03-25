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

package ucl.physiol.neuroconstruct.cell;
 


 /**
  * A class representing an exception when dealing with parameterised expressions, e.g. for variable mechanism densities
  *
  * @author Padraig Gleeson
  *  
  *
  */
@SuppressWarnings("serial")
public class ParameterException extends Exception
{

    public ParameterException(String exception)
    {
        super(exception);
    }
}
