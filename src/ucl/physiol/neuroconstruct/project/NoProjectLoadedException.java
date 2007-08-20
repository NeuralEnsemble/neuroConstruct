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
 * An exception thrown when a method is called which should only be called if
 * a valid project is loaded
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")
public class NoProjectLoadedException extends Exception
{
    public NoProjectLoadedException()
    {
    }

}