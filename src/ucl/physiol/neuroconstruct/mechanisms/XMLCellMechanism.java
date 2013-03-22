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
import javax.xml.parsers.*;

import org.xml.sax.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 * Base class for all XML based Cell Mechanism. Contains (in addition to info
 * in CellMechanism class) a string with the name of the file and a SimpleXMLDocument
 * containing the contents of the xml (once initialised) and a number of mappings to NEURON, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class XMLCellMechanism extends CellMechanism
{
    private String xmlFile = null;
    protected SimpleXMLDocument xmlDoc = null;

    private ArrayList<SimulatorMapping> simMappings = new ArrayList<SimulatorMapping>();


    public XMLCellMechanism()
    {
        logger = new ClassLogger("XMLCellMechanism");
        //logger.setThisClassVerbose(true);
    }


    public boolean isNeuroML2()
    {
        return getMechanismType().indexOf("NeuroML 2")>=0;
    }

    public void initPropsFromPropsFile(File propsFile) throws IOException
    {
        Properties cellMechProps = new Properties();
        cellMechProps.loadFromXML(new FileInputStream(propsFile));
        String xmlFileName = cellMechProps.getProperty(CellMechanismHelper.PROP_CHANNELML_FILE);

        if (xmlFileName ==null || xmlFileName.length()==0)
        {
            xmlFileName = cellMechProps.getProperty(CellMechanismHelper.PROP_SBML_FILE);
        }

        setXMLFile(xmlFileName);
        setInstanceName(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_NAME));
        setMechanismModel(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_MODEL));
        setMechanismType(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_TYPE));
        setDescription(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_DESCRIPTION));

        Enumeration names = cellMechProps.propertyNames();

        while (names.hasMoreElements())
        {
            String nextPropName = (String) names.nextElement();

            if (nextPropName.endsWith(CellMechanismHelper.PROP_MAPPING_SUFFIX))
            {
                String simEnv = nextPropName.substring(0, nextPropName.lastIndexOf(" "));
                boolean requiresCompilation = false;
                String stringReqComp = cellMechProps.getProperty(simEnv + CellMechanismHelper.PROP_NEEDS_COMP_SUFFIX);

                if (stringReqComp != null && stringReqComp.equals("true"))
                {
                    requiresCompilation = true;
                }

                SimulatorMapping map = new SimulatorMapping(cellMechProps.getProperty(nextPropName), simEnv,
                                                      requiresCompilation);

                addSimMapping(map);
            }
        }
    }


    


    public String getXMLFile()
    {
        return xmlFile;
    }



    public File getXMLFile(Project project)
    {
        File cellMechDir = ProjectStructure.getDirForCellMechFiles(project, this.getInstanceName());

        return  new File(cellMechDir, xmlFile);

    }


    public SimpleXMLDocument getXMLDoc() throws XMLMechanismException
    {
        if (xmlFile==null)
            throw new XMLMechanismException("XML file not initialised in XMLCellMechanism");

        if (xmlDoc==null)
            throw new XMLMechanismException("SimpleXMLDocument not yet created in XMLCellMechanism");

        return xmlDoc;
    }


    public void setXMLFile(String xmlFile)
    {
        this.xmlFile = xmlFile;
    }


    @Override
    public abstract String getMechanismType();


    public abstract String getNameXPath();

    public abstract String getDescriptionXPath();



    @Override
    public String getDescription()
    {
        String desc = null;

        if (xmlDoc == null)
        {
            desc = description; // and hope for the best...
        }
        else
        {
            desc = xmlDoc.getValueByXPath(this.getDescriptionXPath());

        }
        return (desc!=null ? desc : "");
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;

        if (this.xmlDoc!=null)
        {
            xmlDoc.setValueByXPath(this.getDescriptionXPath(), description);
        }

    }

    @Override
    public String getInstanceName()
    {
        if (xmlDoc == null)
        {
            return instanceName;
        }
        else
        {
            return xmlDoc.getValueByXPath(this.getNameXPath());
        }
    }


    @Override
    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;

        if (this.xmlDoc!=null)
        {
            xmlDoc.setValueByXPath(this.getNameXPath(), instanceName);
        }
    }

    /**
     * Sets the xmlDoc to null and reinitialises
     */
    public File reset(Project project, boolean save) throws XMLMechanismException
    {
        xmlDoc = null;
        return initialise(project, save);
    }


    /**
     * Loads up the xmlDoc with information from the file specified, using the
     * location based on the project directory
     * @return The actual File used
     */
    public File initialise(Project project, boolean save) throws XMLMechanismException
    {
        //this.initialiseProperties(project);

        String currentInstanceName = getInstanceName();
        if (xmlDoc != null)
        {
            logger.logComment("XML based Mechanism "+currentInstanceName+" has already been initialised");

            File file = this.getXMLFile(project);

            logger.logComment("File for xml: "+file);

            return file;

        }

        logger.logComment("XML Mechanism "+currentInstanceName+" being initialised in project: "+ project.getProjectName());
        FileInputStream instream = null;
        InputSource is = null;
        File fileUsed = null;
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            SimpleXMLReader docBuilder = new SimpleXMLReader();
            xmlReader.setContentHandler(docBuilder);

            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", docBuilder);

            fileUsed = this.getXMLFile(project);

            instream = new FileInputStream(fileUsed);

            is = new InputSource(instream);

            xmlReader.parse(is);

            xmlDoc = docBuilder.getDocRead();

            if (save)
            {
                xmlDoc.setValueByXPath(this.getNameXPath(), currentInstanceName);

                saveCurrentState(project);
            }

        }
        catch (ParserConfigurationException e)
        {
            throw new XMLMechanismException( "Error when parsing XML file: "+xmlFile, e);
        }
        catch (SAXException e)
        {
            throw new XMLMechanismException("Error when parsing XML file: "+xmlFile, e);
        }

        catch (IOException e)
        {
            throw new XMLMechanismException("Error with XML file: "+xmlFile, e);
        }


        return fileUsed;

    }


    public void saveCurrentState(Project project) throws XMLMechanismException
    {
        logger.logComment("Saving the state of the XML CellMechanism "+this.getInstanceName());
        try
        {

            File myFile = this.getXMLFile(project);

            logger.logComment("xmlFile: "+ myFile.getAbsolutePath());

            FileWriter fw = new FileWriter(myFile);

            if (this.xmlDoc == null)
            {
                throw new XMLMechanismException("Error: XML CellMechanism "+this.getInstanceName()+" not yet initialised");
            }

            fw.write(this.xmlDoc.getXMLString("", false));

            fw.close();
        }
        catch (IOException e)
        {
            GuiUtils.showErrorMessage(logger, "Error with XML file: "+xmlFile, e, null);
        }
    }


    /**
     * Create a script file for the specified simulation environment
     */
    public abstract boolean createImplementationFile(String targetEnv,
                                            int unitsSystem,
                                            File fileToGenerate,
                                            Project project,
                                            boolean requiresCompilation,
                                            boolean includeComments,
                                            boolean forceCorrectInit,
                                            boolean parallelMode);




    public void addSimMapping(SimulatorMapping simMapping)
    {
        for (int i = 0; i < simMappings.size(); i++)
        {
            if (simMappings.get(i).getSimEnv().equals(simMapping.getSimEnv()))
            {
                simMappings.remove(i);
            }
        }
        simMappings.add(simMapping);
    }


    public String getValue(String simpleXPathExp)
    {
        if (xmlDoc!=null)
        {
            return xmlDoc.getValueByXPath(simpleXPathExp);
        }
        return null;
    }

    public boolean setValue(String path, String value)
    {
        if (xmlDoc!=null)
        {
            return xmlDoc.setValueByXPath(path, value);
        }
        return false;
    }


    public ArrayList<SimulatorMapping> getSimMappings()
    {
        return this.simMappings;
    }

    public SimulatorMapping getSimMapping(String simEnv)
    {
        for (int i = 0; i < simMappings.size(); i++)
        {
            if (simMappings.get(i).getSimEnv().equals(simEnv))
            {
                return simMappings.get(i);
            }
        }
        return null;

    }


    public void setSimMappings(ArrayList<SimulatorMapping> simMappings)
    {
        this.simMappings = simMappings;
    }



}
