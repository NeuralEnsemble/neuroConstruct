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


/**
 * Interface to update the OneCell representation when changing groups
 *
 * @author Padraig Gleeson
 *  
 */

public interface UpdateOneCell
{

    public void refreshGroup(String groupName);

}
