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

package ucl.physiol.neuroconstruct.j3D;

import java.awt.*;

/**
 * Interface for panel on which simulation data can be run (i.e cell colours
 * can be changed)
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public interface SimulationInterface
{
    /**
     * Set the colour of a segment, synapse or whole cell. See implementation for specifics
     */
    public void setColour(Color colour, String cellItemRef);


    public void validSimulationLoaded();

    public void noSimulationLoaded();

    //public Hashtable getAllCellGroupPopulations();


}


