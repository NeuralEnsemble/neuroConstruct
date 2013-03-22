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
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Base class of Cell Mechanisms which represent voltage dependent channels of
 * a certain maximum conductance density distributed across the membrane
 *
 * @author Padraig Gleeson
 *  
 *
 */

public abstract class DistMembraneMechanism extends AbstractedCellMechanism
{
    ClassLogger logger = new ClassLogger("DistMembraneMechanism");

    public static final String COND_DENSITY = "Max Conductance Density";
    public static final String COND_DENSITY_DESC
        = "DEFAULT Maximum conductance for the channel mechanism per unit"
          +"area. NOTE: overwritten when mechanism is placed on membrane";

    public DistMembraneMechanism()
    {
        super.setDescription("A voltage dependent conductance on the cell membrane");
        super.setMechanismType(CHANNEL_MECHANISM);
        super.setDefaultInstanceName("MembraneMechanism");



        addNewParameter(COND_DENSITY,
                        COND_DENSITY_DESC,
                        3e-9f,
                        UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

    }

    public boolean setParameter(String parameterName, float parameterValue) throws CellMechanismException
    {
        if (parameterName.equals(COND_DENSITY))
        {
            if (parameterValue <= 0)
                throw new CellMechanismException("The conductance density cannot be negative");
        }
        return super.setParameter(parameterName, parameterValue);

    };



}
