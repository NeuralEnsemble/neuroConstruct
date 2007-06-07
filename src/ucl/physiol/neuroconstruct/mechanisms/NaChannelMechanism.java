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
 * @version 1.0.3
 *
 * @deprecated Use ChannelML instead...
 *
 */

public class NaChannelMechanism extends HHMembraneMechanism
{
    ClassLogger logger = new ClassLogger("NaChannelMechanism");


    public static final String REV_POTENTIAL = "Reversal Potential";


    String comment =  " (Leave as is for normal HH channel behaviour)";

    public static final String ACTIV_ALPHA_V0 = "V0 var for Activation Alpha component";
    public static final String ACTIV_BETA_V0 = "V0 var for Activation Beta component";
    public static final String INACTIV_ALPHA_V0 = "V0 var for Inactivation Alpha component";
    public static final String INACTIV_BETA_V0 = "V0 var for Inactivation Beta component";



    public NaChannelMechanism()
    {
        super.setMechanismModel("Na Channel Mechanism");
        super.setDefaultInstanceName("NaConductance");
        super.setDescription("A Sodium conductance on the cell membrane, containing some typical values for the Squid giant axon");

        try
        {
            setParameter(HHMembraneMechanism.COND_DENSITY, 1.2e-6f);
            setParameter(HHMembraneMechanism.REV_POTENTIAL, 50);
            setParameter(HHMembraneMechanism.ION_SPECIES, 1);
            setParameter(HHMembraneMechanism.ACTIV_STATE_VAR_POWER, 3);
            setParameter(HHMembraneMechanism.INACTIV_STATE_VAR_POWER, 1);

            setParameter(HHMembraneMechanism.ACTIV_ALPHA_FORM, 3);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_A, -.1f);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_B, -10);
            setParameter(HHMembraneMechanism.ACTIV_ALPHA_V0, -40);

            setParameter(HHMembraneMechanism.ACTIV_BETA_FORM, 1);
            setParameter(HHMembraneMechanism.ACTIV_BETA_A, 4f);
            setParameter(HHMembraneMechanism.ACTIV_BETA_B, -18);
            setParameter(HHMembraneMechanism.ACTIV_BETA_V0, -65);


            setParameter(HHMembraneMechanism.INACTIV_ALPHA_FORM, 1);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_A, 0.07f);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_B, -20);
            setParameter(HHMembraneMechanism.INACTIV_ALPHA_V0, -65);

            setParameter(HHMembraneMechanism.INACTIV_BETA_FORM, 2);
            setParameter(HHMembraneMechanism.INACTIV_BETA_A, 1);
            setParameter(HHMembraneMechanism.INACTIV_BETA_B, -10);
            setParameter(HHMembraneMechanism.INACTIV_BETA_V0, -35);



            setParameterDefault(HHMembraneMechanism.COND_DENSITY, 0.12f);
            setParameterDefault(HHMembraneMechanism.REV_POTENTIAL, 50);
            setParameterDefault(HHMembraneMechanism.ION_SPECIES, 1);
            setParameterDefault(HHMembraneMechanism.ACTIV_STATE_VAR_POWER, 3);
            setParameterDefault(HHMembraneMechanism.INACTIV_STATE_VAR_POWER, 1);

            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_FORM, 3);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_A, -.1f);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_B, -10);
            setParameterDefault(HHMembraneMechanism.ACTIV_ALPHA_V0, -40);

            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_FORM, 1);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_A, 4f);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_B, -18);
            setParameterDefault(HHMembraneMechanism.ACTIV_BETA_V0, -65);


            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_FORM, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_A, 0.07f);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_B, -20);
            setParameterDefault(HHMembraneMechanism.INACTIV_ALPHA_V0, -65);

            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_FORM, 2);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_A, 1);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_B, -10);
            setParameterDefault(HHMembraneMechanism.INACTIV_BETA_V0, -35);






        }
        catch (CellMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error instantiating the cell Mechanism: " + this.toString(), ex, null);
        }




    }

    public Object clone()
    {
        NaChannelMechanism mech = new NaChannelMechanism();
        for (int i = 0; i < physParamList.length; i++)
        {
            try
            {
                mech.setParameter(new String(physParamList[i].parameterName), physParamList[i].getValue());
            }
            catch (CellMechanismException ex)
            {
                return null;
            }
        }
        return mech;

    }


    public static void main(String[] args)
    {
        NaChannelMechanism na = new NaChannelMechanism();

        try
        {
            na.setInstanceName("na");
            na.setParameter(NaChannelMechanism.REV_POTENTIAL, 77);
            na.printDetails();
            System.out.println("Rev pot: " + na.getParameter(NaChannelMechanism.REV_POTENTIAL));

            na.createImplementationFile(SimEnvHelper.NEURON,
                                         UnitConverter.NEURON_UNITS,
                                         new File("../temp/test.mod"),
                                         null,
                                         true,
                true);

            na.createImplementationFile(SimEnvHelper.GENESIS,
                                         UnitConverter.GENESIS_SI_UNITS,
                                         new File("../temp/test.g"),
                                         null,
                                         false,
                true);

        }
        catch (CellMechanismException ex)
        {
            ex.printStackTrace();
        }
    }



}
