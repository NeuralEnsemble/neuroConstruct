/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
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
 *  
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
    public static String PARAMETER_REV_POT = "e";
    public static String PARAMETER_REV_POT_2 = "erev";  // Sometimes used...


    public static String MECHANISM_NAME_ATTR = "name";
    public static String MECHANISM_TYPE_ATTR = "type";
    public static String MECHANISM_PASSIVE_COND_ATTR_pre_v1_7_1 = "passiveConductance";
    public static String MECHANISM_PASSIVE_COND_ATTR = "passive_conductance";

    //public static String MECHANISM_TYPE_MEMB_COND = "Membrane Conductance";
    public static String MECHANISM_TYPE_CHAN_MECH = "Channel Mechanism";

    public static String MECHANISM_TYPE_SYN_LOC = "Potential Synaptic Connection Location";
    public static String MECHANISM_TYPE_SPEC_AX_RES = "Specific Axial Resistance";
    public static String MECHANISM_TYPE_SPEC_CAP = "Specific Capacitance";
    public static String MECHANISM_TYPE_INIT_POT = "Initial Membrane Potential";


    public static String SPEC_AX_RES_NAME = "Ra";
    public static String SPEC_CAP_NAME = "cm";
    public static String INIT_POT_NAME = "initV";

    public static String SPECIFIC_CAP_ELEMENT_pre_v1_7_1 = "specificCapacitance";
    public static String SPECIFIC_CAP_ELEMENT = "spec_capacitance";
    
    public static String SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1 = "specificAxialResistance";
    public static String SPECIFIC_AX_RES_ELEMENT = "spec_axial_resistance";
    
    public static String INITIAL_POT_ELEMENT_pre_v1_7_1 = "initialMembPotential";
    public static String INITIAL_POT_ELEMENT = "init_memb_potential";



    public static String MECHANISM_VALUE_ATTR = "value";

    public static String GROUP_ELEMENT = "group";

    private BiophysicsConstants()
    {
    }



}
