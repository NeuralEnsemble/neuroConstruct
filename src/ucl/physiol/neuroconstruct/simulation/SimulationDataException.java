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

package ucl.physiol.neuroconstruct.simulation;

/**
 * Exception when dealing with data from a Neuron simulation
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class SimulationDataException extends Exception
{

    public SimulationDataException()
    {
    }

    public SimulationDataException(String message)
    {
        super(message);
    }

    public SimulationDataException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SimulationDataException(Throwable cause)
    {
        super(cause);
    }
}