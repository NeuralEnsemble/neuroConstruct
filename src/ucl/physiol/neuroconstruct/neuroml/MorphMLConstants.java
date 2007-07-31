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
 * MorphML file format. Defines tags needed in MorphML files...
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class MorphMLConstants
{

    public static String ROOT_ELEMENT = "morphml";

    public static String NAMESPACE_URI = "http://morphml.org/morphml/schema";

    public static String PREFIX = "mml";

    public static String DEFAULT_SCHEMA_FILENAME = "MorphML.xsd";




    public static String CELLS_ELEMENT = "cells";
    public static String CELL_ELEMENT = "cell";

    public static String CELL_NAME_ATTR = "name";


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
    public static String SECTION_GROUP_ELEMENT = "cablegroup";
    public static String SECTION_GROUP_ENTRY_ELEMENT = "cable";

    public static String SECTION_ID_ATTR = "id";
    public static String SECTION_NAME_ATTR = "name";


    //public static String SECTION_GROUP = "group";

    public static String SOMA_SECTION_GROUP = "soma_group";
    public static String AXON_SECTION_GROUP = "axon_group";
    public static String DENDRITE_SECTION_GROUP = "dendrite_group";

    public static String PROPS_ELEMENT = "properties";

    public static String COMMENT_PROP = "comment";

    public static String FRACT_ALONG_PROP = "fractAlongParent";

    public static String NUMBER_INTERNAL_DIVS_PROP = "numberInternalDivisions";

    public static String FINITE_VOL_PROP = "finiteVolume";







}
