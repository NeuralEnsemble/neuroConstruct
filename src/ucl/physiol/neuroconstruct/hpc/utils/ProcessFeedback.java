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

package ucl.physiol.neuroconstruct.hpc.utils;


/**
 * Simple interface for giving feedback from processes
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public interface ProcessFeedback
{
    public void comment(String comment);

    public void error(String comment);
}
