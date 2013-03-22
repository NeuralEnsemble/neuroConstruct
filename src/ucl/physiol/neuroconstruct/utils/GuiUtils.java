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

import java.awt.*;
import java.io.*;
import javax.swing.*;
import ucl.physiol.neuroconstruct.gui.MainApplication;
import ucl.physiol.neuroconstruct.project.GeneralProperties;

/**
 * Some helper stuff for the GUIs
 *
 * @author Padraig Gleeson
 *  
 */


public class GuiUtils
{
    /** @todo Rethink if this is best way to handle this... */
    /**
     * Useful when GUIs (e.g. error guis) pop up and don't have direct access
     * to pointer to this class...
     */
    private static Frame mainApplicationFrame = null;

    private static boolean showInfoGuis = !MainApplication.getStartupMode().equals(MainApplication.StartupMode.COMMAND_LINE_INTERFACE_MODE);

    private static ClassLogger logger = new ClassLogger("GuiUtils");

    private GuiUtils()
    {
    }

    public static void setShowInfoGuis(boolean show)
    {
        showInfoGuis = show;
    }

    /**
     * Show error message to user
     *
     * @param logger the ClassLogger, so the message can be logged too
     * @param errorMessage The message to pop up on screen
     * @param t The Throwable from the caught Exception
     */
    public static void showErrorMessage(ClassLogger logger, String errorMessage, Throwable t, Component parent)
    {
        if (parent == null) parent = getMainFrame();

        SimpleHtmlDoc fullError = new SimpleHtmlDoc();
        fullError.setIncludeReturnsInHtml(false);

        if (logger!=null)
        {
            logger.logError("User being informed of error");
            logger.logError(errorMessage, t, true);
        }

        Throwable nextThrowable = t;
        int numLines = 0;
        int maxLines = 22;

        fullError.addTaggedElement(errorMessage, "b", "p");

        /*
        String[] lines = errorMessage.split("\\n");
        for (int i = 0; i < lines.length; i++)
        {
            int maxLength = 200;
            String truncated =  lines[i];
            if (truncated.length()>maxLength)
                truncated = truncated.substring(0,maxLength)+"...";

            fullError.addTaggedElement(truncated, "b");

            if (i != lines.length - 1)
                fullError.addBreak();
        }*/

        StringBuffer throwableError = new StringBuffer();
        String format = "font color=\"gray\"";

        while (nextThrowable !=null)
        {
            throwableError.append("\n");

            throwableError.append(nextThrowable.getClass().getName() + ": " + nextThrowable.getMessage()+"\n");

            StackTraceElement[] ste =  nextThrowable.getStackTrace();

            for (int i = 0; i < ste.length; i++)
            {
                if (numLines<maxLines)
                {
                    throwableError.append(ste[i].toString()+"\n");
                }
                else if (numLines==maxLines)
                {
                    throwableError.append("More ..."+"\n");
                }
                numLines++;

            }
            nextThrowable = nextThrowable.getCause();
        }
        fullError.addTaggedElement(throwableError.toString(), format);


        if (logger!=null)
        {
           logger.logComment("User being passed the following error message:");
           logger.logComment("---------------------------------------------------------");
           logger.logComment(fullError.toString());
           logger.logComment("---------------------------------------------------------");
           logger.logComment("HTML: ((("+fullError.toHtmlString()+")))");
        }

        showMessage(parent, fullError, "Error", JOptionPane.ERROR_MESSAGE);

    }

    private static void showMessage(Component parent, String message, String title, int type)
    {
        SimpleHtmlDoc htmlMessage = new SimpleHtmlDoc();
        htmlMessage.setIncludeReturnsInHtml(false);
        htmlMessage.addRawHtml(message);
        showMessage(parent, htmlMessage, title, type);

    }

    private static void showMessage(Component parent, SimpleHtmlDoc message, String title, int type)
    {

    	if (showInfoGuis)
        {
            //String pp = "<html>fff <br/> lll</html>";
            JOptionPane.showMessageDialog(parent, message.toHtmlString(), title, type);
            //JOptionPane.showMessageDialog(parent, pp, title, type);
        }
        else
        {
            String typeInfo = "";
            switch (type)
            {
                case JOptionPane.ERROR_MESSAGE:
                    typeInfo = "ERROR:  ";
                break;
                case JOptionPane.WARNING_MESSAGE:
                    typeInfo = "WARNING:  ";
                break;
                case JOptionPane.INFORMATION_MESSAGE:
                    typeInfo = "INFO:  ";
                break;
                case JOptionPane.PLAIN_MESSAGE:
                    typeInfo = "INFO:  ";
                break;

            }

            System.out.println(typeInfo+message.toString());
        }
    }

