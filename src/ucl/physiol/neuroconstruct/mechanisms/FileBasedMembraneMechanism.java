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
 * Cell Mechanism representing a channel on the membrane, implementation of
*  which is present in a specified file
 *
 * @author Padraig Gleeson
 *  
 */

public class FileBasedMembraneMechanism extends AbstractedCellMechanism
{
    ClassLogger logger = new ClassLogger("FileBasedMembraneMechanism");


    public FileBasedMembraneMechanism()
    {
        super.setDescription("A conductance on the cell membrane, full implementation of which is present in a specified file");
        super.setMechanismModel("File Based Membrane Mechanism");
        super.setDefaultInstanceName("NewMembraneMechanism");

        mechanismImpls = new MechanismImplementation[]{};


    }

    public Object clone()
    {
        logger.logComment("Cloning: "+ this);
        FileBasedMembraneMechanism newMech = new FileBasedMembraneMechanism();

        newMech.setMechanismType(getMechanismType());

        for (int i = 0; i < mechanismImpls.length; i++)
        {
                newMech.specifyNewImplFile(mechanismImpls[i].getSimulationEnvironment(),
                                              mechanismImpls[i].getImplementingFile());
        }
        logger.logComment("Cloned to: "+ newMech);
        return newMech;

    }


    public void specifyMechanismType(String mechType)
    {
        setMechanismType(mechType);

        if (getMechanismType().equals(AbstractedCellMechanism.CHANNEL_MECHANISM))
        {
            //System.out.println("Adding new param...");
            addNewParameter(DistMembraneMechanism.COND_DENSITY,
                            DistMembraneMechanism.COND_DENSITY_DESC,
                            0.001e-3f,
                            UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        }

    }


    public static void main(String[] args)
    {
        FileBasedMembraneMechanism na = new FileBasedMembraneMechanism();

        try
        {
            na.setInstanceName("na");
            na.setMechanismType(AbstractedCellMechanism.CHANNEL_MECHANISM);

            na.specifyNewImplFile("GENESIS", "/home/padraig/genesis/Scripts/pattraub/CaChan.g");


            na.printDetails();

            na = (FileBasedMembraneMechanism)na.clone();
            na.setInstanceName("na2");

            na.addNewParameter(DistMembraneMechanism.COND_DENSITY,
                               DistMembraneMechanism.COND_DENSITY_DESC,
                               0.001f,
                               UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

            na.printDetails();

            na.createImplementationFile(SimEnvHelper.GENESIS,
                                        UnitConverter.GENESIS_SI_UNITS,
                                        new File("../temp/test.mod"),
                                        null, false,
                true,false,false);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }



}
