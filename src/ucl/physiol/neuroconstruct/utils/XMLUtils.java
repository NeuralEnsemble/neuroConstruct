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

package ucl.physiol.neuroconstruct.utils;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

/**
 * Utilities for dealing with XML files
 *
 * @author Padraig Gleeson
 *  
 */


public class XMLUtils
{

    static ClassLogger logger = new ClassLogger("XMLUtils");

    public static String XML_STYLESHEET_NODE = "xml-stylesheet";
    public static String XML_SCHEMA_LOC_ATTR = "xsi:schemaLocation";

    public XMLUtils()
    {
    }


    public static boolean transform(File origXmlFileOrDir,
                                    File xslFile,
                                    File targetDir,
                                    String extension)
    {
        logger.logComment("Going to transform " + origXmlFileOrDir
                          + " into dir " + targetDir+" using: "+ xslFile, true);
        

        if (!origXmlFileOrDir.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XML file/directory: " + origXmlFileOrDir + " doesn't exist", null, null);
            return false;
        }

        if (!xslFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XSL file: " + xslFile + " doesn't exist", null, null);
            return false;
        }

        if (!targetDir.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, target directory: " + targetDir + " doesn't exist", null, null);
            return false;
        }

        if (origXmlFileOrDir.isDirectory())
        {
            logger.logComment("That file is a directory. Converting all of the XML files in it");
            File[] files = origXmlFileOrDir.listFiles();

            boolean totalSuccess = true;
            for (int i = 0; i < files.length; i++)
            {
                 if (!files[i].isDirectory()
                     && (files[i].getName().endsWith(".xml") ||
                         files[i].getName().endsWith(".XML")))
                 {
                     boolean partialSuccess = transform(files[i],
                                                        xslFile,
                                                        targetDir,
                                                        extension);

                     totalSuccess = totalSuccess || partialSuccess;
                 }
                 else if (files[i].isDirectory() && !GeneralUtils.isVersionControlDir(files[i]))
                 {
                     File newFolder = new File(targetDir, files[i].getName());
                     newFolder.mkdir();

                     logger.logComment("Found a sub folder. Going to convert all there into: "+newFolder+"...");

                     transform(files[i],
                                    xslFile,
                                    newFolder,
                                    extension);

                 }

            }
            return totalSuccess;
        }

        String result = transform(origXmlFileOrDir, xslFile);



        String newName = origXmlFileOrDir.getName();

        if (newName.endsWith(".xml") || newName.endsWith(".XML"))
        {
            newName = newName.substring(0, newName.length() - 4) + extension;
        }
        File targetFile = new File(targetDir, newName);

        try
        {
            FileWriter fw = new FileWriter(targetFile);
            fw.write(result);
            fw.close();
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Exception writing to file: "+ targetFile, ex, null);
            return false;
        }


        logger.logComment("The result is in " + targetFile + " *************");

        return result!=null;


    }

    /**
     * Transforms the given XML file using the specified XSL file.
     * @param origXmlFile The file containg the XML to transform
     * @param xslFile The XML Stylesheet conntaining the transform instructions
     * @return String representation of the transformation
     */
    public static String transform(File origXmlFile,
                                    File xslFile)
    {
        if (!origXmlFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XML file: " + origXmlFile + " doesn't exist", null, null);
            return null;
        }

        if (!xslFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XSL file: " + xslFile + " doesn't exist", null, null);
            return null;
        }

        try
        {
            logger.logComment("The xslFile is " + xslFile + " *************");

            TransformerFactory tFactory = TransformerFactory.newInstance();

            StreamSource xslFileSource = new StreamSource(xslFile);

            Transformer transformer = tFactory.newTransformer(xslFileSource);

            StringWriter writer = new StringWriter();

            transformer.transform(new StreamSource(origXmlFile),
                                  new StreamResult(writer));

            return writer.toString();
        }
        catch (Exception e)
        {
            GuiUtils.showErrorMessage(logger, "Error when loading the XML file: " + origXmlFile, e, null);
            return null;
        }
    }

    /**
     * Transforms the given XML file using the specified XSL file.
     * @param origXmlFile The file containg the XML to transform
     * @param xslString The XML Stylesheet conntaining the transform instructions
     * @return String representation of the transformation
     */
    public static String transform(File origXmlFile,
                                    String xslString)
    {
        if (!origXmlFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XML file: " + origXmlFile + " doesn't exist", null, null);
            return null;
        }

        try
        {

            TransformerFactory tFactory = TransformerFactory.newInstance();

            StreamSource xslFileSource = new StreamSource(new StringReader(xslString));

            Transformer transformer = tFactory.newTransformer(xslFileSource);

            StringWriter writer = new StringWriter();

            transformer.transform(new StreamSource(origXmlFile),
                                  new StreamResult(writer));

            return writer.toString();
        }
        catch (Exception e)
        {
            GuiUtils.showErrorMessage(logger, "Error when loading the XML file: " + origXmlFile, e, null);
            return null;
        }
    }