    /**
     * Show info message to user
     *
     * @param logger the ClassLogger, so the message can be logged too
     * @param title Title of the dialog
     * @param message The message to pop up on screen
     */
   public static void showInfoMessage(ClassLogger logger, String title, String message, Component parent)
   {
       if (parent == null) parent = getMainFrame();

       if (logger!=null)
       {
           logger.logComment("User being passed the following information message:");
           logger.logComment("---------------------------------------------------------");
           logger.logComment(message);
           logger.logComment("---------------------------------------------------------");
       }

       showMessage(parent,
                   message,
                   title,
                   JOptionPane.PLAIN_MESSAGE);
   }


   /**
    * Show warning message to user
    *
    * @param logger the ClassLogger, so the message can be logged too
    * @param message The message to pop up on screen
    * @param parent Parent Component
    */
   public static void showWarningMessage(ClassLogger logger, String message, Component parent)
   {
       if (parent == null) parent = getMainFrame();

       if (logger != null)
       {
           logger.logComment("User being passed warning...");
           logger.logComment(message);
       }

       showMessage(parent,
                   message,
                   "Warning",
                   JOptionPane.WARNING_MESSAGE);
   }



   /**
    * Show Yes/No option to user
    *
    * @param logger the ClassLogger, so the message can be logged too
    * @param message The message to pop up on screen
    * @param parent Parent Component
    */
   public static boolean showYesNoMessage(ClassLogger logger, String message, Component parent)
   {
       if (parent == null) parent = getMainFrame();

       if (logger != null)
       {
           logger.logComment("User being asked to confirm...");
           logger.logComment(message);
       }

       int yesNo = JOptionPane.showConfirmDialog(parent, message, "Please Confirm",
                                                 JOptionPane.YES_NO_OPTION);

       return yesNo == JOptionPane.YES_OPTION;
   }

   public static void centreWindow(Window win, float fraction)
   {
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       
       win.setSize(new Dimension((int)(screenSize.getWidth()*fraction), (int)(screenSize.getHeight()*fraction)));

       Dimension dlgSize = win.getSize();


       win.setLocation( (screenSize.width - dlgSize.width) / 2,
                       (screenSize.height - dlgSize.height) / 2);

   }

   public static void centreWindow(Window win)
   {
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       Dimension dlgSize = win.getSize();

       if (dlgSize.height > screenSize.height)
           dlgSize.height = screenSize.height;
       if (dlgSize.width > screenSize.width)
           dlgSize.width = screenSize.width;

       win.setLocation( (screenSize.width - dlgSize.width) / 2,
                       (screenSize.height - dlgSize.height) / 2);

   }

   public static void showImage(File imagefile) throws FileNotFoundException
   {
       String browserPath = GeneralProperties.getBrowserPath(true);

        if (browserPath==null)
        {
            GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, null);
            return;
        }
        String command = null;

        if (!imagefile.exists()) throw new FileNotFoundException("Could not find image file: "+ imagefile);

        try
        {
            Runtime rt = Runtime.getRuntime();

            command = browserPath + " file://" + imagefile.getCanonicalPath();

            logger.logComment("Going to execute command: " + command, true);
            rt.exec(command);
            logger.logComment("Executed command: " + command, true);
        }
        catch (Exception ex)
        {
            logger.logError("Error running " + command, ex);
        }
   }



   public static void setMainFrame(Frame frame)
   {
       mainApplicationFrame = frame;
   }

   public static Frame getMainFrame()
   {
       return mainApplicationFrame;
   }

   public static void main(String[] args)
   {
       ClassLogger logger = new ClassLogger("Dummy");

       Throwable t = new NullPointerException("Something's null\nSomewhere...");
       Throwable t2 = new ClassCastException("BadOldClass");
       t.initCause(t2);

       GuiUtils.showErrorMessage(logger, "This is some major error here!!\nYou must have messed up there...\nOh well!", t, null);

       GuiUtils.showInfoMessage(logger, "Not much up", "Just keeping you informed.\nSafe home now.", null);

   }



}
