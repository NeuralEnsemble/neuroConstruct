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

package ucl.physiol.neuroconstruct.neuroml.hdf5;



/**
 * Exception related to HDF5 functionality
 *
 * @author Padraig Gleeson
 *
 */

public class Hdf5Exception extends Exception
{
    private Hdf5Exception()
    {
    }

    public Hdf5Exception(String message)
    {
        super(message);
    }

    public Hdf5Exception(String comment, Throwable t)
    {
        super(comment, t);
    }


}
