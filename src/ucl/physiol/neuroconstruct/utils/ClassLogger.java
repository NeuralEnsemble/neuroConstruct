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

package ucl.physiol.neuroconstruct.utils;

import java.util.*;
import java.io.*;

/**
 * Simple logging facility
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class ClassLogger implements Serializable
{
    private String myClassName = null;
    private Logger logger = null;
    private boolean silentMode = false;
    private boolean verboseMode = false;

    private static Vector<String> silentClasses = new Vector<String>();

    private ClassLogger()
    {

    }

    public ClassLogger(String classname)
    {
        //System.out.println("New ClassLogger for : "+classname);
        myClassName = classname;
        logger = Logger.getLogger();
    }



    /**
     * Logs a comment to the console/logfile based on settings in GeneralProperties,
     * puts 11-25-41.781> NmodlEditorFrame       :  etc. before each line
     */
    public void logComment(String comment)
    {
        logComment(comment, false);
    }

    /**
     * Same as above but forceConsoleOut = true overrides log to console settings
     */
    public void logComment(String comment, boolean forceConsoleOut)
    {
        if (!silentMode || verboseMode || forceConsoleOut) logger.log(this.myClassName,
                                                                      comment,
                                                                      false,
                                                                      forceConsoleOut || verboseMode);
    }


    /**
     * Logs a comment to the console/logfile based on settings in GeneralProperties,
     * without anything before each line
     */

    public void logSimpleComment(String comment, boolean forceConsoleOut)
    {
        if (!silentMode || verboseMode) logger.logSimple(comment, false, forceConsoleOut);
    }

    public void logError(String comment, boolean forceConsoleOut)
    {
        logger.logError(this.myClassName, comment, forceConsoleOut);
    }

    public void logError(String comment, Throwable t, boolean forceConsoleOut)
    {
        logger.logError(this.myClassName, comment, t, forceConsoleOut);
    }

    public void logError(String comment)
    {
        logger.logError(this.myClassName, comment, false);
    }

    public void logError(String comment, Throwable t)
    {
        logger.logError(this.myClassName, comment, t, false);
    }



    public boolean getInitialSaveToFileState()
    {
        return logger.getInitialSaveToFileState();
    }

    /**
     * turn off just this class
     */
    public void setThisClassSilent(boolean silentMode)
    {
        if (silentMode && (!silentClasses.contains(myClassName)))
            logger.log(this.myClassName, "*** Note *** Class: "+this.myClassName+" is in silent mode. No comments will be printed", false, false);

        if (!silentMode && (silentClasses.contains(myClassName)))
            logger.log(this.myClassName, "*** Note *** Class: "+this.myClassName+" is in coming out of silent mode.", false, false);



        if (silentMode)
            silentClasses.add(myClassName); // To avoid repeatedly printing this message for every instance of the class...
        else
            silentClasses.remove(myClassName);
        this.silentMode = silentMode;
    }

    public void setThisClassVerbose(boolean verboseMode)
    {
        this.verboseMode = verboseMode;
    }



    public boolean getThisClassSilent()
    {
        return silentMode;
    }

    public boolean getThisClassVerbose()
    {
        return verboseMode;
    }



}
