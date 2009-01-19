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

package ucl.physiol.neuroconstruct.mechanisms;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Base class of Cell Mechanism which represent synaptic Mechanisms
 *
 * @author Padraig Gleeson
 *  
 *
 * @deprecated Use ChannelML instead...
 *
 */

public class SynapticMechanism extends AbstractedCellMechanism
{
    ClassLogger logger = new ClassLogger("SynapticMechanism");

   // public static final String MAX_COND = "Max Conductance";

    public SynapticMechanism()
    {
        super.setDescription("A synaptic mechanism");
        super.setMechanismType(SYNAPTIC_MECHANISM);
        super.setDefaultInstanceName("SynMechanism");
/*

        addNewParameter(MAX_COND,
                        "Maximum conductance for the synapse",
                        100,
                        UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);
*/
    }


    public Object clone()
    {
        SynapticMechanism mech = new SynapticMechanism();
        for (int i = 0; i < physParamList.length; i++)
        {
            try
            {
                mech.setParameter(new String(physParamList[i].parameterName), physParamList[i].getValue());
            }
            catch (CellMechanismException ex)
            {
                logger.logError("Error cloning the SynapticMechanism", ex);
                return null;
            }
        }
        return mech;

    }


    public boolean setParameter(String parameterName, float parameterValue) throws CellMechanismException
    {
        return super.setParameter(parameterName, parameterValue);

    };



}
