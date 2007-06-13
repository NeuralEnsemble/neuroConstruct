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
 * @version 1.0.3
 */



public class SimpleXMLNamespace extends SimpleXMLEntity
{
    private String prefix = null;
    private String uri = null;

    public SimpleXMLNamespace(String prefix, String uri)
    {
        mainFormattingColour = "red";
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }


    public String toString()
    {
        return "Namespace: "+this.getPrefix() + ", "+getUri();
    }


    public String getXMLString(String indent, boolean formatted)
    {
        if (!formatted)
        {
            if (prefix.length()==0) return "xmlns=\"" + uri + "\"";
            return "xmlns:"+ prefix + "=\"" + uri + "\"";
        }
        else
        {
            if (prefix.length()==0) return "<b>xmlns</b>= <span style=\"color:"+mainFormattingColour+"\"> \"" + uri + "\"</span>";
            return "<b>"+ "xmlns:"+ prefix + "</b>= <span style=\"color:"+mainFormattingColour+"\"> \"" + uri + "\"</span>";
        }
    }


}
