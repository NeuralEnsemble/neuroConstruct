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

package ucl.physiol.neuroconstruct.neuroml;



/**
 * Biophysics file format. Defines tags needed in Biophysics files...
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class BiophysicsConstants
{

    public static String ROOT_ELEMENT = "biophysics";

    public static String NAMESPACE_URI = "http://morphml.org/biophysics/schema";

    public static String DEFAULT_SCHEMA_FILENAME = "Biophysics.xsd";

    public static String PREFIX = "bio";

    public static String UNITS_ATTR = "units";

    public static String UNITS_SI = "SI Units";
    public static String UNITS_PHYSIOLOGICAL = "Physiological Units";


    public static String MECHANISM_ELEMENT = "mechanism";

    public static String PARAMETER_ELEMENT = "parameter";
    public static String PARAMETER_NAME_ATTR = "name";
    public static String PARAMETER_VALUE_ATTR = "value";


    public static String PARAMETER_GMAX = "gmax";


    public static String MECHANISM_NAME_ATTR = "name";
    public static String MECHANISM_TYPE_ATTR = "type";

    //public static String MECHANISM_TYPE_MEMB_COND = "Membrane Conductance";
    public static String MECHANISM_TYPE_CHAN_MECH = "Channel Mechanism";

    public static String MECHANISM_TYPE_SYN_LOC = "Potential Synaptic Connection Location";
    public static String MECHANISM_TYPE_SPEC_AX_RES = "Specific Axial Resistance";
    public static String MECHANISM_TYPE_SPEC_CAP = "Specific Capacitance";
    public static String MECHANISM_TYPE_INIT_POT = "Initial Membrane Potential";


    public static String SPEC_AX_RES_NAME = "Ra";
    public static String SPEC_CAP_NAME = "cm";
    public static String INIT_POT_NAME = "initV";

    public static String SPEC_CAP_ELEMENT = "specificCapacitance";
    public static String SPEC_AX_RES_ELEMENT = "specificAxialResistance";
    public static String INIT_POT_ELEMENT = "initialMembPotential";



    public static String MECHANISM_VALUE_ATTR = "value";

    public static String GROUP_ELEMENT = "group";



/*
    public static String LENGTH_UNITS = "lengthUnits";


    public static String CELLS_ELEMENT = "cells";
    public static String CELL_ELEMENT = "cell";

    public static String CELL_NAME_ATTR = "name";
    public static String NOTES_ELEMENT = "notes";

    public static String PROPS_ELEMENT = "properties";
    public static String PROP_ELEMENT = "property";
    public static String PROP_TAG_ELEMENT = "tag";
    public static String PROP_VALUE_ELEMENT = "value";

    public static String SEGMENTS_ELEMENT = "segments";
    public static String SEGMENT_ELEMENT = "segment";

    public static String SEGMENT_ID_ATTR = "id";
    public static String SEGMENT_NAME_ATTR = "name";
    public static String SEGMENT_SEC_ID_ATTR = "cable";
    public static String SEGMENT_PARENT_ATTR = "parent";

    public static String SEGMENT_PROXIMAL_ELEMENT = "proximal";
    public static String SEGMENT_DISTAL_ELEMENT = "distal";


    public static String POINT_X_ATTR = "x";
    public static String POINT_Y_ATTR = "y";
    public static String POINT_Z_ATTR = "z";
    public static String POINT_DIAM_ATTR = "diameter";

    public static String SECTIONS_ELEMENT = "cables";
    public static String SECTION_ELEMENT = "cable";

    public static String SECTION_ID_ATTR = "id";
    public static String SECTION_NAME_ATTR = "name";
    public static String SECTION_GROUP_ELEMENT = "group";






    public static String INIT_POT_TAG = "initialPotential";
    public static String SPEC_AX_RES_TAG = "specificAxialResistance";
    public static String SPEC_CAP_TAG = "specificCapacitance";

*/

}
