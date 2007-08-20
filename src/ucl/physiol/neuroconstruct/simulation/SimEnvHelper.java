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

import ucl.physiol.neuroconstruct.utils.*;

/**
 * A helper class for getting info on the simulation environments
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class SimEnvHelper
{

    public static final String NEURON = "NEURON";
    public static final String GENESIS = "GENESIS";

    public String[] currentSimEnvironments = new String[]{NEURON,GENESIS};

    public SimEnvHelper()
    {
    }


    /**
     * changes dend[0]_1 to dend_0__1, etc.
     * Any other substitutions which would ensure sim compat of section/seg names can be added here
     */
    public static String getSimulatorFriendlyName(String oldName)
    {
        String newName = GeneralUtils.replaceAllTokens(oldName, "[", "_");
        newName = GeneralUtils.replaceAllTokens(newName, "]", "_");
        return newName;
    }

}
