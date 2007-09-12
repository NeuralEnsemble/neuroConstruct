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
