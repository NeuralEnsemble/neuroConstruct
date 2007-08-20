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

import java.util.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for Cell Mechanisms
 *
 * @author Padraig Gleeson
 * @version 1.0.6
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
