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
import ucl.physiol.neuroconstruct.nmodleditor.processes.ProcessOutputWatcher;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

/**
 * Base class for all NeuroML based Cell Mechanism. Contains (in addition to info
 * in CellMechanism class) a string with the name of the file and a SimpleXMLDocument
 * containing the contents of the xml (once initialised) and a number of mappings to NEURON, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuroML2Component extends XMLCellMechanism
{
    //public static final String MECHANISM_TYPE = "NeuroML 2 mechanism";

    public NeuroML2Component()
    {
        logger = new ClassLogger("NeuroML2Component");
        //logger.setThisClassVerbose(true);
    }

    
    @Override
    public String toString()
    {
        return "NeuroML 2 Component [InstanceName: "+this.getInstanceName()+", neuroml2File: "+getXMLFile()+"]";
    }


    public String getNameXPath()
    {
        return "/neuroml/@id";  //todo: move to SBMLConstants...
    }

    public String getDescriptionXPath()
    {
        return "/neuroml/notes";  //todo: move to SBMLConstants...
    }



    @Override
    public String getMechanismType()
    {
        return mechanismType;
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
                logger.logComment("Found suitable sim env: "+getSimMappings().get(k));

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

                        File sbmlFile = this.getXMLFile(project);

                        File mappingFile = new File(sbmlFile.getParentFile(), getSimMappings().get(k).getMappingFile());

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

                            if (getXMLDoc() == null)
                            {
                                try
                                {
                                    this.initialise(project, false);
                                }
                                catch (SBMLException ex1)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "Error creating implementation of Cell Mechanism: " +
                                                              this.getInstanceName(),
                                                              ex1,
                                                              null);

                                    return false;

                                }
                            }

                            ArrayList<String> xpathLocs = getXMLDoc().getXPathLocations(true);

                            for (int j = 0; j < xpathLocs.size(); j++)
                            {
                                String name = xpathLocs.get(j);
                                if (name.indexOf("schemaLocation")<0)
                                {
                                    String val = getXMLDoc().getValueByXPath(name);
                                    if(val!=null)
                                    {
                                        String safeValue = GeneralUtils.replaceAllTokens(val, "\n", " ");

                                        if (safeValue.length()>150)
                                            safeValue = safeValue.substring(0,150)+" ...";

                                        fileOut.write(commentLinePrefix + name
                                                      + " = "
                                                      + safeValue
                                                      + " \n");
                                    }
                                }
                            }


                            fileOut.write("\n// File from which this was generated: " + sbmlFile.getAbsolutePath() + "\n");
                            fileOut.write("\n// File with mapping to simulator: " + mappingFile.getAbsolutePath() + "\n");
                        }


                        fileOut.write("\n"+commentBlockEnd+"\n");

                        fileOut.write("\n");
                        

                        String transformed = "";
                        File dirToCreateIn = fileToGenerate.getParentFile();

                        logger.logComment("directoryToExecuteIn: " + dirToCreateIn);

                        String commandToExecute = "python "+ mappingFile.getAbsolutePath()+" "+ sbmlFile.getAbsolutePath();

                        Runtime rt = Runtime.getRuntime();
                        Process currentProcess = rt.exec(commandToExecute, null, dirToCreateIn);

                        ProcessOutputWatcher procOutputMain = new ProcessOutputWatcher(currentProcess.getInputStream(), "Generate");
                        procOutputMain.start();

                        ProcessOutputWatcher procOutputError = new ProcessOutputWatcher(currentProcess.getErrorStream(), "Error");
                        procOutputError.start();

                        logger.logComment("Have successfully executed command: " + commandToExecute);

                        currentProcess.waitFor();

                        logger.logComment("Exit value for compilation: "+currentProcess.exitValue());
                        logger.logComment(procOutputMain.getLog());
                        logger.logComment(procOutputError.getLog());

                        //transformed = GeneralUtils.replaceAllTokens(transformed, "\n\n", "\n");

                        String fileExtn = ".???";
                        String filenameCreated = "";

                        if(targetEnv.equals(SimEnvHelper.NEURON))
                        {
                            fileExtn = ".mod";
                        }
                        if (sbmlFile.getAbsolutePath().endsWith(".xml"))
                        {
                            filenameCreated = sbmlFile.getAbsolutePath().substring(0, sbmlFile.getAbsolutePath().length()-4)+fileExtn;
                        }
                        File fileCreated = new File(filenameCreated);
                        //FileReader fr = new FileReader
                        transformed = GeneralUtils.readShortFile(fileCreated);

                        fileOut.write(transformed);
                        fileCreated.delete();

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
                catch (Exception ex)
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





    public static void main(String[] args)
    {
        try
        {
            // nC_projects\Project_sbml\cellMechanisms\Simple3Species
            Project testProj = Project.loadProject(new File("../nC_projects/Project_sbml/Project_sbml.ncx"),
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
            SBMLCellMechanism sbmlMech = (SBMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("Simple3Species");
            SBMLCellMechanism.logger.setThisClassVerbose(false);

            sbmlMech.initialise(testProj, false);
     


            //System.out.println("contents: "+sbmlMech.getXMLDoc().getXMLString("", false));

   
            System.out.println("sbmlMech: "+sbmlMech.getInstanceName()+" ("+sbmlMech.getDescription()+")");

            SimulatorMapping map = sbmlMech.getSimMappings().get(0);

            System.out.println("mapping for: "+map.getSimEnv()+", file: "+ map.getMappingFile());


           File tempFile = new File("../temp/temp.txt");


           boolean success = sbmlMech.createImplementationFile("NEURON", UnitConverter.GENESIS_SI_UNITS,tempFile, testProj, true, true, false, false);

           System.out.println("Created file: "+tempFile.getAbsolutePath()+": "+ success);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }

}
