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
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLElement;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;


/**
 * Metadata constants. Defines tags needed in many NeuroML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class MetadataConstants
{

    public static String NAMESPACE_URI = "http://morphml.org/metadata/schema";

    public static String DEFAULT_FILE_EXTENSION = "xml";

    public static String DEFAULT_SCHEMA_EXTENSION = "xsd";

    public static String DEFAULT_MAPPING_EXTENSION = "xsl";

    public static String DEFAULT_SCHEMA_FILENAME = "Metadata."+DEFAULT_SCHEMA_EXTENSION;

    public static String PREFIX = "meta";

    public static String LENGTH_UNITS_OLD = "lengthUnits";
    public static String LENGTH_UNITS_NEW = "length_units";

    public static String LENGTH_UNITS_MICROMETER = "micrometer";

    public static String NOTES_ELEMENT = "notes";
    
    public static String ANNOTATION_ELEMENT = "annotation";

    public static String PROPS_ELEMENT = "properties";
    public static String PROP_ELEMENT = "property";

    public static String PROP_TAG_ELEMENT = "tag";
    public static String PROP_VALUE_ELEMENT = "value";


    public static String PROP_TAG_ATTR = "tag";
    public static String PROP_VALUE_ATTR = "value";


    public static String GROUP_ELEMENT = "group";


    public static void addProperty(SimpleXMLElement propertiesElement, String tag, String value, String indent)
    {
        addProperty(propertiesElement, tag, value, indent, NeuroMLVersion.NEUROML_VERSION_1);
    }

    public static void addProperty(SimpleXMLElement propertiesElement, String tag, String value, String indent, NeuroMLVersion version)
    {
        String metadataPrefix = version.isVersion2() ? "" : MetadataConstants.PREFIX + ":";

        SimpleXMLElement propElement = new SimpleXMLElement(metadataPrefix+ MetadataConstants.PROP_ELEMENT);

        propertiesElement.addContent("\n"+indent); // to make it more readable...
        propertiesElement.addChildElement(propElement);
        propertiesElement.addContent("\n"); // to make it more readable...

        propElement.addAttribute(PROP_TAG_ATTR, tag);
        propElement.addAttribute(PROP_VALUE_ATTR, value);

        //propElement.addContent("\n"+indent+"    "); // to make it more readable...

        /*
        SimpleXMLElement tagElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.PROP_TAG_ELEMENT);
        tagElement.addContent(tag);
        propElement.addChildElement(tagElement);

        propElement.addContent("\n"+indent+"    "); // to make it more readable...

        SimpleXMLElement valElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.PROP_VALUE_ELEMENT);
        valElement.addContent(value);
        propElement.addChildElement(valElement);*/
        

    }
    
}
