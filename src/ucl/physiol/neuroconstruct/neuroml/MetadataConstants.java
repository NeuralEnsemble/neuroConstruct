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
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLElement;


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

    public static String LENGTH_UNITS = "lengthUnits";

    public static String NOTES_ELEMENT = "notes";

    public static String PROPS_ELEMENT = "properties";
    public static String PROP_ELEMENT = "property";
    public static String PROP_TAG_ELEMENT = "tag";
    public static String PROP_VALUE_ELEMENT = "value";


    public static String GROUP_ELEMENT = "group";


    public static void addProperty(SimpleXMLElement propertiesElement, String tag, String value, String indent)
    {
        String metadataPrefix = MetadataConstants.PREFIX + ":";

        SimpleXMLElement propElement = new SimpleXMLElement(metadataPrefix+ MetadataConstants.PROP_ELEMENT);
        propertiesElement.addChildElement(propElement);

        propElement.addContent("\n"+indent+"    "); // to make it more readable...
        SimpleXMLElement tagElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.PROP_TAG_ELEMENT);
        tagElement.addContent(tag);
        propElement.addChildElement(tagElement);

        propElement.addContent("\n"+indent+"    "); // to make it more readable...

        SimpleXMLElement valElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.PROP_VALUE_ELEMENT);
        valElement.addContent(value);
        propElement.addChildElement(valElement);
        propElement.addContent(indent+"\n"+indent); // to make it more readable...

    }

}
