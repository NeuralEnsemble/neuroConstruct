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

package ucl.physiol.neuroconstruct.utils.xml;


/**
 * Simple XML API. Note this is a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 *  
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
