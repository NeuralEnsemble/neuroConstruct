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

import java.util.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import java.io.*;

/**
 * Simple XML API. Note this is a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 *  
 */


public class SimpleXMLDocument extends SimpleXMLEntity
{
    static ClassLogger logger = new ClassLogger("SimpleXMLDocument");


    SimpleXMLElement root = null;

    /**
     * Comments before root element
     */
    ArrayList<SimpleXMLComment> comments = new ArrayList<SimpleXMLComment>();

    //String name = null;

    public SimpleXMLDocument()
    {
        logger.setThisClassSilent(true);
    }


    public void addRootElement(SimpleXMLElement rootElement)
    {
        root = rootElement;
    }

    public SimpleXMLElement getRootElement()
    {
        return root;
    }


    public void addComment(SimpleXMLComment comment)
    {
        comments.add(comment);
    }



    public String getXMLString(String indent, boolean formatted)
    {
        StringBuffer fullXMLString = new StringBuffer();


        if (!formatted) fullXMLString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        else fullXMLString.append("<pre>&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;<br>\n");

        String endOfLine  = "\n";
        if (formatted) endOfLine  = "<br>\n";

        for (int i = 0; i < comments.size(); i++)
        {
            fullXMLString.append(this.comments.get(i).getXMLString("", formatted)+endOfLine);

        }

        try
        {
            fullXMLString.append(this.root.getXMLString(indent, formatted));
            if (formatted) fullXMLString.append("</pre>");
        }
        catch (Exception ex)
        {
            logger.logError("Error:", ex);
        }


        return fullXMLString.toString();
    }


    public String toString()
    {
        return "Document with root: "+root.getName();
    }


    /**
     * Note: a very limited subset of XPath! See implementation
     */
    public String getValueByXPath(String simpleXPathExp)
    {
        logger.logComment(",,    Getting val of: " + simpleXPathExp);

        if (simpleXPathExp.startsWith("//"))
        {
            return root.getValueByXPath(simpleXPathExp);
        }

        // remove //'s
        if (simpleXPathExp.startsWith("/"))
        {
            simpleXPathExp = simpleXPathExp.substring(1);
        }

        if (simpleXPathExp.startsWith(root.getName()))
        {
            if (simpleXPathExp.equals(root.getName()))
            {
                //root.get
            }
            //System.out.println("Returning...");
            String foundAtRoot = root.getValueByXPath(simpleXPathExp.substring(root.getName().length()+1));
            logger.logComment("foundAtRoot: "+ foundAtRoot);
            return foundAtRoot;

        }
        else

        return null;
    }

    /**
     * Note: a very limited subset of XPath! See implementation
     */
    public boolean setValueByXPath(String simpleXPathExp, String value)
    {
        logger.logComment("Attempting to set path: "+simpleXPathExp +" to "+value);
        // remove //'s
        while (simpleXPathExp.startsWith("/"))
        {
            simpleXPathExp = simpleXPathExp.substring(1);
        }
        if (simpleXPathExp.startsWith(root.getName()))
        {
            return root.setValueByXPath(simpleXPathExp.substring(root.getName().length()+1), value);

        }
        return false;
    }

    /**
     * Gets a SimpleXMLEntity in the tree from an expression in a limited form of XPath
     */
    public SimpleXMLEntity[] getXMLEntities(String simpleXPathExp)
    {
        logger.logComment("GGetting val of: " + simpleXPathExp);

        if (simpleXPathExp.startsWith("//"))
        {
            return root.getXMLEntities(simpleXPathExp);
        }

        // remove //'s
        if (simpleXPathExp.startsWith("/"))
        {
            simpleXPathExp = simpleXPathExp.substring(1);
        }

        if (simpleXPathExp.startsWith(root.getName()))
        {
            if (simpleXPathExp.equals(root.getName()))
            {
                //root.get
            }
            return root.getXMLEntities(simpleXPathExp.substring(root.getName().length()+1));

        }
        else

        return null;
    }


