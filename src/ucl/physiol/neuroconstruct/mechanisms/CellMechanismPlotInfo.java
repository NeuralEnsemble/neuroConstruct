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

package ucl.physiol.neuroconstruct.mechanisms;

import java.io.*;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.XMLUtils;

/**
 * Class for handling XML stored info on interesting plots for Cell Mechanism
 *
 * @author Padraig Gleeson
 *  
 */

public class CellMechanismPlotInfo
{
    ClassLogger logger = new ClassLogger("CellMechanismPlotInfo");

    Document plotInfoDoc = null;
    Vector<CellMechanismPlot> parsedCellMechanismPlots = new Vector<CellMechanismPlot>();

    Project project = null;


    private final String PLOT_LIST = "plotlist";
    private final String PLOT = "plot";
    private final String PLOT_NAME = "plotname";
    private final String INDEP_VARIABLE = "independentvariable";
    private final String EXPRESSION = "expression";
    private final String MIN_VALUE = "minvalue";
    private final String MAX_VALUE = "maxvalue";
    private final String PARAMETER = "parameter";
    private final String PARAMETER_NAME = "parametername";
    private final String SYMBOL = "symbol";

    public CellMechanismPlotInfo(Project project,
                               File plotInfoFile) throws CellMechanismException
    {
        //this.plotInfoFile = plotInfoFile;

        this.project = project;

        try
        {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            plotInfoDoc = parser.parse(plotInfoFile);
        }
        catch(ParserConfigurationException pce)
        {
            throw new CellMechanismException("Problem with the XML Parser Configuration. "
                + "\nAre you sure you are using a version of Java after "
                +GeneralProperties.getMinimumVersionJava()+"?", pce);
        }
        catch(SAXException sex)
        {

            throw new CellMechanismException("Problem parsing the XML file: "+plotInfoFile, sex);
        }

        catch(IOException ioex)
        {
            throw new CellMechanismException("Problem reading the XML file: "+plotInfoFile, ioex);
        }

        if (!validate())
        {
            throw new CellMechanismException("Could not validate this XML file against its XSD schema");

        }
        Node mainNode = plotInfoDoc.getFirstChild();

        if (!mainNode.getNodeName().equals(PLOT_LIST))
        {
            throw new CellMechanismException("Did not find expected first XML node: "+ PLOT_LIST);

        }

        NodeList nl = mainNode.getChildNodes();

        Hashtable<String, String> allParameters = new Hashtable<String, String>();
        for (int topNodeIndex = 0; topNodeIndex < nl.getLength(); topNodeIndex++)
        {
            Node node = nl.item(topNodeIndex);
            logger.logComment("Name: " + node.getNodeName());


            if (node.getNodeName().equals(PARAMETER))
            {
                logger.logComment("  -------    Found a parameter");

                NodeList paramNodes = node.getChildNodes();

                String name = null;
                String symbol = null;
                for (int paramNodeIndex = 0; paramNodeIndex < paramNodes.getLength(); paramNodeIndex++)
                {
                    Node paramNode = paramNodes.item(paramNodeIndex);

                 //   logger.logComment("Name: " + paramNode.getNodeName()
                  //             + ", Value: " + paramNode.getNodeValue()
                  //             + ", getTextContent: " + paramNode.getTextContent());


                    if (paramNode.getNodeName().equals(PARAMETER_NAME))
                    {
                        name = paramNode.getTextContent();
                    }
                    if (paramNode.getNodeName().equals(SYMBOL))
                    {
                        symbol = paramNode.getTextContent();
                    }

                }

                allParameters.put(symbol, name);

                logger.logComment("All params so far: "+ allParameters);
            }

            if (node.getNodeName().equals(PLOT))
            {
                CellMechanismPlot plot = new CellMechanismPlot();

                logger.logComment("    -----------     Found a plot!!!");

                NodeList plotNodes = node.getChildNodes();

                for (int childNodeIndex = 0; childNodeIndex < plotNodes.getLength(); childNodeIndex++)
                {
                    Node childNode = plotNodes.item(childNodeIndex);

                    //logger.logComment("Name: " + childNode.getNodeName()
                 //                     + ", Value: " + childNode.getNodeValue()
                 //                     + ", getTextContent: " + childNode.getTextContent());

                    String nodeName = childNode.getNodeName();

                    logger.logComment("Looking at: " + nodeName);

                    if (nodeName.equals(PLOT_NAME))
                    {
                        plot.plotName = childNode.getTextContent();
                    }
                    if (nodeName.equals(INDEP_VARIABLE))
                    {
                        plot.independentVariable = childNode.getTextContent();
                    }
                    if (nodeName.equals(EXPRESSION))
                    {
                        plot.expression = childNode.getTextContent();
                    }

                    if (nodeName.equals(MIN_VALUE))
                    {
                        plot.minValue = Float.parseFloat( childNode.getTextContent());
                    }
                    if (nodeName.equals(MAX_VALUE))
                    {
                        plot.maxValue = Float.parseFloat( childNode.getTextContent());
                    }

                }
                plot.parameters = allParameters;

                logger.logComment("Created: "+ plot);
                logger.logComment("parameters: "+ plot.parameters);

                parsedCellMechanismPlots.add(plot);
            }

        }




        logger.logComment("Finished creating the CellMechanismPlotInfo.");
        logger.logComment("Contents: "+parsedCellMechanismPlots);


    }


    public CellMechanismPlot[] getPlots()
    {
        CellMechanismPlot[] plots = new CellMechanismPlot[parsedCellMechanismPlots.size()];
        for (int i = 0; i < plots.length; i++)
        {
            plots[i] = (CellMechanismPlot)parsedCellMechanismPlots.elementAt(i);
        }
        return plots;
    }


    public boolean validate()
    {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        File xsdFile = XMLUtils.getSchemaFile(plotInfoDoc);
        if (xsdFile == null)
        {
            logger.logError("No Schema doc found in XML file");
            return false;
        }

        if (!xsdFile.exists())
        {
            xsdFile = new File(ProjectStructure.getXMLTemplatesDir(), xsdFile.getName());

            if (!xsdFile.exists())
            {
                GuiUtils.showErrorMessage(logger,
                                          "File: " + xsdFile + " specified as the XML Schema document, doesn't exist", null, null);
                return false;
            }
        }

        logger.logComment("Found the XSD file: "+ xsdFile);


        try
        {
            Source schemaFile = new StreamSource(xsdFile);
            Schema schema = factory.newSchema(schemaFile);

            Validator validator = schema.newValidator();


            validator.validate(new DOMSource(plotInfoDoc));
            return true;
        }
        catch (IOException e)
        {
            logger.logError("IOException with file", e);
            return false;
        }
        catch (SAXException e)
        {
            //e.
            GuiUtils.showErrorMessage(logger, "Document is invalid", e, null);
            return false;
        }



    }



    public static void main(String[] args)
    {

        try
        {
            Project testProj = Project.loadProject(new File("projects/Simple/Simple.neuro.xml"),
                                               new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });

            File d2plots = new File("templates/xmlTemplates/DoubExpSynPlots.xml");
            new CellMechanismPlotInfo(testProj, d2plots);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

 

}
