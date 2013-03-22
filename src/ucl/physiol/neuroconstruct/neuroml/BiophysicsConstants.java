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
    public static String VAR_PARAMETER_ELEMENT = "variable_parameter";

    public static String PARAMETER_NAME_ATTR = "name";
    public static String PARAMETER_VALUE_ATTR = "value";
    public static String INHOMOGENEOUS_VALUE = "inhomogeneous_value";
    public static String INHOMOGENEOUS_PARAM_NAME = "param_name";
    public static String INHOMOGENEOUS_PARAM_VALUE = "value";



    public static String PARAMETER_GMAX = "gmax";
    public static String PARAMETER_REV_POT = "e";
    public static String PARAMETER_REV_POT_2 = "erev";  // Sometimes used...


    public static String PARAMETER_CONC_INT = "conc_i";
    public static String PARAMETER_CONC_EXT = "conc_e";


    public static String MECHANISM_NAME_ATTR = "name";
    public static String MECHANISM_TYPE_ATTR = "type";
    public static String MECHANISM_PASSIVE_COND_ATTR_pre_v1_7_1 = "passiveConductance";
    public static String MECHANISM_PASSIVE_COND_ATTR = "passive_conductance";

    //public static String MECHANISM_TYPE_MEMB_COND = "Membrane Conductance";
    public static String MECHANISM_TYPE_CHAN_MECH = "Channel Mechanism";
    public static String MECHANISM_TYPE_ION_CONC = "Ion Concentration";

    public static String MECHANISM_TYPE_SYN_LOC = "Potential Synaptic Connection Location";
    public static String MECHANISM_TYPE_SPEC_AX_RES = "Specific Axial Resistance";
    public static String MECHANISM_TYPE_SPEC_CAP = "Specific Capacitance";
    public static String MECHANISM_TYPE_INIT_POT = "Initial Membrane Potential";


    public static String SPEC_AX_RES_NAME = "Ra";
    public static String SPEC_CAP_NAME = "cm";
    public static String INIT_POT_NAME = "initV";

    public static String SPIKE_THRESHOLD_v2 = "spikeThresh";

    public static String SPECIFIC_CAP_ELEMENT_pre_v1_7_1 = "specificCapacitance";
    public static String SPECIFIC_CAP_ELEMENT = "spec_capacitance";
    public static String SPECIFIC_CAP_ELEMENT_v2 = "specificCapacitance";
    
    public static String SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1 = "specificAxialResistance";
    public static String SPECIFIC_AX_RES_ELEMENT = "spec_axial_resistance";
    public static String SPECIFIC_AX_RES_ELEMENT_V2 = "resistivity";

    public static String INITIAL_POT_ELEMENT_pre_v1_7_1 = "initialMembPotential";
    public static String INITIAL_POT_ELEMENT = "init_memb_potential";
    public static String INITIAL_POT_ELEMENT_V2 = "initMembPotential";


    public static String ION_PROPS_ELEMENT = "ion_props";
    public static String ION_PROPS_NAME_ATTR = "name";



    public static String MECHANISM_VALUE_ATTR = "value";

    public static String GROUP_ELEMENT = "group";

    public static String SEG_GROUP_ATTR_V2 = "segmentGroup";

    public static String VALUE_ATTR_V2 = "value";
    
    public static String TYPE_ATTR_V2 = "type";



    public static String BIOPHYS_PROPS_ELEMENT_V2 = "biophysicalProperties";

    public static String MEMB_PROPS_ELEMENT_V2 = "membraneProperties";
    public static String INTRACELL_PROPS_ELEMENT_V2 = "intracellularProperties";


    public static String CHAN_DENSITY_ELEMENT_V2 = "channelDensity";
    public static String CHAN_DENSITY_NERNST_ELEMENT_V2 = "channelDensityNernst";

    public static String ION_CHAN_ATTR_V2 = "ionChannel";
    public static String COND_DENS_ATTR_V2 = "condDensity";
    public static String REV_POT_ATTR_V2 = "erev";
    public static String ION_ATTR_V2 = "ion";
    
    public static String SPECIES_ELEMENT_V2 = "species";
    
    private BiophysicsConstants()
    {
    }



}
