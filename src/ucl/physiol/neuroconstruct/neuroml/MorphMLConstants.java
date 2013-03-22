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
 * MorphML file format. Defines tags needed in MorphML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class MorphMLConstants
{

    public static String ROOT_ELEMENT = "morphml";

    public static String NAMESPACE_URI = "http://morphml.org/morphml/schema";

    public static String PREFIX = "mml";

    public static String DEFAULT_SCHEMA_LOCATION = "http://neuroml.svn.sourceforge.net/viewvc/neuroml/trunk/web/NeuroMLFiles/Schemata/v1.8.1/Level1/MorphML_v1.8.1.xsd";


    public static String CELLS_ELEMENT = "cells";
    public static String CELL_ELEMENT = "cell";

    public static String CELL_NAME_ATTR = "name";    


    public static String SEGMENTS_ELEMENT = "segments";


    public static String SEGMENT_ELEMENT = "segment";

    public static String SEGMENT_ID_ATTR = "id";
    public static String SEGMENT_NAME_ATTR = "name";
    public static String SEGMENT_CABLE_ID_ATTR = "cable";
    public static String SEGMENT_PARENT_ATTR = "parent";

    public static String SEGMENT_PROXIMAL_ELEMENT = "proximal";
    public static String SEGMENT_DISTAL_ELEMENT = "distal";


    public static String POINT_X_ATTR = "x";
    public static String POINT_Y_ATTR = "y";
    public static String POINT_Z_ATTR = "z";
    public static String POINT_DIAM_ATTR = "diameter";

    public static String CABLES_ELEMENT = "cables";
    public static String CABLE_ELEMENT = "cable";
    public static String CABLE_GROUP_ELEMENT = "cablegroup";
    public static String CABLE_GROUP_NAME = "name";
    public static String CABLE_GROUP_ENTRY_ELEMENT = "cable";

    public static String CABLE_ID_ATTR = "id";
    public static String CABLE_NAME_ATTR = "name";

    public static String SOMA_CABLE_GROUP = "soma_group";
    public static String AXON_CABLE_GROUP = "axon_group";
    public static String DENDRITE_CABLE_GROUP = "dendrite_group";

    public static String PROPS_ELEMENT = "properties";

    public static String COMMENT_PROP = "comment";

    public static String FRACT_ALONG_PARENT_ATTR = "fract_along_parent";
    public static String FRACT_ALONG_PARENT_ATTR_pre_v1_7_1 = "fractAlongParent";

    public static String NUMBER_INTERNAL_DIVS_PROP = "numberInternalDivisions";

    public static String FINITE_VOL_PROP = "finiteVolume";

    public static String INHOMO_PARAM = "inhomogeneous_param";
    public static String INHOMO_PARAM_NAME_ATTR = "name";
    public static String INHOMO_PARAM_VARIABLE_ATTR = "variable";

    public static String INHOMO_PARAM_METRIC = "metric";
    public static String INHOMO_PARAM_PROXIMAL = "proximal";
    public static String INHOMO_PARAM_PROXIMAL_TRANS_START_ATTR = "translationStart";

    public static String INHOMO_PARAM_DISTAL = "distal";
    public static String INHOMO_PARAM_DISTAL_NORM_END_ATTR = "normalizationEnd";



    public static String INCLUDE_V2 = "include";
    public static String HREF_V2 = "href";

    public static String MORPHOLOGY_V2 = "morphology";
    public static String PARENT_V2 = "parent";
    public static String SEGMENT_V2 = "segment";
    public static String PARENT_FRACT_ALONG_V2 = "fractionAlong";


    public static String SEG_GROUP_V2 = "segmentGroup";
    public static String MEMBER_V2 = "member";



    private MorphMLConstants()
    {
    }







}
