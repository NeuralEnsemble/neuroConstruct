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
 * containing the contents of the xml (once initialised) and a number of SimulatorMapping to NEURON, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public class ChannelMLCellMechanism extends XMLCellMechanism
{

    public ChannelMLCellMechanism()
    {
        logger = new ClassLogger("ChannelMLCellMechanism");
        //logger.setThisClassVerbose(true);
    }

    /**
     * To support the templates of common channels included in the neuroConstruct
     * distribution
     */
    public static ChannelMLCellMechanism createFromTemplate(File templateDir, Project project) throws ChannelMLException
    {
        logger.logComment("Trying to create a cml cell mechanism from contents of dir: "+ templateDir);
        ChannelMLCellMechanism cmlMech = new ChannelMLCellMechanism();

        cmlMech.initPropsFromTemplate(templateDir, project);

        return cmlMech;
    }

    protected void initPropsFromTemplate(File templateDir, Project project) throws ChannelMLException
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
            
            if (!absFile.exists())
            {
                File readme = new File(ProjectStructure.getXMLTemplatesDir(), "README");
                throw new ChannelMLException("Directory does not exist: "+ absFile.getAbsolutePath()+"\n\n" +
                    "Check that the NeuroML example files and XSL mappings for ChannelML have been downloaded. The latest version of these can\n" +
                    "be obtained from the Sourceforge SVN repository as outlined in "+readme.getAbsolutePath()+"\n\n" +
                    "If you have checked out the neuroConstruct source code from the neuroConstruct SVN repository, you should be able to run\n" +
                    "./updatenC.sh (Linux/Mac) or updatenC.bat (Windows) to update all of the required files from the different repositories \n" +
                    "for a single consistent set of code/examples/NeuroML files, etc.");
            }

            File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

            setXMLFile(newFile.getName());

            if (props.getProperty("MappingNEURON")!=null)
            {
                relativeFile = props.getProperty("MappingNEURON");

                absFile = new File(templateDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingNEURON file found in props to be: " + absFile);

                newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
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

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                          SimEnvHelper.GENESIS, false);

                addSimMapping(mapping);
            }
            if (props.getProperty("MappingPSICS") != null)
            {
                relativeFile = props.getProperty("MappingPSICS");

                absFile = new File(templateDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingPSICS file found in props to be: " + absFile);

                newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                          SimEnvHelper.PSICS, false);

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

    
    @Override
    public String toString()
    {
        return "ChannelMLCell Mechanism [InstanceName: "+this.getInstanceName()+", channelMLFile: "+getXMLFile()+"]";
    }





    public String getNameXPath()
    {
        if (this.isChannelMechanism())
        {
            return ChannelMLConstants.getChannelNameXPath();
        }
        else if (this.isPointProcess())
        {
            return ChannelMLConstants.getChannelNameXPath();
        }
        else if (this.isGapJunctionMechanism())
        {
            return ChannelMLConstants.getSynapseNameXPath();
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

    public String getDescriptionXPath()
    {
        if (this.isChannelMechanism())
        {
            return ChannelMLConstants.getFirstChannelNotesXPath();
        }
        else if (this.isPointProcess())
        {
            return ChannelMLConstants.getFirstChannelNotesXPath();
        }
        else if (this.isGapJunctionMechanism())
        {
            return ChannelMLConstants.getFirstSynapseNotesXPath();
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





    @Override
    public String getMechanismType()
    {
        try
        {
            // check internal cml setting first..
            if (getXMLDoc() != null)
            {
                if (getXMLDoc().getValueByXPath(ChannelMLConstants.getIandFXPath()) != null)
                {
                    mechanismType = CellMechanism.POINT_PROCESS;
                    return mechanismType;
                }
                else if (getXMLDoc().getValueByXPath(ChannelMLConstants.getChannelTypeXPath()) != null)
                {
                    mechanismType = CellMechanism.CHANNEL_MECHANISM;
                    return mechanismType;
                }
                else if (getXMLDoc().getValueByXPath(ChannelMLConstants.getElecSynapseXPath()) != null)
                {
                    mechanismType = CellMechanism.GAP_JUNCTION;
                    return mechanismType;
                }
                else if (getXMLDoc().getValueByXPath(ChannelMLConstants.getSynapseTypeXPath()) != null)
                {
                    mechanismType = CellMechanism.SYNAPTIC_MECHANISM;
                    return mechanismType;
                }
                else if (getXMLDoc().getValueByXPath(ChannelMLConstants.getIonConcTypeXPath()) != null)
                {
                    mechanismType = CellMechanism.ION_CONCENTRATION;
                    return mechanismType;
                }
            }
            return mechanismType;
        }
        catch (XMLMechanismException ex)
        {
            return "Unknown mechanism type!";
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
                                            boolean includeComments,
                                            boolean forceCorrectInit,
                                            boolean parallelMode)
    {
        logger.logComment("Creating file for env: "+targetEnv+" file: "+ fileToGenerate);

        for (int k = 0; k < getSimMappings().size(); k++)
        {
            if(getSimMappings().get(k).getSimEnv().equals(targetEnv))
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
                        else if (targetEnv.equals(SimEnvHelper.PSICS))
                        {
                            commentBlockStart = "<!--";
                            commentBlockEnd = "-->";
                            commentLinePrefix = "   ";
                        }

                        File cmlFile = this.getXMLFile(project);

                        File xslFile = new File(cmlFile.getParentFile(), getSimMappings().get(k).getMappingFile());

                        if (!targetEnv.equals(SimEnvHelper.PSICS)) // temp disabling comments in psics xml
                        {
                            fileOut.write(commentBlockStart + "\n\n");
                            fileOut.write(commentLinePrefix + "**************************************************\n");
                            fileOut.write(commentLinePrefix + "File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+" \n");
                            fileOut.write(commentLinePrefix + "**************************************************\n\n");
                        }


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
                                catch (XMLMechanismException ex1)
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
                                    
                                    if (safeValue.length()>150)
                                        safeValue = safeValue.substring(0,150)+" ...";

                                    fileOut.write(commentLinePrefix + name
                                                  + " = "
                                                  + safeValue
                                                  + " \n");
                                }
                            }


                            fileOut.write("\n// File from which this was generated: " + cmlFile.getAbsolutePath() + "\n");
                            fileOut.write("\n// XSL file with mapping to simulator: " + xslFile.getAbsolutePath() + "\n");
                        }
                        
                        if (!targetEnv.equals(SimEnvHelper.PSICS)) // temp disabling comments in psics xml
                        {
                            fileOut.write("\n"+commentBlockEnd+"\n");

                            fileOut.write("\n");
                        }

                        String transformed = null;

                        if (targetEnv.equals(SimEnvHelper.PSICS))
                        {
                            logger.logComment("   -----   xslFile: "+xslFile.getAbsolutePath());

                            SimpleXMLDocument xslDoc = SimpleXMLReader.getSimpleXMLDoc(xslFile);

                            SimpleXMLEntity[] variables = xslDoc.getXMLEntities(ChannelMLConstants.PSICS_SING_CHAN_COND_ELEMENT);

                            SimpleXMLElement singChanVariableEl = null;

                            for (int j = 0; j < variables.length; j++)
                            {
                                logger.logComment("Checking variable: "+ variables[j].getXMLString("", false));

                                if (variables[j] instanceof SimpleXMLElement)
                                {
                                    SimpleXMLElement var = (SimpleXMLElement) variables[j];
                                        logger.logComment("In element: ("+ var+")");

                                    String name = var.getAttributeValue("name");
                                    if (name !=null && name.equals(ChannelMLConstants.PSICS_SING_CHAN_COND_NAME))
                                    {
                                        logger.logComment("..Found the correct element: ("+ var+") in xslFile: "+xslFile.getAbsolutePath());
                                        singChanVariableEl = var;
                                    }
                                 
                                }
                            }
                            if (singChanVariableEl!=null)
                            {
                                singChanVariableEl.removeAllContents();
                                singChanVariableEl.addContent(project.psicsSettings.getSingleChannelCond()*1e9+"");// Converting mS to pS
                                logger.logComment("Current variable: "+ singChanVariableEl.getXMLString("", false));
                            }

                            transformed = XMLUtils.transform(cmlFile, xslDoc.getXMLString("", false));

                        }
                        else if (targetEnv.equals(SimEnvHelper.GENESIS))
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

                            if (unitsSystem == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                                unitsVariable.setText(ChannelMLConstants.PHYSIOLOGICAL_UNITS);
                            else if (unitsSystem == UnitConverter.GENESIS_SI_UNITS)
                                unitsVariable.setText(ChannelMLConstants.SI_UNITS);

                            transformed = XMLUtils.transform(cmlFile, xslDoc.getXMLString("", false));

                        }
                        else if (targetEnv.equals(SimEnvHelper.NEURON))
                        {
                            logger.logComment("   -----   xslFile: "+xslFile.getAbsolutePath());

                            SimpleXMLDocument xslDoc = SimpleXMLReader.getSimpleXMLDoc(xslFile);

                            SimpleXMLEntity[] variables = xslDoc.getXMLEntities(ChannelMLConstants.FORCE_INIT_ELEMENT);

                            //SimpleXMLContent unitsVariable = null;

                            for (int j = 0; j < variables.length; j++)
                            {
                                logger.logComment("Checking variable: ("+ variables[j].getXMLString("", false)+")");

                                if (variables[j] instanceof SimpleXMLElement)
                                {
                                    SimpleXMLElement var = (SimpleXMLElement) variables[j];
                                    
                                    logger.logComment("Name of variable: "+ var.getAttributeValue(ChannelMLConstants.VARIABLE_NAME_ATTR));
                                    
                                    
                                    if (var.getAttributeValue(ChannelMLConstants.VARIABLE_NAME_ATTR).equals(ChannelMLConstants.FORCE_INIT_ATTR_VAL))
                                    {

                                        logger.logComment("..Found the element: ("+ var+") in xslFile: "+xslFile.getAbsolutePath());
                                        
                                        
                                        for (int l = 0; l < var.getContents().size(); l++)
                                        {
                                            logger.logComment("Checking content "+l+": ("+var.getContents().get(l)+")");

                                            if (var.getContents().get(l) instanceof SimpleXMLContent)
                                            {
                                                SimpleXMLContent cont = (SimpleXMLContent) var.getContents().get(l);

                                                if(forceCorrectInit)
                                                    cont.setText("1");
                                                else
                                                    cont.setText("0");
                                            }
                                        }
                                    }
                                    
                                    if (var.getAttributeValue(ChannelMLConstants.VARIABLE_NAME_ATTR).equals(ChannelMLConstants.PARALLEL_MODE_VAL))
                                    {

                                        logger.logComment("..Found the element: ("+ var+") in xslFile: "+xslFile.getAbsolutePath());
                                        
                                        
                                        for (int l = 0; l < var.getContents().size(); l++)
                                        {
                                            logger.logComment("Checking content "+l+": ("+var.getContents().get(l)+")");

                                            if (var.getContents().get(l) instanceof SimpleXMLContent)
                                            {
                                                SimpleXMLContent cont = (SimpleXMLContent) var.getContents().get(l);

                                                if(parallelMode)
                                                    cont.setText("1");
                                                else
                                                    cont.setText("0");
                                            }
                                        }
                                    }
                                }
                                
                                logger.logComment("NOW variable: ("+ xslDoc.getXMLEntities(ChannelMLConstants.FORCE_INIT_ELEMENT)[j].getXMLString("", false)+")");
                            }
                            
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


    public String getUnitsUsedInFile()
    {
        if (xmlDoc!=null)
        {
            return xmlDoc.getValueByXPath(ChannelMLConstants.getUnitsXPath());
        }
        return null;
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

        xpath = ChannelMLConstants.getPreV1_7_3GateXPath(1);
        logger.logComment("Checking gates xpath: "+xpath);

        SimpleXMLEntity[] gates = xmlDoc.getXMLEntities(xpath);


        if (gates!=null)
        {
            for (int i = 0; i < gates.length; i++)
            {
                logger.logComment("Got entity: " + gates[i]);
            }
        }

        boolean gatesAbsent = (gates==null || gates.length==0);
        
        if(gatesAbsent)
        {
            xpath = ChannelMLConstants.getIonNameXPath();
            String possName = xmlDoc.getValueByXPath(xpath);
            if (possName!=null && possName.equals(ChannelMLConstants.NON_SPECIFIC_ION_NAME))
            {
                logger.logComment("Ion is non specific in post v1.7.3 format");
                return true;
            }
        }

        boolean nonSpecific = false;

        String ionXpath = ChannelMLConstants.getPreV1_7_3IonsXPath();

        logger.logComment("Checking xpath: " + xpath);

        SimpleXMLEntity[] ions = xmlDoc.getXMLEntities(ionXpath);

        if (ions != null)
        {
            for (int i = 0; i < ions.length; i++)
            {
                logger.logComment("Got entity: " + ions[i].getXMLString("", false));

                if ( ( (SimpleXMLElement) ions[i]).getAttributeValue(ChannelMLConstants.LEGACY_ION_NAME_ATTR).equals(ChannelMLConstants.NON_SPECIFIC_ION_NAME))
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
            Project testProj = Project.loadProject(new File("C:/fullCheckout/tempModels/neuroConstruct/Ex7_GranuleCell/Ex7_GranuleCell.neuro.xml"),
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

            System.out.println("\n\n\n");
            ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("Gran_KCa_98");
            ChannelMLCellMechanism.logger.setThisClassVerbose(false);

            cmlMech.initialise(testProj, false);

            System.out.println("Channel: "+cmlMech);
            System.out.println("Is this passive: "+ cmlMech.isPassiveNonSpecificCond());
            
            
            cmlMech = (ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("GranPassiveCond");

            cmlMech.initialise(testProj, false);

            System.out.println("Channel: "+cmlMech);
            System.out.println("Is this passive: "+ cmlMech.isPassiveNonSpecificCond());




            /*



            logger.logComment("contents: "+cmlProc.getXMLDoc().getXMLString("", false));

            String gmaxXpath = "//@default_gmax";
            String eXpath = "channelml/channel_type/current_voltage_relation/ohmic/@default_erev";

            //cmlProc.setValue(gmaxXpath, "1000");
            //cmlProc.setValue(eXpath, "-10");


            logger.logComment("Value of " + gmaxXpath + ": " + cmlProc.getValue(gmaxXpath));
            //logger.logComment("Value of " + eXpath + ": " + cmlProc.getValue(eXpath));

            SimulatorMapping map = (SimulatorMapping)cmlProc.getSimMappings().get(0);

            logger.logComment("mapping for: "+map.getSimEnv()+", file: "+ map.getXslFile());

           // map.

           File tempFile = new File("../temp/temp.txt");


           boolean success = cmlProc.createImplementationFile("NEURON", UnitConverter.GENESIS_SI_UNITS,tempFile, testProj, false, false, false);

           System.out.println("Created file: "+tempFile.getAbsolutePath()+": "+ success);
*/
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }

}
