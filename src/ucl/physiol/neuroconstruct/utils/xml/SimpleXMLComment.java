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



public class SimpleXMLComment extends SimpleXMLEntity
{
    private String comment = null;

    public SimpleXMLComment(String comment)
    {
        mainFormattingColour = "green";

        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }



    public String toString()
    {
        return "Comment: "+comment;
    }


    public String getXMLString(String indent, boolean formatted)
    {
        if (!formatted)
        {
            return indent +"<!--" + comment + "-->\n"; // note comment contents aren't trimmed so the before and after spaces will be there...
        }
        else
        {
            return indent+"<span style=\"color:"+mainFormattingColour+"\">&lt;!-- " + comment + " --&gt;</span>";
        }
    }


}
