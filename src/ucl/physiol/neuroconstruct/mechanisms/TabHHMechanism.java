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

import java.io.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.simulation.SimEnvHelper;

/**
 * Cell Mechanism representing a HH channel on the membrane, implemented using
* tabchannel in GENESIS
 *
 * @author Padraig Gleeson
 *  
 *
 * @deprecated Use ChannelML instead...
 *
 */

public class TabHHMechanism extends DistMembraneMechanism
{
    ClassLogger logger = new ClassLogger("TabHHMechanism");

    public static final String REV_POTENTIAL = "Reversal Potential";

    public static final String ION_SPECIES = "Ion Species";


    public static final String ACTIV_STATE_VAR_POWER = "Activation State Variable Power";
    public static final String INACTIV_STATE_VAR_POWER = "Inactivation State Variable Power";


    public static final String AAX = "AAX";
    public static final String AAX_DESC = "Activation A variable, A value";

    public static final String ABX = "ABX";
    public static final String ABX_DESC = "Activation A variable, B value";

    public static final String ACX = "ACX";
    public static final String ACX_DESC = "Activation A variable, C value";

    public static final String ADX = "ADX";
    public static final String ADX_DESC = "Activation A variable, D value";

    public static final String AFX = "AFX";
    public static final String AFX_DESC = "Activation A variable, F value";

    public static final String BAX = "BAX";
    public static final String BAX_DESC = "Activation B variable, A value";

    public static final String BBX = "BBX";
    public static final String BBX_DESC = "Activation B variable, B value";

    public static final String BCX = "BCX";
    public static final String BCX_DESC = "Activation B variable, C value";

    public static final String BDX = "BDX";
    public static final String BDX_DESC = "Activation B variable, D value";

    public static final String BFX = "BFX";
    public static final String BFX_DESC = "Activation B variable, F value";



    public static final String AAY = "AAY";
    public static final String AAY_DESC = "Inactivation A variable, A value";

    public static final String ABY = "ABY";
    public static final String ABY_DESC = "Inactivation A variable, B value";

    public static final String ACY = "ACY";
    public static final String ACY_DESC = "Inactivation A variable, C value";

    public static final String ADY = "ADY";
    public static final String ADY_DESC = "Inactivation A variable, D value";

    public static final String AFY = "AFY";
    public static final String AFY_DESC = "Inactivation A variable, F value";

    public static final String BAY = "BAY";
    public static final String BAY_DESC = "Inactivation B variable, A value";

    public static final String BBY = "BBY";
    public static final String BBY_DESC = "Inactivation B variable, B value";

    public static final String BCY = "BCY";
    public static final String BCY_DESC = "Inactivation B variable, C value";

    public static final String BDY = "BDY";
    public static final String BDY_DESC = "Inactivation B variable, D value";

    public static final String BFY = "BFY";
    public static final String BFY_DESC = "Inactivation B variable, F value";




    public static final String LINOIDAL_CONV_FACTOR = "Linoidal Conversion Factor";
    public static final String LINOIDAL_CONV_FACTOR_DESC = "Needed for GENESIS file to convert the A factor when the equation is Linoidal. DON'T CHANGE from 1! See implementing file";

    public static final String EXP_TEMP = "Experiment Temperature";
    public static final String EXP_TEMP_DESC = "Temp at which A, B, V0 values estimated (used for Q10 in NEURON impl)";


    public TabHHMechanism()
    {
        super.setDescription("A voltage dependent conductance on the cell membrane, according to the HH model. "
                             +"Implemented using tabchannel in GENESIS");

        super.setMechanismModel("Table based HH Type Channel");
        super.setDefaultInstanceName("TabHHMechanism");

        try
        {
            setParameterDefault(COND_DENSITY, 0.12f);
            setParameter(COND_DENSITY, 0.12f);
        }
        catch (CellMechanismException ex)
        {
            logger.logError("Problem setting up TabHHMechanism", ex);
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



        addNewParameter(AAX, AAX_DESC, 0.1f*(-40f),
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(ABX, ABX_DESC, -0.1f,
                        UnitConverter.perUnitTimeVoltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(ACX, ACX_DESC, -1,
                        Units.DIMENSIONLESS);


        addNewParameter(ADX, ADX_DESC, -1*(-40f),
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(AFX, AFX_DESC, -10f,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);



        addNewParameter(BAX, BAX_DESC, 4f,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BBX, BBX_DESC, 0,
                        UnitConverter.perUnitTimeVoltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BCX, BCX_DESC, 0,
                        Units.DIMENSIONLESS);


        addNewParameter(BDX, BDX_DESC, -1*(-65f),
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BFX, BFX_DESC, 18f,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);




        addNewParameter(AAY, AAY_DESC, 0.070f,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(ABY, ABY_DESC, 0,
                        UnitConverter.perUnitTimeVoltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(ACY, ACY_DESC, 0,
                        Units.DIMENSIONLESS);


        addNewParameter(ADY, ADY_DESC, -1*(-65f),
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(AFY, AFY_DESC, 20f,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);



        addNewParameter(BAY, BAY_DESC, 1.0f,
                        UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BBY, BBY_DESC, 0,
                        UnitConverter.perUnitTimeVoltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BCY, BCY_DESC, 1.0f,
                        Units.DIMENSIONLESS);


        addNewParameter(BDY, BDY_DESC, -1*(-35f),
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);


        addNewParameter(BFY, BFY_DESC, -10.0f,
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




        MechanismImplementation genesisImpl
            = new MechanismImplementation(SimEnvHelper.GENESIS,
                                        "templates/genesisTemplates/TabHHChannel.g");


        mechanismImpls = new MechanismImplementation[]
            {neuronImpl, genesisImpl};



    }


    public boolean setParameter(String parameterName, float parameterValue) throws CellMechanismException
    {
        String errorMessage = null;
        if (parameterName.equals(ION_SPECIES))
        {
            if (parameterValue!=(int)parameterValue
                || parameterValue<0
                || parameterValue>6)
                errorMessage = "Please enter a valid value for parameter: "+ parameterName;

        }

        if (errorMessage!=null) throw new CellMechanismException("Please enter a valid value for parameter: "+ parameterName);

        for (int i = 0; i < physParamList.length; i++)
        {
            if (physParamList[i].getParameterName().equals(parameterName))
            {
                physParamList[i].setValue(parameterValue);
                return true;
            }
        }

                return false;

    };


    public Object clone()
    {
        TabHHMechanism mech = new TabHHMechanism();
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
        TabHHMechanism hh = new TabHHMechanism();
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
                true,false,false);

            success = hh.createImplementationFile(SimEnvHelper.GENESIS,
                                                          UnitConverter.GENESIS_SI_UNITS,
                                                          new File("../temp/testna/"+hh.getInstanceName()+"_SI.g"),
                                                          null,false,
                true,false,false);

            success = hh.createImplementationFile(SimEnvHelper.GENESIS,
                                                          UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                          new File("../temp/testna/"+hh.getInstanceName()+".g"),
                                                          null,
                                                          true,
                true,false,false);

            System.out.println("Success: "+ success);
        }
        catch (CellMechanismException ex)
        {
            ex.printStackTrace();
        }
    }



}
