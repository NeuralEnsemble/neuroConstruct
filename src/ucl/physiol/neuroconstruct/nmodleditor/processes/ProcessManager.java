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

package ucl.physiol.neuroconstruct.nmodleditor.processes;

import java.io.*;
import java.text.*;
import java.util.Date;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.neuron.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 *  
 */

public class ProcessManager
{
    ClassLogger logger = new ClassLogger("ProcessManager");

    File myFile = null;

    public ProcessManager(File modFile)
    {
        logger.logComment("ProcessManager created for mod file: "+ modFile.getAbsolutePath());
        myFile = modFile;
    }



    public boolean testFileWithNeuron() throws NeuronException
    {
        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;
        try
        {

            String locationOfNeuron = GeneralProperties.getNeuronHomeDir();
            String modlunitExecutable =null;

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");
                modlunitExecutable = locationOfNeuron
                                   + System.getProperty("file.separator")
                                   + "bin"
                                   + System.getProperty("file.separator")
                                   + "modlunit.exe";

                commandToExecute = modlunitExecutable
                                   + " "
                                   + myFile.getAbsolutePath();
            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                modlunitExecutable = locationOfNeuron
                                   + System.getProperty("file.separator")
                                   + "bin"
                                   + System.getProperty("file.separator")
                                   + "modlunit";

                commandToExecute = GeneralProperties.getExecutableCommandLine()
                                   + " "
                                   + modlunitExecutable
                                   + " "
                                   + myFile.getAbsolutePath();
            }


            Process currentProcess = rt.exec(commandToExecute);
            logger.logComment("Have successfully executed command: " + commandToExecute);

            ProcessOutputWatcher procOutputMain = new ProcessOutputWatcher(currentProcess.getInputStream(), "Test");
            procOutputMain.start();

            ProcessOutputWatcher procOutputError = new ProcessOutputWatcher(currentProcess.getErrorStream(), "Error");
            procOutputError.start();

            currentProcess.waitFor();
            logger.logComment("Exit value: "+currentProcess.exitValue());

            if(currentProcess.exitValue()==0)
            {
                logger.logComment("Successful termination");
                GuiUtils.showInfoMessage(logger, "Success","Have successfully tested file: "+myFile.getAbsolutePath(),
                                        null);

                return true;
            }
            else
            {
                logger.logComment("Unsuccessful termination");

                GuiUtils.showErrorMessage(logger, "Problem with the file. \nError output: \n"+procOutputError.getLog(),
                                          null, null);

               return false;
            }

        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new NeuronException("Error testing: " +
                                       myFile.getAbsolutePath(), ex);
        }

    }
    
    public boolean compileFileWithNeuron(boolean forceRecompile) throws NeuronException
    {
        return compileFileWithNeuron(forceRecompile, true);
    }

    /*
     * Compliles all of the mod files at the specified location using NEURON's nrnivmodl/mknrndll.sh
     */
    public boolean compileFileWithNeuron(boolean forceRecompile, boolean showDialog) throws NeuronException
    {
        logger.logComment("Going to compile the file: "+ myFile.getAbsolutePath()+", forcing recompile: "+ forceRecompile);

        Runtime rt = Runtime.getRuntime();
        String neuronHome = GeneralProperties.getNeuronHomeDir();
        String commandToExecute = null;

        try
        {
            String directoryToExecuteIn = myFile.getParent();
            File fileToBeCreated = null;
            File otherCheckFileToBeCreated = null; // for now...

            logger.logComment("Parent dir: "+ directoryToExecuteIn);

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                String filename = directoryToExecuteIn
                                  + System.getProperty("file.separator")
                                  + "nrnmech.dll";
                
                
                fileToBeCreated = new File(filename);

                logger.logComment("Name of file to be created: " + fileToBeCreated.getAbsolutePath());

                File modCompileScript = ProjectStructure.getNeuronUtilsWinModCompileFile();

                if (showDialog)
                {
                    commandToExecute = neuronHome
                                   + "\\bin\\rxvt.exe -e "
                                   + neuronHome
                                   + "/bin/sh \""
                                   + modCompileScript.getAbsolutePath()
                                   + "\" "
                                   + neuronHome
                                   + " ";
                }
                else
                {
                   commandToExecute = neuronHome
                                   + "/bin/sh \""
                                   + modCompileScript.getAbsolutePath()
                                   + "\" "
                                   + neuronHome
                                   + " "+" -q"; //quiet mode, no "press any key to continue"...
                }
                
                logger.logComment("commandToExecute: " + commandToExecute);
            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                String myArch = GeneralUtils.getArchSpecificDir();
                
                String backupArchDir = GeneralUtils.DIR_64BIT;
                
                if (myArch.equals(GeneralUtils.ARCH_64BIT))
                    backupArchDir = GeneralUtils.DIR_I686;
                
                
                String filename = directoryToExecuteIn
                    + System.getProperty("file.separator")
                    + myArch
                    /*+ System.getProperty("file.separator")
                    + ".libs"*/
                    + System.getProperty("file.separator")
                    + "libnrnmech.la";
                
                // In case, e.g. a 32 bit JDK is used on a 64 bit system
                String backupFilename = directoryToExecuteIn
                    + System.getProperty("file.separator")
                    + backupArchDir
                    /*+ System.getProperty("file.separator")
                    + ".libs"*/
                    + System.getProperty("file.separator")
                    + "libnrnmech.la";

                /** @todo Needs checking on Mac/powerpc/i686 */
                if (GeneralUtils.isMacBasedPlatform())
                {
                    filename = directoryToExecuteIn
                        + System.getProperty("file.separator")
                        + GeneralUtils.getArchSpecificDir()
                        + System.getProperty("file.separator")
                        + "libnrnmech.la";

                    backupFilename = directoryToExecuteIn
                        + System.getProperty("file.separator")
                        + "umac"
                        + System.getProperty("file.separator")
                        + "libnrnmech.la";
                  
                }

                logger.logComment("Name of file to be created: " + filename);
                logger.logComment("Backup file to check for success: " + backupFilename);

                fileToBeCreated = new File(filename);
                otherCheckFileToBeCreated = new File(backupFilename);
                
                

                commandToExecute = neuronHome
                                   + System.getProperty("file.separator")
                                   + "bin"
                                   + System.getProperty("file.separator")
                                   + "nrnivmodl";

                logger.logComment("commandToExecute: " + commandToExecute);

            }
            
            if (!forceRecompile)
            {
                File fileToCheck = null;

                if (fileToBeCreated.exists()) 
                    fileToCheck = fileToBeCreated;

                if (otherCheckFileToBeCreated!=null && otherCheckFileToBeCreated.exists()) 
                    fileToCheck = otherCheckFileToBeCreated;

                logger.logComment("Going to check if mods in "+myFile.getParentFile()+"" +
                            " are newer than "+ fileToCheck);

                if (fileToCheck!=null)
                {
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
                    
                    boolean newerModExists = false;
                    File[] allMods = myFile.getParentFile().listFiles(new SimpleFileFilter(new String[]{".mod"}, ""));
                    for(File f: allMods)
                    {
                        if (f.lastModified() > fileToCheck.lastModified())
                        {
                            newerModExists = true;
                            logger.logComment("File "+f+" ("+df.format(new Date(f.lastModified()))+") was modified later than "+ fileToCheck +" ("+df.format(new Date(fileToCheck.lastModified()))+")");
                        }
                    }
                    if (!newerModExists) 
                    {
                        logger.logComment("Not being asked to recompile, and no mod files exist in "+myFile.getParentFile()+"" +
                            " which are newer than "+ fileToCheck);
                        return true;
                    }
                    else
                    {
                        logger.logComment("Newer mod files exist!");
                    }
                }

            }
            else
            {
                logger.logComment("Forcing recompile...");
            }

            logger.logComment("Trying to delete any previous: " + fileToBeCreated.getAbsolutePath());

            if (fileToBeCreated.exists())
            {
                fileToBeCreated.delete();

                logger.logComment("Deleted.");
            }
            
            
            
            
            logger.logComment("directoryToExecuteIn: " + directoryToExecuteIn);
            

            Process currentProcess = rt.exec(commandToExecute, null, new File(directoryToExecuteIn));
            ProcessOutputWatcher procOutputMain = new ProcessOutputWatcher(currentProcess.getInputStream(), "Compile");
            procOutputMain.start();

            ProcessOutputWatcher procOutputError = new ProcessOutputWatcher(currentProcess.getErrorStream(), "Error");
            procOutputError.start();

            logger.logComment("Have successfully executed command: " + commandToExecute);

            currentProcess.waitFor();

            GeneralUtils.timeCheck("Exit value for compilation: "+currentProcess.exitValue());


            if(fileToBeCreated.exists() || otherCheckFileToBeCreated.exists())
            {
                // In case, e.g. a 32 bit JDK is used on a 64 bit system
                File createdFile  = fileToBeCreated;
                if (!createdFile.exists())
                    createdFile = otherCheckFileToBeCreated;
                
                logger.logComment("Successful compilation");
                if (showDialog)
                {
                    GuiUtils.showInfoMessage(logger, "Success", "Have successfully compiled the mods file into: "+ createdFile.getAbsolutePath(),
                                               null);
                }

                  return true;
            }
            else if (GeneralUtils.isMacBasedPlatform())
            {
                if (showDialog)
                {
                    GuiUtils.showInfoMessage(logger, "Success", 
                            "The conditions for successful compilation of the files on a Mac haven't fully been determined,"
                            +" so assuming successful compilation.\n\n"
                            +"Note: it is essential that you can compile NEURON mod files on your system (via nrnivmodl) before running NEURON from neuroConstruct.\n"
                            +"This will involve installing the Developer Tools (XCode) in addition to the NEURON *.dmg",
                                               null);
                }
                

                  return true;


            }

            else
            {
                logger.logComment("Unsuccessful compilation. File doesn't exist: "+ fileToBeCreated.getAbsolutePath()
                    +" (and neither does "+otherCheckFileToBeCreated.getAbsolutePath()+")");

                GuiUtils.showErrorMessage(logger, "Problem with the compilation. File doesn't exist: "+ fileToBeCreated.getAbsolutePath()
                    +" (and neither does "+otherCheckFileToBeCreated.getAbsolutePath()+")\nPlease note that Neuron checks every *.mod file"
                                          +" in this file's home directory\n("+myFile.getParent()+").\nFor more information when this error occurs, enable logging at Settings -> General Properties & Project Defaults -> Logging",
                                          null, null);
                return false;
            }



        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            String dirContents = "bin/nrniv";
            if (GeneralUtils.isWindowsBasedPlatform())
            {
                dirContents = "bin\\neuron.exe";
            }
            throw new NeuronException("Error testing: " +
                                       myFile.getAbsolutePath()+".\nIs NEURON correctly installed?\n"
                                       +"NEURON home dir being used: "+GeneralProperties.getNeuronHomeDir()
                                       +"\nThis should be set to the correct location (the folder containing "+dirContents+") at Settings -> General Properties & Project Defaults\n\n"
                                       +"Note: leave that field blank in that options window and restart and neuroConstruct will search for a possible location of NEURON\n\n", ex);
        }

    }





    public static void main(String args[])
    {
        //File f = new File("/home/padraig/temp/mod/leak.mod");
        File f = new File("D:\\nC_projects\\Project_1hjkfjhk\\generatedNEURON\\CurrentClampExt.mod");

        System.out.println("Neuron home: "+ GeneralProperties.getNeuronHomeDir());


        try
        {
            ProcessManager pm = new ProcessManager(f);
            //System.out.println("Trying test neuron...");
            //pm.testFileWithNeuron();
            System.out.println("Trying complie...");
            pm.compileFileWithNeuron(false, true);
            System.out.println("Done!");

            System.exit(0);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }
}
