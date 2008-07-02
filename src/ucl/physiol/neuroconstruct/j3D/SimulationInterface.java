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

package ucl.physiol.neuroconstruct.j3D;

import java.awt.*;
import javax.media.j3d.Appearance;

/**
 * Interface for panel on which simulation data can be run (i.e cell colours
 * can be changed)
 *
 * @author Padraig Gleeson
 *  
 */


public interface SimulationInterface
{
    /**
     * Set the colour of a segment, synapse or whole cell. See implementation for specifics
     */
    public void setColour(String cellItemRef, Color colour);
    
    public void setTransparent(String cellItemRef);
    
    public void setTempAppearance(String cellOnlyRef, Appearance app);
    
    public void removeTempAppearance(String cellOnlyRef);

    public void validSimulationLoaded();

    public void noSimulationLoaded();
    
    public void refreshAll3D();

    //public Hashtable getAllCellGroupPopulations();


}


