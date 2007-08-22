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

package ucl.physiol.neuroconstruct.mechanisms;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.XMLUtils;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 * Base class for all ChannelML based Cell Mechanism. Contains (in addition to info
 * in CellMechanism class) a string with the name of the file and a SimpleXMLDocument
 * containing the contents of the xml (once initialised) and a number of SimXSLMapping to NEURON, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public class ChannelMLCellMechanism extends CellMechanism
{
    private static ClassLogger logger = new ClassLogger("ChannelMLCellMechanism");

    private String channelMLFile = null;
    private SimpleXMLDocument xmlDoc = null;

    private ArrayList<SimXSLMapping> simMappings = new ArrayList<SimXSLMapping>();

    //File templateDir = null;

    public ChannelMLCellMechanism()
    {
        logger.setThisClassSilent(true);
    }

    public void initPropsFromPropsFile(File propsFile) throws IOException
    {
        Properties cellMechProps = new Properties();
        cellMechProps.loadFromXML(new FileInputStream(propsFile));
        setChannelMLFile(cellMechProps.getProperty(CellMechanismHelper.PROP_CHANNELML_FILE));
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

                SimXSLMapping map = new SimXSLMapping(cellMechProps.getProperty(nextPropName), simEnv,
                                                      requiresCompilation);

                addSimMapping(map);
            }
        }
    }


    /**
     * To support the templates of common channels included in the neuroConstruct
     * distribution
     */
    public static ChannelMLCellMechanism createFromTemplate(File templateDir, Project project)
    {
        logger.logComment("Trying to create a cml cell mechanism from contents of dir: "+ templateDir);
        ChannelMLCellMechanism cmlMech = new ChannelMLCellMechanism();

        cmlMech.initPropsFromTemplate(templateDir, project);

        return cmlMech;
    }

    protected void initPropsFromTemplate(File templateDir, Project project)
    {
        Properties props = new Properties();

        try
        {
            props.load(new FileInputStream(new File(templateDir, "properties")));

            setInstanceName(props.getProperty("DefaultName"));

            String type = props.getProperty("CellProcessType");
            if (type==null)
            {
                type = props.getProperty("CellMechanismType");
            }

            setMechanismType(type);

            String relativeFile = props.getProperty("ChannelMLFile");

            File absFile = new File(templateDir, relativeFile);

            absFile = absFile.getCanonicalFile();

            logger.logComment("ChannelML file found in props to be: "+ absFile);

            File cellMechDir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory());

            File dirForCMLFiles = new File(cellMechDir, getInstanceName());

            File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

            setChannelMLFile(newFile.getName());

            if (props.getProperty("MappingNEURON")!=null)
            {
                relativeFile = props.getProperty("MappingNEURON");

                absFile = new File(templateDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingNEURON file found in props to be: " + absFile);

                newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimXSLMapping mapping = new SimXSLMapping(newFile.getName(),
                                                          SimEnvHelper.NEURON, true); // true can be reset later

                addSimMapping(mapping);
            }

            if (props.getProperty("MappingGENESIS") != null)
            {
                relativeFile = props.getProperty("MappingGENESIS");

                absFile = new File(templateDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingGENESIS file found in props to be: " + absFile);

                newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimXSLMapping mapping = new SimXSLMapping(newFile.getName(),
                                                          SimEnvHelper.GENESIS, false);

                addSimMapping(mapping);
            }

            if (props.getProperty("NEURONNeedsCompilation") != null)
            {
                Boolean b = Boolean.parseBoolean(props.getProperty("NEURONNeedsCompilation"));
                getSimMapping(SimEnvHelper.NEURON).setRequiresCompilation(b.booleanValue());
            }

            if (props.getProperty("Description") != null)
            {
                setDescription(props.getProperty("Description"));
            }
            else
            {
                setDescription("Template based ChannelML file");
            }
            setMechanismModel("Template based ChannelML file");


        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem getting properties", ex, null);

        }

        logger.logComment("init props cml mech:  "+this);


    }

    public String toString()
    {
        return "ChannelMLCell Mechanism [InstanceName: "+this.getInstanceName()+", channelMLFile: "+channelMLFile+"]";
    }



    public String getChannelMLFile()
    {
        return channelMLFile;
    }






    public File getChannelMLFile(Project project)
    {
        File cellMechDir = ProjectStructure.getDirForCellMechFiles(project, this.getInstanceName());

        return  new File(cellMechDir, channelMLFile);

    }


    public SimpleXMLDocument getXMLDoc() throws ChannelMLException
    {
        if (channelMLFile==null)
            throw new ChannelMLException("ChannelML file not initialised in ChannelMLCellMechanism");

        if (xmlDoc==null)
            throw new ChannelMLException("SimpleXMLDocument not yet created in ChannelMLCellMechanism");

        return xmlDoc;
    }



    public void setChannelMLFile(String channelMLFile)
    {
        this.channelMLFile = channelMLFile;
    }

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

    public void setDescription(String description)
    {
        this.description = description;

        if (this.xmlDoc!=null)
        {
            xmlDoc.setValueByXPath(this.getDescriptionXPath(), description);
        }

    }

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

    private String getNameXPath()
    {
        if (this.isChannelMechanism())
        {
            return ChannelMLConstants.getChannelNameXPath();
        }
        else if (this.isSynapticMechanism())
        {
            return ChannelMLConstants.getSynapseNameXPath();
        }
        else if (this.isIonConcMechanism())
        {
            return ChannelMLConstants.getIonConcNameXPath();
        }


        return null;
    }

    private String getDescriptionXPath()
    {
        if (this.isChannelMechanism())
        {
            return ChannelMLConstants.getFirstChannelNotesXPath();
        }
        else if (this.isSynapticMechanism())
        {
            return ChannelMLConstants.getFirstSynapseNotesXPath();
        }
        else if (this.isIonConcMechanism())
        {
            return ChannelMLConstants.getFirstIonConcNotesXPath();
        }


        return null;
    }



    public boolean isChannelMechanism()
    {
        return getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM);
    }



    public boolean isSynapticMechanism()
    {
        return getMechanismType().equals(CellMechanism.SYNAPTIC_MECHANISM);
    }

    public boolean isIonConcMechanism()
    {
        return getMechanismType().equals(CellMechanism.ION_CONCENTRATION);
    }



    public String getMechanismType()
    {
        // check internal cml setting first..
        if (xmlDoc!=null)
        {
            if (xmlDoc.getValueByXPath(ChannelMLConstants.getChannelTypeXPath()) != null)
            {
                mechanismType = CellMechanism.CHANNEL_MECHANISM;
                return mechanismType;
            }
            else if (xmlDoc.getValueByXPath(ChannelMLConstants.getSynapseTypeXPath()) != null)
            {
                mechanismType = CellMechanism.SYNAPTIC_MECHANISM;
                return mechanismType;
            }
            else if (xmlDoc.getValueByXPath(ChannelMLConstants.getIonConcTypeXPath()) != null)
            {
                mechanismType = CellMechanism.ION_CONCENTRATION;
                return mechanismType;
            }
        }
        return mechanismType;
    }




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
    public File reset(Project project, boolean save) throws ChannelMLException
    {
        xmlDoc = null;
        return initialise(project, save);
    }


    /**
     * Loads up the xmlDoc with information from the file specified, using the
     * location based on the project directory
     * @return The actual File used
     */
    public File initialise(Project project, boolean save) throws ChannelMLException
    {
        //this.initialiseProperties(project);

        String currentInstanceName = getInstanceName();
        if (xmlDoc != null)
        {
            logger.logComment("ChannelML Mechanism "+currentInstanceName+" has already been initialised");

            File file = this.getChannelMLFile(project);

            logger.logComment("File for cml proc: "+file);

            return file;

        }
        logger.logComment("ChannelML Mechanism "+currentInstanceName+" being initialised in project: "+ project.getProjectName());
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

            fileUsed = this.getChannelMLFile(project);

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
            throw new ChannelMLException( "Error when parsing XML file: "+channelMLFile, e);
        }
        catch (SAXException e)
        {
            throw new ChannelMLException("Error when parsing XML file: "+channelMLFile, e);
        }

        catch (IOException e)
        {
            throw new ChannelMLException("Error with XML file: "+channelMLFile, e);
        }

        catch (ChannelMLException e)
        {
            throw new ChannelMLException("Error with XML file: "+channelMLFile, e);
        }

        return fileUsed;

    }


    public void saveCurrentState(Project project) throws ChannelMLException
    {
        logger.logComment("Saving the state of the ChannelMLCellMechanism "+this.getInstanceName());
        try
        {

            File myFile = this.getChannelMLFile(project);

            logger.logComment("Channelml file: "+ myFile.getAbsolutePath());

            FileWriter fw = new FileWriter(myFile);

            if (this.xmlDoc == null)
            {
                throw new ChannelMLException("Error: ChannelMLCellMechanism "+this.getInstanceName()+" not yet initialised");
            }

            fw.write(this.xmlDoc.getXMLString("", false));

            fw.close();
        }
        catch (IOException e)
        {
            GuiUtils.showErrorMessage(logger, "Error with XML file: "+channelMLFile, e, null);
        }
    }


    /**
     * Create a script file for the specified simulation environment
     */
    public boolean createImplementationFile(String targetEnv,
                                            int unitsSystem,
                                            File fileToGenerate,
                                            Project project,
                                            boolean requiresCompilation,
                                            boolean includeComments)
    {
        logger.logComment("Creating file for env: "+targetEnv+" file: "+ fileToGenerate);

        for (int k = 0; k < simMappings.size(); k++)
        {
            if(simMappings.get(k).getSimEnv().equals(targetEnv))
            {
                logger.logComment("Found suitable sim env");


                    try
                    {
                        FileWriter fileOut = new FileWriter(fileToGenerate);
                        //FileDescriptor f;

                        //System.out.println("Encoding, "+fileToGenerate+": "+ fileOut.getEncoding());

                        String commentBlockStart = null;
                        String commentBlockEnd = null;
                        String commentLinePrefix = null;

                        if (targetEnv.equals(SimEnvHelper.GENESIS) ||
                            (targetEnv.equals(SimEnvHelper.NEURON) &&
                             !requiresCompilation))
                        {
                            commentBlockStart = "";
                            commentBlockEnd = "";
                            commentLinePrefix = "// ";
                        }
                        else if (targetEnv.equals(SimEnvHelper.NEURON))
                        {
                            commentBlockStart = "COMMENT";
                            commentBlockEnd = "ENDCOMMENT";
                            commentLinePrefix = "   ";
                        }

                        File cmlFile = this.getChannelMLFile(project);

                        File xslFile = new File(cmlFile.getParentFile(), simMappings.get(k).getXslFile());

                        fileOut.write(commentBlockStart + "\n\n");
                        fileOut.write(commentLinePrefix + "**************************************************\n");
                        fileOut.write(commentLinePrefix + "File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+" \n");
                        fileOut.write(commentLinePrefix + "**************************************************\n\n");


                        if (includeComments)
                        {

                            fileOut.write(commentLinePrefix + "This file holds the implementation in " + targetEnv + " of the Cell Mechanism:\n");
                            fileOut.write(commentLinePrefix + this.getInstanceName()
                                          + " (Type: " + this.getMechanismType()
                                          + ", Model: " + this.getMechanismModel() + ")\n\n");
                            fileOut.write(commentLinePrefix + "with parameters: \n");

                            if (xmlDoc == null)
                            {
                                try
                                {
                                    this.initialise(project, false);
                                }
                                catch (ChannelMLException ex1)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "Error creating implementation of Cell Mechanism: " +
                                                              this.getInstanceName(),
                                                              ex1,
                                                              null);

                                    return false;

                                }
                            }
                            ArrayList<String> xpathLocs = xmlDoc.getXPathLocations(true);

                            for (int j = 0; j < xpathLocs.size(); j++)
                            {
                                String name = xpathLocs.get(j);
                                if (name.indexOf("schemaLocation")<0)
                                {
                                    String safeValue = GeneralUtils.replaceAllTokens(xmlDoc.getValueByXPath(name),
                                        "\n", " ");

                                    fileOut.write(commentLinePrefix + name
                                                  + " = "
                                                  + safeValue
                                                  + " \n");
                                }
                            }


                            fileOut.write("\n// File from which this was generated: " + cmlFile.getAbsolutePath() + "\n");
                            fileOut.write("\n// XSL file with mapping to simulator: " + xslFile.getAbsolutePath() + "\n");
                        }

                        fileOut.write("\n"+commentBlockEnd+"\n");


                        fileOut.write("\n");

                        String transformed = null;

                        if (targetEnv.equals(SimEnvHelper.GENESIS))
                        {
                            logger.logComment("   -----   xslFile: "+xslFile.getAbsolutePath());

                            SimpleXMLDocument xslDoc = SimpleXMLReader.getSimpleXMLDoc(xslFile);

                            SimpleXMLEntity[] variables = xslDoc.getXMLEntities(ChannelMLConstants.XSL_TARGET_UNITS_ELEMENT);

                            SimpleXMLContent unitsVariable = null;

                            for (int j = 0; j < variables.length; j++)
                            {
                                logger.logComment("Checking variable: "+ variables[j].getXMLString("", false));

                                if (variables[j] instanceof SimpleXMLElement)
                                {
                                    SimpleXMLElement var = (SimpleXMLElement) variables[j];

                                    logger.logComment("..Found the element for the units: ("+ var+") in xslFile: "+xslFile.getAbsolutePath());
                                    for (int l = 0; l < variables.length; l++)
                                    {
                                        logger.logComment("Checking content "+l+": ("+var.getContents().get(l)+")");

                                        if (var.getContents().get(l) instanceof SimpleXMLContent)
                                            unitsVariable = (SimpleXMLContent) var.getContents().get(l);
                                    }
                                }
                            }

                            String unitsXPath = ChannelMLConstants.getUnitsXPath();

                            if (unitsSystem == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                            {
                                unitsVariable.setText(ChannelMLConstants.PHYSIOLOGICAL_UNITS);
                            }
                            else if (unitsSystem == UnitConverter.GENESIS_SI_UNITS)
                            {
                                unitsVariable.setText(ChannelMLConstants.SI_UNITS);
                            }

                            //System.out.println("Unit system: "+ unitsVariable.getText());

                            //System.out.println("XSL file:\n"+xslDoc.getXMLString("", false));


                            transformed = XMLUtils.transform(cmlFile, xslDoc.getXMLString("", false));

                        }
                        else
                        {
                            transformed = XMLUtils.transform(cmlFile, xslFile);

                        }

                        transformed = GeneralUtils.replaceAllTokens(transformed, "\r\n", "\n");
                        //transformed = GeneralUtils.replaceAllTokens(transformed, "\n\n", "\n");

                        fileOut.write(transformed);

                        fileOut.close();


                    return true;
                }
                catch (IOException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Error creating implementation of Cell Mechanism: "+this.getInstanceName(),
                                              ex,
                                              null);
                    return false;
                }
                catch (SAXException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Error creating implementation of Cell Mechanism: "+this.getInstanceName(),
                                              ex,
                                              null);
                    return false;
                }
                catch (ParserConfigurationException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Error creating implementation of Cell Mechanism: "+this.getInstanceName(),
                                              ex,
                                              null);
                    return false;
                }
            }
        }
        return false;
    }


    public void addSimMapping(SimXSLMapping simMapping)
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

    public String getUnitsUsedInFile()
    {
        if (xmlDoc!=null)
        {
            return xmlDoc.getValueByXPath(ChannelMLConstants.getUnitsXPath());
        }
        return null;
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


    public ArrayList<SimXSLMapping> getSimMappings()
    {
        return this.simMappings;
    }

    public SimXSLMapping getSimMapping(String simEnv)
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


    public void setSimMappings(ArrayList simMappings)
    {
        this.simMappings = simMappings;
    }

    /**
     * Needed as GENESIS treats the first passive, non specific conductance on a compartment
     * differently (adjusts the memb resistance)
     */
    public boolean isPassiveNonSpecificCond() throws CMLMechNotInitException
    {
        if (xmlDoc==null)
        {
            logger.logComment("Cannot determine isPassiveConductance()");
            throw new CMLMechNotInitException();
        }
        String xpath = ChannelMLConstants.getChannelTypeXPath();
        logger.logComment("Checking xpath: "+xpath);
        SimpleXMLEntity[] chanType = xmlDoc.getXMLEntities(xpath);

        if (chanType==null || chanType.length==0) return false;

        xpath = ChannelMLConstants.getGateXPath(1);
        logger.logComment("Checking xpath: "+xpath);

        SimpleXMLEntity[] gates = xmlDoc.getXMLEntities(xpath);


        if (gates!=null)
        {
            for (int i = 0; i < gates.length; i++)
            {
                logger.logComment("Got entity: " + gates[i]);
            }
        }

        boolean gatesAbsent = (gates==null || gates.length==0);

        boolean nonSpecific = false;

        String ionXpath = ChannelMLConstants.getIonsXPath();

        logger.logComment("Checking xpath: " + xpath);

        SimpleXMLEntity[] ions = xmlDoc.getXMLEntities(ionXpath);

        if (ions != null)
        {
            for (int i = 0; i < ions.length; i++)
            {
                logger.logComment("Got entity: " + ions[i].getXMLString("", false));

                if ( ( (SimpleXMLElement) ions[i]).getAttributeValue(ChannelMLConstants.ION_NAME_ATTR).equals(ChannelMLConstants.NON_SPECIFIC_ION_NAME))
                {
                    nonSpecific = true;
                }
            }
        }

        logger.logComment("isPassiveNonSpecificCond(): "+(gatesAbsent && nonSpecific));

        return (gatesAbsent && nonSpecific);
    }




    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("projects/KStest/KStest.neuro.xml"),
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

            ChannelMLCellMechanism cmlProc = (ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("KConductance");

            cmlProc.initialise(testProj, false);

            System.out.println("Is this passive: "+ cmlProc.isPassiveNonSpecificCond());




            /*



            logger.logComment("contents: "+cmlProc.getXMLDoc().getXMLString("", false));

            String gmaxXpath = "//@default_gmax";
            String eXpath = "channelml/channel_type/current_voltage_relation/ohmic/@default_erev";

            //cmlProc.setValue(gmaxXpath, "1000");
            //cmlProc.setValue(eXpath, "-10");


            logger.logComment("Value of " + gmaxXpath + ": " + cmlProc.getValue(gmaxXpath));
            //logger.logComment("Value of " + eXpath + ": " + cmlProc.getValue(eXpath));

            SimXSLMapping map = (SimXSLMapping)cmlProc.getSimMappings().get(0);

            logger.logComment("mapping for: "+map.getSimEnv()+", file: "+ map.getXslFile());
*/
           // map.

           File tempFile = new File("../temp/temp.txt");


           boolean success = cmlProc.createImplementationFile("GENESIS", UnitConverter.GENESIS_SI_UNITS,tempFile, testProj, false, false);

           System.out.println("Created file: "+tempFile.getAbsolutePath()+": "+ success);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }

}
