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
 * NeuroML constants. Defines tags needed in NeuroML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuroMLConstants
{
    public static String NEUROML_LEVEL_1 = "Level 1";

    public static String NEUROML_LEVEL_2 = "Level 2";

    public static String NEUROML_LEVEL_3 = "Level 3";

    public static String NEUROML_VERSION_1 = "1.x";
    
    public static String NEUROML_VERSION_2 = "2.x";

    public static String ROOT_ELEMENT = "neuroml";

    public static String NAMESPACE_URI = "http://morphml.org/neuroml/schema";

    public static String DEFAULT_SCHEMA_FILENAME = "NeuroML.xsd";


    /**
     * General constants used in NeuroML/MorphML
     */

    public static String XSI_PREFIX = "xsi";

    public static String XML_NS = "xmlns";

    public static String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    public static String XSI_SCHEMA_LOC = "xsi:schemaLocation";
}
