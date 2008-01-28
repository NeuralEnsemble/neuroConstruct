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

package ucl.physiol.neuroconstruct.project;

import java.util.EventListener;

/**
 * Interface to add to components in the Main Frame which, when altered, change
 * the info of the project
 *
 * @author Padraig Gleeson
 *  
 */

public interface ProjectEventListner extends EventListener
{
    public void tableDataModelUpdated(String tableModelName);

    public void tabUpdated(String tabName);
}