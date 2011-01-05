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

package ucl.physiol.neuroconstruct.hpc.utils;

import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple class for spawning external processes
 *
 * @author Padraig Gleeson
 *  
 */


public class ProcessManager
{
    private static ClassLogger logger = new ClassLogger("ProcessManager");



    public static String runCommand(String command, String title, int millisToWait) throws IOException
    {
        return runCommand(command, title, millisToWait, new File("."));
    }

    public static String runCommand(String command, String title, int millisToWait, File directoryToExecuteIn) throws IOException
    {
        final String feedbackInfo = title;
        ProcessFeedback pf = new ProcessFeedback()
        {
            public void comment(String comment)
            {
                logger.logComment(feedbackInfo+": output >> "+comment);
            }
            public void error(String comment)
            {
                logger.logComment(feedbackInfo+": error  >> "+comment);
            }
        };

        return runCommand(command, pf, directoryToExecuteIn, millisToWait);
    }

    public static String runCommand(String command, ProcessFeedback feedback, int millisToWait) throws IOException
    {
        File directoryToExecuteIn =  new File(".");
        return runCommand(command, feedback, directoryToExecuteIn, millisToWait);
    }

    public static String runCommand(String command, ProcessFeedback feedback, File directoryToExecuteIn, int millisToWait) throws IOException
    {

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
