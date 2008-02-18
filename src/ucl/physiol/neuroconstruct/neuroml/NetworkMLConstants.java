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
 * NetworkML file format. Defines tags needed in NetworkML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class NetworkMLConstants
{

    public static String DEFAULT_NETWORKML_FILENAME = "Generated.net.xml";
    
    public static String ROOT_ELEMENT = "networkml";

    public static String NAMESPACE_URI = "http://morphml.org/networkml/schema";

    //public static String DEFAULT_SCHEMA_FILENAME = "http://www.morphml.org:8080/NeuroMLValidator/NeuroMLFiles/Schemata/v1.7/Level3/NetworkML_v1.7.xsd";
    public static String DEFAULT_SCHEMA_FILENAME = "NetworkML_v1.7.xsd";
    //public static String DEFAULT_SCHEMA_FILENAME = "http://localhost:8084/NeuroMLValidator/NeuroMLFiles/Schemata/v1.7.1/Level3/NetworkML_v1.7.1.xsd";

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


    public static String POTENTIAL_SYN_LOC_ELEMENT = "potentialSynapticLocation";
    public static String SYN_DIR_ELEMENT = "synapse_direction";
    public static String SYN_DIR_PRE = "pre";
    public static String SYN_DIR_POST = "post";
    public static String SYN_DIR_PRE_ANDOR_POST = "preAndOrPost";


    //////  neuroConstruct only constants

    public static String NC_NETWORK_GEN_RAND_SEED = "neuroConstruct_random_seed";
    public static String NC_SIM_CONFIG = "neuroConstruct_sim_config";





}
