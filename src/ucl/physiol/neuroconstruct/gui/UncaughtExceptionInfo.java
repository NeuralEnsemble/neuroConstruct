/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */


package ucl.physiol.neuroconstruct.gui;

import java.lang.Thread.*;

import ucl.physiol.neuroconstruct.utils.*;


/**
 * A class to catch uncaught exceptions, especially OutOfMemeory...
 * @author Padraig Gleeson
 *  
 */

public class UncaughtExceptionInfo implements UncaughtExceptionHandler 
{
    private static ClassLogger logger = new ClassLogger("UncaughtExceptionInfo");
    private boolean shownMemError = false;
    
    public UncaughtExceptionInfo()
    {
    }
    
    public void uncaughtException(Thread t, Throwable e) 
    {
       String error = "Java error: "+ e.getMessage();
       
       StackTraceElement[] ste = e.getStackTrace();
       //ste[0].
       
       if (ste.length>0 && ste[0].toString().contains("WrappedPlainView$WrappedLine.paint"))
       {
           // Annoying error in GUI startup, ignoring...
               
       }
       else if (e instanceof OutOfMemoryError)
       {
            String run = GeneralUtils.isWindowsBasedPlatform()?"run.bat":"run.sh";
           
            error = error + "\n\nJava has run out of available memory for this task.\n\n" +
               "Note that not all RAM is available to an instance of the JVM when it starts. The amount available is set by the\n" +
               "-Xmx flag in the command line call to start a Java application. The neuroConstruct installers set this to 250MB\n" +
               "of RAM by default. If you'd like to use more, use an altered version of "+run+" in the install directory, and run\n" +
               "this from a shell/command line console.";
            
            System.out.println("Exception on thread: "+ t.getName()+", id: "+t.getId()+"\n"+error);
           
            if (!shownMemError)
                GuiUtils.showErrorMessage(logger, error, e, null);
            
            shownMemError = true;
            
            
       }
       else
       {
           logger.logError("Uncaught exception, no GUI warning...\n"+e.getMessage(), e, true);
       }
      
    }

}