/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * A helper class for getting info on the simulation environments
 *
 * @author Padraig Gleeson
 *  
 */

public class SimEnvHelper
{

    public static final String NEURON = "NEURON";
    public static final String GENESIS = "GENESIS";
    public static final String PSICS = "PSICS";

    public static String[] currentSimEnvironments = new String[]{NEURON,GENESIS, PSICS};

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
