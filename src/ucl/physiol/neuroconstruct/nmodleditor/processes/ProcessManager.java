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

package ucl.physiol.neuroconstruct.nmodleditor.processes;

import java.io.*;
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

    public boolean compileFileWithNeuron() throws NeuronException
    {
        logger.logComment("Going to compile the file: "+ myFile.getAbsolutePath());

        Runtime rt = Runtime.getRuntime();
        String neuronHome = GeneralProperties.getNeuronHomeDir();
        String commandToExecute = null;

        try
        {
            String directoryToExecuteIn = myFile.getParent();
            File fileToBeCreated = null;

            logger.logComment("Parent dir: "+ directoryToExecuteIn);


            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                String filename = directoryToExecuteIn
                                  + System.getProperty("file.separator")
                                  + "nrnmech.dll";

                logger.logComment("Name of file to be created: " + filename);

                fileToBeCreated = new File(filename);

                logger.logComment("Trying to delete any previous: " + fileToBeCreated.getAbsolutePath());

                if (fileToBeCreated.exists())
                {
                    fileToBeCreated.delete();
                    logger.logComment("Deleted.");
                }

                commandToExecute = neuronHome
                                   + "\\bin\\rxvt.exe -e "
                                   + neuronHome
                                   + "/bin/sh "
                                   + neuronHome
                                   + "/lib/mknrndll.sh "
                                   + neuronHome
                                   + " ";
            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                String filename = directoryToExecuteIn
                    + System.getProperty("file.separator")
                    + NeuronFileManager.getArchSpecificDir()
                    + System.getProperty("file.separator")
                    + ".libs"
                    + System.getProperty("file.separator")
                    + "libnrnmech.so";

                /** @todo Needs checking on Mac/powerpc/i686 */
                if (GeneralUtils.isMacBasedPlatform())
                {
                    if (System.getProperty("os.arch").equalsIgnoreCase("ppc"))
                    {
                        filename = directoryToExecuteIn
                            + System.getProperty("file.separator")
                            + "powerpc";
                    }
                    else
                    {
                        // hope for the best...
                        filename = directoryToExecuteIn
                            + System.getProperty("file.separator")
                            + System.getProperty("os.arch");

                    }
                }

                logger.logComment("Name of file to be created: " + filename);

                fileToBeCreated = new File(filename);

                logger.logComment("Trying to delete any previous: " + fileToBeCreated.getAbsolutePath());

                if (fileToBeCreated.exists())
                {
                    fileToBeCreated.delete();

                    logger.logComment("Deleted.");
                }

                commandToExecute = neuronHome
                                   + System.getProperty("file.separator")
                                   + "bin"
                                   + System.getProperty("file.separator")
                                   + "nrnivmodl";

                logger.logComment("commandToExecute: " + commandToExecute);

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


            if(fileToBeCreated.exists())
            {
                logger.logComment("Successful compilation");
                GuiUtils.showInfoMessage(logger, "Success", "Have successfully compiled the mods file into: "+ fileToBeCreated.getAbsolutePath(),
                                           null);

                  return true;
            }
            else if (GeneralUtils.isMacBasedPlatform())
            {
                GuiUtils.showInfoMessage(logger, "Probable success", 
                        "The conditions for successful compilation of the files on a Mac haven't fully been determined,"
                        +" so assuming successful compilation.\n\n"
                        +"Note: it is essential that you can compile NEURON mod files on your system (via nrnivmodl) before running NEURON from neuroConstruct.\n"
                        +"This will involve installing the Developer Tools (XCode) in addition to the NEURON *.dmg",
                                           null);

                  return true;


            }

            else
            {
                logger.logComment("Unsuccessful compilation");

                GuiUtils.showErrorMessage(logger, "Problem with the compilation. Please note that Neuron checks every *.mod file"
                                          +" in this file's home directory\n("+myFile.getParent()+").\nFor more information when this error occurs, enable logging at Settings -> General Properties & Project Defaults -> Logging",
                                          null, null);
                return false;
            }



        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new NeuronException("Error testing: " +
                                       myFile.getAbsolutePath()+".\nIs NEURON correctly installed?\n"
                                       +"NEURON home dir being used: "+GeneralProperties.getNeuronHomeDir()
                                       +"\nThis should be set to the correct location at Settings -> General Properties & Project Defaults\n\n"
                                       +"Note: leave that field blank in that options window and restart and neuroConstruct will search for a possible location of NEURON\n\n", ex);
        }

    }


    public void runAsHocFile() throws NeuronException
    {

        logger.logComment("Trying to run file as hoc: "+ myFile.getAbsolutePath());

        File directoryToExecuteIn =  myFile.getParentFile();

        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;
        try
        {
            //commandToExecute = "cmd /K start \"Neuron...\" /wait "+neuronHome+"\\bin\\neuron.exe " + myFile.getAbsolutePath();

            String locationOfNeuron = GeneralProperties.getNeuronHomeDir();
            String neuronExecutable =null;

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");
                neuronExecutable = locationOfNeuron
                                   + System.getProperty("file.separator")
                                   + "bin"
                                   + System.getProperty("file.separator")
                                   + "neuron.exe";
            }
            else
            {
                logger.logComment("Assuming *nix environment...");

                neuronExecutable = locationOfNeuron
                               + System.getProperty("file.separator")
                               + "bin"
                               + System.getProperty("file.separator")
                               + "nrngui -dll "
                               + directoryToExecuteIn
                               + System.getProperty("file.separator")
                               + NeuronFileManager.getArchSpecificDir()
                               + System.getProperty("file.separator")
                               + ".libs"
                               + System.getProperty("file.separator")
                               + "libnrnmech.so";
            }

            commandToExecute = GeneralProperties.getExecutableCommandLine() + " " +
                       neuronExecutable + " " + myFile.getAbsolutePath();

            Process currentProcess = rt.exec(commandToExecute, null, directoryToExecuteIn);
            ProcessOutputWatcher procOutputMain = new ProcessOutputWatcher(currentProcess.getInputStream(), "Output");
            procOutputMain.start();

            ProcessOutputWatcher procOutputError = new ProcessOutputWatcher(currentProcess.getErrorStream(), "Error");
            procOutputError.start();

            logger.logComment("Have successfully executed command: " + commandToExecute + " in directory: "+ directoryToExecuteIn);
        }
        catch (Exception ex)
        {
            throw new NeuronException("Error running the command: "+ commandToExecute);
        }
    }





    public static void main(String args[])
    {
        //File f = new File("/home/padraig/temp/mod/leak.mod");
        File f = new File("c:\\temp\\mod\\leak.mod");

        System.out.println("Neuron home: "+ GeneralProperties.getNeuronHomeDir());


        try
        {
            ProcessManager pm = new ProcessManager(f);
            System.out.println("Trying test neuron...");
            pm.testFileWithNeuron();
            System.out.println("Trying complie...");
            pm.compileFileWithNeuron();
            System.out.println("Done!");

            System.exit(0);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }
}