    /**
     * Gets locations of 'interesting' items in the form of simplified XPath expressions
     * @param ignoreEmptyContent If true, ignore SimpleXMLContent entries whith only whitespaces,
     * some of which may be left in for clarity when regenerating the parsed XML file
     */
    public ArrayList<String> getXPathLocations(boolean ignoreEmptyContent)
    {
        ArrayList<String> rootElXPathLocations = root.getXPathLocations(ignoreEmptyContent);
        ArrayList<String> docXPathLocations = new ArrayList<String>();
        for (int i = 0; i < rootElXPathLocations.size(); i++)
        {
            docXPathLocations.add("/"+this.root.getName() + "/"+ rootElXPathLocations.get(i));
        }
        return docXPathLocations;
    }


    public static void main(String[] args)
    {

        System.out.println("-----------  First test, create document  -----------");

        SimpleXMLDocument doc = new SimpleXMLDocument();

        SimpleXMLElement sx = new SimpleXMLElement("channelml");

        doc.addRootElement(sx);

        sx.addComment(new SimpleXMLComment("This file has been generated by neuroConstruct"));


        sx.addNamespace(new SimpleXMLNamespace("", "http://morphml.org/channelml/schema/1.0.0"));
        sx.addNamespace(new SimpleXMLNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
        sx.addAttribute(new SimpleXMLAttribute("xsi:schemaLocation", "http://morphml.org/channelml/schema/1.0.0     ../../Schemata/ChannelML/v1.0/ChannelML_1.0.xsd"));


        SimpleXMLElement sx2 = new SimpleXMLElement("channel_type");
        sx2.addAttribute(new SimpleXMLAttribute("name", "leak"));
        sx2.addAttribute(new SimpleXMLAttribute("density", "yes"));


        SimpleXMLElement sx3 = new SimpleXMLElement("current_voltage_relation");
        SimpleXMLElement sx4 = new SimpleXMLElement("ohmic");
        sx4.addAttribute(new SimpleXMLAttribute("NonSpecific", "yes &lt; no"));
        sx4.addAttribute(new SimpleXMLAttribute("default_erev", "-0.1"));

        sx.addChildElement(sx2);
        sx2.addChildElement(sx3);

        sx3.addChildElement(sx4);

/*
        SimpleXMLElement sx21 = new SimpleXMLElement("another_chan");

        sx.addChildElement(sx21);

        SimpleXMLElement sx31 = new SimpleXMLElement("current_voltage_relation");
        SimpleXMLElement sx41 = new SimpleXMLElement("ohmic");
        SimpleXMLElement sx51 = new SimpleXMLElement("   \n");
        SimpleXMLElement sx61 = new SimpleXMLElement("ohmic2");
        sx21.addChildElement(sx31);

        sx31.addChildElement(sx41);
        sx31.addChildElement(sx51);
        sx31.addChildElement(sx61);
*/

        System.out.println("Simple xml string:");

        String inXml = doc.getXMLString("",false);

        System.out.println(inXml);

        File outF = new File("../temp/Generic.xml");

        //File testFile = new File("models/MDeSXML/cellProcesses/Generic_Na/NaF_Chan.xml");
        File testFile = new File("templates/xmlTemplates/Examples/ChannelML/KChannel.xml");
        //File testFile = new File("../temp/Generic2.xml");

        System.out.println("Testing file: "+ testFile.getAbsolutePath());

        FileWriter fw = null;
        try
        {
            fw = new FileWriter(outF);
            fw.write(inXml);
            fw.close();

            System.out.println("-----------  Second test, save and load document  -----------");

            SimpleXMLDocument newDoc = SimpleXMLReader.getSimpleXMLDoc(testFile);

            System.out.println("Docread: ");
            String stringForm = newDoc.getXMLString("", false);
            System.out.println(stringForm);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }















        if (true) System.exit(0);

        String xpath1 = "channelml/channel_type/current_voltage_relation/ohmic/@default_erev";
        //String xpath1 = "channelml/channel_type/current_voltage_relation/ohmic/conductance";
        //String xpath1 = "//@default_gmax";

        ArrayList<String> locs = doc.getXPathLocations(true);

        for (int i = 0; i < locs.size(); i++)
        {
            System.out.println("        ----      "+locs.get(i) + " = " + doc.getValueByXPath(locs.get(i)));
        }


        System.out.println("Value of "+xpath1+": "+doc.getValueByXPath(xpath1));

        System.out.println("Set value? "+doc.setValueByXPath(xpath1, "555"));

        System.out.println("Value of "+xpath1+": "+doc.getValueByXPath(xpath1));


    }

}
