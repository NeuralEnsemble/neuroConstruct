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

import java.util.EventListener;

/**
 * Interface to add to objects which need updating when the voltage changes
 * during rerun of simulations
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public interface VoltageEventListener extends EventListener
{
    public void updateVoltage(float voltage, String cellGroup, int cellNumber, boolean refresh);


    //public void refresh();
}
