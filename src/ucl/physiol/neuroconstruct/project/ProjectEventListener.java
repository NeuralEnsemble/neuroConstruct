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
 * Interface to add to Main Frame which, when altered, change
 * the info of the project
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public interface ProjectEventListener extends EventListener
{
    public void tableDataModelUpdated(String tableModelName);

    public void tabUpdated(String tabName);

    public void cellMechanismUpdated();

}
