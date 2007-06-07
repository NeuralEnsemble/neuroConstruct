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

public class HHMembraneMechanism extends DistMembraneMechanism
{
    ClassLogger logger = new ClassLogger("HHMembraneMechanism");

    public static final String REV_POTENTIAL = "Reversal Potential";

    public static final String ION_SPECIES = "Ion Species";


    public static final String ACTIV_STATE_VAR_POWER = "Activation State Variable Power";
    public static final String INACTIV_STATE_VAR_POWER = "Inactivation State Variable Power";



    public static final String ACTIV_ALPHA_FORM = "Activation State Function Alpha Form";

    public static final String ACTIV_ALPHA_A = "Activation Alpha A variable";
    public static final String ACTIV_ALPHA_B = "Activation Alpha B variable";
    public static final String ACTIV_ALPHA_V0 = "Activation Alpha V0 variable";

    public static final String ACTIV_BETA_FORM = "Activation State Function Beta Form";

    public static final String ACTIV_BETA_A = "Activation Beta A variable";
    public static final String ACTIV_BETA_B = "Activation Beta B variable";
    public static final String ACTIV_BETA_V0 = "Activation Beta V0 variable";

    public static final String INACTIV_ALPHA_FORM = "Inactivation State Function Alpha Form";

    public static final String INACTIV_ALPHA_A = "Inactivation Alpha A variable";
    public static final String INACTIV_ALPHA_B = "Inactivation Alpha B variable";
    public static final String INACTIV_ALPHA_V0 = "Inactivation Alpha V0 variable";

    public static final String INACTIV_BETA_FORM = "Inactivation State Function Beta Form";

    public static final String INACTIV_BETA_A = "Inactivation Beta A variable";
    public static final String INACTIV_BETA_B = "Inactivation Beta B variable";
    public static final String INACTIV_BETA_V0 = "Inactivation Beta V0 variable";

    public static final String LINOIDAL_CONV_FACTOR = "Linoidal Conversion Factor";
    public static final String LINOIDAL_CONV_FACTOR_DESC = "Needed for GENESIS file to convert the A factor when the equation is Linoidal. DON'T CHANGE from 1! See implementing file";

    public static final String EXP_TEMP = "Experiment Temperature";
    public static final String EXP_TEMP_DESC = "Temp at which A, B, V0 values estimated (used for Q10 in NEURON impl)";


