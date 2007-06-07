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

package ucl.physiol.neuroconstruct.utils;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import ucl.physiol.neuroconstruct.project.*;
import javax.swing.*;

/**
 * Reads tool tips from a file and allows components to use the entries for their
 * tool tips
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class ToolTipHelper
{
    ClassLogger logger = new ClassLogger("ToolTipHelper");

    //private Properties toolTips = new Properties();
    private Hashtable toolTips = new Hashtable();

    private static ToolTipHelper myInstance = null;

    public static ToolTipHelper getInstance()
    {
        if (myInstance==null)
        {
            myInstance = new ToolTipHelper();

            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        }
        return myInstance;
    }

    private ToolTipHelper()
    {
        String helpFilename = ProjectStructure.getToolTipFile();
        File myFile = new File(helpFilename);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;
        try
        {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce)
        {
            logger.logError("Error parsing tool tips file: "+myFile, pce);
            return;
        }

        Document doc = null;
        try
        {
            doc = db.parse(myFile);
        }
        catch (SAXException se)
        {
            logger.logError("Error parsing tool tips file: "+myFile, se);
            return;
        }
        catch (IOException ioe)
        {
            logger.logError("Error parsing tool tips file: "+myFile, ioe);
            return;
        }

        try
        {

        // Note: would have been better to do this with xsl, but hey...

            for (Node child = doc.getDocumentElement().getFirstChild(); child != null;
                 child = child.getNextSibling())
            {
                //logger.logComment("Looking at node: " + child.getNodeName());
                if (child.getNodeName().equals("term"))
                {
                    NodeList nl = child.getChildNodes();

                    String name = null;
                    String defined = null;

                    for (int i = 0; i < nl.getLength(); i++)
                    {
                        if (nl.item(i).getNodeName().equals("name"))
                        {
                            name = nl.item(i).getFirstChild().getNodeValue();
                        }

                        if (nl.item(i).getNodeName().equals("defined"))
                        {
                            StringBuffer sb = new StringBuffer();

                            for (Node definedChild = nl.item(i).getFirstChild();
                                 definedChild != null;
                                 definedChild = definedChild.getNextSibling())
                            {
                                if (definedChild.getNodeName().equals("#text"))
                                {
                                    String contents = definedChild.getNodeValue();
                                    // trim carridge returns but not spaces...
                                    while (contents.endsWith("\n")) contents = contents.substring(0, contents.length()-1);
                                    while (contents.startsWith("\n")) contents = contents.substring(1, contents.length());

                                    if (contents.trim().length() > 0)
                                    {
                                        sb.append(contents);
                                    }
                                }
                                else if (definedChild.getNodeName().equals("a")) // for hyperlinks
                                {
                                    sb.append(definedChild.getFirstChild().getNodeValue());
                                    String hyperlink = definedChild.getAttributes().getNamedItem("href").getNodeValue();
                                    //logger.logComment("Hyperlink found: "+ hyperlink);
                                    if (hyperlink.indexOf("#")<0) // i.e. not an internal reference...
                                    {
                                        sb.append(" ("+ hyperlink +")");
                                    }
                                }
                                else if (definedChild.getNodeName().equals("intref")) // for internal hyperlinks
                                {
                                    sb.append(definedChild.getFirstChild().getNodeValue());
                                    // ignore...
                                }

                                else if (definedChild.getNodeName().equals("i")) // for internal hyperlinks
                                {
                                    sb.append(definedChild.getFirstChild().getNodeValue());
                                    // ignore...
                                }
                                else if (definedChild.getNodeName().equals("b")) // for internal hyperlinks
                                {
                                    sb.append("<b>"+definedChild.getFirstChild().getNodeValue()+"</b>");
                                    // ignore...
                                }



                                else if (definedChild.getNodeName().equals("li")) // for <li> elements
                                {
                                    printNodeInfo("li: ", definedChild);
                                    if (sb.charAt(sb.length()-1)!='\n') sb.append("\n");
                                    sb.append("- " + definedChild.getFirstChild().getNodeValue() + "\n");
                                }
                                else if (definedChild.getNodeName().equals("ul")) // for <ul> elements
                                {
                                    if (sb.charAt(sb.length() - 1) != '\n') sb.append("\n");
                                    NodeList nlUL = definedChild.getChildNodes();

                                    //printNodeInfo("-----------    definedChild", definedChild);

                                    for (Node definedChildUL = nlUL.item(0);
                                         definedChildUL != null;
                                         definedChildUL = definedChildUL.getNextSibling())
                                    {
                                       // printNodeInfo("definedChildUL",definedChildUL);
                                        if (definedChildUL.getNodeName().equals("li"))
                                        {
                                            if (sb.charAt(sb.length() - 1) != '\n') sb.append("\n");

                                            NodeList nlLI = definedChildUL.getChildNodes();

                                            sb.append("- ");

                                            for (Node definedChildLI = nlLI.item(0);
                                                 definedChildLI != null;
                                                 definedChildLI = definedChildLI.getNextSibling())
                                            {
                                                //printNodeInfo("definedChildLI",definedChildLI);
                                                if (definedChildLI.getNodeName().equals("intref"))
                                                {

                                                    sb.append( definedChildLI.getFirstChild().getNodeValue() );
                                                }
                                                else if (definedChildLI.getNodeName().equals("b"))
                                                {
                                                    sb.append("<b>");
                                                    sb.append( definedChildLI.getFirstChild().getNodeValue() );
                                                    sb.append("</b>");
                                                }


                                                else
                                                {
                                                    sb.append(" " + definedChildLI.getNodeValue());
                                                }
                                            }
                                            sb.append("\n");
                                        }

                                    }
                                    sb.append("\n");

                                }


                                else if (definedChild.getNodeName().equals("br")) // for hyperlinks
                                {
                                    sb.append("\n");
                                }

                            }
                            defined = sb.toString();
                            // trim off returns..
                            while (defined.endsWith("\n")) defined = defined.substring(0, defined.length()-1);

                        }
                    }

                    if (name != null && defined != null)
                        toolTips.put(name, defined);

                }
                //echo(child);

            }

        }
        catch (NullPointerException ne)
        {
            logger.logError("NullPointerException: ", ne);
        }

    }


    private void printNodeInfo(String ref, Node node)
    {
            logger.logComment(ref+": Node Type: " + node.getNodeType()+", Name: " + node.getNodeName()+", Value: " + node.getNodeValue());
    }

    /**
     * Converts text in Properties file containing \n to HTML based lines for
     * proper display as tool tip text
     */
    public String getToolTip(String name)
    {
        String tip = (String)toolTips.get(name);

        if (tip==null)
        {
            logger.logError("The tool tip for : "+name+" was not found...", null);
            return "- No tip found -";
        }

        if (tip.indexOf("\n")>0)
        {
            String[] lines = tip.split("\\n");
            StringBuffer htmlForm = new StringBuffer("<html>");
            for (int i = 0; i < lines.length; i++)
            {
                htmlForm.append(lines[i]);
                if (i!=lines.length-1)
                    htmlForm.append("<br>");
                else
                    htmlForm.append("</html>");
            }
            return htmlForm.toString();
        }
        else
        {
            return tip;
        }

    }


    public void printContents()
    {
        logger.logComment("Tool tips: " + toolTips);
    }



    public static void main(String[] args)
    {
        ToolTipHelper simpleHelpText1 = ToolTipHelper.getInstance();

        System.out.println("Prop: "+simpleHelpText1.getToolTip("Variables to plot/save"));

        //simpleHelpText1.printContents();

      //  simpleHelpText1.saveToFile();

    }

}
