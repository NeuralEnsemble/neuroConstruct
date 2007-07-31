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

import java.util.*;

/**
 * nmodlEditor application software. Interface for elements of Mod File to inform parent of change in data
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public interface ModFileChangeListener extends EventListener
{
    public void modFileElementChanged(String modFileElementType);

    public void modFileChanged();
}
