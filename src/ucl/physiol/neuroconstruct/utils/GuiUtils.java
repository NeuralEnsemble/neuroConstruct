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

package ucl.physiol.neuroconstruct.utils;

import java.awt.*;
import javax.swing.*;

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

    private GuiUtils()
    {
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

        StringBuffer fullError = new StringBuffer();
        if (logger!=null)
        {
            logger.logError("User being informed of error");
            logger.logError(errorMessage, t);
        }

        Throwable nextThrowable = t;
        int numLines = 0;
        int maxLines = 10;

        while (nextThrowable !=null)
        {
            fullError.append(toSimpleHtmlSmall(nextThrowable.getClass().getName() + ": " + nextThrowable.getMessage()));
            fullError.append("<br >");
            StackTraceElement[] ste =  nextThrowable.getStackTrace();
            for (int i = 0; i < ste.length; i++)
            {
                if (numLines<maxLines)
                {
                    fullError.append(toSimpleHtmlSmall(ste[i].toString()) + "<br>");
                }
                else if (numLines==maxLines)
                {
                    fullError.append(toSimpleHtmlSmall("More ...")+"<br>");
                }
                numLines++;

            }
            nextThrowable = nextThrowable.getCause();
            fullError.append( "<br>");
        }


        StringBuffer htmlMessage = new StringBuffer();

        htmlMessage.append("<html><b>");


        String[] lines = errorMessage.split("\\n");
        for (int i = 0; i < lines.length; i++)
        {
            int maxLength = 200;
            String truncated =  lines[i];
            if (truncated.length()>maxLength)
                truncated = truncated.substring(0,maxLength)+"...";

            htmlMessage.append(truncated);
            if (i != lines.length - 1)
                htmlMessage.append("<br>");
            else
                htmlMessage.append("</b>");
        }



        if (t != null)
            htmlMessage.append("<br><br>" + fullError.toString());

        htmlMessage.append("</html>");
        if (logger!=null)
        {
            logger.logComment("HTML to show: " + htmlMessage.toString());
        }
        JOptionPane.showMessageDialog(parent, htmlMessage.toString(), "Error",
                                      JOptionPane.ERROR_MESSAGE);

    }
    
    private static void showMessageDialog(Component parent, String message, String title, int type)
    {
    	// todo: make this default message showing dialog...
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
           logger.logComment("User being passed info");
           logger.logComment(message);
       }

       JOptionPane.showMessageDialog(parent,
                                     toSimpleHtmlWrapped(message),
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

       JOptionPane.showMessageDialog(parent,
                                     toSimpleHtmlWrapped(message),
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


   public static String toSimpleHtmlWrapped(String message)
   {
       StringBuffer htmlMessage = new StringBuffer();
       htmlMessage.append("<html><b>");

       htmlMessage.append(toSimpleHtmlMain(message));

       htmlMessage.append("</b></html>");
       return htmlMessage.toString();
   }

   public static String toSimpleHtmlSmall(String message)
   {
       StringBuffer htmlBit = new StringBuffer();
       htmlBit.append("<font size =\"2\">");
       String[] lines = message.split("\\n");
       for (int i = 0; i < lines.length; i++)
       {
           htmlBit.append(lines[i]);
           if (i != lines.length - 1)
               htmlBit.append("<br>");
       }
       htmlBit.append("</font>");
       return htmlBit.toString();
   }


   public static String toSimpleHtmlMain(String message)
   {
       StringBuffer htmlBit = new StringBuffer();
       htmlBit.append("<b>");
       String[] lines = message.split("\\n");
       for (int i = 0; i < lines.length; i++)
       {
           htmlBit.append(lines[i]);
           if (i != lines.length - 1)
               htmlBit.append("<br>");
       }
       htmlBit.append("</b>");
       return htmlBit.toString();
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
       GuiUtils.showErrorMessage(logger, "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTOOPPPPPPPPPPPPPPPPPPPPPPPPPP", null, null);

   }





}