    public HHMembraneMechanism()
    {
        super.setDescription("A voltage dependent conductance on the cell membrane, according to the HH model");
        super.setMechanismModel("HH Type Channel");
        super.setDefaultInstanceName("HHChannel");

        this.logger.setThisClassSilent(true);

        try
        {
            setParameterDefault(COND_DENSITY, 1.2e-6f);
            setParameter(COND_DENSITY, 1.2e-6f);
        }
        catch (CellMechanismException ex)
        {
            logger.logError("Problem setting up HHMembrane Mechanism", ex);
        }

        addNewParameter(REV_POTENTIAL,
                        "Reversal potential of the channel type",
                        50,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(ION_SPECIES,
                        "Type of ion flowing through the channel (NEURON only) (1=Na, 2=K, 3=Cl, 4=Ca, 5=Mg)",
                        1,
                        Units.DIMENSIONLESS);




        addNewParameter(ACTIV_STATE_VAR_POWER,
                        "Activation State Variable Power",
                        3,
                        Units.DIMENSIONLESS);


        addNewParameter(INACTIV_STATE_VAR_POWER,
                        "Inactivation State Variable Power",
                        1,
                        Units.DIMENSIONLESS);


        addNewParameter(ACTIV_ALPHA_FORM,
                        "Form of eqn for alpha component of function for activation state.",
                        3,
                        Units.DIMENSIONLESS);

        addNewParameter(ACTIV_ALPHA_A, ACTIV_ALPHA_A, -.1f,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(ACTIV_ALPHA_B, ACTIV_ALPHA_B, -10,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(ACTIV_ALPHA_V0, ACTIV_ALPHA_V0, -40,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);





        addNewParameter(ACTIV_BETA_FORM,
                        "Form of eqn for beta component of function for activation state.",
                        1,
                        Units.DIMENSIONLESS);

        addNewParameter(ACTIV_BETA_A, ACTIV_BETA_A, 4,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(ACTIV_BETA_B, ACTIV_BETA_B, -18,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(ACTIV_BETA_V0, ACTIV_BETA_V0,-65,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);



        addNewParameter(INACTIV_ALPHA_FORM,
                        "Form of eqn for alpha component of function for inactivation state.",
                        1,
                        Units.DIMENSIONLESS);

        addNewParameter(INACTIV_ALPHA_A, INACTIV_ALPHA_A, .07f,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(INACTIV_ALPHA_B, INACTIV_ALPHA_B, -20,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(INACTIV_ALPHA_V0, INACTIV_ALPHA_V0, -65,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(INACTIV_BETA_FORM,
                        "Form of eqn for beta component of function for inactivation state.",
                        2,
                        Units.DIMENSIONLESS);

        addNewParameter(INACTIV_BETA_A, INACTIV_BETA_A, 1,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(INACTIV_BETA_B, INACTIV_BETA_B, -10,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(INACTIV_BETA_V0, INACTIV_BETA_V0, -35,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(LINOIDAL_CONV_FACTOR, LINOIDAL_CONV_FACTOR_DESC, 1,
                        UnitConverter.perUnitVoltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(EXP_TEMP,
                        EXP_TEMP_DESC,
                        6.3f,
                        Units.CELSIUS);




        MechanismImplementation neuronImpl
            = new MechanismImplementation(SimEnvHelper.NEURON,
                                        "templates/modFileTemplates/GenericHHChannel.mod");

        // Using 1=Na, 2=K, 3=Cl, 4=Ca, 5=Mg
        neuronImpl.addParamValueSubstitute(ION_SPECIES, 1,"na");
        neuronImpl.addParamValueSubstitute(ION_SPECIES, 2, "k");
        neuronImpl.addParamValueSubstitute(ION_SPECIES, 3, "cl");
        neuronImpl.addParamValueSubstitute(ION_SPECIES, 4, "ca");
        neuronImpl.addParamValueSubstitute(ION_SPECIES, 5, "mg");


        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 0, "");
        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 1, "*X");
        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 2, "*X*X");
        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 3, "*X*X*X");
        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 4, "*X*X*X*X");
        neuronImpl.addParamValueSubstitute(ACTIV_STATE_VAR_POWER, 5, "*X*X*X*X*X");

        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 0, "");
        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 1, "*Y");
        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 2, "*Y*Y");
        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 3, "*Y*Y*Y");
        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 4, "*Y*Y*Y*Y");
        neuronImpl.addParamValueSubstitute(INACTIV_STATE_VAR_POWER, 5, "*Y*Y*Y*Y*Y");

        String EQUALITY = "1";
        String EXPONENTIAL_FORM = "A * exp((v - V0) / B)";
        String SIGMOIDAL_FORM = "A / (exp((v - V0) / B) + 1)";
        String LINOIDAL_FORM = "A * vtrap((v - V0), B)";


        neuronImpl.addParamValueSubstitute(ACTIV_ALPHA_FORM, 0, EQUALITY);
        neuronImpl.addParamValueSubstitute(ACTIV_ALPHA_FORM, 1, EXPONENTIAL_FORM);
        neuronImpl.addParamValueSubstitute(ACTIV_ALPHA_FORM, 2, SIGMOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(ACTIV_ALPHA_FORM, 3, LINOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(ACTIV_BETA_FORM, 0, EQUALITY);
        neuronImpl.addParamValueSubstitute(ACTIV_BETA_FORM, 1, EXPONENTIAL_FORM);
        neuronImpl.addParamValueSubstitute(ACTIV_BETA_FORM, 2, SIGMOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(ACTIV_BETA_FORM, 3, LINOIDAL_FORM);

        neuronImpl.addParamValueSubstitute(INACTIV_ALPHA_FORM, 0, EQUALITY);
        neuronImpl.addParamValueSubstitute(INACTIV_ALPHA_FORM, 1, EXPONENTIAL_FORM);
        neuronImpl.addParamValueSubstitute(INACTIV_ALPHA_FORM, 2, SIGMOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(INACTIV_ALPHA_FORM, 3, LINOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(INACTIV_BETA_FORM, 0, EQUALITY);
        neuronImpl.addParamValueSubstitute(INACTIV_BETA_FORM, 1, EXPONENTIAL_FORM);
        neuronImpl.addParamValueSubstitute(INACTIV_BETA_FORM, 2, SIGMOIDAL_FORM);
        neuronImpl.addParamValueSubstitute(INACTIV_BETA_FORM, 3, LINOIDAL_FORM);



        MechanismImplementation genesisImpl
            = new MechanismImplementation(SimEnvHelper.GENESIS,
                                        "templates/genesisTemplates/GenericHHChannel.g");


        mechanismImpls = new MechanismImplementation[]
            {neuronImpl, genesisImpl};



    }


    public boolean setParameter(String parameterName, float parameterValue) throws CellMechanismException
    {
       logger.logComment("Setting in HH: "+ parameterName);
        String errorMessage = null;
        if (parameterName.equals(ION_SPECIES))
        {
            if (parameterValue!=(int)parameterValue
                || parameterValue<0
                || parameterValue>6)
                errorMessage = "Please enter a valid value for parameter: "+ parameterName;

        }
        if (parameterName.equals(ACTIV_ALPHA_FORM) ||
            parameterName.equals(INACTIV_ALPHA_FORM) ||
            parameterName.equals(ACTIV_BETA_FORM) ||
            parameterName.equals(INACTIV_BETA_FORM))
        {
           // logger.logComment("Setting "+parameterName+" to: "+ parameterValue);
            if (!(parameterValue==0 || parameterValue==1f || parameterValue==2f || parameterValue==3f))
            errorMessage = "Please enter a valid value for parameter: "+ parameterName +"1 = EXPONENTIAL, 2 = SIGMOID, 3 = LINOID";
        }

        if (errorMessage!=null) throw new CellMechanismException("Please enter a valid value for parameter: "+ parameterName);

        for (int i = 0; i < physParamList.length; i++)
        {
            logger.logComment("Checking "+physParamList[i].getParameterName());

            if (physParamList[i].getParameterName().equals(parameterName))
            {
                logger.logComment("Got it...");
                physParamList[i].setValue(parameterValue);
                return true;

            }
        }
        logger.logComment("Done...");
        return false;

    };


    public Object clone()
    {
        HHMembraneMechanism mech = new HHMembraneMechanism();
        for (int i = 0; i < physParamList.length; i++)
        {
            try
            {
                mech.setParameter(new String(physParamList[i].parameterName), physParamList[i].getValue());
            }
            catch (CellMechanismException ex)
            {
                logger.logError("Error cloning the HHMembraneMechanism", ex);
                return null;
            }
        }
        return mech;

    }

    public static void main(String[] args)
    {
        HHMembraneMechanism hh = new HHMembraneMechanism();
        hh.setInstanceName("testna");

        try
        {
            hh.setParameter(ION_SPECIES, 2f);
            hh.setParameter(ACTIV_STATE_VAR_POWER, 4);


            hh.setParameter(INACTIV_STATE_VAR_POWER, 0);
            hh.printDetails();
            System.out.println("Rev pot: " + hh.getParameter(PassiveMembraneMechanism.REV_POTENTIAL));

            boolean success = hh.createImplementationFile(SimEnvHelper.NEURON,
                                                          UnitConverter.NEURON_UNITS,
                                                          new File("../temp/testna/"+hh.getInstanceName()+".mod"),
                                                          null,
                                                          true,
                true);

            success = hh.createImplementationFile(SimEnvHelper.GENESIS,
                                                          UnitConverter.GENESIS_SI_UNITS,
                                                          new File("../temp/testna/"+hh.getInstanceName()+"_SI.g")
                                                        ,null,
                                                        false,
                true);

            success = hh.createImplementationFile(SimEnvHelper.GENESIS,
                                                          UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                          new File("../temp/testna/"+hh.getInstanceName()+".g"),
                                                          null,
                                                          false,
                true);

            System.out.println("Success: "+ success);
        }
        catch (CellMechanismException ex)
        {
            ex.printStackTrace();
        }
    }



}
