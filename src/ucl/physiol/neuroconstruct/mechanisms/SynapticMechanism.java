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

/**
 * Base class of Cell Mechanism which represent synaptic Mechanisms
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
        super.setMechanismType(super.SYNAPTIC_MECHANISM);
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
