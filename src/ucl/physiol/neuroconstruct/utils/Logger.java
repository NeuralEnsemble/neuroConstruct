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

import ucl.physiol.neuroconstruct.project.*;

/**
 * Simple logging facility.
 *
 * @author Padraig Gleeson
 *  
 */

public class Logger
{
    //private static boolean isInstantiated = false;

    private File myLogFile = null;
    private FileWriter myLogFileWriter = null;

    private static Logger myLoggerInstance = null;

    private int nominalLengthOfClassname = 22;

    // True if the logger was set to log to file when instantiated
    private boolean initialSaveToFileState = false;

    public static final String LOG_FILE_SUFFIX = "log";

    private Logger()
    {
        //System.out.println("In logger");
    }

    private void initialise()
    {
        //System.out.print("initialising  logger");
        initialSaveToFileState = GeneralProperties.getLogFileSaveToFilePolicy();

        String myFilename = GeneralProperties.getLogFileDir()
                     + System.getProperty("file.separator")
                     + "Log_"+ GeneralUtils.replaceAllTokens(GeneralUtils.getCurrentTimeAsNiceString(), ":", "-")
                     + "_"
                     + GeneralUtils.getCurrentDateAsNiceString()
                     +"."+LOG_FILE_SUFFIX;


        if (initialSaveToFileState)
        {
            try
            {
                myLogFile = new File(myFilename);
                myLogFileWriter = new FileWriter(this.myLogFile);
                this.log("Logger","Log file: "+ myLogFile.getAbsolutePath()+" created", false, false);
            }
            catch (IOException ex)
            {
                System.err.println("Problem creating log file: "+ myFilename);
                ex.printStackTrace();
            }
        }

        this.log("Logger","-------------------------------------------------", false, false);
        this.log("Logger","", false, false);

        if (!GeneralProperties.getLogFilePrintToScreenPolicy())
        {
            //System.out.println("*** Logging turned off!! ***");
            //System.out.println("Setting can be changed in neuroConstruct main interface");
        }
        //System.out.println("Created Logger");
    }

    public void closeLogFile()
    {
            try
            {
                myLogFileWriter.flush();
                myLogFileWriter.close();
            }
            catch (Exception ex)
            {
                if (initialSaveToFileState)
                {
                    this.logError("Logger","Problem closing log file: "+ myLogFile.getAbsolutePath(), ex, false);
                }
            }
    }

    public static Logger getLogger()
    {
        //System.out.println("Getting logger...");
        if (myLoggerInstance == null)
        {
           // System.out.println("Creating new...");
            myLoggerInstance = new Logger();
            myLoggerInstance.initialise();
        }
        return myLoggerInstance;
    }

    public boolean getInitialSaveToFileState()
    {
        return initialSaveToFileState;
    }


    protected void log(String className, String comment, boolean error, boolean forceConsoleOut)
    {
        if (! (initialSaveToFileState ||
               GeneralProperties.getLogFileSaveToFilePolicy() ||
               GeneralProperties.getLogFilePrintToScreenPolicy() ||
               forceConsoleOut))
            return;

        if (comment.indexOf("\n") > 0)
        {
            String[] singleLines = comment.split("\\n");
            for (int i = 0; i < singleLines.length; i++)
            {
                log(className, singleLines[i], error, forceConsoleOut);
            }
            return;
        }
        StringBuffer precedingComment = new StringBuffer(className);
        if (className.length() <= nominalLengthOfClassname)
        {
            for (int i = className.length(); i < nominalLengthOfClassname + 1; i++)
            {
                precedingComment.append(" ");
            }
        }
        String stringToLog = GeneralUtils.getCurrentTimeAsNiceStringWithMillis() + "> " + precedingComment + ": " + comment;

        logSimple(stringToLog, error, forceConsoleOut);

    }

    protected void logSimple(String comment, boolean error, boolean forceConsoleOut)
    {
        try
        {
            // Only log to file if the current setting says so (obviously)
            // and the initial setting was true (i.e. so we know the file was created...)
            if (GeneralProperties.getLogFileSaveToFilePolicy()
                && initialSaveToFileState)
            {
                myLogFileWriter.write(comment + "\r\n");
                myLogFileWriter.flush();
            }
        }
        catch (Exception ex)
        {
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        // this can change in the middle of looking at a project
        if (GeneralProperties.getLogFilePrintToScreenPolicy() || forceConsoleOut)
        {
            if (!error)
                System.out.println(comment);
            else
                System.err.println(comment);


        }
    }

    protected void logError(String className, String comment, boolean forceConsoleOut)
    {
        if (comment.indexOf("\n") > 0)
        {
            String[] singleLines = comment.split("\\n");
            for (int i = 0; i < singleLines.length; i++)
            {
                log(className, "*** ERROR*** : "+singleLines[i], true, forceConsoleOut);
            }
            return;
        }

        this.log(className, "*** ERROR*** : "+comment, true, forceConsoleOut);

    }


    protected void logError(String className, String comment, Throwable t, boolean forceConsoleOut)
    {
        this.logError(className, comment, forceConsoleOut);

        try
        {
            StringWriter traceAsString = new StringWriter();
            PrintWriter trace = new PrintWriter(traceAsString);
            
            t.printStackTrace(trace);        
            this.logError(className, traceAsString.toString(), forceConsoleOut);
            
            Throwable cause = null;
            while((cause = t.getCause())!=null)
            {
                this.logError(className, "------- more.... ", forceConsoleOut);
                cause.printStackTrace(trace);        
                this.logError(className, traceAsString.toString(), forceConsoleOut);
                t = cause;
            }
        }
        catch (Exception ex)
        {
            System.err.println("Exception: " + ex.getMessage());
        }
    }



    public static void main(String[] args)
    {

        Logger l = Logger.getLogger();

        System.out.println("Current date: " + GeneralUtils.getCurrentDateAsNiceString());
        System.out.println("Current time: " + GeneralUtils.getCurrentTimeAsNiceString());
        System.out.println("Current time millis: " + GeneralUtils.getCurrentTimeAsNiceStringWithMillis());


        l.log("DummyClassDummmmyClass", "Hello there...", false, false);
        l.log("Dummyer", "Hello there...", false, false);
        l.log("DummyDummyDummyDummyDummyDummyDummyDummy", "Hello there\nHello there\nHello there...", false, false);

        l.logError("Dummyer", "Hello \nthere...", false);

        try
        {
            "".charAt(100);
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();

            l.logError("Dum", "Some \nerrr...", ex, false);
        }


        l.closeLogFile();
    }
}
