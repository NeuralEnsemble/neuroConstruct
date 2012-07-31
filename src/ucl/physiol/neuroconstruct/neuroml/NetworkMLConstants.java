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
 * NetworkML file format. Defines tags needed in NetworkML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class NetworkMLConstants
{

    public static String DEFAULT_NETWORKML_FILENAME_XML = "Generated.net.xml";
    public static String DEFAULT_NETWORKML_FILENAME_HDF5 = "Generated.net.h5";
    
    public static String ROOT_ELEMENT = "networkml";

    public static String NAMESPACE_URI = "http://morphml.org/networkml/schema";

    public static String DEFAULT_SCHEMA_FILENAME = "http://www.neuroml.org/NeuroMLValidator/NeuroMLFiles/Schemata/v1.8.1/Level3/NetworkML_v1.8.1.xsd";
    //public static String DEFAULT_SCHEMA_FILENAME = "NetworkML_v1.8.1.xsd";

    public static String PREFIX = "net";

    public static String UNITS_ATTR = "units";

    public static String UNITS_SI = "SI Units";
    public static String UNITS_PHYSIOLOGICAL = "Physiological Units";


    public static String POPULATIONS_ELEMENT = "populations";

    public static String POPULATION_ELEMENT = "population";

    public static String POP_NAME_ATTR = "name";

    ////Pre v1.7.1 specification
    public static String CELLTYPE_ELEMENT = "cell_type";
    ////Post v1.7.1 specification
    public static String CELLTYPE_ATTR = "cell_type";
    
    public static String INSTANCES_ELEMENT = "instances";

    public static String INSTANCES_SIZE_ATTR = "size";
    
    public static String INSTANCE_ELEMENT = "instance";
    public static String INSTANCE_ID_ATTR = "id";
    public static String NODE_ID_ATTR = "node_id";

    public static String LOCATION_ELEMENT = "location";
    public static String LOC_X_ATTR = "x";
    public static String LOC_Y_ATTR = "y";
    public static String LOC_Z_ATTR = "z";



    public static String PROJECTIONS_ELEMENT = "projections";
    public static String PROJECTION_ELEMENT = "projection";

    public static String PROJ_NAME_ATTR = "name";

    ////Pre v1.7.1 specification
    public static String SOURCE_ELEMENT = "source";
    public static String TARGET_ELEMENT = "target";
    
    ////Post v1.7.1 specification
    public static String SOURCE_ATTR = "source";
    public static String TARGET_ATTR = "target";

    public static String SYN_PROPS_ELEMENT = "synapse_props";
    
    ////Pre v1.7.1 specification
    public static String SYN_TYPE_ELEMENT = "synapse_type";
    public static String DEFAULT_VAL_ELEMENT = "default_values";
    
    ////Post v1.7.1 specification
    public static String SYN_TYPE_ATTR = "synapse_type";



    public static String INTERNAL_DELAY_ATTR = "internal_delay";
    public static String PRE_DELAY_ATTR = "pre_delay";
    public static String POST_DELAY_ATTR = "post_delay";
    public static String PROP_DELAY_ATTR = "prop_delay";

    public static String WEIGHT_ATTR = "weight";
    public static String THRESHOLD_ATTR = "threshold";


    public static String CONNECTIONS_ELEMENT = "connections";
    public static String CONNECTIONS_SIZE_ATTR = "size";

    public static String CONNECTION_ELEMENT = "connection";

    public static String CONNECTION_ID_ATTR = "id";


    public static String PRE_CONN_ELEMENT = "pre";
    public static String POST_CONN_ELEMENT = "post";

    public static String CELL_ID_ATTR = "cell_id";
    public static String SEGMENT_ID_ATTR = "segment_id";
    public static String FRACT_ALONG_ATTR = "fraction_along";
    
    
    public static String PRE_CELL_ID_ATTR = "pre_cell_id";
    public static String PRE_SEGMENT_ID_ATTR = "pre_segment_id";
    public static String PRE_FRACT_ALONG_ATTR = "pre_fraction_along";
    
    public static String POST_CELL_ID_ATTR = "post_cell_id";
    public static String POST_SEGMENT_ID_ATTR = "post_segment_id";
    public static String POST_FRACT_ALONG_ATTR = "post_fraction_along";
    

    public static String CONN_PROP_ELEMENT = "properties";
    
    
    public static String GROUP_ELEMENT = "group";


    public static String CONNECTIVITY_ELEMENT = "connectivity";
    public static String POT_SYN_LOC_ELEMENT_preV1_7_1 = "potentialSynapticLocation";
    public static String POT_SYN_LOC_ELEMENT = "potential_syn_loc";
    
    public static String SYN_DIR_ELEMENT = "synapse_direction";
    public static String SYN_DIR_PRE = "pre";
    public static String SYN_DIR_POST = "post";
    public static String SYN_DIR_PRE_ANDOR_POST = "preAndOrPost";

    
    public static String INPUTS_ELEMENT = "inputs";
    public static String INPUT_ELEMENT = "input";

    public static String INPUT_NAME_ATTR = "name";

    public static String INPUT_TARGET_ELEMENT = "target";
    public static String INPUT_TARGET_SITES_ELEMENT = "sites";
    
    public static String INPUT_SITES_SIZE_ATTR = "size";
    
    public static String INPUT_TARGET_SITE_ELEMENT = "site";
    public static String INPUT_TARGET_CELLGROUP_OLD_ATTR = "cell_group";
    public static String INPUT_TARGET_POPULATION_ATTR = "population";

    public static String PULSEINPUT_ELEMENT = "pulse_input";
    public static String PULSEINPUT_INSTANCE_ELEMENT = "pulse_input_instance";

    public static String INPUT_DELAY_ATTR = "delay";
    public static String INPUT_DUR_ATTR = "duration";
    public static String INPUT_AMP_ATTR = "amplitude";  

    public static String RANDOMSTIM_ELEMENT = "random_stim";
    public static String RANDOMSTIM_INSTANCE_ELEMENT = "random_stim_instance";

    public static String RND_STIM_FREQ_ATTR = "frequency";    
    public static String RND_STIM_MECH_ATTR = "synaptic_mechanism";

    public static String INPUT_SITE_CELLID_ATTR = "cell_id";
    public static String INPUT_SITE_SEGID_ATTR = "segment_id";
    public static String INPUT_SITE_FRAC_ATTR = "fraction_along";
    
    //////  neuroConstruct only constants

    public static String NC_NETWORK_GEN_RAND_SEED = "neuroConstruct_random_seed";
    public static String NC_SIM_CONFIG = "neuroConstruct_sim_config";
    public static String NC_SIM_DURATION = "neuroConstruct_sim_duration";    
    public static String NC_SIM_TIME_STEP = "neuroConstruct_sim_time_step";
    public static String NC_TEMPERATURE = "neuroConstruct_temperature";


    public static String NEUROML2_NETWORK_ELEMENT = "network";
    public static String NEUROML2_NETWORK_ID_PREFIX = "network_";
    
    public static String NEUROML2_EXTRACELLULAR_PROPS_ELEMENT = "extracellularProperties";
    public static String NEUROML2_TEMPERATURE_ATTR = "temperature";


    public static String NEUROML2_PULSE_GEN_ELEMENT = "pulseGenerator";

    public static String NEUROML2_EXP_INPUT_ELEMENT = "explicitInput";
    public static String NEUROML2_EXP_INPUT_TARGET_ATTR = "target";
    public static String NEUROML2_EXP_INPUT_INPUT_ATTR = "input";


    public static String NEUROML2_EXP_CONN_ELEMENT = "synapticConnection";
    public static String NEUROML2_EXP_CONN_WD_ELEMENT = "synapticConnectionWD";
    public static String NEUROML2_EXP_CONN_FROM_ATTR = "from";
    public static String NEUROML2_EXP_CONN_TO_ATTR = "to";
    public static String NEUROML2_EXP_CONN_SYN_ATTR = "synapse";
    public static String NEUROML2_EXP_CONN_WEIGHT_ATTR = "weight";
    public static String NEUROML2_EXP_CONN_DELAY_ATTR = "delay";

    
    private NetworkMLConstants()
    {
    }





}
