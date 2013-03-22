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

import java.io.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple XML API. Note this is a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 *  
 */


public class SimpleXMLContent extends SimpleXMLEntity
{

    String text = null;


    public SimpleXMLContent(String text)
    {
        mainFormattingColour = "black";
        this.text = text;
    }



    public void setText(String text)
    {
        this.text = text;
    }

    public void appendText(String extraText)
    {
        this.text = new String(text + extraText);
    }



    public String getText()
    {
        return text;
    }


    public String toString()
    {
        return "Content: "+this.getText();
    }


    public String getXMLString(String indent, boolean formatted)
    {

        if (!formatted)
        {
            //return indent + convertToXMLFriendly(text);
            return indent + text;
        }
        else
        {
            if (text.trim().length()==0)
            {
                // i.e. spaces + carr returns...
                return text;
            }
            String newIndent = GeneralUtils.replaceAllTokens(indent, " ", "&nbsp;");


            return newIndent + "<span style=\"color:"+mainFormattingColour+"\">" + SimpleXMLElement.convertXMLToHTMLFriendly(text)+"</span>";
        }

    }



    public static void main(String[] args)
    {
        try
        {
            File xmlFile = new File("templates/xmlTemplates/Examples/ChannelML/KChannelCom.xml");
            File xslFile = new File("templates/xmlTemplates/Schemata/v1.2/Level2/ChannelML_v1.2_GENESIStab.xsl");
            //File tempFile = new File("../temp");

            SimpleXMLDocument xmlDoc = SimpleXMLReader.getSimpleXMLDoc(xmlFile);
            SimpleXMLDocument xslDoc = SimpleXMLReader.getSimpleXMLDoc(xslFile);

            System.out.println(xslDoc.getXMLString("", false));
            System.out.println("\n\n---------------------------------\n\n");


            //XMLUtils.transform(xmlDoc.getXMLString("", false), xslDoc.getXMLString("", false), tempFile, ".g");
            System.out.println(XMLUtils.transform(xmlDoc.getXMLString("", false), xslDoc.getXMLString("", false)));
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }


}
