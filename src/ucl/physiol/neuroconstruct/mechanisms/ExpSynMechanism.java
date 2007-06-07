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
 * Cell Mechs representing a simple exponential synapse
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 * @deprecated Use ChannelML instead...
 *
 */

public class ExpSynMechanism extends SynapticMechanism
{
    ClassLogger logger = new ClassLogger("ExpSynMechanism");

//    public static final String DELAY = "Delay";
//    public static final String WEIGHT = "Weight";
 //   public static final String THRESHOLD = "Threshold";
    public static final String TAU = "Tau";

    public ExpSynMechanism()
    {
        super.setDescription("A simple synapse with an exponentially decaying time course");
        super.setMechanismModel("Single Exponential Synapse");
/*
        addNewParameter(DELAY,
                        "Delay before firing passed on to post synaptic terminal",
                        10);
        addNewParameter(WEIGHT,
                        "Weight of the synapse",
                        1);
        addNewParameter(THRESHOLD,
                        "Potential above which the cell will fire",
                        -20);*/
        addNewParameter(TAU,
                        "Time course of exponential decay of conductance",
                        10,
                        UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        mechanismImpls = new MechanismImplementation[]
            {new MechanismImplementation(SimEnvHelper.NEURON,
                                       "templates/modFileTemplates/SingleExpSyn.mod")};

    }

    public Object clone()
    {
        ExpSynMechanism mech = new ExpSynMechanism();
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
        ExpSynMechanism e = new ExpSynMechanism();
        e.setInstanceName("Expoo");

        e.printDetails();

        e.createImplementationFile("NEURON",
                                   UnitConverter.NEURON_UNITS,
                                   new File("../temp/Exp.mod"),
                                   null,
                                   true,
                true);
    }


}
