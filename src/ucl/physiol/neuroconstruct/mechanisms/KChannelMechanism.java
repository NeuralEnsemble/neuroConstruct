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

import java.io.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.simulation.SimEnvHelper;

/**
 * Cell Mechanism representing a HH channel on the membrane
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 *
 * @deprecated Use ChannelML instead...
 *
 */

public class KChannelMechanism extends HHMembraneMechanism
{
    ClassLogger logger = new ClassLogger("KChannelMechanism");

    public KChannelMechanism()
    {

        super.setDescription("A Potassium conductance on the cell membrane, containing some typical values for the Squid giant axon");

        super.setMechanismModel("K Channel Mechanism");
        super.setDefaultInstanceName("KConductance");

        try
        {
            setParameter(HHMembraneMechanism.COND_DENSITY, 3.6e-7f);
            setParameter(HHMembraneMechanism.REV_POTENTIAL, -77);
            setParameter(HHMembraneMechanism.ION_SPECIES, 2);
            setParameter(HHMembraneMechanism.ACTIV_STATE_VAR_POWER, 4);
            setParameter(HHMembraneMechanism.INACTIV_STATE_VAR_POWER, 0);

            setParameter(HHMembraneMechanism.ACTIV_ALPHA_FORM, 3);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_A, -0.01f);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_B, -10);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_V0, -55);

            setParameter(HHMembraneMechanism.ACTIV_BETA_FORM, 1);
            setParameter(HHMembraneMechanism.ACTIV_BETA_A, 0.125f);
            setParameter(HHMembraneMechanism.ACTIV_BETA_B, -80);
            setParameter(HHMembraneMechanism.ACTIV_BETA_V0, -65);


            setParameter(HHMembraneMechanism.INACTIV_ALPHA_FORM, 1);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_A, 0);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_B, 1);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_V0, 1);

            setParameter(HHMembraneMechanism.INACTIV_BETA_FORM, 1);
            setParameter(HHMembraneMechanism.INACTIV_BETA_A, 0);
            setParameter(HHMembraneMechanism.INACTIV_BETA_B, 1);
            setParameter(HHMembraneMechanism.INACTIV_BETA_V0, 1);




            setParameterDefault(HHMembraneMechanism.COND_DENSITY, 0.036f);
            setParameterDefault(HHMembraneMechanism.REV_POTENTIAL, -77);
            setParameterDefault(HHMembraneMechanism.ION_SPECIES, 2);
            setParameterDefault(HHMembraneMechanism.ACTIV_STATE_VAR_POWER, 4);
            setParameterDefault(HHMembraneMechanism.INACTIV_STATE_VAR_POWER, 0);

            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_FORM, 3);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_A, -0.01f);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_B, -10);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_V0, -55);

            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_FORM, 1);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_A, 0.125f);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_B, -80);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_V0, -65);


            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_FORM, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_A, 0);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_B, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_V0, 1);

            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_FORM, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_A, 0);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_B, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_V0, 1);




        }
        catch (CellMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error instantiating the cell Mechanism: " + this.toString(), ex, null);
        }


    }


    public Object clone()
    {
        KChannelMechanism mech = new KChannelMechanism();
        for (int i = 0; i < physParamList.length; i++)
        {
            try
            {
                mech.setParameter(new String(physParamList[i].parameterName), physParamList[i].getValue());
            }
            catch (CellMechanismException ex)
            {
                logger.logError("Error cloning the KChannelMechanism", ex);
                return null;
            }
        }
        return mech;

    }



    public static void main(String[] args)
    {
        KChannelMechanism k = new KChannelMechanism();
        k.setInstanceName("testK");

        try
        {
            System.out.println("Cell Mechanism: "+ k);
            k.printDetails();

            boolean success = k.createImplementationFile(SimEnvHelper.GENESIS,
                                                         UnitConverter.GENESIS_SI_UNITS,
                                                         new File("../temp/testna/" + k.getInstanceName() + ".g"),
                                                         null,
                                                         false,
                true);

            success = k.createImplementationFile(SimEnvHelper.NEURON,
                                              UnitConverter.NEURON_UNITS,
                                              new File("../temp/testna/" + k.getInstanceName() + ".mod"),
                                              null,
                                              true,
                true);


    System.out.println("Success: "+success);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }



}
