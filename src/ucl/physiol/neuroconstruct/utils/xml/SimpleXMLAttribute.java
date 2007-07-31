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

package ucl.physiol.neuroconstruct.utils.xml;


/**
 * Simple XML API. Note this is a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class SimpleXMLAttribute extends SimpleXMLEntity
{
    private String name = null;

    private String value = null;

    //String nsPrefix = null;
    protected String secondaryFormattingColour = null;


    public SimpleXMLAttribute(String name, String value)
    {
        mainFormattingColour = "#990043";
        secondaryFormattingColour = "blue";
        /*
        if (name.indexOf(":") >= 0)
        {
            this.name = name.substring(name.indexOf(":")+1);
        //    this.nsPrefix = name.substring(0, name.indexOf(":"));

        }

        else*/ this.name = name;

        this.value = value;
    }

    public String getName()
    {
        return name;
    }
/*
    public String getQualifiedName()
    {
        if (nsPrefix==null) return name;
        else return nsPrefix + ":" + name;

    }
*/

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
/*
    public String getNsPrefix()
    {
        return nsPrefix;
    };


    public void setNsPrefix(String nsPrefix)
    {
        this.nsPrefix = nsPrefix;
    };
*/


    public String toString()
    {
        //return "Attribute: "+this.getQualifiedName();
        return "Attribute: "+this.getName();
    }


    public String getXMLString(String indent, boolean formatted)
    {
        if (!formatted)
        {
            return getName() + "=\"" + value + "\"";
        }
        else
        {
            //return "<b>"+ getName() + "</b> = <span style=\"color:"+mainFormattingColour+"\">\"" + value + "\"</span>";
            return "<span style=\"color:"+mainFormattingColour+"\">" + getName() + "</span> = "
                   + "<span style=\"color:"+secondaryFormattingColour+"\">\"" + SimpleXMLElement.convertXMLToHTMLFriendly(value) + "\"</span>";
        }
    }


}