    /**
     * Transforms the given XML string using the specified XSL file.
     * @param xmlString The String containg the XML to transform
     * @param xslFile The XML Stylesheet conntaining the transform instructions
     * @return String representation of the transformation
     */
    public static String transform(String xmlString,
                                    File xslFile)
    {

        if (!xslFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Warning, XSL file: " + xslFile + " doesn't exist", null, null);
            return null;
        }

        String shortString = new String(xmlString);
        if (shortString.length()>100) shortString = shortString.substring(0,100) + "...";


        try
        {
            logger.logComment("The xslFile is " + xslFile.getAbsolutePath() + " *************");

            TransformerFactory tFactory = TransformerFactory.newInstance();

            logger.logComment("Transforming string: "+ shortString);

            StreamSource xslFileSource = new StreamSource(xslFile);

            Transformer transformer = tFactory.newTransformer(xslFileSource);

            StringWriter writer = new StringWriter();

            transformer.transform(new StreamSource(new StringReader(xmlString)),
                                  new StreamResult(writer));


            String shortResult = writer.toString();
            if (shortResult.length()>100) shortResult = shortResult.substring(0,100) + "...";

            logger.logComment("Result: "+ shortResult);

            return writer.toString();
        }
        catch (TransformerException e)
        {
            GuiUtils.showErrorMessage(logger, "Error when transforming the XML: " + shortString, e, null);
            return null;
        }
    }

    /**
     * Transforms the given XML string using the XSL string.
     * @param xmlString The String containg the XML to transform
     * @param xslString The XML Stylesheet String containing the transform instructions
     * @return String representation of the transformation
     */
    public static String transform(String xmlString,
                                   String xslString)
    {


        String shortString = new String(xmlString);
        if (shortString.length()>100) shortString = shortString.substring(0,100) + "...";


        try
        {

            TransformerFactory tFactory = TransformerFactory.newInstance();

            logger.logComment("Transforming string: "+ shortString);

            StreamSource xslFileSource = new StreamSource(new StringReader(xslString));

            Transformer transformer = tFactory.newTransformer(xslFileSource);

            StringWriter writer = new StringWriter();

            transformer.transform(new StreamSource(new StringReader(xmlString)),
                                  new StreamResult(writer));


            String shortResult = writer.toString();
            if (shortResult.length()>100) shortResult = shortResult.substring(0,100) + "...";

            logger.logComment("Result: "+ shortResult);

            return writer.toString();
        }
        catch (TransformerException e)
        {
            GuiUtils.showErrorMessage(logger, "Error when transforming the XML: " + shortString, e, null);
            return null;
        }
    }





    // Gets the specified XML Schema doc if one is mentioned in the file
    public static File getSchemaFile(File xmlDoc)
    {
        /** @todo Must be an easier way of doing this... */

        logger.logComment("Getting schema file for: "+ xmlDoc);
        try
        {
            //File xslFile = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(xmlDoc);

            return getSchemaFile(doc);
        }
        catch (Exception e)
        {
            GuiUtils.showErrorMessage(logger, "Error when looking at the XML file: " + xmlDoc, e, null);
            return null;
        }
    }


    // Gets the specified XML Schema doc if one is mentioned in the file
    public static File getSchemaFile(Document xmlDoc)
    {
        /** @todo Must be an easier way of doing this... */

        logger.logComment("Getting schema file for: "+ xmlDoc.getDocumentURI());

        NodeList nl = xmlDoc.getChildNodes();
        for (int j = 0; j < nl.getLength(); j++)
        {
            Node node = nl.item(j);
            logger.logComment("Type: " + node.getNodeType()+"Name: " + node.getNodeName());
            if (node.getNodeName().equals(XML_STYLESHEET_NODE))
            {
                String nodeVal = node.getNodeValue();

                logger.logComment("Looking at: " + nodeVal);
                String xslFileName = nodeVal.substring(nodeVal.indexOf("href=\"") + 6, nodeVal.length() - 1);
                File xslFile = new File(xslFileName);
                return  xslFile;

            }

            if (node.getAttributes().getLength()>0)
            {
                logger.logComment("Attributes: "+ node.getAttributes());
                if (node.getAttributes().getNamedItem(XML_SCHEMA_LOC_ATTR)!=null)
                {
                    String locString = node.getAttributes().getNamedItem(XML_SCHEMA_LOC_ATTR).getNodeValue();
                    logger.logComment("Loc string: "+ locString);
                    String file = locString.split("\\s")[1];
                    return new File(file);
                }
            }
        }
        logger.logError("No node found with name: "+ XML_STYLESHEET_NODE);
        return null;
    }




    /**
     * This main function is used for the ant task helpdocs.
     * Don't change for testing!!
     */
    public static void main(String[] args)
    {
        if (args.length!=4)
        {
            System.out.println("Usage: java ucl.physiol.neuroconstruct.utils.XMLUtils originalXMLFile xslFile targetFile\n");
            System.out.println("with: ");
            System.out.println("   originalXMLFile      the original XML file to be transformed");
            System.out.println("                        (Note: if it's a directory will generate all xml files in dir)");
            System.out.println("   xslFile              the XSL file used to generate the file(s)");
            System.out.println("   targetDir            the target directory for the XML/HTML/etc.");
            System.out.println("   extension            filename extension for the new files (.xml, .html, etc.)\n");
            return;
        }

        File origFile = new File (args[0]);
        File xslFile = new File (args[1]);
        File targetFile = new File (args[2]);
        String extension = args[3];
/*
        File origFile = new File ("docs/XML/xmlForHtml/docs");
        File xslFile = new File ("docs/XML/helpViewer/helpdocs.xsl");
        File targetFile = new File ("../temp/tmm");
        String extension = ".html";
*/


        logger.logComment("Result: "+  XMLUtils.transform(origFile, xslFile, targetFile, extension));
    }
}
