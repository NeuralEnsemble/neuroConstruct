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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

import java.util.EventListener;

/**
 * Interface to add to components in the Main Frame which, when altered, change
 * the info of the project
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public interface ModFileEventListener extends EventListener
{
    public void tabUpdated(String tabName);
}
