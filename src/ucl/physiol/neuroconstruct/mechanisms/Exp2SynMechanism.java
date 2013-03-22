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

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.simulation.SimEnvHelper;

/**
 * Cell Mech representing a double exponential synapse
 *
 * @author Padraig Gleeson
 *  
 * @deprecated Use ChannelML instead...
 *
 */

public class Exp2SynMechanism extends SynapticMechanism
{
    ClassLogger logger = new ClassLogger("Exp2SynMechanism");

    public static final String TAU_RISE = "Tau Rise";
    public static final String TAU_DECAY = "Tau Decay";

    public static final String REV_POTENTIAL = "Reversal Potential";
    public static final String MAX_COND = "Max Conductance";
    public static final String MAX_COND_DESC = "Maximum conductance of the synapse";


    public Exp2SynMechanism()
    {
        super.setDescription("A synapse with a doubly exponential rising and decaying time course");
        super.setMechanismModel("Double Exponential Synapse");
        super.setDefaultInstanceName("DoubExpSyn");


        addNewParameter(TAU_RISE,
                        "Time course of exponential rise of conductance",
                        1,
                        UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(TAU_DECAY,
                        "Time course of exponential decay of conductance",
                        2,
                        UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(REV_POTENTIAL,
                        REV_POTENTIAL,
                        0,
                        UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        addNewParameter(MAX_COND,
                        MAX_COND_DESC,
                        0.00001f,
                        UnitConverter.conductanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);



        mechanismImpls = new MechanismImplementation[]
            {new MechanismImplementation(SimEnvHelper.NEURON,
                                       ProjectStructure.getModTemplatesDir()
                                      + System.getProperty("file.separator") + "DoubleExpSyn.mod"),
            new MechanismImplementation(SimEnvHelper.GENESIS,
                                      ProjectStructure.getGenesisTemplatesDir()
                                      + System.getProperty("file.separator") +"DoubleExpSyn.g")};

        setPlotInfoFile(ProjectStructure.getXMLTemplatesDir()
                                      + System.getProperty("file.separator") +"DoubExpSynPlots.xml");

    }

    public Object clone()
    {
        Exp2SynMechanism mech = new Exp2SynMechanism();
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
        AbstractedCellMechanism e = CellMechanismHelper.getCellMechInstance("Double Exponential Synapse");
        e.setInstanceName("Expoo");

        e.printDetails();

        e.createImplementationFile("NEURON",
                                   UnitConverter.NEURON_UNITS,
                                   new File("../temp/Exp.mod"),
                                   null,
                                   true,
                true,false,false);
    }


}
