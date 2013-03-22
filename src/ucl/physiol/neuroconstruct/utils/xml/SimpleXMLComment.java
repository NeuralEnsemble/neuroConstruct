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
