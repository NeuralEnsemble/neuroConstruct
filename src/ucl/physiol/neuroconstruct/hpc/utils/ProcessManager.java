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

package ucl.physiol.neuroconstruct.hpc.utils;

import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple class for spawning external processes
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class ProcessManager
{
    private static ClassLogger logger = new ClassLogger("ProcessManager");



        public static String runCommand(String command, ProcessFeedback feedback, int millisToWait) throws IOException
        {
            File directoryToExecuteIn =  new File(".");
            return runCommand(command, feedback, directoryToExecuteIn, millisToWait);
        }

        public static String runCommand(String command, ProcessFeedback feedback, File directoryToExecuteIn, int millisToWait) throws IOException
        {

        //File directoryToExecuteIn =  new File("c:\\condor\\bin\\");

        Runtime rt = Runtime.getRuntime();

        logger.logComment("Trying to execute command: " + command + " in directory: " +
                          directoryToExecuteIn.getCanonicalPath());

        Process currentProcess = rt.exec(command, null, directoryToExecuteIn);
        ProcessOutputCatcher procOutputMain = new ProcessOutputCatcher(currentProcess.getInputStream(), "Output", feedback, ProcessOutputCatcher.STDIO_OUTPUT);
        procOutputMain.start();

        ProcessOutputCatcher procOutputError = new ProcessOutputCatcher(currentProcess.getErrorStream(), "Error", feedback, ProcessOutputCatcher.ERR_OUTPUT);
        procOutputError.start();

        logger.logComment("Have successfully executed command: " + command + " in directory: " + directoryToExecuteIn);

        try
        {
            Thread.sleep(millisToWait);
            int retVal = currentProcess.exitValue();
            return ""+retVal;
        }
        catch (Exception ex)
        {
            return "Not returned after "+millisToWait+" ms";
        }


    }
}
