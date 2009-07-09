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

import java.util.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for Cell Mechanisms
 *
 * @author Padraig Gleeson
 *  
 */

public  class CellMechanismHelper
{
    static ClassLogger logger = new ClassLogger("CellMechanismHelper");

    public static final String PROP_CELL_MECH_NAME = "Mechanism Name";
    public static final String PROP_CELL_MECH_DESCRIPTION = "Description";
    public static final String PROP_CELL_MECH_DEFAULT_NAME = "Default Instance Name";
    public static final String PROP_CELL_MECH_MODEL = "Mechanism Model";
    public static final String PROP_CELL_MECH_TYPE = "Mechanism Type";
    public static final String PROP_PLOT_INFO_FILE = "Plot Info File";
    public static final String PROP_CHANNELML_FILE = "ChannelML file";
    public static final String PROP_SBML_FILE = "SBML file";
    public static final String PROP_IMPL_METHOD = "Implementation method";
    public static final String PROP_SIMENV_SUFFIX = " implementation";
    public static final String PROP_MAPPING_SUFFIX = " mapping";

    public static final String PROP_NEEDS_COMP_SUFFIX = " file requires compilation";

    public static final String INTERNAL_PROPS_FILENAME = "InternalProperties.xml";
    public static final String PROPERTIES_FILENAME = "properties.xml";


    private static Vector availableCellMechs = new Vector();

    static
    {
        /** @todo automatically look these up... */
        availableCellMechs.add(new PassiveMembraneMechanism());
        availableCellMechs.add(new Exp2SynMechanism());
    }

    public static Vector getCellMechanisms()
    {
        return availableCellMechs;
    }

    public static AbstractedCellMechanism getCellMechInstance(String mechanismModel)
    {
        logger.logComment("Getting CellMechanism for mechanismModel: "+ mechanismModel);
        /** @todo replace with reflection... */
        for (int i = 0; i < availableCellMechs.size(); i++)
        {
            AbstractedCellMechanism cellMechanism = (AbstractedCellMechanism)availableCellMechs.elementAt(i);
            logger.logComment("Checking: "+ cellMechanism.getMechanismModel());
            if (cellMechanism.getMechanismModel()!=null && cellMechanism.getMechanismModel().equals(mechanismModel))
            return (AbstractedCellMechanism)cellMechanism.clone();
        }
        return null;
    }

}
